/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.performance.results;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.gradle.performance.results.AbstractTablePageGenerator.Tag.FAILED;
import static org.gradle.performance.results.AbstractTablePageGenerator.Tag.FROM_CACHE;
import static org.gradle.performance.results.AbstractTablePageGenerator.Tag.IMPROVED;
import static org.gradle.performance.results.AbstractTablePageGenerator.Tag.NEARLY_FAILED;
import static org.gradle.performance.results.AbstractTablePageGenerator.Tag.REGRESSED;
import static org.gradle.performance.results.AbstractTablePageGenerator.Tag.UNKNOWN;
import static org.gradle.performance.results.AbstractTablePageGenerator.Tag.UNTAGGED;

public class IndexPageGenerator extends AbstractTablePageGenerator {
    public static final int DEFAULT_RETRY_COUNT = 3;

    public IndexPageGenerator(ResultsStore resultsStore, File resultJson) {
        super(resultsStore, resultJson);
    }

    @Override
    protected int howManyMostRecentExecutionsRead() {
        return DEFAULT_RETRY_COUNT;
    }

    @Override
    public void render(final ResultsStore store, Writer writer) throws IOException {
        long successCount = scenarios.stream().filter(ScenarioBuildResultData::isSuccessful).count();
        long failureCount = scenarios.size() - successCount;

        new TableHtml(writer) {
            @Override
            protected String getPageTitle() {
                return "Profile report for channel " + ResultsStoreHelper.determineChannel();
            }

            @Override
            protected String getTableTitle() {
                StringBuilder sb = new StringBuilder("Scenarios (").append(successCount).append(" successful");
                if (failureCount > 0) {
                    sb.append(", ").append(failureCount).append(" failed");
                }
                sb.append(")");
                return sb.toString();
            }

            @Override
            protected boolean renderFailureSelectButton() {
                return failureCount > 0;
            }

            @Override
            protected List<ScenarioBuildResultData> getCrossVersionScenarios() {
                return scenarios.stream().filter(ScenarioBuildResultData::isCrossVersion).collect(toList());
            }

            @Override
            protected List<ScenarioBuildResultData> getCrossBuildScenarios() {
                return scenarios.stream().filter(ScenarioBuildResultData::isCrossBuild).collect(toList());
            }

            @Override
            protected String determineScenarioBackgroundColorCss(ScenarioBuildResultData scenario) {
                if (scenario.isUnknown()) {
                    return "alert-dark";
                } else if (!scenario.isSuccessful()) {
                    return "alert-danger";
                } else if (scenario.isAboutToRegress()) {
                    return "alert-warning";
                } else if (scenario.isImproved()) {
                    return "alert-success";
                } else {
                    return "alert-info";
                }
            }

            @Override
            protected Set<Tag> determineTags(ScenarioBuildResultData scenario) {
                Set<Tag> result = new HashSet<>();
                if (scenario.isFromCache()) {
                    result.add(FROM_CACHE);
                }
                if (scenario.isUnknown()) {
                    result.add(UNKNOWN);
                } else if (scenario.isBuildFailed()) {
                    result.add(FAILED);
                } else if (scenario.isRegressed()) {
                    result.add(REGRESSED);
                } else if (scenario.isAboutToRegress()) {
                    result.add(NEARLY_FAILED);
                } else if (scenario.isImproved()) {
                    result.add(IMPROVED);
                }

                if (result.isEmpty()) {
                    result.add(UNTAGGED);
                }
                return result;
            }

            @Override
            protected void renderScenarioButtons(int index, ScenarioBuildResultData scenario) {
                a().target("_blank").classAttr("btn btn-primary btn-sm").href(scenario.getWebUrl()).text("Build").end();
            }
        };
    }

}
