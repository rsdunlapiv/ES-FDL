$(document).ready(function () {
    // Create jqxDockPanel
    //$("#jqxDockPanel").jqxDockPanel({ width: document.body.clientWidth, height: document.body.clientHeight});
    var source =
    {
        dataType: "json",
        dataFields: [
            { name: "label", type: "string" },
            { name: "iri", type: "string" },
            { name: "leaf", type: "bool" },
            { name: "desc", type: "string" }, 
            { name: "children", type: "array" }
        ],                 
			hierarchy:
            {
                root: "children"
            },
            id: "iri",
            url: "/owl/searchtree"
    };

    var dataAdapter = new $.jqx.dataAdapter(source, {
        loadComplete: function () {
                }
    });            
            // create jqxTreeGrid.
    $("#TreeGrid").jqxTreeGrid(
    {
        source: dataAdapter,
        altRows: false,
        width: 500,
        selectionMode: 'custom',
        checkboxes: function (rowKey, dataRow) {
	    var leaf = dataRow.leaf;
	    return leaf;	
	},
    ready: function () {
        //$("#first").jqxTreeGrid('expandRow', '1');
        //$("#first").jqxTreeGrid('expandRow', '2');
    },
    columns: [
        { text: "Label", align: "left", dataField: "label", width: 300 },
        { text: "Description", align: "left", dataField: "desc", width: 200}             
    ]});
      // Apply custom layout depending on the user's choice.
    });
        
function getValuesFromObject(obj){
	var queue = [];
	var checked_values = [];
	var checked_props = [];
	for(var i=0; i<obj.length; i++){
		queue.push(obj[i]);
    }
    while(queue.length!=0){
        var j = queue.shift();
        if(j['children']=== undefined){
        	if(j['checked'] === true){
        		checked_props.push(j['label']);
        		checked_values.push({'iri':j['iri'],'parentiri':j['parent']['iri']});
        	}
        }else{
        	for(var k=0;k<j['children'].length;k++){
        		queue.push(j['children'][k]);
        	}
        }
    }
    return checked_values;
}

function getValues(){
    var selection = $('#TreeGrid').jqxTreeGrid('getRows');
	var selection_json = [];
	var temp_map = {}
    for(var i=0;i<selection.length;i++){
        var rowvalue = selection[i];
        if(rowvalue['children']!= undefined){
       		var value = getValuesFromObject(rowvalue['children']);
       		for(var j=0; j < value.length;j++){
	       		if(value[j]["parentiri"] in temp_map){
	       			temp_map[value[j]["parentiri"]].push(value[j]["iri"]);
	       		}else{
	       			temp_map[value[j]["parentiri"]] = [];
	       			temp_map[value[j]["parentiri"]].push(value[j]["iri"]);       				
	       		}
       		}
        }
    }    
    for(var k in temp_map){
    	var temp_selection = {};
    	temp_selection["parentiri"] = k;
    	temp_selection["selected"] = temp_map[k];
    	selection_json.push(temp_selection);
    }
    
    selection_json = JSON.stringify(selection_json);
    
    $.ajax({
    	url : "/owl/query",
    	type : 'POST',
    	dataType : 'json',
    	contentType : 'application/json',
    	data : selection_json
    	}).done(function(data){
    		console.log(data);
    		var table_to_be_inserted = '<table id = "hook_added_table">';
    		for(var key in data){
    			//console.log(key + " -> ");
    			table_to_be_inserted = table_to_be_inserted + '<tr><td><b>' + key + '</b></td></tr>';
    			for(var val in data[key]){
    				table_to_be_inserted = table_to_be_inserted + '<tr><td>' + data[key][val]['label'] + '</td><td>' + data[key][val]['description'] + '</td></tr>';
    				//console.log(data[key][val]['label']);
    				//console.log(data[key][val]['description']);
    			}
    		   	table_to_be_inserted = table_to_be_inserted + '<tr></tr>';
    		}
    	table_to_be_inserted = table_to_be_inserted + '</table>';
    	var previously_added_table = $('#hook_added_table');
    	if(previously_added_table.length != 0){
    		previously_added_table.remove();
    	}
    	$('#section1').append(table_to_be_inserted);
    	});
}