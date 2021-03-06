package com.tqdev.crudapi.column.reflection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Set;

import org.jooq.DSLContext;
import org.jooq.Table;

public class DatabaseReflection {

	private DSLContext dsl;

	private LinkedHashMap<String, ReflectedTable> tables;

	private String tablePrefix;

	public DatabaseReflection(DSLContext dsl) {
		this.dsl = dsl;
		this.tablePrefix = findTablePrefix();
	}

	public boolean exists(String tableName) {
		return tables.containsKey(tableName);
	}

	public ReflectedTable get(String tableName) {
		return tables.get(tableName);
	}

	private String findTablePrefix() {
		Connection connection = dsl.configuration().connectionProvider().acquire();
		String catalog = null, schema = null;
		try {
			catalog = connection.getCatalog();
			schema = connection.getSchema();
		} catch (SQLException e) {
			// error on table prefix
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				// ignore
			}
		}
		String prefix = "";
		if (catalog != null) {
			prefix += "\"" + catalog + "\".";
		}
		if (schema != null) {
			prefix += "\"" + schema + "\".";
		}
		return prefix;
	}

	public void update() {
		tables = new LinkedHashMap<>();
		for (Table<?> table : dsl.meta().getTables()) {
			if (!(table.toString().startsWith(tablePrefix))) {
				// table not in current catalog or schema
				continue;
			}
			tables.put(table.getName(), new ReflectedTable(table));
		}
	}

	public Set<String> getTableNames() {
		return tables.keySet();
	}
}
