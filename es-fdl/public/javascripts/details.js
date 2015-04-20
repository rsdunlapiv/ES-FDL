$(document).ready(function(){
	var currentLocationPathname = window.location.pathname;
	var modelingInfra = currentLocationPathname.split("details/")[1];
	var url = "/getallmodelinginfrabriefdetails/" + modelingInfra;
	$.ajax({
		url: url,
		type: 'GET',
		dataType: 'json',
		contentType: 'application/json'
	}).done(function(data){
		if(data['longName'] != undefined){
			$("#name_div").append('<b>' + data['longName'] + '</b>');
		}
		if(data['homePage'] != undefined){
			$("#home_page_div").append('<a href="' + data['homePage'] + '">' + data['homePage'] + '</a>');
		}
	});
	var url = '/getmodelinginfradetails/' + modelingInfra;
	$.ajax({
		url: url,
		type: 'GET',
		dataType: 'json',
		contentType: 'application/json',		
	}).done(function(data){
		var tab_names = '<ul>';
		var tab_contents = '';
		console.log(data);
		for(var item in data){
			console.log(item + '-->');
			tab_names = tab_names + '<li>' + item + '</li>';
			tab_contents = tab_contents + '<div id="data_table_div">';
			tab_contents = tab_contents + '<table id="data_table">';
			for(i=0; i<data[item].length; i++){
				var obj = data[item][i];
				for(var prop in obj){
					console.log(prop + ':');
					tab_contents = tab_contents + '<tr><td>' + prop + '</td>';
					for(j=0; j< obj[prop].length; j++){
						if(j == 0){
							tab_contents = tab_contents + '<td>' + obj[prop][j] +'</td>' + '</tr>';
						}else{
							tab_contents = tab_contents + '<tr><td></td><td>' + obj[prop][j] +'</td>' + '</tr>';
						}
						console.log(obj[prop][j]);
					}
				tab_contents = tab_contents + '<tr><td></td><td></td></tr><tr><td></td><td></td></tr>';
				}
			}
			tab_contents = tab_contents + '</table></div>';
		}
		tab_names = tab_names + '</ul>';
		$("#jqxTabs").append(tab_names);
		$("#jqxTabs").append(tab_contents);
		var number_of_tabs = $("#jqxTabs").children("ul").children("li").length;
		if(number_of_tabs > 0){
			$('#jqxTabs').jqxTabs({position: 'top'});
		}
	});
});