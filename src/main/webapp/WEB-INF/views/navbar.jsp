<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
		 pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<style>
	strong{ padding:5px;
		font-size: 17px;
		font-family: ShonarBangla, Helvetica,Arial,sans-serif;}

	li{
		font-size: 13px;
		font-family: ShonarBangla,Helvetica,Arial,sans-serif;}
</style>

<nav class="navbar navbar-expand-lg navbar-dark bg-dark fixed-top" id="mainNav">
	<div class="collapse navbar-collapse" id="navbarResponsive">


		<ul class="navbar-nav ml-auto">

			<li class="nav-item dropdown">
				<a class="nav-link dropdown-toggle mr-lg-2" href="<c:url value="/home.html?lang=${locale}"/>" >
					<strong><spring:message code="lbl.home"/></strong>
				</a>
			</li>
			<li class="nav-item dropdown"><a
					class="nav-link dropdown-toggle mr-lg-2" id="locationDropdown"
					href="#" data-toggle="dropdown"> <spring:message code="lbl.location"/> </a>
				<div class="dropdown-menu">
					<div class="dropdown-divider"></div>
					<a class="dropdown-item"
					   href="<c:url value="/locationtag/list.html?lang=${locale}"/>">
						<strong> <spring:message code="lbl.manageTag"/></strong>
					</a>
					<div class="dropdown-divider"></div>
					<a class="dropdown-item" href="<c:url value="/location/list.html?"/>">
						<strong> <spring:message code="lbl.manageLocation"/></strong>
					</a>
				</div></li>

			<li class="nav-item dropdown"><a
					class="nav-link dropdown-toggle mr-lg-2" id="teamDropdown" href="#"
					data-toggle="dropdown"><spring:message code="lbl.team"/> </a>
				<div class="dropdown-menu">
					<div class="dropdown-divider"></div>
					<a class="dropdown-item" href="<c:url value="/team/list.html?lang=${locale}"/>">
						<strong> <spring:message code="lbl.manageTeam"/></strong>
					</a>
					<div class="dropdown-divider"></div>
					<a class="dropdown-item" href="<c:url value="/team/add.html?lang=${locale}"/>"> <strong>
							<spring:message code="lbl.addNew"/></strong></a>
				</div>
			</li>

			<li class="nav-item"><a class="nav-link" data-toggle="modal"
									data-target="#exampleModal">(USER NAMEMMMMEE) <i class="fa fa-fw fa-sign-out"></i><spring:message code="lbl.logout"/>
			</a></li>
		</ul>
	</div>
</nav>

