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
		BUSINESS, APPLICATION, PHYSICAL
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
}