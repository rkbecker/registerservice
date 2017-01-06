package edu.uark.dataaccess.repository;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.heroku.sdk.jdbc.DatabaseUrl;

import edu.uark.dataaccess.entities.BaseEntity;
import edu.uark.dataaccess.entities.BaseFieldNames;
import edu.uark.dataaccess.repository.helpers.PostgreFunctionType;
import edu.uark.dataaccess.repository.helpers.SQLComparisonType;
import edu.uark.dataaccess.repository.helpers.join.JoinContainer;
import edu.uark.dataaccess.repository.helpers.orderby.OrderByContainer;
import edu.uark.dataaccess.repository.helpers.where.WhereClause;
import edu.uark.dataaccess.repository.helpers.where.WhereContainer;

public abstract class BaseRepository<T extends BaseEntity<T>> implements BaseRepositoryInterface<T> {
	public int count() {
		return countWhere(null, null);
	}

	public T get(UUID id) {
		return firstOrDefaultWhere(
			null,
			(new WhereContainer(
				(new WhereClause()).
					table(primaryTable).
					fieldName(BaseFieldNames.ID).
					comparison(SQLComparisonType.EQUALS)
			)),
			null,
			(ps) -> {
				try {
					ps.setObject(1, id);
				} catch (SQLException e) {}

				return ps;
			}
		);
	}
	
	public Collection<T> all() {
		return allWhere(null, null, null, INVALID_INDEX, INVALID_INDEX, null);
	}
	
	public boolean exists(UUID id) {
		return existsWhere(
			null,
			(new WhereContainer(
				(new WhereClause()).
					table(primaryTable).
					fieldName(BaseFieldNames.ID).
					comparison(SQLComparisonType.EQUALS)
			)),
			null,
			(ps) -> {
				try {
					ps.setObject(1, id);
				} catch (SQLException e) {}

				return ps;
			}
		);
	}
	
	public void saveMany(Collection<T> allToSave) {
		UUID lastAttemptedId = new UUID(0, 0);
		
		try {
			Connection connection = openConnection();
			
			try {
				connection.setAutoCommit(false);
				
				for (T model : allToSave) {
					lastAttemptedId = model.getId();
					model.save(connection);
				}

				connection.commit();
			} catch (SQLException e) {
				//TODO: Replace with real logging...
				System.out.printf("A SQLException occurred in save many. The last attempted ID was %s. %s\n", lastAttemptedId.toString(), e.getMessage());
			} finally {
				connection.setAutoCommit(true);
			}
		} catch (SQLException e) {
			//TODO: Replace with real logging...
			System.out.printf("A SQLException occurred in save many while attempting a connection. %s\n", e.getMessage());
		} catch (URISyntaxException e) {
			System.out.printf("A URISyntaxException occurred in save many while attempting a connection. %s\n", e.getMessage());
		}
	}
	
	public Collection<T> getMany(Collection<UUID> ids) {
		return allWhere(
			(new WhereContainer(
				(new WhereClause()).
					table(primaryTable).
					fieldName(BaseFieldNames.ID).
					comparison(SQLComparisonType.EQUALS).
					postgreFunction(PostgreFunctionType.ANY)
			)),
			(ps) -> {
				try {
					ps.setArray(1, ps.getConnection().createArrayOf("uuid", ids.toArray(new UUID[ids.size()])));
				} catch (SQLException e) {}
				
				return ps;
			}
		);
	}
	
	public void deleteMany(Collection<T> allToDelete) {
		UUID lastAttemptedId = new UUID(0, 0);
		
		try {
			Connection connection = openConnection();
			
			try {
				connection.setAutoCommit(false);
				
				for (T model : allToDelete) {
					lastAttemptedId = model.getId();
					model.delete(connection);
				}

				connection.commit();
			} catch (SQLException e) {
				//TODO: Replace with real logging...
				System.out.printf("A SQLException occurred in delete many. The last attempted ID was %s. %s\n", lastAttemptedId.toString(), e.getMessage());
			} finally {
				connection.setAutoCommit(true);
			}
		} catch (SQLException e) {
			//TODO: Replace with real logging...
			System.out.printf("A SQLException occurred in delete many while attempting a connection. %s\n", e.getMessage());
		} catch (URISyntaxException e) {
			System.out.printf("A URISyntaxException occurred in delete many while attempting a connection. %s\n", e.getMessage());
		}
	}
	
