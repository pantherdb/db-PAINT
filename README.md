# db-PAINT
Application for curators to make Phylogenetic-based gene function predictions.
***
This project will build both client and server packages for db-PAINT

***

The client version is based on paint from svn://svn.code.sf.net/p/pantherdb/code (version 430).  It uses packages from bbop and ghoul (https://sourceforge.net/p/geneontology/svn/HEAD/tree/).  Directory geneontology contains a zip file that contains the packages that are used by gopaint client

***

The client version has dependencies on database related libraries (such as hibernate, Postgresql, mySQL) and others that are no longer used.  These have to be removed as part of code cleanup.

***

There are three main components:
+The client (gopaint), 
+The server(gopaintServer) and
+Classes common to both client and server (gopaintCommon)
***
Each of these can be build separately.  There is a README.txt file in each subdirectory with instructions for building.  Building gopaintServer and publishing will create the server package as well as the client download.
Before building, update the property files in the config subdirectory for each component.  These specify database connection parameters, server locations, etc. Also properties in file user.properties.paintCuration have to be modified to specify server location, etc.


