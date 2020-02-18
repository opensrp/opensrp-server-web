<%@page import="org.opensrp.domain.Practitioner"%>
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
		<h2><spring:message code="lbl.teamMemberList"/> of ${team.name}</h2>
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
						<th>Username</th>
						<th>Role</th>
						<th><spring:message code="lbl.action"/></th>
					</tr>
				</thead>
				
				<tbody>
				
				<%
					List<Map<String, Object>> teamMembers = (List<Map<String, Object>>) request.getAttribute("members");
					System.out.print(teamMembers);
				
					for (Map<String, Object> teamMember : teamMembers) {
						pageContext.setAttribute("teamMember", teamMember);
				%>
						<tr>
							<td>${teamMember.member.identifier}</td>										
							<td>${teamMember.member.name}</td>
							<td>${teamMember.member.active}</td>
							<td>${teamMember.member.username}</td>
							<td>${teamMember.role[0].code}</td>
							<td>
								<a href="<c:url value="/team/${team.identifier}/member/${teamMember.member.identifier}/edit.html"/>"><spring:message code="lbl.edit"/></a> 
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

