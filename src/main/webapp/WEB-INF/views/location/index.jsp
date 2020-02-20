<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="org.opensrp.domain.PhysicalLocation"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

		<jsp:include page="/WEB-INF/views/header.jsp" />

		<div class="container-fluid">
		<h2><spring:message code="lbl.locationTitle"/></h2>
		<div class="form-group">				
			<jsp:include page="/WEB-INF/views/location/location-tag-link.jsp" />
		</div>
					<div class="table-responsive">
						<table class="table table-bordered" id="dataTable">
							<thead>
								<tr>
									<th><spring:message code="lbl.name"/></th>									
									<th><spring:message code="lbl.identifier"/></th>									
									<th><spring:message code="lbl.tag"/></th>									
									<th><spring:message code="lbl.action"/></th>
								</tr>
							</thead>
							
							<tbody>
							<%
							PhysicalLocation location = (PhysicalLocation) request.getAttribute("location");
							pageContext.setAttribute("location", location); 
							if (location != null) 
							{
							%>
									<tr>
										<th><b><%=location.getProperties().getName() %></b></th>
										<th><%=location.getId() %></th>
										<th><%=location.getProperties().getType() %></th>
										<th>
										<a href="<c:url value="/location/<%=location.getId() %>/edit.html?lang=${locale}"/>"><spring:message code="lbl.edit"/></a>
										|
										<a href="<c:url value="/location/list.html?id=${location.properties.parentId}"/>">Navigate Up</a>
										|
										<a href="<c:url value="/location/add.html?parent=${location.id}"/>">New Child</a>
										
										</th>
									</tr>
							<%
							}
							%>
							
							<%
								List<PhysicalLocation> children = (List<PhysicalLocation>) request.getAttribute("children");
								for (PhysicalLocation child : children) 
									{
									pageContext.setAttribute("child", child); 
							%>
								
									<tr>
										<td>  >> <%=child.getProperties().getName() %></td>
										<td><%=child.getId() %></td>
										<td><%=child.getProperties().getType() %></td>
										<td>
										<a href="<c:url value="/location/${child.id}/edit.html?lang=${locale}"/>"><spring:message code="lbl.edit"/></a>
										|
										<a href="<c:url value="/location/list.html?id=${child.id}"/>">Navigate Down</a>
										</td>
									</tr>
							<%
							}
							%>
							
							<%
								if (children.isEmpty())  {
							%>
								
									<tr>
										<td colspan="100"><div>No Children Found</div></td>
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