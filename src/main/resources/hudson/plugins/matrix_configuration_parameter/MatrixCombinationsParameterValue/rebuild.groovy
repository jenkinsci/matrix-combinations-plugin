package hudson.plugins.matrix_configuration_parameter.MatrixCombinationsParameterValue


import hudson.matrix.AxisList
import hudson.matrix.Layouter
import hudson.matrix.MatrixBuild
import hudson.matrix.MatrixProject
import lib.LayoutTagLib
import org.kohsuke.stapler.jelly.groovy.Namespace

l = namespace(LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")
f = namespace("lib/form")
nsProject = namespace("/lib/hudson/project")


MatrixProject project = request.findAncestorObject(MatrixProject.class);
AxisList axes = project.getAxes();
MatrixBuild build = request.findAncestorObject(MatrixBuild.class);
if (build == null) //in case you are looking at a specific run, MatrixRun Ancestor will replace the MatrixBuild
    return;
def valueIt = it;
Layouter layouter = build.getLayouter();
if (layouter==null)
    return;

drawParameterBody(f, valueIt, axes, project, layouter);



private void drawParameterBody(Namespace f,valueIt,AxisList axes,MatrixProject project,Layouter layouter) {
    h2() { raw("Matrix Combinations") }

    f.entry(title: valueIt.getName(), description: it.getDescription()) {
        div(name: "parameter") {
            input(type: "hidden", name: "name", value: valueIt.getName())
            nsProject.matrix(it: build) {
              drawTableBall(p, project.axes, valueIt, project, layouter);
            }
        }//div
    }
}

private void drawTableBall(MatrixBuild.RunPtr runPtr,AxisList axes,matrixValue,MatrixProject project,Layouter layouter) {

    run = runPtr.getRun();
    result = matrixValue.combinationExists(runPtr.combination);
    if (result){
        a(href:request.getRootPath()+"/"+run.getUrl()){
            img(src: "${imagesURL}/24x24/"+run.getBuildStatusUrl());
            if (!layouter.x || !layouter.y) {
              text(runPtr.combination.toString(layouter.z))
            }
            f.checkbox(checked: "true", name: "values",id: "checkbox"+matrixValue.getName());
            input(type: "hidden", name: "confs", value: runPtr.combination.toString());
        }

    } else {
        img(src: "${imagesURL}/24x24/grey.gif");
        if (!layouter.x || !layouter.y) {
          text(runPtr.combination.toString(layouter.z))
        }
        f.checkbox(checked: "false", name: "values",id: "checkbox"+matrixValue.getName());
        input(type: "hidden", name: "confs", value: runPtr.combination.toString());
    }
}
