package edu.uark.dataaccess.repository.helpers;

public enum SQLComparisonType {
	NONE(""),
	EQUALS("="),
	LESS_THAN("<"),
	LESS_THAN_OR_EQUAL_TO("<="),
	GREATER_THAN(">"),
	GREATER_THAN_OR_EQUAL_TO(">="),
	NOT_EQUAL("!="),
	IS_NULL("IS NULL"),
	IS_NOT_NULL("IS NOT NULL");
	
	public String getLabel() {
		return label;
	}
	
	private final String label;
	
	private SQLComparisonType(String label) {
		this.label = label;
	}
}
