package it.unitn.tlsaf.func;

import it.unitn.tlsaf.ds.Actor;
import it.unitn.tlsaf.ds.ActorAssociationGraph;
import it.unitn.tlsaf.ds.Element;
import it.unitn.tlsaf.ds.RequirementGraph;
import it.unitn.tlsaf.ds.InfoEnum;
import it.unitn.tlsaf.ds.Link;
import it.unitn.tlsaf.ds.RequirementElement;
import it.unitn.tlsaf.ds.RequirementLink;
import it.unitn.tlsaf.ds.SecurityGoal;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Processing logic of inference rules
 * 
 * @author litong30
 */
public class Inference {

	public static void importReqModel(ModelSet cp, boolean from_canvas) throws IOException, ScriptException {
		//this import allows us to incrementally add model, but not delete/overwrite model, which should be done later
		if(from_canvas){
			String script_path = "applescript/import_info.applescript";
			execAppleScript(script_path);
		}
		//TODO: this should be made flexible later
		String result = readFile("applescript/graph_info.txt", Charset.defaultCharset());
		
		//pre-processing the results to classify information to different layers 
		String bus_result="";
		String app_result="";
		String phy_result="";
		List<String> elements = Arrays.asList(result.split("\n"));
		for(String s : elements){
			if(s.indexOf(InfoEnum.Layer.BUSINESS.name())>=0){
				bus_result+=s+"\n";
			}
			else if(s.indexOf(InfoEnum.Layer.APPLICATION.name())>=0){
				app_result+=s+"\n";
			}
			else if(s.indexOf(InfoEnum.Layer.PHYSICAL.name())>=0){
				phy_result+=s+"\n";
			}
		}
		
		cp.req_bus_model.importGraphInfo(bus_result);
		cp.req_app_model.importGraphInfo(app_result);
		cp.req_phy_model.importGraphInfo(phy_result);
	}


	/*
	 * public inference functions
	 */
	public static void importActorModel(ActorAssociationGraph actor_model, Boolean from_canvas) throws IOException, ScriptException {
		if(from_canvas){
			String script_path = "applescript/import_info.applescript";
			execAppleScript(script_path);
		}
		
		String result = readFile("applescript/graph_info.txt", Charset.defaultCharset());
		actor_model.importGraphInfo(result);
		CommandPanel.logger.info(actor_model.generateFormalExpression());
	}

	private static void importReqModel(RequirementGraph req_model, boolean from_canvas) throws IOException, ScriptException {
		if(from_canvas){
			String script_path = "applescript/import_info.applescript";
			execAppleScript(script_path);
		}

		String result = readFile("applescript/graph_info.txt", Charset.defaultCharset());
		req_model.importGraphInfo(result);

		//		for(Element re: req_model.getElements()){
		//			System.out.println(re.getFormalExpression());
		//		}
		//		for(Link l: req_model.getLinks()){
		//			System.out.println(l.getFormalExpression());
		//		}

		//		for(Element re: req_model.getElements()){
		//			if(re.getType().equals(InfoEnum.RequirementElementType.ACTOR.name())){
		//				System.out.println(re.getName());
		//				Actor a = (Actor) re;
		//				for(RequirementElement children: a.getOwnedElement()){
		//					if(!children.getName().equals("empty"))
		//						System.out.println(children.getName());
		//				}
		//				System.out.println();
		//			}
		//		}
	}

	/**
	 * This method calculate all possible refinements and represent them in
	 * another graph. Thus, the analysis result will not be shown in the graph.
	 * The output of this analysis should be put into a separate data structure,
	 * which is only designed to carry out the complete refinements analysis.
	 * 
	 * @param req_model
	 * @param actor_model 
	 * @param visualization 
	 * @throws IOException
	 * @throws ScriptException 
	 */
	public static void exhaustiveSecurityGoalRefineAnalysis(RequirementGraph req_model, ActorAssociationGraph actor_model, int visual_type, int scope) throws IOException, ScriptException {
		// first empty the potential security goal set.
		req_model.getSg_elem().clear();
		req_model.getSg_links().clear();
		
		String expression_file = req_model.generateFormalExpressionToFile(scope);
		String security_model_file = "dlv/models/security_model_"+req_model.getLayer().toLowerCase()+".dl ";
		
		String refine_rule = "";
		refine_rule = "dlv/dlv -silent -nofacts dlv/rules/refine_all.rule "
				+ "dlv/models/asset_model.dl "
				+ expression_file+" "+security_model_file;
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(refine_rule);

		BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = null;

		// parse inference results
		while ((line = input.readLine()) != null) {
			line = line.substring(1, line.length() - 1);
			String[] result = line.split(", ");

			//Assign all security goals with ordered numbers, which are just used as identifiers.
			int number = 1;
			for (String s : result) {
				// only consider related security goals
				if (s.startsWith("ex_and_refined_sec_goal")) {
					// parse facts
					s = s.replaceAll("ex_and_refined_sec_goal\\(", "");
					s = s.replaceAll("\\)", "");
					String[] sg = s.split(",");

					// create two security goals, and the and-refinement relation between them.
					SecurityGoal new_sg = req_model.findExhausiveSecurityGoalByAttributes(sg[0], sg[1], sg[2], sg[3]);
					SecurityGoal refined_sg = req_model.findExhausiveSecurityGoalByAttributes(sg[4], sg[5], sg[6], sg[7]);
					
					//add elements to the security goal graph
					if (new_sg == null) {
						new_sg = new SecurityGoal(sg[0], sg[1], sg[2], sg[3],
								InfoEnum.RequirementElementType.SECURITY_GOAL.name(), req_model.getLayer());
						new_sg.setId(String.valueOf(number));
						number++;
						req_model.getSg_elem().add(new_sg);
					}
					if (refined_sg == null) {
						refined_sg = new SecurityGoal(sg[4], sg[5], sg[6], sg[7],
								InfoEnum.RequirementElementType.SECURITY_GOAL.name(), req_model.getLayer());
						refined_sg.setId(String.valueOf(number));
						number++;
						req_model.getSg_elem().add(refined_sg);
					}
					
					// record related links
					RequirementLink new_and_refine = new RequirementLink(
							InfoEnum.RequirementLinkType.AND_REFINE.name(), new_sg, refined_sg);
					//determine the type of this refinement
					if (!sg[1].equals(sg[5])) {
						new_and_refine.refine_type = InfoEnum.RefineType.ATTRIBUTE.name();
					} else if (!sg[2].equals(sg[6])) {
						new_and_refine.refine_type = InfoEnum.RefineType.ASSET.name();
					} else if (!sg[3].equals(sg[7])) {
						new_and_refine.refine_type = InfoEnum.RefineType.INTERVAL.name();
					} else {
						CommandPanel.logger.log(Level.SEVERE, "Refine type is not set correctly");
					}
					//the refinement links should always be added, as there may be several elements that refine/be refined to one element.
					if(!req_model.getSg_links().contains(new_and_refine)){
						req_model.getSg_links().add(new_and_refine);
					}
					
					//add the refinement link to the target security goal
					refined_sg.and_refine_links.add(new_and_refine);
					//add refined security and links to its refinement
					new_sg.parent = refined_sg;
					new_sg.parent_link = new_and_refine;
				}
			}
			
			//visualize exhaustive refinements via Graphviz
			if(visual_type==InfoEnum.GRAPHVIZ){
				// graphviz can generate the three view separately
				visualizeGraph(req_model, actor_model, InfoEnum.GRAPHVIZ, InfoEnum.INITIAL_VIEW);
				visualizeGraph(req_model, actor_model, InfoEnum.GRAPHVIZ, InfoEnum.HIGHLIGHT_VIEW);
				visualizeGraph(req_model, actor_model, InfoEnum.GRAPHVIZ, InfoEnum.SIMPLIFIED_VIEW);
			}else if (visual_type == InfoEnum.CANVAS){
				// we only provide one view in the canvas
				// the highlight and simpliefied view are put together in one view
				//visualizeGraph(req_model, actor_model, InfoEnum.CANVAS, InfoEnum.INITIAL_VIEW);
				visualizeGraph(req_model, actor_model, InfoEnum.CANVAS, InfoEnum.HIGHLIGHT_VIEW);
			}
			else{
				CommandPanel.logger.warning("Visualization type error!");
			}

//			visualizeGraph(req_model, actor_model, 1);
//			visualizeGraph(req_model, actor_model, 2);
		}
	}


