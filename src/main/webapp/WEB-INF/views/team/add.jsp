<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

		<jsp:include page="/WEB-INF/views/header.jsp" />

<c:url var="saveUrl" value="/team/add.html" />

		<div class="container-fluid">
		<h2><spring:message code="lbl.addTeam"/></h2>
		
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
									<form:input path="identifier" class="form-control" required="required" />
									<form:errors path="identifier" cssClass="error" />
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
										<input name="selectedLocations" value="${selectedLocations}" class="form-control" required="required"/>
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
