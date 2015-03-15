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

package org.terasology.master.dbip;

import org.terasology.master.GeoLocation;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class DbIpQueryResponse implements GeoLocation {

    private String error;
    private String address;
    private String country;
    private String stateprov;
    private String city;

    public boolean isSuccess() {
        return error == null;
    }

    public String getError() {
        return error;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public String getCountry() {
        return country;
    }

    @Override
    public String getStateOrProvince() {
        return stateprov;
    }

    @Override
    public String getCity() {
        return city;
    }

    @Override
    public String toString() {
        return String.format("Respone [error=%s, address=%s, country=%s, stateprov=%s, city=%s]", error, address, country, stateprov, city);
    }
}