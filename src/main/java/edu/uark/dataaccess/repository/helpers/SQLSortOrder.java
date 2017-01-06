package edu.uark.dataaccess.repository.helpers;

public enum SQLSortOrder {
	ASC("ASC"),
	DESC("DESC");
	
	public String getLabel() {
		return label;
	}
	
	private final String label;
	
	private SQLSortOrder(String label) {
		this.label = label;
	}
}
