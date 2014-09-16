package edu.buffalo.cse.cse486586.groupmessenger;

import android.database.sqlite.SQLiteConstraintException;

import android.database.MatrixCursor;

import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteQueryBuilder;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 *
 */
public class GroupMessengerProvider extends ContentProvider {

    private DB_Helper db;
    String table = DB_Helper.getTableName();
    
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        
        long rowId;
        SQLiteDatabase sqldb = db.getWritableDatabase();
        try
        {
            Log.v("provider","inside insert");
        rowId =  sqldb.insertOrThrow(table, null, values);
       
        }
        catch (android.database.sqlite.SQLiteConstraintException e)
        {
            Log.v("Exception",e.getMessage());
           sqldb.update(table, values, "( key = '"+values.keySet().toString()+"' )", null);
        }
       getContext().getContentResolver().notifyChange(uri, null);
       
        Log.v("insert", values.toString());
        return uri;
        //return Uri.withAppendedPath(uri, String.valueOf(rowId));
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        db = new DB_Helper(getContext());
        Log.v("Provider", "DB helper called");
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
     
        SQLiteDatabase sqlDB = db.getWritableDatabase();
        
        SQLiteQueryBuilder query = new SQLiteQueryBuilder();
       // query.setTables(table);
        
        //query.appendWhere("key = ");
        Log.d("tablename", table);
        Log.d("selection", selection);
        Log.d("Uri", uri.toString());
        
        //MatrixCursor mc = new MatrixCursor(new String[] {"key","value"});
       
        Cursor cursor = sqlDB.query(table, null, "( key = '"+selection+"' )", null, null, null, null);
      // Cursor cursor = query.query(sqlDB, null,"(key = "+selection+")", null, null, null, null);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        Log.v("cursor", String.valueOf(cursor.getCount()));
        Log.v("query", selection);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }
}
