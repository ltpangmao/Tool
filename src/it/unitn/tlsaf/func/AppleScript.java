package it.unitn.tlsaf.func;

import it.unitn.tlsaf.ds.InfoEnum;
import it.unitn.tlsaf.ds.RequirementElement;
import it.unitn.tlsaf.ds.RequirementLink;
import it.unitn.tlsaf.ds.SecurityGoal;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class AppleScript {

	public static String drawRequirementLink(RequirementLink rl) throws ScriptException {
		// InfoEnum.Layer.BUSINESS.name(), "51689", "51699",

		// String layer = InfoEnum.Layer.BUSINESS.name();
		// String target_id = "51699";
		// String source_id = "51689";

		String layer = rl.getSource().getLayer();
		String target_id = rl.getTarget().getId();
		String source_id = rl.getSource().getId();

		String head_type = "";
		String stroke_pattern = "";
		String label = "";

		/*
		 * Customize each parameter according to specific notation.
		 */
		// and_refine link1
		if (rl.getType().equals(InfoEnum.RequirementLinkType.AND_REFINE_ARROW.name())
				& rl.getSource().getType().equals(InfoEnum.RequirementElementType.MIDDLE_POINT.name())) {
			head_type = "SharpArrow";
			stroke_pattern = "0";
			label = "";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.AND_REFINE.name())) {
			head_type = "";
			stroke_pattern = "0";
			label = "";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.REFINE.name())) {
			head_type = "SharpArrow";
			stroke_pattern = "0";
			label = "";
		} else if (rl.getTarget().equals(InfoEnum.RequirementLinkType.OPERATIONALIZE.name())) {
			head_type = "StickArrow";
			stroke_pattern = "0";
			label = "";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.SUPPORT.name())) {
			head_type = "SharpArrow";
			stroke_pattern = "1";
			label = "";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.PREFERRED_TO.name())) {
			head_type = "DoubleArrow";
			stroke_pattern = "0";
			label = "";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.MAKE.name())) {
			head_type = "StickArrow";
			stroke_pattern = "0";
			label = "make new label at end of labels of result_line with properties {text:{size:14, alignment:center, text:\"make\"}, draws shadow:false, draws stroke:false}\n";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.HELP.name())) {
			head_type = "StickArrow";
			stroke_pattern = "0";
			label = "make new label at end of labels of result_line with properties {text:{size:14, alignment:center, text:\"help\"}, draws shadow:false, draws stroke:false}\n";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.HURT.name())) {
			head_type = "StickArrow";
			stroke_pattern = "0";
			label = "make new label at end of labels of result_line with properties {text:{size:14, alignment:center, text:\"hurt\"}, draws shadow:false, draws stroke:false}\n";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.BREAK.name())) {
			head_type = "StickArrow";
			stroke_pattern = "0";
			label = "make new label at end of labels of result_line with properties {text:{size:14, alignment:center, text:\"break\"}, draws shadow:false, draws stroke:false}\n";
		} else {

		}

		return drawRequirementLink(layer, target_id, source_id, head_type, stroke_pattern, label);

	}

	/**
	 * @param layer
	 * @param target_id
	 * @param source_id
	 * @param head_type
	 * @param stroke_pattern
	 * @param label
	 * @return id
	 * @throws ScriptException
	 */
	public static String drawRequirementLink(String layer, String target_id, String source_id, String head_type,
			String stroke_pattern, String label) throws ScriptException {
		// harden code here instead of passing through files
		String script = "global target_canvas_name\n";
		script += "set target_canvas_name to \"" + layer + "\"\n" + "global target_canvas\n"
				+ "set target_canvas to missing value\n" + "global target_id\n" + "set target_id to " + target_id
				+ "\n" + "global source_id\n" + "set source_id to " + source_id + "\n" + "global target_elem\n"
				+ "set target_elem to missing value\n" + "global source_elem\n" + "set source_elem to missing value\n"
				+ "tell application id \"OGfl\"\n" + "set canvas_list to canvases of front window\n"
				+ "repeat with canvas_temp in canvas_list\n"
				+ "if (name of canvas_temp is equal to target_canvas_name) then\n"
				+ "set target_canvas to canvas_temp\n" + "set shape_list to graphics of canvas_temp\n"
				+ "repeat with shape_temp in shape_list\n" + "if (id of shape_temp is equal to source_id) then\n"
				+ "set source_elem to shape_temp\n" + "end if\n"
				+ "	if (id of shape_temp is equal to target_id) then\n" + "set target_elem to shape_temp\n"
				+ "end if\n" + "end repeat\n" + "end if\n" + "end repeat\n" + "tell target_canvas\n"
				+ "set result_line to (connect source_elem to target_elem with properties {head type:\"" + head_type
				+ "\", stroke pattern:" + stroke_pattern + "})\n" + label + "id of result_line\n" + "end tell\n"
				+ "end tell\n";

		// System.out.println(script);
		// return null;
		ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("AppleScript");
		String id = String.valueOf((long) scriptEngine.eval(script));
		System.out.println(id);
		return id;
	}

	public static String drawRequirementElement(RequirementElement target, String reference_id, String direction)
			throws ScriptException {
		// customized parameters
		String layer = target.getLayer();
		String offset = "";
		if (direction.equals("up")) {
			offset = "{0, -200}";
		} else if (direction.equals("down")) {
			offset = "{0, +200}";
		} else if (direction.equals("left")) {
			offset = "{-200, 0}";
		} else if (direction.equals("right")) {
			offset = "{+200, 0}";
		} else {
			offset = "{+100,+100}";
		}
		String shape = InfoEnum.reverse_req_elem_type_map.get(target.getType());
		String size = "{150,90}";
		String corner_radius = "0";
		String name = target.getName();
		if (target.getType().equals(InfoEnum.RequirementElementType.ACTOR.name())) {
			size = "{100,100}";
		} else if (target.getType().equals(InfoEnum.RequirementElementType.MIDDLE_POINT.name())) {
			size = "{15,15.1}";
		} else if (target.getType().equals(InfoEnum.RequirementElementType.DOMAIN_ASSUMPTION.name())) {
			corner_radius = "5";
		} else if (target.getType().equals(InfoEnum.RequirementElementType.SECURITY_GOAL.name())) {
			name = "(S)\n" + name;
		} else if (target.getType().equals(InfoEnum.RequirementElementType.SECURITY_MECHANISM.name())) {
			name = "(S)\n" + name;
		}

		return drawRequirementElement(reference_id, layer, offset, shape, size, corner_radius, name);
	}

	/**
	 * @param reference_id
	 * @param layer
	 * @param offset
	 * @param shape
	 * @param size
	 * @param corner_radius
	 * @param name
	 * @return
	 * @throws ScriptException
	 */
	public static String drawRequirementElement(String reference_id, String layer, String offset, String shape,
			String size, String corner_radius, String name) throws ScriptException {
		// harden code here instead of passing through files
		String script = "global target_canvas_name\n";
		script += "set target_canvas_name to \"" + layer + "\"\n" + "global target_canvas\n"
				+ "set target_canvas to missing value\n" + "global reference_element_id\n"
				+ "set reference_element_id to " + reference_id + "\n" + "global reference_element\n"
				+ "set reference_element to missing value\n" + "global target_offset\n" + "set target_offset to "
				+ offset + "\n" + "global target_size\n" + "set target_size to " + size + "\n" + "global target_name\n"
				+ "set target_name to \"" + shape + "\"\n" + "global target_text\n" + "set target_text to \"" + name
				+ "\"\n";
		script += "tell application id \"OGfl\"\n"
				+ "set canvas_list to canvases of front window\n"
				+ "repeat with canvas_temp in canvas_list\n"
				+ "if (name of canvas_temp is equal to target_canvas_name) then\n"
				+ "set target_canvas to canvas_temp\n"
				+ "set shape_list to graphics of canvas_temp\n"
				+ "repeat with shape_temp in shape_list\n"
				+ "if (id of shape_temp is equal to reference_element_id) then\n"
				+ "set reference_element to shape_temp\n"
				+ "end if\n"
				+ "end repeat\n"
				+ "end if\n"
				+ "end repeat\n"
				+ "set reference_origin to origin of reference_element\n"
				+ "set target_origin to {(item 1 of target_offset) + (item 1 of reference_origin), (item 2 of target_offset) + (item 2 of reference_origin)}\n"
				+ "tell target_canvas\n"
				+ "set new_node to make new shape at end of graphics with properties {name:target_name, origin:target_origin, size:target_size, text:{size:14, alignment:center, text:target_text}, draws shadow:false, draws stroke:true, corner radius:"
				+ corner_radius + "}\n" + "id of new_node\n" + "end tell\n" + "end tell\n";
		// System.out.println(script);
		ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("AppleScript");
		String id = String.valueOf((long) scriptEngine.eval(script));
		System.out.println(id);
		// String id = extractID((String) scriptEngine.eval(script));
		return id;
	}

	/**
	 * @param layer
	 * @param shape
	 * @param size
	 * @param position
	 * @param corner_radius
	 * @param name
	 * @return
	 * @throws ScriptException
	 */
	public static String drawRequirementElement(String layer, String shape, String size, String position,
			String corner_radius, String name) throws ScriptException {
		// harden code here instead of passing through files
		String script = "global target_canvas_name\n";
		script += "set target_canvas_name to \"" + layer + "\"\n" + "global target_canvas\n"
				+ "set target_canvas to missing value\n" + "global target_size\n" + "set target_size to " + size + "\n"
				+ "global target_name\n" + "set target_name to \"" + shape + "\"\n" + "global target_text\n"
				+ "set target_text to \"" + name + "\"\n";
		script += "tell application id \"OGfl\"\n"
				+ "set canvas_list to canvases of front window\n"
				+ "repeat with canvas_temp in canvas_list\n"
				+ "if (name of canvas_temp is equal to target_canvas_name) then\n"
				+ "set target_canvas to canvas_temp\n"
				+ "end if\n"
				+ "end repeat\n"
				+ "set target_origin to "
				+ position
				+ "\n"
				+ "tell target_canvas\n"
				+ "set new_node to make new shape at end of graphics with properties {name:target_name, origin:target_origin, size:target_size, text:{size:14, alignment:center, text:target_text}, draws shadow:false, draws stroke:true, corner radius:"
				+ corner_radius + "}\n" + "id of new_node\n" + "end tell\n" + "end tell\n";
		//		 System.out.println(script);
		ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("AppleScript");
		String id = String.valueOf((long) scriptEngine.eval(script));
		System.out.println(id);
		// String id = extractID((String) scriptEngine.eval(script));
		return id;
	}

	public static void changeAttributeOfElement(String layer, String id, String command) throws ScriptException {

		// harden code here instead of passing through files
		String script = "global target_canvas_name\n";
		script += "set target_canvas_name to \"" + layer + "\"\n" + "global target_canvas\n"
				+ "set target_canvas to missing value\n" + "global target_element_id\n" + "set target_element_id to "
				+ id + "\n" + "global target_element\n" + "set target_element to missing value\n";
		script += "tell application id \"OGfl\"\n" + "set canvas_list to canvases of front window\n"
				+ "repeat with canvas_temp in canvas_list\n"
				+ "if (name of canvas_temp is equal to target_canvas_name) then\n"
				+ "set target_canvas to canvas_temp\n" + "set shape_list to graphics of canvas_temp\n"
				+ "repeat with shape_temp in shape_list\n"
				+ "if (id of shape_temp is equal to target_element_id) then\n" + "set target_element to shape_temp\n"
				+ "end if\n" + "end repeat\n" + "end if\n" + "end repeat\n" + "tell target_canvas\n" + command + "\n"
				+ "end tell\n" + "end tell\n";
		// System.out.println(script);
		ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("AppleScript");
		scriptEngine.eval(script);

	}

}
