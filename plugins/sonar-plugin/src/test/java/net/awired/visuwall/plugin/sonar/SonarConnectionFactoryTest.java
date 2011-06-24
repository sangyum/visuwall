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

package net.awired.visuwall.plugin.sonar;

import static org.junit.Assert.assertFalse;
import net.awired.visuwall.api.exception.ConnectionException;

import org.junit.Test;

public class SonarConnectionFactoryTest {

    SonarConnectionFactory sonarConnectionFactory = new SonarConnectionFactory();

    @Test(expected = NullPointerException.class)
    public void can_t_pass_null_to_create_method() throws ConnectionException {
        sonarConnectionFactory.create(null);
    }

    @Test
    public void should_create_sonar_connection() throws ConnectionException {
        SonarConnection connection = sonarConnectionFactory.create("http://sonar:9000");
        assertFalse(connection.isClosed());
    }

}
