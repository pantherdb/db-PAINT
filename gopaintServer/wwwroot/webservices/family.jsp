<%@ page import="java.net.*,edu.usc.ksom.pm.panther.paintServer.webservices.*"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%
String value = request.getParameter(WSConstants.SEARCH_PARAMETER_BOOKS_SEARCH_VALUE);
String db = request.getParameter(WSConstants.SEARCH_PARAMETER_BOOKS_SEARCH_DATABASE);
String version = request.getParameter(WSConstants.SEARCH_PARAMETER_BOOKS_SEARCH_VERSION);
String type = request.getParameter(WSConstants.SEARCH_PARAMETER_BOOKS_SEARCH_TYPE);
response.setContentType("application/xml");
%>
<%=FamilyUtil.getFamilyInfo(URLDecoder.decode(value, WSConstants.STANDARD_DECODER), db, version, type)%>
<%
if (null != request.getSession(false)) {    
    request.getSession().invalidate();
}
%>