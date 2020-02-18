<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

		<jsp:include page="/WEB-INF/views/header.jsp" />

<c:url var="saveUrl" value="/team/${team.identifier}/member/${id}/edit.html" />

		<div class="container-fluid">
		<h2><spring:message code="lbl.editTeamMember"/> of ${team.name}</h2>
		
		<div class="form-group">				
			<jsp:include page="/WEB-INF/views/team/team-member-link.jsp" />		
		</div>
			<div class="card mb-3">
				<div class="card-body">
					<spring:hasBindErrors name="command">
					    <c:forEach items="${status.errorMessages}" var="errorMessage">
					        <li>
					            <c:out value="${errorMessage}" />
					            <br />
					        </li>
					    </c:forEach>
					</spring:hasBindErrors>
					<form:form method="POST" action="${saveUrl}" modelAttribute="command">
						<div class="form-group">
							<div class="row">
								<div class="col-5">
									<label for="exampleInputName"><spring:message code="lbl.identifier"/></label>
									<%-- <form:input path="practitioner.identifier" class="form-control" required="required" />
									<form:errors path="practitioner.identifier" cssClass="error" /> --%>
									${command.practitioner.identifier}
								</div>
							</div>
						</div>
						<div class="form-group">
							<div class="row">
								<div class="col-5">
									<label for="exampleInputName"><spring:message code="lbl.name"/>  </label>
									<form:input path="practitioner.name" class="form-control" required="required"/> 
									<form:errors path="practitioner.name" cssClass="error" />
								</div>
							</div>
						</div>	
						<div class="form-group">
							<div class="row">
								<div class="col-5">
									<label for="exampleInputName"><spring:message code="lbl.userName"/>  </label>
									<%-- <form:input path="username" class="form-control" required="required"/> 
									<form:errors path="username" cssClass="error" /> --%>
									${command.username}
								</div>
							</div>
						</div>		
						<div class="form-group">
							<div class="row">
								<div class="col-5">
									<label for="exampleInputName"><spring:message code="lbl.password"/>  </label>
									<form:input path="password" class="form-control" required="required"/> 
									<form:errors path="password" cssClass="error" />
								</div>
							</div>
						</div>
						<div class="form-group">
							<div class="row">
								<div class="col-5">
									<label for="exampleInputName"><spring:message code="lbl.confirmedPassword"/>  </label>
									<form:input path="confirmedPassword" class="form-control" required="required"/> 
								</div>
							</div>
						</div>
						<div class="form-group">
							<div class="row">
								<div class="col-5">
									<label for="exampleInputName"><spring:message code="lbl.email"/>  </label>
									<form:input path="email" class="form-control"/> 
									<form:errors path="email" cssClass="error" />
								</div>
							</div>
						</div>	
						<div class="form-group">
							<div class="row">
								<div class="col-5">
									<label for="exampleInputName"><spring:message code="lbl.role"/>  </label>
									<%-- <form:select class="custom-select custom-select-lg mb-3" path="role">
									   <form:option value="" label="--- Select ---"/>
									   <form:options items="${roles}" itemValue="name" itemLabel="name"/>
									</form:select>
									<form:errors path="role" cssClass="error" /> --%>
									${command.role}
								</div>
							</div>
						</div>	
						<div class="form-group">							
							<div class="row">									
								<div class="col-5">
									<div id="cm" class="ui-widget">
										<label>Active </label>
										<form:select class="custom-select custom-select-lg mb-3" path="active" required="required">
										   <form:option value="true" label="Yes"/>
										   <form:option value="false" label="No"/>
										</form:select>
									</div>
								</div>									
							</div>
						</div>
						<div class="form-group">
							<div class="row">
								<div class="col-3">
									<input type="submit" value="Save" class="btn btn-primary btn-block" />
								</div>
							</div>
						</div>
					</form:form>
				</div>
			</div>
		</div>
		  <script src="<c:url value='/resources/js/jquery-ui.js'/>"></script>
		  
		<!-- /.container-fluid-->
		<!-- /.content-wrapper-->
		<jsp:include page="/WEB-INF/views/footer.jsp" />
