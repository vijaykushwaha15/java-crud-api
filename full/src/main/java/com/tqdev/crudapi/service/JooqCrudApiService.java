package com.tqdev.crudapi.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import com.tqdev.crudapi.service.definition.DatabaseDefinition;
import com.tqdev.crudapi.service.record.ListResponse;
import com.tqdev.crudapi.service.record.Record;
import com.tqdev.crudapi.spatial.SpatialDSL;

public class JooqCrudApiService extends BaseCrudApiService
		implements CrudApiService, JooqConditions, JooqColumnSelector, JooqOrdering, JooqPagination {

	private DSLContext dsl;

	public JooqCrudApiService(DSLContext dsl) {
		this.dsl = dsl;
		SpatialDSL.registerDataTypes(dsl);
		updateDefinition();
	}

	@Override
	public String create(String table, Record record, Params params) {
		sanitizeRecord(table, record);
		Table<?> t = DSL.table(DSL.name(table));
		LinkedHashMap<Field<?>, Object> columns = columnValues(table, record, params, definition);
		Field<?> pk = DSL.field(definition.get(table).getPk());
		Object result = dsl.insertInto(t).set(columns).returning(pk).fetchOne();
		return result == null ? null : String.valueOf(result);
	}

	@Override
	public Record read(String table, String id, Params params) {
		Table<?> t = DSL.table(DSL.name(table));
		ArrayList<Field<?>> columns = columnNames(table, params, definition);
		Field<Object> pk = DSL.field(definition.get(table).getPk());
		org.jooq.Record record = dsl.select(columns).from(t).where(pk.eq(id)).fetchOne();
		return record == null ? null : Record.valueOf(record.intoMap());
	}

	@Override
	public Integer update(String table, String id, Record record, Params params) {
		sanitizeRecord(table, record);
		Table<?> t = DSL.table(DSL.name(table));
		LinkedHashMap<Field<?>, Object> columns = columnValues(table, record, params, definition);
		Field<Object> pk = DSL.field(definition.get(table).getPk());
		return dsl.update(t).set(columns).where(pk.eq(id)).execute();
	}

	@Override
	public Integer delete(String table, String id, Params params) {
		Table<?> t = DSL.table(DSL.name(table));
		Field<Object> pk = DSL.field(definition.get(table).getPk());
		return dsl.deleteFrom(t).where(pk.eq(id)).execute();
	}

	@Override
	public ListResponse list(String table, Params params) {
		Table<?> t = DSL.table(DSL.name(table));
		ArrayList<Field<?>> columns = columnNames(table, params, definition);
		ArrayList<Record> records = new ArrayList<>();
		for (org.jooq.Record record : dsl.select(columns).from(t).where(conditions(params)).orderBy(ordering(params))
				.limit(offset(params), numberOfRows(params)).fetch()) {
			records.add(Record.valueOf(record.intoMap()));
		}
		return new ListResponse(records.toArray(new Record[records.size()]));
	}

	@Override
	public boolean updateDefinition() {
		DatabaseDefinition definition = DatabaseDefinition.fromValue(dsl);
		if (definition != null) {
			this.definition = definition;
			return true;
		}
		return false;
	}

}