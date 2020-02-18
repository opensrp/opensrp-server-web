<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<ol class="breadcrumb">
  <li class="breadcrumb-item">
  	<a  href="<c:url value="/locationtag/list.html?lang=${locale}"/>"> <strong><spring:message code="lbl.manageTags"/> </strong> 	</a>   
  </li>
  <li class="breadcrumb-item">	
  	<a href="<c:url value="/locationtag/add.html"/>"> <strong>New Location Type</strong></a>
  </li>
  <li class="breadcrumb-item">
  	<a  href="<c:url value="/location/list.html?lang=${locale}"/>"> <strong><spring:message code="lbl.manageLocations"/></strong></a>  
  </li> 
  <li class="breadcrumb-item">
	<a href="<c:url value="/location/add.html?parent="/>"> <strong>New Root Location</strong></a>
  </li>
  
  <c:if test="${not empty param.id}">
  
  <li class="breadcrumb-item">
	<a href="<c:url value="/location/list.html"/>"><strong>Back to Top</strong></a>
  </li> 
  
  </c:if>

  
</ol>

