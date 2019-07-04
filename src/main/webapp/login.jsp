<%@ page contentType="application/xhtml+xml; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jspf/taglibs.jspf" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>


<div class="oauth-container-div text-center" id="content">
  <form class="oauth" id="loginForm" name="loginForm" action="<c:url value='/login.do'/>" method="post">
    <img class="mb-4 opensrplogo" src="images/opensrplogo.png" alt="opensrp logo" />
    <h1 class="h3 mb-3 font-weight-normal">Please Log in</h1>
    <c:if test="${not empty param.authentication_error}">
      <div class="alert alert-danger" role="alert">
        <p class="h5 mb-3 font-weight-normal">Error!</p>

        <p>Your login attempt was not successful.</p>
      </div>

    </c:if>
    <c:if test="${not empty param.authorization_error}">
      <div class="alert alert-danger" role="alert">
        <p class="h5 mb-3 font-weight-normal">Error!</p>

        <p>You are not permitted to access that resource.</p>
      </div>


    </c:if>

    <div class="form-group">
    <label for="username" class="sr-only">Username</label>
    <input type="text" id="username" name='j_username' class="form-control" placeholder="Username" required="required" autofocus="autofocus" />
    </div>

    <div class="form-group">
    <label for="password" class="sr-only">Password</label>
    <input type="password" name='j_password' id="password" class="form-control" placeholder="Password" required="required" />
    </div>

    <div class="form-group">
    <input class="btn btn-primary btn-block" type="submit" value="Login" />
    </div>

  </form>
</div>


<%@ include file="/WEB-INF/jspf/footer.jspf" %>
