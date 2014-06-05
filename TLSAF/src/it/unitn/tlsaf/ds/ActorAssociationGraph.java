package it.unitn.tlsaf.ds;

import it.unitn.tlsaf.func.CommandPanel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ActorAssociationGraph {
	private LinkedList<Element> elements = new LinkedList<Element>();
	private LinkedList<Link> links = new LinkedList<Link>();
	private String type;
	
	
	
	public ActorAssociationGraph() {
		super();
	}
	
	
	public ActorAssociationGraph(String type) {
		super();
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
	
	
	public void importGraphInfo(String result) throws IOException {
		if (this.getType() == InfoEnum.ModelCategory.ACTOR.name()) {
			List<String> elements = Arrays.asList(result.split("\n"));
			// first processing, which simply imports all information from the text file
			for (String element : elements) {
				if (element.startsWith("element")) {
					List<String> factors = Arrays.asList(element.split(";"));
					if (this.findElementById(factors.get(1)) == null) {
						// avoid adding redundant elements
						RequirementElement elem = parseActorElementInfo(factors);
						this.getElements().add(elem);
					}
				}
			}
			for (String element : elements) {
				if (element.startsWith("link")) {
					List<String> factors = Arrays.asList(element.split(";"));
					if (this.findLinkById(factors.get(1)) == null) {
						// avoid adding redundant links
						RequirementLink link = parseActorLinkInfo(factors);
						this.getLinks().add(link);
					}
					// if (link != null) link.printInfo();
				}
			}
		}
	}
	
	
	private RequirementElement parseActorElementInfo(List<String> factors) {
		/*
		 * this part is exclusively for requirement elements 0)notation,element;
		 * 1)id,51670; 2)shape,Hexagon; 3)name,Calculate price;
		 * 4)Layer, Layer 1 by default; 5)thickness,; 6)double stroke; 7)size:
		 * 117.945899963379 43.817626953125; 8)no fill; 9)0.0 corner radius 10)
		 * stroke pattern: 0 11) origin: 87.234039306641 1084.06665039062
		 * 12) owner: xx 13) Canvas, Actor association
		 */
		
		
		RequirementElement new_actor = new RequirementElement();
		if(!factors.get(13).equals("Actor association")){
			CommandPanel.logger.fine("only processing actor association diagram");
		}
		//actors
		if (factors.get(2).equals("Circle")) {
			new_actor = new Actor();
			new_actor.setId(factors.get(1));
			new_actor.setName(factors.get(3));
			new_actor.setType(InfoEnum.RequirementElementType.ACTOR.name());
		}
		else if (factors.get(2).equals("AndGate")) {
			new_actor.setId(factors.get(1));
			new_actor.setName(factors.get(3));
			new_actor.setType(InfoEnum.RequirementElementType.LABEL.name());
		}
		else {
			System.out.println("element type is not correct.");
			return null;
		}
		return new_actor;
	}
	
	
	private RequirementLink parseActorLinkInfo(List<String> factors) {
		/*
		 * this part is exclusively for requirement elements 0)link; 1)id,51690
		 * 2)arrow type,StickArrow; 3)line type, curved; 4)source/tail,51670;
		 * 5)destination/head,51490; 6)label,NoLabel;(The shape of that label is
		 * not considered, only the content of that label) 7)dash type,0;
		 * 8)thickness,1.0; 9)head scale,1.0 10) layer, Layer 1
		 */
		RequirementLink new_link = new RequirementLink();
		// first process complex links

		new_link.setId(factors.get(1));
		Element source = findElementById(factors.get(4));
		Element target = findElementById(factors.get(5));
		new_link.setSource(source);
		new_link.setTarget(target);
		source.getOutLinks().add(new_link);
		target.getInLinks().add(new_link);

		if (factors.get(2).equals("StickArrow") & factors.get(7).equals("0") & factors.get(6).contains("T")) {
			new_link.setType(InfoEnum.RequirementLinkType.TRUST.name());
			new_link.trust_level=factors.get(6).substring(1, 2);
		} 
		else if (factors.get(2).equals("StickArrow") & factors.get(7).equals("0") & factors.get(6).equals("U")) {
			new_link.setType(InfoEnum.RequirementLinkType.USE.name());
		} else if (factors.get(2).equals("StickArrow") & factors.get(7).equals("0") & factors.get(6).equals("M")) {
			new_link.setType(InfoEnum.RequirementLinkType.MAINTAIN.name());
		} else if (factors.get(2).equals("StickArrow") & factors.get(7).equals("0") & factors.get(6).equals("O")) {
			new_link.setType(InfoEnum.RequirementLinkType.OWN.name());
		} else{
			System.out.println("actor association processing error");
		}
	
		return new_link;
	}

	//TODO: can be further abstracted to a parent class, the same for the following methods.
	public String generateFormalExpression(){
		String result = "";
		for (Element e : this.elements) {
			if (e.getFormalExpressions() != "")
				result += e.getFormalExpressions() + "\n";
		}
		for (Link l : this.links) {
			if (l.getFormalExpressions() != "")
				result += l.getFormalExpressions() + "\n";
		}
		result = result.toLowerCase();
		return result;
	}
	
	public String generateFormalExpressionToFile() throws FileNotFoundException, UnsupportedEncodingException {
		String result = generateFormalExpression(); 
				
		String output = "";
		if (this.getType() == InfoEnum.ModelCategory.ACTOR.name()) {
			output = "dlv/models/actor_association_model.dl";
		} else {
			System.out.println("Actor association model error");
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
	
}
