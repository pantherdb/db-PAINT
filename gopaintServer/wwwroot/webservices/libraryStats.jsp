<%@ page import="java.net.*,edu.usc.ksom.pm.panther.paintServer.webservices.*"%>
<%
String type = request.getParameter(WSConstants.SEARCH_PARAMETER_SEARCH_TYPE);            
String format = request.getParameter(WSConstants.REQUEST_FORMAT);
%>
<%=LibraryStats.getStats(type, format)%>
<%
if (null != request.getSession(false)) {    
    request.getSession().invalidate();
}
%>
