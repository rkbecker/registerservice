package edu.uark.dataaccess.repository.helpers.join;

import org.apache.commons.lang3.StringUtils;

import edu.uark.dataaccess.repository.DatabaseTable;
import edu.uark.dataaccess.repository.helpers.SQLComparisonType;

public class BaseJoinCriteria {
	private SQLJoinType type;
	private DatabaseTable joinOnTable;
	private String joinOnTableName;
	private String joinOnFieldName;
	private SQLComparisonType comparison;
	private DatabaseTable joinWithTable;
	private String joinWithTableName;
	private String joinWithFieldName;
	
	public BaseJoinCriteria joinType(SQLJoinType type) {
		this.type = type;
		return this;
	}
	
	public BaseJoinCriteria joinOnTable(DatabaseTable joinOnTable) {
		this.joinOnTable = joinOnTable;
		return this;
	}
	
	public BaseJoinCriteria joinOnTableName(String joinOnTableName) {
		this.joinOnTableName = joinOnTableName;
		return this;
	}
	
	public BaseJoinCriteria joinOnFieldName(String joinOnFieldName) {
		this.joinOnFieldName = joinOnFieldName;
		return this;
	}
	
	public BaseJoinCriteria comparison(SQLComparisonType comparison) {
		this.comparison = comparison;
		return this;
	}
	
	public BaseJoinCriteria joinWithTable(DatabaseTable joinWithTable) {
		this.joinWithTable = joinWithTable;
		return this;
	}
	
	public BaseJoinCriteria joinWithTableName(String joinWithTableName) {
		this.joinWithTableName = joinWithTableName;
		return this;
	}
	
	public BaseJoinCriteria joinWithFieldName(String joinWithFieldName) {
		this.joinWithFieldName = joinWithFieldName;
		return this;
	}
	
	@Override
	public String toString() {
		if (!validate()) {
			return StringUtils.EMPTY;
		}
		
		if (joinOnTable != DatabaseTable.NONE) {
			joinOnTableName = joinOnTable.getLabel();
		}
		if (joinWithTable != DatabaseTable.NONE) {
			joinWithTableName = joinWithTable.getLabel();
		}
		
		return ((new StringBuilder(SPACE)).
			append(type.getLabel()).
			append(JOIN_KEYWORD).
			append(joinOnTableName).
			append(ON_KEYWORD).
			append(joinOnTableName).
			append(TABLE_FIELD_SEPARATOR).
			append(joinOnFieldName).
			append(SPACE).
			append(comparison.getLabel()).
			append(SPACE).
			append(joinWithTableName).
			append(TABLE_FIELD_SEPARATOR).
			append(joinWithFieldName)).
			toString();
	}
	
	private boolean validate() {
		boolean valid = true;
		
		if ((type == SQLJoinType.NONE) && joinOnTable.equals(StringUtils.EMPTY)) {
			valid = false;
		}
		if (valid && joinOnFieldName.equals(StringUtils.EMPTY)) {
			valid = false;
		}
		if (valid && ((comparison == SQLComparisonType.NONE) || (comparison == SQLComparisonType.IS_NULL) || (comparison == SQLComparisonType.IS_NOT_NULL))) {
			valid = false;
		}
		if (valid && (joinWithTable == DatabaseTable.NONE) && (joinWithTableName == StringUtils.EMPTY)) {
			valid = false;
		}
		if (valid && joinWithFieldName.equals(StringUtils.EMPTY)) {
			valid = false;
		}
		
		return valid;
	}
	
	private static final String SPACE = " ";
	private static final String ON_KEYWORD = " ON ";
	private static final String JOIN_KEYWORD = " JOIN ";
	private static final String TABLE_FIELD_SEPARATOR = ".";

	public BaseJoinCriteria() {
		type = SQLJoinType.NONE;
		joinOnTable = DatabaseTable.NONE;
		joinOnTableName = StringUtils.EMPTY;
		joinOnFieldName = StringUtils.EMPTY;
		comparison = SQLComparisonType.NONE;
		joinWithTable = DatabaseTable.NONE;
		joinWithTableName = StringUtils.EMPTY;
		joinWithFieldName = StringUtils.EMPTY;
	}
}
