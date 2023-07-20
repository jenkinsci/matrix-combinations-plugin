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

import java.util.Arrays;

import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

import org.htmlunit.html.HtmlPage;

import hudson.matrix.AxisList;
import hudson.matrix.MatrixProject;
import hudson.matrix.TextAxis;
import hudson.model.Item;
import hudson.model.ParametersDefinitionProperty;
import hudson.plugins.matrix_configuration_parameter.MatrixCombinationsJenkinsRule;
import hudson.plugins.matrix_configuration_parameter.MatrixCombinationsParameterDefinition;

/**
 * Tests for {@link CombinationFilterShortcut}
 */
public class CombinationFilterShortcutTest {
    @ClassRule
    public static MatrixCombinationsJenkinsRule j = new MatrixCombinationsJenkinsRule();

    @Test
    public void testConfiguration() throws Exception {
        AxisList axes = new AxisList(
            new TextAxis("axis1", "value1-1", "value2-1"),
            new TextAxis("axis2", "value2-1", "value2-2")
        );
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        MatrixCombinationsParameterDefinition def = new MatrixCombinationsParameterDefinition(
            "COMBINATIONS",
            "",
            "",
            Arrays.<MatrixCombinationsShortcut>asList(
                new CombinationFilterShortcut(
                    "FILTER",
                    "!(axis1 == 'value1-2' && axis2 == 'value2-2')"
                )
            )
        );
        p.addProperty(new ParametersDefinitionProperty(def));

        j.configRoundtrip((Item)p);

        j.assertEqualDataBoundBeans(
            def,
            p.getProperty(ParametersDefinitionProperty.class).getParameterDefinition("COMBINATIONS")
        );
    }

    @Test
    public void testCheck() throws Exception {
        AxisList axes = new AxisList(
            new TextAxis("axis1", "value1-1", "value1-2"),
            new TextAxis("axis2", "value2-1", "value2-2")
        );
        MatrixProject p = j.createMatrixProject();
        p.setAxes(axes);
        MatrixCombinationsParameterDefinition def = new MatrixCombinationsParameterDefinition(
            "COMBINATIONS",
            "",
            "",
            Arrays.<MatrixCombinationsShortcut>asList(
                new CombinationFilterShortcut(
                    "FILTER",
                    "!(axis1 == 'value1-2' && axis2 == 'value2-2')"
                )
            )
        );
        p.addProperty(new ParametersDefinitionProperty(def));

        WebClient wc = j.createAllow405WebClient();
        HtmlPage page = wc.getPage(p, "build");

        j.clickShortcut(page, "filter-FILTER");

        j.assertCombinationChecked(page, true, axes, "value1-1", "value2-1");
        j.assertCombinationChecked(page, true, axes, "value1-2", "value2-1");
        j.assertCombinationChecked(page, true, axes, "value1-1", "value2-2");
        j.assertCombinationChecked(page, false, axes, "value1-2", "value2-2");
    }

    @Test
    public void testCheckEmpty() throws Exception {
        MatrixProject p = j.createMatrixProject();
        MatrixCombinationsParameterDefinition def = new MatrixCombinationsParameterDefinition(
            "COMBINATIONS",
            "",
            "",
            Arrays.<MatrixCombinationsShortcut>asList(
                new CombinationFilterShortcut(
                    "FILTER",
                    ""
                )
            )
        );
        p.addProperty(new ParametersDefinitionProperty(def));

        WebClient wc = j.createAllow405WebClient();
        HtmlPage page = wc.getPage(p, "build");

        j.clickShortcut(page, "filter-FILTER");
    }
}
