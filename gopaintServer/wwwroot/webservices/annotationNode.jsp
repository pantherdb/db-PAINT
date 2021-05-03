<%@ page import="java.net.*,edu.usc.ksom.pm.panther.paintServer.webservices.*"%>
<%
String book = request.getParameter(WSConstants.SEARCH_PARAMETER_ANNOTATION_NODE_BOOK);
String db = request.getParameter(WSConstants.SEARCH_PARAMETER_ANNOTATION_NODE_DATABASE);
String version = request.getParameter(WSConstants.SEARCH_PARAMETER_ANNOTATION_NODE_VERSION);
String type = request.getParameter(WSConstants.SEARCH_PARAMETER_ANNOTATION_NODE_TYPE);
response.setContentType("application/xml");
%>
<%=BookUtil.getAnnotationNodeInfo(book, db, version, type)%>
<%
if (null != request.getSession(false)) {    
    request.getSession().invalidate();
}
%>