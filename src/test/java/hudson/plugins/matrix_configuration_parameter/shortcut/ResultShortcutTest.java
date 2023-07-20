/*
 * The MIT License
 *
 * Copyright (c) 2016 IKEDA Yasuyuki
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.plugins.matrix_configuration_parameter.shortcut;

import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

import org.htmlunit.html.HtmlPage;

import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.Item;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import hudson.plugins.matrix_configuration_parameter.ConditionalFailBuilder;
import hudson.plugins.matrix_configuration_parameter.ConditionalUnstablePublisher;
import hudson.plugins.matrix_configuration_parameter.MatrixCombinationsJenkinsRule;
import hudson.plugins.matrix_configuration_parameter.MatrixCombinationsParameterDefinition;

/**
 * Tests for {@link ResultShortcut}
 */
public class ResultShortcutTest {
    @ClassRule
    public static MatrixCombinationsJenkinsRule j = new MatrixCombinationsJenkinsRule();

    @Test
    public void testConfigurationWithSingleResult() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        MatrixCombinationsParameterDefinition def = new MatrixCombinationsParameterDefinition(
            "COMBINATIONS",
            "",
            "",
            Arrays.<MatrixCombinationsShortcut>asList(new ResultShortcut(
                "ABORTED",
                false,
                Result.ABORTED
            ))
        );
        p.addProperty(new ParametersDefinitionProperty(def));

        j.configRoundtrip((Item)p);

