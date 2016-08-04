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

import hudson.model.Result;

def f = namespace("lib/form");
f.entry(field: "name", title: _("Name")) {
    f.textbox();
}
f.entry(field: "exact", title: _("Only combinations run exactly")) {
    f.checkbox();
}
f.entry(field: "resultsToCheck", title: _("Results to check")) {
    // Result.all is private.
    for (Result r: [Result.SUCCESS, Result.UNSTABLE, Result.FAILURE, Result.NOT_BUILT, Result.ABORTED]) {
        f.checkbox(
            json: r.toString(),
            checked: instance.resultsToCheck.contains(r.toString()),
            title: r.toString()
        );
    }
}
