/*
 * The MIT License
 *
 * Copyright (c) 2014 IKEDA Yasuyuki
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

package hudson.plugins.matrix_configuration_parameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import hudson.markup.RawHtmlMarkupFormatter;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.Cause;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import org.htmlunit.html.HtmlPage;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

/**
 *
 */
public class MatrixCombinationsParameterValueTest {
    @Rule
    public MatrixCombinationsJenkinsRule j = new MatrixCombinationsJenkinsRule();

    @Test
    public void testParametersPageWithSingleAxis() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition("combinations", "", "axis1 != 'value2'")));

        MatrixBuild b = p.scheduleBuild2(0).get();

        j.assertBuildStatusSuccess(b);

        WebClient wc = j.createWebClient();
        HtmlPage page = wc.getPage(b, "parameters");

        j.assertCombinationChecked(page, true, axes, "value1");
        j.assertCombinationChecked(page, false, axes, "value2");
        j.assertCombinationChecked(page, true, axes, "value3");
    }

    @Test
    public void testParametersPageWithDoubleAxis() throws Exception {
        AxisList axes = new AxisList(
                new TextAxis("axis1", "value1-1", "value1-2"), new TextAxis("axis2", "value2-1", "value2-2"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(new MatrixCombinationsParameterDefinition(
                "combinations", "", "!(axis1 == 'value1-1' && axis2 == 'value2-2')")));
        p.save();

        MatrixBuild b = p.scheduleBuild2(0).get();

        j.assertBuildStatusSuccess(b);

        WebClient wc = j.createWebClient();
        HtmlPage page = wc.getPage(b, "parameters");

        j.assertCombinationChecked(page, true, axes, "value1-1", "value2-1");
        j.assertCombinationChecked(page, false, axes, "value1-1", "value2-2");
        j.assertCombinationChecked(page, true, axes, "value1-2", "value2-1");
        j.assertCombinationChecked(page, true, axes, "value1-2", "value2-2");
    }

    @Issue("JENKINS-27233")
    @Test
    public void testNonMatrixBuild() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject();

        @SuppressWarnings("deprecation")
        Cause cause = new Cause.UserCause();
        FreeStyleBuild b = p.scheduleBuild2(
                        0,
                        cause,
                        List.of(new ParametersAction(new MatrixCombinationsParameterValue(
                                "combinations",
                                new Boolean[] {true, false, true},
                                new String[] {"axis1=value1", "axis1=value2", "axis1=value3"}))))
                .get();

        WebClient wc = j.createWebClient();
        wc.getPage(b, "parameters");
    }

    @Test
    public void testReadResolve() {
        final String SERIALIZED = "<hudson.plugins.matrix__configuration__parameter.MatrixCombinationsParameterValue>"
                + "<name>combinations</name>"
                + "<description>test</description>"
                + "<values>"
                + "<boolean>true</boolean>"
                + "<boolean>false</boolean>"
                + "<boolean>true</boolean>"
                + "</values>"
                + "<confs>"
                + "<string>axis1=value1</string>"
                + "<string>axis1=value2</string>"
                + "<string>axis1=value3</string>"
                + "</confs>"
                + "</hudson.plugins.matrix__configuration__parameter.MatrixCombinationsParameterValue>";
        MatrixCombinationsParameterValue v = (MatrixCombinationsParameterValue) Jenkins.XSTREAM2.fromXML(SERIALIZED);
        assertEquals("combinations", v.getName());
        assertEquals("test", v.getDescription());
        assertNull(v.getValues());
        assertNull(v.getConfs());
        assertEquals(Arrays.asList("axis1=value1", "axis1=value3"), v.getCombinations());
    }

    @Test
    public void testReadResolveOfDefaultMatrixCombinationsParameterValue() {
        final String SERIALIZED =
                "<hudson.plugins.matrix__configuration__parameter.DefaultMatrixCombinationsParameterValue>"
                        + "<name>combinations</name>"
                        + "<description>test</description>"
                        + "<combinationFilter>axis1 != &apos;value2&apos;</combinationFilter>"
                        + "</hudson.plugins.matrix__configuration__parameter.DefaultMatrixCombinationsParameterValue>";
        DefaultMatrixCombinationsParameterValue v =
                (DefaultMatrixCombinationsParameterValue) Jenkins.XSTREAM2.fromXML(SERIALIZED);
        assertEquals("combinations", v.getName());
        assertEquals("test", v.getDescription());
        assertNull(v.getValues());
        assertNull(v.getConfs());
        assertEquals(Collections.emptyList(), v.getCombinations());
        assertEquals("axis1 != 'value2'", v.getCombinationFilter());
    }

    @Issue("JENKINS-42902")
    @Test
    public void testSafeTitle() throws Exception {
        j.jenkins.setMarkupFormatter(new RawHtmlMarkupFormatter(true));
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition("<span id=\"test-not-expected\">combinations</span>", "")));

        MatrixBuild b = j.assertBuildStatusSuccess(p.scheduleBuild2(0).get());

        WebClient wc = j.createWebClient();
        HtmlPage page = wc.getPage(b, "parameters");

        assertNull(page.getElementById("test-not-expected"));
    }
}
