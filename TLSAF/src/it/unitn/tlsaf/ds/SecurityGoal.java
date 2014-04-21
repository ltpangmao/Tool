package it.unitn.tlsaf.ds;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SecurityGoal extends RequirementElement {
	private boolean criticality;
	private boolean non_deterministic;

	private String asset;
	private String security_attribute;
	private String interval;
	private String importance;
	// programming sugar...
	public Actor owner = null;
	// for next layer duplication
	public SecurityGoal next_layer_copy = null;
	// for best path generation
	public SecurityGoal parent = null;
	public RequirementLink parent_link = null;

	public SecurityGoal() {
		super();
		this.setType(InfoEnum.RequirementElementType.SECURITY_GOAL.name());
		// TODO Auto-generated constructor stub
	}

	public SecurityGoal(String name, String type, String layer, LinkedList<RequirementLink> inLinks,
			LinkedList<RequirementLink> outLinks) {
		super(name, type, layer, inLinks, outLinks);
		// TODO Auto-generated constructor stub
	}

	public SecurityGoal(String name, String type, String layer) {
		super(name, type, layer);
		// TODO Auto-generated constructor stub
	}

	public SecurityGoal(String importance, String security_attribute, String asset, String interval, String type,
			String layer) {
		super((importance + " " + security_attribute + " [" + asset + ", " + interval + "]").replaceAll("\\_", " "),
				type, layer);
		this.setSecurityAttribute(security_attribute);
		this.setAsset(asset);
		this.setInterval(interval);
		this.setImportance(importance);
	}

	public boolean isCriticality() {
		return criticality;
	}

	public void setCriticality(boolean criticality) {
		this.criticality = criticality;
	}
	
	public boolean isNon_deterministic() {
		return non_deterministic;
	}

	public void setNon_deterministic(boolean non_deterministic) {
		this.non_deterministic = non_deterministic;
	}

	public String getAsset() {
		return asset;
	}

	public void setAsset(String asset) {
		this.asset = asset;
		// resetName();
	}

	public String getSecurityAttribute() {
		return security_attribute;
	}

	public void setSecurityAttribute(String security_attribute) {
		this.security_attribute = security_attribute;
		// resetName();
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
		// resetName();
	}

	public String getImportance() {
		return importance;
	}

	public void setImportance(String importance) {
		this.importance = importance;
		// resetName();
	}

	// avoid to use this function
	public void resetName() {
		this.setName(this.importance + " " + this.security_attribute + " [" + this.asset + ", " + this.interval + "]");
	}

	public void extractInfoFromName() {
		String sg = this.getName();
		// sg = sg.replaceAll("\\(S\\)", "");
		// first part
		String pre_sg = sg.substring(0, sg.indexOf('[')).trim();
		List<String> pre_list = Arrays.asList(pre_sg.split("\\s+"));
		this.setImportance(pre_list.get(0));
		if (pre_list.size() == 2) {
			this.setSecurityAttribute(pre_list.get(1));
		} else if (pre_list.size() == 3) {
			this.setSecurityAttribute(pre_list.get(1) + " " + pre_list.get(2));
		} else {
		}
		// second part
		String post_sg = sg.substring(sg.indexOf('[') + 1, sg.length() - 1);
		List<String> post_list = Arrays.asList(post_sg.split(","));
		this.setAsset(post_list.get(0).trim());
		this.setInterval(post_list.get(1).trim());
	}

	
	/**
	 * As a security goal has a structured name in its graphic representation,
	 * which cannot be used directly in the DLV reasoning (all others can), we
	 * have this additional formal name to support related reasoning.
	 * 
	 * @return formal name
	 */
	@Override
	public String getFormalName() {
		String expression = this.getImportance() + "_" + this.getSecurityAttribute() + "_" + this.getAsset() + "_"
				+ this.getInterval();
		expression = expression.replaceAll(" ", "_");

		return expression.toLowerCase();
	}
	
	
	
	@Override
	public String getFormalExpressions() {
		//String expression = "sec_goal(" + this.getImportance() + "_" + this.getSecurityAttribute() + "_"
			//	+ this.getAsset() + "_" + this.getInterval() + ").\n";
		String expression = this.getSingleFormalExpression() + "\n";
		
		if (this.isCriticality() == true) {
			expression += "is_critical(" + this.getFormalName() + ").\n"; 
		}
		
		expression += "sec_attribute(" + this.getSecurityAttribute() + ").\n";
		expression += "asset(" + this.getAsset() + ").\n";
		expression += "importance(" + this.getImportance() + ").\n";
		expression += "interval(" + this.getInterval() + ").\n"; 
		expression += "has_properties(" + getFormalName() + "," + this.getImportance() + ","
				+ this.getSecurityAttribute() + "," + this.getAsset() + "," + this.getInterval() + ").";

		//also output the ownership of this security goal, which maybe redundant, but helpful in exhaustive security goals analysis.
		if(this.owner!=null){
			expression += "has(" + this.owner.getFormalName()+"," +this.getFormalName()+ ").\n";
		}
		
		expression = expression.replaceAll(" ", "_");
		return expression.toLowerCase();
	}



	@Override
	public void printInfo() {
		System.out.println("ID:" + this.getId() + "\n" + "Name:" + this.getName() + "\n" + "Type:" + this.getType()
				+ "\n" + "Layer:" + this.getLayer() + "\n" + "Remark:" + this.getRemark() + "\n" + "Criticality:"
				+ this.isCriticality() + "\n" + "Importance:" + this.getImportance() + "\n" + "Security Attribute:"
				+ this.getSecurityAttribute() + "\n" + "Asset:" + this.getAsset() + "\n" + "Interval:"
				+ this.getInterval());
	}

}
