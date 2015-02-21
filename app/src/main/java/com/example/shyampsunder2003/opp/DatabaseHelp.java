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
    public static final String KEY_LAT = "Latitude";
    public static final String KEY_LONG = "Longitude";
    public static final String KEY_TIME = "Time";       //Timestamp at the time of clicking check
    public static final String KEY_RESULT = "Result";   //Result of the operation, its either 'Failure' or 'Success'
    public static final String KEY_REASON = "Reason";
    private static final String DATABASE_NAME = "DatabaseDB";
    private static final String DATABASE_TABLE1 = "locations";  //Contains all the clues downloaded from parse
    private static final String DATABASE_TABLE2 = "Results";    //Contains all the results of check along with timestamps
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
                            KEY_LAT + " TEXT NOT NULL, " +
                            KEY_LONG + " TEXT NOT NULL);"
            );
            db.execSQL("CREATE TABLE " + DATABASE_TABLE2 + " (" +                               //Table creation for results
                            KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            KEY_TIME + " TEXT NOT NULL, " + KEY_RESULT + " TEXT NOT NULL, "+ KEY_REASON + " TEXT NOT NULL);"
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

    public long createEntry(String latitude, String longitude) {            //Inserting the clues
        // TODO Auto-generated method stub
        ContentValues cv = new ContentValues();
        cv.put(KEY_LAT, latitude);
        cv.put(KEY_LONG, longitude);
        return ourDatabase.insert(DATABASE_TABLE1, null, cv);
    }
    public long createResult(String timestamp, String result, String reason) {             //Inserting the results
        // TODO Auto-generated method stub
        ContentValues cv = new ContentValues();
        cv.put(KEY_TIME, timestamp);
        cv.put(KEY_RESULT, result);
        cv.put(KEY_REASON, reason);
        return ourDatabase.insert(DATABASE_TABLE2, null, cv);
    }
    public int countSolvedClues()                                           //This is to enable us to find out how many clues have passed
    {                                                                       // and which clue must be served next
        String[] columns = new String[]{ KEY_ROWID, KEY_TIME, KEY_RESULT};
        Cursor c = ourDatabase.query(DATABASE_TABLE2, columns,KEY_RESULT+"=\"Success\"", null, null, null, null);
        int result=0;
        result=c.getCount();
        return result;
    }
    public LinkedList getResults()
    {
        Log.d("Results", "Written");
        LinkedList l=new LinkedList();
        String[] columns = new String[]{ KEY_ROWID, KEY_TIME, KEY_RESULT};
        Cursor c = ourDatabase.query(DATABASE_TABLE2, columns,null, null, null, null, null);
        int iRow = c.getColumnIndex(KEY_ROWID);
        int iTime = c.getColumnIndex(KEY_TIME);
        int iResult = c.getColumnIndex(KEY_RESULT);
        String result="";
        Log.d("Results",String.valueOf(c.getCount()));
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            result = c.getString(iRow) + " " + c.getString(iTime) + " " + c.getString(iResult) ;
            l.addLast(result);
            Log.d("Results","Written");
        }

        return l;
    }
    public String getData(int val) {                                        //To get the clue of our choice
        // TODO Auto-generated method stub
        String[] columns = new String[]{ KEY_ROWID, KEY_LAT, KEY_LONG};
        Cursor c = ourDatabase.query(DATABASE_TABLE1, columns, null, null, null, null, null);
        String result = "";

        int iRow = c.getColumnIndex(KEY_ROWID);
        int iLat = c.getColumnIndex(KEY_LAT);
        int iLong = c.getColumnIndex(KEY_LONG);
        c.moveToPosition(val-1);
        result = c.getString(iLat) + " " + c.getString(iLong) + "\n";


        return result;
    }

}
