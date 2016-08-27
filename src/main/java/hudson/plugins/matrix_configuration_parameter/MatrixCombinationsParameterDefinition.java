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

import hudson.Extension;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.Result;
import hudson.plugins.matrix_configuration_parameter.shortcut.MatrixCombinationsShortcut;
import hudson.plugins.matrix_configuration_parameter.shortcut.MatrixCombinationsShortcutDescriptor;
import hudson.plugins.matrix_configuration_parameter.shortcut.ResultShortcut;
import net.sf.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


public class MatrixCombinationsParameterDefinition extends ParameterDefinition {

    private static final long serialVersionUID = 1L;
    private final String defaultCombinationFilter;
    @SuppressFBWarnings(value="SE_BAD_FIELD")
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
    @Nonnull
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
            new MatrixCombinationsShortcut.None()
        );
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
    public MatrixCombinationsParameterDefinition(String name, String description, String defaultCombinationFilter, List<MatrixCombinationsShortcut> shortcutList) {
        super(name, description);
        this.defaultCombinationFilter = !StringUtils.isBlank(defaultCombinationFilter)?defaultCombinationFilter:null;
        this.shortcutList = (shortcutList != null) ? shortcutList : getDefaultShortcutList();
    }

    public MatrixCombinationsParameterDefinition(String name, String description, String defaultCombinationFilter) {
        this(name, description, defaultCombinationFilter, getDefaultShortcutList());
    }

    public MatrixCombinationsParameterDefinition(String name, String description) {
        this(name, description, null);
    }

    private Object readResolve() {
        if (this.shortcutList == null) {
            // the one from < 1.1.0
            return new MatrixCombinationsParameterDefinition(getName(), getDescription(), getDefaultCombinationFilter());
        }
        return this;
    }

    @Override
    public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
        MatrixCombinationsParameterValue value = req.bindJSON(MatrixCombinationsParameterValue.class, jo);
        value.setDescription(getDescription());
        return value;
    }

    @Override
    public ParameterValue createValue(StaplerRequest req) {
        String[] value = req.getParameterValues(getName());
        if (value == null || value.length < 1) {
            return getDefaultParameterValue();
        } else {
            return new MatrixCombinationsParameterValue(getName(),new Boolean[]{},new String[]{});
        }
    }


    

    @Override
    public MatrixCombinationsParameterValue getDefaultParameterValue() {
        MatrixCombinationsParameterValue v = new DefaultMatrixCombinationsParameterValue(getName(), getDescription(), getDefaultCombinationFilter());
        return v;
    }

    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {



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
