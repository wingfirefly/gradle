/*
 * Copyright 2019 the original author or authors.
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

import com.google.common.collect.ImmutableMap;
import groovy.json.JsonOutput;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FlakinessScenarioPageGenerator extends HtmlPageGenerator<PerformanceTestHistory> {
    private final PerformanceTestHistory testResults;

    public FlakinessScenarioPageGenerator(PerformanceTestHistory testResults) {
        this.testResults = testResults;
    }

    @Override
    public void render(PerformanceTestHistory model, Writer writer) throws IOException {
        List<Graph> graphs = new ArrayList<>();
        // @formatter:off
        new MetricsHtml(writer) {{
            html();
                head();
                    headSection(this);
                    title("Flaky report for "+ testResults.getDisplayName()).end();
                end();
                body();
                    h2().text("Flaky report for " + testResults.getDisplayName()).end();
                    div().id("flot-placeholder").end();
                    graphs.forEach(this::renderGraph);
                end();
            end();
        }

        private void renderGraph(Graph graph) {
            h3().text(graph.title).end();
            div().id(graph.id).classAttr("chart").end();
            script().raw(String.format("$.plot('#%s', %s, %s)", graph.id,graph.getData(), graph.getOptions())).end();
        }
        };
        // @formatter:on
    }

    private static class Graph {
        String id;
        String title;
        List<Line> data;
        List<String> xAxisLabel;

        String getData() {
            return JsonOutput.toJson(data);
        }

        String getOptions() {
            List<List<Object>> ticks = IntStream.range(0, xAxisLabel.size())
                .mapToObj(index -> Arrays.<Object>asList(index, xAxisLabel.get(index)))
                .collect(Collectors.toList());
            return JsonOutput.toJson(ImmutableMap.of("xaxis", ImmutableMap.of("ticks", ticks)));
        }
    }

    private static class Line {
        private static final Map<String, Object> SHOW_TRUE = ImmutableMap.of("show", true);
        private static final Map<String, Object> SHOW_FALSE = ImmutableMap.of("show", false);
        String label;
        List<List<Number>> data = new ArrayList<>();

        public String getLabel() {
            return label;
        }

        public List<List<Number>> getData() {
            return data;
        }

        public boolean getStack() {
            return false;
        }

        public Map getBars() {
            return SHOW_FALSE;
        }

        public Map getLines() {
            return SHOW_TRUE;
        }

        public Map getPoints() {
            return SHOW_TRUE;
        }
    }
}
