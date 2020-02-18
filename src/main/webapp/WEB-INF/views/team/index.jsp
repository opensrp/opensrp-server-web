<%@page import="org.opensrp.web.dashboard.util.AssignedLocationsWrapper"%>
<%@page import="org.opensrp.domain.Organization"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

		<jsp:include page="/WEB-INF/views/header.jsp" />

		<div class="container-fluid">
		<h2><spring:message code="lbl.teamList"/></h2>
		<div class="form-group">				
			<jsp:include page="/WEB-INF/views/team/team-member-link.jsp" />		
		</div>
		
		<div class="table-responsive">
			<table class="table table-bordered" id="dataTable">
				<thead>
					<tr>
						<th><spring:message code="lbl.identifier"/></th>									
						<th><spring:message code="lbl.name"/></th>
						<th>Active</th>
						<th><spring:message code="lbl.location"/></th>
						<th>Members</th>
						<th><spring:message code="lbl.action"/></th>
					</tr>
				</thead>
				
				<tbody>
				
				<%
					List<Organization> teams = (List<Organization>) request.getAttribute("organizations");
					System.out.print(teams);
				
					for (Organization team : teams) {
						pageContext.setAttribute("team", team);
				%>
						<tr>
							<td><%=team.getIdentifier() %></td>										
							<td><%=team.getName() %></td>
							<td><%=team.isActive() %></td>
							<td><%=team.getAssignedLocations() %></td>
							<td>
								<c:if test="${team.partOf > 0}">
									<a href="<c:url value="/team/${team.identifier}/member/list.html"/>"><%=team.getPartOf() %></a> 
								</c:if>
								<c:if test="${team.partOf <= 0}"><%=team.getPartOf() %></c:if>
							</td>
							<td>
								<a href="<c:url value="/team/${team.identifier}/edit.html"/>"><spring:message code="lbl.edit"/></a> 
							</td>

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


