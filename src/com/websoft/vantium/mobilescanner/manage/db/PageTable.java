package com.websoft.vantium.mobilescanner.manage.db;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import com.websoft.vantium.mobilescanner.model.Page;


public class PageTable extends DBTable {

	private final String TABLE_NAME = "tblPage";

	static final class ColumnName {
		static final String Id = "id";
		static final String DocId = "doc_id";
		static final String Name = "name";
		static final String ImageUrl = "image_url";
		static final String ThumbUrl = "thumb_url";
	}

	@Override
	String getTableName() {
		return TABLE_NAME;
	}

	@Override
	void buildColumns(List<ColumnStruct> columns) {
		columns.add(new ColumnStruct(ColumnName.Id, ColumnType.COLUMN_TYPE_INTEGER));
		columns.add(new ColumnStruct(ColumnName.DocId, ColumnType.COLUMN_TYPE_INTEGER));
		columns.add(new ColumnStruct(ColumnName.Name, ColumnType.COLUMN_TYPE_TEXT));
		columns.add(new ColumnStruct(ColumnName.ImageUrl, ColumnType.COLUMN_TYPE_TEXT));
		columns.add(new ColumnStruct(ColumnName.ThumbUrl, ColumnType.COLUMN_TYPE_TEXT));
	}

	@Override
	void buildQueryFields(List<QueryField> fields, Object object) {
		Page page = (Page) object;
		fields.add(new QueryField(ColumnName.Id, String.valueOf(page.getId())));
	}

	@Override
	Object[] db2Objects(Cursor cursor) {
		int count = cursor.getCount();
		if (count == 0)
			return null;

		int idIndex = cursor.getColumnIndex(ColumnName.Id);
		int docId= cursor.getColumnIndex(ColumnName.DocId);
		int nameIndex = cursor.getColumnIndex(ColumnName.Name);
		int urlIndex = cursor.getColumnIndex(ColumnName.ImageUrl);
		int thumbIndex = cursor.getColumnIndex(ColumnName.ThumbUrl);

		Page[] objects = new Page[count];
		Page object = null;

		for (int i = 0; cursor.moveToNext(); i++) {
			object = new Page();
			object.setId(cursor.getInt(idIndex));
			object.setDocId(cursor.getInt(docId));
			object.setName(cursor.getString(nameIndex));
			object.setUrl(cursor.getString(urlIndex));
			object.setThumbUrl(cursor.getString(thumbIndex));
			objects[i] = object;
		}

		return objects;
	}

	@Override
	ContentValues object2Db(Object object) {
		Page page = (Page) object;
		ContentValues values = new ContentValues();

		if (page.getId() != 0) {
			values.put(ColumnName.Id, page.getId());
		}

		values.put(ColumnName.DocId, page.getDocId());
		values.put(ColumnName.Name, page.getName());
		values.put(ColumnName.ImageUrl, page.getUrl());
		values.put(ColumnName.ThumbUrl, page.getThumbUrl());

		return values;
	}

}
