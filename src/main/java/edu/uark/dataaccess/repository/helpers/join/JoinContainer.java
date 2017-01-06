package edu.uark.dataaccess.repository.helpers.join;

import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

public class JoinContainer {
	public JoinContainer addAdditionalCriteria(AdditionalJoinCriteria additionalCriteria) {
		this.additionalCriteria.add(additionalCriteria);
		return this;
	}
	
	@Override
	public String toString() {
		if (!validate()) {
			return StringUtils.EMPTY;
		}
		
		String baseCriteriaAsString = baseCriteria.toString();
		if (StringUtils.isBlank(baseCriteriaAsString)) {
			return StringUtils.EMPTY;
		}

		StringBuilder join = new StringBuilder(baseCriteriaAsString);
		for (AdditionalJoinCriteria ajc : additionalCriteria) {
			join.append(ajc.toString());
		}
		
		return join.toString();
	}
	
	private boolean validate() {
		return (baseCriteria != null);
	}
	
	private BaseJoinCriteria baseCriteria;
	private LinkedList<AdditionalJoinCriteria> additionalCriteria;
	
	public JoinContainer(BaseJoinCriteria baseCriteria) {
		this.baseCriteria = baseCriteria;
		additionalCriteria = new LinkedList<AdditionalJoinCriteria>();
	}
}
