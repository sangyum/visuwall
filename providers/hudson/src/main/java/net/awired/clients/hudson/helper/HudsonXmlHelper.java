/**
 *     Copyright (C) 2010 Julien SMADJA <julien dot smadja at gmail dot com>
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

package net.awired.clients.hudson.helper;

import java.util.List;
import net.awired.clients.hudson.resource.Build;
import net.awired.clients.hudson.resource.Culprit;
import net.awired.clients.hudson.resource.MavenModuleSet;
import com.google.common.base.Preconditions;

public class HudsonXmlHelper {

    private HudsonXmlHelper() {
    }

    public static boolean isSuccessful(Build setBuild) {
        checkSetBuild(setBuild);
        String state = setBuild.getResult();
        return "SUCCESS".equals(state);
    }

    public static String[] getCommiterNames(Build setBuild) {
        checkSetBuild(setBuild);
        List<Culprit> users = setBuild.getCulprits();
        String[] commiters = new String[users.size()];
        for (int i = 0; i < users.size(); i++) {
            Culprit hudsonModelUser = users.get(i);
            String name = hudsonModelUser.getFullName();
            commiters[i] = name;
        }
        return commiters;
    }

    public static boolean getIsBuilding(MavenModuleSet modelJob) {
        String color = modelJob.getColor();
        return color.endsWith("_anime");
    }

    private static void checkSetBuild(Build setBuild) {
        Preconditions.checkNotNull(setBuild, "setBuild is mandatory");
    }

}
