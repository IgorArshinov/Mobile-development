package be.igorarshinov.avatar_creator.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import be.igorarshinov.avatar_creator.data.AvatarContract.AvatarEntry;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOGTAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "avatar-creator.db";

    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_WAITLIST_TABLE = "CREATE TABLE " + AvatarEntry.TABLE_NAME + " (" +
                AvatarEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                AvatarEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                AvatarEntry.COLUMN_DATETIME + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                AvatarEntry.COLUMN_IMAGE + " BLOB NOT NULL " +
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_WAITLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AvatarEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AvatarEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}