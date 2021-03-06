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

import java.util.Collection;
import java.util.Set;

import org.terasology.module.Module;
import org.terasology.naming.Name;
import org.terasology.naming.Version;

/**
 * Provides a list of modules.
 */
public interface ModuleListModel {

    Set<Name> getModuleIds();

    Collection<Module> getModuleVersions(Name module);

    Module getModule(Name module, Version version);

    Module getLatestModuleVersion(Name name);

    Set<Module> resolve(Name name, Version version);

    void updateModule(Name module);

    void updateAllModules();
}
