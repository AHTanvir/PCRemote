package anwar.pcremote.Service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import anwar.pcremote.Model.RowItem;


/**
 * Created by anwar on 7/6/2017.
 */

public class Database extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME ="Pcremote.db";
    private static final String Receive_list="Receive_table";
    private static final String Send_list="Send_table";
    private static final String Name="name";
    private static final String Status="status";
    private static final String Progress="progress";
    private static final String Id="Id";
    public Database(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String Create_Table_Receive="CREATE TABLE "+Receive_list+"("+Id+ " INTEGER PRIMARY KEY  AUTOINCREMENT, " +
                ""+Name+" VARCHAR(55) NOT NULL,"+Status+" VARCHAR(20),"+Progress+" INT"+")";
        String Create_Table_send="CREATE TABLE "+Send_list+"("+Id+ " INTEGER PRIMARY KEY  AUTOINCREMENT, " +
                ""+Name+" VARCHAR(55) NOT NULL,"+Status+" VARCHAR(20) ,"+Progress+" INT"+")";
        db.execSQL(Create_Table_Receive);
        db.execSQL(Create_Table_send);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Receive_list);
        db.execSQL("DROP TABLE IF EXISTS " + Send_list);
        onCreate(db);
    }
    public  int addReceiveFile(String name,String status,int progress) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Name,name);
        values.put(Status,status);
        values.put(Progress,progress);
       long id= db.insert(Receive_list,null,values);
        db.close();
        return (int) id;
    }
    public  int addSendFile(String name,String status,int progress) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Name,name);
        values.put(Status,status);
        values.put(Progress,progress);
        long id=db.insert(Send_list,null,values);
        db.close();
        return (int) id;
    }
    public List getReceiveList() {
        List<RowItem> row=new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery ="SELECT  * FROM " + Receive_list+" ORDER BY "+Id+" DESC";
        Cursor cursor = db.rawQuery(selectQuery, null);
        int i=0;
        if (cursor.moveToFirst()) {
            do {
               RowItem rowItem=new RowItem(cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getInt(3));
                row.add(rowItem);
            } while (cursor.moveToNext());
        }
        return row;
    }
    public List getSendeList() {
        List<RowItem> row=new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery ="SELECT  * FROM " + Send_list+" ORDER BY "+Id+" DESC";
        Cursor cursor= db.rawQuery(selectQuery, null);
        int i=0;
        if (cursor.moveToFirst()) {
            do {
                RowItem rowItem=new RowItem(cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getInt(3));
                row.add(rowItem);
            } while (cursor.moveToNext());
        }
        return row;
    }
    public void updateReceiveItem(String rowId,String status,int progress){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Status,status);
        values.put(Progress,progress);
        db.update(Receive_list, values, Id + " = ?",
                new String[] { String.valueOf(rowId) });
       // db.close();
    }
    public void updateSendItem(String rowId,String status,int progress){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Status,status);
        values.put(Progress,progress);
        db.update(Send_list, values, Id + " = ?",
                new String[] { String.valueOf(rowId) });
        db.close();
    }
    public void DeleteRecevieItem(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Receive_list, Id + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }
    public void DeleteSendItem(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Send_list, Id + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }
    public void DeleteRecevieList() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(Receive_list,null,null);
        db.close();
    }
    public void DeleteSendList() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(Send_list,null,null);
        db.close();
    }
}
