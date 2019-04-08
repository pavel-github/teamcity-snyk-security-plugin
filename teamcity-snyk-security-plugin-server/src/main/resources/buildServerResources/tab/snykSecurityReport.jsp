<%--suppress XmlPathReference --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<%--<table id="snykSecurityReportHead">--%>
<%--  <tr>--%>
<%--    <th class="col">Tested with Snyk</th>--%>
<%--  </tr>--%>
<%--</table>--%>

<div>
  <%@include file="snykSecurityReport.html" %>
</div>
