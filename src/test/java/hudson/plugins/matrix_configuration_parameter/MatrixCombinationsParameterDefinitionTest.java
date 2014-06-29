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
import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 *
 */
public class MatrixCombinationsParameterDefinitionTest {
    @Rule
    public MatrixCombinationsJenkinsRule j = new MatrixCombinationsJenkinsRule();
    
    @Test
    public void testDefaultTriggerSingleAxisWithoutFilter() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition("combinations", "", "")
        ));
        
        MatrixBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);
        
        // Assert that all combinations are triggered
        assertNotNull(b.getExactRun(new Combination(axes, "value1")));
        assertNotNull(b.getExactRun(new Combination(axes, "value2")));
        assertNotNull(b.getExactRun(new Combination(axes, "value3")));
    }
    
    @Test
    public void testDefaultTriggerSingleAxisWithFilter() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition("combinations", "", "axis1 != 'value3'")
        ));
        
        MatrixBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);
        
        // the default combination filter works.
        assertNotNull(b.getExactRun(new Combination(axes, "value1")));
        assertNotNull(b.getExactRun(new Combination(axes, "value2")));
        assertNull(b.getExactRun(new Combination(axes, "value3")));
    }
    
    @Test
    public void testDefaultTriggerDoubleAxesWithoutFilter() throws Exception {
        AxisList axes = new AxisList(
                new TextAxis("axis1", "value1-1", "value1-2"),
                new TextAxis("axis2", "value2-1", "value2-2")
        );
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        // exclude axis1=value1-2,axis2=value2-2.
        p.setCombinationFilter("!(axis1 == 'value1-2' && axis2 == 'value2-2')");
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition("combinations", "", "")
        ));
        
        MatrixBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);
        
        // Assert that the combination filter of the project works.
        assertNotNull(b.getExactRun(new Combination(axes, "value1-1", "value2-1")));
        assertNotNull(b.getExactRun(new Combination(axes, "value1-1", "value2-2")));
        assertNotNull(b.getExactRun(new Combination(axes, "value1-2", "value2-1")));
        assertNull(b.getExactRun(new Combination(axes, "value1-2", "value2-2")));
    }
    
    @Test
    public void testDefaultTriggerDoubleAxesWithFilter() throws Exception {
        AxisList axes = new AxisList(
                new TextAxis("axis1", "value1-1", "value1-2"),
                new TextAxis("axis2", "value2-1", "value2-2")
        );
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        // exclude axis1=value1-2,axis2=value2-2.
        p.setCombinationFilter("!(axis1 == 'value1-2' && axis2 == 'value2-2')");
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition("combinations", "", "!(axis1 == 'value1-1' && axis2 == 'value2-1')")
        ));
        
        MatrixBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(b);
        
        // Assert that the combination filter of the project and default combination filter of the parameter works.
        assertNull(b.getExactRun(new Combination(axes, "value1-1", "value2-1")));
        assertNotNull(b.getExactRun(new Combination(axes, "value1-1", "value2-2")));
        assertNotNull(b.getExactRun(new Combination(axes, "value1-2", "value2-1")));
        assertNull(b.getExactRun(new Combination(axes, "value1-2", "value2-2")));
    }
    
    @Test
    public void testWebConfiguration() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition("combinations", "Test", "axis1 != 'value3'")
        ));
        p.save();
        
        WebClient wc = j.createWebClient();
        j.submit(wc.getPage(p, "configure").getFormByName("config"));
        
        p = j.jenkins.getItemByFullName(p.getFullName(), MatrixProject.class);
        ParametersDefinitionProperty prop = p.getProperty(ParametersDefinitionProperty.class);
        MatrixCombinationsParameterDefinition def = (MatrixCombinationsParameterDefinition)prop.getParameterDefinition("combinations");
        assertEquals("combinations", def.getName());
        assertEquals("Test", def.getDescription());
        assertEquals("axis1 != 'value3'", def.getDefaultCombinationFilter());
    }
    
    @Test
    public void testBuildPageWithSingleAxis() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition("combinations", "", "")
        ));
        p.save();
        
        WebClient wc = j.createAllow405WebClient();
        HtmlPage page = wc.getPage(p, "build");
        HtmlForm form = page.getFormByName("parameters");
        
        ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1")).setChecked(true);
        ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value2")).setChecked(false);
        ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value3")).setChecked(true);
        
        j.submit(form);
        
        j.waitUntilNoActivity();
        MatrixBuild b = p.getLastBuild();
        assertNotNull(b);
        j.assertBuildStatusSuccess(b);
        
        assertNotNull(b.getExactRun(new Combination(axes, "value1")));
        assertNull(b.getExactRun(new Combination(axes, "value2")));
        assertNotNull(b.getExactRun(new Combination(axes, "value3")));
    }
    
    @Test
    public void testBuildPageJsWithSingleAxis() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition("combinations", "", "")
        ));
        // fails when axis1 == value2
        p.getBuildersList().add(new ConditionalFailBuilder("${axis1}", "value2"));
        p.save();
        
        {
            MatrixBuild b = p.scheduleBuild2(0).get();
            j.assertBuildStatus(Result.FAILURE, b);
            j.assertBuildStatus(Result.SUCCESS, b.getExactRun(new Combination(axes, "value1")));
            j.assertBuildStatus(Result.FAILURE, b.getExactRun(new Combination(axes, "value2")));
            j.assertBuildStatus(Result.SUCCESS, b.getExactRun(new Combination(axes, "value3")));
        }
        WebClient wc = j.createAllow405WebClient();
        HtmlPage page = wc.getPage(p, "build");
        HtmlForm form = page.getFormByName("parameters");
        
        /** This does not work now (JENKINS-23609).
        // Successful link
        {
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1")).setChecked(false);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value2")).setChecked(true);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value3")).setChecked(true);
            
            ((HtmlAnchor)page.getElementById("shortcut-combinations-successful")).click();
            
            assertTrue(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1")).isChecked());
            assertFalse(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value2")).isChecked());
            assertTrue(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value3")).isChecked());
            
        }
        */
        
        /** This does not work now (JENKINS-23609).
        // Failed link
        {
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1")).setChecked(true);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value2")).setChecked(false);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value3")).setChecked(false);
            
            ((HtmlAnchor)page.getElementById("shortcut-combinations-failed")).click();
            
            assertFalse(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1")).isChecked());
            assertTrue(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value2")).isChecked());
            assertFalse(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value3")).isChecked());
            
        }
        */
        
        // All link
        {
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1")).setChecked(false);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value2")).setChecked(false);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value3")).setChecked(true);
            
            ((HtmlAnchor)page.getElementById("shortcut-combinations-all")).click();
            
            assertTrue(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1")).isChecked());
            assertTrue(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value2")).isChecked());
            assertTrue(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value3")).isChecked());
            
        }
        
        // None link
        {
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1")).setChecked(true);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value2")).setChecked(true);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value3")).setChecked(false);
            
            ((HtmlAnchor)page.getElementById("shortcut-combinations-successful")).click();
            
            assertFalse(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1")).isChecked());
            assertFalse(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value2")).isChecked());
            assertFalse(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value3")).isChecked());
            
        }
    }
    
    @Test
    public void testBuildPageWithDoubleAxis() throws Exception {
        AxisList axes = new AxisList(
                new TextAxis("axis1", "value1-1", "value1-2"),
                new TextAxis("axis2", "value2-1", "value2-2")
        );
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition("combinations", "", "")
        ));
        p.save();
        
        WebClient wc = j.createAllow405WebClient();
        HtmlPage page = wc.getPage(p, "build");
        HtmlForm form = page.getFormByName("parameters");
        
        ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-1")).setChecked(false);
        ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-2")).setChecked(true);
        ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-1")).setChecked(true);
        ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-2")).setChecked(false);
        
        j.submit(form);
        
        j.waitUntilNoActivity();
        MatrixBuild b = p.getLastBuild();
        assertNotNull(b);
        j.assertBuildStatusSuccess(b);
        
        assertNull(b.getExactRun(new Combination(axes, "value1-1", "value2-1")));
        assertNotNull(b.getExactRun(new Combination(axes, "value1-1", "value2-2")));
        assertNotNull(b.getExactRun(new Combination(axes, "value1-2", "value2-1")));
        assertNull(b.getExactRun(new Combination(axes, "value1-2", "value2-2")));
    }
    
    
    @Test
    public void testBuildPageJsWithDoubleAxis() throws Exception {
        AxisList axes = new AxisList(
                new TextAxis("axis1", "value1-1", "value1-2"),
                new TextAxis("axis2", "value2-1", "value2-2")
        );
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        // fails when axis1, axis2 == value1-1, value2-2
        p.getBuildersList().add(new ConditionalFailBuilder("${axis1},${axis2}", "value1-1,value2-2"));
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition("combinations", "", "")
        ));
        p.save();
        
        {
            MatrixBuild b = p.scheduleBuild2(0).get();
            j.assertBuildStatus(Result.FAILURE, b);
            j.assertBuildStatus(Result.SUCCESS, b.getExactRun(new Combination(axes, "value1-1", "value2-1")));
            j.assertBuildStatus(Result.FAILURE, b.getExactRun(new Combination(axes, "value1-1", "value2-2")));
            j.assertBuildStatus(Result.SUCCESS, b.getExactRun(new Combination(axes, "value1-2", "value2-1")));
            j.assertBuildStatus(Result.SUCCESS, b.getExactRun(new Combination(axes, "value1-2", "value2-2")));
        }
        WebClient wc = j.createAllow405WebClient();
        HtmlPage page = wc.getPage(p, "build");
        HtmlForm form = page.getFormByName("parameters");
        
        /** This does not work now (JENKINS-23609).
        // Successful link
        {
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-1")).setChecked(true);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-2")).setChecked(false);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-1")).setChecked(true);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-2")).setChecked(false);
            
            ((HtmlAnchor)page.getElementById("shortcut-combinations-successful")).click();
            
            assertTrue(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-1")).isChecked());
            assertFalse(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-2")).isChecked());
            assertTrue(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-1")).isChecked());
            assertTrue(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-2")).isChecked());
        }
        */
        
        /** This does not work now (JENKINS-23609).
        // Failed link
        {
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-1")).setChecked(false);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-2")).setChecked(true);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-1")).setChecked(false);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-2")).setChecked(true);
            
            ((HtmlAnchor)page.getElementById("shortcut-combinations-failed")).click();
            
            assertFalse(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-1")).isChecked());
            assertTrue(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-2")).isChecked());
            assertFalse(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-1")).isChecked());
            assertFalse(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-2")).isChecked());
        }
        */
        
        // All link
        {
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-1")).setChecked(false);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-2")).setChecked(true);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-1")).setChecked(false);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-2")).setChecked(true);
            
            ((HtmlAnchor)page.getElementById("shortcut-combinations-all")).click();
            
            assertTrue(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-1")).isChecked());
            assertTrue(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-2")).isChecked());
            assertTrue(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-1")).isChecked());
            assertTrue(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-2")).isChecked());
        }
        
        // None link
        {
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-1")).setChecked(false);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-2")).setChecked(true);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-1")).setChecked(false);
            ((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-2")).setChecked(true);
            
            ((HtmlAnchor)page.getElementById("shortcut-combinations-successful")).click();
            
            assertFalse(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-1")).isChecked());
            assertFalse(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-1-axis2-value2-2")).isChecked());
            assertFalse(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-1")).isChecked());
            assertFalse(((HtmlCheckBoxInput)form.getElementById("checkboxcombinations-axis1-value1-2-axis2-value2-2")).isChecked());
        }
    }
    
}
