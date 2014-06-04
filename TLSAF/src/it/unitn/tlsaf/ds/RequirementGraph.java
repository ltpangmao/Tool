package it.unitn.tlsaf.ds;

import it.unitn.tlsaf.func.AppleScript;
import it.unitn.tlsaf.func.CommandPanel;
import it.unitn.tlsaf.func.Inference;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptException;
import javax.sound.midi.MidiDevice.Info;

/**
 * A data structure to organize all model information
 * 
 * @author litong30
 */
public class RequirementGraph {
	
	private LinkedList<Element> elements = new LinkedList<Element>();
	private LinkedList<Link> links = new LinkedList<Link>();
	private String type;
	private String layer;
	/*
	 * The following data structures are specific to the requirement graph, but
	 * I didn't create additional class, as i am not sure whether I will
	 * implement graphs for other graphs... Or I just change this class to a
	 * specific requirement graph, and add others whenever I need in the future.
	 */

	/*
	 * Specially designed data structure, which support the complete security
	 * refinement analysis. After the analysis within this model, the result
	 * should be reflected in the main model In other words, this is just used
	 * temporarily
	 */
	private LinkedList<SecurityGoal> sg_elems = new LinkedList<SecurityGoal>();
	private LinkedList<RequirementLink> sg_links = new LinkedList<RequirementLink>();

	public RequirementGraph() {
		super();
	}

	public RequirementGraph(String type) {
		super();
		this.type = type;
	}

	public RequirementGraph(String type, String layer) {
		super();
		this.type = type;
		this.layer = layer;
	}

	public RequirementGraph(LinkedList<Element> elements, LinkedList<Link> links) {
		super();
		this.elements = elements;
		this.links = links;
	}
	
	public LinkedList<RequirementLink> getSg_links() {
		return sg_links;
	}

