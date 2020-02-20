<%@page import="com.google.gson.JsonArray"%>
<%@page import="com.google.gson.JsonObject"%>
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONArray"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="ISO-8859-1"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.opensrp.common.util.NumberToDigit"%>
<%@page import="org.opensrp.common.visualization.HighChart"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>

<!DOCTYPE html>
<html lang="en">

<head>
<meta charset="utf-8">
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name='viewport'
	content='initial-scale=1,maximum-scale=1,user-scalable=no' />

<title><spring:message code="lbl.growthMonitoringDashboardPageTitle"/></title>

<script
	src='https://api.tiles.mapbox.com/mapbox-gl-js/v0.46.0/mapbox-gl.js'></script>

<jsp:include page="/WEB-INF/views/css.jsp" />

<link href="https://fonts.googleapis.com/css?family=Open+Sans"
	rel="stylesheet">
<link type="text/css" href="<c:url value="/resources/css/mapbox-gl.css"/>"
	rel="stylesheet">
<link type="text/css" href="<c:url value="/resources/css/style.css"/>"
	rel="stylesheet">
<link type="text/css" href="<c:url value="/resources/css/maps.css"/>"
	rel="stylesheet">
<style type="text/css">
#container {
	min-width: 310px;
	max-width: 800px;
	height: 400px;
	margin: 0 auto
}
</style>
</head>

