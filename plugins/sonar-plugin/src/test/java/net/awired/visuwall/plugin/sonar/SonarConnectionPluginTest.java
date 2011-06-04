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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.awired.visuwall.api.domain.ProjectId;
import net.awired.visuwall.api.domain.TestResult;
import net.awired.visuwall.api.domain.quality.QualityMeasure;
import net.awired.visuwall.api.domain.quality.QualityMetric;
import net.awired.visuwall.api.domain.quality.QualityResult;
import net.awired.visuwall.plugin.sonar.exception.SonarMeasureNotFoundException;
import net.awired.visuwall.plugin.sonar.exception.SonarMetricsNotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class SonarConnectionPluginTest {

    Map<String, QualityMetric> metricList = createMetricList();

    MetricFinder metricFinder = Mockito.mock(MetricFinder.class);
    MeasureFinder measureFinder = Mockito.mock(MeasureFinder.class);

    @Before
    public void init() throws SonarMetricsNotFoundException {
        when(metricFinder.findMetrics()).thenReturn(metricList);
    }

    @Test
    public void should_create_quality_measure() throws SonarMeasureNotFoundException {
        ProjectId projectId = new ProjectId();
        projectId.setArtifactId("artifactId");

        QualityMeasure coverageMeasure = new QualityMeasure();
        coverageMeasure.setName("Coverage");
        coverageMeasure.setFormattedValue("5%");
        coverageMeasure.setValue(5D);
        coverageMeasure.setKey("coverage");
        when(measureFinder.findQualityMeasure("artifactId", "coverage")).thenReturn(coverageMeasure);

        SonarConnectionPlugin sonarPlugin = new SonarConnectionPlugin(measureFinder, metricFinder);

        QualityResult qualityResult = sonarPlugin.analyzeQuality(projectId);
        QualityMeasure freshCoverageMeasure = qualityResult.getMeasure("coverage");

        assertEquals(coverageMeasure.getKey(), freshCoverageMeasure.getKey());
        assertEquals(coverageMeasure.getName(), freshCoverageMeasure.getName());
        assertEquals(coverageMeasure.getValue(), freshCoverageMeasure.getValue());
        assertEquals(coverageMeasure.getFormattedValue(), freshCoverageMeasure.getFormattedValue());
    }

    @Test
    public void should_find_project() {
        SonarConnectionPlugin sonarPlugin = new SonarConnectionPlugin(measureFinder, metricFinder);

        ProjectId projectId = new ProjectId();
        projectId.setArtifactId("artifactId");

        assertTrue(sonarPlugin.contains(projectId));
    }

    @Test
    public void should_not_find_project() throws SonarMeasureNotFoundException {
        SonarConnectionPlugin sonarPlugin = new SonarConnectionPlugin(measureFinder, metricFinder);

        when(measureFinder.findMeasure(Matchers.anyString(), Matchers.anyString())).thenThrow(
                new SonarMeasureNotFoundException(""));

        ProjectId projectId = new ProjectId();
        projectId.setArtifactId("artifactId");

        assertFalse(sonarPlugin.contains(projectId));
    }

    @Test
    public void should_return_false_when_no_artifact_id_found() {
        SonarConnectionPlugin sonarPlugin = new SonarConnectionPlugin(measureFinder, metricFinder);
        assertFalse(sonarPlugin.contains(new ProjectId()));
    }

    @Test
    public void should_build_valid_unit_test_result() throws SonarMeasureNotFoundException {
        SonarConnectionPlugin sonarPlugin = new SonarConnectionPlugin(measureFinder, metricFinder);

        when(measureFinder.findMeasureValue("artifactId", "coverage")).thenReturn(8D);

        ProjectId projectId = new ProjectId();
        projectId.setArtifactId("artifactId");

        TestResult unitTestResult = sonarPlugin.analyzeUnitTests(projectId);

        assertEquals(8, unitTestResult.getCoverage(), 0);
    }

    @Test
    public void should_build_valid_integration_test_result() throws SonarMeasureNotFoundException {
        SonarConnectionPlugin sonarPlugin = new SonarConnectionPlugin(measureFinder, metricFinder);

        when(measureFinder.findMeasureValue("artifactId", "it_coverage")).thenReturn(8D);

        ProjectId projectId = new ProjectId();
        projectId.setArtifactId("artifactId");

        TestResult integrationTestResult = sonarPlugin.analyzeIntegrationTests(projectId);

        assertEquals(8, integrationTestResult.getCoverage(), 0);
    }

    @Test
    /***
     * <metric>
    	<key>generated_lines</key>
    	<name>Generated Lines</name>
    	<description>Number of generated lines</description>
    	<domain>Size</domain>
    	<qualitative>false</qualitative>
    	<direction>-1</direction>
    	<user_managed>false</user_managed>
    	<val_type>INT</val_type>
    	<hidden>false</hidden>
    </metric>
     */
    public void should_build_valid_metric_map() throws SonarMetricsNotFoundException {
        QualityMetric generatedLinesMetric = new QualityMetric();
        generatedLinesMetric.setKey("generated_lines");
        generatedLinesMetric.setName("Generated Lines");
        generatedLinesMetric.setDescription("Number of generated lines");
        generatedLinesMetric.setDomain("Size");
        generatedLinesMetric.setQualitative(false);
        generatedLinesMetric.setDirection(-1);
        generatedLinesMetric.setUserManaged(false);
        generatedLinesMetric.setValTyp("INT");
        generatedLinesMetric.setHidden(false);

        Map<String, QualityMetric> qualityMetrics = new HashMap<String, QualityMetric>();
        qualityMetrics.put("size", generatedLinesMetric);
        when(metricFinder.findMetrics()).thenReturn(qualityMetrics);

        SonarConnectionPlugin sonarPlugin = new SonarConnectionPlugin(measureFinder, metricFinder);
        Map<String, List<QualityMetric>> metrics = sonarPlugin.getMetricsByCategory();
        List<QualityMetric> sizeMetrics = metrics.get("Size");

        assertEquals(generatedLinesMetric, sizeMetrics.get(0));
    }

    private Map<String, QualityMetric> createMetricList() {
        Map<String, QualityMetric> metricList = new HashMap<String, QualityMetric>();
        QualityMetric coverageMetric = new QualityMetric();
        coverageMetric.setKey("coverage");
        coverageMetric.setName("Coverage");
        metricList.put(coverageMetric.getKey(), coverageMetric);
        return metricList;
    }

}