	public void setSg_links(LinkedList<RequirementLink> sg_links) {
		this.sg_links = sg_links;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public LinkedList<Element> getElements() {
		return elements;
	}

	public void setElements(LinkedList<Element> elements) {
		this.elements = elements;
	}

	public LinkedList<Link> getLinks() {
		return links;
	}

	public void setLinks(LinkedList<Link> links) {
		this.links = links;
	}

	public String getLayer() {
		return layer;
	}

	public void setLayer(String layer) {
		this.layer = layer;
	}

	public LinkedList<SecurityGoal> getSg_elem() {
		return sg_elems;
	}

	public void setSg_elem(LinkedList<SecurityGoal> sg_elem) {
		this.sg_elems = sg_elem;
	}

	/*
	 * Public Methods
	 */
	public void importGraphInfo(String result) throws IOException {
		List<String> elements = Arrays.asList(result.split("\n"));
		// first processing, which simply imports all information from the
		// text file
		for (String element : elements) {
			if (element.startsWith("element")) {
				List<String> factors = Arrays.asList(element.split(";"));
				if (this.findElementById(factors.get(1)) == null) {
					// avoid adding redundant elements
					RequirementElement elem = parseReqElementInfo(factors);
					this.getElements().add(elem);
				}
			}
		}
		for (String element : elements) {
			if (element.startsWith("link")) {
				List<String> factors = Arrays.asList(element.split(";"));
				if (this.findLinkById(factors.get(1)) == null) {
					// avoid adding redundant links
					RequirementLink link = parseReqLinkInfo(factors);
					if (link!=null) {
						this.getLinks().add(link);
					}
				}
				// if (link != null) link.printInfo();
			}
		}

		// second around processing, which processes redundant
		// "graph sugar", and correct and_refine, trust, depend relations.
		for (Element elem : this.getElements()) {
			reprocessRequirementElement((RequirementElement) elem);
		}
		
		// in case a security goal still lacks of owner
		for (Element e : this.getElements()) {
			if (e.getType().equals(InfoEnum.RequirementElementType.SECURITY_GOAL.name())){
				SecurityGoal sg = (SecurityGoal)e;
				if(sg.owner_text.equals("null")){
					CommandPanel.logger.severe("Missing the ownership of security goal "+sg.getName());
				} else{
					sg.owner= new Actor();
					sg.owner.setName(sg.owner_text);
				}
			}
		}

		/* finally identify the owner of each security goal
		 * Here we check whether all security goals have been added with owner info
		 * In our analysis, we don't really need the Actor object, but only its name.
		 */
				
		/*
		 * This the ownership from BP layer can be inferred, but only for business layer!!!
		 * In following layers, this should be filled as "user data"
		 * But after evolve the code 
		
		if (this.getType().equals(InfoEnum.Layer.BUSINESS.name())) {
			reprocessSecurityGoalOwnership(this.getLayer(), this.getElements());
		}*/
		
	}

	/*
	 * private methods
	 */
	private RequirementElement parseReqElementInfo(List<String> factors) {
		/*
		 * this part is exclusively for requirement elements 0)notation,element;
		 * 1)id,51670; 2)shape,Hexagon; 3)name,Calculate price;
		 * 4)layer,Business; 5)thickness, 1.0; 6)double stroke; 7)size:
		 * 117.945899963379 43.817626953125; 8)no fill; 9)0.0 corner radius 10)
		 * stroke pattern: 0 11) origin: 87.234039306641 1084.06665039062 
		 * 12) owner: xx 13) Canvas, Model
		 */
		//TODO: pre-process all numbers fields, don't know why so far...
		factors.set(5, factors.get(5).replaceAll(",", "."));
		factors.set(7, factors.get(7).replaceAll(",", "."));
		factors.set(9, factors.get(9).replaceAll(",", "."));
		factors.set(11, factors.get(11).replaceAll(",", "."));
		
		
		RequirementElement new_elem;
		//security goals
		if (factors.get(3).startsWith("(S)") & factors.get(2).equals("Cloud")) {
			new_elem = new SecurityGoal();
			new_elem.setId(factors.get(1));
			new_elem.setType(InfoEnum.RequirementElementType.SECURITY_GOAL.name());

			String sg_name = factors.get(3);
			// remove"(S)" at the first beginning
			sg_name = sg_name.replaceAll("\\(S\\)", "");

			new_elem.setName(sg_name.trim());
			new_elem.setLayer(factors.get(4));
			// get value for security-specific attributes
			if (Float.valueOf(factors.get(5)) > 1) {
				((SecurityGoal) new_elem).setCriticality(true);
				;
			} else {
				((SecurityGoal) new_elem).setCriticality(false);
				;
			}
			//this value can be useful in the following analysis 
			new_elem.owner_text=factors.get(12);
			((SecurityGoal) new_elem).extractInfoFromName();
		}
		//actors
		else if (checkCircle(factors.get(7))) {
			new_elem = new Actor();
			new_elem.setId(factors.get(1));
			new_elem.setName(factors.get(3));
			new_elem.setLayer(factors.get(4));
			new_elem.setType(InfoEnum.RequirementElementType.ACTOR.name());
		}
		//all others
		else {
			new_elem = new RequirementElement();
			new_elem.setId(factors.get(1));
			if (factors.get(3).startsWith("(S)") & factors.get(2).equals("Hexagon")) {
				new_elem.setType(InfoEnum.RequirementElementType.SECURITY_MECHANISM.name());
			} else if (factors.get(3).equals("empty") & factors.get(2).equals("Circle") & factors.get(10).equals("0")) {
				new_elem.setType(InfoEnum.RequirementElementType.MIDDLE_POINT.name());
				// new_elem.setRemark(InfoEnum.ElementRemark.REFINEUM.name());
			} else if (factors.get(3).equals("empty") & factors.get(2).equals("Circle") & factors.get(10).equals("1")) {
				new_elem.setType(InfoEnum.RequirementElementType.ACTOR_BOUNDARY.name());
				new_elem.setRemark(InfoEnum.ElementRemark.BOUNDARY.name());
			} else if (factors.get(2).equals("AndGate")
					|| (factors.get(2).equals("Rectangle") & factors.get(9).equals("0.0"))) {//Why rectangle?
				new_elem.setType(InfoEnum.RequirementElementType.LABEL.name());
			} else {
				new_elem.setType(InfoEnum.req_elem_type_map.get(factors.get(2)));
			}

			if (factors.get(3).startsWith("(S)")) {
				String sm_name = factors.get(3);
				// remove"(S)" at the first beginning
				sm_name = sm_name.replaceAll("\\(S\\)", "");
				new_elem.setName(sm_name.trim());
			} else {
				new_elem.setName(factors.get(3));
			}
			new_elem.setLayer(factors.get(4));
		}

		// The layout related information is applicable for all types of elements
		String[] temp = factors.get(7).split(" ");
		new_elem.width = Double.parseDouble(temp[0]);
		new_elem.height = Double.parseDouble(temp[1]);
		String[] temp2 = factors.get(11).split(" ");
		new_elem.origin_x = Double.parseDouble(temp2[0]);
		new_elem.origin_y = Double.parseDouble(temp2[1]);

		return new_elem;
	}

	private RequirementLink parseReqLinkInfo(List<String> factors) {
		/* first check the source and destination of each link.
		 * If they are not missing, it means this link is a  
		 */
		
		//if this is a support link, we record its sourceID and desId, which will be processed later
		if(factors.get(2).equals("SharpArrow") & factors.get(7).equals("1")){
			RequirementLink new_link = new RequirementLink();
			new_link.setId(factors.get(1));
			new_link.setType(InfoEnum.RequirementLinkType.SUPPORT.name());
			new_link.source_id = factors.get(4);
			new_link.des_id = factors.get(5);
			// fake elements used to prevent unexpected errors.
			new_link.setSource(new RequirementElement());
			new_link.setTarget(new RequirementElement());
			return new_link;
		}
		
		// obtain the elements of the link
		// if it is the cross-layer refinements, we don't process it for this moment
		// Here mainly processes the cross-layer refinements
		Element source = findElementById(factors.get(4));
		Element target = findElementById(factors.get(5));
		if(target == null || source == null){
			return null;
		}
		
		/*
		 * this part is exclusively for requirement elements 0)link; 1)id,51690
		 * 2)arrow type,StickArrow; 3)line type, curved; 4)source/tail,51670;
		 * 5)destination/head,51490; 6)label,NoLabel;(The shape of that label is
		 * not considered, only the content of that label) 7)dash type,0;
		 * 8)thickness,1.0; 9)head scale,1.0; 10) layer, BUSINESS
		 */
		RequirementLink new_link = new RequirementLink();
		// first process complex links
		new_link.setId(factors.get(1));
		new_link.setSource(source);
		new_link.setTarget(target);
		source.getOutLinks().add(new_link);
		target.getInLinks().add(new_link);

		if (factors.get(2).equals("SharpArrow") & factors.get(7).equals("0")
				& !new_link.getSource().getType().equals(InfoEnum.RequirementElementType.MIDDLE_POINT.name())) {
			new_link.setType(InfoEnum.RequirementLinkType.REFINE.name());
		} else if (factors.get(2).equals("NoHead") & factors.get(7).equals("0") & factors.get(6).equals("NoLabel")) {
			new_link.setType(InfoEnum.RequirementLinkType.AND_REFINE.name());
		} else if (factors.get(2).equals("SharpArrow") & factors.get(7).equals("1")) {
			new_link.setType(InfoEnum.RequirementLinkType.SUPPORT.name());
		} else if (factors.get(2).equals("StickArrow") & factors.get(7).equals("0") & factors.get(6).equals("NoLabel")) {
			new_link.setType(InfoEnum.RequirementLinkType.OPERATIONALIZE.name());
		} else if (factors.get(2).equals("NoHead")
				& factors.get(7).equals("0")
				& (factors.get(6).equals("T1") || factors.get(6).equals("T2") || factors.get(6).equals("T3")
						|| factors.get(6).equals("T4") || factors.get(6).equals("T5"))) {
			new_link.setType(InfoEnum.RequirementLinkType.TRUST.name());
			new_link.setRemark(factors.get(6).substring(0, 1));
		} else if (factors.get(2).equals("NoHead") & factors.get(7).equals("0") & factors.get(6).equals("D")) {
			new_link.setType(InfoEnum.RequirementLinkType.DEPEND.name());
		} else if (factors.get(2).equals("StickArrow") & factors.get(7).equals("0")
				& factors.get(6).toLowerCase().equals("make")) {
			new_link.setType(InfoEnum.RequirementLinkType.MAKE.name());
		} else if (factors.get(2).equals("StickArrow") & factors.get(7).equals("0")
				& factors.get(6).toLowerCase().equals("help")) {
			new_link.setType(InfoEnum.RequirementLinkType.HELP.name());
		} else if (factors.get(2).equals("StickArrow") & factors.get(7).equals("0")
				& factors.get(6).toLowerCase().equals("hurt")) {
			new_link.setType(InfoEnum.RequirementLinkType.HURT.name());
		} else if (factors.get(2).equals("StickArrow") & factors.get(7).equals("0")
				& factors.get(6).toLowerCase().equals("break")) {
			new_link.setType(InfoEnum.RequirementLinkType.BREAK.name());
		} else if (factors.get(2).equals("DoubleArrow") & factors.get(7).equals("0")) {
			new_link.setType(InfoEnum.RequirementLinkType.PREFERRED_TO.name());
		} else if (factors.get(2).equals("SharpArrow") & factors.get(7).equals("0")
				& new_link.getSource().getType().equals(InfoEnum.RequirementElementType.MIDDLE_POINT.name())) {
			new_link.setType(InfoEnum.RequirementLinkType.AND_REFINE_ARROW.name());
			//mark as redundant
			new_link.setRemark(InfoEnum.LinkRemark.REDUNDANT.name());
		} else {
			CommandPanel.logger.severe("Unknown links cannot be imported");
		}

		return new_link;
	}

	/**
	 * This is only applicable to the Business layer. 
	 * ownership information should be specified in following layers.
	 * Not work correctly after an evolution...
	 * @param layer
	 * @param elems
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void reprocessSecurityGoalOwnership(String layer, LinkedList<Element> elems) throws IOException {
		//		String expression_file1 = up_req_model.generateFormalExpression();
		//		String expression_file2 = down_req_model.generateFormalExpression();

		String expression_file = this.generateFormalExpressionToFile(InfoEnum.ALL_MODELS);
		String refine_rule = "";
		if (layer.equals(InfoEnum.Layer.BUSINESS.name())) {
			refine_rule = "dlv/dlv -silent -nofacts dlv/rules/sec_goal_ownership.rule " + expression_file;
		} 
		/*else if (layer.equals(InfoEnum.Layer.APPLICATION.name())) {
			refine_rule = "dlv/dlv -silent -nofacts dlv/rules/sec_goal_ownership.rule dlv/models/temp_fact_app.dl "
					+ "dlv/models/asset_model.dl " + expression_file;
		} else if (layer.equals(InfoEnum.Layer.PHYSICAL.name())) {
			refine_rule = "dlv/dlv -silent -nofacts dlv/rules/sec_goal_ownership.rule dlv/models/temp_fact_app.dl "
					+ "dlv/models/asset_model.dl " + expression_file;
		}*/ 
		else {
			CommandPanel.logger.severe("processing security goal ownership error");
		}

		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(refine_rule);

		BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = null;

		while ((line = input.readLine()) != null) {
			// line = input.readLine();
			line = line.substring(1, line.length() - 1);
			String[] result = line.split(", ");

			for (String s : result) {
				// only consider related security goals
				if (s.startsWith("has")) {
					// parse facts
					s = s.replaceAll("has\\(", "");
					s = s.replaceAll("\\)", "");
					String[] re = s.split(",");

					// add the information to the layer-specific agent.
					if (this.findElementByFormalName(re[0]) != null) {
						Actor a = (Actor) this.findElementByFormalName(re[0]);
						RequirementElement sg = (RequirementElement) this.findElementByFormalName(re[1]);
						if (sg.getType().equals(InfoEnum.RequirementElementType.SECURITY_GOAL.name())) {
							a.getOwnedElement().add(sg);
							((SecurityGoal) sg).owner = a;
						}
					} else {
						/*
						 * the corresponding actor is not available in this layer. We don't process it here, 
						 * but adding related information to the related file, which is used for inferring security goal ownerships.
						 */
					}
				}
			}
		}

	}

