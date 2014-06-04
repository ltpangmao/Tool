package it.unitn.tlsaf.ds;

/**
 * @author litong30
 * 
 */
public class RequirementLink implements Link {

	private String Id;
	private String type;
	private RequirementElement source;
	private RequirementElement target;
	private RequirementElement attachment;
	private String remark = InfoEnum.LinkRemark.NORMAL.name(); //used for dealing with special cases
	
	//programming sugar...
	public String refine_type = "";
	public String trust_level = "";
	
	public String source_id = "";
	public String des_id = "";
	
	public RequirementLink() {
		super();
	}

	public RequirementLink(String type, RequirementElement source, RequirementElement target) {
		super();
		this.type = type;
		this.source = source;
		this.target = target;
	}

	public RequirementLink(String type, RequirementElement source, RequirementElement target,
			RequirementElement attachment) {
		super();
		this.type = type;
		this.source = source;
		this.target = target;
		this.attachment = attachment;
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
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public RequirementElement getSource() {
		return source;
	}

	@Override
	public void setSource(Element source) {
		this.source = (RequirementElement) source;
	}

	@Override
	public RequirementElement getTarget() {
		return target;
	}

	@Override
	public void setTarget(Element target) {
		this.target = (RequirementElement) target;
	}

	public RequirementElement getAttachment() {
		return attachment;
	}

	public void setAttachment(RequirementElement attachment) {
		this.attachment = attachment;
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
	public void printInfo() {
		System.out.println("ID:" + this.Id + "\n" + "Type:" + this.type + "\n" + "Source:" + this.source.getName()
				+ "\n" + "Target:" + this.target.getName() + "\n" + "Remark:" + this.remark + "\n");
	}

	@Override
	public String getFormalExpressions() {
		String expression = null;
		String source = "";
		String target = "";
		String attachment = "";

		//The redundant links are not taken into account during automatic inference
		if (this.getRemark().equals(InfoEnum.LinkRemark.REDUNDANT.name())) {
			return "";
		}

		//Get related information
		if(this.getSource() !=null){
			source = this.getSource().getFormalName();
		}
		if(this.getTarget() != null){
			target = this.getTarget().getFormalName();
		}
		if (this.getAttachment() != null) {
			attachment = this.getAttachment().getFormalName();
		}

		switch (InfoEnum.RequirementLinkType.valueOf(this.getType())) {
		case REFINE:
			expression = "refine(" + source + "," + target + ").";
			break;
		case AND_REFINE:
			expression = "and_refine(" + source + "," + target + ").";
			break;
		case OPERATIONALIZE:
			expression = "operationalize(" + source + "," + target + ").";
			break;
		case PREFERRED_TO:
			expression = "preferred_to(" + source + "," + target + ").";
			break;
		case DEPEND:
			expression = "depend(" + source + "," + target + "," + attachment + ").";
			break;
		
		case SUPPORT:
			expression = "support(" + source + "," + target + ").";
			break;
		case MAKE:
			expression = "make(" + source + "," + target + ").";
			break;
		case HELP:
			expression = "help(" + source + "," + target + ").";
			break;
		case HURT:
			expression = "hurt(" + source + "," + target + ").";
			break;
		case BREAK:
			expression = "break(" + source + "," + target + ").";
			break;
		case TRUST:
			expression = "trust(" + source + "," + target + "," + trust_level + ").";
			break;
		case USE:
			expression = "use(" + source + "," + target + ").";
			break;
		case MAINTAIN:
			expression = "maintain(" + source + "," + target + ").";
			break;
		case OWN:
			expression = "own(" + source + "," + target + ").";
			break;
			
		default:
			expression = "";
			break;
		}
		
		return expression;
	}

}
