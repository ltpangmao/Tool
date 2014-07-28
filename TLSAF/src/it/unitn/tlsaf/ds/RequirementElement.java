package it.unitn.tlsaf.ds;

import it.unitn.tlsaf.func.CommandPanel;

import java.util.LinkedList;

public class RequirementElement implements Element{
	private String Id;
	private String name;
	private String type;
	private String layer;
	private String category = InfoEnum.ModelCategory.REQUIREMENT.name();
	private String remark = InfoEnum.ElementRemark.NORMAL.name(); //used for dealing with special cases
	private LinkedList <RequirementLink> inLinks = new LinkedList<RequirementLink>();  //Current element is target
	private LinkedList <RequirementLink> outLinks = new LinkedList<RequirementLink>(); //Current element is source
	
	// programming sugar...
	public Actor owner = null;
	public String owner_text = null;
	//redundant variable
	public LinkedList <RequirementLink> and_refine_links = new LinkedList<RequirementLink>();  //facilitate and-refinement
	public LinkedList <RequirementLink> make_help_links = new LinkedList<RequirementLink>();  //facilitate operationalization
	//layout information
	public double origin_x=-1;
	public double origin_y=-1;
	public double width=-1;
	public double height=-1;

	
	
	public RequirementElement() {
		super();
	}

	public RequirementElement(String name, String type, String layer) {
		super();
		this.name = name;
		this.type = type;
		this.layer = layer;
	}

	public RequirementElement(String name, String type, String layer,
			LinkedList<RequirementLink> inLinks, LinkedList<RequirementLink> outLinks) {
		super();
		this.name = name;
		this.type = type;
		this.layer = layer;
		this.inLinks = inLinks;
		this.outLinks = outLinks;
	}

	@Override
	public String getRemark() {
		return remark;
	}
	@Override
	public void setRemark(String remark) {
		this.remark = remark;
	}
	@Override
	public String getId() {
		return Id;
	}
	@Override
	public void setId(String id) {
		Id = id;
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String getType() {
		return type;
	}
	@Override
	public void setType(String type) {
		this.type = type;
	}
	public String getLayer() {
		return layer;
	}

	public void setLayer(String layer) {
		this.layer = layer;
	}
	@Override
	public LinkedList<RequirementLink> getInLinks() {
		return inLinks;
	}
	@Override
	public void setInLinks(LinkedList<RequirementLink> inLinks) {
		this.inLinks = inLinks;
	}
	@Override
	public LinkedList<RequirementLink> getOutLinks() {
		return outLinks;
	}
	@Override
	public void setOutLinks(LinkedList<RequirementLink> outLinks) {
		this.outLinks = outLinks;
	}
	@Override
	public void printInfo(){
		System.out.println("ID:"+this.Id+"\n"+"Name:"+this.name+"\n"+"Type:"+this.type+"\n"+"Layer:"+this.layer+"\n"+"Remark:"+this.remark+"\n");
	}

	@Override
	public String getCategory() {
		return this.category;
	}

	@Override
	public void setCategory(String category) {
		this.category = category;
	}


	@Override
	public String getFormalName() {
		String expression = "";
		if(this.name!=null){
			 expression = this.getName();
		}
		else{
			CommandPanel.logger.warning("Element's name is null!");
			return "null";
		}
		
		expression = expression.replaceAll(" ", "_");
		return expression.toLowerCase();
	}
	
	
	
	@Override
	public String getSingleFormalExpression() {
		//filter the redundant elements
		if(!this.getRemark().equals(InfoEnum.ElementRemark.NORMAL.name())){
			return "";
		}
		
		String expression = null;
		switch (InfoEnum.RequirementElementType.valueOf(this.getType())){
			case ACTOR:
				expression = "actor("+this.getFormalName()+").";
				break;
			case GOAL:
				expression = "goal("+this.getFormalName()+").";
				break;
			case TASK:
				expression = "task("+this.getFormalName()+").";
				break;
			case SOFTGOAL:
				expression = "softgoal("+this.getFormalName()+").";
				break;
			case DOMAIN_ASSUMPTION:
				expression = "d_assumption("+this.getFormalName()+").";
				break;
			case QUALITY_CONSTRAINT:
				expression = "q_constraint("+this.getFormalName()+").";
				break;
			case SECURITY_GOAL:
				// remove "(S)"
				expression = "sec_goal("+this.getFormalName()+").";
				break;
			case SECURITY_MECHANISM:
				// remove "(S)"
				expression = "sec_mechanism("+this.getFormalName()+").";
				break;
			default:
				expression = "";
				break;
		}
		
//		expression = expression.replaceAll(" ", "_");
		return expression;//.toLowerCase();
	}
	
	@Override
	public String getFormalExpressions() {
		return getSingleFormalExpression();
	}
	
}
