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

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.matrix.MatrixProject;
import java.io.IOException;
import org.htmlunit.WebResponse;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlCheckBoxInput;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.jvnet.hudson.test.JenkinsRule;

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
        WebClient webClient = new WebClient() {
            private static final long serialVersionUID = 2209855651713458482L;

            @Override
            public void throwFailingHttpStatusCodeExceptionIfNecessary(WebResponse webResponse) {
                if (webResponse.getStatusCode() == 405) {
                    // allow 405.
                    return;
                }
                super.throwFailingHttpStatusCodeExceptionIfNecessary(webResponse);
            }

            @Override
            public void printContentIfNecessary(WebResponse webResponse) {
                if (webResponse.getStatusCode() == 405) {
                    // allow 405.
                    return;
                }
                super.printContentIfNecessary(webResponse);
            }
        };
        webClient.getOptions().setFetchPolyfillEnabled(true);
        return webClient;
    }

    public void checkCombination(HtmlPage page, boolean checked, AxisList axes, String... values) throws Exception {
        checkCombination(page, 0, checked, axes, values);
    }

    public void checkCombination(HtmlPage page, int index, boolean checked, AxisList axes, String... values)
            throws Exception {
        HtmlElement param = byXPath(
                page.getDocumentElement(), "//*[@class='matrix-combinations-parameter']", index, HtmlElement.class);
        HtmlCheckBoxInput checkbox = firstByXPath(
                param,
                String.format(
                        ".//*[@data-combination='%s']//input[@type='checkbox']",
                        new Combination(axes, values).toIndex(axes)),
                HtmlCheckBoxInput.class);
        checkbox.setChecked(checked);
    }

    public void clickShortcut(HtmlPage page, String name) throws Exception {
        clickShortcut(page, 0, name);
    }

    public void clickShortcut(HtmlPage page, int index, String name) throws Exception {
        HtmlElement param = byXPath(
                page.getDocumentElement(), "//*[@class='matrix-combinations-parameter']", index, HtmlElement.class);
        HtmlElement shortcut = firstByXPath(param, String.format(".//a[@data-shortcut-id='%s']", name));
        shortcut.click();
    }

    public void assertCombinationChecked(HtmlPage page, boolean checked, AxisList axes, String... values)
            throws Exception {
        assertCombinationChecked(page, 0, checked, axes, values);
    }

    public void assertCombinationChecked(HtmlPage page, int index, boolean checked, AxisList axes, String... values)
            throws Exception {
        HtmlElement param;
        if (index == 0) {
            param = page.getDocumentElement();
        } else {
            param = byXPath(
                    page.getDocumentElement(), "//*[@class='matrix-combinations-parameter']", index, HtmlElement.class);
        }
        HtmlCheckBoxInput checkbox = firstByXPath(
                param,
                String.format(
                        ".//*[@data-combination='%s']//input[@type='checkbox']",
                        new Combination(axes, values).toIndex(axes)),
                HtmlCheckBoxInput.class);
        assertEquals(checked, checkbox.isChecked());
    }

    @NonNull
    protected static HtmlElement nodeToElement(@Nullable DomNode node) {
        return nodeToElement(node, HtmlElement.class);
    }

    @NonNull
    protected static <T extends Object> T nodeToElement(@Nullable Object node, Class<T> c) throws AssertionError {
        assertNotNull("Node is null", node);
        assertThat(String.format("Node is not %s: %s", c, node), node, instanceOf(c));
        return (T) node;
    }

    @NonNull
    protected static HtmlElement firstByXPath(@NonNull HtmlElement root, String xpath) throws AssertionError {
        return byXPath(root, xpath, 0, HtmlElement.class);
    }

    @NonNull
    protected static <T extends Object> T firstByXPath(@NonNull HtmlElement root, String xpath, Class<T> c)
            throws AssertionError {
        return byXPath(root, xpath, 0, c);
    }

    @NonNull
    protected static <T extends Object> T byXPath(@NonNull HtmlElement root, String xpath, int index, Class<T> c)
            throws AssertionError {
        Object o = root.getByXPath(xpath).get(index);
        assertNotNull(String.format("Failed to fetch element #%d for query '%s'. Node: %s", index, xpath, root), o);
        assertThat(String.format("Node is not %s: %s", c, o), o, instanceOf(c));
        return (T) o;
    }

    public MatrixProject createMatrixProject() throws IOException {
        return createProject(MatrixProject.class);
    }
}
