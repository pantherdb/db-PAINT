<%@page import="edu.usc.ksom.pm.panther.paintServer.services.api.ApiSupportMgr"%>
<%@page import="edu.usc.ksom.pm.panther.paintServer.services.ServiceUtils"%>
<%
    response.setStatus(HttpServletResponse.SC_OK);
    response.setCharacterEncoding(ServiceUtils.ENCODING_UTF_8);
    response.setContentType(ServiceUtils.OUTPUT_TYPE_JSON);
%>
<%=ApiSupportMgr.getInst().getJsonAPIForPaintServer()%>
