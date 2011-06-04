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

import static org.junit.Assert.assertFalse;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class JenkinsPluginTest {

    @Test(expected = NullPointerException.class)
    public void should_thrown_an_exception_when_passing_null_to_is_jenkins_instance() {
        new JenkinsPlugin().isManageable(null);
    }

    @Test
    public void should_not_fail_if_url_is_not_manageable() throws MalformedURLException {
        JenkinsPlugin jenkinsPlugin = new JenkinsPlugin();
        String url = "http://www.google.fr";
        boolean manageable = jenkinsPlugin.isManageable(new URL(url));
        assertFalse(manageable);
    }
}