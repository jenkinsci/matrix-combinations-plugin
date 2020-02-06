# Matrix combinations parameter plugin for Jenkins

This plugin allows a user to choose which matrix combinations he wants to run, as opposed to the default behaviour where jenkins runs all combinations and translate it into a groovy condition such as:

axis1=="axis1value1" && axis2=="axis2value1" || axis1=="axis1value2" && axis2=="axis2value2" || ...:

## Configuration

After installation a new type of parameter was added (Matrix Combinations Parameter).
 
![Matrix combinations parameter definition example](docs/images/MatrixCombinationsParameterDefinitionExample.png)

-   Configure your matrix job and add exactly one parameter with this type
-   Fill the name of the parameter
-   Define the Axes as usual
  
## Usage

After clicking the build, the user gets a html table with a checkbox for each matrix combination.

After clicking the submit button, only checked combinations will run.

![Matrix combinations parameter value example](docs/images/MatrixCombinationsParameterValueExample.png)


## Acknowledgements

Many thanks to Nathan Grunzweig for giving me the idea and help with the implementation of this plugin.  
I would also like to thank Christian Wolfgang, who developed the Matrix Reloaded Plugin which gave me the inspiration to implement it.
