<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

		<jsp:include page="/WEB-INF/views/header.jsp" />

<c:url var="saveUrl" value="/locationtag/add.html" />

		<div class="container-fluid">
		<h2><spring:message code="lbl.addLocationTag"/></h2>
		<div class="form-group">				
			<jsp:include page="/WEB-INF/views/location/location-tag-link.jsp" />

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
									<label for="exampleInputName">Key</label>
									<form:input path="key" class="form-control" required="required"/>
								</div>
							</div>
						</div>
						<div class="form-group">
							<div class="row">
								<div class="col-5">
									<label for="exampleInputName">Label</label>
									<form:input path="label" class="form-control" required="required"/>
								</div>
							</div>
						</div>
						<div class="form-group">
							<div class="row">
								<div class="col-3">
									<input type="submit" value="<spring:message code="lbl.save"/>"
										class="btn btn-primary btn-block" />
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