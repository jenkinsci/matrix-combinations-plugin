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

import org.apache.commons.httpclient.HttpStatus;
import org.jvnet.hudson.test.JenkinsRule;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.matrix.AxisList;
import hudson.matrix.Combination;

/**
 *
 */
public class MatrixCombinationsJenkinsRule extends JenkinsRule {
    /**
     * Get Web Client that allows 405 Method Not Allowed.
     * This happens when accessing build page of a project with parameters.
     * 
     * @return WebClient
     */
    public WebClient createAllow405WebClient() {
        return new WebClient() {
            private static final long serialVersionUID = 2209855651713458482L;
            
            @Override
            public void throwFailingHttpStatusCodeExceptionIfNecessary(
                    WebResponse webResponse
            ) {
                if(webResponse.getStatusCode() == HttpStatus.SC_METHOD_NOT_ALLOWED) {
                    // allow 405.
                    return;
                }
                super.throwFailingHttpStatusCodeExceptionIfNecessary(webResponse);
            }
            
            @Override
            public void printContentIfNecessary(WebResponse webResponse) {
                if(webResponse.getStatusCode() == HttpStatus.SC_METHOD_NOT_ALLOWED)
                {
                    // allow 405.
                    return;
                }
                super.printContentIfNecessary(webResponse);
            }
        };
    }

    public void checkCombination(HtmlPage page, boolean checked, AxisList axes, String... values) throws Exception {
        checkCombination(page, 0, checked, axes, values);
    }

    public void checkCombination(HtmlPage page, int index, boolean checked, AxisList axes, String... values) throws Exception {
        page.<HtmlElement>selectNodes("//*[@class='matrix-combinations-parameter']").get(index)
            .<HtmlCheckBoxInput>selectNodes(String.format(
                "//*[@data-combination='%s']//input[@type='checkbox']",
                new Combination(axes, values).toIndex(axes)
            )).get(0).setChecked(checked);
    }

    public void clickShortcut(HtmlPage page, String name) throws Exception {
        clickShortcut(page, 0, name);
    }

    public void clickShortcut(HtmlPage page, int index, String name) throws Exception {
        page.<HtmlElement>selectNodes("//*[@class='matrix-combinations-parameter']").get(index)
            .<HtmlAnchor>selectNodes(String.format(
                "//a[@data-shortcut-id='%s']",
                name
            )).get(0).click();
    }

    public void assertCombinationChecked(HtmlPage page, boolean checked, AxisList axes, String... values) throws Exception {
        assertCombinationChecked(page, 0, checked, axes, values);
    }

    public void assertCombinationChecked(HtmlPage page, int index, boolean checked, AxisList axes, String... values) throws Exception {
        assertEquals(
            checked,
            page.<HtmlElement>selectNodes("//*[@class='matrix-combinations-parameter']").get(index)
                .<HtmlCheckBoxInput>selectNodes(String.format(
                    "//*[@data-combination='%s']//input[@type='checkbox']",
                    new Combination(axes, values).toIndex(axes)
                )).get(0).isChecked()
        );
    }
}
