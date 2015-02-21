package com.example.shyampsunder2003.opp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;

/**
 * Created by shyampsunder2003 on 21-02-2015.
 */
public class DatabaseHelp {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_MAC = "MAC Address";
    public static final String KEY_TIME = "Time";       //Timestamp at the time of clicking check
    public static final String KEY_MESSAGE= "Message";
    public static final String KEY_UID= "Unique Identifier";
    private static final String DATABASE_NAME = "DatabaseDB";
    private static final String DATABASE_TABLE1 = "Devices";  //Contains all the clues downloaded from parse
    private static final String DATABASE_TABLE2 = "Messages";    //Contains all the results of check along with timestamps
    private static final int DATABASE_VERSION = 1;

    private DbHelper ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDatabase;

    private static class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL("CREATE TABLE " + DATABASE_TABLE1 + " (" +                               //Table creation for clues
                            KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            KEY_MAC + " TEXT NOT NULL, " +
                            KEY_TIME + " TEXT NOT NULL);"
            );
            db.execSQL("CREATE TABLE " + DATABASE_TABLE2 + " (" +                               //Table creation for results
                            KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            KEY_TIME + " TEXT NOT NULL, " + KEY_MESSAGE + " TEXT NOT NULL, "+ KEY_MAC + " TEXT NOT NULL);"
            );

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE1);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE2);
            onCreate(db);
        }
    }

    public DatabaseHelp(Context c){
        ourContext = c;
    }
    public void delete()
    {
        ourDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE1);
        ourDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE2);     //Only during testing, during execution, results must not be tampered with
        ourHelper.onCreate(ourDatabase);
        open();
    }

    public DatabaseHelp open() throws SQLException {
        ourHelper = new DbHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }
    public void close(){
        ourHelper.close();
    }

    public long createDeviceEntry(String mac, String timestamp) {            //Inserting the clues
        // TODO Auto-generated method stub
        ContentValues cv = new ContentValues();
        cv.put(KEY_MAC, mac);
        cv.put(KEY_TIME, timestamp);
        return ourDatabase.insert(DATABASE_TABLE1, null, cv);
    }
    public long createMessageEntry(String message, String timestamp, String mac) {             //Inserting the results
        // TODO Auto-generated method stub
        ContentValues cv = new ContentValues();
        cv.put(KEY_MESSAGE, message);
        cv.put(KEY_TIME, timestamp);
        cv.put(KEY_MAC, mac);
        return ourDatabase.insert(DATABASE_TABLE2, null, cv);
    }
    public boolean containsdevice(String mac)                                           //This is to enable us to find out how many clues have passed
    {                                                                       // and which clue must be served next
        String[] columns = new String[]{ KEY_ROWID, KEY_MAC, KEY_TIME};
        Cursor c = ourDatabase.query(DATABASE_TABLE2, columns,KEY_MAC+"=\""+mac+"\"", null, null, null, null);
        int result=0;
        result=c.getCount();
        if(result==0)
            return false;
        else
            return true;
    }
    public LinkedList getDevices()
    {
        Log.d("Database", "getDevices method invoked");
        LinkedList l=new LinkedList();
        String[] columns = new String[]{ KEY_ROWID, KEY_MAC, KEY_TIME};
        Cursor c = ourDatabase.query(DATABASE_TABLE2, columns,null, null, null, null, null);
        int iRow = c.getColumnIndex(KEY_ROWID);
        int iMac = c.getColumnIndex(KEY_MAC);
        int iTime = c.getColumnIndex(KEY_TIME);
        String result="";
        Log.d("Database",String.valueOf(c.getCount()));
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            result =c.getString(iMac) + " " + c.getString(iTime) ;
            l.addLast(result);
        }

        return l;
    }
    public LinkedList getMessages()
    {
        Log.d("Database", "getMessages method invoked");
        LinkedList l=new LinkedList();
        String[] columns = new String[]{ KEY_ROWID, KEY_TIME, KEY_MESSAGE, KEY_MAC};
        Cursor c = ourDatabase.query(DATABASE_TABLE2, columns,null, null, null, null, null);
        int iRow = c.getColumnIndex(KEY_ROWID);
        int iTime = c.getColumnIndex(KEY_TIME);
        int iMessage = c.getColumnIndex(KEY_MESSAGE);
        int iMac = c.getColumnIndex(KEY_MAC);
        String result="";
        Log.d("Database",String.valueOf(c.getCount()));
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            result =c.getString(iTime) + " " + c.getString(iMessage) + " " + c.getString(iMac) ;
            l.addLast(result);
        }
        return l;
    }
    public void updateDeviceTime(String mac,String timestamp)
    {
        ContentValues cv = new ContentValues();
        cv.put(KEY_MAC, mac);
        cv.put(KEY_TIME, timestamp);
        ourDatabase.update(DATABASE_TABLE1,cv,KEY_MAC+"=\""+mac+"\"",null);
    }

}
