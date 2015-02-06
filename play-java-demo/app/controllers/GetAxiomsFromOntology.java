package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class GetAxiomsFromOntology {

	HashMap<String, ArrayList<String>> objpropandvalues;
	HashMap<String, ArrayList<String>> datapropandvalues;
	OWLOntologyManager manager;
	File file;
	OWLOntology owlOntology;
	IRI ontologyIRI;
	OWLDataFactory factory;
	OWLIndividual modelInfraIndividal;

	public GetAxiomsFromOntology(String modellinginfraname) {
		objpropandvalues = new HashMap<String, ArrayList<String>>();
		datapropandvalues = new HashMap<String, ArrayList<String>>();
		manager = OWLManager.createOWLOntologyManager();
		file = new File("owl/EFFDL_From_Questionnaire.owl");
		try {
			owlOntology = manager.loadOntologyFromOntologyDocument(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ontologyIRI = owlOntology.getOntologyID().getOntologyIRI();
		factory = manager.getOWLDataFactory();
		modelInfraIndividal = factory.getOWLNamedIndividual(IRI
				.create(ontologyIRI + "#" + modellinginfraname));
	}

	public HashMap<String, ArrayList<String>> getObjpropandVal() {
		Set<OWLObjectPropertyAssertionAxiom> objpropaxioms = owlOntology
				.getObjectPropertyAssertionAxioms(modelInfraIndividal);
		for (OWLObjectPropertyAssertionAxiom e : objpropaxioms) {
			Set<OWLObjectProperty> owlopset = e
					.getObjectPropertiesInSignature();
			Iterator<OWLObjectProperty> itop = owlopset.iterator();
			OWLObjectProperty firstop = itop.next();
			Set<OWLAnnotation> annotationsset = firstop.getAnnotations(
					owlOntology, factory.getRDFSLabel());
			Iterator<OWLAnnotation> itann = annotationsset.iterator();
			OWLAnnotation first_an = itann.next();
			String prop = first_an.getValue().toString();
			prop = prop.substring(1, prop.length() - 1);
			String obj = e.getObject().toString().split("ontology#")[1];
			obj = obj.substring(0, obj.length() - 1);
			ArrayList<String> temp;
			if (objpropandvalues.containsKey(prop)) {
				temp = objpropandvalues.get(prop);
				temp.add(obj);
			} else {
				temp = new ArrayList<String>();
				temp.add(obj);
			}
			objpropandvalues.put(prop, temp);
		}
		return objpropandvalues;
	}

	public HashMap<String, ArrayList<String>> getDatapropandVal() {
		Set<OWLDataPropertyAssertionAxiom> datapropaxioms = owlOntology
				.getDataPropertyAssertionAxioms(modelInfraIndividal);
		for (OWLDataPropertyAssertionAxiom e : datapropaxioms) {
			Set<OWLDataProperty> owldpset = e.getDataPropertiesInSignature();
			Iterator<OWLDataProperty> itdp = owldpset.iterator();
			OWLDataProperty firstdp = itdp.next();
			Set<OWLAnnotation> annotationsset = firstdp.getAnnotations(
					owlOntology, factory.getRDFSLabel());
			Iterator<OWLAnnotation> itann = annotationsset.iterator();
			OWLAnnotation first_an = itann.next();
			String prop = first_an.getValue().toString();
			prop = prop.substring(1, prop.length() - 1);
			String obj = e.getObject().toString().split("xsd")[0];
			obj = obj.substring(1, obj.length() - 3);
			ArrayList<String> temp;
			if (datapropandvalues.containsKey(prop)) {
				temp = datapropandvalues.get(prop);
				temp.add(obj);
			} else {
				temp = new ArrayList<String>();
				temp.add(obj);
			}
			datapropandvalues.put(prop, temp);
		}
		return datapropandvalues;
	}

	public static void main(String args[]) {
		GetAxiomsFromOntology obj = new GetAxiomsFromOntology("ESMF");
		System.out.println("Object property assertion axioms:");
		for (Map.Entry<String, ArrayList<String>> e : obj.getObjpropandVal()
				.entrySet()) {
			System.out.println(e.getKey());
			for (String val : e.getValue()) {
				System.out.println(val);
			}
			System.out.println("--------------------");
		}
		System.out.println("Data property assertion axioms");
		for (Map.Entry<String, ArrayList<String>> e : obj.getDatapropandVal()
				.entrySet()) {
			System.out.println(e.getKey());
			for (String val : e.getValue()) {
				System.out.println(val);
			}
			System.out.println("--------------------");
		}
	}
}