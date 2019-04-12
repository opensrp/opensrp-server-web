<%@ page contentType="application/xhtml+xml; charset=UTF-8" pageEncoding="UTF-8" %>                    
<%@ include file="/WEB-INF/jspf/taglibs.jspf" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>


<h1>OpenSRP</h1>

<div id="content">
    <c:if test="${not empty param.authentication_error}">
        <h1>Error!</h1>

        <p styleclass="error">Your login attempt was not successful.</p>
    </c:if>
    <c:if test="${not empty param.authorization_error}">
        <h1>Error!!</h1>

        <p styleclass="error">You are not permitted to access that resource.</p>
    </c:if>


    <h2>Login</h2>

    <form id="loginForm" name="loginForm" action="<c:url value='/login.do'/>" method="post">
        <p><label>Username: <input type='text' name='j_username' /></label></p>
        <p><label>Password: <input type='password' name='j_password' /></label></p>

        <p><input name="login" value="Login" type="submit" /></p>
    </form>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

