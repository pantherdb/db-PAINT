<%@ page import="java.net.*,com.usc.panther.paintServer.webservices.*"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%
response.setContentType("application/xml");
%>
<%=Stats.getXMLForSupportedOrgs()%>
