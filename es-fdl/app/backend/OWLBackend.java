package backend;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.stringtemplate.v4.ST;

import com.fasterxml.jackson.databind.JsonNode;
//import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Annotation;

import controllers.Application;
import play.Logger;

public class OWLBackend {

	private OWLOntology ontology;
	private OWLDataFactory factory;
	private PrefixManager pm;

	// private static OWLAnnotationProperty annUISearchTreeItem;
	// private static OWLAnnotationProperty annUISearchTreeItemGroup1;
	// private static OWLAnnotationProperty annUISearchTreeItemGroup2;
	private static OWLAnnotationProperty annUISearchTreeTopLevel;
	private static OWLAnnotationProperty annUISearchTreeOrder;

	public OWLBackend() {
		loadOntology();
		// annUISearchTreeItem =
		// factory.getOWLAnnotationProperty("#uiSearchTreeItem", pm);
		// annUISearchTreeItemGroup1 =
		// factory.getOWLAnnotationProperty("#uiSearchTreeItemGroup1", pm);
		// annUISearchTreeItemGroup2 =
		// factory.getOWLAnnotationProperty("#uiSearchTreeItemGroup2", pm);
		annUISearchTreeTopLevel = factory.getOWLAnnotationProperty(
				"#uiSearchTreeTopLevel", pm);
		annUISearchTreeOrder = factory.getOWLAnnotationProperty(
				"#uiSearchTreeOrder", pm);
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

	public List<Map<String, Object>> query(JsonNode json) {

		if (Application.isDebug) {
			// reload every time while debugging and change ontology frequently
			loadOntology();
		}

		// the final result list
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();

		// the query map has the class name as the key and the
		// list of selected instances as the value, e.g.:
		// GridCoordinateSystem => [Cartesian, Spherical]
		Map<String, List<String>> queryMap = new HashMap<String, List<String>>();

		Iterator<JsonNode> iter = json.elements();
		while (iter.hasNext()) {
			JsonNode jsn = iter.next();
			IRI parentIRI = IRI.create(jsn.get("parentiri").asText());
			String parentClassName = parentIRI.getFragment();
			// Logger.info("iri fragment =" + iri.getFragment());

			List<String> instanceList = new ArrayList<String>();
			Iterator<JsonNode> selected = jsn.get("selected").elements();
			while (selected.hasNext()) {
				JsonNode jsns = selected.next();
				IRI selectedIRI = IRI.create(jsns.asText());
				String instanceName = selectedIRI.getFragment();
				instanceList.add(instanceName);
			}
			queryMap.put(parentClassName, instanceList);
		}

		// String query =
		// "ModelingInfrastructure and (hasBasicCapabilities some ({AngleConversion}))";

		OWLReasoner reasoner = new Reasoner.ReasonerFactory()
				.createReasoner(ontology);
		ShortFormProvider shortFormProvider = new SimpleShortFormProvider();

		DLQueryEngine engine = new DLQueryEngine(reasoner, shortFormProvider);

		ST query = new ST(
				"ModelingInfrastructure and <qm:{k  | allows<k> some <leftcurly> <qm.(k):{ins | <ins>}; separator=\", \"> <rightcurly> }; separator=\" and \">");
		query.add("qm", queryMap);
		query.add("leftcurly", "{"); // required due to StringTemplate
		query.add("rightcurly", "}");

		Logger.debug("Query: " + query.render());
		// String query = "ModelingInfrastructure";

		try {
			Set<OWLNamedIndividual> instances = engine.getInstances(
					query.render(), false);
			for (OWLNamedIndividual i : instances) {
				Map<String, Object> fields = new HashMap<String, Object>();
				fields.put("iri", i.getIRI().toString());
				fields.put("label", getLabel(i));
				String desc = getAnnotationPropertyValue(i,
						factory.getRDFSComment());
				if (desc != null) {
					fields.put("desc", desc);
				}
				ret.add(fields);
			}
		} catch (ParserException e) {
			Logger.error("Invalid query: " + query, e);
		}

		return ret;
	}

	public List<Map<String, Object>> getSearchTree() {

		if (Application.isDebug) {
			// reload every time while debugging and change ontology frequently
			loadOntology();
		}

		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();

		for (OWLClass c : ontology.getClassesInSignature()) {
			Set<OWLAnnotation> anns = c.getAnnotations(ontology,
					annUISearchTreeTopLevel);
			for (OWLAnnotation a : anns) {
				if (a.getValue() instanceof OWLLiteral) {
					boolean val = ((OWLLiteral) a.getValue()).parseBoolean();
					if (val) {
						Map<String, Object> fields = getSearchTree(c);
						ret.add(fields);
						break; // annotation loop
					}
				}
			}
		}

		sort(ret);
		return ret;

	}

	public Map<String, Object> getSearchTree(OWLClass c) {

		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("iri", c.getIRI().toString());
		fields.put("label", getLabel(c));
		String desc = getAnnotationPropertyValue(c, factory.getRDFSComment());
		if (desc != null) {
			fields.put("desc", desc);
		}
		fields.put("order", getSortOrder(c));

		List<Map<String, Object>> children = getSearchTreeChildren(c);
		if (children.size() > 0) {
			fields.put("children", children);
		}

		return fields;
	}

	public List<Map<String, Object>> getSearchTreeChildren(OWLClass c) {
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();

		Set<OWLClassExpression> subclasses = c.getSubClasses(ontology);

		if (subclasses.size() > 0) {
			for (OWLClassExpression oce : subclasses) {
				if (oce instanceof OWLClass) {
					ret.add(getSearchTree((OWLClass) oce));
				}
			}
		} else {
			// at leaf level, look for individuals
			for (OWLIndividual i : c.getIndividuals(ontology)) {
				if (i.isNamed()) {
					OWLNamedIndividual ni = i.asOWLNamedIndividual();
					Map<String, Object> fields = new HashMap<String, Object>();
					fields.put("iri", ni.getIRI().toString());
					fields.put("label", getLabel(ni));
					String desc = getAnnotationPropertyValue(ni,
							factory.getRDFSComment());
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
		Collections.sort(toSort, new Comparator<Map<String, Object>>() {

			@Override
			public int compare(Map<String, Object> left,
					Map<String, Object> right) {
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

	public String getAnnotationPropertyValue(OWLEntity e,
			OWLAnnotationProperty prop) {
		Set<OWLAnnotation> anns = e.getAnnotations(ontology, prop);
		// take first one
		for (OWLAnnotation a : anns) {
			if (a.getValue() instanceof OWLLiteral) {
				return ((OWLLiteral) a.getValue()).getLiteral();
			}
		}
		return null;
	}

	public HashMap<String, String> getModellingInfrastructureBriefDetails(
			String name) {
		OWLClass modelinginfrastructure_class = this.factory.getOWLClass(IRI
				.create(this.ontology.getOntologyID().getOntologyIRI() + "#"
						+ "ModelingInfrastructure"));
		HashMap<String, HashMap<String, String>> modelinginfra_data = new HashMap<String, HashMap<String, String>>();
		for (OWLIndividual I : modelinginfrastructure_class
				.getIndividuals(ontology)) {
			OWLEntity entity = (OWLEntity) I;
			HashMap<String, String> temp = new HashMap<String, String>();
			for (OWLAnnotation annotation : entity.getAnnotations(ontology)) {
				if (annotation.getProperty().toString().contains("label")) {
					temp.put(
							"label",
							annotation.getValue().toString().split("\"\\^\\^")[0]
									.substring(1));
				} else if (annotation.getProperty().toString()
						.contains("shortName")) {
					temp.put("shortName", annotation.getValue().toString()
							.split("\"\\^\\^")[0].substring(1));
				} else if (annotation.getProperty().toString()
						.contains("comment")) {
					temp.put(
							"comment",
							annotation.getValue().toString().split("\"\\^\\^")[0]
									.substring(1));
				} else if (annotation.getProperty().toString()
						.contains("longName")) {
					temp.put("longName", annotation.getValue().toString()
							.split("\"\\^\\^")[0].substring(1));
				}
			}
			modelinginfra_data.put(temp.get("label"), temp);
		}
		System.out.println(modelinginfra_data);
		return modelinginfra_data.get(name);
	}

	public HashMap<String, ArrayList<HashMap<String, ArrayList<String>>>> getModellingInfraData(
			String name) {
		HashMap<String, ArrayList<HashMap<String, ArrayList<String>>>> data = new HashMap<String, ArrayList<HashMap<String, ArrayList<String>>>>();
		OWLIndividual individual = this.factory.getOWLNamedIndividual(IRI
				.create(this.ontology.getOntologyID().getOntologyIRI()
						.toString()
						+ "#" + name));
		Map<OWLDataPropertyExpression, Set<OWLLiteral>> dataProps = individual
				.getDataPropertyValues(ontology);
		for (Map.Entry<OWLDataPropertyExpression, Set<OWLLiteral>> e : dataProps
				.entrySet()) {
			OWLDataPropertyExpression data_prop = e.getKey();
			String property = e.getKey().toString().split("#")[1];
			property = property.substring(0, property.length() - 1);
			ArrayList<String> values = new ArrayList<String>();
			HashMap<String, ArrayList<String>> prop_and_values = new HashMap<String, ArrayList<String>>();
			for (OWLLiteral o : e.getValue()) {
				String temp_str = o.toString().split("\"\\^\\^")[0];
				temp_str = temp_str.substring(1, temp_str.length());
				values.add(temp_str);
			}
			prop_and_values.put(property, values);
			OWLEntity entity = (OWLEntity) data_prop;
			String uiTab_value = null;
			for (OWLAnnotation annotation : entity.getAnnotations(ontology)) {
				if (annotation.getProperty().toString().contains("uiTab")) {
					uiTab_value = annotation.getValue().toString()
							.split("\"\\^\\^")[0];
					uiTab_value = uiTab_value
							.substring(1, uiTab_value.length());
				}
			}
			ArrayList<HashMap<String, ArrayList<String>>> temp;
			if (data.containsKey(uiTab_value)) {
				temp = data.get(uiTab_value);
				temp.add(prop_and_values);
			} else {
				temp = new ArrayList<HashMap<String, ArrayList<String>>>();
				temp.add(prop_and_values);
			}
			data.put(uiTab_value, temp);
		}
		return data;
	}
}