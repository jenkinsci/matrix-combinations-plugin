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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import hudson.markup.MarkupFormatter;
import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.model.*;

import hudson.util.VariableResolver;
import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.base.Function;
import com.google.common.collect.Lists;


public class MatrixCombinationsParameterValue extends ParameterValue {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(MatrixCombinationsParameterValue.class.getName());

    private List<String> combinations;

    @Deprecated
    transient Boolean[] values;
    @Deprecated
    transient String[] confs;

    /**
     * ctor
     *
     * @param name name of parameter
     * @param combinations combinations to build
     * @since 1.1.0
     */
    @DataBoundConstructor
    public MatrixCombinationsParameterValue(String name, String description, List<String> combinations) {
        super(name,  description);
        this.combinations = (combinations != null)
            ? Collections.unmodifiableList(combinations)
            : Collections.<String>emptyList();
    }

    @Deprecated
    public MatrixCombinationsParameterValue(String name, Boolean[] values, String[] confs) {
        this(name, values, confs, null);
    }

    @Deprecated
    public MatrixCombinationsParameterValue(String name, Boolean[] values, String[] confs, String description) {
        this(name, description, convertValuesAndConfs(values, confs));
    }

    @Deprecated
    public Boolean[] getValues() {
        return (values != null) ? values.clone() : null;
    }

    @Deprecated
    public String[] getConfs() {
        return (confs != null) ? confs.clone() : null;
    }

    /**
     * @return combinations to build
     * @since 1.1.0
     */
    @Nonnull
    public List<String> getCombinations() {
        return combinations;
    }

    protected Object readResolve() {
        if (combinations == null) {
            // < 1.1.0
            this.combinations = convertValuesAndConfs(this.values, this.confs);
            this.confs = null;
            this.values = null;
        }
        return this;
    }

    private static List<String> convertValuesAndConfs(Boolean[] values, String[] confs) {
        List<String> ret = new ArrayList<String>();

        if (values == null || confs == null) {
            return ret;
        }

        for (int i = 0; i < values.length; ++i) {
            if (values[i] != null && values[i]) {
                ret.add(confs[i]);
            }
        }
        return ret;
    }

    @Override
    public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) {
        return new VariableResolver<String>() {
            public String resolve(String name) {
                if (!MatrixCombinationsParameterValue.this.name.equals(name)) {
                    return null;
                }

                return StringUtils.join(Lists.transform(
                    getCombinations(),
                    new Function<String, String>() {
                        public String apply(String combination) {
                            return String.format(
                                "(%s')",
                                combination.replace("=", " == '").replace(",", "' && ")
                            );
                        }
                    }
                ), " || ");
            }
        };
    }

    public boolean combinationExists(AxisList axes, Combination c){
        return getCombinations().contains(c.toString());
    }

    @Deprecated
    public boolean combinationExists(Combination c){
        return combinationExists(null, c);
    }

    @Override
    public int hashCode() {
        final int prime = 71;
        int result = super.hashCode();
        result = prime * result;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MatrixCombinationsParameterValue other = (MatrixCombinationsParameterValue)obj;

        return getCombinations().equals(other.getCombinations());
    }

    @Override
    public String toString() {
        StringBuffer valueStr= new StringBuffer("");
        valueStr.append("(MatrixCombinationsParameterValue) " + getName()+"\n");
        for (String combination: getCombinations()) {
            valueStr.append(String.format("%s%n", combination));
        }
        return valueStr.toString();
    }

    /**
     * return parameter description, applying the configured {@link MarkupFormatter} for jenkins instance.
     *
     * This is a backport from Jenkins-2.44 or Jenkins-2.32.2.
     *
     * @since 1.2.0
     */
    public String getFormattedDescription() {
        try {
            return Jenkins.getInstance().getMarkupFormatter().translate(getDescription());
        } catch (IOException e) {
            LOGGER.log(
                Level.WARNING,
                "failed to translate description using configured markup formatter: {0}",
                getDescription()
            );
            return "";
        }
    }

}
