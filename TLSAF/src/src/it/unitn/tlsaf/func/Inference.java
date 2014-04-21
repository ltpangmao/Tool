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
import java.util.LinkedList;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Processing logic of inference rules
 * 
 * @author litong30
 */
public class Inference {

	/*
	 * public inference functions
	 */
	public static void importActorModel(ActorAssociationGraph actor_model) throws IOException, ScriptException {
		String script_path = "applescript/import_info.applescript";
		execAppleScript(script_path);

		String result = readFile("applescript/graph_info.txt", Charset.defaultCharset());
		actor_model.importGraphInfo(result);
	}

	public static void importReqModel(RequirementGraph req_model, boolean from_canvas) throws IOException, ScriptException {
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
	public static void exhaustiveSecurityGoalRefineAnalysis(RequirementGraph req_model, ActorAssociationGraph actor_model, int visualization) throws IOException, ScriptException {
		// first empty the potential security goal set.
		req_model.getSg_elem().clear();
		req_model.getSg_links().clear();
		
		String expression_file = req_model.generateFormalExpression();
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
						System.out.println("Refine type is not set correctly");
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
			
			// original graph
			if(visualization == 0){
				// visualize  security goals
				showSimpleGraph(req_model, visualization);
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
				showSimpleGraph(req_model, visualization);
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
			actor_model_file = actor_model.generateFormalExpression();	
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
			System.out.println("Error refinement type!");
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

	/**
	 * for exhaustive security goal refinement analysis
	 * 
	 * @param req_model
	 * @param visualization 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private static void showSimpleGraph(RequirementGraph req_model, int visualization) throws IOException {
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
				System.out.println("Refinement type of the graph has problems.");
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
	 * 
	 * @param req_model
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
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
				System.out.println("Refinement type of the graph has problems.");
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

	public static void securityGoalRefine(RequirementGraph req_model, String type) throws IOException, ScriptException {
		String expression_file = req_model.generateFormalExpression();
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
			System.out.println("Error refinement type!");
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
					// create new element
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
	 * @param refined_elems
	 *            : a list of elements (security goals) that are fined.
	 * @throws ScriptException
	 */
	public static void drawAndRefinement(LinkedList<RequirementElement> refined_elems) throws ScriptException {
		// draw the reasoning result on omnigraffle
		for (RequirementElement refined_goal : refined_elems) {
			if (refined_goal.and_refine_links.size() == 1) {
				RequirementLink target_link = refined_goal.and_refine_links.getFirst();
				String source_id = AppleScript.drawRequirementElement(target_link.getSource(), refined_goal.getId(),
						"down");
				target_link.getSource().setId(source_id);
				// if there is only one refinement, we change and_refine to
				// refine
				target_link.setType(InfoEnum.RequirementLinkType.REFINE.name());
				String link_id = AppleScript.drawRequirementLink(target_link);
				target_link.setId(link_id);
			} else {
				// redundant link
				RequirementElement mid = new RequirementElement("",
						InfoEnum.RequirementElementType.MIDDLE_POINT.name(), refined_goal.getLayer());
				String mid_id = AppleScript.drawRequirementElement(mid, refined_goal.getId(), "down");
				mid.setId(mid_id);
				// doesn't add this into the logic model, as it does not make
				// sense.
				RequirementLink redundant_link = new RequirementLink(
						InfoEnum.RequirementLinkType.AND_REFINE_ARROW.name(), mid, refined_goal);
				redundant_link.setRemark(InfoEnum.LinkRemark.REDUNDANT.name());

				String redundant_id = AppleScript.drawRequirementLink(redundant_link);
				redundant_link.setId(redundant_id);
				// every and_refine link
				RequirementLink first_rl = refined_goal.and_refine_links.getFirst();
				RequirementElement first_re = first_rl.getSource();
				String temp_id = AppleScript.drawRequirementElement(first_re, mid.getId(), "down");
				first_re.setId(temp_id);
				RequirementLink fake_rl = new RequirementLink(first_rl.getType(), first_rl.getSource(), mid);
				String link_id = AppleScript.drawRequirementLink(fake_rl);
				fake_rl.setId(link_id);

				// refined_goal.refine_links.removeFirst();
				RequirementElement next = null;
				String reference_id = first_re.getId();
				RequirementLink rl = null;
				for (int i = 1; i < refined_goal.and_refine_links.size(); i++) {
					rl = refined_goal.and_refine_links.get(i);
					fake_rl = new RequirementLink(rl.getType(), rl.getSource(), mid);
					next = rl.getSource();
					String next_id = AppleScript.drawRequirementElement(next, reference_id, "right");
					next.setId(next_id);
					reference_id = next_id;

					link_id = AppleScript.drawRequirementLink(fake_rl);
					fake_rl.setId(link_id);
				}
			}
		}
	}

	public static void securityGoalSimplification(RequirementGraph req_model, ActorAssociationGraph actor_model)
			throws IOException, ScriptException {
		String req_model_file = req_model.generateFormalExpression();
		// normally the actor association model keeps unchanged, so no need to rewrite it.
		String actor_model_file = "dlv/models/actor_association_model.dl ";
		if(actor_model.getElements().size()!=0){ 
			actor_model_file = actor_model.generateFormalExpression();	
		}

		String inference_rule = "";
		if (req_model.getLayer().equals(InfoEnum.Layer.BUSINESS.name())) {
			inference_rule = "dlv/dlv -silent -nofacts " + "dlv/rules/simplification_bus.rule "
					+ "dlv/rules/simplification_general.rule " + "dlv/models/business_process_model.dl "
					+ "dlv/models/asset_model.dl " + req_model_file + " " + actor_model_file;
		} else if (req_model.getLayer().equals(InfoEnum.Layer.APPLICATION.name())) {
			inference_rule = "dlv/dlv -silent -nofacts " + "dlv/rules/simplification_app.rule "
					+ "dlv/rules/simplification_general.rule " + "dlv/rules/sec_goal_ownership.rule "
					+ "dlv/models/software_architecture_model.dl " + "dlv/models/asset_model.dl "
					+ "dlv/models/temp_app_fact.dl " + req_model_file + " " + actor_model_file;
		} else if (req_model.getLayer().equals(InfoEnum.Layer.PHYSICAL.name())) {
			inference_rule = " " + req_model_file;
		} else {
			System.out.println("Error refinement type!");
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
				System.out.println(s);

				// highlight the critical one
				if (s.startsWith("is_critical")) {
					// parse facts
					s = s.replaceAll("is\\_critical\\(", "");
					s = s.replaceAll("\\)", "");

					SecurityGoal critical_sec_goal = (SecurityGoal) req_model.findElementByFormalName(s);
					critical_sec_goal.setCriticality(true);
					AppleScript.changeAttributeOfElement(critical_sec_goal.getLayer(), critical_sec_goal.getId(),
							"set thickness of target_element to 5");
				}
				// show the not determined one.
				else if (s.startsWith("non_deterministic")) {
					// parse facts
					s = s.replaceAll("non\\_deterministic\\(", "");
					s = s.replaceAll("\\)", "");

					SecurityGoal critical_sec_goal = (SecurityGoal) req_model.findElementByFormalName(s);
					critical_sec_goal.setNon_deterministic(true);
					AppleScript.changeAttributeOfElement(critical_sec_goal.getLayer(), critical_sec_goal.getId(),
							"set thickness of target_element to 3");

				}
			}
		}

	}

	public static void securityGoalOperationalization(RequirementGraph req_model) throws IOException, ScriptException {

		String expression_file = req_model.generateFormalExpression();
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
					String source_id = AppleScript.drawRequirementElement(mh.getSource(), mh.getTarget().getId(),
							"down");
					mh.getSource().setId(source_id);
					String link_id = AppleScript.drawRequirementLink(mh);
					mh.setId(link_id);
				}
			}
		}
	}

	public static void securityAlternativeSolutions(RequirementGraph req_model) {
		LinkedList<SecurityGoal> sg_set = new LinkedList<SecurityGoal>();
		SecurityGoal sg_temp = new SecurityGoal();
		for (Element elem : req_model.getElements()) {
			if (elem.getType().equals(InfoEnum.RequirementElementType.SECURITY_GOAL.name())) {
				sg_temp = (SecurityGoal) elem;
				if (sg_temp.isCriticality() == true) {
					sg_set.add(sg_temp);
				}
			}
		}

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

		for (int i = 0; i < all.size(); i++) {
			System.out.print("Alternative "+ i+": ");
			for (int j = 0; j < all.get(i).size(); j++) {
				if (((RequirementLink) all.get(i).get(j)).getSource().getName().contains("not treat")) {
					//System.out.print(((RequirementLink) all.get(i).get(j)).getSource().getName() + " ");
					System.out.print("not treat sg_"+ ((RequirementLink) all.get(i).get(j)).getTarget().getId() + " ");
				} else {
					//System.out.print(((RequirementLink) all.get(i).get(j)).getFormalExpression() + " ");
					System.out.print("apply "+((RequirementLink) all.get(i).get(j)).getSource().getName() + " to "+ "sg_"+
							((RequirementLink) all.get(i).get(j)).getTarget().getId()+";  ");
				}
			}
			System.out.println();
		}
	}

	private static void getCombination(LinkedList<SecurityGoal> sg_set, LinkedList<LinkedList> all,
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

	public static void securityBusToAppTransformation(RequirementGraph req_bus_model, RequirementGraph req_app_model)
			throws ScriptException, IOException {
		// process security mechanisms
		crossLayerSecurityMechanism(req_bus_model, req_app_model);

		// process security goals
		crossLayerSecurityGoal(req_bus_model, req_app_model);

	}

	private static void crossLayerSecurityGoal(RequirementGraph up_req_model, RequirementGraph down_req_model)
			throws IOException, ScriptException {
		String expression_file1 = up_req_model.generateFormalExpression();
		String expression_file2 = down_req_model.generateFormalExpression();

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
			String id = AppleScript.drawRequirementElement(sg.getLayer(), "Cloud", "{150,90}", "{2000, 100}", "0",
					"(S)\n" + sg.getName());
			sg.setId(id);
		}

		drawAndRefinement(refined_elems);
	}

	/**
	 * @param req_bus_model
	 * @param req_app_model
	 * @throws ScriptException
	 */
	private static void crossLayerSecurityMechanism(RequirementGraph req_bus_model, RequirementGraph req_app_model)
			throws ScriptException {

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
				id = AppleScript.drawRequirementElement(sm.getLayer(), "Hexagon", "{100,90}", "{600, 100}", "0",
						"(S)\n" + sm.getName());
				sm.setId(id);
				temp_id = AppleScript.drawRequirementElement(sm_goal, id, "down");
				sm_goal.setId(temp_id);
				temp_id = AppleScript.drawRequirementElement(app_sg1, id, "down");
				app_sg1.setId(temp_id);
				temp_id = AppleScript.drawRequirementElement(app_sg2, id, "down");
				app_sg2.setId(temp_id);
				temp_id = AppleScript.drawRequirementLink(support_sm1);
				support_sm1.setId(temp_id);
				//				temp_id = AppleScript.drawRequirementLink(support_sm2);
				//				support_sm2.setId(temp_id);
				//				temp_id = AppleScript.drawRequirementLink(support_sm3);
				//				support_sm3.setId(temp_id);
			}
		}
	}

	public static void showReqModel(RequirementGraph req_model) throws IOException {
		if(req_model==null){
			System.out.println("The required model is empty");
			return; 
		}
		
		String result = "";
		for (Element e : req_model.getElements()) {
			if (e.getFormalExpressions() != "")
				result += e.getFormalExpressions() + "\n";
		}
		for (Link l : req_model.getLinks()) {
			if (l.getFormalExpression() != "")
				result += l.getFormalExpression() + "\n";
		}

		result = result.toLowerCase();
		System.out.println(result);
	}

	public static void showActorModel(ActorAssociationGraph actor_model) throws IOException {
		if(actor_model==null){
			System.out.println("The required model is empty");
			return; 
		}
		
		String result = "";
		for (Element e : actor_model.getElements()) {
			if (e.getFormalExpressions() != "")
				result += e.getFormalExpressions() + "\n";
		}
		for (Link l : actor_model.getLinks()) {
			if (l.getFormalExpression() != "")
				result += l.getFormalExpression() + "\n";
		}

		result = result.toLowerCase();
		System.out.println(result);
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
