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

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;


public class MatrixCombinationsParameterDefinition extends ParameterDefinition {

    private static final long serialVersionUID = 1L;
    private final String defaultCombinationFilter;

    /**
     * @return groovy expression to specify default checked combinations
     */
    public String getDefaultCombinationFilter() {
        return defaultCombinationFilter;
    }

    @DataBoundConstructor
    public MatrixCombinationsParameterDefinition(String name, String description, String defaultCombinationFilter) {
        super(name, description);
        this.defaultCombinationFilter = !StringUtils.isBlank(defaultCombinationFilter)?defaultCombinationFilter:null;
    }

    public MatrixCombinationsParameterDefinition(String name, String description) {
        this(name, description, null);
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


    }

}
