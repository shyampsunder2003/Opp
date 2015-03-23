package com.example.shyampsunder2003.opp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by shyampsunder2003 on 21-02-2015.
 */
public class DatabaseHelp {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_MAC = "MACAddress";
    public static final String KEY_TIME = "Time";       //Timestamp at the time of clicking check
    public static final String KEY_MESSAGE= "Message";
    public static final String KEY_MESSAGE_HASH= "MessageHash";
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
            db.execSQL("CREATE TABLE " + DATABASE_TABLE1 + " (" +
                            KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            KEY_MAC + " TEXT NOT NULL, " +
                            KEY_TIME + " TEXT NOT NULL);"
            );
            db.execSQL("CREATE TABLE " + DATABASE_TABLE2 + " (" +
                            KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            KEY_TIME + " TEXT NOT NULL, " + KEY_MESSAGE + " TEXT NOT NULL, "+ KEY_MESSAGE_HASH + " TEXT NOT NULL, "+ KEY_MAC + " TEXT NOT NULL);"
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

    public long createDeviceEntry(String mac) {            //Inserting the clues
        // TODO Auto-generated method stub
        long time = (long) (System.currentTimeMillis());
        String timestamp=String.valueOf(time);
        ContentValues cv = new ContentValues();
        cv.put(KEY_MAC, mac);
        cv.put(KEY_TIME, timestamp);
        Log.d("Database","Database entry for device created "+mac);
        return ourDatabase.insert(DATABASE_TABLE1, null, cv);
    }
    public long createMessageEntry(String message, String mac) {
        // TODO Auto-generated method stub
        long time = (long) (System.currentTimeMillis());
        String timestamp=String.valueOf(time);
        ContentValues cv = new ContentValues();
        cv.put(KEY_MESSAGE, message);
        cv.put(KEY_MESSAGE_HASH, MD5(message));
        cv.put(KEY_TIME, timestamp);
        cv.put(KEY_MAC, mac);
        Log.d("Database","Message entry created "+message);
        return ourDatabase.insert(DATABASE_TABLE2, null, cv);
    }
    public boolean containsdevice(String mac)
    {
        String[] columns = new String[]{ KEY_ROWID, KEY_MAC, KEY_TIME};
        Cursor c;
        c = ourDatabase.query(DATABASE_TABLE1, columns,KEY_MAC + "=\'" + mac + "\'", null, null, null, null);
        int result=0;
        result=c.getCount();
        c.close();
        if(result==0)
            return false;
        else
            return true;

    }
    public boolean containsMessage(String hash)
    {
        String[] columns = new String[]{ KEY_ROWID, KEY_TIME, KEY_MESSAGE,KEY_MESSAGE_HASH, KEY_MAC};
        Cursor c;
        c = ourDatabase.query(DATABASE_TABLE2, columns,KEY_MESSAGE_HASH + "=\'" + hash + "\'", null, null, null, null);
        int result=0;
        result=c.getCount();
        c.close();
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
        Cursor c = ourDatabase.query(DATABASE_TABLE1, columns,null, null, null, null, null);
        int iRow = c.getColumnIndex(KEY_ROWID);
        int iMac = c.getColumnIndex(KEY_MAC);
        int iTime = c.getColumnIndex(KEY_TIME);
        String result="";
        Log.d("Database",String.valueOf(c.getCount()));
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            result =c.getString(iMac) + "*" + c.getString(iTime) ;
            l.addLast(result);
        }
        c.close();
        return l;
    }
    public LinkedList getMessages()
    {
        Log.d("Database", "getMessages method invoked");
        LinkedList l=new LinkedList();
        String[] columns = new String[]{ KEY_ROWID, KEY_TIME, KEY_MESSAGE,KEY_MESSAGE_HASH, KEY_MAC};
        Cursor c = ourDatabase.query(DATABASE_TABLE2, columns,null, null, null, null, null);
        int iRow = c.getColumnIndex(KEY_ROWID);
        int iTime = c.getColumnIndex(KEY_TIME);
        int iMessage = c.getColumnIndex(KEY_MESSAGE);
        int iMac = c.getColumnIndex(KEY_MAC);
        int iMessageHash = c.getColumnIndex(KEY_MESSAGE_HASH);
        String result="";
        Log.d("Database",String.valueOf(c.getCount()));
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            result =c.getString(iMessage);
            l.addLast(result);
        }
        c.close();
        return l;
    }
    public LinkedList getMessagesHash()
    {
        Log.d("Database", "getMessages method invoked");
        LinkedList l=new LinkedList();
        String[] columns = new String[]{ KEY_ROWID, KEY_TIME, KEY_MESSAGE,KEY_MESSAGE_HASH, KEY_MAC};
        Cursor c = ourDatabase.query(DATABASE_TABLE2, columns,null, null, null, null, null);
        int iMessageHash = c.getColumnIndex(KEY_MESSAGE_HASH);
        String result="";
        Log.d("Database",String.valueOf(c.getCount()));
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            result =c.getString(iMessageHash);
            l.addLast(result);
        }
        c.close();
        return l;
    }
    public String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }
    public String getMessageListHash()
    {
        String[] columns = new String[]{ KEY_ROWID, KEY_TIME, KEY_MESSAGE,KEY_MESSAGE_HASH, KEY_MAC};
        Cursor c = ourDatabase.query(DATABASE_TABLE2, columns,null, null, null, null, null);
        int iRow = c.getColumnIndex(KEY_ROWID);
        int iTime = c.getColumnIndex(KEY_TIME);
        int iMessage = c.getColumnIndex(KEY_MESSAGE);
        int iMessageHash = c.getColumnIndex(KEY_MESSAGE_HASH);
        int iMac = c.getColumnIndex(KEY_MAC);
        LinkedList messages = new LinkedList();
        String result="";
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            messages.addFirst(c.getString(iMessage));
        }
        Collections.sort(messages);
        for(int i=0;i<messages.size();++i)
        {
            result+="/"+messages.get(i);
        }
        c.close();
        Log.d("Database","Hashlist is: "+MD5(result));
        return MD5(result);
    }
    public void updateDeviceTime(String mac)
    {
        long time = (long) (System.currentTimeMillis());
        String timestamp=String.valueOf(time);
        ContentValues cv = new ContentValues();
        cv.put(KEY_MAC, mac);
        cv.put(KEY_TIME, timestamp);
        Log.d("Database","Database entry for device updated"+mac+timestamp);
        ourDatabase.update(DATABASE_TABLE1,cv,KEY_MAC+"=\""+mac+"\"",null);
    }
    public long getDeviceTimestamp(String mac)
    {
        String[] columns = new String[]{ KEY_ROWID, KEY_MAC, KEY_TIME};
        Cursor c = ourDatabase.query(DATABASE_TABLE1, columns,KEY_MAC+"=\""+mac+"\"", null, null, null, null);
        int iRow = c.getColumnIndex(KEY_ROWID);
        int iMac = c.getColumnIndex(KEY_MAC);
        int iTime = c.getColumnIndex(KEY_TIME);
        String result=null;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            result =c.getString(iTime) ;
        }
        c.close();
        Log.d("Long value",result);
        return Long.valueOf(result);
    }

}
