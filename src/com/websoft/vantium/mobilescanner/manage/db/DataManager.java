package com.websoft.vantium.mobilescanner.manage.db;

import android.content.Context;

import com.websoft.vantium.mobilescanner.model.Doc;
import com.websoft.vantium.mobilescanner.model.Page;


public class DataManager extends DBHelper {

	private static DataManager instance = null;

	/** part table id */
	public static final int TABLE_ID_DOC = 0;

	/** game table id */
	public static final int TABLE_ID_PAGE = 1;

	/** table count */
	private static final int TABLE_COUNT = TABLE_ID_PAGE + 1;

	/** database name */
	private static final String DATABASE_NAME = "mobilescanner.db";

	/** database version */
	private static final int DATABASE_VERSION = 1;

	public static void init(Context context) {
		instance = new DataManager(context);
	}

	public static DataManager manager() {
		return instance;
	}

	private DataManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		mTables = new DBTable[TABLE_COUNT];
		mTables[TABLE_ID_DOC] = new DocTable();
		mTables[TABLE_ID_PAGE] = new PageTable();
	}

	public Doc[] loadDocs() {
		return (Doc[]) getAllObjects(TABLE_ID_DOC);
	}

	public boolean saveDoc(Doc doc) {
		boolean result = false;
		if (isExistObject(TABLE_ID_DOC, doc)) {
			result = updateObject(TABLE_ID_DOC, doc);
		} else {
			result = insertObject(TABLE_ID_DOC, doc);
		}
		return result;
	}

	public boolean deleteDoc(Doc doc) {
		return deleteObject(TABLE_ID_DOC, doc);
	}

	public Page[] loadAllPages() {
		return (Page[]) getAllObjects(TABLE_ID_PAGE);
	}

	public Page[] loadPages(int docId) {
		return (Page[]) getObjects(TABLE_ID_PAGE, new String[] {PageTable.ColumnName.DocId}, new String[] {String.valueOf(docId)});
	}

	public boolean savePage(Page page) {
		boolean result = false;
		if (isExistObject(TABLE_ID_PAGE, page)) {
			result = updateObject(TABLE_ID_PAGE, page);
		}else{
			result = insertObject(TABLE_ID_PAGE, page);
		}
		return result;
	}
	
	public boolean deletePage(Page page){
		return deleteObject(TABLE_ID_PAGE, page);
	}
}
