/**
 * Copyright (C) 2010 Julien SMADJA <julien.smadja@gmail.com> - Arnaud LEMAIRE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jsmadja.wall.projectwall.domain;

import com.google.common.base.Objects;

public class Project {

    private String name;
    private String description;
    private double coverage;
    private double rulesCompliance;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public double getCoverage() {
        return coverage;
    }
    public void setCoverage(double coverage) {
        this.coverage = coverage;
    }
    public double getRulesCompliance() {
        return rulesCompliance;
    }
    public void setRulesCompliance(double rulesCompliance) {
        this.rulesCompliance = rulesCompliance;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
        .add("name", name)
        .add("description", description)
        .add("coverage", coverage)
        .add("rules compliance", rulesCompliance)
        .toString();
    }

}