	/**
	 * @param req_model
	 * @param actor_model
	 * @param visualization
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws ScriptException
	 */
	private static void visualizeGraph(RequirementGraph req_model, ActorAssociationGraph actor_model, int type, int visualization)
			throws IOException, FileNotFoundException, UnsupportedEncodingException, ScriptException {
		// original graph
		if(visualization == 0){
			if (type == InfoEnum.GRAPHVIZ) {
				// visualize security goals in Graphviz
				showSimpleGraphInGraphviz(req_model, visualization);
			} else if (type==InfoEnum.CANVAS){
				// visualize security goals in Canvas
				showSimpleGraphInCanvas(req_model, visualization);
			}else{
			}
		} else{
			//reprocess ownership of security goals
			for(SecurityGoal sg: req_model.getSg_elem()){
				SecurityGoal temp_sg = (SecurityGoal)req_model.findElementByFormalName(sg.getFormalName());
				if(temp_sg!=null){
					sg.setRemark(InfoEnum.ElementRemark.TOPSG.name());
					sg.owner = temp_sg.owner;
					propagateSecurityGoalOwnership(sg, temp_sg.owner);
				}
			}
			// identify criticality for each potential security goals
			identifyCriticalSecurityGoal(req_model, actor_model);
			// identify best refinement path
			identifyBestRefinePath(req_model);
			// visualize security goals according to the type of visualization
			if (type == InfoEnum.GRAPHVIZ) {
				// visualize security goals in Graphviz
				showSimpleGraphInGraphviz(req_model, visualization);
			} else if (type==InfoEnum.CANVAS){
				// visualize security goals in Canvas
				showSimpleGraphInCanvas(req_model, visualization);
			} else{
			}
			
		}
	}

	
	
	private static void propagateSecurityGoalOwnership(SecurityGoal sg, Actor owner) {
		for(RequirementLink rl: sg.and_refine_links){
			SecurityGoal temp_sg = (SecurityGoal)rl.getSource();
			temp_sg.owner = owner;
			propagateSecurityGoalOwnership(temp_sg, owner);
		}
	}

	
	/**
	 * bottom-up way for identifying best path
	 * currently the path is not guaranteed to be the best one, as the parent of critical goals are always replaced by the last one.
	 * @param req_model
	 */
	private static void identifyBestRefinePath(RequirementGraph req_model) {
		for(SecurityGoal sg: req_model.getSg_elem()){
			if(sg.isCriticality()){
				if(sg.parent!=null&&sg.parent_link!=null){
					sg.parent.setRemark(InfoEnum.ElementRemark.BESTPATH.name());
					sg.parent_link.setRemark(InfoEnum.LinkRemark.BESTPATH.name());
					propagateBestRefinePath(sg.parent);
				}
			}
		}
	}

	
	
	private static void propagateBestRefinePath(SecurityGoal sg) {
		if(sg.parent!=null&&sg.parent_link!=null){
			sg.parent.setRemark(InfoEnum.ElementRemark.BESTPATH.name());
			sg.parent_link.setRemark(InfoEnum.LinkRemark.BESTPATH.name());
			propagateBestRefinePath(sg.parent);
		}
		else{
			return;
		}
		
	}

	/**
	 * top-down analysis for refinement path
	 * @param req_model
	 */
private static void identifyTopDownBestRefinePath(RequirementGraph req_model) {
		
		for(SecurityGoal sg: req_model.getSg_elem()){
			if(sg.getRemark().equals(InfoEnum.ElementRemark.TOPSG.name())){
				propagateTopDownBestRefinePath(sg);
			}
		}
	}

	private static void propagateTopDownBestRefinePath(SecurityGoal sg) {
		//sg.setRemark(InfoEnum.ElementRemark.BESTPATH.name());
		boolean refined = false;
		if(sg.isCriticality()||sg.isNon_deterministic()){
			return;
		}
		else{
			//first refine via interval
			for(RequirementLink rl: sg.and_refine_links){
				if(rl.refine_type.equals(InfoEnum.RefineType.INTERVAL.name())){
					refined = true;
					rl.setRemark(InfoEnum.LinkRemark.BESTPATH.name());
					rl.getSource().setRemark(InfoEnum.ElementRemark.BESTPATH.name());
					propagateTopDownBestRefinePath((SecurityGoal)rl.getSource());
				}
			}
			// then, refine via security attribute 
			if(!refined){
				for(RequirementLink rl: sg.and_refine_links){
					if(rl.refine_type.equals(InfoEnum.RefineType.ATTRIBUTE.name())){
						refined = true;
						rl.setRemark(InfoEnum.LinkRemark.BESTPATH.name());
						rl.getSource().setRemark(InfoEnum.ElementRemark.BESTPATH.name());
						propagateTopDownBestRefinePath((SecurityGoal)rl.getSource());
					}
				}	
				//finally, refine via asset
				if(!refined){
					for(RequirementLink rl: sg.and_refine_links){
						if(rl.refine_type.equals(InfoEnum.RefineType.ASSET.name())){
							refined = true;
							rl.setRemark(InfoEnum.LinkRemark.BESTPATH.name());
							rl.getSource().setRemark(InfoEnum.ElementRemark.BESTPATH.name());
							propagateTopDownBestRefinePath((SecurityGoal)rl.getSource());
						}
					}	
				}
			}
		}
	}


