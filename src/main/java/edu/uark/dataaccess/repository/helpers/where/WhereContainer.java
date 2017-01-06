package edu.uark.dataaccess.repository.helpers.where;

import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

public class WhereContainer {
	public WhereContainer addWhereClause(WhereClause additionalClause) {
		additionalClauses.add(additionalClause);
		return this;
	}
	
	@Override
	public String toString() {
		if ((initialClause == null) || !initialClause.validate(true)) {
			return StringUtils.EMPTY;
		}
		
		StringBuilder whereClause = new StringBuilder(SPACE);
		whereClause.append(WHERE_PREAMBLE);
		
		whereClause.append(initialClause.toString());
		for (WhereClause additionalClause : additionalClauses) {
			if (additionalClause.validate(false)) {
				whereClause.append(additionalClause.toString());
			}
		}
		
		return whereClause.toString();
	}
	
	private WhereClause initialClause;
	private LinkedList<WhereClause> additionalClauses;
	
	private static final String SPACE = " ";
	private static final String WHERE_PREAMBLE = "WHERE";
	
	public WhereContainer(WhereClause initialClause) {
		this.initialClause = initialClause;
		additionalClauses = new LinkedList<WhereClause>();
	}
}
