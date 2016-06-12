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

import java.util.ArrayList;
import java.util.List;

import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.model.*;

import hudson.util.VariableResolver;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;


public class MatrixCombinationsParameterValue extends ParameterValue {

    Boolean[] values;
    String[] confs;

    @DataBoundConstructor
    public MatrixCombinationsParameterValue(String name, Boolean[] values, String[] confs) {
        this(name, values, confs, null);
    }

    public MatrixCombinationsParameterValue(String name, Boolean[] values, String[] confs, String description) {
        super(name,  description);
        this.values = (values != null) ? values.clone() : null;
        this.confs = (confs != null) ? confs.clone() : null;
    }

    public Boolean[] getValues() {
        return (values != null) ? values.clone() : null;
    }

    public String[] getConfs() {
        return (confs != null) ? confs.clone() : null;
    }

    @Override
    public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) {
        return new VariableResolver<String>() {
            public String resolve(String name) {
                if (!MatrixCombinationsParameterValue.this.name.equals(name)) {
                    return null;
                }

                List<String> conds = new ArrayList<String>();

                for (int uniqueIdIndex= 0 ; uniqueIdIndex < values.length ; uniqueIdIndex++){
                    Boolean value=values[uniqueIdIndex];
                    String conf = confs[uniqueIdIndex];

                    if (value.booleanValue()){
                        conds.add("("+conf.replace("="," == '").replace(",","' && ")+"')");

                    }


                }
                return StringUtils.join(conds, " || ");


            }
        };
    }
    public boolean combinationExists(AxisList axes, Combination c){


        if (values == null || confs == null || values.length != confs.length)
            return false;

        for (int i = 0; i < values.length ; i++){
            if (confs[i].equals(c.toString()) && values[i]==true)
                return true;
        }
        return false;
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

        if (values.length!= other.getValues().length) {
            return false;
        }
        for (int i=0; i< values.length;i++){
            if (values[i].booleanValue()!=other.getValues()[i].booleanValue())
                return false;
        }

        if (confs.length!= other.getConfs().length) {
            return false;
        }
        for (int i=0; i< values.length;i++){
            if (!confs[i].equals(other.getConfs()[i]))
                return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuffer valueStr= new StringBuffer("");
        valueStr.append("(MatrixCombinationsParameterValue) " + getName()+"\n");
        for (int i=0; i< values.length; i++)
        {
            valueStr.append(String.format("%s:%s%n",confs[i],values[i]));
        }
        return valueStr.toString();
    }
}
