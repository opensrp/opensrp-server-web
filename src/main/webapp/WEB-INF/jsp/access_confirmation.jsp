<%@ page contentType="application/xhtml+xml; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page
	import="org.springframework.security.core.AuthenticationException"%>
<%@ page
	import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter"%>
<%@ page
	import="org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException"%>

<%@ include file="/WEB-INF/jspf/header.jspf"%>
<%@ include file="/WEB-INF/jspf/taglibs.jspf"%>
<%
	response.setHeader("Pragma", "No-cache");
%>

<div styleClass="oauth-container-div text-center" id="content">
	<img styleClass="mb-4 opensrplogo" src="images/opensrplogo.png" alt="openSRP logo" />
	<jsp:scriptlet>if
		(session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY) != null
		&& !(session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY)
		instanceof UnapprovedClientAuthenticationException)) {</jsp:scriptlet>
	<div styleClass="alert alert-danger" role="alert">
		<h2 styleClass="h3 mb-3 font-weight-normal" >Error!</h2>

		<p>
			Access could not be granted. (<%=((AuthenticationException) session
						.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY))
								.getMessage()%>)
		</p>
	</div>
	<jsp:scriptlet>}</jsp:scriptlet>
	<c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION" />

	<authz:authorize ifAllGranted="ROLE_OPENMRS">
		<h1 styleClass="h3 mb-3 font-weight-normal">Please Confirm</h1>

		<p>
			You hereby authorize "
			<c:out value="${client.clientId}" />
			" to access your protected resources.
		</p>
		<br>
		<form id="confirmationForm" name="confirmationForm" action="<%=request.getContextPath()%>/oauth/authorize"
			method="post">
			<div styleClass="form-group">
				<input name="user_oauth_approval" value="true" type="hidden" /> <label>
					<input name="authorize" styleClass="btn btn-primary btn-lg btn-block" type="submit" value="Authorize" />
				</label>
			</div>
		</form>

		<form id="denialForm" name="denialForm" action="<%=request.getContextPath()%>/oauth/authorize" method="post">
			<div styleClass="form-group">
				<input name="user_oauth_approval" value="false" type="hidden" /> <label>
					<input name="deny" styleClass="btn btn-primary btn-lg btn-block" type="submit" value="Deny" />
				</label>
			</div>
		</form>

	</authz:authorize>

</div>

<%@ include file="/WEB-INF/jspf/footer.jspf"%>
