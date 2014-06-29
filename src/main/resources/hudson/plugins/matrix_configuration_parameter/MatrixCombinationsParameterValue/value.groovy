package hudson.plugins.matrix_configuration_parameter.matrixcombinationparametervalue

import hudson.matrix.AxisList
import hudson.matrix.Combination
import hudson.matrix.Layouter
import hudson.matrix.MatrixBuild
import hudson.matrix.MatrixProject
import hudson.plugins.matrix_configuration_parameter.MatrixCombinationsParameterValue
import lib.LayoutTagLib
import org.kohsuke.stapler.jelly.groovy.Namespace

l = namespace(LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")
f = namespace("lib/form")

MatrixProject project = request.findAncestorObject(MatrixProject.class);
AxisList axes = project.getAxes();
MatrixBuild build = request.findAncestorObject(MatrixBuild.class);
if (build == null) //in case you are looking at a specific run, MatrixRun Ancestor will replace the MatrixBuild
    return;
MatrixCombinationsParameterValue valueIt = it;
Layouter layouter = build.getLayouter();
if (layouter==null)
    return;

drawParameterBody(f, valueIt, axes, project, layouter);



private void drawParameterBody(Namespace f,MatrixCombinationsParameterValue valueIt,AxisList axes,MatrixProject project,Layouter layouter) {
    h2() { raw("Matrix Combinations") }

    f.entry(title: valueIt.getName(), description: it.getDescription()) {
        div(name: "parameter") {
            input(type: "hidden", name: "name", value: valueIt.getName())
            table(border: "1", class: "middle-align center-align", id: "configuration-matrix") {

                drawTableHeader(layouter);

                drawTableBody(layouter, axes, valueIt, project);


            }//table
        }//div
    }
}

private void drawTableBody(Layouter layouter,AxisList axes,MatrixCombinationsParameterValue valueIt,MatrixProject project) {
    for (row in layouter.rows) {
        tr() {
            int i = 0;
            for (y in layouter.y) {
                if (row.drawYHeader(i) != null) {
                    td(rowspan: layouter.height(i), class: "matrix-leftcolumn") { raw(row.drawYHeader(i)) }

                }

                i++;
            }
            for (c in row) {
                td() {
                    for (p in c) {
                        div() {
                            drawTableBall(p, axes, valueIt, project);
                        }


                    }


                }

            }
        }
    }
}

private void drawTableHeader(Layouter layouter) {
    int i = 0;
    for (x in layouter.x) {
        tr(class: "matrix-row") {
            if (!layouter.y.isEmpty()) {
                td(colspan: +layouter.y.size(), id: "matrix-title") { raw("Configuration Matrix") }
            }
            for (row in 1..layouter.repeatX(i)) {
                for (axis in x.values) {
                    td(class: "matrix-header", colspan: layouter.width(i)) { raw(axis) }
                }
            }
        }
        i++;
    }

}//entry

private void drawTableBall(MatrixBuild.RunPtr runPtr,AxisList axes,MatrixCombinationsParameterValue matrixValue,MatrixProject project) {

    run = runPtr.getRun();
    result = matrixValue.combinationExists(axes, runPtr.combination);
    if (result){
        a(href:rootURL+"/"+run.getUrl()){
            img(src: "${imagesURL}/24x24/"+run.getBuildStatusUrl());
            f.checkbox(checked: "true",onclick:"return false;", onkeydown:"return false;", name: "values",id: String.format("checkbox%s-%s", matrixValue.getName(), runPtr.combination.toString('-' as char, '-' as char)));
            input(type: "hidden", name: "confs", value: runPtr.combination.toString());
        }

    } else {
        img(src: "${imagesURL}/24x24/grey.gif");
        f.checkbox(checked: "false",onclick:"return false;", onkeydown:"return false;", name: "values",id: String.format("checkbox%s-%s", matrixValue.getName(), runPtr.combination.toString('-' as char, '-' as char)));
        input(type: "hidden", name: "confs", value: runPtr.combination.toString());
    }
}
