package it.unitn.tlsaf.func;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unitn.tlsaf.ds.InfoEnum;
import it.unitn.tlsaf.ds.RequirementElement;
import it.unitn.tlsaf.ds.RequirementLink;
import it.unitn.tlsaf.ds.SecurityGoal;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.sound.midi.MidiDevice.Info;

/**
 * This class is designed to directly interact with OmniGraffle with AppleScript
 * The scripts are hardened into java code in order to customize their parameters
 * @author litong30
 * 
 */
public class AppleScript {

	/**
	 * Draw a single requirement link object onto the canvas.
	 * @param rl
	 * @param layer_condition
	 * @return
	 * @throws ScriptException
	 */
	public static String drawRequirementLink(RequirementLink rl, int layer_condition) throws ScriptException {
		String canvas_layer="";
		String target_layer="";
		
		if(layer_condition == InfoEnum.CROSS_LAYERS){
			canvas_layer = "none";
			target_layer = rl.getSource().getLayer();
		} else if (layer_condition == InfoEnum.SINGLE_LAYER){
			canvas_layer = rl.getSource().getLayer();
			target_layer = rl.getSource().getLayer();
		} else{
			CommandPanel.logger.severe("Cross-layer option error!");;
		}
		
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
			label = "none";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.AND_REFINE.name())) {
			head_type = "";
			stroke_pattern = "0";
			label = "none";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.REFINE.name())) {
			head_type = "SharpArrow";
			stroke_pattern = "0";
			label = "none";
		} else if (rl.getTarget().equals(InfoEnum.RequirementLinkType.OPERATIONALIZE.name())) {
			head_type = "StickArrow";
			stroke_pattern = "0";
			label = "none";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.SUPPORT.name())) {
			head_type = "SharpArrow";
			stroke_pattern = "1";
			label = "none";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.PREFERRED_TO.name())) {
			head_type = "DoubleArrow";
			stroke_pattern = "0";
			label = "none";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.MAKE.name())) {
			head_type = "StickArrow";
			stroke_pattern = "0";
			label = "Make";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.HELP.name())) {
			head_type = "StickArrow";
			stroke_pattern = "0";
			label = "Help";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.HURT.name())) {
			head_type = "StickArrow";
			stroke_pattern = "0";
			label = "Hurt";
		} else if (rl.getType().equals(InfoEnum.RequirementLinkType.BREAK.name())) {
			head_type = "StickArrow";
			stroke_pattern = "0";
			label = "Break";
		} else {
		}

		// here the layer_value = canvas_layer
		return drawArbitraryRequirementLink(InfoEnum.REQ_TARGET_CANVAS, canvas_layer, target_id, source_id, head_type,
				stroke_pattern, label, target_layer);
	}
	
	
	public static String drawESGRefinementLink(RequirementLink rl) throws ScriptException {
		
		String initial_layer = "none";
		String layer_value = "All";
		String target_id = rl.getTarget().getId();
		String source_id = rl.getSource().getId();

		String head_type = "StickArrow";
		String stroke_pattern = "0";
		String label = null;
		
		if(rl.refine_type.equals(InfoEnum.RefineType.ATTRIBUTE.name())){
			label ="S";
		}else if (rl.refine_type.equals(InfoEnum.RefineType.ASSET.name())){
			label ="A";
		}else if (rl.refine_type.equals(InfoEnum.RefineType.INTERVAL.name())){
			label = "I";
		}else {
			CommandPanel.logger.warning("Refinement type error!");
		}
		
		return drawArbitraryRequirementLink(InfoEnum.esg_canvas.get(rl.getSource().getLayer()), initial_layer, target_id, source_id, head_type, stroke_pattern, label, layer_value);
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
	
	public static String drawArbitraryRequirementLink(String canvas, String layer, String target_id, String source_id,
			String head_type, String stroke_pattern, String label, String link_layer) throws ScriptException {
		// TODO: for simplicity 
		layer = "none";
		
		//set parameters & call the exact method
		String script = "";
		script += "set target_canvas_name to \""+canvas+"\"\n"
				+ "set target_layer_name to \""+layer+"\"\n"
				+ "set target_id to "+ target_id +"\n"
				+ "set source_id to "+ source_id+"\n"
				+ "set head_type to \""+head_type+"\"\n"
				+ "set stroke_pattern to " + stroke_pattern +"\n"
				+ "set label_text to \""+ label +"\"\n"
				+ "set link_layer to \""+ link_layer +"\"\n"
				+ "draw_link(target_canvas_name, target_layer_name, target_id, source_id, head_type, stroke_pattern, label_text, link_layer)\n";
		
		//import the method codes
		String method_file = "applescript/drawing_methods.applescript";
		try {
			script = loadMethods(script, method_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println(script);

		//execute methods
		//TODO: need to decide throw exception or handle it here?
		ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("AppleScript");
		String id = String.valueOf((long) scriptEngine.eval(script));
		//System.out.println(id);
		return id;
	}

	
	public static String drawRequirementElement(RequirementElement target, RequirementElement reference, String direction)
			throws ScriptException {
		// customized parameters
		String layer = target.getLayer();

		double x = 0, y = 0;
		String position = "";
		
		if (direction.equals("up")) {
			x = reference.origin_x;
			y = reference.origin_y - 200;
		} else if (direction.equals("down")) {
			x = reference.origin_x;
			y = reference.origin_y + 200;
		} else if (direction.equals("left")) {
			x = reference.origin_x - 200;
			y = reference.origin_y;
		} else if (direction.equals("right")) {
			x = reference.origin_x + 200;
			y = reference.origin_y;
		} else {
		}
		position = "{"+ x +","+ y +"}";
		// assign the position information to the target element
		target.origin_x=x;
		target.origin_y=y;
		
		String shape = InfoEnum.reverse_req_elem_type_map.get(target.getType());
		String corner_radius = "0";
		String name = target.getName();
		
		if (target.getType().equals(InfoEnum.RequirementElementType.DOMAIN_ASSUMPTION.name())) {
			corner_radius = "5";
		} else if (target.getType().equals(InfoEnum.RequirementElementType.SECURITY_GOAL.name())) {
			name = "(S)\n" + name;
		} else if (target.getType().equals(InfoEnum.RequirementElementType.SECURITY_MECHANISM.name())) {
			name = "(S)\n" + name;
		}
		
		int size_type = 0;
		if(target.getType().equals(InfoEnum.RequirementElementType.ACTOR.name())){
			size_type = InfoEnum.ACTOR_SIZE;
		} else if (target.getType().equals(InfoEnum.RequirementElementType.MIDDLE_POINT.name())){
			size_type = InfoEnum.POINT_SIZE;
		}

//		return drawReferredRequirementElement(reference_id, InfoEnum.REQ_TARGET_CANVAS, layer, offset, shape, size, corner_radius, name);
		return drawArbitraryRequirementElement(InfoEnum.REQ_TARGET_CANVAS, layer, shape, size_type, position, corner_radius, name);
				
	}


	


	/**
	 * Draw an requirement element according to another referred element
	 * @param reference_id
	 * @param canvas
	 * @param layer
	 * @param offset
	 * @param shape
	 * @param size
	 * @param corner_radius
	 * @param name
	 * @return
	 * @throws ScriptException
	 */
	@Deprecated
	private static String drawReferredRequirementElement(String reference_id, String canvas, String layer, String offset, String shape,
			String size, String corner_radius, String name) throws ScriptException {
		//set parameters & call the exact method
		String script = "";
		script += "set reference_element_id to "+reference_id+"\n"
				+ "set target_canvas_name to \""+canvas+"\"\n"
				+ "set target_layer_name to \""+layer+"\"\n"
				+ "set target_size to "+ size +"\n"
				+ "set target_name to \""+shape+"\"\n"
				+ "set target_text to \""+name+"\"\n"
				+ "set corner_ridius to " + corner_radius + "\n"
				+ "set target_offset to " + offset +"\n"
				+ "draw_referred_element(reference_element_id, target_canvas_name, target_layer_name, target_size, target_name, target_text, corner_ridius, target_offset)\n";
				
		//import the method codes
		String method_file="applescript/drawing_methods.applescript";
		try {
			script = loadMethods(script, method_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(script);

		//execute methods
		//TODO: need to decide throw exception or handle it here?
		ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("AppleScript");
		String id = String.valueOf((long) scriptEngine.eval(script));
		//System.out.println(id);
		return id;
	}

	
	/**
	 * Draw a requirement element to an arbitrary position 
	 * @param canvas
	 * @param layer
	 * @param shape
	 * @param size
	 * @param position
	 * @param corner_radius
	 * @param name
	 * @param size 
	 * @return
	 * @throws ScriptException
	 */
	public static String drawArbitraryRequirementElement(String canvas, String layer, String shape, int size_type, String position,
			String corner_radius, String name) throws ScriptException {
		//pre-calculate size according to the length of name;
		String size="";
		if (size_type == InfoEnum.NORMAL_SIZE) {
			size = approximateSize(name);
		} else if (size_type == InfoEnum.POINT_SIZE) {
			size = "{15,15.1}";
		} else if (size_type == InfoEnum.ACTOR_SIZE){
			size = "{100,100}";
		} else{
			CommandPanel.logger.severe("Draw elements size type error!");
		}
		
		
		//set parameters & call the exact method
		String script = "";
		script += "set target_canvas_name to \""+canvas+"\"\n"
				+ "set target_layer_name to \""+layer+"\"\n"
				+ "set target_size to "+ size +"\n"
				+ "set target_name to \"" + shape + "\"\n"
				+ "set target_text to \"" + name + "\"\n"
				+ "set target_origin to " + position +"\n"
				+ "set corner_ridius to " + corner_radius +"\n"
				+ "draw_isolated_element(target_canvas_name, target_layer_name, target_size, target_name, target_text, target_origin, corner_ridius)\n";
				
		//import the method codes
		String method_file="applescript/drawing_methods.applescript";
		try {
			script = loadMethods(script,method_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(script);

		//execute methods
		ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("AppleScript");
		String id = String.valueOf((long) scriptEngine.eval(script));
		//System.out.println(id);
		return id;
	}
	

	public static void changeAttributeOfElement(String canvas, String layer, String target_id, String thickness, String color, String layer_value) throws ScriptException {
		//set parameters & call the exact method
		String script = "";
		script += "set target_canvas_name to \""+canvas+"\"\n"
				+ "set target_layer_name to \""+layer+"\"\n"
				+ "set target_id to " + target_id +"\n"
				+ "set thick_value to " + thickness + "\n"
				+ "set color_value to \"" + color + "\"\n"
				+ "set layer_value to \"" + layer_value + "\"\n"
				+ "change_element_attribute(target_canvas_name, target_layer_name, target_id, thick_value, color_value, layer_value)\n";
		//import the method codes
		String method_file = "applescript/drawing_methods.applescript";
		try {
			script = loadMethods(script, method_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//execute methods
		ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("AppleScript");
		scriptEngine.eval(script);
	}
	
	public static void changeAttributeOfLink(String canvas, String layer, String target_id, String thickness, String color, String layer_value) throws ScriptException {
		//set parameters & call the exact method
		String script = "";
		script += "set target_canvas_name to \""+canvas+"\"\n"
				+ "set target_layer_name to \""+layer+"\"\n"
				+ "set link_id to " + target_id +"\n"
				+ "set thick_value to " + thickness + "\n"
				+ "set color_value to \"" + color + "\"\n"
				+ "set layer_value to \"" + layer_value + "\"\n"
				
				+ "change_link_attribute(target_canvas_name, target_layer_name, link_id, thick_value, color_value, layer_value)\n";
		//import the method codes
		String method_file = "applescript/drawing_methods.applescript";
		try {
			script = loadMethods(script, method_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//execute methods
		ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("AppleScript");
		scriptEngine.eval(script);
	}
	
	
	public static void addUserData(String canvas, String layer, String target_id, String owner) throws ScriptException {
		//set parameters & call the exact method
		String script = "";
		script += "set target_canvas_name to \""+canvas+"\"\n"
				+ "set target_layer_name to \""+layer+"\"\n"
				+ "set owner to \"" + owner + "\"\n"
				+ "set target_id to " + target_id +"\n"
				+ "add_user_data(target_canvas_name, target_layer_name, target_id, owner)\n";
						
		//import the method codes
		String method_file = "applescript/drawing_methods.applescript";
		try {
			script = loadMethods(script, method_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(script);

		//execute methods
		ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("AppleScript");
		//				String result = 
		scriptEngine.eval(script);
		//System.out.println(result);
	}
	
	
	public static ArrayList<Long> getSelectedGraph() throws ScriptException {
		//set parameters & call the exact method
		String script = "get_selected_graph()\n";
						
		//import the method codes
		String method_file = "applescript/drawing_methods.applescript";
		try {
			script = loadMethods(script, method_file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(script);

		//execute methods
		ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("AppleScript");
		ArrayList<Long> result = (ArrayList<Long>) scriptEngine.eval(script);
		return result;
		//System.out.println(result);
	}
	
	/**
	 * @param script
	 * @return
	 * @throws IOException
	 */
	private static String loadMethods(String script, String file) throws IOException {
		String result = readFile(file, Charset.defaultCharset());
		List<String> elements = Arrays.asList(result.split("\n"));
		boolean methods = false;
		for (String s : elements) {
			//find the start point
			if (!methods && s.indexOf("methods") >= 0) {
				methods = true;
			}
			// then import text from the start point
			if (methods) {
				script += s + "\n";
			}
		}
		return script;
	}

	private static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
	
	
	private static String approximateSize(String target) {
		// TODO Auto-generated method stub
		int text_length = target.length();
		int graph_width = 0;
		int graph_height = 0;
		if(text_length<=60){
			graph_width = 180;
			graph_height = 110;	
		}
		else{
			graph_width = 180+(text_length-60);
			graph_height = 110+(text_length-60);
		}
		
		return "{"+graph_width+","+graph_height+"}";
	}
}
