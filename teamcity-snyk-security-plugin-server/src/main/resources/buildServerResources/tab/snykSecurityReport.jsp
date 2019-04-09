<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%--@elvariable id="build" type="jetbrains.buildServer.serverSide.SBuild"--%>
<jsp:useBean id="buildNew" type="jetbrains.buildServer.serverSide.SBuild" scope="request"/>


<%--<table id="snykSecurityReportHead">--%>
<%--  <tr>--%>
<%--    <th class="col">Tested with Snyk</th>--%>
<%--  </tr>--%>
<%--</table>--%>

<%--<c:if test="${build.finished}">--%>
<%--  <c:out value="Build is not finished yet"/>--%>
<%--</c:if>--%>

<table>
  <tr>
    <th>Build finished: ${build.finished}</th>
    <th>Build status: ${build.buildStatus}</th>

    <td>BUildNew finished: ${buildNew.finished}</td>
    <td>BUildNew status: ${buildNew.buildStatus}</td>
  </tr>
</table>

