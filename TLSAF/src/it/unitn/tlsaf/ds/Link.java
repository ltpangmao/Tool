package it.unitn.tlsaf.ds;

public interface Link {
	
	public String getId();
	public void setId(String id);

	public String getType();
	public void setType(String type);

	public Element getSource();
	public void setSource(Element source) ;

	public Element getTarget();
	public void setTarget(Element target);

	public String getRemark();
	public void setRemark(String remark);

	public void printInfo();
	
	public String getFormalExpression();
}
