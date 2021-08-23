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
+ The client (gopaint), 
+ The server(gopaintServer) and
+ Classes common to both client and server (gopaintCommon)  

Each of these can be build separately.  There is a README.txt file in each subdirectory with instructions for building.  Building gopaintServer and publishing will create the server package as well as the client download.
Before building, update the property files in the config subdirectory for each component.  These specify database connection parameters, server locations, etc. Also properties in file user.properties.paintCuration have to be modified to specify server location, etc.

***
'''Rules for creating IBD annotations'''
* When creating an IBD annotation, the software will include all available expeimental annotations from non-pruned leaf nodes with matching qualifiers as evidence.
* The annotation will be propagated to all descendant non-pruned leaf nodes including ones with conflicting qualifier annotations (as per [http://wiki.geneontology.org/index.php/3_Aug_2021_PAINT_Conference_Call PAINT meeting discussion on August 3rd]  and PANTHER group meeting).  The only way to stop propagation will be for the curator to create a 'NOT' annotation.
* If there is a taxonomy violation for the node and term, the system will warn the user to create a ticket to remove the taxonomy violation.
* If there is a taxonomy violation for a descendant node, the system will not create a TCV annotation and stop propagation.

