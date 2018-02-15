package com.websoft.vantium.mobilescanner.manage.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {

	/** database tables */
	protected DBTable[] mTables = null;

	public DBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.beginTransaction();
			for (int i = 0; i < mTables.length; i++) {
				mTables[i].createTable(db);
			}
			db.setTransactionSuccessful();
		} catch(SQLException e) {
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// delete table and create
		if (oldVersion != newVersion) {
			for (int i = 0; i < mTables.length; i++) {
				mTables[i].upgradeTable(db);
			}
		}
	}

	/** all tables delete and create */
	synchronized boolean initializeAllTables() {
		boolean result = true;
		SQLiteDatabase db = this.getWritableDatabase();

		try {
			db.beginTransaction();
			for (int i = 0; i < mTables.length; i++) {
				mTables[i].deleteTable(db);
				mTables[i].createTable(db);
			}
			db.setTransactionSuccessful();
		} catch(SQLException e) {
			result = false;
		} finally {
			db.endTransaction();
		}

		db.close();
		return result;
	}

	/** get all object by table id */
	synchronized Object[] getAllObjects(int tableId) {
		Object[] objects = null;
		SQLiteDatabase db = getWritableDatabase();
		try {
			objects = mTables[tableId].getAllObjects(db);
		} catch (SQLException e) {
		}
		db.close();
		return objects;
	}

	/** get columns by columns==keys */
	synchronized Object[] getObjects(int tableId, String[] columns, String[] keys) {
		Object[] objects = null;
		SQLiteDatabase db = getWritableDatabase();
		try {
			objects = mTables[tableId].getObjects(db, columns, keys);
		} catch (SQLException e) {
		}
		db.close();
		return objects;
	}

	/** check exist object */
	synchronized boolean isExistObject(int tableId, Object object) {
		boolean answer;
		SQLiteDatabase db = getWritableDatabase();
		try {
			answer = mTables[tableId].hasObject(db, object);
		} catch (SQLException e) {
			answer = false;
		}
		db.close();
		return answer;
	}

	/** insert new object */
	synchronized boolean insertObject(int tableId, Object object) {
		boolean answer;
		SQLiteDatabase db = getWritableDatabase();
		try {
			answer = mTables[tableId].insertObject(db, object);
		} catch (SQLException e) {
			answer = false;
		}
		db.close();
		return answer;
	}

	synchronized boolean updateObject(int tableId, Object object) {
		boolean answer;
		SQLiteDatabase db = getWritableDatabase();
		try {
			answer = mTables[tableId].updateObject(db, object);
		} catch (SQLException e) {
			answer = false;
		}
		db.close();
		return answer;
	}

	/** delete object from table by table id */
	synchronized boolean deleteObject(int tableId, Object object) {
		boolean answer;
		SQLiteDatabase db = getWritableDatabase();
		try {
			answer = mTables[tableId].deleteObject(db, object);
		} catch (SQLException e) {
			answer = false;
		}
		db.close();
		return answer;
	}
}