	/**
	 * After all elements and links are imported, some special elements need
	 * further review according to their related links. Refineum, dependum,
	 * trustum, supported task Also actors should be revised to add "has"
	 * relation
	 * 
	 * @param elem
	 */
	private void reprocessRequirementElement(RequirementElement elem) {
		// first tackle actor-related issues
		if (elem.getType().equals(InfoEnum.RequirementElementType.ACTOR.name())) {
			Actor actor = (Actor) elem;
			// First find the boundary element for each actor
			for (Element e : elements) {
				RequirementElement boundary = (RequirementElement) e;
				if (boundary.getType().equals(InfoEnum.RequirementElementType.ACTOR_BOUNDARY.name())) {
					double distance = Math.sqrt((elem.origin_x - boundary.origin_x)
							* (elem.origin_x - boundary.origin_x) + (elem.origin_y - boundary.origin_y)
							* (elem.origin_y - boundary.origin_y));
					if (distance < 60) {
						actor.setBoundary(boundary);
						break;
					}
				}
			}
			// Then we find all elements within each actor, and attribute them to the actor
			double left_up_x = actor.getBoundary().origin_x;
			double left_up_y = actor.getBoundary().origin_y;
			double right_down_x = actor.getBoundary().origin_x + actor.getBoundary().width;
			double right_down_y = actor.getBoundary().origin_y + actor.getBoundary().height;
			for (Element e : elements) {
				RequirementElement re = (RequirementElement) e;
				if (re.origin_x > left_up_x && re.origin_x < right_down_x && re.origin_y > left_up_y
						&& re.origin_y < right_down_y) {
					if(!actorOwn(actor, re)){
						actor.getOwnedElement().add(re);
					}
				}
			}
			
			//Then, identify the owners of security goals
			for (Element e : elements) {
				if (e.getType().equals(InfoEnum.RequirementElementType.SECURITY_GOAL.name())){
					SecurityGoal sg = (SecurityGoal)e;
					if(sg.owner_text.equals(actor.getFormalName())){
						sg.owner= actor;
						actor.getOwnedElement().add(sg);
					}
				}
			}
			
			return;
		}
		// mark redundant support elements as redundant, don't process support link
		/*else if (elem.getInLinks().size() == 1
				&& (elem.getInLinks().getFirst().getType().equals(InfoEnum.RequirementLinkType.SUPPORT.name()))) {
			if (elem.getType().equals(InfoEnum.RequirementElementType.TASK.name())
					|| elem.getType().equals(InfoEnum.RequirementElementType.SECURITY_GOAL.name())) {
				elem.setRemark(InfoEnum.ElementRemark.SUPPORTUM.name());
				elem.getInLinks().getFirst().setRemark(InfoEnum.LinkRemark.REDUNDANT.name());
			}
		} */
		// mark redundant links as redundant
		else if (elem.getType().equals(InfoEnum.RequirementElementType.MIDDLE_POINT.name())) {
			elem.setRemark(InfoEnum.ElementRemark.REFINEUM.name());
			// process refineum related "and_refine" links
			if (elem.getOutLinks().size() != 1) {
				CommandPanel.logger.severe("and_refine link processing error");
			} else {
				RequirementElement target = elem.getOutLinks().getFirst().getTarget();
				for (RequirementLink l : elem.getInLinks()) {
					l.setTarget(target);
					// use additional space to store info about and-refine
					// relations
					target.and_refine_links.add(l);
				}
			}
			return;
		} else {
			for (RequirementLink link : elem.getInLinks()) {
				// insert this to facilitate security operationalization analysis.
				if (link.getType().equals(InfoEnum.RequirementLinkType.MAKE.name())
						|| link.getType().equals(InfoEnum.RequirementLinkType.HELP.name())) {
					link.getTarget().make_help_links.add(link);
				}
				// trust relation processing decides whether an element is a "xxdum"
				// this part has been depleted
				else if (link.getType().equals(InfoEnum.RequirementLinkType.TRUST.name())) {
					elem.setRemark(InfoEnum.ElementRemark.TRUSTUM.name());
					if (elem.getOutLinks().size() == 1 & elem.getInLinks().size() == 1) {
						RequirementLink in_trust_link = elem.getInLinks().getFirst();
						// use the in_trust_link to represent the whole trust relation
						in_trust_link.setAttachment((RequirementElement) elem);
						in_trust_link.setTarget(elem.getOutLinks().getFirst().getTarget());
						// deplete the out_trust_link
						elem.getOutLinks().getFirst().setRemark(InfoEnum.LinkRemark.REDUNDANT.name());
					} else {
						CommandPanel.logger.severe("trust link processing error");
					}
					return;
				}
				// depend relation processing decides whether an element is a "xxdum"
				// because actors have been processed before, it will not appear here. 
				else if (link.getType().equals(InfoEnum.RequirementLinkType.DEPEND.name())) {
					elem.setRemark(InfoEnum.ElementRemark.DEPENDUM.name());
					if (elem.getOutLinks().size() == 1 & elem.getInLinks().size() == 1) {
						RequirementLink in_depend_link = elem.getInLinks().getFirst();
						// use the in_depend_link to represent the whole depend relation
						in_depend_link.setAttachment((RequirementElement) elem);
						in_depend_link.setTarget(elem.getOutLinks().getFirst().getTarget());
						// deplete the out_depend_link
						elem.getOutLinks().getFirst().setRemark(InfoEnum.LinkRemark.REDUNDANT.name());
					} else {
						CommandPanel.logger.severe("depend link processing error");
					}
					return;
				}
			}
		}
	}

