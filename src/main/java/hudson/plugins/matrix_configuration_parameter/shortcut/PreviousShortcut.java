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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.matrix.Combination;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Shortcut to check combinations used in the previous build
 */
public class PreviousShortcut extends MatrixCombinationsShortcut {
    @DataBoundConstructor
    public PreviousShortcut() {}

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Collection<Combination> getCombinations(@Nonnull MatrixProject project, @CheckForNull MatrixBuild build) {
        if (build == null) {
            return Collections.emptyList();
        }
        return Lists.transform(build.getExactRuns(), new Function<MatrixRun, Combination>() {
            @Override
            public Combination apply(MatrixRun r) {
                return r.getParent().getCombination();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getName() {
        return getDescriptor().getDisplayName();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getId() {
        return "Previous";
    }

    /**
     * Descriptor for {@link PreviousShortcut}
     */
    @Extension
    public static class DescriptorImpl extends MatrixCombinationsShortcutDescriptor {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.PreviousShortcut_DisplayName();
        }
    }
}
