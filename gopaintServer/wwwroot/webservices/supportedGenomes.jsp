<%@ page import="java.net.*,edu.usc.ksom.pm.panther.paintServer.webservices.*"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%
response.setContentType("application/xml");
%>
<%=Stats.getXMLForSupportedOrgs()%>
<%
if (null != request.getSession(false)) {    
    request.getSession().invalidate();
}
%>
