package com.websoft.vantium.mobilescanner.manage.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public abstract class DBTable {

	static final class ColumnStruct {
		String name;
		String type;

		public ColumnStruct(String name, String type) {
			this.name = name;
			this.type = type;
		}
	}

	static final class ColumnType {
		public static final String COLUMN_TYPE_PRIMARY = "INTEGER PRIMARY KEY";
		public static final String COLUMN_TYPE_TEXT = "TEXT";
		public static final String COLUMN_TYPE_INTEGER = "INTEGER";
	}

	static final class QueryField {
		String name;
		String value;

		public QueryField(String name, String value) {
			this.name = name;
			this.value = value;
		}

		String getQuery() {
			return name + "='" + value + "'";
		}
	}

	void createTable(SQLiteDatabase db) {
		String sql = createSQL();
		if (sql != null) {
			db.execSQL(sql);
		}
	}

	void upgradeTable(SQLiteDatabase db) {
		// delete and recreate
		deleteTable(db);
		createTable(db);
	}

	void deleteTable(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + getTableName());
	}

	Object[] getAllObjects(SQLiteDatabase db) {
		Object[] objects = null;
		String query = "SELECT * FROM " + getTableName();
		Cursor cursor = db.rawQuery(query, null);
		if (cursor != null) {
			objects = db2Objects(cursor);
			cursor.close();
		}
		return objects;
	}

	Object[] getObjects(SQLiteDatabase db, String[] columns, String[] keys) {
		Object[] objects = null;
		int count = columns.length;
		String query = "SELECT * FROM " + getTableName() +" WHERE ";
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				query += " AND ";
			}
			query += columns[i] + "='" + keys[i] + "'";
		}

		Cursor cursor = db.rawQuery(query, null);
		if (cursor != null) {
			objects = db2Objects(cursor);
			cursor.close();
		}

		return objects;
	}

	private String createSQL() {
		List<ColumnStruct> columns = new ArrayList<ColumnStruct>();
		buildColumns(columns);

		final int size = columns.size();
		if (size > 0) {
			StringBuilder builder = new StringBuilder();
			builder.append("create table ");
			builder.append(getTableName());
			builder.append("(");

			ColumnStruct column = null;

			for (int i = 0; i < size; i++) {
				column = columns.get(i);
				builder.append(column.name);
				builder.append(" ");
				builder.append(column.type);
				if (i < size-1) {
					builder.append(", ");
				}
			}

			builder.append(")");
			return builder.toString();
		}

		return null;
	}

	boolean hasObject(SQLiteDatabase db, Object object) {
		boolean answer = false;

		List<QueryField> queryFields = new ArrayList<QueryField>();
		buildQueryFields(queryFields, object);

		final int size = queryFields.size();
		if (size > 0) {
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT * FROM ");
			builder.append(getTableName());
			builder.append(" WHERE ");

			for (int i = 0; i < size; i++) {
				QueryField field = queryFields.get(i);
				builder.append(field.getQuery());
				if (i < size-1) {
					builder.append(" AND ");
				}
			}

			String query = builder.toString();
			Cursor cursor = db.rawQuery(query, null);
			if (cursor != null) {
				answer = (cursor.getCount() > 0);
				cursor.close();
			}
		}

		return answer;
	}

	boolean insertObject(SQLiteDatabase db, Object object) {
		ContentValues values = object2Db(object);
		return (db.insert(getTableName(), null, values) != -1);
	}

	boolean updateObject(SQLiteDatabase db, Object object) {
		boolean answer = false;
		ContentValues values = object2Db(object);

		List<QueryField> queryFields = new ArrayList<QueryField>();
		buildQueryFields(queryFields, object);

		final int size = queryFields.size();
		if (size > 0) {
			StringBuilder builder = new StringBuilder();

			for (int i = 0; i < size; i++) {
				QueryField field = queryFields.get(i);
				builder.append(field.getQuery());
				if (i < size-1) {
					builder.append(" AND ");
				}
			}

			String query = builder.toString();
			return (db.update(getTableName(), values, query, null) != -1);
		}

		return answer;
	}

	boolean deleteObject(SQLiteDatabase db, Object object) {
		List<QueryField> queryFields = new ArrayList<QueryField>();
		buildQueryFields(queryFields, object);
		final int size = queryFields.size();
		if (size > 0) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < size; i++) {
				QueryField field = queryFields.get(i);
				builder.append(field.getQuery());
				if (i < size-1) {
					builder.append(" AND ");
				}
			}

			String query = builder.toString();
			return (db.delete(getTableName(), query, null) != 0);
		}

		return false;
	}

	abstract String getTableName();

	abstract void buildColumns(List<ColumnStruct> columns);
	abstract void buildQueryFields(List<QueryField> fields, Object object);

	abstract Object[] db2Objects(Cursor cursor);
	abstract ContentValues object2Db(Object object);

}
