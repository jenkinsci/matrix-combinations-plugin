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

import static org.junit.Assert.*;

import java.util.Arrays;

import hudson.markup.RawHtmlMarkupFormatter;
import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.Cause;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import hudson.model.StringParameterValue;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 *
 */
public class MatrixCombinationsRebuildParameterProviderTest
{
    @Rule
    public MatrixCombinationsJenkinsRule j = new MatrixCombinationsJenkinsRule();
    
    @Test
    public void testRebuildOneAxis() throws Exception {
        MatrixProject p = j.createMatrixProject();
        p.setAxes(new AxisList(new TextAxis("axis1", "value1", "value2", "value3")));
        p.addProperty(new ParametersDefinitionProperty(new MatrixCombinationsParameterDefinition("combinations", "")));
        
        // first run.
        // asserts that only combinations specified with MatrixCombinationsParameterValue are built.
        @SuppressWarnings("deprecation")
        Cause cause = new Cause.UserCause();
        MatrixBuild b1 = p.scheduleBuild2(0, cause, Arrays.asList(
                new ParametersAction(new MatrixCombinationsParameterValue(
                        "combinations",
                        new Boolean[]{ true, false, true },
                        new String[]{ "axis1=value1", "axis1=value2", "axis1=value3" }
                ))
        )).get();
        j.assertBuildStatusSuccess(b1);
        assertNotNull(b1.getExactRun(new Combination(p.getAxes(), "value1")));
        assertNull(b1.getExactRun(new Combination(p.getAxes(), "value2")));
        assertNotNull(b1.getExactRun(new Combination(p.getAxes(), "value3")));
        
        
        // second run (rebuild)
        // asserts that only combinations in the first run are built.
        WebClient wc = j.createWebClient();
        HtmlPage page = wc.getPage(b1, "rebuild");
        HtmlForm form = page.getFormByName("config");
        j.submit(form);
        
        j.waitUntilNoActivity();
        
        MatrixBuild b2  = p.getLastBuild();
        assertNotEquals(b1.getNumber(), b2.getNumber());
        j.assertBuildStatusSuccess(b2);
        assertNotNull(b2.getExactRun(new Combination(p.getAxes(), "value1")));
        assertNull(b2.getExactRun(new Combination(p.getAxes(), "value2")));
        assertNotNull(b2.getExactRun(new Combination(p.getAxes(), "value3")));
    }
    
    @Test
    public void testRebuildTwoAxes() throws Exception {
        MatrixProject p = j.createMatrixProject();
        p.setAxes(new AxisList(
                new TextAxis("axis1", "value1-1", "value1-2"),
                new TextAxis("axis2", "value2-1", "value2-2")
        ));
        p.addProperty(new ParametersDefinitionProperty(new MatrixCombinationsParameterDefinition("combinations", "")));
        
        // first run.
        // asserts that only combinations specified with MatrixCombinationsParameterValue are built.
        @SuppressWarnings("deprecation")
        Cause cause = new Cause.UserCause();
        MatrixBuild b1 = p.scheduleBuild2(0, cause, Arrays.asList(
                new ParametersAction(new MatrixCombinationsParameterValue(
                        "combinations",
                        new Boolean[]{ false, true, false, true },
                        new String[]{
                                "axis1=value1-1,axis2=value2-1",
                                "axis1=value1-2,axis2=value2-1",
                                "axis1=value1-1,axis2=value2-2",
                                "axis1=value1-2,axis2=value2-2",
                        }
                ))
        )).get();
        j.assertBuildStatusSuccess(b1);
        assertNull(b1.getExactRun(new Combination(p.getAxes(), "value1-1", "value2-1")));
        assertNotNull(b1.getExactRun(new Combination(p.getAxes(), "value1-2", "value2-1")));
        assertNull(b1.getExactRun(new Combination(p.getAxes(), "value1-1", "value2-2")));
        assertNotNull(b1.getExactRun(new Combination(p.getAxes(), "value1-2", "value2-2")));
        
        
        // second run (rebuild)
        // asserts that only combinations in the first run are built.
        WebClient wc = j.createWebClient();
        HtmlPage page = wc.getPage(b1, "rebuild");
        HtmlForm form = page.getFormByName("config");
        j.submit(form);
        
        j.waitUntilNoActivity();
        
        MatrixBuild b2  = p.getLastBuild();
        assertNotEquals(b1.getNumber(), b2.getNumber());
        assertNull(b1.getExactRun(new Combination(p.getAxes(), "value1-1", "value2-1")));
        assertNotNull(b1.getExactRun(new Combination(p.getAxes(), "value1-2", "value2-1")));
        assertNull(b1.getExactRun(new Combination(p.getAxes(), "value1-1", "value2-2")));
        assertNotNull(b1.getExactRun(new Combination(p.getAxes(), "value1-2", "value2-2")));
    }
    
