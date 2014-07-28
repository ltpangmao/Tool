package it.unitn.tlsaf.ds;

import java.util.HashMap;
import java.util.Map;

/**
 * @author litong30 enumerate all types of elements and links for different
 *         models, such as goal model, business process model
 */
public class InfoEnum {
	// private static final String[] REQUIREMENT_ELEMENT_TYPE = { "positive",
	// "negative",
	// "unknown" };
	
	//Object parameters
	public static final int ALL_MODELS= 0;
	public static final int SELECTED_MODELS= 1;
	//Visualization parameters
	public static final int GRAPHVIZ= 0;
	public static final int CANVAS= 1;
	//View parameters
	public static final int INITIAL_VIEW= 0;
	public static final int HIGHLIGHT_VIEW= 1;
	public static final int SIMPLIFIED_VIEW= 2;
	
	//Draw elements size parameter
	public static final int NORMAL_SIZE= 0;
	public static final int POINT_SIZE= 1;
	public static final int ACTOR_SIZE= 2;
		
	
	
	//Draw Link parameters
	public static final int CROSS_LAYERS= 0;
	public static final int SINGLE_LAYER= 1;
	
	//global variable for configuring the name of canvas of requirements model.
	public static final String REQ_TARGET_CANVAS="Model";
//	public static final String ESG_BP_CANVAS="Business SG";
	
	public static final Map<String, String> esg_canvas = new HashMap<String, String>();
	static {
		esg_canvas.put(InfoEnum.Layer.BUSINESS.name(), "Business SG");
		esg_canvas.put(InfoEnum.Layer.APPLICATION.name(), "Application SG");
		esg_canvas.put(InfoEnum.Layer.PHYSICAL.name(), "Physical SG");
	}
	
	// determine type of scanned shape
	public static final Map<String, String> req_elem_type_map = new HashMap<String, String>();
	static {
		req_elem_type_map.put("Circle", InfoEnum.RequirementElementType.GOAL.name());
		req_elem_type_map.put("Cloud", InfoEnum.RequirementElementType.SOFTGOAL.name());
		req_elem_type_map.put("Hexagon", InfoEnum.RequirementElementType.TASK.name());
		req_elem_type_map.put("Diamond", InfoEnum.RequirementElementType.QUALITY_CONSTRAINT.name());
		req_elem_type_map.put("Rectangle", InfoEnum.RequirementElementType.DOMAIN_ASSUMPTION.name());
	}

	// determine type of scanned shape
	public static final Map<String, String> reverse_req_elem_type_map = new HashMap<String, String>();
	static {
		reverse_req_elem_type_map.put(InfoEnum.RequirementElementType.GOAL.name(), "Circle");
		reverse_req_elem_type_map.put(InfoEnum.RequirementElementType.ACTOR.name(), "Circle");
		reverse_req_elem_type_map.put(InfoEnum.RequirementElementType.MIDDLE_POINT.name(), "Circle");
		reverse_req_elem_type_map.put(InfoEnum.RequirementElementType.SOFTGOAL.name(), "Cloud");
		reverse_req_elem_type_map.put(InfoEnum.RequirementElementType.SECURITY_GOAL.name(), "Cloud");
		reverse_req_elem_type_map.put(InfoEnum.RequirementElementType.TASK.name(), "Hexagon");
		reverse_req_elem_type_map.put(InfoEnum.RequirementElementType.SECURITY_MECHANISM.name(), "Hexagon");
		reverse_req_elem_type_map.put(InfoEnum.RequirementElementType.QUALITY_CONSTRAINT.name(), "Diamond");
		reverse_req_elem_type_map.put(InfoEnum.RequirementElementType.DOMAIN_ASSUMPTION.name(), "Rectangle");
	}

	
	
	// determine layer of each security mechanism, especially the one that is related to 
	public static final Map<String, String> security_mechanisms = new HashMap<String, String>();
	static {
		security_mechanisms.put("cryptographic_control", InfoEnum.Layer.BUSINESS.name());
		security_mechanisms.put("access_control", InfoEnum.Layer.BUSINESS.name());
		security_mechanisms.put("auditing", InfoEnum.Layer.BUSINESS.name());
		security_mechanisms.put("backup", InfoEnum.Layer.BUSINESS.name());

		security_mechanisms.put("full_view_with_errors", InfoEnum.Layer.APPLICATION.name());
		security_mechanisms.put("limited_view", InfoEnum.Layer.APPLICATION.name());
		security_mechanisms.put("secure_pipe", InfoEnum.Layer.APPLICATION.name());
		security_mechanisms.put("encrypted_storage", InfoEnum.Layer.APPLICATION.name());
		security_mechanisms.put("secure_access_layer", InfoEnum.Layer.APPLICATION.name());
		security_mechanisms.put("data_backup", InfoEnum.Layer.APPLICATION.name());
		security_mechanisms.put("server_sand_box", InfoEnum.Layer.APPLICATION.name());
		security_mechanisms.put("input_guard", InfoEnum.Layer.APPLICATION.name());
		security_mechanisms.put("firewall", InfoEnum.Layer.APPLICATION.name());
		security_mechanisms.put("replicated_system", InfoEnum.Layer.APPLICATION.name());
		security_mechanisms.put("load_balancer", InfoEnum.Layer.APPLICATION.name());

		security_mechanisms.put("secure_office", InfoEnum.Layer.PHYSICAL.name());
		security_mechanisms.put("physical_entry_control", InfoEnum.Layer.PHYSICAL.name());
		security_mechanisms.put("monitor", InfoEnum.Layer.PHYSICAL.name());
		security_mechanisms.put("anti_tamper_protection", InfoEnum.Layer.PHYSICAL.name());
		security_mechanisms.put("ups", InfoEnum.Layer.PHYSICAL.name());

	}
	
