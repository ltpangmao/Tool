package it.unitn.tlsaf.ds;

import java.util.LinkedList;

public class Actor extends RequirementElement {
	private LinkedList<RequirementElement> ownedElement = new LinkedList<RequirementElement>();
	private RequirementElement boundary = null;

	public Actor() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Actor(String name, String type, String layer) {
		super(name, type, layer);
		// TODO Auto-generated constructor stub
	}

	public Actor(String name, String type, String layer, LinkedList<RequirementLink> inLinks,
			LinkedList<RequirementLink> outLinks) {
		super(name, type, layer, inLinks, outLinks);
		// TODO Auto-generated constructor stub
	}

	public LinkedList<RequirementElement> getOwnedElement() {
		return ownedElement;
	}

	public void setOwnedElement(LinkedList<RequirementElement> ownedElement) {
		this.ownedElement = ownedElement;
	}

	public RequirementElement getBoundary() {
		return boundary;
	}

	public void setBoundary(RequirementElement boundary) {
		this.boundary = boundary;
	}

	@Override
	public String getFormalExpressions() {
		String expression =this.getSingleFormalExpression() + "\n";
		for (RequirementElement re : this.getOwnedElement()) {
			expression += "has(" + this.getFormalName() + "," + re.getFormalName() + ").\n";
//			if (!re.getName().equals("empty") && !re.getType().equals(InfoEnum.RequirementElementType.SECURITY_GOAL.name())) {
//				expression += "has(" + this.getName() + "," + re.getName() + ").\n";
//			}
//			else if(re.getType().equals(InfoEnum.RequirementElementType.SECURITY_GOAL.name())){
//				expression += "has(" + this.getName() + "," + ((SecurityGoal)re).getFormalName() + ").\n";
//			}
		}
		//expression = expression.replaceAll(" ", "_");
		return expression;//.toLowerCase();
	}

}
