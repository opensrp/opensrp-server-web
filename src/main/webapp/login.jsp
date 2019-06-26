<%@ page contentType="application/xhtml+xml; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jspf/taglibs.jspf" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>


<div class="body text-center" id="content">


    <div class="oauth">
        <form class="oauth" id="loginForm" name="loginForm" action="<c:url value='/login.do'/>" method="post">
            <img class="mb-4" src="https://avatars2.githubusercontent.com/u/7898027?s=200&v=4" alt="" width="72"
                height="72">
            <section class="opensrp-header">
                <h1 id="first-part">OPEN</h1>
                <h1 id="second-part">SRP</h1>
                <hr>
            </section>

            <c:if test="${not empty param.authentication_error}">
                <h1>Error!</h1>

                <p styleclass="error">Your login attempt was not successful.</p>
            </c:if>
            <c:if test="${not empty param.authorization_error}">
                <h1>Error!!</h1>

                <p styleclass="error">You are not permitted to access that resource.</p>
            </c:if>

            <label for="username" class="sr-only">Username</label>
            <input type="text" id="username" name='j_username' class="form-control" placeholder="Username" required
                autofocus>
            <label for="password" class="sr-only">Password</label>
            <input type="password" name='j_password' id="password" class="form-control" placeholder="Password" required>
            <input class="btn btn-lg btn-block" type="submit" value="Login">
        </form>
    <div>
</div>


        <%@ include file="/WEB-INF/jspf/footer.jspf" %>