	/**
	 * for exhaustive security goal refinement analysis
	 *  
	 * @param req_model
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws ScriptException 
	 */
	private static void identifyCriticalSecurityGoal(RequirementGraph req_model, ActorAssociationGraph actor_model) throws FileNotFoundException,
	UnsupportedEncodingException, IOException, ScriptException {
				
		String ex_req_model_file = req_model.generateExhaustiveFormalExpression();
		// normally the actor association model keeps unchanged, so no need to rewrite it.
		String actor_model_file = "dlv/models/actor_association_model.dl ";
		if(actor_model.getElements().size()!=0){ 
			actor_model_file = actor_model.generateFormalExpressionToFile();	
		}

		String inference_rule = "";
		if (req_model.getLayer().equals(InfoEnum.Layer.BUSINESS.name())) {
			inference_rule = "dlv/dlv -silent -nofacts "
					+ "dlv/rules/simplification_bus.rule dlv/rules/simplification_general.rule "
					+ "dlv/models/business_process_model.dl dlv/models/asset_model.dl " 
					+ ex_req_model_file + " " + actor_model_file;
		} else if (req_model.getLayer().equals(InfoEnum.Layer.APPLICATION.name())) {
			inference_rule = "dlv/dlv -silent -nofacts " 
					+ "dlv/rules/simplification_app.rule dlv/rules/simplification_general.rule "
					+ "dlv/rules/sec_goal_ownership.rule dlv/models/temp_app_fact.dl "// infer security goal ownership from upper layers.
					+ "dlv/models/software_architecture_model.dl " + "dlv/models/asset_model.dl "
					+ ex_req_model_file + " " + actor_model_file;
		} else if (req_model.getLayer().equals(InfoEnum.Layer.PHYSICAL.name())) {
			inference_rule = " " + ex_req_model_file;
		} else {
			CommandPanel.logger.severe("Error refinement type!");
		}

		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(inference_rule);

		BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = null;

		while ((line = input.readLine()) != null) {
			// line = input.readLine();
			line = line.substring(1, line.length() - 1);
			String[] result = line.split(", ");
			for (String s : result) {
				// highlight the critical one
				if (s.startsWith("is_critical")) {
					// parse facts
					s = s.replaceAll("is\\_critical\\(", "");
					s = s.replaceAll("\\)", "");
					SecurityGoal critical_sec_goal = (SecurityGoal) req_model.findExhaustiveSecurityGoalByFormalName(s);
					critical_sec_goal.setCriticality(true);
				}
				// show the not determined one.
				else if (s.startsWith("non_deterministic")) {
					// parse facts
					s = s.replaceAll("non\\_deterministic\\(", "");
					s = s.replaceAll("\\)", "");
					SecurityGoal critical_sec_goal = (SecurityGoal) req_model.findExhaustiveSecurityGoalByFormalName(s);
					critical_sec_goal.setNon_deterministic(true);
				}
			}
		}		
	}
	
	
	
	private static void showSimpleGraphInCanvas(RequirementGraph req_model, int visualization) throws ScriptException {
		//processing elements
		for (SecurityGoal sg: req_model.getSg_elem()){
			String element_id = AppleScript.drawArbitraryRequirementElement(
					InfoEnum.esg_canvas.get(req_model.getLayer()), "All",
					InfoEnum.reverse_req_elem_type_map.get(InfoEnum.RequirementElementType.SOFTGOAL.name()),
					InfoEnum.NORMAL_SIZE, "{500,500}", "0", sg.getName());
			sg.setId(element_id);

			if (sg.isCriticality()) {
				AppleScript.changeAttributeOfElement(InfoEnum.esg_canvas.get(req_model.getLayer()), "none", element_id,
						"5", "Red", "Simple");
			} else if (sg.isNon_deterministic()) {
			} else if (visualization != InfoEnum.INITIAL_VIEW
					&& sg.getRemark().equals(InfoEnum.ElementRemark.BESTPATH.name())) {
				//only process under particular view.
				AppleScript.changeAttributeOfElement(InfoEnum.esg_canvas.get(req_model.getLayer()), "none", element_id,
						"5", "Blue", "Simple");
			}
			//if(sg.getRemark().equals(InfoEnum.ElementRemark.TOPSG.name()))
		}
		
		//processing links
		for (RequirementLink rl : req_model.getSg_links()) {
			String link_id = AppleScript.drawESGRefinementLink(rl);
			rl.setId(link_id);
			//set the layer of the link to "All", which cannot be done in last step...
			//TODO: further work might be done to fix this problem.
			AppleScript.changeAttributeOfLink(InfoEnum.esg_canvas.get(rl.getSource().getLayer()), "none",
					rl.getId(), "1", "none", "All");
			
			if (visualization != InfoEnum.INITIAL_VIEW && rl.getRemark().equals(InfoEnum.LinkRemark.BESTPATH.name())) {
				AppleScript.changeAttributeOfLink(InfoEnum.esg_canvas.get(rl.getSource().getLayer()), "none",
						rl.getId(), "3", "Blue", "Simple");
			}
			//if(((SecurityGoal)rl.getSource()).isCriticality() && ((SecurityGoal)rl.getTarget()).isCriticality())
		}
	}

