/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.web.geo.GeoLocation;
import org.terasology.web.geo.GeoLocationService;
import org.terasology.web.geo.dbip.GeoLocationServiceDbIp;

/**
 * @author Martin Steiger
 */
public final class JooqDatabase implements DataBase {

    private static final Logger logger = LoggerFactory.getLogger(JooqDatabase.class);
    private final String dbUri;


    /**
     * @param dbUri the database URI in the form <code>jdbc:mysql://server/db?user=[user]&password=[pass]</code>.
     */
    public JooqDatabase(String dbUri) {
        this.dbUri = dbUri;

        logger.info("SQL dialect detected: {}", DSL.using(dbUri).configuration().dialect());
    }

    @Override
    public boolean remove(String tableName, String address, int port) throws SQLException {

        try (Connection conn = DriverManager.getConnection(dbUri)) {
            DSLContext context = DSL.using(conn);
            Table<Record> table = DSL.table(DSL.name(tableName));

            Field<Object> addressField = DSL.field(DSL.name("address"));
            Field<Object> portField = DSL.field(DSL.name("port"));

            // "DELETE FROM <tableName> WHERE address='<address>' AND port=<port>;"
            Query q = context.deleteFrom(table).where(addressField.eq(address).and(portField.eq(port)));
            int affected = q.execute();

            // If everything went well, exactly 1 row should have been affected (=removed)
            return (affected == 1);
        }
    }

    @Override
    public List<Map<String, Object>> readAll(String tableName) throws SQLException {

        try (Connection conn = DriverManager.getConnection(dbUri)) {
            DSLContext context = DSL.using(conn);
            Table<Record> table = DSL.table(DSL.name(tableName));

            Result<Record> content = context.select().from(table).fetch();
            List<Map<String, Object>> entries = new ArrayList<>(content.size());

            for (Record record : content) {
                Map<String, Object> entry = new LinkedHashMap<>();

                for (int i = 0; i < record.size(); i++) {
                    entry.put(content.field(i).getName(), record.getValue(i));
                }
                entries.add(entry);
            }

            return entries;
        }
    }

    @Override
    public boolean insert(String tableName, String name, String address, int port, String owner) throws SQLException {

        try (Connection conn = DriverManager.getConnection(dbUri)) {
            DSLContext context = DSL.using(conn);
            Table<?> table = tableExists(context, tableName);
            if (table == null) {
                table = DSL.table(DSL.name(tableName));
                createTable(context, table);
            }

            InsertSetMoreStep<?> statement = context.insertInto(table)
                .set(DSL.field(DSL.name("name")), name)
                .set(DSL.field(DSL.name("address")), address)
                .set(DSL.field(DSL.name("port")), port)
                .set(DSL.field(DSL.name("owner")), owner);

            GeoLocationService geoService = new GeoLocationServiceDbIp();
            try {
                GeoLocation geoLoc = geoService.resolve(address);
                String country = geoLoc.getCountry();
                String stateProv = geoLoc.getStateOrProvince();
                String city = geoLoc.getCity();

                statement
                    .set(DSL.field(DSL.name("country")), country)
                    .set(DSL.field(DSL.name("stateprov")), stateProv)
                    .set(DSL.field(DSL.name("city")), city);

            } catch (IOException e) {
                logger.error("Could not resolve geo-location for {}", address, e);
            }

            int affected = statement.execute();
            logger.info("Complete - {} rows affected", affected);
            return (affected == 1);
        }
    }

    private void createTable(DSLContext context, Table<?> table) {

        context.createTable(table)
            .column("name", SQLDataType.VARCHAR.length(256))
            .column("address", SQLDataType.VARCHAR.length(256).nullable(false))
            .column("port", SQLDataType.INTEGER.nullable(false))
            .column("country", SQLDataType.VARCHAR.length(256))
            .column("stateprov", SQLDataType.VARCHAR.length(256))
            .column("city", SQLDataType.VARCHAR.length(256))
            .column("owner", SQLDataType.VARCHAR.length(256))
            .column("modtime", SQLDataType.TIMESTAMP)
            .execute();

        // modtime timestamp DEFAULT current_timestamp
        context.alterTable(table)
            .alter(DSL.field(DSL.name("modtime"), Timestamp.class)).defaultValue(DSL.currentTimestamp())
            .execute();

        // PRIMARY KEY (address, port)
        context.alterTable(table)
            .add(DSL.constraint("primary_key").primaryKey("address", "port"))
            .execute();
    }

    private Table<?> tableExists(DSLContext context, String tableName) {
        for (Table<?> tab : context.meta().getTables()) {
            if (tab.getName().equals(tableName)) {
                return tab;
            }
        }
        return null;
    }

    @Override
    public boolean update(String tableName, String name, String address, int port, String owner) throws SQLException {

        try (Connection conn = DriverManager.getConnection(dbUri)) {
            DSLContext context = DSL.using(conn);
            Table<Record> table = DSL.table(DSL.name(tableName));

            UpdateSetMoreStep<Record> statement = context.update(table)
                .set(DSL.field(DSL.name("name")), name)
                .set(DSL.field(DSL.name("owner")), owner);

            GeoLocationService geoService = new GeoLocationServiceDbIp();
            try {
                GeoLocation geoLoc = geoService.resolve(address);
                String country = geoLoc.getCountry();
                String stateProv = geoLoc.getStateOrProvince();
                String city = geoLoc.getCity();

                statement
                    .set(DSL.field(DSL.name("country")), country)
                    .set(DSL.field(DSL.name("stateprov")), stateProv)
                    .set(DSL.field(DSL.name("city")), city);

            } catch (IOException e) {
                logger.error("Could not resolve geo-location for {}", address, e);
            }

            Field<Object> addressField = DSL.field(DSL.name("address"));
            Field<Object> portField = DSL.field(DSL.name("port"));

            int affected = statement
                    .where(addressField.eq(address).and(portField.eq(port)))
                    .execute();

            return (affected == 1);
        }
    }

}
