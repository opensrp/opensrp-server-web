<%@page import="org.opensrp.web.util.PaginationHelperUtil"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<%
	List<Integer> pageList = (List<Integer>) session
			.getAttribute("pageList");
	String offSet = request.getParameter("pageNumber");

	Map<String, String> paginationAtributes = (Map<String, String>) session
			.getAttribute("paginationAtributes");

	// String paginationLink = divisionLink+districtLink+upazilaLink+unionLink + subunitLink+mauzaparaLink+providerLink+nameLink+searchLink;
	/* disabledLINK has been used to to make current page number nonhiperlink i.e unclickable
	e.g if user is at page number 15 then page number 15 should not be clickable*/
	String paginationLink = "";
	if (paginationAtributes.containsKey("paginationLink")) {
		paginationLink = paginationAtributes.get("paginationLink");
	}
	int disabledLINK = 0;
	if (offSet != null) {
		disabledLINK = Integer.parseInt(offSet);
	}
	/* size is used for moving user to end page  by clicking on END link*/
	int size = Integer
			.parseInt(session.getAttribute("size").toString());
%>

<div class="row">
	<div class="col-sm-12 col-md-5">
		<div class="dataTables_info" id="dataTable_info"></div>
	</div>

	<div class="col-sm-12 col-md-7">
		<div class="dataTables_paginate paging_simple_numbers"
			id="dataTable_paginate">
			<ul class="pagination">

				<li class="paginate_button page-item next" id="dataTable_next" onclick="goTo(0)">
					<a data-dt-idx="7" tabindex="0" class="page-link">Start</a>
				</li>

				<%
					for (Integer i : pageList) {
						if (disabledLINK == i) {
							if (disabledLINK != size) {
				%>
				<li class="paginate_button page-item disabled" ><a
					data-dt-idx="1" tabindex="0" class="page-link" onclick="goTo(<%=i%>)" > <%=i + ""%></a></li>
				<%
					}
						} else {
				%>
				<li class="paginate_button page-item active" ><a data-dt-idx="1"
					tabindex="0" class="page-link"  onclick="goTo(<%=i%>)"  > <%=i + ""%></a></li>
				<%
					}
					}
				%>
				<li class="paginate_button page-item next" id="dataTable_next"><a
					data-dt-idx="7" tabindex="0" class="page-link" onclick="goTo(<%=  Integer.parseInt(session.getAttribute("size").toString()) -1 %>)" >End</a>
				</li>
			</ul>
		</div>
	</div>
</div>
