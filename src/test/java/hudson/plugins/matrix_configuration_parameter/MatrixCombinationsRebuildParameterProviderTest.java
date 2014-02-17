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

import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.Cause;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 *
 */
public class MatrixCombinationsRebuildParameterProviderTest
{
    @Rule
    public JenkinsRule j = new JenkinsRule();
    
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
}
