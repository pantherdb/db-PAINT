<%@page language="java" info = "Copyright (c) 2023 University of Southern California"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.Set"%>
<%@page import="com.sri.panther.paintCommon.Constant"%>
<%@page import="java.util.Vector"%>
<%@page import="com.sri.panther.paintCommon.util.Utils"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.HashSet"%>
<%@page import="com.sri.panther.paintServer.logic.DataValidationManager"%>
<html>
     <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="/css/style.css" type="text/css" rel="stylesheet">
    </head>
    <body>
        <div id="mainbody">
        <img src="/images/panther/panther_sm.gif" align=center>
    <BR><BR><BR>
    <div class="header1">Validation Information</div>
    <%
        DataValidationManager dvm = DataValidationManager.getInstance();
        if (false == dvm.existsBooksWithOrgInfo()) {
    %>
            Unable to retrieve organism information for books.
    <%
            return;
        }
        HashMap<String, HashSet<String>> booksWithIncompleteTaxonInfo = dvm.getBooksWithIncompleteTaxonInfo();
        if (null != booksWithIncompleteTaxonInfo && 0 != booksWithIncompleteTaxonInfo.size()) {
    %>
    The following indicates books and associated organisms that do not have taxonomy constraint rules<BR><BR> 
            <TABLE border="1">
                <TR><TD>Book</TD><TD>Organism</TD></TR>
    <%
            for (String book: booksWithIncompleteTaxonInfo.keySet()) {
                HashSet<String> orgs = booksWithIncompleteTaxonInfo.get(book);
                Vector<String> list = new Vector<String>(orgs); 
                Collections.sort(list); 
                String orgStr = Utils.listToString(list, "<BR>", Constant.STR_EMPTY);
    %>
                <TR>
                    <td><%=book%></td><td><%=orgStr%></td>
                </TR>
    <%
            }
    %>
            </TABLE>
            <BR>
            <BR>
    <%
        }

        Set<String> unsupportedTerms = dvm.getTermsNotSupportedByTaxonConstraints();
        if (null != unsupportedTerms && 0 != unsupportedTerms.size()) {
        ArrayList<String> list = new ArrayList<String>(unsupportedTerms); 
        Collections.sort(list); 
    %>
    The following terms are part of the GO hierarchy of terms but not defined in the taxonomy constraints information<BR><BR>
            <TABLE border="1">
                <TR><TD>Term</TD></TR>
    <%
                for (String term: list) {
    %>
                <tr><td><%=term%></td></tr>
    <%            
                }
    %>
            </TABLE>
    <%
        }

    %>
        </div>
    </body>
</html>
