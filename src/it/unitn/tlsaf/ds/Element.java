package it.unitn.tlsaf.ds;

import java.util.LinkedList;

public interface Element {
	
	public String getCategory();
	public void setCategory(String category);
	
	public String getId();
	public void setId(String id);

	public String getName();
	public void setName(String name);
	
	public String getType();
	public void setType(String type);
	
	public String getRemark();
	public void setRemark(String remark);

	public LinkedList<RequirementLink> getInLinks();
	public void setInLinks(LinkedList<RequirementLink> inLinks);
	
	public LinkedList<RequirementLink> getOutLinks();
	public void setOutLinks(LinkedList<RequirementLink> outLinks);

	public void printInfo();

	/**
	 * only here needs to tackle the white space and lower-case conversion.
	 * @return dlv formal name of this element
	 */
	public String getFormalName();
	
	/**
	 * @return dlv formal expression that declares this element
	 */
	public String getSingleFormalExpression();
	
	/**
	 * @return dlv formal expressions related to this element
	 */
	public String getFormalExpressions();
	
	
}
