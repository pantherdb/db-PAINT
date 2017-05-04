Used JAVA 1.8 for building.

This project is dependent on classes from bbop and ghoul from the geneontology project.  The following was done to build bbop.jar and ghoul.jar.
Retrieved code from svn branch 4886 from geneontology project and updated to compile with Java 1.8.


Retrieved the code from SVN 
cd into directory geneontology-svn-4886\GHOUL and executed command ant.
This built ghoul.jar in directory geneontology-svn-4886\GHOUL\lib\runlibs.
This jar file was copied into gopaint's lib directory.
Also jar files from directories geneontology-svn-4886\GHOUL\hibernateLib, geneontology-svn-4886\GHOUL\jars and geneontology-svn-4886\GHOUL\jars\mysql-connector-java-5.1.6 were copied over into gopaint's lib directory

cd into directory geneontology-svn-4886\java\bbop\trunk and executed command ant.
This  built bbop.jar in directory geneontology-svn-4886\java\bbop\trunk.
This jar file was copied into gopaint's lib directory.
Also jar files from geneontology-svn-4886\java\bbop\trunk\lib were copied into gopaint's lib directory

Sometimes jar files were overwritten if same jar file was used in both projects.


Functionality from GHOUL project is not used to connect to any database from gopaint.  However, there are classes that still use packages from GHOUL.


gopaint is also dependent on gopaintCommon.  The build file automatically builds this project and copies over gopaintCommon.jar

To build gopaint, do as follows:

1.  Install JDK1.8.  This is the version that was last used for building.

2.  Install ant version 1.9

3.  cd into gopaint directory

4.  Copy file ncbi_taxa_ids.txt into directory gopaint/src/org/paint/resources

5.  Execute command 'ant cleanall', if previous builds have been done and to delete files from previous builds.

6.  Execute command 'ant'.  This will build the gopaintCommon.jar and copy jar files into lib_output.  Directory paintApp will be created with necessary files for running the paint application



When running on IDE, ensure "prespectives" folder and contents are in classpath.  Also user.properties. 