	/**
	 * for exhaustive security goal refinement analysis
	 * This one only shows id of each security goal.
	 * 
	 * @param req_model
	 * @param visualization 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private static void showSimpleGraphInGraphviz(RequirementGraph req_model, int visualization) throws IOException {
		//export the security goal graph and visualize it.
		//the simple way to represent the graph, which is based on the id of each element, it is simple, but does not make much sense.
		String graph = "digraph G {\n" 
					+ "rankdir = BT;\n";//Determine layout direction

		for (SecurityGoal sg: req_model.getSg_elem()){
			String temp_graph="";
			
			if(sg.isCriticality()){
				temp_graph += "sg_"+sg.getId() + "[shape=ellipse, fontname=\"Helvetica-Bold\", style = filled, color = red];\n";
			} else if (sg.isNon_deterministic()){
				temp_graph += "sg_"+sg.getId() + "[shape=ellipse, fontname=\"Helvetica-Bold\", style = filled, color = green];\n";
			} else{
				temp_graph += "sg_"+sg.getId() + "[shape=ellipse, fontname=\"Helvetica-Bold\"";
				//only process when visualization is 1.
				if(sg.getRemark().equals(InfoEnum.ElementRemark.BESTPATH.name())){
					temp_graph += ", style = filled, color=blue";
				}
				temp_graph+="];\n";
			}
			
			//conditionally adding this edge.
			if(visualization !=2){
				graph += temp_graph;
			} else if(sg.isCriticality()||sg.getRemark().equals(InfoEnum.ElementRemark.TOPSG.name())||
					sg.getRemark().equals(InfoEnum.ElementRemark.BESTPATH.name())){
				graph += temp_graph;
			}
		}
		
		for (RequirementLink rl : req_model.getSg_links()) {
			String temp_graph="";
			SecurityGoal sg_source = (SecurityGoal) rl.getSource();
			SecurityGoal sg_target = (SecurityGoal) rl.getTarget();
			temp_graph += "sg_" + sg_source.getId() + " -> " + "sg_" + sg_target.getId();
			if (rl.refine_type.equals(InfoEnum.RefineType.ATTRIBUTE.name())) {
				temp_graph += "[label=\"S\"";
			} else if (rl.refine_type.equals(InfoEnum.RefineType.ASSET.name())) {
				temp_graph += "[label=\"A\"";
			} else if (rl.refine_type.equals(InfoEnum.RefineType.INTERVAL.name())) {
				temp_graph += "[label=\"I\"";
			} else {
				CommandPanel.logger.severe("Refinement type of the graph has problems.");
			}
			
			if(rl.getRemark().equals(InfoEnum.LinkRemark.BESTPATH.name())){
				temp_graph += ", penwidth = 2.5, color=blue";
			}
			
			temp_graph+="];\n";
			
			//conditionally adding this edge.
			if(visualization !=2){ // non-trimmed graph
				graph += temp_graph;
			}
			// trimmed graph contains links, which are in the best path and are highlighted in blue
			else if(rl.getRemark().equals(InfoEnum.LinkRemark.BESTPATH.name())){
				graph += temp_graph;
			}
			// trimmed graph also contains refinement links, which connect critical security goals
			else if(((SecurityGoal)rl.getSource()).isCriticality() && ((SecurityGoal)rl.getTarget()).isCriticality()){
				graph += temp_graph;
			}
		}

		graph += "}";

		String output = "graphviz/sec_goal_"+visualization+".gv";
		PrintWriter writer = new PrintWriter(output, "UTF-8");
		writer.println(graph);
		writer.close();

		//draw pdf figure for the corresponding graph
		Runtime rt;
		Process pr;
		String draw_graphviz = "graphviz/dot -Tpdf graphviz/sec_goal_"+visualization+".gv -o graphviz/sec_goal_"+visualization+".pdf";
		rt = Runtime.getRuntime();
		pr = rt.exec(draw_graphviz);
	}
	


	/**
	 * for exhaustive security goal refinement analysis
	 * This one shows the detailed content of each security goal
	 * 
	 * @param req_model
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	@Deprecated
	@SuppressWarnings({ "unused"})
	private static void showComplexGraph(RequirementGraph req_model) throws FileNotFoundException,
			UnsupportedEncodingException, IOException {
		
		//export the security goal graph and visualize it.
		String graph = "digraph G {\n" + "rankdir = BT\n";//Determine layout direction

		// This is a more complex way to represent security goals, which shows all dimensions. 
		for (RequirementLink rl : req_model.getSg_links()) {
			SecurityGoal sg_source = (SecurityGoal) rl.getSource();
			SecurityGoal sg_target = (SecurityGoal) rl.getTarget();
			graph += sg_source.getId() + " [shape=none, margin=0, label=< "
					+ "<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"5\">" + " <TR><TD>sg_"
					+ sg_source.getId() + "</TD></TR>" + " <TR><TD>" + sg_source.getImportance() + "</TD></TR>"
					+ " <TR><TD>" + sg_source.getSecurityAttribute() + "</TD></TR>" + " <TR><TD>"
					+ sg_source.getAsset() + "</TD></TR>" + " <TR><TD>" + sg_source.getInterval() + "</TD></TR>"
					+ " </TABLE>>];" + sg_target.getId() + " [shape=none, margin=0, label=<"
					+ "<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"5\">" + "<TR><TD>sg_"
					+ sg_target.getId() + "</TD></TR>" + "<TR><TD>" + sg_target.getImportance() + "</TD></TR>"
					+ " <TR><TD>" + sg_target.getSecurityAttribute() + "</TD></TR>" + "<TR><TD>" + sg_target.getAsset()
					+ "</TD></TR>" + "<TR><TD>" + sg_target.getInterval() + "</TD></TR>" + "</TABLE>>];"
					+ sg_source.getId() + " -> " + sg_target.getId();
			if (rl.refine_type.equals(InfoEnum.RefineType.ATTRIBUTE.name())) {
				graph += "[color=red, label=\"S\"];\n";
			} else if (rl.refine_type.equals(InfoEnum.RefineType.ASSET.name())) {
				graph += "[color=blue, label=\"A\"];\n";
			} else if (rl.refine_type.equals(InfoEnum.RefineType.INTERVAL.name())) {
				graph += "[color=green, label=\"I\"];\n";
			} else {
				CommandPanel.logger.severe("Refinement type of the graph has problems.");
			}
		}

		graph += "}";

		String output = "graphviz/sec_goal.gv";
		PrintWriter writer = new PrintWriter(output, "UTF-8");
		writer.println(graph);
		writer.close();

		//draw pdf figure for the corresponding graph
		Runtime rt;
		Process pr;
		String draw_graphviz = "graphviz/dot -Tpdf graphviz/sec_goal.gv -o graphviz/sec_goal.pdf";
		rt = Runtime.getRuntime();
		pr = rt.exec(draw_graphviz);
	}

	public static void securityGoalRefine(RequirementGraph req_model, String type, int scope) throws IOException, ScriptException {
		String expression_file = req_model.generateFormalExpressionToFile(scope);
		
		String security_model_file = "dlv/models/security_model_"+req_model.getLayer().toLowerCase()+".dl ";
		// absolute path: /Users/litong30/research/Trento/Workspace/research/TLSAF/
		String refine_rule = "";
		if (type.equals("asset")) {
			refine_rule = "dlv/dlv -silent -nofacts dlv/rules/refine_asset.rule dlv/models/asset_model.dl "
					+ expression_file;
		} else if (type.equals("attribute")) {
			refine_rule = "dlv/dlv -silent -nofacts dlv/rules/refine_security_attribute.rule "
					+ expression_file+" "+security_model_file;
		} else if (type.equals("interval")) {
			refine_rule = "dlv/dlv -silent -nofacts dlv/rules/refine_interval.rule " + expression_file;
		} else {
			CommandPanel.logger.severe("Error refinement type!");
		}

		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(refine_rule);

		BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = null;

		/*
		 * having the restriction that we only process "and_refined_sec_goal" we
		 * could harden a bit drawing logic into code.
		 */
		//		LinkedList<RequirementLink> new_links = new LinkedList<RequirementLink>();
		LinkedList<RequirementElement> refined_elems = new LinkedList<RequirementElement>();

