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

import java.util.Collection;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import hudson.Extension;
import hudson.Util;
import hudson.matrix.Combination;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;

/**
 * Shortcuts to select combinations with a filter expression
 */
public class CombinationFilterShortcut extends MatrixCombinationsShortcut {
    private final String name;
    private final String combinationFilter;

    /**
     * ctor
     *
     * @param name display name of the link
     * @param combinationFilter filter expression
     */
    @DataBoundConstructor
    public CombinationFilterShortcut(String name, String combinationFilter) {
        this.name = Util.fixNull(name);
        this.combinationFilter = Util.fixNull(combinationFilter);
    }

    /**
     * @return filter expression
     */
    @Nonnull
    public String getCombinationFilter() {
        return combinationFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Collection<Combination> getCombinations(
        @Nonnull final MatrixProject project,
        @CheckForNull MatrixBuild build
    ) {
        return Collections2.filter(
            Collections2.transform(
                project.getActiveConfigurations(),
                new Function<MatrixConfiguration, Combination>() {
                    public Combination apply(MatrixConfiguration c) {
                        return c.getCombination();
                    }
                }
            ),
            new Predicate<Combination>() {
                public boolean apply(Combination c) {
                    return c.evalGroovyExpression(project.getAxes(), getCombinationFilter());
                }
            }
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return String.format(
            "filter-%s",
            getName().replaceAll("[^A-Za-z0-9]+", "-")
        );
    }

    /**
     * Descriptor for {@link CombinationFilterShortcut}
     */
    @Extension
    public static class DescriptorImpl extends MatrixCombinationsShortcutDescriptor {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.CombinationFilterShortcut_DisplayName();
        }
    }
}