        j.assertEqualDataBoundBeans(
            def,
            p.getProperty(ParametersDefinitionProperty.class).getParameterDefinition("COMBINATIONS")
        );
    }

    @Test
    public void testConfigurationWithMultipleResult() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        MatrixCombinationsParameterDefinition def = new MatrixCombinationsParameterDefinition(
            "COMBINATIONS",
            "",
            "",
            Arrays.<MatrixCombinationsShortcut>asList(new ResultShortcut(
                "NotSuccessful",
                false,
                Result.UNSTABLE,
                Result.FAILURE
            ))
        );
        p.addProperty(new ParametersDefinitionProperty(def));

        j.configRoundtrip((Item)p);

        j.assertEqualDataBoundBeans(
            def,
            p.getProperty(ParametersDefinitionProperty.class).getParameterDefinition("COMBINATIONS")
        );
    }

    @Test
    public void testCheckSingleResult() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        MatrixCombinationsParameterDefinition def = new MatrixCombinationsParameterDefinition(
            "COMBINATIONS",
            "",
            "",
            Arrays.<MatrixCombinationsShortcut>asList(new ResultShortcut(
                "FAILURE",
                false,
                Result.FAILURE
            ))
        );
        p.addProperty(new ParametersDefinitionProperty(def));
        p.getBuildersList().add(new ConditionalFailBuilder("${axis1}", "value2"));
        p.getPublishersList().add(new ConditionalUnstablePublisher("${axis1}", "value3"));

        MatrixBuild b = p.scheduleBuild2(0).get();

        j.assertBuildStatus(Result.SUCCESS, b.getExactRun(new Combination(axes, "value1")));
        j.assertBuildStatus(Result.FAILURE, b.getExactRun(new Combination(axes, "value2")));
        j.assertBuildStatus(Result.UNSTABLE, b.getExactRun(new Combination(axes, "value3")));

        WebClient wc = j.createAllow405WebClient();
        HtmlPage page = wc.getPage(p, "build");

        j.clickShortcut(page, "FAILURE");

        j.assertCombinationChecked(page, false, axes, "value1");
        j.assertCombinationChecked(page, true, axes, "value2");
        j.assertCombinationChecked(page, false, axes, "value3");
    }

    @Test
    public void testCheckMultipleResult() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        MatrixCombinationsParameterDefinition def = new MatrixCombinationsParameterDefinition(
            "COMBINATIONS",
            "",
            "",
            Arrays.<MatrixCombinationsShortcut>asList(new ResultShortcut(
                "UNSTABLE-FAILURE",
                false,
                Result.UNSTABLE,
                Result.FAILURE
            ))
        );
        p.addProperty(new ParametersDefinitionProperty(def));
        p.getBuildersList().add(new ConditionalFailBuilder("${axis1}", "value2"));
        p.getPublishersList().add(new ConditionalUnstablePublisher("${axis1}", "value3"));

        MatrixBuild b = p.scheduleBuild2(0).get();

        j.assertBuildStatus(Result.SUCCESS, b.getExactRun(new Combination(axes, "value1")));
        j.assertBuildStatus(Result.FAILURE, b.getExactRun(new Combination(axes, "value2")));
        j.assertBuildStatus(Result.UNSTABLE, b.getExactRun(new Combination(axes, "value3")));

        WebClient wc = j.createAllow405WebClient();
        HtmlPage page = wc.getPage(p, "build");

        j.clickShortcut(page, "UNSTABLE-FAILURE");

        j.assertCombinationChecked(page, false, axes, "value1");
        j.assertCombinationChecked(page, true, axes, "value2");
        j.assertCombinationChecked(page, true, axes, "value3");
    }

    @Test
    public void testExact() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        MatrixCombinationsParameterDefinition def = new MatrixCombinationsParameterDefinition(
            "COMBINATIONS",
            "",
            "",
            Arrays.<MatrixCombinationsShortcut>asList(new ResultShortcut(
                "SUCCESS",
                true,
                Result.SUCCESS
            ))
        );
        p.addProperty(new ParametersDefinitionProperty(def));
        p.getBuildersList().add(new ConditionalFailBuilder("${axis1}", "value1"));

        MatrixBuild b1 = p.scheduleBuild2(0).get();

        j.assertBuildStatus(Result.FAILURE, b1.getExactRun(new Combination(axes, "value1")));
        j.assertBuildStatus(Result.SUCCESS, b1.getExactRun(new Combination(axes, "value2")));
        j.assertBuildStatus(Result.SUCCESS, b1.getExactRun(new Combination(axes, "value3")));

        p.setCombinationFilter("axis1 != 'value3'");

        MatrixBuild b2 = p.scheduleBuild2(0).get();

        j.assertBuildStatus(Result.FAILURE, b2.getExactRun(new Combination(axes, "value1")));
        j.assertBuildStatus(Result.SUCCESS, b2.getExactRun(new Combination(axes, "value2")));
        assertNull(b2.getExactRun(new Combination(axes, "value3")));

        p.setCombinationFilter("");

        WebClient wc = j.createAllow405WebClient();
        HtmlPage page = wc.getPage(p, "build");

        j.clickShortcut(page, "SUCCESS");

        j.assertCombinationChecked(page, false, axes, "value1");
        j.assertCombinationChecked(page, true, axes, "value2");
        j.assertCombinationChecked(page, false, axes, "value3");
    }


    @Test
    public void testNotExact() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        MatrixCombinationsParameterDefinition def = new MatrixCombinationsParameterDefinition(
            "COMBINATIONS",
            "",
            "",
            Arrays.<MatrixCombinationsShortcut>asList(new ResultShortcut(
                "SUCCESS",
                false,
                Result.SUCCESS
            ))
        );
        p.addProperty(new ParametersDefinitionProperty(def));
        p.getBuildersList().add(new ConditionalFailBuilder("${axis1}", "value1"));

        MatrixBuild b1 = p.scheduleBuild2(0).get();

        j.assertBuildStatus(Result.FAILURE, b1.getExactRun(new Combination(axes, "value1")));
        j.assertBuildStatus(Result.SUCCESS, b1.getExactRun(new Combination(axes, "value2")));
        j.assertBuildStatus(Result.SUCCESS, b1.getExactRun(new Combination(axes, "value3")));

        p.setCombinationFilter("axis1 != 'value3'");

        MatrixBuild b2 = p.scheduleBuild2(0).get();

        j.assertBuildStatus(Result.FAILURE, b2.getExactRun(new Combination(axes, "value1")));
        j.assertBuildStatus(Result.SUCCESS, b2.getExactRun(new Combination(axes, "value2")));
        assertNull(b2.getExactRun(new Combination(axes, "value3")));

        p.setCombinationFilter("");

        WebClient wc = j.createAllow405WebClient();
        HtmlPage page = wc.getPage(p, "build");

        j.clickShortcut(page, "SUCCESS");

        j.assertCombinationChecked(page, false, axes, "value1");
        j.assertCombinationChecked(page, true, axes, "value2");
        j.assertCombinationChecked(page, true, axes, "value3");
    }
}
