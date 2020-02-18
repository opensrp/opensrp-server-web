<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri = "http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

		<jsp:include page="/WEB-INF/views/header.jsp" />

<c:url var="saveUrl" value="/location/add.html" />

		<div class="container-fluid">
		<h2><spring:message code="lbl.addLocation"/></h2>
		<div class="form-group">				
			<jsp:include page="/WEB-INF/views/location/location-tag-link.jsp" />

		</div>
			<div class="card mb-3">
				<div class="card-body">
				<span> ${uniqueErrorMessage}</span>
					<form:form method="POST" action="${saveUrl}" modelAttribute="command">
						<div class="form-group">
							<div class="row">
								<div class="col-5">
									<label for="exampleInputName"><spring:message code="lbl.name"/>  </label>
									<form:input path="properties.name" class="form-control"
										required="required" value="${name}" />
									<form:errors path="properties.name" cssClass="error" />
								</div>
							</div>
						</div>
						<div class="form-group">
							<div class="row">
								<div class="col-5">
									<label for="exampleInputName"><spring:message code="lbl.code"/></label>
									<form:input path="properties.code" class="form-control"
										required="required"/>
								</div>
							</div>
						</div>

						<div class="form-group">							
								<div class="row">									
									<div class="col-5">
										<div id="cm" class="ui-widget">
											  <label><spring:message code="lbl.parentLocation"/> </label>
											  ${parentLocationName}
									
									<form:hidden path="properties.parentId" class="form-control"/>
									
									<!-- to persist in form when validation fails -->
									<input type="hidden" name="parent" value="${command.properties.parentId}"/>
									<input type="hidden" name="parentLocationName" value="${parentLocationName}"/>
										</div>
									</div>									
								</div>
						</div>
						
						<div class="form-group">							
								<div class="row">									
									<div class="col-5">
									<label for="exampleInputName"> <spring:message code="lbl.tag"/></label>

			<c:set var="locationTypes" value="${fn:split('Site,Para,Block',',')}" />

									<form:select class="custom-select custom-select-lg mb-3" path="type">
									   <form:option value="" label="--- Select ---"/>
									   <form:options items="${locationTypes}" />
									</form:select>
										<!-- <select class="custom-select custom-select-lg mb-3" id="locationTag" name="locationTag" required="required">
											<option value="Site">Site</option>
											<option value="Para">Para</option>
											<option value="Block">Block</option>
										</select> -->
									</div>									
								</div>
							
						</div>
						
						<div class="form-group">
							<div class="row">
								<div class="col-5">
									<label for="exampleInputName">Geographic Level</label>
									<form:input path="properties.geographicLevel" class="form-control"
										required="required" pattern="[1-9]" title="must be between 1-9"/>
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