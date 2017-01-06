package edu.uark.dataaccess.repository.helpers.orderby;

import org.apache.commons.lang3.StringUtils;

import edu.uark.dataaccess.repository.DatabaseTable;
import edu.uark.dataaccess.repository.helpers.SQLSortOrder;

public class OrderByContainer {
	private DatabaseTable orderByTable;
	private String orderByTableName;
	private String orderByFieldName;
	private SQLSortOrder direction;
	
	public OrderByContainer orderByTable(DatabaseTable orderByTable) {
		this.orderByTable = orderByTable;
		return this;
	}
	
	public OrderByContainer orderByTableName(String orderByTableName) {
		this.orderByTableName = orderByTableName;
		return this;
	}
	
	public OrderByContainer orderByFieldName(String orderByFieldName) {
		this.orderByFieldName = orderByFieldName;
		return this;
	}
	
	public OrderByContainer direction(SQLSortOrder direction) {
		this.direction = direction;
		return this;
	}
	
	@Override
	public String toString() {
		if (!validate()) {
			return StringUtils.EMPTY;
		}
		
		StringBuilder orderByClause = new StringBuilder();

		if (orderByTable != DatabaseTable.NONE) {
			orderByClause.append(orderByTable.getLabel()).
				append(TABLE_FIELD_SEPARATOR);
		} else {
			orderByClause.append(orderByTableName).
				append(TABLE_FIELD_SEPARATOR);
		}

		orderByClause.append(orderByFieldName).
			append(SPACE).
			append(direction.toString());

		return orderByClause.toString();
	}
	
	private boolean validate() {
		boolean valid = true;
		
		if ((orderByTable == DatabaseTable.NONE) && orderByTableName.equals(StringUtils.EMPTY)) {
			valid = false;
		}
		if (valid && orderByFieldName.equals(StringUtils.EMPTY)) {
			valid = false;
		}
		
		return valid;
	}
	
	private static final String SPACE = " ";
	private static final String TABLE_FIELD_SEPARATOR = ".";

	public OrderByContainer() {
		orderByTable = DatabaseTable.NONE;
		orderByTableName = StringUtils.EMPTY;
		orderByFieldName = StringUtils.EMPTY;
		direction = SQLSortOrder.ASC;
	}
}
