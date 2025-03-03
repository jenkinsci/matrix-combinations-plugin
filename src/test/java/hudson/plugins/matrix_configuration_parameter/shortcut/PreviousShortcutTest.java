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

import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.Item;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import hudson.plugins.matrix_configuration_parameter.ConditionalFailBuilder;
import hudson.plugins.matrix_configuration_parameter.MatrixCombinationsJenkinsRule;
import hudson.plugins.matrix_configuration_parameter.MatrixCombinationsParameterDefinition;
import java.util.List;
import org.htmlunit.html.HtmlPage;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

/**
 * Tests for {@link PreviousShortcut}
 */
public class PreviousShortcutTest {
    @ClassRule
    public static MatrixCombinationsJenkinsRule j = new MatrixCombinationsJenkinsRule();

    @Test
    public void testConfiguration() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        MatrixCombinationsParameterDefinition def =
                new MatrixCombinationsParameterDefinition("COMBINATIONS", "", "", List.of(new PreviousShortcut()));
        p.addProperty(new ParametersDefinitionProperty(def));

        j.configRoundtrip((Item) p);

        j.assertEqualDataBoundBeans(
                def, p.getProperty(ParametersDefinitionProperty.class).getParameterDefinition("COMBINATIONS"));
    }

    @Test
    public void testCheckLastBuild() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        MatrixCombinationsParameterDefinition def =
                new MatrixCombinationsParameterDefinition("COMBINATIONS", "", "", List.of(new PreviousShortcut()));
        p.addProperty(new ParametersDefinitionProperty(def));
        p.getBuildersList().add(new ConditionalFailBuilder("${axis1}", "value2"));

        MatrixBuild b1 = p.scheduleBuild2(0).get();

        j.assertBuildStatus(Result.SUCCESS, b1.getExactRun(new Combination(axes, "value1")));
        j.assertBuildStatus(Result.FAILURE, b1.getExactRun(new Combination(axes, "value2")));
        j.assertBuildStatus(Result.SUCCESS, b1.getExactRun(new Combination(axes, "value3")));

        p.setCombinationFilter("axis1 != 'value3'");

        MatrixBuild b2 = p.scheduleBuild2(0).get();

        j.assertBuildStatus(Result.SUCCESS, b2.getExactRun(new Combination(axes, "value1")));
        j.assertBuildStatus(Result.FAILURE, b2.getExactRun(new Combination(axes, "value2")));
        assertNull(b2.getExactRun(new Combination(axes, "value3")));

        p.setCombinationFilter("");

        WebClient wc = j.createAllow405WebClient();
        HtmlPage page = wc.getPage(p, "build");

        j.clickShortcut(page, "Previous");

        j.assertCombinationChecked(page, true, axes, "value1");
        j.assertCombinationChecked(page, true, axes, "value2");
        j.assertCombinationChecked(page, false, axes, "value3");
    }

    @Test
    public void testCheckNoLastBuild() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        MatrixCombinationsParameterDefinition def =
                new MatrixCombinationsParameterDefinition("COMBINATIONS", "", "", List.of(new PreviousShortcut()));
        p.addProperty(new ParametersDefinitionProperty(def));
        p.setCombinationFilter("axis1 != 'value3'");
        p.getBuildersList().add(new ConditionalFailBuilder("${axis1}", "value2"));

        WebClient wc = j.createAllow405WebClient();
        HtmlPage page = wc.getPage(p, "build");

        j.clickShortcut(page, "Previous");

        j.assertCombinationChecked(page, false, axes, "value1");
        j.assertCombinationChecked(page, false, axes, "value2");
        j.assertCombinationChecked(page, false, axes, "value3");
    }

    @Test
    public void testCheckRebuild() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        MatrixCombinationsParameterDefinition def =
                new MatrixCombinationsParameterDefinition("COMBINATIONS", "", "", List.of(new PreviousShortcut()));
        p.addProperty(new ParametersDefinitionProperty(def));
        p.getBuildersList().add(new ConditionalFailBuilder("${axis1}", "value2"));

        MatrixBuild b1 = p.scheduleBuild2(0).get();

        j.assertBuildStatus(Result.SUCCESS, b1.getExactRun(new Combination(axes, "value1")));
        j.assertBuildStatus(Result.FAILURE, b1.getExactRun(new Combination(axes, "value2")));
        j.assertBuildStatus(Result.SUCCESS, b1.getExactRun(new Combination(axes, "value3")));

        p.setCombinationFilter("axis1 != 'value3'");

        MatrixBuild b2 = p.scheduleBuild2(0).get();

        j.assertBuildStatus(Result.SUCCESS, b2.getExactRun(new Combination(axes, "value1")));
        j.assertBuildStatus(Result.FAILURE, b2.getExactRun(new Combination(axes, "value2")));
        assertNull(b2.getExactRun(new Combination(axes, "value3")));

        p.setCombinationFilter("");

        MatrixBuild b3 = p.scheduleBuild2(0).get();

        j.assertBuildStatus(Result.SUCCESS, b3.getExactRun(new Combination(axes, "value1")));
        j.assertBuildStatus(Result.FAILURE, b3.getExactRun(new Combination(axes, "value2")));
        j.assertBuildStatus(Result.SUCCESS, b3.getExactRun(new Combination(axes, "value3")));

        WebClient wc = j.createAllow405WebClient();
        HtmlPage buildPage = wc.getPage(b2);
        HtmlPage page = buildPage.getAnchorByText("Rebuild").click();

        j.clickShortcut(page, "Previous");

        j.assertCombinationChecked(page, true, axes, "value1");
        j.assertCombinationChecked(page, true, axes, "value2");
        j.assertCombinationChecked(page, false, axes, "value3");
    }
}