	public Collection<T> inRange(int limit, int offset) {
		return allWhere(null, null, null, limit, offset, null);
	}

	
	public void connectAndRun(Consumer<Connection> action) {
		if (action == null) {
			return;
		}

		try (Connection connection = openConnection()) {
			action.accept(connection);
		} catch (SQLException e) {
			//TODO: Replace with real logging...
			System.out.printf("A SQLException occurred in connect and run. %s\n", e.getMessage());
		} catch (URISyntaxException e) {
			System.out.printf("A URISyntaxException occurred in connect and run. %s\n", e.getMessage());
		}
	}

	public String getPrimaryTableName() {
		return primaryTable.getLabel();
	}
	
	protected abstract T createOne();

	protected DatabaseTable primaryTable;
	
	protected String getDefaultProjection() {
		return String.format("%s.*", primaryTable.getLabel());
	}

	protected Connection openConnection() throws SQLException, URISyntaxException {
		return DatabaseUrl.extract().getConnection();
	}
	
	// Select first or default
	protected T firstOrDefaultWhere(WhereContainer where, UnaryOperator<PreparedStatement> setArgsOperator) {
		return firstOrDefaultWhere(null, where, null, setArgsOperator);
	}
	
	protected T firstOrDefaultWhere(JoinContainer[] joins, WhereContainer where, UnaryOperator<PreparedStatement> setArgsOperator) {
		return firstOrDefaultWhere(joins, where, null, setArgsOperator);
	}
	
	protected T firstOrDefaultWhere(JoinContainer[] joins, WhereContainer where, OrderByContainer[] orderBy, UnaryOperator<PreparedStatement> setArgsOperator) {
		return selectFirstOrDefault(
			selectQuery((new String[] { getDefaultProjection() }), joins, where, orderBy, 1, INVALID_INDEX),
			setArgsOperator
		);
	}

	private T selectFirstOrDefault(String query, UnaryOperator<PreparedStatement> setArgsOperator) {
		T model = null;

		try (Connection connection = openConnection()) {
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setQueryTimeout(120);
			
			if (setArgsOperator != null) {
				ps = setArgsOperator.apply(ps);
			}
			
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				model = createOne();
				model.load(rs);
			}
		} catch (SQLException e) {
			//TODO: Replace with real logging...
			System.out.printf("A SQLException occurred in first or default query. %s\n", e.getMessage());
		} catch (URISyntaxException e) {
			System.out.printf("A URISyntaxException occurred in first or default query. %s\n", e.getMessage());
		}
		
		if (model != null) {
			model.onLoadComplete();
		}

