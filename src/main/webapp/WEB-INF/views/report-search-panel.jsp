<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="org.opensrp.web.util.AuthenticationManagerUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
		 pageEncoding="ISO-8859-1"%>
		 <%@ page import="org.opensrp.core.entity.Branch" %>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>


<%
	List<Object[]> divisions = (List<Object[]>) session.getAttribute("divisions");
	String startDate = (String) session.getAttribute("startDate");
	String endDate = (String) session.getAttribute("endDate");
%>
<div class="card mb-3">
	<div class="card-header">
		<i class="fa fa-table"></i> ${title.toString()} <spring:message code="lbl.searchArea"/>
	</div>
	<div class="card-body">

		<div class="row">
		</div>
			<div class="row">
				<div class="col-2">
					<label><spring:message code="lbl.startDate"/></label>
					<input class="form-control custom-select custom-select-lg mb-3" type=text
						   name="start" id="start" value="<%=startDate%>">
				</div>
				<div class="col-2">
					<label><spring:message code="lbl.endDate"/></label>
					<input class="form-control custom-select custom-select-lg mb-3" type="text"
						   name="end" id="end" value="<%=endDate%>">
				</div>
				<% if (AuthenticationManagerUtil.isAM()){%>
				<div class="col-2">
					<label for="">Report Options</label>
					<select  class="custom-select custom-select-lg mb-3" id="locationoptions"
							name="division">
							<option value="">Select location</option>
							
						<option value="catchmentArea">Own Area
						</option>
						<option value="geolocation">Geo Location
						</option>
					</select>
				</div>   
				<%}%>
                        <% if (AuthenticationManagerUtil.isAM()) {%>
                            <div class="col-2" id="branchHide">
                                <label><spring:message code="lbl.branches"/></label>
                                <select class="custom-select custom-select-lg mb-3" id="branchaggregate" name="branch">
                                    <option value="">All Branch</option>
                                    <%
                                        List<Branch> ret = (List<Branch>) session.getAttribute("branchList");
                                        for (Branch str : ret) {
                                    %>
                                    <option value="<%=str.getId()%>"><%=str.getName()%></option>
                                    <%}%>
                                </select>
                            </div>
                            <%}%>
				<div class="col-2" id="divisionHide">
					<label><spring:message code="lbl.selectDivision"/></label>
					<select required class="custom-select custom-select-lg mb-3" id="division"
							name="division">
						<option value=""><spring:message code="lbl.selectDivision"/>
						</option>
						<%
							for (Object[] objects : divisions) {
						%>
						<option value="<%=objects[1]%>?<%=objects[0]%>"><%=objects[0]%></option>
						<%
							}
						%>
					</select>
				</div>

				<div class="col-2" id="districtHide">
					<label><spring:message code="lbl.selectDistrict"/></label>
					<select class="custom-select custom-select-lg mb-3" id="district"
							name="district">
						<option value="0?"><spring:message code="lbl.selectDistrict"/></option>
						<option value=""></option>
					</select>
				</div>
				<div class="col-2" id="upazilaHide">
					<label><spring:message code="lbl.selectUpazila"/></label>
					<select class="custom-select custom-select-lg mb-3" id="upazila"
							name="upazila">
						<option value="0?"><spring:message code="lbl.selectUpazila"/></option>
						<option value=""></option>

					</select>
				</div>
			</div>
			<input type="hidden" id ="address_field" name="address_field"/>
			<input type="hidden" id ="searched_value" name="searched_value"/>
			<input type="hidden" id ="currentUser" value="<%= AuthenticationManagerUtil.isAM()%>">
			<div class="row">

				<div class="col-6">
					<button onclick="onSearchClicked()"
							type="submit"
							class="btn btn-primary">
						<spring:message code="lbl.search"/>
					</button>
				</div>
			</div>
	</div>
	<div class="card-footer small text-muted"></div>
</div>
