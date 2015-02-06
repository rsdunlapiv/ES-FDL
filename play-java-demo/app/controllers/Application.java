package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;

import models.Modellinginfrastructure;
import models.PropertiesAndValues;
import play.*;
import play.data.Form;
import play.mvc.*;
import play.libs.Json;

import views.html.*;

public class Application extends Controller {

	final static Form<Modellinginfrastructure> modellingInfraForm = Form
			.form(Modellinginfrastructure.class);

	public static Result index() {
		return ok(index.render(modellingInfraForm));
	}

	public static Result submit() {
		if (modellingInfraForm.hasErrors()) {
			return badRequest(index.render(modellingInfraForm));
		} else {
			Modellinginfrastructure given = modellingInfraForm
					.bindFromRequest().get();
			GetAxiomsFromOntology axiomretriever = new GetAxiomsFromOntology(
					given.name);
			PropertiesAndValues propvalsAndValues = new PropertiesAndValues();
			propvalsAndValues.objpropandval = axiomretriever.getObjpropandVal();
			propvalsAndValues.datapropandval = axiomretriever
					.getDatapropandVal();
			return ok(submit.render(propvalsAndValues, given.name));
		}
	}

	public static Result getNames(String id) {
		ObjectNode result = Json.newObject();
		result.put("working", "working");
		return ok(result);
	}

	public static Result getProperties(String name) {
		GetAxiomsFromOntology axiomretriever = new GetAxiomsFromOntology(name);
		PropertiesAndValues propandvals = new PropertiesAndValues();
		propandvals.datapropandval = axiomretriever.getDatapropandVal();
		propandvals.objpropandval = axiomretriever.getObjpropandVal();
		return ok(Json.toJson(propandvals));
	}
}