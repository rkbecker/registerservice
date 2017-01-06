package edu.uark.dataaccess.entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import edu.uark.dataaccess.repository.BaseRepositoryInterface;

public abstract class BaseEntity<T extends BaseEntity<T>> {
	protected UUID id;
	public UUID getId() {
		return this.id;
	}
	public BaseEntity<T> setId(UUID id) {
		this.id = id;
		this.onIdSet();
		
		return this;
	}
	
	protected boolean isNew;
	protected boolean getIsNew() {
		return this.isNew;
	}
	protected void setIsNew(boolean isNew) {
		this.isNew = isNew;
	}

	protected boolean isDirty;
	protected boolean getIsDirty() {
		return this.isDirty;
	}
	protected void setIsDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
	
	protected void propertyChanged(String fieldName) {
		if (!isDirty) {
			this.isDirty = true;
		}
		
		if (this.toUpdateFieldNames.indexOf(fieldName) < 0) {
			this.toUpdateFieldNames.add(fieldName);
		}
	}
	
	public boolean hasChanged() {
		return (this.isNew || (this.isDirty && (this.toUpdateFieldNames.size() > 0)));
	}

	protected LinkedList<String> toUpdateFieldNames;

	protected abstract void fillFromRecord(ResultSet rs) throws SQLException;
	protected abstract Map<String, Object> fillRecord(Map<String, Object> record);
	
	public void load(ResultSet rs) throws SQLException {
		this.isNew = false;
		this.isDirty = false;

		this.id = ((UUID) rs.getObject(BaseFieldNames.ID));

		this.fillFromRecord(rs);
	}

	protected void onIdSet() { }
	public void onLoadComplete() {}

	public void save() {
		this.repository.connectAndRun((connection) -> {
			try {
				if ((connection == null) || connection.isClosed()) {
					throw new IllegalArgumentException("connection");
				}
	
				this.save(connection);
			} catch (SQLException e) {
				//TODO: Replace with real logging...
				if (this.id != null) {
					System.out.printf("A SQLException occurred in single record save, %s.  %s\n", this.id.toString(), e.getMessage());
				} else {
					System.out.printf("A SQLException occurred in single record save.  %s\n", e.getMessage());
				}
			}
		});
	}

	public void save(Connection connection) throws SQLException {
		if (!this.isNew && this.isDirty) {
			this.updateRecord(connection);
		} else if (this.isNew) {
			this.insertNewRecord(connection);
		}

		this.isNew = false;
		this.isDirty = false;
		this.toUpdateFieldNames.clear();
	}

	private final static String INSERT_PREAMBLE = "INSERT INTO ";
	private final static String INSERT_VALUES_PREAMBLE = " VALUES ";

	private void insertNewRecord(Connection connection) throws SQLException {
		if ((this.id == null) || this.id.equals(new UUID(0,0))) {
			this.id = UUID.randomUUID();
			this.onIdSet();
		}
		Map<String, Object> record = new HashMap<String, Object>();
		record.put(BaseFieldNames.ID, this.id);

		record = this.fillRecord(record);

		PreparedStatement ps = this.buildInsertStatement(record, connection);
		ps.setQueryTimeout(120);
		ps.execute();
	}

	private PreparedStatement buildInsertStatement(Map<String, Object> record, Connection connection) throws SQLException {
		LinkedList<Object> values = new LinkedList<Object>();
		StringBuilder fieldsBuilder = new StringBuilder(" ( ");
		StringBuilder insertBuilder = new StringBuilder(INSERT_PREAMBLE).
			append(this.repository.getPrimaryTableName());
		StringBuilder paramsBuilder = new StringBuilder(INSERT_VALUES_PREAMBLE).append("( ");

		boolean appendFieldDivider = false;
		for (String key : record.keySet()) {
			if (appendFieldDivider) {
				fieldsBuilder.append(",");
				paramsBuilder.append(",");
			} else {
				appendFieldDivider = true;
			}

			fieldsBuilder.append(key);
			paramsBuilder.append("?");

			values.add(record.get(key));
		}

		fieldsBuilder.append(")");
		paramsBuilder.append(")");

		insertBuilder.append(fieldsBuilder).append(paramsBuilder);

		int keyIndex = 1;
		PreparedStatement ps = connection.prepareStatement(insertBuilder.toString());
		for (Object value : values) {
			ps.setObject(keyIndex++, value);
		}
		values.clear();
		
		return ps;
	}

