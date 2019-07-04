<%@ page contentType="application/xhtml+xml; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jspf/taglibs.jspf" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>


<div styleClass="oauth-container-div text-center" id="content">
  <form styleClass="oauth" id="loginForm" name="loginForm" action="<c:url value='/login.do'/>" method="post">
    <img styleClass="mb-4 opensrplogo" src="images/opensrplogo.png" alt="opensrp logo" />
    <h1 styleClass="h3 mb-3 font-weight-normal">Please Log in</h1>
    <c:if test="${not empty param.authentication_error}">
      <div styleClass="alert alert-danger" role="alert">
        <p styleClass="h5 mb-3 font-weight-normal">Error!</p>

        <p>Your login attempt was not successful.</p>
      </div>

    </c:if>
    <c:if test="${not empty param.authorization_error}">
      <div styleClass="alert alert-danger" role="alert">
        <p styleClass="h5 mb-3 font-weight-normal">Error!</p>

        <p>You are not permitted to access that resource.</p>
      </div>


    </c:if>

    <div styleClass="form-group">
    <label for="username" styleClass="sr-only">Username</label>
    <input type="text" id="username" name='j_username' styleClass="form-control" placeholder="Username" required="required" autofocus="autofocus" />
    </div>

    <div styleClass="form-group"
    <label for="password" styleClass="sr-only">Password</label>
    <input type="password" name='j_password' id="password" styleClass="form-control" placeholder="Password" required="required" />
    </div>

    <div styleClass="form-group">
    <input styleClass="btn btn-primary btn-block" type="submit" value="Login" />
    </div>

  </form>
</div>


<%@ include file="/WEB-INF/jspf/footer.jspf" %>
