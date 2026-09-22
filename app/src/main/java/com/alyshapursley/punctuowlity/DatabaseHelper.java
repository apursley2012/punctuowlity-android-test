package com.alyshapursley.punctuowlity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public final class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "PunctuOwlityDB.db";
    private static final int DATABASE_VERSION = 3;

    private static final String USER_TABLE = "users";
    private static final String USER_ID = "id";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static final String EVENT_TABLE = "events";
    private static final String EVENT_ID = "id";
    private static final String EVENT_TITLE = "title";
    private static final String EVENT_DATE = "date";
    private static final String EVENT_TIME = "time";
    private static final String EVENT_REMINDER = "reminder_enabled";
    private static final String EVENT_CATEGORY = "category";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(
                "CREATE TABLE " + USER_TABLE + " (" +
                        USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USERNAME + " TEXT UNIQUE NOT NULL, " +
                        PASSWORD + " TEXT NOT NULL)"
        );

        database.execSQL(
                "CREATE TABLE " + EVENT_TABLE + " (" +
                        EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        EVENT_TITLE + " TEXT NOT NULL, " +
                        EVENT_DATE + " TEXT NOT NULL, " +
                        EVENT_TIME + " TEXT NOT NULL, " +
                        EVENT_REMINDER + " INTEGER NOT NULL DEFAULT 0, " +
                        EVENT_CATEGORY + " TEXT NOT NULL DEFAULT 'general')"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // Version 2 adds reminder state without deleting existing users or events.
        if (oldVersion < 2) {
            database.execSQL(
                    "ALTER TABLE " + EVENT_TABLE +
                            " ADD COLUMN " + EVENT_REMINDER + " INTEGER NOT NULL DEFAULT 0"
            );
        }
        if (oldVersion < 3) {
            database.execSQL(
                    "ALTER TABLE " + EVENT_TABLE +
                            " ADD COLUMN " + EVENT_CATEGORY + " TEXT NOT NULL DEFAULT 'general'"
            );
        }
    }

    boolean insertUser(String username, String password) {
        ContentValues values = new ContentValues();
        values.put(USERNAME, username);
        values.put(PASSWORD, PasswordHasher.hash(password));

        return getWritableDatabase().insert(USER_TABLE, null, values) != -1;
    }

    boolean checkUser(String username, String password) {
        SQLiteDatabase database = getWritableDatabase();
        try (Cursor cursor = database.query(
                USER_TABLE,
                new String[]{USER_ID, PASSWORD},
                USERNAME + "=?",
                new String[]{username},
                null,
                null,
                null
        )) {
            if (!cursor.moveToFirst()) {
                return false;
            }

            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(PASSWORD));
            boolean matches = PasswordHasher.verify(password, storedPassword);

            if (matches && PasswordHasher.isLegacyValue(storedPassword)) {
                ContentValues values = new ContentValues();
                values.put(PASSWORD, PasswordHasher.hash(password));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID));
                database.update(
                        USER_TABLE,
                        values,
                        USER_ID + "=?",
                        new String[]{String.valueOf(userId)}
                );
            }

            return matches;
        }
    }

    long insertEvent(String title, String date, String time, boolean reminderEnabled, String category) {
        ContentValues values = eventValues(title, date, time, reminderEnabled, category);
        return getWritableDatabase().insert(EVENT_TABLE, null, values);
    }

    boolean updateEvent(
            int id,
            String title,
            String date,
            String time,
            boolean reminderEnabled,
            String category
    ) {
        int rowsUpdated = getWritableDatabase().update(
                EVENT_TABLE,
                eventValues(title, date, time, reminderEnabled, category),
                EVENT_ID + "=?",
                new String[]{String.valueOf(id)}
        );
        return rowsUpdated > 0;
    }

    boolean deleteEvent(int id) {
        return getWritableDatabase().delete(
                EVENT_TABLE,
                EVENT_ID + "=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();

        try (Cursor cursor = getReadableDatabase().query(
                EVENT_TABLE,
                null,
                null,
                null,
                null,
                null,
                null
        )) {
            while (cursor.moveToNext()) {
                events.add(readEvent(cursor));
            }
        }

        return events;
    }

    Event getEventById(int id) {
        try (Cursor cursor = getReadableDatabase().query(
                EVENT_TABLE,
                null,
                EVENT_ID + "=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        )) {
            return cursor.moveToFirst() ? readEvent(cursor) : null;
        }
    }

    private ContentValues eventValues(
            String title,
            String date,
            String time,
            boolean reminderEnabled,
            String category
    ) {
        ContentValues values = new ContentValues();
        values.put(EVENT_TITLE, title);
        values.put(EVENT_DATE, date);
        values.put(EVENT_TIME, time);
        values.put(EVENT_REMINDER, reminderEnabled ? 1 : 0);
        values.put(EVENT_CATEGORY, category);
        return values;
    }

    private Event readEvent(Cursor cursor) {
        return new Event(
                cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(EVENT_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(EVENT_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(EVENT_TIME)),
                cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_REMINDER)) == 1,
                cursor.getString(cursor.getColumnIndexOrThrow(EVENT_CATEGORY))
        );
    }
}
