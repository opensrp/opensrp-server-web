<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<ol class="breadcrumb">
	<li class="breadcrumb-item">
		<a  href="<c:url value="/team/list.html"/>"> <strong> <spring:message code="lbl.manageTeam"/></strong> 	</a> 
	</li> 
	<li class="breadcrumb-item">
		<a href="<c:url value="/team/add.html?lang=${locale}"/>"> <strong>
				<spring:message code="lbl.addNew"/> Team</strong></a>
	</li>
	
	<c:if test="${not empty team.identifier}">
	
	<li class="breadcrumb-item">
		<a href="<c:url value="/team/${team.identifier}/member/add.html?lang=${locale}"/>"> <strong>
				<spring:message code="lbl.addNew"/> Member</strong></a>
	</li> 
	<li class="breadcrumb-item">
		<a  href="<c:url value="/team/list.html"/>"> <strong>Back</strong> 	</a> 
	</li> 
	
	</c:if>
</ol>

