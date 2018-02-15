package com.websoft.vantium.mobilescanner.manage.db;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import com.websoft.vantium.mobilescanner.model.Doc;

public class DocTable extends DBTable {

	private static final String TABLE_NAME = "tblDoc";

	static final class ColumnName {
		static final String Id = "_id";
		static final String Name = "name";
		static final String Date = "date";
		static final String Count = "count";
	}

	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}

	@Override
	protected void buildColumns(List<ColumnStruct> columns) {
		columns.add(new ColumnStruct(ColumnName.Id, ColumnType.COLUMN_TYPE_INTEGER));
		columns.add(new ColumnStruct(ColumnName.Name, ColumnType.COLUMN_TYPE_TEXT));
		columns.add(new ColumnStruct(ColumnName.Date, ColumnType.COLUMN_TYPE_TEXT));
		columns.add(new ColumnStruct(ColumnName.Count, ColumnType.COLUMN_TYPE_INTEGER));
	}

	@Override
	protected void buildQueryFields(List<QueryField> fields, Object object) {
		Doc doc = (Doc) object;
		fields.add(new QueryField(ColumnName.Id, String.valueOf(doc.getId())));
	}

	@Override
	protected Object[] db2Objects(Cursor cursor) {
		int count = cursor.getCount();
		if (count == 0)
			return null;

		int idIndex = cursor.getColumnIndex(ColumnName.Id);
		int nameIndex = cursor.getColumnIndex(ColumnName.Name);
		int dateIndex = cursor.getColumnIndex(ColumnName.Date);
		int countIndex = cursor.getColumnIndex(ColumnName.Count);

		Doc[] objects = new Doc[count];
		Doc object = null;

		for (int i = 0; cursor.moveToNext(); i++) {
			object = new Doc();
			object.setId(cursor.getInt(idIndex));
			object.setName(cursor.getString(nameIndex));
			object.setDate(cursor.getString(dateIndex));
			object.setCount(cursor.getInt(countIndex));
			objects[i] = object;
		}

		return objects;
	}

	@Override
	protected ContentValues object2Db(Object object) {
		Doc doc = (Doc) object;
		ContentValues values = new ContentValues();

		if (doc.getId() != 0) {
			values.put(ColumnName.Id, doc.getId());
		}

		values.put(ColumnName.Name, doc.getName());
		values.put(ColumnName.Date, doc.getDate());
		values.put(ColumnName.Count, doc.getCount());

		return values;
	}

}