	private final static String UPDATE_PREAMBLE = "UPDATE %s SET";

	private void updateRecord(Connection connection) throws SQLException {
		if (this.toUpdateFieldNames.size() <= 0) {
			return;
		}

		Map<String, Object> record = new HashMap<String, Object>();
		record = fillRecord(record);

		PreparedStatement ps = this.buildUpdateStatement(record, connection);
		ps.setQueryTimeout(120);
		ps.execute();
	}

	private PreparedStatement buildUpdateStatement(Map<String, Object> record, Connection connection) throws SQLException {
		LinkedList<Object> values = new LinkedList<Object>();
		StringBuilder updateBuilder = new StringBuilder(String.format(UPDATE_PREAMBLE, this.repository.getPrimaryTableName()));

		boolean appendFieldDivider = false;
		for (String key : this.toUpdateFieldNames) {
			if (appendFieldDivider) {
				updateBuilder.append(",");
			} else {
				appendFieldDivider = true;
			}

			updateBuilder.append(String.format(" %s = ?", key));
			values.add(record.get(key));
		}

		updateBuilder.append(" WHERE ").append(BaseFieldNames.ID).append(" = ?");
		
		int keyIndex = 1;
		PreparedStatement ps = connection.prepareStatement(updateBuilder.toString());
		for (Object value : values) {
			ps.setObject(keyIndex++, value);
		}
		ps.setObject(keyIndex, this.id);
		
		return ps;
	}
	
	private static final String DELETE_COMMAND_FORMAT = "DELETE FROM %s WHERE %s = ?";

	public void delete() {
		if (this.isNew) {
			return;
		}
		
		this.repository.connectAndRun((connection) -> {
			try {
				if ((connection == null) || connection.isClosed()) {
					throw new IllegalArgumentException("connection");
				}
	
				this.delete(connection);
			} catch (SQLException e) {
				//TODO: Replace with real logging...
				if (this.id != null) {
					System.out.printf("A SQLException occurred in single record delete, %s.  %s\n", this.id.toString(), e.getMessage());
				} else {
					System.out.printf("A SQLException occurred in single record delete.  %s\n", e.getMessage());
				}
			}
		});
	}
	
	public void delete(Connection connection) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(
			String.format(DELETE_COMMAND_FORMAT, this.repository.getPrimaryTableName(), BaseFieldNames.ID)
		);
		
		ps.setQueryTimeout(120);
		ps.setObject(1, this.id);
		ps.execute();
	}
	
	protected BaseRepositoryInterface<T> repository;
	
	protected void ConnectAndRun(Consumer<Connection> action) {
		this.repository.connectAndRun(action);
	}
	
	protected boolean stringHasChanged(String one, String two) {
		return !StringUtils.equals(one, two);
	}
	
	protected boolean stringHasChangedIgnoreCase(String one, String two) {
		return !StringUtils.equalsIgnoreCase(one, two);
	}
	
	protected boolean uuidHasChanged(UUID one, UUID two) {
		return !one.equals(two);
	}
	
	protected boolean localDateTimeHasChanged(LocalDateTime one, LocalDateTime two) {
		return !one.equals(two);
	}
	
	protected boolean byteArrayHasChanged(byte[] one, byte[] two) {
		boolean changed = (one.length != two.length);
		
		if (!changed) {
			for (int i = 0; i < one.length; i++) {
				if (one[i] != two[i]) {
					changed = true;
					break;
				}
			}
		}
		
		return changed;
	}

	@Override
	public int hashCode() {
		int result = 1;
		final int prime = 31;

		return ((prime * result) + ((this.id == null) ? 0 : this.id.hashCode()));
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		BaseEntity<?> other = ((BaseEntity<?>) obj);
		if (this.id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!this.id.equals(other.id)) {
			return false;
		}

		return true;
	}

	protected BaseEntity(BaseRepositoryInterface<T> repository) {
		this((new UUID(0, 0)), repository);
	}
	
	protected BaseEntity(UUID id, BaseRepositoryInterface<T> repository) {
		this.isNew = true;
		this.isDirty = true;
		
		this.id = id;
		this.repository = repository;
		this.toUpdateFieldNames = new LinkedList<String>();

		this.onIdSet();
	}
}
