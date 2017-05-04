<%@ page import="java.net.*,com.usc.panther.paintServer.webservices.*"%>
<%
String book = request.getParameter(WSConstants.SEARCH_PARAMETER_ANNOTATION_NODE_BOOK);
String db = request.getParameter(WSConstants.SEARCH_PARAMETER_ANNOTATION_NODE_DATABASE);
String version = request.getParameter(WSConstants.SEARCH_PARAMETER_ANNOTATION_NODE_VERSION);
String type = request.getParameter(WSConstants.SEARCH_PARAMETER_ANNOTATION_NODE_TYPE);

%>
<%=BookUtil.getAnnotationNodeInfo(book, db, version, type)%>