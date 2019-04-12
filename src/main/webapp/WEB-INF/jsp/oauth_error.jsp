<%@ page contentType="application/xhtml+xml; charset=UTF-8" pageEncoding="UTF-8" %>                    
<%@ include file="/WEB-INF/jspf/taglibs.jspf" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<h1>OpenSRP Authorization Error</h1>

<div id="content">
    <p><c:out value="${message}" /> (<c:out value="${error.summary}" />)</p>
    
    <p>Please go back to your client application and try again, or contact the owner and ask for support</p>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
