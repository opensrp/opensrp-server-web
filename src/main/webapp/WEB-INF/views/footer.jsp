<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
 
 	<footer class="sticky-footer">
      <div class="container">
        <div class="text-center">
          <small>Copyright © OpenSRP Vital 2020</small>
        </div>
      </div>
    </footer>
    <!-- got to previous page -->
    <a class="scroll-to-left rounded" href="#" onclick="history.back()" > 
    	<i class="fa fa-angle-left"></i>
    </a>
    <!-- Scroll to Top Button-->
    <a class="scroll-to-top rounded" href="#page-top">
      <i class="fa fa-angle-up"></i>
    </a>
    <!-- Bootstrap core JavaScript-->
    <script src="<c:url value='/resources/js/jquery-1.10.2.js'/>"></script>
    <script src="<c:url value='/resources/vendor/bootstrap/js/bootstrap.bundle.min.js'/>"></script>
    
    <!-- Core plugin JavaScript-->
    <script src="<c:url value='/resources/vendor/jquery-easing/jquery.easing.min.js'/>"></script>
    
   
    <!-- Custom scripts for all pages-->
    <script src="<c:url value='/resources/js/sb-admin.min.js'/>"></script>
    <!-- Custom scripts for this page-->
    <%-- <script src="<c:url value='/resources/js/sb-admin-datatables.min.js'/>"></script> --%>
    <script src="<c:url value='/resources/js/location.js'/>"></script>
   <script src="<c:url value='/resources/js/checkbox.js'/>"></script>

</div>
<!-- Ending div content-wrapper from header -->
</body>
</html>