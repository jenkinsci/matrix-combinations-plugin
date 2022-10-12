package hudson.plugins.matrix_configuration_parameter.MatrixCombinationsParameterValue


import hudson.matrix.AxisList
import hudson.matrix.Combination
import hudson.matrix.Layouter
import hudson.matrix.MatrixBuild
import hudson.matrix.MatrixProject
import hudson.model.ParametersDefinitionProperty
import hudson.plugins.matrix_configuration_parameter.MatrixCombinationsParameterDefinition
import lib.LayoutTagLib
import org.kohsuke.stapler.jelly.groovy.Namespace

l = namespace(LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")
f = namespace("lib/form")
nsProject = namespace("/hudson/plugins/matrix_configuration_parameter/taglib")


def valueIt = it;

MatrixProject project = request.findAncestorObject(MatrixProject.class);
MatrixBuild build = request.findAncestorObject(MatrixBuild.class);
if (project == null || build == null) {
    //in case you are looking at a specific run, MatrixRun Ancestor will replace the MatrixBuild
    set("escapeEntryTitleAndDescription", false);
    f.entry(title: h.escape(valueIt.name), description: it.formattedDescription) {
        // In the case the parameter is not defined in this project,
        // sending parameters cause rebuild-plugin throws exception.
        // Acts as if I'm not here.
        text(_("Not applicable. Applicable only to multi-configuration projects."))
    }
    return;
}
AxisList axes = project.getAxes();
Layouter layouter = new Layouter<Combination>(axes) {
    protected Combination getT(Combination c) {
        return c;
    }
};

def parameterDefinition = project.getProperty(ParametersDefinitionProperty.class).getParameterDefinition(valueIt.name);
if (!(parameterDefinition instanceof MatrixCombinationsParameterDefinition)) {
    parameterDefinition = null;
}

drawParameterBody(parameterDefinition, f, valueIt, axes, project, build, layouter);



private void drawParameterBody(MatrixCombinationsParameterDefinition paramDef, Namespace f,valueIt,AxisList axes,MatrixProject project,MatrixBuild build,Layouter layouter) {
    set("escapeEntryTitleAndDescription", false);
    f.entry(title: h.escape(valueIt.name), description: it.formattedDescription) {
        div(name: "parameter", class: "matrix-combinations-parameter") {
            input(type: "hidden", name: "name", value: valueIt.getName())
            nsProject.matrix(it: build, layouter: layouter) {
              drawTableBall(p, project.axes, valueIt, project, build, layouter);
            }
            if (paramDef != null) {
              nsProject.shortcut(parameter: paramDef, project: project, build: build);
            }
        }//div
    }
}

private void drawTableBall(Combination combination,AxisList axes,matrixValue,MatrixProject project,MatrixBuild build,Layouter layouter) {

    run = build.getRun(combination);
    result = matrixValue.combinationExists(axes, combination);
    if (run != null && result){
        a(href:rootURL+"/"+run.getUrl()){
            l.icon(class:"icon-md "+run.getIconColor().getIconClassName())
            img(src: "${imagesURL}/24x24/"+run.getBuildStatusUrl());
            if (!layouter.x || !layouter.y) {
              text(combination.toString(layouter.z))
            }
        }
        span(class: "combination", "data-combination": combination.toIndex(axes)) {
            f.checkbox(checked: true, name: "combinations", json: combination.toString());
        }

    } else {
        l.icon(class:"icon-md icon-nobuilt")
        if (!layouter.x || !layouter.y) {
          text(combination.toString(layouter.z))
        }
        span(class: "combination", "data-combination": combination.toIndex(axes)) {
            f.checkbox(checked: "false", name: "combinations", json: combination.toString())
        }
    }
}
