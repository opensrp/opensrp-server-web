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
				<a class="nav-link dropdown-toggle mr-lg-2" href="<c:url value="/?lang=${locale}"/>" >
					<strong><spring:message code="lbl.home"/></strong>
				</a>
			</li>

			<li class="nav-item dropdown">
				<a class="nav-link dropdown-toggle mr-lg-2" href="
					<c:url value="/client/household-member-list.html"/>">
					<strong><spring:message code="lbl.memberApproval"/></strong>
				</a>
			</li>

			<li class="nav-item dropdown">
				<a class="nav-link dropdown-toggle mr-lg-2" id="formDropdown" href="#" data-toggle="dropdown">
					<spring:message code="lbl.form"/>
				</a>
				<div class="dropdown-menu">
					<div class="dropdown-divider"></div>
					<a class="dropdown-item"
					   href="<c:url value="/form/uploadForm.html?lang=${locale}"/>"> <strong>
						<spring:message code="lbl.uploadForm"/> </strong>
					</a>
					<div class="dropdown-divider"></div>
					<a class="dropdown-item"
					   href="<c:url value="/form/downloadForm.html?lang=${locale}"/>"> <strong>
						<spring:message code="lbl.formList"/> </strong>
					</a>
				</div>
			</li>


			<li class="nav-item dropdown">
				<a class="nav-link dropdown-toggle mr-lg-2" id="clientDropdown" href="#" data-toggle="dropdown">
					<spring:message code="lbl.client"/>
				</a>
				<div class="dropdown-menu">

					<div class="dropdown-divider"></div>
					<a class="dropdown-item"
					   href="<c:url value="/client/household.html?lang=${locale}"/>">
						<strong><spring:message code="lbl.household"/></strong>
					</a>
					<div class="dropdown-divider"></div>
					<a class="dropdown-item"
					   href="<c:url value="/client/mother.html?lang=${locale}"/>"> <strong><spring:message code="lbl.mother"/></strong>
					</a>

					<div class="dropdown-divider"></div>
					<a class="dropdown-item"
					   href="<c:url value="/client/child.html?lang=${locale}"/>"> <strong><spring:message code="lbl.child"/></strong>
					</a>
					<div class="dropdown-divider"></div>
					<a class="dropdown-item"
					   href="<c:url value="/client/member.html?lang=${locale}"/>"> <strong><spring:message code="lbl.member"/></strong>
					</a>
					<div class="dropdown-divider"></div>
					<a class="dropdown-item" href="<c:url value="/client/similarClient.html?lang=${locale}"/>">
						<strong><spring:message code="lbl.similarCLient"/></strong>
					</a>
					<div class="dropdown-divider"></div>
					<a class="dropdown-item"
					   href="<c:url value="/client/similarEvent.html?lang=${locale}"/>"> <strong><spring:message code="lbl.similarEvent"/></strong>
					</a>
					<div class="dropdown-divider"></div>
					<a class="dropdown-item"
					   href="<c:url value="/client/similarityDefinitionOfClient.html?lang=${locale}"/>"> <strong><spring:message code="lbl.similarclientRuleDefination"/></strong>
					</a>
					<div class="dropdown-divider"></div>
					<a class="dropdown-item"
					   href="<c:url value="/client/similarityDefinitionOfEvent.html?lang=${locale}"/>"> <strong><spring:message code="lbl.similareventRuleDefination"/></strong>
					</a>

				</div>
			</li>


			<li class="nav-item dropdown"><a
					class="nav-link dropdown-toggle mr-lg-2" id="reportDropdown" href="#"
					data-toggle="dropdown"><spring:message code="lbl.report"/> </a>
				<div class="dropdown-menu">
					<div class="dropdown-divider"></div>
					<a class="dropdown-item" href="<c:url value="/report/child-growth.html?lang=${locale}"/>">
						<strong> <spring:message code="lbl.childGrowthReport"/></strong></a>
					<div class="dropdown-divider"></div>
					<a class="dropdown-item" href="<c:url value="/report/summary.html?lang=${locale}"/>">
						<strong><spring:message code="lbl.childGrowthSummaryReport"/> </strong></a>
					<div class="dropdown-divider"></div>
										<a class="dropdown-item" href="<c:url value="/analytics/analytics.html?lang=${locale}"/>">
										<strong><spring:message code="lbl.analytics"/></strong></a>

					<div class="dropdown-divider"></div>
					<a class="dropdown-item" href="<c:url value="/report/householdDataReport.html?lang=${locale}&address_field=division&searched_value=empty"/>">
						<strong><spring:message code="lbl.aggregatedReport"/></strong></a>
					<div class="dropdown-divider"></div>
					<a class="dropdown-item" href="<c:url value="/report/clientDataReport.html?lang=${locale}"/>">
						<strong><spring:message code="lbl.clientDataReport"/></strong></a>
				</div>
			</li>

			<li class="nav-item dropdown"><a
					class="nav-link dropdown-toggle mr-lg-2" id="userDropdown" href="#"
					data-toggle="dropdown"> <spring:message code="lbl.user"/> </a>
				<div class="dropdown-menu">
					<div class="dropdown-divider"></div>
					<a class="dropdown-item" href="<c:url value="/user.html?lang=${locale}"/>"> <strong>
						<spring:message code="lbl.manageUuser"/></strong> </a>
					<div class="dropdown-divider"></div>
					<a class="dropdown-item" href="<c:url value="/role.html?lang=${locale}"/>"> <strong>
						<spring:message code="lbl.manageRole"/></strong>
					</a>
					<div class="dropdown-divider"></div>
					<a class="dropdown-item" href="<c:url value="/branch-list.html?lang=${locale}"/>"> <strong>
						<spring:message code="lbl.manageBranch"/></strong>
					</a>
				</div></li>

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

			<li class="nav-item dropdown"><a
					class="nav-link dropdown-toggle mr-lg-2" id="exportDropdown" href="#"
					data-toggle="dropdown"><spring:message code="lbl.exportTitle"/> </a>
				<div class="dropdown-menu">
					<div class="dropdown-divider"></div>
					<a class="dropdown-item" href="<c:url value="/export/exportlist.html?lang=${locale}"/>">
						<strong> <spring:message code="lbl.exportList"/></strong>
					</a>
					<div class="dropdown-divider"></div>
					<a class="dropdown-item"
					   href="<c:url value="/team/teammember/list.html?lang=${locale}"/>"> <strong>
						<spring:message code="lbl.exportFile"/></strong>
					</a>
				</div>
			</li>
			<li class="nav-item"><a class="nav-link" data-toggle="modal"
									data-target="#exampleModal">(USER NAMEMMMMEE) <i class="fa fa-fw fa-sign-out"></i><spring:message code="lbl.logout"/>
			</a></li>
		</ul>
	</div>
</nav>