    @Bug(27233)
    @Test
    public void testAppliedForNonMatrixProjectRebuild() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject();
        
        @SuppressWarnings("deprecation")
        Cause cause = new Cause.UserCause();
        FreeStyleBuild b1 = p.scheduleBuild2(0, cause, Arrays.asList(
                new ParametersAction(
                        new MatrixCombinationsParameterValue(
                                "combinations",
                                new Boolean[]{ true, false, true },
                                new String[]{ "axis1=value1", "axis1=value2", "axis1=value3" }
                        )
                        // rebuild-plugin causes exception
                        // when requesting a rebuild with no parameters.
                        , new StringParameterValue("dummy", "")
                )
        )).get();
        
        WebClient wc = j.createWebClient();
        HtmlPage page = wc.getPage(b1, "rebuild");
        HtmlForm form = page.getFormByName("config");
        j.submit(form);
        
        j.waitUntilNoActivity();
        
        FreeStyleBuild b2  = p.getLastBuild();
        assertNotEquals(b1.getNumber(), b2.getNumber());
    }

    @Test
    public void testShortcut() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(new MatrixCombinationsParameterDefinition("combinations", "")));

        p.getBuildersList().add(new ConditionalFailBuilder("${axis1}", "value2"));
        p.setCombinationFilter("axis1 != 'value3'");

        MatrixBuild b1 = p.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.SUCCESS, b1.getExactRun(new Combination(axes, "value1")));
        j.assertBuildStatus(Result.FAILURE, b1.getExactRun(new Combination(axes, "value2")));
        assertNull(b1.getExactRun(new Combination(axes, "value3")));

        p.getBuildersList().clear();
        p.setCombinationFilter("");

        MatrixBuild b2 = p.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.SUCCESS, b2.getExactRun(new Combination(axes, "value1")));
        j.assertBuildStatus(Result.SUCCESS, b2.getExactRun(new Combination(axes, "value2")));
        j.assertBuildStatus(Result.SUCCESS, b2.getExactRun(new Combination(axes, "value3")));

        WebClient wc = j.createWebClient();
        HtmlPage page = wc.getPage(b1, "rebuild");

        j.assertCombinationChecked(page, true, axes, "value1");
        j.assertCombinationChecked(page, true, axes, "value2");
        j.assertCombinationChecked(page, false, axes, "value3");

        j.clickShortcut(page, "SUCCESS");
        j.assertCombinationChecked(page, true, axes, "value1");
        j.assertCombinationChecked(page, false, axes, "value2");
        j.assertCombinationChecked(page, false, axes, "value3");

        j.clickShortcut(page, "FAILURE");
        j.assertCombinationChecked(page, false, axes, "value1");
        j.assertCombinationChecked(page, true, axes, "value2");
        j.assertCombinationChecked(page, false, axes, "value3");
    }

    @Issue("JENKINS-42902")
    @Test
    public void testSafeTitle() throws Exception {
        j.jenkins.setMarkupFormatter(new RawHtmlMarkupFormatter(true));
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition(
                    "<span id=\"test-not-expected\">combinations</span>",
                    ""
                )
        ));

        MatrixBuild b = j.assertBuildStatusSuccess(p.scheduleBuild2(0).get());

        WebClient wc = j.createWebClient();
        HtmlPage page = wc.getPage(b, "rebuild");

        assertNull(page.getElementById("test-not-expected"));
    }

    @Issue("JENKINS-42902")
    @Test
    public void testSafeDescription() throws Exception {
        j.jenkins.setMarkupFormatter(new RawHtmlMarkupFormatter(true));

        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition(
                    "combinations",
                    "<span id=\"test-expected\">blahblah</span>"
                        + "<script id=\"test-not-expected\"></script>"
                )
        ));

        MatrixBuild b = j.assertBuildStatusSuccess(p.scheduleBuild2(0).get());

        WebClient wc = j.createWebClient();
        HtmlPage page = wc.getPage(b, "rebuild");

        assertNotNull(page.getElementById("test-expected"));
        assertNull(page.getElementById("test-not-expected"));
    }
}
