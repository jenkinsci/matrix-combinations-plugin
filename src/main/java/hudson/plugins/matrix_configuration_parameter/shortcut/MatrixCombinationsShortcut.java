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
import java.util.Collections;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.matrix.Combination;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Result;

/**
 * Defines shortcut link to select a set of axes combinations.
 *
 * @since 1.1.0
 */
public abstract class MatrixCombinationsShortcut
        extends AbstractDescribableImpl<MatrixCombinationsShortcut> implements ExtensionPoint
{
    /**
     * Return combinations to check for the build
     *
     * @param project the target project.
     * @param build the target build.
     *     Latest build for a new build.
     *     {@code null} if there's no builds.
     * @return combinations to check.
     */
    public abstract Collection<Combination> getCombinations(
        @Nonnull MatrixProject project,
        @CheckForNull MatrixBuild build
    );

    /**
     * Return a value used for javascript.
     *
     * @param project the target project.
     * @param build the target build
     * @return comma-separated list of combination indices
     */
    public final String getCombinationsData(
        @Nonnull final MatrixProject project,
        @CheckForNull MatrixBuild build
    ) {
        return StringUtils.join(Collections2.transform(
            getCombinations(project, build),
            new Function<Combination, String>() {
                public String apply(Combination c) {
                    return Integer.toString(c.toIndex(project.getAxes()));
                }
            }
        ), ',');
    }

    /**
     * @return name used for the link text.
     */
    public String getName() {
        return getDescriptor().getDisplayName();
    }

    /**
     * @return name used to distinguish links.
     */
    public String getLinkId() {
        return getDescriptor().getId().replace('.', '-').replace('$', '-');
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatrixCombinationsShortcutDescriptor getDescriptor() {
        return (MatrixCombinationsShortcutDescriptor)super.getDescriptor();
    }

    /**
     * Checks all combinations with successful builds.
     */
    public static class Successful extends MatrixCombinationsShortcut {
        /**
         * ctor
         */
        @DataBoundConstructor
        public Successful() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<Combination> getCombinations(
            @Nonnull MatrixProject project,
            @CheckForNull MatrixBuild build
        ) {
            if (build == null) {
                return Collections.emptyList();
            }
            return Collections2.transform(
                Collections2.filter(
                    build.getRuns(),
                    new Predicate<MatrixRun>() {
                        public boolean apply(MatrixRun r) {
                            Result result = r.getResult();
                            return (result != null)
                                && result.isBetterOrEqualTo(Result.SUCCESS);
                        }
                    }
                ),
                new Function<MatrixRun, Combination>() {
                    public Combination apply(MatrixRun r) {
                        return r.getParent().getCombination();
                    }
                }
            );
        }

        /**
         * Descriptor for {@link Successful}
         */
        @Extension(ordinal=140) // Top Most
        public static class DescriptorImpl extends MatrixCombinationsShortcutDescriptor {
            /**
             * {@inheritDoc}
             */
            @Override
            public String getDisplayName() {
                return "Successful";
            }
        }
    }

    /**
     * Checks all combinations with unstable or more worse builds.
     */
    public static class Failed extends MatrixCombinationsShortcut {
        /**
         * ctor
         */
        @DataBoundConstructor
        public Failed() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<Combination> getCombinations(
            @Nonnull MatrixProject project,
            @CheckForNull MatrixBuild build
        ) {
            if (build == null) {
                return Collections.emptyList();
            }
            return Collections2.transform(
                Collections2.filter(
                    build.getRuns(),
                    new Predicate<MatrixRun>() {
                        public boolean apply(MatrixRun r) {
                            Result result = r.getResult();
                            return (result != null)
                                && result.isWorseThan(Result.UNSTABLE);
                        }
                    }
                ),
                new Function<MatrixRun, Combination>() {
                    public Combination apply(MatrixRun r) {
                        return r.getParent().getCombination();
                    }
                }
            );
        }

        /**
         * Descriptor for {@link Failed}
         */
        @Extension(ordinal=130) // Next to Successful
        public static class DescriptorImpl extends MatrixCombinationsShortcutDescriptor {
            /**
             * {@inheritDoc}
             */
            @Override
            public String getDisplayName() {
                return "Failed";
            }
        }
    }

    /**
     * Checks all combinations.
     */
    public static class All extends MatrixCombinationsShortcut {
        /**
         * ctor
         */
        @DataBoundConstructor
        public All() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<Combination> getCombinations(
            @Nonnull MatrixProject project,
            @CheckForNull MatrixBuild build
        ) {
            return Collections2.transform(
                project.getActiveConfigurations(),
                new Function<MatrixConfiguration, Combination>() {
                    public Combination apply(MatrixConfiguration c) {
                        return c.getCombination();
                    }
                }
            );
        }

        /**
         * Descriptor for {@link All}
         */
        @Extension(ordinal=120) // Next to Failed
        public static class DescriptorImpl extends MatrixCombinationsShortcutDescriptor {
            /**
             * {@inheritDoc}
             */
            @Override
            public String getDisplayName() {
                return "All";
            }
        }
    }

    /**
     * Uncheck all
     */
    public static class None extends MatrixCombinationsShortcut {
        /**
         * ctor
         */
        @DataBoundConstructor
        public None() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<Combination> getCombinations(
            @Nonnull MatrixProject project,
            @CheckForNull MatrixBuild build
        ) {
            return Collections.emptyList();
        }

        /**
         * Descriptor for {@link None}
         */
        @Extension(ordinal=110) // Next to All
        public static class DescriptorImpl extends MatrixCombinationsShortcutDescriptor {
            /**
             * {@inheritDoc}
             */
            @Override
            public String getDisplayName() {
                return "None";
            }
        }
    }
}
