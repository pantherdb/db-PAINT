<%@page language="java" info = "Copyright (c) 2023 University of Southern California"%>
<%@page import="com.sri.panther.paintCommon.User"%>
<%@page import="edu.usc.ksom.pm.panther.paintServer.logic.UserManager"%>
<%@page import="com.sri.panther.paintServer.datamodel.ClassificationVersion"%>
<%@page import="com.sri.panther.paintCommon.Constant"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap"%>
<%@page import="edu.usc.ksom.pm.panther.paintServer.logic.UpdateHistory"%>
<%@page import="edu.usc.ksom.pm.panther.paintServer.services.servlet.FamilyHistory"%>
<%
    String id = request.getParameter(FamilyHistory.ID);
    if (null == id) {
        return;
    }
    
    HashMap<String, String> params = new HashMap<String, String>();
    params.put(FamilyHistory.ID, id);
    UpdateHistory uh = new UpdateHistory();
    ArrayList<UpdateHistory.OperationInfoDetail> operationsList = uh.getUpdateHistoryForBook(params, FamilyHistory.ID, false);
    if (null == operationsList) {
        return;
    } 
%>
<html>
     <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="/css/style.css" type="text/css" rel="stylesheet">
    </head>    
    <body>
        <div id="mainbody">
        <img src="/images/panther/panther_sm.gif" align=center>
    <BR><BR><BR>
    <div class="header1">Family Update History for <%=id%></div>
    <BR><BR><BR>
    <table border=0 class="tablesep"  cellspacing="0" cellpadding="3" style="width:600px">
        <tr>
            <th class=tableheaderline style="white-space:nowrap">Release</th>
            <th class=tableheaderline style="white-space:nowrap">Operation Type</th>
            <th class=tableheaderline style="white-space:nowrap">Identifier</th>
            <th class=tableheaderline style="white-space:nowrap">Created By</th>
            <th class=tableheaderline style="white-space:nowrap">Obsoleted By</th>
            <th class=tableheaderline style="white-space:nowrap">Details</th>        
        </tr>
    <%
        UserManager um = UserManager.getInstance();
        String patternLineReturn = "\\r\\n|\\r|\\n";
        String LINE_BREAK = "<br/>";
        String STYLE_NO_WRAP = "style=\"white-space:nowrap\"";
        String STYLE_NO_WRAP_STKIKETHROUGH_RED = "style=\"white-space:nowrap;color:rgb(255,0,0);text-decoration-line:line-through;\""; 
        for (UpdateHistory.OperationInfoDetail oid: operationsList) {
            boolean obsoleted = false;
            String title = Constant.STR_EMPTY;
            String release = Constant.STR_DASH;
            if (null != oid.releaseId) {
                ClassificationVersion cv = UpdateHistory.CLS_VERSION_SID_LOOKUP.get(oid.releaseId);
                if (null != cv) {
                    release = cv.getName();
                }
            }
            String operation = Constant.STR_DASH;
            if (null != oid.operation) {
                operation = oid.operation.label;
            }
            String identifier = Constant.STR_DASH;
            if (null != oid.id) {
                identifier = oid.id;
            }
            String creationDate = Constant.STR_DASH;
            if (null != oid.creationDate) {
                creationDate = UpdateHistory.DATE_FORMATTER.format(oid.creationDate);
            }
            String createdBy = Constant.STR_DASH;
            if (null != oid.createdBy) {
                createdBy = oid.createdBy;
            }
            String obsolescenceDate = Constant.STR_DASH;
            if (null != oid.obsolescenceDate) {
                obsoleted = true;
                obsolescenceDate = UpdateHistory.DATE_FORMATTER.format(oid.obsolescenceDate);
            }
            String obsoletedBy = Constant.STR_DASH;
            if (null != oid.obsoletedBy) {
                obsoleted = true;
                obsoletedBy = oid.obsoletedBy;
            }
            String details = Constant.STR_DASH;
            switch(oid.operation) {
                case NODE_ANNOTATION: {
                    details = oid.confidenceCode;
                    break;
                }
                case FAMILY_COMMENT: {
                    if (null != oid.comment) {
                        title = oid.comment.replaceAll("<[^>]*>", "");
                        if (title.length() > 10) {
                            details = title.substring(0, 10);
                        }
                    }
                    break;
                }
                case FAMILY_STATUS: {
                    if (null != oid.status) {
                        details = oid.status;
                    }
                }
            }
            String style = STYLE_NO_WRAP;
            if (true == obsoleted) {
                style = STYLE_NO_WRAP_STKIKETHROUGH_RED;
            }
    %>        
            <TR>
                <TD <%=style%>><%=release%></TD>
                <TD <%=style%>><%=operation%></TD>
                <TD <%=style%>><%=identifier%></TD>
                <TD <%=style%>><%=createdBy%> (<%=creationDate%>)</TD>
                <TD <%=style%>><%=obsoletedBy%> (<%=obsolescenceDate%>)</TD>
                <TD <%=style%> title="<%=title%>"><%=details%></TD>            
            </TR>
    <%        
        }
    %>    
    </table>
        </div>
    </body>
</html>