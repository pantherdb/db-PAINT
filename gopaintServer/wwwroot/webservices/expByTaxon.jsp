<%@ page import="java.net.*,edu.usc.ksom.pm.panther.paintServer.webservices.*"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%
String taxa = request.getParameter("taxon");
String paramTaxa = null;
if (null != taxa) {
    paramTaxa = URLDecoder.decode(taxa, WSConstants.STANDARD_DECODER);
}
String format = request.getParameter("format");
String paramFormat = null;
if (null != format) {
    paramFormat = URLDecoder.decode(format, WSConstants.STANDARD_DECODER);
}
if (null == format || true == "XML".equalsIgnoreCase(format)) {
    response.setContentType("application/xml");
}
else {
    response.setContentType("text/plain");    
}
%>
<%=Stats.getExperimentalEvidence(paramTaxa, paramFormat)%>
<%
if (null != request.getSession(false)) {    
    request.getSession().invalidate();
}
%>