	private boolean actorOwn(Actor actor, RequirementElement re) {
		for(Element e: actor.getOwnedElement()){
			if (e.getId().equals(re.getId())){
				return true;
			}
		}
		return false;
	}

	/**
	 * Generate formal expressions for each element and link
	 * @return the list of formal expressions.
	 */
	public String generateFormalExpression(int scope) {
		String result = "";
		if (scope == InfoEnum.ALL_MODELS) {
			for (Element e : this.elements) {
				if (e.getFormalExpressions() != "")
					result += e.getFormalExpressions() + "\n";
			}
			for (Link l : this.links) {
				if (l.getFormalExpressions() != "")
					result += l.getFormalExpressions() + "\n";
			}
			result = result.toLowerCase();
		} else if (scope == InfoEnum.SELECTED_MODELS) {
			//obtain selected elements' id
			ArrayList<Long> selected_elements = null;
			try {
				selected_elements = AppleScript.getSelectedGraph();
			} catch (ScriptException e1) {
				e1.printStackTrace();
			}
			for (Element e : this.elements) {
				// We only selectively choose security goal model, other models are all chosen by default
				// so only the security security goal that are not selected will be excluded.
				if (!e.getType().equals(InfoEnum.RequirementElementType.SECURITY_GOAL.name())
						|| selected_elements.contains(Long.valueOf(e.getId()))) {
					if (e.getFormalExpressions() != "")
						result += e.getFormalExpressions() + "\n";
				}
			}
			for (Link l : this.links) {
				// We here assume that 
				if (l.getFormalExpressions() != "")
					result += l.getFormalExpressions() + "\n";
			}
		}
		return result;
	}
	