	// record questions for checking undecidable context
	public static final Map<String, String> pattern_context_question = new HashMap<String, String>();
	static {
		// ids-c1
		pattern_context_question.put("question(ids_c1q1)", "are there nodes communicating with each other using the Internet?");
		pattern_context_question.put("question(ids_c1q1)y", "communicate(system_node, internet).");
		pattern_context_question.put("question(ids_c1q1)n", "dis_communicate(system_node, internet).");
		// ids-c2
		pattern_context_question.put("question(ids_c2q1)", "are requests coming from a non-suspicious address harmful?");
		pattern_context_question.put("question(ids_c2q1)y", "harmful(request_from_non_suspicious_address).");
		pattern_context_question.put("question(ids_c2q1)n", "non_harmful(request_from_non_suspicious_address).");
		// ids-c3
		pattern_context_question.put("question(ids_c3q1)", "is there sufficient and appropriate information?");
		pattern_context_question.put("question(ids_c3q1)y", "sufficient(attack_information).");
		pattern_context_question.put("question(ids_c3q1)n", "not_sufficient(attack_information).");
		// audit-c1
		pattern_context_question.put("question(audit_c1q1)", "does the system handle sensitive data?");
		pattern_context_question.put("question(audit_c1q1)y", "handle(system, sensitive_data).");
		pattern_context_question.put("question(audit_c1q1)n", "not_handle(system, sensitive_data).");
	
	}
	

	// Types of elements and links
	public enum ModelCategory {
		REQUIREMENT, BUSINESS_PROCESS, SOFTWARE_ARCHITECTURE, DEPLOYMENT, ASSET, ACTOR
	}

	/*
	 * Types of elements and links, they should cover all types of links.
	 * Additional information of these elements, could be added in the remark
	 * part.
	 */
	public enum RequirementElementType {
		ACTOR, GOAL, TASK, SOFTGOAL, DOMAIN_ASSUMPTION, QUALITY_CONSTRAINT, SECURITY_GOAL, SECURITY_MECHANISM, ACTOR_BOUNDARY, MIDDLE_POINT, LABEL // syntax

	}

	public enum RequirementLinkType {
		REFINE, AND_REFINE, OPERATIONALIZE, PREFERRED_TO, DEPEND, TRUST, SUPPORT, MAKE, HELP, HURT, BREAK,
		USE, MAINTAIN, OWN, // used here as 
		AND_REFINE_ARROW//REDUNDANT_LINK

	}

	public enum ResourceElementType {
		RESOURCE
	}

	// Other enumerations
	public enum Layer {
		BUSINESS, APPLICATION, PHYSICAL, ALL
	}

	public enum AssetType {
		SERVICE, DATA, APPLICATION, HARDWARE
	}

	public enum SGImportance {
		Low, Medium, High
	}

	/*
	 * public enum SGSecurityAttribute { Security, Confidentiality,
	 * ServiceConfidentiality, DataConfidentiality, ApplicationConfidentiality,
	 * HardwareConfidentiality, Integrity, ServiceIntegrity, DataIntegrity,
	 * ApplicationIntegrity, HardwareIntegrity, Availability,
	 * ServiceAvailability, ApplicationAvailability, HardwareAvailability }
	 */

	//Except for normal, all other remarks deplete the element
	public enum ElementRemark {
		NORMAL, TRUSTUM, DEPENDUM, REFINEUM, BOUNDARY, SUPPORTUM, TOPSG, BESTPATH
	}

	public enum LinkRemark {
		NORMAL, BESTPATH, REDUNDANT//DEPLETED TRUST, DEPEND, REFINE ARROW, MAKE, HELP,
	}

	// Additional remark
	public enum RefineType {
		ATTRIBUTE, ASSET, INTERVAL
	}
	
	/*
	 * enumerate all commands that can be done by the tool
	 */
	public enum Commands {
		IMP_SELECTION, IMP_FILE, REF_ALL_ONE_STEP, REF_ALL_EXHAUSTIVE
	}
}
