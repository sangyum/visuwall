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

package net.awired.visuwall.plugin.jenkins;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import net.awired.visuwall.api.domain.Build;
import net.awired.visuwall.api.domain.Project;
import net.awired.visuwall.api.domain.ProjectStatus.State;
import net.awired.visuwall.hudsonclient.domain.HudsonBuild;
import net.awired.visuwall.hudsonclient.domain.HudsonProject;
import net.awired.visuwall.plugin.jenkins.ProjectBuilder;

import org.junit.Test;

public class ProjectBuilderTest {

    private ProjectBuilder projectBuilder = new ProjectBuilder();

    @Test
    public void should_not_fail_if_state_doesnt_exist() {
        HudsonBuild hudsonBuild = new HudsonBuild();
        hudsonBuild.setState("NOT_EXIST_BUILT");
        Build build = projectBuilder.buildBuildFrom(hudsonBuild);
        assertEquals(State.UNKNOWN, build.getState());
    }

    @Test
    public void should_not_fail_if_state_is_null() {
        HudsonBuild hudsonBuild = new HudsonBuild();
        hudsonBuild.setState(null);
        Build build = projectBuilder.buildBuildFrom(hudsonBuild);
        assertEquals(State.UNKNOWN, build.getState());
    }

    @Test
    public void should_set_valid_state() {
        HudsonBuild hudsonBuild = new HudsonBuild();
        hudsonBuild.setState(State.ABORTED.name());
        Build build = projectBuilder.buildBuildFrom(hudsonBuild);
        assertEquals(State.ABORTED, build.getState());
    }

    @Test
    public void should_set_valid_state_case_insensitive() {
        HudsonBuild hudsonBuild = new HudsonBuild();
        hudsonBuild.setState(State.ABORTED.name().toLowerCase());
        Build build = projectBuilder.buildBuildFrom(hudsonBuild);
        assertEquals(State.ABORTED, build.getState());
    }

    @Test
    public void should_add_current_build_and_completed_build() {
        HudsonBuild currentBuild = new HudsonBuild();
        currentBuild.setCommiters(new String[]{"commiter1"});

        HudsonBuild completedBuild = new HudsonBuild();
        completedBuild.setCommiters(new String[]{"commiter2"});

        HudsonProject hudsonProject = new HudsonProject();
        hudsonProject.setCurrentBuild(currentBuild);
        hudsonProject.setCompletedBuild(completedBuild);

        Project project = new Project("");
        projectBuilder.addCurrentAndCompletedBuilds(project, hudsonProject);

        assertEquals("commiter1", project.getCurrentBuild().getCommiters()[0]);
        assertEquals("commiter2", project.getCompletedBuild().getCommiters()[0]);
    }

    @Test
    public void should_build_valid_project() {
        HudsonProject hudsonProject = new HudsonProject();
        hudsonProject.setDescription("description");
        hudsonProject.setName("name");
        int[] buildNumbers = new int[]{1,2,3};
        hudsonProject.setBuildNumbers(buildNumbers );

        Project project = projectBuilder.buildProjectFrom(hudsonProject);

        assertEquals(hudsonProject.getDescription(), project.getDescription());
        assertEquals(hudsonProject.getName(), project.getName());
        assertArrayEquals(buildNumbers, project.getBuildNumbers());
    }
}