	/**
	 * Generate formal expression and output to files
	 * @return address of the output file
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public String generateFormalExpressionToFile(int scope) throws FileNotFoundException, UnsupportedEncodingException {
		String result = generateFormalExpression(scope); 
		CommandPanel.logger.fine(result);

		String output = "";
		if (this.getType() == InfoEnum.ModelCategory.REQUIREMENT.name()) {
			output = "dlv/models/req_" + this.getLayer().toLowerCase() + "_model.dl";
		} else {
			output = "dlv/models/other_model.dl";
		}
		PrintWriter writer = new PrintWriter(output, "UTF-8");
		writer.println(result);
		writer.close();

		return output;
	}
	
	public String generateExhaustiveFormalExpression() throws FileNotFoundException, UnsupportedEncodingException {
		String result = "";
		for (Element e : this.elements) {
			if (e.getFormalExpressions() != "")
				result += e.getFormalExpressions() + "\n";
		}
		for(SecurityGoal sg : this.sg_elems){
			if (sg.getFormalExpressions() != "")
				result += sg.getFormalExpressions() + "\n";
		}
		for (Link l : this.links) {
			if (l.getFormalExpressions() != "")
				result += l.getFormalExpressions() + "\n";
		}

		result = result.toLowerCase();

		String output = "";
		if (this.getType() == InfoEnum.ModelCategory.REQUIREMENT.name()) {
			output = "dlv/models/ex_req_" + this.getLayer().toLowerCase() + "_model.dl";
		} else {
			output = "dlv/models/other_model.dl";
		}
		PrintWriter writer = new PrintWriter(output, "UTF-8");
		writer.println(result);
		writer.close();

		return output;
	}

	public Element findElementById(String id) {
		for (Element e : this.elements) {
			if (e.getId().equals(id))
				return e;
		}
		return null;
	}

	public Link findLinkById(String id) {
		for (Link l : this.links) {
			if (l.getId().equals(id))
				return l;
		}
		return null;
	}

	public Element findElementByFormalName(String fname) {
		for (Element e : this.elements) {
			if (e.getFormalName().equals(fname))
				return e;
		}
		return null;
	}

	
	private boolean checkCircle(String size) {
		size = size.trim();
		String s1 = size.substring(0, size.indexOf(" "));
		String s2 = size.substring(size.indexOf(" ") + 1);
		return s1.equals(s2);
	}

	/**
	 * Find a security goal in the separated space according to its related
	 * properties.
	 * 
	 * @param importance
	 * @param attribute
	 * @param asset
	 * @param interval
	 * @return corresponding security goal or null
	 */
	public SecurityGoal findExhausiveSecurityGoalByAttributes(String importance, String attribute, String asset, String interval) {
		for (SecurityGoal sg : sg_elems) {
			if (sg.getImportance().equals(importance) && sg.getSecurityAttribute().equals(attribute)
					&& sg.getAsset().equals(asset) && sg.getInterval().equals(interval)) {
				return sg;
			}
		}
		return null;
	}

	/**
	 * Find a security goal in the separated space according to its formal name
	 * @param s
	 * @return corresponding security goal or null
	 */
	public SecurityGoal findExhaustiveSecurityGoalByFormalName(String s) {
		for (SecurityGoal sg : sg_elems) {
			if (sg.getFormalName().equals(s)) {
				return sg;
			}
		}
		return null;
	}
	
	public void printModel(){
		CommandPanel.logger.info(this.generateFormalExpression(InfoEnum.ALL_MODELS));
	}

	

	

}
