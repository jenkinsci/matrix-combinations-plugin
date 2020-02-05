This plugin allows a user to choose which matrix combinations he wants to run, as opposed to the default behaviour where jenkins runs all combinations and translate it into a groovy condition such as:

axis1=="axis1value1" && axis2=="axis2value1" || axis1=="axis1value2" && axis2=="axis2value2" || ...:

  
Using:

After clicking the build, the user gets a html table with a checkbox for each matrix combination.

After clicking the submit button, only checked combinations will run.

![](https://wiki.jenkins.io/download/attachments/69271855/MatrixJobConfig.png?version=1&modificationDate=1375012523000&api=v2)

Configuration:

After installation a new type of parameter was added (Matrix Combinations Parameter).

 
![](https://wiki.jenkins.io/download/attachments/69271855/MatrixPluginConfgJob.png?version=1&modificationDate=1375012524000&api=v2)

-   Configure your matrix job and add exactly one parameter with this type.
-   Fill the name of the parameter.
-   Define the Axes as usual

# **Acknowledgements**

Many thanks to Nathan Grunzweig for giving me the idea and help with the implementation of this plugin.  
I would also like to thank Christian Wolfgang, who developed the Matrix Reloaded Plugin which gave me the inspiration to implement it.

# Change Log

#### Version 1.3.0 (3 Mar 2018)

-   Now targets Jenkins \>= 1.651.3
-   FIXED: Exception when building a project with matrix-combinations in Jenkins 2.102+ (JEP-200) ([JENKINS-49573](https://issues.jenkins-ci.org/browse/JENKINS-49573))

#### Version 1.2.0 (3 Jun 2017)

-   Now targets Jenkins \>= 1.532
-   FIXED: HTML in description is always escaped ([JENKINS-42902](https://issues.jenkins-ci.org/browse/JENKINS-42902))

#### Version 1.1.1 (17 Apr 2017)

-   Fixed: "By build result" cannot be added (NPE when adding "By build result") ([JENKINS-42626](https://issues.jenkins-ci.org/browse/JENKINS-42626))
-   Remove the duplicated parameter name display ([JENKINS-39157](https://issues.jenkins-ci.org/browse/JENKINS-39157))

#### Version 1.1.0 (1 Oct 2016)

-   New feature: Configurable shortcut links ([JENKINS-29797](https://issues.jenkins-ci.org/browse/JENKINS-29797), [JENKINS-36894](https://issues.jenkins-ci.org/browse/JENKINS-36894), [JENKINS-36860](https://issues.jenkins-ci.org/browse/JENKINS-36860))
    -   You can define shortcut links in the job configuration page:
        -   All: Select all combinations
        -   None: Delelect all combinations
        -   Previous: Select combinations build previously
        -   By build result: Select combinations with previous results
        -   By combination filter: Select combinations with combinations filter expression
-   Support CLI ([JENKINS-25624](https://issues.jenkins-ci.org/browse/JENKINS-25624))
    -   You can pass a combinations filter expression as the parameter
        value via CLI.
-   Support WebAPI ([JENKINS-37815](https://issues.jenkins-ci.org/browse/JENKINS-37815))
    -   You can pass a combinations filter expression as the parameter value via WebAPI.
-   Refactored HTML implementations (Fixed [JENKINS-30918](https://issues.jenkins-ci.org/browse/JENKINS-30918))

#### Version 1.0.9 (11 Jul 2015)

-   Fixed NPE for running builds ([JENKINS-28824](https://issues.jenkins-ci.org/browse/JENKINS-28824))
-   Fixed exceptions in parameter pages or rebuild pages for non multi-configuration builds triggered with combinations parameters (in the case triggered with parameterized-trigger-plugin). ([JENKINS-27233](https://issues.jenkins-ci.org/browse/JENKINS-27233))

#### Version 1.0.8 (28 Apr 2015)

-   FIXED: "Successful" and "Failed" links do not work ([JENKINS-23609](https://issues.jenkins-ci.org/browse/JENKINS-23609))

#### Version 1.0.7 (05 Jul 2014)

-   Make it work also on Jenkins \>= 1.561 ([JENKINS-23561](https://issues.jenkins-ci.org/browse/JENKINS-23561))
-   Avoid NPE when some combinations of matrix is not available ([JENKINS-23030](https://issues.jenkins-ci.org/browse/JENKINS-23030))

#### Version 1.0.6 (29 Mar 2014)

-   Allow to change the combination to rebuild ([JENKINS-21970](https://issues.jenkins-ci.org/browse/JENKINS-21970))
-   Added a default combination filter ([JENKINS-21971](https://issues.jenkins-ci.org/browse/JENKINS-21971))
    -   Also this allows work for scheduled builds ([JENKINS-21851](https://issues.jenkins-ci.org/browse/JENKINS-21851))

#### Version 1.0.5 (17 Feb 2014)

-   Support in rebuild

#### Version 1.0.4 (24 November 2013)

-   No need to fill combinations filter

#### Version 1.0.3 (28 July 2013)

-   Initial release
