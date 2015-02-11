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
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
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
	private static OWLAnnotationProperty annUISearchTreeTopLevel;
	
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
		annUISearchTreeTopLevel = factory.getOWLAnnotationProperty("#uiSearchTreeTopLevel", pm);
		
	}
	
	/*
	public List<Map<?,?>> getSearchTree() {
		
		List<Map<?,?>> ret = new ArrayList<Map<?,?>>();
		
		Map<Object, Object> m1 = new HashMap<Object, Object>();
		m1.put("m1a", "aaa");
		m1.put("m1b", "bbb");
		
		Map<Object, Object> m2 = new HashMap<Object, Object>();
		m2.put("m2a", "aaaaa");
		m2.put("m2b", "bbbbbb");
		m2.put("anothermap", m1);
		
		ret.add(m2);
		
		return ret;
		
	}
	*/
	
	public List<Map<String,Object>> getSearchTree() {
		
		List<Map<String,Object>> ret = new ArrayList<Map<String,Object>>();
		
		for (OWLClass c : ontology.getClassesInSignature()) {
			Set<OWLAnnotation> anns = c.getAnnotations(ontology, annUISearchTreeTopLevel);
			for (OWLAnnotation a : anns) {
				if (a.getValue() instanceof OWLLiteral) {
					boolean val = ((OWLLiteral) a.getValue()).parseBoolean();
					if (val) {	
						Map<String, Object> fields = getSearchTree(c);
						ret.add(fields);
						break;  //annotation loop
					}
				}
			}
		}
	
		return ret;
		
	}
	
	public Map<String,Object> getSearchTree(OWLClass c) {
	
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("iri", c.getIRI().toString());
		fields.put("label", getLabel(c));
		
		List<Map<String,Object>> children = getSearchTreeChildren(c);
		if (children.size() > 0) {
			fields.put("children", children);
		}
		
		return fields;
	}
	
	public List<Map<String,Object>> getSearchTreeChildren(OWLClass c) {
		List<Map<String,Object>> ret = new ArrayList<Map<String,Object>>();
		
		Set<OWLClassExpression> subclasses = c.getSubClasses(ontology);
		
		if (subclasses.size() > 0) {
			for (OWLClassExpression oce : subclasses) {
				if (oce instanceof OWLClass) {
					ret.add(getSearchTree((OWLClass) oce));
				}
			}
		}
		else {
			//at leaf level, look for individuals
			for (OWLIndividual i : c.getIndividuals(ontology)) {
				if (i.isNamed()) {
					Map<String, Object> fields = new HashMap<String, Object>();
					fields.put("iri", i.asOWLNamedIndividual().getIRI().toString());
					fields.put("label", getLabel(i.asOWLNamedIndividual()));
					fields.put("leaf", true);
					ret.add(fields);
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
	
	public String getLabel(OWLEntity e) {
		String val = getAnnotationPropertyValue(e, factory.getRDFSLabel());
		if (val == null) {
			val = e.getIRI().getFragment();
		}
		return val;
	}
	
	public String getAnnotationPropertyValue(OWLEntity e, OWLAnnotationProperty prop) {
		Set<OWLAnnotation> anns = e.getAnnotations(ontology, prop);
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
