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

import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.model.AbstractBuild;
import hudson.util.VariableResolver;

/**
 * {@link MatrixCombinationsParameterValue} created when the build started
 * without specifying parameter.
 * 
 * {@link MatrixCombinationsParameterValue#getConfs()} and {@link MatrixCombinationsParameterValue#getValues()}
 * does not work (always return an empty array).
 */
public class DefaultMatrixCombinationsParameterValue extends MatrixCombinationsParameterValue {
    private static final long serialVersionUID = -812826069693143705L;
    private final String combinationFilter;
    
    public DefaultMatrixCombinationsParameterValue(String name, String description, String combinationFilter) {
        super(name, new Boolean[0], new String[0], description);
        this.combinationFilter = combinationFilter;
    }
    
    @Override
    public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) {
        return new VariableResolver<String>() {
            public String resolve(String name) {
                if (!DefaultMatrixCombinationsParameterValue.this.name.equals(name)) {
                    return null;
                }
                return (combinationFilter != null)?combinationFilter:"";
            }
        };
    }
    
    /**
     * @param axes
     * @param c
     * @return true if that combination should be built.
     * @see hudson.plugins.matrix_configuration_parameter.MatrixCombinationsParameterValue#combinationExists(hudson.matrix.AxisList, hudson.matrix.Combination)
     */
    @Override
    public boolean combinationExists(AxisList axes, Combination c){
        if (axes == null || combinationFilter == null) {
            // when axes is null, the combination filter cannot be evaluated
            // when combination filter is null, allow all combination.
            return true;
        }
        return c.evalGroovyExpression(axes, combinationFilter);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(getClass().equals(obj.getClass()))) {
            return false;
        }
        DefaultMatrixCombinationsParameterValue other = (DefaultMatrixCombinationsParameterValue)obj;
        
        if (combinationFilter == null) {
            return other.combinationFilter == null;
        }
        
        return combinationFilter.equals(other.combinationFilter);
    }
    
    @Override
    public String toString() {
        return String.format("(%s) %s: %s", 
                getClass().getName(), getName(), combinationFilter);
    }
}
