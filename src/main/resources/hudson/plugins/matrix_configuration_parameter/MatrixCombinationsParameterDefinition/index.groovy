package hudson.plugins.matrix_configuration_parameter.matrixcombinationparameterDefinition

import hudson.matrix.AxisList
import hudson.matrix.Combination
import hudson.matrix.Layouter
import hudson.matrix.MatrixProject
import hudson.matrix.MatrixBuild
import hudson.matrix.MatrixRun
import hudson.model.Result
import hudson.plugins.matrix_configuration_parameter.MatrixCombinationsParameterDefinition
import lib.LayoutTagLib
import org.kohsuke.stapler.jelly.groovy.Namespace

l = namespace(LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")
f = namespace("lib/form")
nsProject = namespace("/hudson/plugins/matrix_configuration_parameter/taglib")


def paramDef = it;
String nameIt = it.getName();
MatrixProject project = request.findAncestorObject(MatrixProject.class);
if (project == null) {
   //in case project is not a Matrix Project
    f.entry(title: nameIt, description: it.getDescription()) {
        div(name: "parameter") {
            input(type: "hidden", name: "name", value: nameIt)
            text(_("Not applicable. Applicable only to multi-configuration projects."))
        }//div
    }
    return;
}

AxisList axes =  project.getAxes();
Layouter layouter = new Layouter<Combination>(axes) {
    protected Combination getT(Combination c) {
        return c;
    }
};





drawMainBody(paramDef, f, nameIt, axes, project, project.lastBuild, layouter)

private void drawMainBody(MatrixCombinationsParameterDefinition paramDef, Namespace f, String nameIt, AxisList axes,MatrixProject project,MatrixBuild build,Layouter layouter) {

    drawMainLinksJS(nameIt)


    f.entry(title: nameIt, description: it.getDescription()) {
        div(name: "parameter", class: "matrix-combinations-parameter") {
            input(type: "hidden", name: "name", value: nameIt)
            nsProject.matrix(it: project, layouter: layouter) {
              drawMainBall(paramDef, p, project.axes, nameIt, project, layouter);
            }
            nsProject.shortcut(parameter: paramDef, project: project, build: build);
        }//div
    }
}

private void drawMainBall(MatrixCombinationsParameterDefinition paramDef, Combination combination,AxisList axes,String matrixName,MatrixProject project,Layouter layouter) {

    lastBuild = project.getLastBuild();
    if (lastBuild != null && lastBuild.getRun(combination)!=null){
        lastRun = lastBuild.getRun(combination);
        if (lastRun != null){
            a(href:rootURL+"/"+lastRun.getUrl()){
            img(src: "${imagesURL}/24x24/"+lastRun.getBuildStatusUrl())
            if (!layouter.x || !layouter.y) {
              text(combination.toString(layouter.z))
            }
            }
            checked = combination.evalGroovyExpression(axes, paramDef.defaultCombinationFilter?:project.combinationFilter)
            span(class: "combination", "data-combination": combination.toIndex(axes)) {
                f.checkbox(checked: checked, name: "values")
                input(type: "hidden", name: "confs", value: combination.toString())
            }
        }

    } else{
        img(src: "${imagesURL}/24x24/grey.gif")
        if (!layouter.x || !layouter.y) {
          text(combination.toString(layouter.z))
        }
        
        checked = combination.evalGroovyExpression(axes, paramDef.defaultCombinationFilter?:project.combinationFilter)
        span(class: "combination", "data-combination": combination.toIndex(axes)) {
            f.checkbox(checked: checked, name: "values")
            input(type: "hidden", name: "confs", value: combination.toString())
        }
    }

}
