<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

		<jsp:include page="/WEB-INF/views/header.jsp" />

<c:url var="saveUrl" value="/team/${command.identifier}/edit.html" />

		<div class="container-fluid">
		<h2><spring:message code="lbl.editTeam"/></h2>
		
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
									${command.identifier}
								</div>
							</div>
						</div>
						<div class="form-group">
							<div class="row">
								<div class="col-5">
									<label for="exampleInputName"><spring:message code="lbl.name"/>  </label>
									<form:input path="name" class="form-control" required="required"/> 
									<form:errors path="name" cssClass="error" />
								</div>
							</div>
						</div>			
						<div class="form-group">							
							<div class="row">									
								<div class="col-5">
									<div id="cm" class="ui-widget">
										<label><spring:message code="lbl.location"/> </label>
										${command.assignedLocations}
									</div>
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
		<!-- /.container-fluid-->
		<!-- /.content-wrapper-->
		<jsp:include page="/WEB-INF/views/footer.jsp" />