/**
 *     Copyright (C) 2010 Julien SMADJA <julien dot smadja at gmail dot com> - Arnaud LEMAIRE <alemaire at norad dot fr>
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.awired.visuwall.api.plugin;

import java.net.URL;
import java.util.Map;

import net.awired.visuwall.api.domain.SoftwareId;
import net.awired.visuwall.api.exception.ConnectionException;
import net.awired.visuwall.api.exception.SoftwareNotFoundException;
import net.awired.visuwall.api.plugin.capability.BasicCapability;

public interface VisuwallPlugin<T extends BasicCapability> {

    T getConnection(URL url, Map<String, String> properties) throws ConnectionException;

    /** login, password, ... */
    Map<String, String> getPropertiesWithDefaultValue();

    Class<T> getConnectionClass();

    float getVersion();

    String getName();

    SoftwareId getSoftwareId(URL url, Map<String, String> properties) throws SoftwareNotFoundException;

}
