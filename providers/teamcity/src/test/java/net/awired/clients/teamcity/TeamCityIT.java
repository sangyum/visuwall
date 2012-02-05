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

package net.awired.clients.teamcity;

import java.util.concurrent.TimeUnit;

import net.awired.clients.teamcity.resource.TeamCityBuild;

import org.junit.Test;

public class TeamCityIT {

    @Test
    public void test() throws Exception {
        TeamCity teamcity = new TeamCity("http://localhost:8111", "guest", "");
        while (true) {
            System.out.println("last build:" + teamcity.findLastBuild("bt15").getId());

            TeamCityBuild build = teamcity.findRunningBuild();
            System.out.println(build.getId() + " " + build.isRunning());
            TimeUnit.SECONDS.sleep(1);
        }
    }

}
