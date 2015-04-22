package hudson.plugins.matrix_configuration_parameter.matrixcombinationparameterDefinition

import hudson.matrix.*
import hudson.model.Result
import hudson.plugins.matrix_configuration_parameter.MatrixCombinationsParameterDefinition
import lib.LayoutTagLib
import org.kohsuke.stapler.jelly.groovy.Namespace

l = namespace(LayoutTagLib)
t = namespace("/lib/hudson")
st = namespace("jelly:stapler")
f = namespace("lib/form")
nsProject = namespace("/hudson/plugins/matrix_configuration_parameter/taglib")


MatrixProject project = request.findAncestorObject(MatrixProject.class);
if (project == null)   //in case project is not a Matrix Project
    return;

AxisList axes =  project.getAxes();
def paramDef = it;
String nameIt = it.getName();
Layouter layouter = new Layouter<Combination>(axes) {
    protected Combination getT(Combination c) {
        return c;
    }
};





drawMainBody(paramDef, f, nameIt, axes, project, layouter)

private void drawMainBody(MatrixCombinationsParameterDefinition paramDef, Namespace f, String nameIt, AxisList axes,MatrixProject project,Layouter layouter) {

    drawMainLinksJS(nameIt)


    f.entry(title: nameIt, description: it.getDescription()) {
        div(name: "parameter") {
            input(type: "hidden", name: "name", value: nameIt)
            nsProject.matrix(it: project, layouter: layouter) {
              drawMainBall(paramDef, p, project.axes, nameIt, project, layouter);
            }
            raw("<span style=\"font-weight:bold\">Select: </span> \n" +
                "<a id=\"shortcut-" + nameIt + "-successful\" href=\"#\" onclick=\"click2Change(0);\">Successful</a> - \n" +
                "<a id=\"shortcut-" + nameIt + "-failed\"     href=\"#\" onclick=\"click2Change(2);\">Failed</a> - \n" +
                "<a id=\"shortcut-" + nameIt + "-all\"        href=\"#\" onclick=\"click2Change(1000);\">All</a> - \n" +
                "<a id=\"shortcut-" + nameIt + "-none\"       href=\"#\" onclick=\"click2Change(-1);\">None</a>")

        }//div
    }
}

private void drawMainLinksJS(String nameIt) {
    raw("<script>\n" +
            "function click2Change( status )\n" +
            "{\n" +
            "var i;\n" +
            "for( i = 0, len = document.parameters.elements.length ; i < len ; i++ )\n" +
            "{\n" +
            "var element = document.parameters.elements[i];\n" +
            "if( element.type == 'checkbox' && element.id.lastIndexOf(\"checkbox" + nameIt + "-\", 0) == 0 )\n" +
            "{\n" +
            "if( (document.parameters.elements[i+1].getAttribute('data-status') == status) || status > 999 )\n" +

            "{\n" +
            "element.checked = true;\n" +
            "}\n" +
            "else\n" +
            "{\n" +
            "element.checked = false;\n" +
            "}\n" +
            "}\n" +
            "}\n" +
            "return false;\n" +
            "}\n" +
            "</script>\n")
}

private int encodeRunResult(MatrixRun run) {
    if (Result.SUCCESS.isWorseOrEqualTo(run.getResult())) {
        return 0
    };
    if (Result.UNSTABLE.isWorseOrEqualTo(run.getResult())) {
        return 1
    };
    return 2;
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
            f.checkbox(checked: checked, name: "values",id: String.format("checkbox%s-%s", matrixName, combination.toString('-' as char, '-' as char)))
            input(type: "hidden", name: "confs", value: combination.toString(),'data-status':encodeRunResult(lastRun))

        }

    } else{
        img(src: "${imagesURL}/24x24/grey.gif")
        if (!layouter.x || !layouter.y) {
          text(combination.toString(layouter.z))
        }
        
        checked = combination.evalGroovyExpression(axes, paramDef.defaultCombinationFilter?:project.combinationFilter)
        f.checkbox(checked: checked, name: "values",id: String.format("checkbox%s-%s", matrixName, combination.toString('-' as char, '-' as char)))
        input(type: "hidden", name: "confs", value: combination.toString(), 'data-status': combination.toIndex((AxisList) axes) != -1 ? 0 : -1)
    }

}
