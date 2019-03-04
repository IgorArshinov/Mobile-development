package be.igorarshinov.avatar_creator.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

public class AvatarProvider extends ContentProvider {
    private static final String LOGTAG = "AvatarProvider";
    private DatabaseHelper databaseHelper;

    public static final int AVATARS = 100;
    public static final int AVATARS_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AvatarContract.CONTENT_AUTHORITY, AvatarContract.PATH_AVATARS, AVATARS);
        uriMatcher.addURI(AvatarContract.CONTENT_AUTHORITY, AvatarContract.PATH_AVATARS + "/#", AVATARS_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();

        database.beginTransaction();
        int rowsInserted = 0;
        try {
            for (ContentValues value : values) {

                long _id = database.insert(AvatarContract.AvatarEntry.TABLE_NAME, null, value);
                if (_id != -1) {
                    rowsInserted++;
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        if (rowsInserted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsInserted;
    }

    @Override
    public Cursor query(@NonNull Uri uri, @NonNull String[] projection, @NonNull String selection, @NonNull String[] selectionArgs, @NonNull String sortOrder) {
        Cursor cursor;

        switch (uri.toString()) {

            default:
                cursor = databaseHelper.getReadableDatabase().query(
                        AvatarContract.AvatarEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, @NonNull ContentValues values) {
        final SQLiteDatabase database = databaseHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case AVATARS:

                long id = database.insert(AvatarContract.AvatarEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(AvatarContract.AvatarEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @NonNull String selection, @NonNull String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @NonNull ContentValues values, @NonNull String selection, @NonNull String[] selectionArgs) {
        return 0;
    }
}
