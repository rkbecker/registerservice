package edu.uark.dataaccess.repository.helpers;

public enum PostgreFunctionType {
	NONE(""),
	LOWER("lower"),
	UPPER("upper"),
	ANY("any");
	
	public String getLabel() {
		return label;
	}
	
	private final String label;
	
	private PostgreFunctionType(String label) {
		this.label = label;
	}
}
