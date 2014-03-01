/*
 * The MIT License
 * 
 * Copyright (c) 2013 IKEDA Yasuyuki
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
import hudson.Util;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixBuild;
import hudson.matrix.listeners.MatrixBuildListener;
import hudson.model.ParametersAction;

/**
 *
 */
@Extension
public class MatrixCombinationsParameterMatrixBuildListener extends MatrixBuildListener {
    
    /**
     * If {@link MatrixCombinationsParameterValue} is assigned to MatrixBuild,
     * filter MatrixConfiguration with that value.
     * 
     * @param b
     * @param c
     * @return whether this configuration should run.
     * @see hudson.matrix.listeners.MatrixBuildListener#doBuildConfiguration(hudson.matrix.MatrixBuild, hudson.matrix.MatrixConfiguration)
     */
    @Override
    public boolean doBuildConfiguration(MatrixBuild b, MatrixConfiguration c) {
        ParametersAction paction = b.getAction(ParametersAction.class);
        if (paction == null) {
            return true;
        }
        for (MatrixCombinationsParameterValue value
                :Util.filter(paction.getParameters(), MatrixCombinationsParameterValue.class)) {
            if (!value.combinationExists(b.getParent().getAxes(), c.getCombination())) {
                return false;
            }
        }
        
        return true;
    }
    
}
