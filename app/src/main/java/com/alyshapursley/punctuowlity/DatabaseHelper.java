package com.alyshapursley.punctuowlity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="PunctuOwlityDB.db";
    private static final int DATABASE_VERSION=4;
    private static final String USER_TABLE="users", USER_ID="id", USERNAME="username", PASSWORD="password";
    private static final String FIRST_NAME="first_name", LAST_NAME="last_name", EMAIL="email", PHONE="phone";
    private static final String EVENT_TABLE="events", EVENT_ID="id", EVENT_TITLE="title", EVENT_DATE="date",
            EVENT_TIME="time", EVENT_REMINDER="reminder_enabled", EVENT_CATEGORY="category";

    DatabaseHelper(Context context){super(context,DATABASE_NAME,null,DATABASE_VERSION);}
    @Override public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE "+USER_TABLE+" ("+USER_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                USERNAME+" TEXT UNIQUE NOT NULL, "+PASSWORD+" TEXT NOT NULL, "+FIRST_NAME+" TEXT NOT NULL DEFAULT '', "+
                LAST_NAME+" TEXT NOT NULL DEFAULT '', "+EMAIL+" TEXT NOT NULL DEFAULT '', "+PHONE+" TEXT NOT NULL DEFAULT '')");
        db.execSQL("CREATE TABLE "+EVENT_TABLE+" ("+EVENT_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                EVENT_TITLE+" TEXT NOT NULL, "+EVENT_DATE+" TEXT NOT NULL, "+EVENT_TIME+" TEXT NOT NULL, "+
                EVENT_REMINDER+" INTEGER NOT NULL DEFAULT 0, "+EVENT_CATEGORY+" TEXT NOT NULL DEFAULT 'general')");
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        if(oldVersion<2) db.execSQL("ALTER TABLE "+EVENT_TABLE+" ADD COLUMN "+EVENT_REMINDER+" INTEGER NOT NULL DEFAULT 0");
        if(oldVersion<3) db.execSQL("ALTER TABLE "+EVENT_TABLE+" ADD COLUMN "+EVENT_CATEGORY+" TEXT NOT NULL DEFAULT 'general'");
        if(oldVersion<4){
            db.execSQL("ALTER TABLE "+USER_TABLE+" ADD COLUMN "+FIRST_NAME+" TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE "+USER_TABLE+" ADD COLUMN "+LAST_NAME+" TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE "+USER_TABLE+" ADD COLUMN "+EMAIL+" TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE "+USER_TABLE+" ADD COLUMN "+PHONE+" TEXT NOT NULL DEFAULT ''");
            db.execSQL("UPDATE "+USER_TABLE+" SET "+EMAIL+"="+USERNAME+" WHERE "+EMAIL+"=''");
        }
    }
    boolean insertUser(String first,String last,String email,String phone,String username,String password){
        ContentValues v=new ContentValues(); v.put(FIRST_NAME,first);v.put(LAST_NAME,last);v.put(EMAIL,email);
        v.put(PHONE,phone);v.put(USERNAME,username);v.put(PASSWORD,PasswordHasher.hash(password));
        return getWritableDatabase().insert(USER_TABLE,null,v)!=-1;
    }
    boolean checkUser(String username,String password){
        SQLiteDatabase db=getWritableDatabase();
        try(Cursor c=db.query(USER_TABLE,new String[]{USER_ID,PASSWORD},USERNAME+"=?",new String[]{username},null,null,null)){
            if(!c.moveToFirst()) return false;
            String stored=c.getString(c.getColumnIndexOrThrow(PASSWORD));
            boolean ok=PasswordHasher.verify(password,stored);
            if(ok&&PasswordHasher.isLegacyValue(stored)){
                ContentValues v=new ContentValues();v.put(PASSWORD,PasswordHasher.hash(password));
                db.update(USER_TABLE,v,USER_ID+"=?",new String[]{String.valueOf(c.getInt(c.getColumnIndexOrThrow(USER_ID)))});
            }
            return ok;
        }
    }
    String[] getUserProfile(String username){
        try(Cursor c=getReadableDatabase().query(USER_TABLE,new String[]{FIRST_NAME,LAST_NAME,EMAIL,PHONE,USERNAME},
                USERNAME+"=?",new String[]{username},null,null,null)){
            if(!c.moveToFirst()) return null;
            return new String[]{c.getString(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4)};
        }
    }
    boolean updateUserProfile(String original,String first,String last,String email,String phone,String username){
        ContentValues v=new ContentValues();v.put(FIRST_NAME,first);v.put(LAST_NAME,last);v.put(EMAIL,email);v.put(PHONE,phone);v.put(USERNAME,username);
        try{return getWritableDatabase().update(USER_TABLE,v,USERNAME+"=?",new String[]{original})>0;}
        catch(Exception e){return false;}
    }
    long insertEvent(String title,String date,String time,boolean reminder,String category){
        return getWritableDatabase().insert(EVENT_TABLE,null,eventValues(title,date,time,reminder,category));
    }
    boolean updateEvent(int id,String title,String date,String time,boolean reminder,String category){
        return getWritableDatabase().update(EVENT_TABLE,eventValues(title,date,time,reminder,category),EVENT_ID+"=?",new String[]{String.valueOf(id)})>0;
    }
    boolean deleteEvent(int id){return getWritableDatabase().delete(EVENT_TABLE,EVENT_ID+"=?",new String[]{String.valueOf(id)})>0;}
    List<Event> getAllEvents(){List<Event> out=new ArrayList<>();try(Cursor c=getReadableDatabase().query(EVENT_TABLE,null,null,null,null,null,null)){while(c.moveToNext())out.add(readEvent(c));}return out;}
    Event getEventById(int id){try(Cursor c=getReadableDatabase().query(EVENT_TABLE,null,EVENT_ID+"=?",new String[]{String.valueOf(id)},null,null,null)){return c.moveToFirst()?readEvent(c):null;}}
    private ContentValues eventValues(String t,String d,String tm,boolean r,String cat){ContentValues v=new ContentValues();v.put(EVENT_TITLE,t);v.put(EVENT_DATE,d);v.put(EVENT_TIME,tm);v.put(EVENT_REMINDER,r?1:0);v.put(EVENT_CATEGORY,cat);return v;}
    private Event readEvent(Cursor c){return new Event(c.getInt(c.getColumnIndexOrThrow(EVENT_ID)),c.getString(c.getColumnIndexOrThrow(EVENT_TITLE)),c.getString(c.getColumnIndexOrThrow(EVENT_DATE)),c.getString(c.getColumnIndexOrThrow(EVENT_TIME)),c.getInt(c.getColumnIndexOrThrow(EVENT_REMINDER))==1,c.getString(c.getColumnIndexOrThrow(EVENT_CATEGORY)));}
}