		// parse reasoning result
		while ((line = input.readLine()) != null) {
			line = line.substring(1, line.length() - 1);
			String[] result = line.split(", ");
			for (String s : result) {
				// System.out.println(s);
				// only consider related security goals
				if (s.startsWith("and_refined_sec_goal")) {
					// parse facts
					s = s.replaceAll("and_refined_sec_goal\\(", "");
					s = s.replaceAll("\\)", "");
					String[] sg = s.split(",");
					// create new element`
					SecurityGoal refined_goal = (SecurityGoal) req_model.findElementByFormalName(sg[4]);
					SecurityGoal new_sg = new SecurityGoal(sg[0], sg[1], sg[2], sg[3],
							InfoEnum.RequirementElementType.SECURITY_GOAL.name(), refined_goal.getLayer());
					//update ownership relations
					if (refined_goal.owner != null) {
						refined_goal.owner.getOwnedElement().add(new_sg);
						new_sg.owner = refined_goal.owner;
					}

					req_model.getElements().add(new_sg);
					// create new link
					RequirementLink new_and_refine = new RequirementLink(
							InfoEnum.RequirementLinkType.AND_REFINE.name(), new_sg, refined_goal);
					req_model.getLinks().add(new_and_refine);

					refined_goal.and_refine_links.add(new_and_refine);
					if (refined_elems.indexOf(refined_goal) == -1) {
						refined_elems.add(refined_goal);
					}
					// System.out.println(new_sg.getFormalExpression()+"\n"+new_and_refine.getFormalExpression());
					// no id for newly added elements and links. This should be
					// done after graphic representation.
				}
			}
		}
		drawAndRefinement(refined_elems);
	}

	/**
	 * As the and-refine is not directly drawn in the picture, a bit more
	 * processing is required.
	 * 
	 * Currently, this method is particularly designed for refining security goals
	 * @param refined_elems
	 *            : a list of elements (security goals) that are fined.
	 * @throws ScriptException
	 */
	private static void drawAndRefinement(LinkedList<RequirementElement> refined_elems) throws ScriptException {
		// draw the reasoning result on omnigraffle
		for (RequirementElement refined_goal : refined_elems) {
			if (refined_goal.and_refine_links.size() == 1) {
				RequirementLink target_link = refined_goal.and_refine_links.getFirst();
				String source_id = AppleScript.drawRequirementElement(target_link.getSource(), refined_goal,
						"down");
				target_link.getSource().setId(source_id);
				//add mouse-over annotation
				AppleScript.addUserData(InfoEnum.REQ_TARGET_CANVAS, target_link.getSource().getLayer(), target_link.getSource().getId(),
						target_link.getSource().owner.getFormalName());
				// if there is only one refinement, we change and_refine to
				// refine
				target_link.setType(InfoEnum.RequirementLinkType.REFINE.name());
				String link_id = AppleScript.drawRequirementLink(target_link, InfoEnum.SINGLE_LAYER);
				target_link.setId(link_id);
			} else {
				// redundant link
				RequirementElement mid = new RequirementElement("",
						InfoEnum.RequirementElementType.MIDDLE_POINT.name(), refined_goal.getLayer());
				String mid_id = AppleScript.drawRequirementElement(mid, refined_goal, "down");
				mid.setId(mid_id);
				// doesn't add this into the logic model, as it does not make
				// sense.
				RequirementLink redundant_link = new RequirementLink(
						InfoEnum.RequirementLinkType.AND_REFINE_ARROW.name(), mid, refined_goal);
				redundant_link.setRemark(InfoEnum.LinkRemark.REDUNDANT.name());

				String redundant_id = AppleScript.drawRequirementLink(redundant_link, InfoEnum.SINGLE_LAYER);
				redundant_link.setId(redundant_id);
				// every and_refine link
				RequirementLink first_rl = refined_goal.and_refine_links.getFirst();
				RequirementElement first_re = first_rl.getSource();
				String temp_id = AppleScript.drawRequirementElement(first_re, mid, "down");
				first_re.setId(temp_id);
				//add mouse-over annotation
				AppleScript.addUserData("Model", first_re.getLayer(), first_re.getId(), first_re.owner.getFormalName());
				
				RequirementLink fake_rl = new RequirementLink(first_rl.getType(), first_rl.getSource(), mid);
				String link_id = AppleScript.drawRequirementLink(fake_rl, InfoEnum.SINGLE_LAYER);
				fake_rl.setId(link_id);

				// refined_goal.refine_links.removeFirst();
				RequirementElement next = null;
				RequirementElement reference = first_re;
				RequirementLink rl = null;
				for (int i = 1; i < refined_goal.and_refine_links.size(); i++) {
					rl = refined_goal.and_refine_links.get(i);
					fake_rl = new RequirementLink(rl.getType(), rl.getSource(), mid);
					next = rl.getSource();
					String next_id = AppleScript.drawRequirementElement(next, reference, "right");
					next.setId(next_id);
					reference = next;
					//add mouse-over annotation
					AppleScript.addUserData("Model", next.getLayer(), next.getId(), next.owner.getFormalName());

					link_id = AppleScript.drawRequirementLink(fake_rl, InfoEnum.SINGLE_LAYER);
					fake_rl.setId(link_id);
				}
			}
		}
	}

	public static void securityGoalSimplification(RequirementGraph req_model, ActorAssociationGraph actor_model, int scope)
			throws IOException, ScriptException {
		String req_model_file = req_model.generateFormalExpressionToFile(scope);
		// normally the actor association model keeps unchanged, so no need to rewrite it.
		String actor_model_file = "dlv/models/actor_association_model.dl ";
		if(actor_model.getElements().size()!=0){ 
			actor_model_file = actor_model.generateFormalExpressionToFile();	
		}

		String inference_rule = "";
		if (req_model.getLayer().equals(InfoEnum.Layer.BUSINESS.name())) {
			inference_rule = "dlv/dlv -silent -nofacts " + "dlv/rules/simplification_bus.rule "
					+ "dlv/rules/simplification_general.rule " + "dlv/models/business_process_model.dl "
					+ "dlv/models/asset_model.dl " + req_model_file + " " + actor_model_file;
		} else if (req_model.getLayer().equals(InfoEnum.Layer.APPLICATION.name())) {
			inference_rule = "dlv/dlv -silent -nofacts " + "dlv/rules/simplification_app.rule "
					+ "dlv/rules/simplification_general.rule " + "dlv/models/software_architecture_model.dl " 
					+ "dlv/models/asset_model.dl " + req_model_file + " " + actor_model_file;
					
			//+ "dlv/rules/sec_goal_ownership.rule " + "dlv/models/temp_app_fact.dl " 
		} else if (req_model.getLayer().equals(InfoEnum.Layer.PHYSICAL.name())) {
			inference_rule = " " + req_model_file;
		} else {
			CommandPanel.logger.severe("Error refinement type!");
		}

		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(inference_rule);

		BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = null;

		while ((line = input.readLine()) != null) {
			// line = input.readLine();
			line = line.substring(1, line.length() - 1);
			String[] result = line.split(", ");
			for (String s : result) {
				CommandPanel.logger.info(s);

				// highlight the critical one
				if (s.startsWith("is_critical")) {
					// parse facts
					s = s.replaceAll("is\\_critical\\(", "");
					s = s.replaceAll("\\)", "");

					SecurityGoal critical_sec_goal = (SecurityGoal) req_model.findElementByFormalName(s);
					critical_sec_goal.setCriticality(true);
					AppleScript.changeAttributeOfElement(InfoEnum.REQ_TARGET_CANVAS, critical_sec_goal.getLayer(), critical_sec_goal.getId(),
							"5", "none", "none");
				}
				// show the not determined one.
				else if (s.startsWith("non_deterministic")) {
					// parse facts
					s = s.replaceAll("non\\_deterministic\\(", "");
					s = s.replaceAll("\\)", "");

					SecurityGoal critical_sec_goal = (SecurityGoal) req_model.findElementByFormalName(s);
					critical_sec_goal.setNon_deterministic(true);
					AppleScript.changeAttributeOfElement(InfoEnum.REQ_TARGET_CANVAS, critical_sec_goal.getLayer(), critical_sec_goal.getId(),
							"3", "none", "none");

				}
			}
		}

	}

	public static void securityGoalOperationalization(RequirementGraph req_model, int scope) throws IOException, ScriptException {

		String expression_file = req_model.generateFormalExpressionToFile(scope);
		String security_model_file = "dlv/models/security_model_"+req_model.getLayer().toLowerCase()+".dl ";

		String refine_rule = "dlv/dlv -silent -nofacts dlv/rules/operationalization.rule "
				+ expression_file+" "+security_model_file;

		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(refine_rule);

		BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = null;

		LinkedList<RequirementElement> operated_elems = new LinkedList<RequirementElement>();
		while ((line = input.readLine()) != null) {
			// line = input.readLine();
			line = line.substring(1, line.length() - 1);
			String[] result = line.split(", ");
			for (String s : result) {
				//				System.out.println(s);
				if (s.startsWith("make")) {
					// parse facts
					s = s.replaceAll("make\\(", "");
					s = s.replaceAll("\\)", "");
					String[] sg = s.split(",");
					//only consider security mechanisms that are specific for the current layer
					if (!InfoEnum.security_mechanisms.get(sg[0]).equals(req_model.getLayer())) {
						continue;
					}
					// create new element
					SecurityGoal makedGoal = (SecurityGoal) req_model.findElementByFormalName(sg[1]);
					sg[0] = sg[0].replaceAll("\\_", " ");
					RequirementElement sec_mech = new RequirementElement(sg[0],
							InfoEnum.RequirementElementType.SECURITY_MECHANISM.name(), makedGoal.getLayer());
					req_model.getElements().add(sec_mech);
					// create new link
					RequirementLink new_make = new RequirementLink(InfoEnum.RequirementLinkType.MAKE.name(), sec_mech,
							makedGoal);
					req_model.getLinks().add(new_make);

					makedGoal.make_help_links.add(new_make);
					if (operated_elems.indexOf(makedGoal) == -1) {
						operated_elems.add(makedGoal);
					}

				} else if (s.startsWith("help")) {
					// parse facts
					s = s.replaceAll("help\\(", "");
					s = s.replaceAll("\\)", "");
					String[] sg = s.split(",");
					// create new element
					SecurityGoal helpedGoal = (SecurityGoal) req_model.findElementByFormalName(sg[1]);
					sg[0] = sg[0].replaceAll("\\_", " ");
					RequirementElement sec_mech = new RequirementElement(sg[0],
							InfoEnum.RequirementElementType.SECURITY_MECHANISM.name(), helpedGoal.getLayer());
					req_model.getElements().add(sec_mech);
					// create new link
					RequirementLink new_help = new RequirementLink(InfoEnum.RequirementLinkType.HELP.name(), sec_mech,
							helpedGoal);
					req_model.getLinks().add(new_help);

					helpedGoal.make_help_links.add(new_help);
					if (operated_elems.indexOf(helpedGoal) == -1) {
						operated_elems.add(helpedGoal);
					}
				} else {
				}
			}
			// draw the reasoning result on omnigraffle
			for (RequirementElement operated_elem : operated_elems) {
				for (RequirementLink mh : operated_elem.make_help_links) {
					String source_id = AppleScript.drawRequirementElement(mh.getSource(), mh.getTarget(),
							"down");
					mh.getSource().setId(source_id);
					String link_id = AppleScript.drawRequirementLink(mh, InfoEnum.SINGLE_LAYER);
					mh.setId(link_id);
				}
			}
		}
	}

	public static LinkedList<String> securityAlternativeSolutions(RequirementGraph req_model, int scope) {
		
		LinkedList<SecurityGoal> sg_set = new LinkedList<SecurityGoal>();
		LinkedList<SecurityGoal> sg_set_temp = new LinkedList<SecurityGoal>();
		SecurityGoal sg_temp = new SecurityGoal();
		for (Element elem : req_model.getElements()) {
			if (elem.getType().equals(InfoEnum.RequirementElementType.SECURITY_GOAL.name())) {
				sg_temp = (SecurityGoal) elem;
				if (sg_temp.isCriticality() == true) {
					sg_set_temp.add(sg_temp);
				}
			}
		}
		// If we focus on the selected elements, we remove the unselected one here.
		if (scope == InfoEnum.SELECTED_MODELS) {
			//obtain selected elements' id
			ArrayList<Long> selected_elements = null;
			try {
				selected_elements = AppleScript.getSelectedGraph();
			} catch (ScriptException e1) {
				e1.printStackTrace();
			}
			// Note that we only generate alternatives for critical security goals
			for (SecurityGoal sg: sg_set_temp){
				if(selected_elements.contains(Long.valueOf(sg.getId()))){
					sg_set.add(sg);
				}
			}
		} else if (scope == InfoEnum.ALL_MODELS){
			sg_set=sg_set_temp;
		}

		@SuppressWarnings("rawtypes")
		LinkedList<LinkedList> all = new LinkedList<LinkedList>();
		for (LinkedList<RequirementLink> temp : all) {
			temp = new LinkedList<RequirementLink>();
		}
		LinkedList<RequirementLink> one = new LinkedList<RequirementLink>();

		for (SecurityGoal sg : sg_set) {
			/*
			 * add an additional element to security goals to cover the
			 * situation that not treat the security goal at this level.
			 * Accordingly, a link is added to link this element to the security
			 * goal in order to accommodate the reasoning work. however, this is
			 * an auxiliary element which doesn't exist in the model.
			 */
			RequirementElement empty = new RequirementElement("not treat " + sg.getName(),
					InfoEnum.RequirementElementType.SECURITY_MECHANISM.name(), sg.getLayer());
			RequirementLink rl = new RequirementLink(InfoEnum.RequirementLinkType.MAKE.name(), empty, sg);
			sg.make_help_links.add(rl);
		}
		getCombination(sg_set, all, one, 0);

		LinkedList <String> result = new LinkedList<String>();
		String solution="";
		for (int i = 0; i < all.size(); i++) {
			solution ="Alternative "+ (i+1)+": ";
			for (int j = 0; j < all.get(i).size(); j++) {
				if (((RequirementLink) all.get(i).get(j)).getSource().getName().contains("not treat")) {
					//System.out.print(((RequirementLink) all.get(i).get(j)).getSource().getName() + " ");
					solution += "not treat sg_"+ ((RequirementLink) all.get(i).get(j)).getTarget().getId() + " ";
				} else {
					//System.out.print(((RequirementLink) all.get(i).get(j)).getFormalExpression() + " ");
					solution += "apply "+((RequirementLink) all.get(i).get(j)).getSource().getName() + " to "+ "sg_"+
							((RequirementLink) all.get(i).get(j)).getTarget().getId()+";  ";
				}
			}
			result.add(solution);
		}
		
		return result;
	}

	private static void getCombination(LinkedList<SecurityGoal> sg_set, @SuppressWarnings("rawtypes") LinkedList<LinkedList> all,
			LinkedList<RequirementLink> one, int current) {
		if (current == sg_set.size() - 1) {
			for (RequirementLink mh : sg_set.get(current).make_help_links) {
				one.add(mh);
				LinkedList<RequirementLink> backup = new LinkedList<RequirementLink>();
				for (RequirementLink temp : one) {
					backup.add(temp);
				}
				all.add(backup);
				one.removeLast();
			}
			return;
		}

		for (RequirementLink mh : sg_set.get(current).make_help_links) {
			one.add(mh);
			getCombination(sg_set, all, one, current + 1);
			one.removeLast();
		}
	}

	public static void securityBusToAppTransformation(RequirementGraph req_bus_model, RequirementGraph req_app_model, int scope)
			throws ScriptException, IOException {
		// process security mechanisms
		crossLayerSecurityMechanism(req_bus_model, req_app_model, scope);

		// process untreated security goals
		crossLayerSecurityGoalBUSAPP(req_bus_model, req_app_model, scope);

	}
	
	/**
	 * Transferring security concerns from BP layer to APP layer, which focuses on chosen security mechanisms
	 * This transformation is done via fixed patterns, which do not require further inferences. 
	 * @param up_req_model
	 * @param down_req_model
	 * @param scope
	 * @throws ScriptException
	 * @throws IOException
	 */
	private static void crossLayerSecurityMechanism(RequirementGraph up_req_model, RequirementGraph down_req_model, int scope)
			throws ScriptException, IOException {

		for (Link rl : up_req_model.getLinks()) {
			// logically processing
			if (rl.getType().equals(InfoEnum.RequirementLinkType.HELP.name())
					|| rl.getType().equals(InfoEnum.RequirementLinkType.MAKE.name())) {
				// first add the functional goal in the application layer
				
//				RequirementElement sm = new RequirementElement(rl.getSource().getName(),
//						InfoEnum.RequirementElementType.SECURITY_MECHANISM.name(), InfoEnum.Layer.APPLICATION.name());
				
				RequirementElement sm = (RequirementElement) rl.getSource();
						
				RequirementElement sm_goal = new RequirementElement("support " + rl.getSource().getName(),
						InfoEnum.RequirementElementType.GOAL.name(), InfoEnum.Layer.APPLICATION.name());
				RequirementLink support_sm1 = new RequirementLink(InfoEnum.RequirementLinkType.SUPPORT.name(), sm_goal,
						sm);
				
				// add related security goals
				SecurityGoal bus_sg = (SecurityGoal) rl.getTarget();
				SecurityGoal app_sg1 = new SecurityGoal(bus_sg.getImportance(), "application integrity",
						"corresponding application", sm_goal.getName(),
						InfoEnum.RequirementElementType.SECURITY_GOAL.name(), InfoEnum.Layer.APPLICATION.name());
				if (bus_sg.owner != null) {
					bus_sg.owner.getOwnedElement().add(app_sg1);
					app_sg1.owner = bus_sg.owner;
				}

				SecurityGoal app_sg2 = new SecurityGoal(bus_sg.getImportance(), "application availability",
						"corresponding application", sm_goal.getName(),
						InfoEnum.RequirementElementType.SECURITY_GOAL.name(), InfoEnum.Layer.APPLICATION.name());
				if (bus_sg.owner != null) {
					bus_sg.owner.getOwnedElement().add(app_sg2);
					app_sg2.owner = bus_sg.owner;
				}

				down_req_model.getLinks().add(support_sm1);
//				req_app_model.getElements().add(sm);
				down_req_model.getElements().add(sm_goal);
				down_req_model.getElements().add(app_sg1);
				down_req_model.getElements().add(app_sg2);
				
				
				// graphical processing
				// set the new elements are put right below the security mechanisms 
				String position = "{"+sm.origin_x+","+(sm.origin_y+100)+"}";
				
				String id = "";
				// draw new elements
				id = AppleScript.drawRequirementElement(sm_goal, sm, "down");
				sm_goal.setId(id);
				id = AppleScript.drawRequirementElement(app_sg1, sm, "down");
				app_sg1.setId(id);
				id = AppleScript.drawRequirementElement(app_sg2, sm, "down");
				app_sg2.setId(id);
				
				//add mouse-over annotation
				AppleScript.addUserData(InfoEnum.REQ_TARGET_CANVAS, app_sg1.getLayer(), app_sg1.getId(),
						app_sg1.owner.getName());
				AppleScript.addUserData(InfoEnum.REQ_TARGET_CANVAS, app_sg2.getLayer(), app_sg2.getId(),
						app_sg2.owner.getName());
				
				// draw new links
				id = AppleScript.drawRequirementLink(support_sm1, InfoEnum.CROSS_LAYERS);
				support_sm1.setId(id);
			}
		}
	}

	/**
	 * Transfer untreated security goals to the next layer down
	 * @param up_req_model
	 * @param down_req_model
	 * @param scope
	 * @throws IOException
	 * @throws ScriptException
	 */
	private static void crossLayerSecurityGoalBUSAPP(RequirementGraph up_req_model, RequirementGraph down_req_model, int scope)
			throws IOException, ScriptException {
		//TODO: revise here
		String expression_file1 = up_req_model.generateFormalExpressionToFile(scope);
		String expression_file2 = down_req_model.generateFormalExpressionToFile(scope);

		String refine_rule = "dlv/dlv -silent -nofacts dlv/rules/cross_layer.rule " + expression_file1 + " "
				+ expression_file2;
		// String refine_rule =
		// "dlv/dlv -silent -nofacts dlv/rules/cross_layer.rule dlv/rules/general.rule dlv/models/req_business_model.dl dlv/models/req_application_model.dl";
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(refine_rule);

		BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = null;

		//LinkedList<RequirementLink> new_links = new LinkedList<RequirementLink>();
		LinkedList<RequirementElement> refined_elems = new LinkedList<RequirementElement>();

		while ((line = input.readLine()) != null) {
			// line = input.readLine();
			line = line.substring(1, line.length() - 1);
			String[] result = line.split(", ");
			for (String s : result) {
				//				System.out.println(s);
				if (s.startsWith("refined_sec_goal")) {
					// parse facts
					s = s.replaceAll("refined_sec_goal\\(", "");
					s = s.replaceAll("\\)", "");
					String[] sg = s.split(",");
					// create new element
					SecurityGoal refined_goal = (SecurityGoal) up_req_model.findElementByFormalName(sg[4]);
					
					SecurityGoal new_sg = new SecurityGoal(sg[0], sg[1], sg[2], sg[3],
							InfoEnum.RequirementElementType.SECURITY_GOAL.name(), down_req_model.getLayer());
					// propagate the owner of security goal to its refinements. 
					if (refined_goal.owner != null) {
						refined_goal.owner.getOwnedElement().add(new_sg);
						new_sg.owner = refined_goal.owner;
					}
					
					down_req_model.getElements().add(new_sg);
					
					// create new link
					RequirementLink new_and_refine = new RequirementLink(
							InfoEnum.RequirementLinkType.AND_REFINE.name(), new_sg, refined_goal);
					down_req_model.getLinks().add(new_and_refine);

					refined_goal.and_refine_links.add(new_and_refine);
					if (refined_elems.indexOf(refined_goal) == -1) {
						refined_elems.add(refined_goal);
					}
				}
			}
		}

		
		String position="";

		
//		for (RequirementElement sg : refined_elems) {
//			// add the new elements below the refined sg.
//			position = "{"+sg.origin_x+","+(sg.origin_y+220)+"}";
//			String id = AppleScript.drawArbitraryRequirementElement(InfoEnum.REQ_TARGET_CANVAS, sg.getLayer(), "Cloud", position, "0",
//					"(S)\n" + sg.getName());
//			sg.setId(id);
//		}

		drawAndRefinement(refined_elems);
	}
	
	
	@Deprecated 
	private static void crossLayerSecurityGoalSingleTaskDifferentCanvas(RequirementGraph up_req_model, RequirementGraph down_req_model, int scope)
			throws IOException, ScriptException {
		//TODO: revise here
		String expression_file1 = up_req_model.generateFormalExpressionToFile(scope);
		String expression_file2 = down_req_model.generateFormalExpressionToFile(scope);

		String refine_rule = "dlv/dlv -silent -nofacts dlv/rules/cross_layer.rule " + expression_file1 + " "
				+ expression_file2;
		// String refine_rule =
		// "dlv/dlv -silent -nofacts dlv/rules/cross_layer.rule dlv/rules/general.rule dlv/models/req_business_model.dl dlv/models/req_application_model.dl";
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(refine_rule);

		BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = null;

		//LinkedList<RequirementLink> new_links = new LinkedList<RequirementLink>();
		LinkedList<RequirementElement> refined_elems = new LinkedList<RequirementElement>();

		while ((line = input.readLine()) != null) {
			// line = input.readLine();
			line = line.substring(1, line.length() - 1);
			String[] result = line.split(", ");
			for (String s : result) {
				//				System.out.println(s);
				if (s.startsWith("refined_sec_goal")) {
					// parse facts
					s = s.replaceAll("refined_sec_goal\\(", "");
					s = s.replaceAll("\\)", "");
					String[] sg = s.split(",");
					// create new element
					SecurityGoal refined_goal = (SecurityGoal) up_req_model.findElementByFormalName(sg[4]);
					if (refined_goal.next_layer_copy == null) {
						refined_goal.next_layer_copy = new SecurityGoal(refined_goal.getImportance(),
								refined_goal.getSecurityAttribute(), refined_goal.getAsset(),
								refined_goal.getInterval(), refined_goal.getType(), down_req_model.getLayer());
						refined_goal.next_layer_copy.owner = refined_goal.owner;
					}
					SecurityGoal new_sg = new SecurityGoal(sg[0], sg[1], sg[2], sg[3],
							InfoEnum.RequirementElementType.SECURITY_GOAL.name(), down_req_model.getLayer());
					// propagate the owner of security goal to its refinements. 
					if (refined_goal.owner != null) {
						refined_goal.owner.getOwnedElement().add(new_sg);
						new_sg.owner = refined_goal.owner;
					}

					down_req_model.getElements().add(new_sg);
					if (down_req_model.getElements().indexOf(refined_goal.next_layer_copy) == -1) {
						down_req_model.getElements().add(refined_goal.next_layer_copy);
					}
					// create new link
					RequirementLink new_and_refine = new RequirementLink(
							InfoEnum.RequirementLinkType.AND_REFINE.name(), new_sg, refined_goal.next_layer_copy);
					down_req_model.getLinks().add(new_and_refine);

					refined_goal.next_layer_copy.and_refine_links.add(new_and_refine);
					if (refined_elems.indexOf(refined_goal.next_layer_copy) == -1) {
						refined_elems.add(refined_goal.next_layer_copy);
					}
				}
			}
		}

		for (RequirementElement sg : refined_elems) {
			String id = AppleScript.drawArbitraryRequirementElement(InfoEnum.REQ_TARGET_CANVAS, sg.getLayer(), "Cloud", InfoEnum.NORMAL_SIZE, "{2000, 100}", "0",
					"(S)\n" + sg.getName());
			sg.setId(id);
		}

		drawAndRefinement(refined_elems);
	}

	/**
	 * @param req_bus_model
	 * @param req_app_model
	 * @throws ScriptException
	 * @throws IOException 
	 */
	@Deprecated
	private static void crossLayerSecurityMechanismSingleTaskDifferentCanvas(RequirementGraph req_bus_model, RequirementGraph req_app_model, int scope)
			throws ScriptException, IOException {

		for (Link rl : req_bus_model.getLinks()) {
			// logically processing
			if (rl.getType().equals(InfoEnum.RequirementLinkType.HELP.name())
					|| rl.getType().equals(InfoEnum.RequirementLinkType.MAKE.name())) {
				// first add the functional goal in the application layer
				RequirementElement sm = new RequirementElement(rl.getSource().getName(),
						InfoEnum.RequirementElementType.SECURITY_MECHANISM.name(), InfoEnum.Layer.APPLICATION.name());
				RequirementElement sm_goal = new RequirementElement("support " + rl.getSource().getName(),
						InfoEnum.RequirementElementType.GOAL.name(), InfoEnum.Layer.APPLICATION.name());
				RequirementLink support_sm1 = new RequirementLink(InfoEnum.RequirementLinkType.SUPPORT.name(), sm_goal,
						sm);
				// add related security goals
				SecurityGoal bus_sg = (SecurityGoal) rl.getTarget();
				SecurityGoal app_sg1 = new SecurityGoal(bus_sg.getImportance(), "application integrity",
						"corresponding application", sm_goal.getName(),
						InfoEnum.RequirementElementType.SECURITY_GOAL.name(), InfoEnum.Layer.APPLICATION.name());
				if (bus_sg.owner != null) {
					bus_sg.owner.getOwnedElement().add(app_sg1);
					app_sg1.owner = bus_sg.owner;
				}

				SecurityGoal app_sg2 = new SecurityGoal(bus_sg.getImportance(), "application availability",
						"corresponding application", sm_goal.getName(),
						InfoEnum.RequirementElementType.SECURITY_GOAL.name(), InfoEnum.Layer.APPLICATION.name());
				if (bus_sg.owner != null) {
					bus_sg.owner.getOwnedElement().add(app_sg2);
					app_sg2.owner = bus_sg.owner;
				}

				req_app_model.getLinks().add(support_sm1);
				req_app_model.getElements().add(sm);
				req_app_model.getElements().add(sm_goal);
				req_app_model.getElements().add(app_sg1);
				req_app_model.getElements().add(app_sg2);
				// graphical processing
				String id = "";
				String temp_id = "";
				id = AppleScript.drawArbitraryRequirementElement(InfoEnum.REQ_TARGET_CANVAS, sm.getLayer(), "Hexagon", InfoEnum.NORMAL_SIZE, "{600, 100}", "0",
						"(S)\n" + sm.getName());
				sm.setId(id);
				temp_id = AppleScript.drawRequirementElement(sm_goal, sm, "down");
				sm_goal.setId(temp_id);
				temp_id = AppleScript.drawRequirementElement(app_sg1, sm, "down");
				app_sg1.setId(temp_id);
				temp_id = AppleScript.drawRequirementElement(app_sg2, sm, "down");
				app_sg2.setId(temp_id);
				temp_id = AppleScript.drawRequirementLink(support_sm1, InfoEnum.SINGLE_LAYER);
				support_sm1.setId(temp_id);
				//				temp_id = AppleScript.drawRequirementLink(support_sm2);
				//				support_sm2.setId(temp_id);
				//				temp_id = AppleScript.drawRequirementLink(support_sm3);
				//				support_sm3.setId(temp_id);
			}
		}
	}
	
	/*
	 * Related methods
	 */
	static void execAppleScript(String script_path) throws IOException, ScriptException {
		String script = readFile(script_path, Charset.defaultCharset());
		// print(script);

		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("AppleScript");
		engine.eval(script);
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
	

	

}
