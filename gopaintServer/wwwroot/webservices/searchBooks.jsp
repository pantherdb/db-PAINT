<%@ page import="java.net.*,com.usc.panther.paintServer.webservices.*"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%
String value = request.getParameter(WSConstants.SEARCH_PARAMETER_BOOKS_SEARCH_VALUE);
String db = request.getParameter(WSConstants.SEARCH_PARAMETER_BOOKS_SEARCH_DATABASE);
String version = request.getParameter(WSConstants.SEARCH_PARAMETER_BOOKS_SEARCH_VERSION);
String type = request.getParameter(WSConstants.SEARCH_PARAMETER_BOOKS_SEARCH_TYPE);
if (null != value) {
    value = URLDecoder.decode(value, WSConstants.STANDARD_DECODER);
}
response.setContentType("application/xml");
%>
<%=BookListUtil.searchBooks(value, db, version, type)%>