function fecthDataAndUpdateContent(modellinginfraname){
	var url = "/getallmodelinginfrabriefdetails/" + modellinginfraname;
	var link_to_details = "/details/" + modellinginfraname;
	$.ajax({
		url : url,
		type: 'GET',
		dataType: 'json',
		contentType: 'application/json',
	}).done(function(data){
		console.log(data['longName']);
		if(data['longName'] != undefined){
			$("#longname").empty().append("<a href="+ link_to_details +"><b>" + data['longName'] + "</b></a>");
		}else{
			$("#longname").empty().append("<a href="+ link_to_details +"><b>" + data['shortName'] + "</b></a>");
		}
		$("#details").empty().append(data['comment'])
		if(data['homePage'] != undefined){
			$("#homepage").empty().append(data['homePage']);
		}else{
			$("#homepage").empty();
		}
	});
}

$(document).ready(function () {
    var source = [
   		"ESMF",
   		"CSDMS",
   		"OASIS",
   		"MCT"];
	$("#ListBox").jqxListBox({ selectedIndex: 0, source: source, width: 210, height: 150});
	var item = $('#ListBox').jqxListBox('getItem', 0);
	console.log(item['value']);
	fecthDataAndUpdateContent(item['value']);
	$('#ListBox').on('select', function (event) {
    var args = event.args;
	var item = $('#ListBox').jqxListBox('getItem', args.index);	
    if (item != null) {
		fecthDataAndUpdateContent(item['value']);
    }
    });
});