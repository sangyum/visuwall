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

package net.awired.clients.jenkins;

import net.awired.clients.Urls;
import net.awired.clients.hudson.HudsonRootModuleFinder;
import net.awired.clients.hudson.HudsonUrlBuilder;
import org.junit.Assert;
import org.junit.Test;

public class JenkinsRootModuleFinderIT {

    @Test
    public void should_find_synthesis_root_module_from_jenkins() throws Exception {
        HudsonUrlBuilder hudsonUrlBuilder = new HudsonUrlBuilder(Urls.AWIRED_JENKINS);
        HudsonRootModuleFinder hudsonRootModuleFinder = new HudsonRootModuleFinder(hudsonUrlBuilder);
        String artifactId = hudsonRootModuleFinder.findArtifactId("struts 2 instable");
        Assert.assertEquals("org.apache.struts:struts2-parent", artifactId);
    }
}
