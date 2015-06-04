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

package org.terasology.web.model;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.AbstractList;
import java.util.List;

import org.terasology.web.artifactory.ArtifactoryItem;
import org.terasology.web.artifactory.ArtifactoryRepo;
import org.terasology.web.artifactory.ModuleInfo;

/**
 * Provides a list of modules.
 */
public class ModuleListModelImpl implements ModuleListModel {

    private ArtifactoryRepo a;

    public ModuleListModelImpl() throws IOException {
        String host = "http://artifactory.terasology.org/artifactory";
        a = new ArtifactoryRepo(host, "terasology-snapshot-local", Paths.get("cache").normalize());
    }

    @Override
    public List<ModuleInfo> findModules() throws IOException {
        return new AbstractList<ModuleInfo>() {

            @Override
            public ModuleInfo get(int index) {
                ArtifactoryItem artifactoryItem = a.getModules().get(index);
                return new ModuleInfo(artifactoryItem.downloadUri);
            }

            @Override
            public int size() {
                return a.getModules().size();
            }
        };
    }

}
