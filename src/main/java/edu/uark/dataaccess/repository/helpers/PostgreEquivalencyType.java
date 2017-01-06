package edu.uark.dataaccess.repository.helpers;

public enum PostgreEquivalencyType {
	NONE(""),
	CURRENT_DATE("CURRENT_DATE"),
	CURRENT_TIMESTAMP("CURRENT_TIMESTAMP");
	
	public String getLabel() {
		return label;
	}
	
	private final String label;
	
	private PostgreEquivalencyType(String label) {
		this.label = label;
	}
}
