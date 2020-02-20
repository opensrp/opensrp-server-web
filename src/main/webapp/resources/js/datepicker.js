$(function() {
		$("#start")
				.datepicker(
						{
							dateFormat : "yy-mm-dd",
							maxDate : new Date,
							onSelect : function(selectedDate) {
								var orginalDate = new Date(selectedDate);
								var monthsAddedDate = new Date(new Date(
										orginalDate).setMonth(orginalDate
										.getMonth() + 12));

								$("#end").datepicker("option", 'minDate',
										selectedDate);
								$("#end").datepicker("option", 'maxDate',
										monthsAddedDate);
							}
						});

		$("#end").datepicker(
				{
					dateFormat : "yy-mm-dd",
					maxDate : new Date,
					onSelect : function(selectedDate) {
						var orginalDate = new Date(selectedDate);
						var monthsAddedDate = new Date(new Date(orginalDate)
								.setMonth(orginalDate.getMonth() - 12));

						$("#start").datepicker("option", 'minDate',
								monthsAddedDate);
						$("#start").datepicker("option", 'maxDate',
								selectedDate);
					}
				});
	});
