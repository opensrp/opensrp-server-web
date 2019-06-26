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
<h1>OpenSRP Authorization Server</h1>

<div class="body text-center" id="content">
	<div class="oauth">
		<img class="mb-4" src="https://avatars2.githubusercontent.com/u/7898027?s=200&v=4" alt="" width="72"
			height="72" />
		<section class="opensrp-header">
			<h1 id="first-part">OPEN</h1>
			<h1 id="second-part">SRP</h1>
		</section>
		<hr>
		<jsp:scriptlet>if
			(session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY) != null
			&& !(session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY)
			instanceof UnapprovedClientAuthenticationException)) {</jsp:scriptlet>
		<div styleclass="error">
			<h2>Error!</h2>

			<p>
				Access could not be granted. (<%=((AuthenticationException) session
						.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY))
								.getMessage()%>)
			</p>
		</div>
		<jsp:scriptlet>}</jsp:scriptlet>
		<c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION" />

		<authz:authorize ifAllGranted="ROLE_OPENMRS">
			<h2>Please Confirm</h2>

			<p>
				You hereby authorize "
				<c:out value="${client.clientId}" />
				" to access your protected resources.
			</p>
			<br>
			<form id="confirmationForm" name="confirmationForm" action="<%=request.getContextPath()%>/oauth/authorize"
				method="post">
				<input name="user_oauth_approval" value="true" type="hidden" /> <label>
					<input name="authorize" class="btn btn-lg btn-block" type="submit" value="Authorize" />
				</label>
			</form>

			<form id="denialForm" name="denialForm" action="<%=request.getContextPath()%>/oauth/authorize"
				method="post">
				<input name="user_oauth_approval" value="false" type="hidden" /> <label>
					<input name="deny" class="btn btn-lg btn-block" type="submit" value="Deny" />
				</label>
			</form>

		</authz:authorize>
	</div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf"%>