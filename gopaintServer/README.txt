Instructions for Building the PAINT server package

1.  Install JDK1.8.  This is the version that was last used for building.

2.  Install ant version 1.9

3.  Retrieve code from gopaint, gopaintCommon and gopaintServer packages.  Ensure gopaint, gopaintCommon and gopaintServer directories are parallel. Code from gipaintCommon has to be retrieved since, the gopaintServer package is dependent on gopaintCommon.  The gopaintServer package also invokes the build file from gopaint to create a tar file with all the files necessary for running the PAINT tool.

4.  Update property files with applicable settings.

5.  If necessary, execute command 'ant cleanall' to delete files from previous builds from gopaintserver, gopaintcommon and gopaint  

6.  Execute command 'ant'.  This will compile the files and build jar files.

7.  Execute command 'ant pub'.  This will copy the files into the appropriate directories.  A tar file containing all files necessary for running PAINT will also be created and copied over into the downloads directory.

8.  The wwwroot directory can now be copied into the directory that the webserver uses for retrieving the web pages.  The webserver has to be restarted.


