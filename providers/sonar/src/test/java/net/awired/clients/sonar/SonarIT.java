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

package net.awired.clients.sonar;

import static org.junit.Assert.assertFalse;
import net.awired.clients.sonar.resource.Projects;

import org.junit.Test;

public class SonarIT {

    @Test
    public void test() throws Exception {
        Sonar sonar = new Sonar("http://nemo.sonarsource.org");
        Projects projects = sonar.findProjects();
        assertFalse(projects.getProjects().isEmpty());
    }

}