		return model;
	}

	// Select all
	protected Collection<T> allWhere(JoinContainer[] joins, UnaryOperator<PreparedStatement> setArgsOperator) {
		return allWhere(joins, null, null, INVALID_INDEX, INVALID_INDEX, setArgsOperator);
	}
	
	protected Collection<T> allWhere(WhereContainer where, UnaryOperator<PreparedStatement> setArgsOperator) {
		return allWhere(null, where, null, INVALID_INDEX, INVALID_INDEX, setArgsOperator);
	}
	
	protected Collection<T> allWhere(WhereContainer where, int limit, int offset, UnaryOperator<PreparedStatement> setArgsOperator) {
		return allWhere(null, where, null, limit, offset, setArgsOperator);
	}
	
	protected Collection<T> allWhere(JoinContainer[] joins, WhereContainer where, UnaryOperator<PreparedStatement> setArgsOperator) {
		return allWhere(joins, where, null, INVALID_INDEX, INVALID_INDEX, setArgsOperator);
	}
	
	protected Collection<T> allWhere(WhereContainer where, OrderByContainer[] orderBy, UnaryOperator<PreparedStatement> setArgsOperator) {
		return allWhere(null, where, orderBy, INVALID_INDEX, INVALID_INDEX, setArgsOperator);
	}
	
	protected Collection<T> allWhere(JoinContainer[] joins, WhereContainer where, int limit, int offset, UnaryOperator<PreparedStatement> setArgsOperator) {
		return allWhere(joins, where, null, limit, offset, setArgsOperator);
	}
	
	protected Collection<T> allWhere(WhereContainer where, OrderByContainer[] orderBy, int limit, int offset, UnaryOperator<PreparedStatement> setArgsOperator) {
		return allWhere(null, where, orderBy, limit, offset, setArgsOperator);
	}
	
	protected Collection<T> allWhere(JoinContainer[] joins, WhereContainer where, OrderByContainer[] orderBy, UnaryOperator<PreparedStatement> setArgsOperator) {
		return allWhere(joins, where, null, INVALID_INDEX, INVALID_INDEX, setArgsOperator);
	}
	
	protected Collection<T> allWhere(JoinContainer[] joins, WhereContainer where, OrderByContainer[] orderBy, int limit, int offset, UnaryOperator<PreparedStatement> setArgsOperator) {
		return selectAll(
			selectQuery((new String[] { getDefaultProjection() }), joins, where, orderBy, limit, offset),
			setArgsOperator
		);
	}

	private Collection<T> selectAll(String query, UnaryOperator<PreparedStatement> setArgsOperator) {
		LinkedList<T> results = new LinkedList<T>();

		try (Connection connection = openConnection()) {
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setQueryTimeout(120);
			
			if (setArgsOperator != null) {
				ps = setArgsOperator.apply(ps);
			}
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				T model = createOne();
				model.load(rs);
				
				results.add(model);
			}
		} catch (SQLException e) {
			//TODO: Replace with real logging...
			System.out.printf("A SQLException occurred in all query. %s\n", e.getMessage());
		} catch (URISyntaxException e) {
			System.out.printf("A URISyntaxException occurred in all query. %s\n", e.getMessage());
		}
		
		for (T result : results) {
			result.onLoadComplete();
		}

		return results;
	}

	// Exists
	protected boolean existsWhere(WhereContainer where, UnaryOperator<PreparedStatement> setArgsOperator) {
		return existsWhere(null, where, null, setArgsOperator);
	}
	
	protected boolean existsWhere(JoinContainer[] joins, WhereContainer where, UnaryOperator<PreparedStatement> setArgsOperator) {
		return existsWhere(joins, where, null, setArgsOperator);
	}
	
	protected boolean existsWhere(JoinContainer[] joins, WhereContainer where, OrderByContainer[] orderBy, UnaryOperator<PreparedStatement> setArgsOperator) {
		return exists(
			existsQuery(joins, where, orderBy, 1, INVALID_INDEX),
			setArgsOperator
		);
	}

	private boolean exists(String query, UnaryOperator<PreparedStatement> setArgsOperator) {
		boolean exists = false;

		try (Connection connection = openConnection()) {
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setQueryTimeout(120);
			
			if (setArgsOperator != null) {
				ps = setArgsOperator.apply(ps);
			}
			
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				exists = rs.getBoolean(1);
			}
		} catch (SQLException e) {
			//TODO: Replace with real logging...
			System.out.printf("A SQLException occurred in exists query. %s\n", e.getMessage());
		} catch (URISyntaxException e) {
			System.out.printf("A URISyntaxException occurred in exists query. %s\n", e.getMessage());
		}

		return exists;
	}
	
	//Count
	protected int countWhere(WhereContainer where, UnaryOperator<PreparedStatement> setArgsOperator) {
		return count(
			selectQuery(
				(new String[] { COUNT_PROJECTION }),
				null, where, null, INVALID_INDEX, INVALID_INDEX
			),
			setArgsOperator
		);
	}
	
	private int count(String query, UnaryOperator<PreparedStatement> setArgsOperator) {
		int count = 0;

		try (Connection connection = openConnection()) {
			PreparedStatement ps = connection.prepareStatement(query);

			ps.setQueryTimeout(120);
			if (setArgsOperator != null) {
				ps = setArgsOperator.apply(ps);
			}
			
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (SQLException e) {
			//TODO: Replace with real logging...
			System.out.printf("A SQLException occurred in count query. %s\n", e.getMessage());
		} catch (URISyntaxException e) {
			System.out.printf("A URISyntaxException occurred in count query. %s\n", e.getMessage());
		}
		
		return count;
	}
	
	//Delete
	protected void deleteWhere(WhereContainer where, UnaryOperator<PreparedStatement> setArgsOperator) {
		delete(deleteCommand(where), setArgsOperator);
	}
	
	private void delete(String query, UnaryOperator<PreparedStatement> setArgsOperator) {
		try (Connection connection = openConnection()) {
			PreparedStatement ps = connection.prepareStatement(query);

			ps.setQueryTimeout(300);
			if (setArgsOperator != null) {
				ps = setArgsOperator.apply(ps);
			}
			
			ps.execute();
		} catch (SQLException e) {
			//TODO: Replace with real logging...
			System.out.printf("A SQLException occurred in delete command. %s\n", e.getMessage());
		} catch (URISyntaxException e) {
			System.out.printf("A URISyntaxException occurred in delete command. %s\n", e.getMessage());
		}
	}
	
	private String selectQuery(String[] projection, JoinContainer[] joins, WhereContainer where, OrderByContainer[] orderBy, int limit, int offset) {   
		StringBuilder selectQuery = new StringBuilder().
			append("SELECT ").
			append(String.join(",", projection)).
			append(fromAndWhereClause(joins, where, orderBy, limit, offset));

		return selectQuery.toString();
	}

	//Helper methods
	private String existsQuery(JoinContainer[] joins, WhereContainer where, OrderByContainer[] orderBy, int limit, int offset) {   
		StringBuilder existsQuery = new StringBuilder().
			append("SELECT EXISTS (SELECT 1").
			append(fromAndWhereClause(joins, where, orderBy, limit, offset)).
			append(")");

		return existsQuery.toString();
	}

	private String deleteCommand(WhereContainer where) {
		StringBuilder deleteCommand = new StringBuilder().
			append("DELETE ").
			append(fromAndWhereClause(null, where, null, INVALID_INDEX, INVALID_INDEX));

		return deleteCommand.toString();
	}

	private String fromAndWhereClause(JoinContainer[] joins, WhereContainer where, OrderByContainer[] orderBy, int limit, int offset) {
		StringBuilder fromAndWhereClause = new StringBuilder().
			append(" FROM ").
			append(getPrimaryTableName());

		if ((joins != null) && (joins.length > 0)) {
			for (JoinContainer joinTo : joins) {
				fromAndWhereClause.append(joinTo.toString());
			}
		}

		if (where != null) {
			fromAndWhereClause.append(where.toString());
		}

		if ((orderBy != null) && (orderBy.length > 0)) {
			fromAndWhereClause.append(" ORDER BY ").
				append(
					Arrays.stream(orderBy).
						map(o -> o.toString()).
						collect(Collectors.joining(", "))
				);
		}

		if (limit >= 0) {
			fromAndWhereClause.append(" LIMIT ").append(limit);
		}

		if (offset >= 0) {
			fromAndWhereClause.append(" OFFSET ").append(offset);
		}

		return fromAndWhereClause.toString();
	}

	private static final int INVALID_INDEX = -1;
	private static final String COUNT_PROJECTION = "COUNT(*)";
	
	protected BaseRepository(DatabaseTable primaryTable) {
		this.primaryTable = primaryTable;
	}
}
