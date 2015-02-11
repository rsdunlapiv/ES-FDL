package backend;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import play.Logger;

public class OWLBackend {

	private OWLOntology ontology;
	private OWLDataFactory factory;
	private PrefixManager pm;
	
	//private static OWLAnnotationProperty annUISearchTreeItem; 
	//private static OWLAnnotationProperty annUISearchTreeItemGroup1; 
	//private static OWLAnnotationProperty annUISearchTreeItemGroup2; 
	private static OWLAnnotationProperty annUISearchTreeTopLevel;
	private static OWLAnnotationProperty annUISearchTreeOrder;
	
	public OWLBackend() {
		loadOntology();
		//annUISearchTreeItem = factory.getOWLAnnotationProperty("#uiSearchTreeItem", pm);
		//annUISearchTreeItemGroup1 = factory.getOWLAnnotationProperty("#uiSearchTreeItemGroup1", pm);
		//annUISearchTreeItemGroup2 = factory.getOWLAnnotationProperty("#uiSearchTreeItemGroup2", pm);
		annUISearchTreeTopLevel = factory.getOWLAnnotationProperty("#uiSearchTreeTopLevel", pm);
		annUISearchTreeOrder = factory.getOWLAnnotationProperty("#uiSearchTreeOrder", pm);
	}
	
	private void loadOntology() {
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
	}
	
	
	public List<Map<String,Object>> getSearchTree() {
	
		loadOntology();  //reloading every time while debugging, comment out before deploying
		
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
	
		sort(ret);
		return ret;
		
	}
	
	public Map<String,Object> getSearchTree(OWLClass c) {
	
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("iri", c.getIRI().toString());
		fields.put("label", getLabel(c));
		String desc = getAnnotationPropertyValue(c, factory.getRDFSComment());
		if (desc != null) {
			fields.put("desc", desc);
		}
		fields.put("order", getSortOrder(c));
		
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
					OWLNamedIndividual ni = i.asOWLNamedIndividual();
					Map<String, Object> fields = new HashMap<String, Object>();
					fields.put("iri", ni.getIRI().toString());
					fields.put("label", getLabel(ni));
					String desc = getAnnotationPropertyValue(ni, factory.getRDFSComment());
					if (desc != null) {
						fields.put("desc", desc);
					}
					fields.put("order", getSortOrder(ni));
					fields.put("leaf", true);
					ret.add(fields);
				}
			}
		}
		
		sort(ret);
		
		return ret;
		
	}
	
	private void sort(List<Map<String, Object>> toSort) {
		Collections.sort(toSort, new Comparator<Map<String,Object>>() {

			@Override
			public int compare(Map<String, Object> left, Map<String, Object> right) {
				int lefti = 0;
				int righti = 0;
				
				if (left.containsKey("order")) {
					lefti = (int) left.get("order");
				}
				if (right.containsKey("order")) {
					righti = (int) right.get("order");
				}
				
				return lefti - righti;
			}
			
		});
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
	
	public int getSortOrder(OWLEntity e) {
		String val = getAnnotationPropertyValue(e, annUISearchTreeOrder);
		if (val != null) {
			return Integer.valueOf(val);
		}
		return 0;
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
