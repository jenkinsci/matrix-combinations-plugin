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

import java.net.URLEncoder;

import hudson.cli.CLI;
import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.queue.QueueTaskFuture;
import hudson.model.Result;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.jvnet.hudson.test.SleepBuilder;

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
        
        j.checkCombination(page, true, axes, "value1");
        j.checkCombination(page, false, axes, "value2");
        j.checkCombination(page, true, axes, "value3");
        
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
        
        // Successful link
        {
            j.checkCombination(page, false, axes, "value1");
            j.checkCombination(page, true, axes, "value2");
            j.checkCombination(page, true, axes, "value3");
            
            j.clickShortcut(page, "SUCCESS");
            
            j.assertCombinationChecked(page, true, axes, "value1");
            j.assertCombinationChecked(page, false, axes, "value2");
            j.assertCombinationChecked(page, true, axes, "value3");
        }
 
        // Failed link
        {
            j.checkCombination(page, true, axes, "value1");
            j.checkCombination(page, false, axes, "value2");
            j.checkCombination(page, false, axes, "value3");
            
            j.clickShortcut(page, "FAILURE");
            
            j.assertCombinationChecked(page, false, axes, "value1");
            j.assertCombinationChecked(page, true, axes, "value2");
            j.assertCombinationChecked(page, false, axes, "value3");
        }
        
        // All link
        {
            j.checkCombination(page, false, axes, "value1");
            j.checkCombination(page, false, axes, "value2");
            j.checkCombination(page, true, axes, "value3");
            
            j.clickShortcut(page, "All");
            
            j.assertCombinationChecked(page, true, axes, "value1");
            j.assertCombinationChecked(page, true, axes, "value2");
            j.assertCombinationChecked(page, true, axes, "value3");
        }
        
        // None link
        {
            j.checkCombination(page, true, axes, "value1");
            j.checkCombination(page, true, axes, "value2");
            j.checkCombination(page, false, axes, "value3");
            
            j.clickShortcut(page, "None");
            
            j.assertCombinationChecked(page, false, axes, "value1");
            j.assertCombinationChecked(page, false, axes, "value2");
            j.assertCombinationChecked(page, false, axes, "value3");
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
        
        j.checkCombination(page, false, axes, "value1-1", "value2-1");
        j.checkCombination(page, true, axes, "value1-1", "value2-2");
        j.checkCombination(page, true, axes, "value1-2", "value2-1");
        j.checkCombination(page, false, axes, "value1-2", "value2-2");
        
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
        
        // Successful link
        {
            j.checkCombination(page, true, axes, "value1-1", "value2-1");
            j.checkCombination(page, false, axes, "value1-1", "value2-2");
            j.checkCombination(page, true, axes, "value1-2", "value2-1");
            j.checkCombination(page, false, axes, "value1-2", "value2-2");
            
            j.clickShortcut(page, "SUCCESS");
            
            j.assertCombinationChecked(page, true, axes, "value1-1", "value2-1");
            j.assertCombinationChecked(page, false, axes, "value1-1", "value2-2");
            j.assertCombinationChecked(page, true, axes, "value1-2", "value2-1");
            j.assertCombinationChecked(page, true, axes, "value1-2", "value2-2");
        }
        
        // Failed link
        {
            j.checkCombination(page, false, axes, "value1-1", "value2-1");
            j.checkCombination(page, true, axes, "value1-1", "value2-2");
            j.checkCombination(page, false, axes, "value1-2", "value2-1");
            j.checkCombination(page, true, axes, "value1-2", "value2-2");
            
            j.clickShortcut(page, "FAILURE");
            
            j.assertCombinationChecked(page, false, axes, "value1-1", "value2-1");
            j.assertCombinationChecked(page, true, axes, "value1-1", "value2-2");
            j.assertCombinationChecked(page, false, axes, "value1-2", "value2-1");
            j.assertCombinationChecked(page, false, axes, "value1-2", "value2-2");
        }
        
        // All link
        {
            j.checkCombination(page, false, axes, "value1-1", "value2-1");
            j.checkCombination(page, true, axes, "value1-1", "value2-2");
            j.checkCombination(page, false, axes, "value1-2", "value2-1");
            j.checkCombination(page, true, axes, "value1-2", "value2-2");
            
            j.clickShortcut(page, "All");
            
            j.assertCombinationChecked(page, true, axes, "value1-1", "value2-1");
            j.assertCombinationChecked(page, true, axes, "value1-1", "value2-2");
            j.assertCombinationChecked(page, true, axes, "value1-2", "value2-1");
            j.assertCombinationChecked(page, true, axes, "value1-2", "value2-2");
        }
        
        // None link
        {
            j.checkCombination(page, false, axes, "value1-1", "value2-1");
            j.checkCombination(page, true, axes, "value1-1", "value2-2");
            j.checkCombination(page, false, axes, "value1-2", "value2-1");
            j.checkCombination(page, true, axes, "value1-2", "value2-2");
            
            j.clickShortcut(page, "None");
            
            j.assertCombinationChecked(page, false, axes, "value1-1", "value2-1");
            j.assertCombinationChecked(page, false, axes, "value1-1", "value2-2");
            j.assertCombinationChecked(page, false, axes, "value1-2", "value2-1");
            j.assertCombinationChecked(page, false, axes, "value1-2", "value2-2");
        }
    }
    
    @Bug(23230)
    @Test
    public void testInvalidAxis() throws Exception {
        MatrixProject p = j.createMatrixProject();
        
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2"));
        p.setAxes(axes);
        p.setCombinationFilter("axis1 != 'value3'");
        axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        p.setAxes(axes);
        
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition("combinations", "", "")
        ));
        p.save();
        
        WebClient wc = j.createAllow405WebClient();
        HtmlPage page = wc.getPage(p, "build");
        j.submit(page.getFormByName("parameters"));
        
        j.waitUntilNoActivity();
        MatrixBuild b = p.getLastBuild();
        assertNotNull(b);
        j.assertBuildStatusSuccess(b);
        
        assertNotNull(b.getExactRun(new Combination(axes, "value1")));
        assertNotNull(b.getExactRun(new Combination(axes, "value2")));
        assertNull(b.getExactRun(new Combination(axes, "value3")));
    }
    
    @Bug(28824)
    @Test
    public void testBuildPageForBuilding() throws Exception {
        MatrixProject p = j.createMatrixProject();
        
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2"));
        p.setAxes(axes);
        
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition("combinations", "", "")
        ));
        
        p.getBuildersList().add(new SleepBuilder(5000));
        
        QueueTaskFuture<MatrixBuild> f = p.scheduleBuild2(0);
        
        WebClient wc = j.createAllow405WebClient();
        wc.getPage(p, "build");
        
        f.cancel(true);
    }
    
    @Test
    public void testAppliedForNonMatrixProject() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject();
        p.addProperty(new ParametersDefinitionProperty(
                new MatrixCombinationsParameterDefinition("combinations", "", "")
        ));
        
        {
            WebClient wc = j.createAllow405WebClient();
            HtmlPage page = wc.getPage(p, "build");
            j.submit(page.getFormByName("parameters"));
            
            j.waitUntilNoActivity();
            FreeStyleBuild b = p.getLastBuild();
            assertNotNull(b);
            j.assertBuildStatusSuccess(b);
        }
        
        // default trigger
        j.assertBuildStatusSuccess(p.scheduleBuild2(0));
    }

    @Issue("JENKINS-36861")
    @Test
    public void testNotBuilt() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(
            new MatrixCombinationsParameterDefinition("combinations", "", "")
        ));
        p.getBuildersList().add(new ConditionalFailBuilder("${axis1}", "value2"));
        p.setCombinationFilter("axis1 != 'value3'");

        MatrixBuild b = p.scheduleBuild2(0).get();

        j.assertBuildStatus(Result.SUCCESS, b.getExactRun(new Combination(axes, "value1")));
        j.assertBuildStatus(Result.FAILURE, b.getExactRun(new Combination(axes, "value2")));
        assertNull(b.getExactRun(new Combination(axes, "value3")));

        p.setCombinationFilter("");

        WebClient wc = j.createAllow405WebClient();
        HtmlPage page = wc.getPage(p, "build");

        j.clickShortcut(page, "SUCCESS");
        j.assertCombinationChecked(page, true, axes, "value1");
        j.assertCombinationChecked(page, false, axes, "value2");
        j.assertCombinationChecked(page, false, axes, "value3");

        j.clickShortcut(page, "FAILURE");
        j.assertCombinationChecked(page, false, axes, "value1");
        j.assertCombinationChecked(page, true, axes, "value2");
        j.assertCombinationChecked(page, false, axes, "value3");

        j.clickShortcut(page, "All");
        j.assertCombinationChecked(page, true, axes, "value1");
        j.assertCombinationChecked(page, true, axes, "value2");
        j.assertCombinationChecked(page, true, axes, "value3");
    }

    @Issue("JENKINS-30918")
    @Test
    public void testMultiParameters() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(
            new MatrixCombinationsParameterDefinition("combinations1", "", ""),
            new MatrixCombinationsParameterDefinition("combinations2", "", "")
        ));
        p.setCombinationFilter("axis1 != 'value3'");

        WebClient wc = j.createAllow405WebClient();
        HtmlPage page = wc.getPage(p, "build");

        j.assertCombinationChecked(page, 0, true, axes, "value1");
        j.assertCombinationChecked(page, 0, true, axes, "value2");
        j.assertCombinationChecked(page, 0, false, axes, "value3");
        j.assertCombinationChecked(page, 1, true, axes, "value1");
        j.assertCombinationChecked(page, 1, true, axes, "value2");
        j.assertCombinationChecked(page, 1, false, axes, "value3");

        j.clickShortcut(page, 1, "None");

        j.assertCombinationChecked(page, 0, true, axes, "value1");
        j.assertCombinationChecked(page, 0, true, axes, "value2");
        j.assertCombinationChecked(page, 0, false, axes, "value3");
        j.assertCombinationChecked(page, 1, false, axes, "value1");
        j.assertCombinationChecked(page, 1, false, axes, "value2");
        j.assertCombinationChecked(page, 1, false, axes, "value3");

        j.clickShortcut(page, 0, "None");

        j.assertCombinationChecked(page, 0, false, axes, "value1");
        j.assertCombinationChecked(page, 0, false, axes, "value2");
        j.assertCombinationChecked(page, 0, false, axes, "value3");
        j.assertCombinationChecked(page, 1, false, axes, "value1");
        j.assertCombinationChecked(page, 1, false, axes, "value2");
        j.assertCombinationChecked(page, 1, false, axes, "value3");

        j.clickShortcut(page, 1, "All");

        j.assertCombinationChecked(page, 0, false, axes, "value1");
        j.assertCombinationChecked(page, 0, false, axes, "value2");
        j.assertCombinationChecked(page, 0, false, axes, "value3");
        j.assertCombinationChecked(page, 1, true, axes, "value1");
        j.assertCombinationChecked(page, 1, true, axes, "value2");
        j.assertCombinationChecked(page, 1, false, axes, "value3");

        j.clickShortcut(page, 0, "All");

        j.assertCombinationChecked(page, 0, true, axes, "value1");
        j.assertCombinationChecked(page, 0, true, axes, "value2");
        j.assertCombinationChecked(page, 0, false, axes, "value3");
        j.assertCombinationChecked(page, 1, true, axes, "value1");
        j.assertCombinationChecked(page, 1, true, axes, "value2");
        j.assertCombinationChecked(page, 1, false, axes, "value3");
    }

    @Test
    public void testCliBuild() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(
            new MatrixCombinationsParameterDefinition("combinations", "", "")
        ));

        CLI cli = new CLI(j.getURL());
        int ret = cli.execute(
            "build",
            p.getFullName(),
            "-p",
            // You can't use axis1 != 'value2'
            // for JENKINS-21160 (fixed in Jenkins 1.606)
            "combinations=axis1 in ['value1', 'value3']"
        );
        assertEquals(0, ret);

        j.waitUntilNoActivity();

        MatrixBuild b = p.getLastBuild();
        j.assertBuildStatusSuccess(b);

        assertNotNull(b.getExactRun(new Combination(axes, "value1")));
        assertNull(b.getExactRun(new Combination(axes, "value2")));
        assertNotNull(b.getExactRun(new Combination(axes, "value3")));
    }

    @Test
    public void testBuildWithParameters() throws Exception {
        AxisList axes = new AxisList(new TextAxis("axis1", "value1", "value2", "value3"));
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        p.addProperty(new ParametersDefinitionProperty(
            new MatrixCombinationsParameterDefinition("combinations", "", "")
        ));

        WebClient wc = j.createWebClient();
        wc.getPage(p, String.format(
            "/buildWithParameters?combinations=%s",
            URLEncoder.encode("axis1 in ['value1', 'value3']", "UTF-8")
        ));

        j.waitUntilNoActivity();

        MatrixBuild b = p.getLastBuild();
        j.assertBuildStatusSuccess(b);

        assertNotNull(b.getExactRun(new Combination(axes, "value1")));
        assertNull(b.getExactRun(new Combination(axes, "value2")));
        assertNotNull(b.getExactRun(new Combination(axes, "value3")));
    }
}
