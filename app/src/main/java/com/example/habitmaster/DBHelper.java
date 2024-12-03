package com.example.habitmaster;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserDatabase.db";
    private static final int DATABASE_VERSION = 5;

    private static final String TAG = "DBHelper";

    // Table and Column Constants
    public static final String TABLE_USERS = "users";
    public static final String TABLE_HABITS = "habits";
    public static final String TABLE_HABIT_HISTORY = "habit_history";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_FREQUENCY = "frequency";
    public static final String COLUMN_HABIT_ID = "habit_id";
    public static final String COLUMN_DATE_COMPLETED = "date_completed";
    public static final String COLUMN_DATE = "date"; // Habit date column
    public static final String COLUMN_IS_COMPLETED = "isCompleted"; // Completion status column

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");

        // Create users table
        String createUsersTable = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PASSWORD + " TEXT, " +
                "is_logged_in INTEGER DEFAULT 1)";
        db.execSQL(createUsersTable);

        // Create habits table with user_id
        String createHabitsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_HABITS +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_FREQUENCY + " TEXT, " +
                COLUMN_IS_COMPLETED + " INTEGER DEFAULT 0, " +
                COLUMN_DATE_COMPLETED + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_USER_ID + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        db.execSQL(createHabitsTable);

        // Create habit history table with user_id
        String createHabitHistoryTable = "CREATE TABLE IF NOT EXISTS " + TABLE_HABIT_HISTORY +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HABIT_ID + " INTEGER, " +
                COLUMN_DATE_COMPLETED + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_HABIT_ID + ") REFERENCES " + TABLE_HABITS + "(" + COLUMN_ID + "))";
        db.execSQL(createHabitHistoryTable);

        Log.d(TAG, "Database tables created successfully.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABIT_HISTORY);
        onCreate(db);
        Log.d(TAG, "Database upgraded successfully.");
    }


    public boolean registerUser(String name, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }


    public boolean addHabit(String name, String frequency, String dateTime, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_FREQUENCY, frequency);
        values.put(COLUMN_DATE, dateTime);
        values.put(COLUMN_USER_ID, userId);

        long result = db.insert(TABLE_HABITS, null, values);
        return result != -1;
    }
    public boolean deleteHabit(int habitId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_HABITS, COLUMN_ID + " = ?", new String[]{String.valueOf(habitId)});
        if (rowsDeleted > 0) {
            Log.d(TAG, "Habit deleted successfully, ID: " + habitId);
            return true;
        } else {
            Log.d(TAG, "No habit found with ID: " + habitId);
            return false;
        }
    }



    public boolean checkUser(String name, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_NAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{name, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }


    public Cursor getUserHabits(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_HABITS + " WHERE " + COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
    }


    @SuppressLint("Range")
    public String getLastCompletionDate(int habitId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String lastDate = null;
        try (Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_DATE_COMPLETED +
                        " FROM " + TABLE_HABIT_HISTORY +
                        " WHERE " + COLUMN_HABIT_ID + " = ?" +
                        " ORDER BY " + COLUMN_DATE_COMPLETED + " DESC LIMIT 1",
                new String[]{String.valueOf(habitId)})) {

            if (cursor != null && cursor.moveToFirst()) {
                lastDate = cursor.getString(cursor.getColumnIndex(COLUMN_DATE_COMPLETED));
            }
        }
        return lastDate;
    }



    public boolean addHabitHistory(int habitId, String dateCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_ID, habitId);
        values.put(COLUMN_DATE_COMPLETED, dateCompleted);
        long result = db.insert(TABLE_HABIT_HISTORY, null, values);
        return result != -1;
    }


    public boolean markHabitAsCompleted(int habitId, String dateCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_COMPLETED, 1);
        values.put(COLUMN_DATE_COMPLETED, dateCompleted);

        int rows = db.update(TABLE_HABITS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(habitId)});
        return rows > 0;
    }

    public int getUserId(String name, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_USERS + " WHERE " + COLUMN_NAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{name, password});

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int userId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            cursor.close();
            return userId;
        } else {
            cursor.close();
            return -1;
        }
    }

}
