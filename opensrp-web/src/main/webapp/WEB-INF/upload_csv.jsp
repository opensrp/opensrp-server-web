<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>


<!DOCTYPE html>
<html lang="en">

<head>
<meta charset="utf-8">
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>Uplaod household and member</title>

</head>

<c:url var="saveUrl" value="/data/migration.html" />

<body class="fixed-nav sticky-footer bg-dark" id="page-top">
	
	<div class="content-wrapper" style="text-align: center;">
		<div class="container-fluid">
		
			<div class="card mb-3">
				<div class="card-header">
					<i class="fa fa-table"></i> Upload household and member
				</div>
				<br />
				<br />
				<div class="card-body">				
					<form:form method="POST" action="${saveUrl}?${_csrf.parameterName}=${_csrf.token}" modelAttribute="location" enctype="multipart/form-data">
						<div class="form-group">
							<div class="row">
								<div class="col-5">
									<label for="exampleInputName">File  </label>
									<input id="file" type="file" name="file" />										
								</div>
								
							</div>
							<span class="text-red">${msg}</span>
						</div>
						<br />
						<br />
						<div class="form-group">
							<div class="row">
								<div class="col-3">
									<input type="submit" value="Upload"
										class="btn btn-primary btn-block" />
								</div>
							</div>
						</div>
					</form:form>
				</div>
			</div>
		</div>
		<!-- /.container-fluid-->
		<!-- /.content-wrapper-->
		
	</div>
	
  
        
</body>
</html>
