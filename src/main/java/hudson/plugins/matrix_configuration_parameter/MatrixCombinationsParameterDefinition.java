/*
 * The MIT License
 *
 * Copyright (c) 2012, Piotr Skotnicki
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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Result;
import hudson.model.SimpleParameterDefinition;
import hudson.plugins.matrix_configuration_parameter.shortcut.MatrixCombinationsShortcut;
import hudson.plugins.matrix_configuration_parameter.shortcut.MatrixCombinationsShortcutDescriptor;
import hudson.plugins.matrix_configuration_parameter.shortcut.ResultShortcut;
import java.io.Serial;
import java.util.Arrays;
import java.util.List;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest2;

public class MatrixCombinationsParameterDefinition extends SimpleParameterDefinition {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String defaultCombinationFilter;

    @SuppressFBWarnings(
            value = "SE_BAD_FIELD",
            justification =
                    "For whatever reason ParameterDefinition is defined as Serializable though only XStream actually serializes it.")
    private final List<MatrixCombinationsShortcut> shortcutList;

    /**
     * @return groovy expression to specify default checked combinations
     */
    public String getDefaultCombinationFilter() {
        return defaultCombinationFilter;
    }

    /**
     * @return list of shortcuts
     * @since 1.1.0
     */
    @NonNull
    public List<MatrixCombinationsShortcut> getShortcutList() {
        return shortcutList;
    }

    /**
     * @return list of shortcuts that should be used as defaults
     * @since 1.1.0
     */
    public static List<MatrixCombinationsShortcut> getDefaultShortcutList() {
        return Arrays.asList(
                new ResultShortcut("Successful", false, Result.SUCCESS),
                new ResultShortcut("Failed", false, Result.FAILURE),
                new MatrixCombinationsShortcut.All(),
                new MatrixCombinationsShortcut.None());
    }

    /**
     * ctor
     *
     * @param name the name of the parameter
     * @param description the description for the parameter
     * @param defaultCombinationFilter combinations filter to used to calculate default checks
     * @param shortcutList the list of shortcuts to display
     * @since 1.1.0
     */
    @DataBoundConstructor
    public MatrixCombinationsParameterDefinition(
            String name,
            String description,
            String defaultCombinationFilter,
            List<MatrixCombinationsShortcut> shortcutList) {
        super(name);
        setDescription(description);
        this.defaultCombinationFilter =
                !StringUtils.isBlank(defaultCombinationFilter) ? defaultCombinationFilter : null;
        this.shortcutList = (shortcutList != null) ? shortcutList : getDefaultShortcutList();
    }

    public MatrixCombinationsParameterDefinition(String name, String description, String defaultCombinationFilter) {
        this(name, description, defaultCombinationFilter, getDefaultShortcutList());
    }

    public MatrixCombinationsParameterDefinition(String name, String description) {
        this(name, description, null);
    }

    @Serial
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "This is not normal.")
    private Object readResolve() {
        boolean shortcutListMissing = false;
        if (shortcutList == null) {
            shortcutListMissing = true;
        } else {
            try {
                shortcutList.toString();
            } catch (Exception x) { // e.g., NPE because Arrays.ArrayList.a == null
                shortcutListMissing = true;
            }
        }
        if (shortcutListMissing) {
            // the one from < 1.1.0
            return new MatrixCombinationsParameterDefinition(
                    getName(), getDescription(), getDefaultCombinationFilter());
        }
        return this;
    }

    @Override
    public MatrixCombinationsParameterValue createValue(StaplerRequest2 req, JSONObject jo) {
        MatrixCombinationsParameterValue value = req.bindJSON(MatrixCombinationsParameterValue.class, jo);
        value.setDescription(getDescription());
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatrixCombinationsParameterValue createValue(String value) {
        return new DefaultMatrixCombinationsParameterValue(getName(), getDescription(), value);
    }

    @Override
    public MatrixCombinationsParameterValue getDefaultParameterValue() {
        return createValue(getDefaultCombinationFilter());
    }

    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {

        @NonNull
        @Override
        public String getDisplayName() {
            return "Matrix Combinations Parameter";
        }

        @Override
        public String getHelpFile() {
            return "/plugin/matrix-configuration-parameter/help.html";
        }

        /**
         * @return list of shortcuts that should be used as defaults
         * @since 1.1.0
         */
        public List<MatrixCombinationsShortcut> getDefaultShortcutList() {
            return MatrixCombinationsParameterDefinition.getDefaultShortcutList();
        }

        public List<MatrixCombinationsShortcutDescriptor> getShortcutDescriptorList() {
            return MatrixCombinationsShortcutDescriptor.all();
        }
    }
}
