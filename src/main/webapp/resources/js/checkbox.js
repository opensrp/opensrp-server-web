jQuery(document).ready(function($) {
    $("#ckbCheckAll").click(function () {    	
        $(".checkBoxClass").prop('checked', $(this).prop('checked'));
    });
});