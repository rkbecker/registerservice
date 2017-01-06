package edu.uark.dataaccess.repository.helpers.where;

import org.apache.commons.lang3.StringUtils;

import edu.uark.dataaccess.repository.DatabaseTable;
import edu.uark.dataaccess.repository.helpers.PostgreFunctionType;
import edu.uark.dataaccess.repository.helpers.SQLComparisonType;
import edu.uark.dataaccess.repository.helpers.SQLConditionalType;

public class WhereClause {
	private SQLConditionalType conditional;
	private DatabaseTable table;
	private String tableName;
	private PostgreFunctionType postgreFunction;
	private String fieldName;
	private SQLComparisonType comparison;
	
	public WhereClause conditional(SQLConditionalType conditional) {
		this.conditional = conditional;
		return this;
	}
	
	public WhereClause postgreFunction(PostgreFunctionType postgreFunction) {
		this.postgreFunction = postgreFunction;
		return this;
	}
	
	public WhereClause table(DatabaseTable table) {
		this.table = table;
		return this;
	}
	
	public WhereClause tableName(String tableName) {
		this.tableName = tableName;
		return this;
	}
	
	public WhereClause fieldName(String fieldName) {
		this.fieldName = fieldName;
		return this;
	}
	
	public WhereClause comparison(SQLComparisonType comparison) {
		this.comparison = comparison;
		return this;
	}
	
	@Override
	public String toString() {
		if (StringUtils.isBlank(tableName) && (table != DatabaseTable.NONE)) {
			tableName = table.getLabel();
		}
		
		StringBuilder whereClause = new StringBuilder(SPACE);
		
		if (conditional != SQLConditionalType.NONE) {
			whereClause.append(conditional.getLabel()).append(SPACE);
		}
		
		if ((postgreFunction != PostgreFunctionType.NONE) && (postgreFunction != PostgreFunctionType.ANY)) {
			whereClause.append(postgreFunction.getLabel()).append(OPEN_FUNCTION);
		}
		
		if (!StringUtils.isBlank(tableName)) {
			whereClause.append(tableName).append(TABLE_FIELD_SEPARATOR);
		}

		whereClause.append(fieldName);
		
		if ((postgreFunction != PostgreFunctionType.NONE) && (postgreFunction != PostgreFunctionType.ANY)) {
			whereClause.append(CLOSE_FUNCTION);
		}
		
		whereClause.append(SPACE).append(comparison.getLabel());
		
		if ((comparison != SQLComparisonType.IS_NULL) && (comparison != SQLComparisonType.IS_NOT_NULL)) {
			whereClause.append(SPACE);
			
			if (postgreFunction == PostgreFunctionType.ANY) {
				whereClause.append(postgreFunction.getLabel()).append(OPEN_FUNCTION);
			}

			whereClause.append(PARAMETER_PLACEHOLDER);

			if (postgreFunction == PostgreFunctionType.ANY) {
				whereClause.append(CLOSE_FUNCTION);
			}
		}
		
		return whereClause.toString();
	}
	
	public boolean validate(boolean initialClause) {
		boolean valid = true;
		
		if (!initialClause && (conditional == SQLConditionalType.NONE)) {
			valid = false;
		}
		
		if (valid && StringUtils.isBlank(fieldName)) {
			valid = false;
		}
		
		if (valid && (comparison == SQLComparisonType.NONE)) {
			valid = false;
		}
		
		return valid;
	}
	
	private static final String SPACE = " ";
	private static final String OPEN_FUNCTION = "(";
	private static final String CLOSE_FUNCTION = ")";
	private static final String TABLE_FIELD_SEPARATOR = ".";
	private static final String PARAMETER_PLACEHOLDER = "?";
	
	public WhereClause() {
		conditional = SQLConditionalType.NONE;
		postgreFunction = PostgreFunctionType.NONE;
		table = DatabaseTable.NONE;
		tableName = StringUtils.EMPTY;
		fieldName = StringUtils.EMPTY;
		comparison = SQLComparisonType.NONE;
	}
}
