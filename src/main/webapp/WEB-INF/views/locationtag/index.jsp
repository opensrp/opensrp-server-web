<%@page import="java.util.List"%>
<%@page import="org.opensrp.domain.setting.Setting"%>
<%@page import="org.opensrp.domain.PhysicalLocation"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

		<jsp:include page="/WEB-INF/views/header.jsp" />

		<div class="container-fluid">
		<h2><spring:message code="lbl.manageTag"/></h2>
		<div class="form-group">				
			<jsp:include page="/WEB-INF/views/location/location-tag-link.jsp" />
		</div>
			<div class="table-responsive">
				<table class="table table-bordered" id="dataTable">
					<tbody>
					<%
						List<Setting> tags = (List<Setting>) request.getAttribute("locationtags");
						for (Setting tag : tags) 
							{
							pageContext.setAttribute("tag", tag); 
					%>
						
							<tr>
								<td title="<%=tag.getKey()%>"><%=tag.getKey()%></td>
								<td><%=tag.getLabel() %></td>
								<td>
								<a href="<c:url value="/locationtag/${tag.key}/edit.html"/>"><spring:message code="lbl.edit"/></a>
								</td>
							</tr>
					<%
					}
					%>
					
					<%
						if (tags.isEmpty())  {
					%>
						
							<tr>
								<td colspan="100"><div>No Location Types Found</div></td>
							</tr>
					<%
					}
					%>
					</tbody>
				</table>
			</div>
		</div>
		<!-- /.container-fluid-->
		<!-- /.content-wrapper-->
		<jsp:include page="/WEB-INF/views/footer.jsp" />

