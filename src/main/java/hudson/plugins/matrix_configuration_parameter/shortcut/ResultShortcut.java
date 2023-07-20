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
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.Util;
import hudson.matrix.Combination;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.model.Result;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Shortcut to select combinations based on build results
 *
 * @since 1.1.0
 */
public class ResultShortcut extends MatrixCombinationsShortcut {
    private final String name;
    private final boolean exact;
    private final List<String> resultsToCheck;

    /**
     * ctor
     *
     * @param name name to display
     * @param exact whether to test exact child builds
     * @param resultsToCheck names of results to check
     */
    @DataBoundConstructor
    public ResultShortcut(String name, boolean exact, List<String> resultsToCheck) {
        this.name = Util.fixNull(name);
        this.exact = exact;
        this.resultsToCheck =
                (resultsToCheck != null) ? new ArrayList<>(resultsToCheck) : Collections.<String>emptyList();
    }

    /**
     * ctor
     *
     * @param name name to display
     * @param exact whether to test exact child builds
     * @param results results to check
     */
    public ResultShortcut(String name, boolean exact, Result... results) {
        this(name, exact, Lists.transform(Arrays.asList(results), new Function<Result, String>() {
            @Override
            public String apply(Result result) {
                return result.toString();
            }
        }));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    /**
     * @return whether to test exact child builds
     */
    public boolean isExact() {
        return exact;
    }

    /**
     * @return results to check
     */
    public List<String> getResultsToCheck() {
        return resultsToCheck;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return StringUtils.join(getResultsToCheck(), '-');
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Combination> getCombinations(MatrixProject project, MatrixBuild build) {
        if (build == null) {
            return Collections.emptyList();
        }
        return Collections2.transform(
                Collections2.filter(isExact() ? build.getExactRuns() : build.getRuns(), new Predicate<MatrixRun>() {
                    @Override
                    public boolean apply(MatrixRun run) {
                        Result result = run.getResult();
                        if (result == null) {
                            return false;
                        }
                        for (String s : getResultsToCheck()) {
                            if (result.equals(Result.fromString(s))) {
                                return true;
                            }
                        }
                        return false;
                    }
                }),
                new Function<MatrixRun, Combination>() {
                    @Override
                    public Combination apply(MatrixRun r) {
                        return r.getParent().getCombination();
                    }
                });
    }

    @Extension
    public static class DescriptorImpl extends MatrixCombinationsShortcutDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.ResultShortcut_DisplayName();
        }
    }
}
