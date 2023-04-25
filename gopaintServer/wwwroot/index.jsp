<%@page language="java" info = "Copyright (c) 2023 University of Southern California"%>
<%@page import="com.sri.panther.paintServer.datamodel.FullGOAnnotVersion"%>
<%@page import="com.sri.panther.paintServer.logic.VersionManager"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">        
        <link href="/css/style.css" type="text/css" rel="stylesheet">
    </head>
    <body>
        <div id="mainbody">
    <%
        VersionManager vm = VersionManager.getInstance();
        FullGOAnnotVersion fgv = vm.getFullGOAnnotVersion();
    %>
    <img src="/images/panther/panther_sm.gif" align=center>
    <BR><BR><BR>
    <div class="header1">Welcome GO PAINT Curators</div>
    <p>Click on the following <A HREF="downloads/paintApp.tar">link </A> to download the PAINT (Phylogenetic tree Annotation INference Tool) application. Installation instructions are available <a href="/doc/Installation.jsp">here</a>. Refer to the <a href='https://wiki.geneontology.org/index.php/PAINT_User_Guide#PAINT_software'> User Guide</a> for additional information</p>
    <BR>
    <BR>
    <p>This is UPL Version <%=vm.getPantherVersion().getId()%> with Full GO Version <%=fgv.getId()%> released on <%=fgv.getReleaseDate()%></p>
    <BR>
    <BR>
    <p>PAINT services information is available from <a href="/services/index.jsp">here</a>.</p>
    <BR>
    <BR>
    <p>Consistency checks information is available from <a href="/validation/index.jsp">here</a>.</p>
    </div>
    </body>
</html>