package backend;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import play.Logger;

public class OWLBackend {

	private OWLOntology ontology;
	private OWLDataFactory factory;
	private PrefixManager pm;
	
	private static OWLAnnotationProperty annUISearchTreeItem; 
	private static OWLAnnotationProperty annUISearchTreeItemGroup1; 
	private static OWLAnnotationProperty annUISearchTreeItemGroup2; 
	
	public OWLBackend() {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File("owl/ESFDL_Ontology_v2.owl");
		OWLOntology ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(file);
		} catch (Exception e) {
			Logger.error("Could not load ontology file", e);
			return;
		}
		
		this.ontology = ontology;
		this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI();
		this.pm = new DefaultPrefixManager(ontologyIRI.toString());
		
		annUISearchTreeItem = factory.getOWLAnnotationProperty("#uiSearchTreeItem", pm);
		annUISearchTreeItemGroup1 = factory.getOWLAnnotationProperty("#uiSearchTreeItemGroup1", pm);
		annUISearchTreeItemGroup2 = factory.getOWLAnnotationProperty("#uiSearchTreeItemGroup2", pm);
		
	}
	
		
	public List<Map<String,String>> getSearchTree() {
		
		List<Map<String,String>> ret = new ArrayList<Map<String,String>>();
		
		for (OWLClass c : ontology.getClassesInSignature()) {
			Set<OWLAnnotation> anns = c.getAnnotations(ontology, annUISearchTreeItem);
			for (OWLAnnotation a : anns) {
				if (a.getValue() instanceof OWLLiteral) {
					boolean val = ((OWLLiteral) a.getValue()).parseBoolean();
					if (val) {					
						Map<String,String> fields = new HashMap<String,String>();
						fields.put("iri", c.getIRI().toString());
						fields.put("label", getLabel(c));
						fields.put("group1", getAnnotationPropertyValue(c,  annUISearchTreeItemGroup1));
						fields.put("group2", getAnnotationPropertyValue(c,  annUISearchTreeItemGroup2));
						ret.add(fields);
						break;
					}
				}
			}
		}
	
		return ret;
		
	}
	
	public List<OWLClass> getChildren(OWLClass c) {
		ArrayList<OWLClass> ret = new ArrayList<OWLClass>();
		for (OWLClassExpression oce : c.getSubClasses(ontology)) {
			if (oce instanceof OWLClass) {
				ret.add((OWLClass) oce);
			}
		}
		return ret;
	}
	
	public String getLabel(OWLClass c) {
		String val = getAnnotationPropertyValue(c, factory.getRDFSLabel());
		if (val == null) {
			val = c.getIRI().getFragment();
		}
		return val;
	}
	
	public String getAnnotationPropertyValue(OWLClass c, OWLAnnotationProperty prop) {
		Set<OWLAnnotation> anns = c.getAnnotations(ontology, prop);
		//take first one
		for (OWLAnnotation a : anns) {
			if (a.getValue() instanceof OWLLiteral) {
				return ((OWLLiteral)a.getValue()).getLiteral();
			}
		}
		return null;
	}
	
	public static class TreeNode {
		OWLClass clazz;
		OWLIndividual individual;
		String label;
	}
	
	
}