<body class="fixed-nav sticky-footer bg-dark" id="page-top">
	<jsp:include page="/WEB-INF/views/navbar.jsp" />

	<div class="content-wrapper">
		<div class="container-fluid">
			<!-- Breadcrumbs-->
			<ol class="breadcrumb">
				<li class="breadcrumb-item"><spring:message code="lbl.growthMonitoringDashboardPageTitle"/></li>
				
			</ol>
			<!-- Icon Cards-->
			<div class="row">
				<%
					if(session.getAttribute("dashboardAggregatedList") != null){
						List<Object> dashboardAggregatedList = (List<Object>) session.getAttribute("dashboardAggregatedList");
						Iterator dashboardAggregatedListIterator = dashboardAggregatedList.iterator();
						int count = 0;
						while (dashboardAggregatedListIterator.hasNext()) {
							Object[] dashboardCountObject = (Object[]) dashboardAggregatedListIterator.next();
							String countType = String.valueOf(dashboardCountObject[0]);
							String totalCount = String.valueOf(dashboardCountObject[1]);
							String classColor = String.valueOf(dashboardCountObject[2]);
							String isPercentage = String.valueOf(dashboardCountObject[3]);
				%>
				<div class="col-xl-4 col-sm-6 mb-4">
					<div class="card text-white o-hidden h-100 <%=classColor%>">
						<div class="card-body">
							<div class="card-body-icon">
								<%
									if(count > 2) {
								%>
								<i class="fa fa-fw fa-female"></i>
								<%
									} else {
								%>
								<i class="fa fa-fw fa-child"></i>
								<%
									}
								%>
							</div>
							<div class="mr-5">
								<%
									Integer totalChildCount = (Integer) session.getAttribute("totalChildCount");
									if (isPercentage.equalsIgnoreCase("true")) {
								%>
								<h3><%=totalCount%>%
								</h3>
								<%
									} else {
										int total = (int) Double.parseDouble(totalCount);
								%>
								<h3><%=total%></h3>
								<%
									}
								%>
								<%=countType%>
							</div>
						</div>
					</div>
				</div>
				<%
					count++;
							}
						}
				%>
			</div>



			<!-- Area Chart Example-->
			<div class="card mb-3">
				<div class="card-header">
					<spring:message code="lbl.growthfalteringStatus"/>
				</div>
				<div class="card-body" style="height: 440px">
				    <div id='map' style="height: 400px; padding: 0"></div>
				</div>
				<div class="card-footer medium text-muted">
					<span class="col-6">
					    <img src="<c:url value="/resources/images/adequate_growth.jpg"/>" width="40"
					    height="10"></span><spring:message code="lbl.adequateGrowth"/> 
					<span class="col-6">
					   <img src="<c:url value="/resources/images/growth_faltering.jpg"/>" width="40"
					    height="10"></span> <spring:message code="lbl.inadequateGrowth"/> 
				</div>
			</div>



			<!-- Area Chart Example-->
			<div class="card mb-3">
				<div id="lineChart" class="card-body"></div>
			</div>





		</div>
		<!-- /.container-fluid-->


		<%
		    JSONArray lineChartData = null;
		    JSONArray lineChartCategory = null;
		    JSONObject featureCollectionOfGrowthFaltering = null;
		    JSONObject featureCollectionOfAdequateGrowth = null;
		    if (session.getAttribute("lineChartData") != null) {
			    lineChartData = (JSONArray)session.getAttribute("lineChartData");
		    }
		    if (session.getAttribute("lineChartCategory") != null) {
			    lineChartCategory = (JSONArray)session.getAttribute("lineChartCategory");
		    }
		    if (session.getAttribute("featureCollectionOfGrowthFaltering") != null) {
			    featureCollectionOfGrowthFaltering = (JSONObject)session.getAttribute("featureCollectionOfGrowthFaltering");
		    }
		    if (session.getAttribute("featureCollectionOfAdequateGrowth") != null) {
		        featureCollectionOfAdequateGrowth = (JSONObject)session.getAttribute("featureCollectionOfAdequateGrowth");
		    }
		%>
		<jsp:include page="/WEB-INF/views/footer.jsp" />
	</div>


	<script>
		mapboxgl.accessToken = 'pk.eyJ1IjoibnVyc2F0aiIsImEiOiJjamp6ZDU5ZmswOG9zM3JwNTJvN3FzYWNyIn0.PLU3v5A_kNUrfkZLQq4E8w';

		var map = new mapboxgl.Map({
			container : 'map',
			style : 'mapbox://styles/mapbox/streets-v9',
			center : [ 90.399452, 23.777176 ],
			zoom : 12
		});

		// code from the next step will go here!
		var geojsonAdequateGrowth = <%=featureCollectionOfAdequateGrowth%>
		var geoJsonGrowthFaltering = <%=featureCollectionOfGrowthFaltering%>

		// add markers to map
		geojsonAdequateGrowth.features.forEach(function(marker) {
			// create a HTML element for each feature
			var el = document.createElement('div');
			el.className = 'markerGreen';

			// make a marker for each feature and add to the map
			new mapboxgl.Marker(el).setLngLat(marker.geometry.coordinates)
					.setPopup(
							new mapboxgl.Popup({
								offset : 25
							}) // add popups
							.setHTML('<h5>' + marker.properties.title
									+ '</h5><p>'
									+ 'Gender: ' + marker.properties.gender
									+ '<br />'
									+ 'Age: ' +marker.properties.age
									+ '<br />'
									+ 'Weight: ' +marker.properties.weight
									+ '<br />'
									+ 'Provider: ' + marker.properties.provider
									+ '</p>'))
					.addTo(map);
		});

		geoJsonGrowthFaltering.features.forEach(function(marker) {
			// create a HTML element for each feature
			var el = document.createElement('div');
			el.className = 'markerRed';

			// make a marker for each feature and add to the map
			new mapboxgl.Marker(el).setLngLat(marker.geometry.coordinates)
					.setPopup(
							new mapboxgl.Popup({
								offset : 25
							}) // add popups
							.setHTML('<h5>' + marker.properties.title
									+ '</h5><p>'
									+ 'Gender: ' + marker.properties.gender
									+ '<br />'
									+ 'Age: ' +marker.properties.age
									+ '<br />'
									+ 'Weight: ' +marker.properties.weight
									+ '<br />'
									+ 'Provider: ' + marker.properties.provider
									+ '</p>'))
					.addTo(map);
		});
	</script>

	<script src="<c:url value='/resources/chart/highcharts.js'/>"></script>
	<script src="<c:url value='/resources/chart/data.js'/>"></script>
	<script src="<c:url value='/resources/chart/drilldown.js'/>"></script>
	<script src="<c:url value='/resources/chart/series-label.js'/>"></script>
	<script type="text/javascript">
		Highcharts.chart('lineChart', {
			chart : {
				type : 'line'
			},
			title : {
				text : '<spring:message code="lbl.growthFalteringOverTime"/>' 
			},
			subtitle : {
				text : ''
			},
			credits : {
				enabled : false
			},
			xAxis : {
				categories : <%=lineChartCategory%>
		},
			yAxis : {
				title : {
					text : '<spring:message code="lbl.growthFaltering"/>'  
				}
			},

			legend : {
				layout : 'vertical',
				align : 'right',
				verticalAlign : 'middle'
			},

			plotOptions : {
				line : {
					dataLabels : {
						enabled : true
					},
					enableMouseTracking : true
				}
			},

			responsive : {
				rules : [ {
					condition : {
						maxWidth : 500
					},
					chartOptions : {
						legend : {
							layout : 'horizontal',
							align : 'center',
							verticalAlign : 'bottom'
						}
					}
				} ]
			},

			series : <%=lineChartData%>
		});
	</script>

</body>
</html>