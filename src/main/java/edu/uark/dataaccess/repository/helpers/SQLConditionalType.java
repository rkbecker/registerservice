package edu.uark.dataaccess.repository.helpers;

public enum SQLConditionalType {
	NONE(""),
	AND("AND"),
	OR("OR");
	
	public String getLabel() {
		return label;
	}
	
	private final String label;
	
	private SQLConditionalType(String label) {
		this.label = label;
	}
}
