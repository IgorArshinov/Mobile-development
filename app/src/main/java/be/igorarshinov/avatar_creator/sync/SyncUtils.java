
package be.igorarshinov.avatar_creator.sync;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import be.igorarshinov.avatar_creator.data.AvatarContract;

public class SyncUtils {

    private static boolean initialized;
    private static final String LOGTAG = "SyncUtils";

    synchronized public static void initialize(@NonNull final Context context) {

        if (initialized) return;

        initialized = true;

        new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... voids) {

                Uri forecastQueryUri = AvatarContract.AvatarEntry.CONTENT_URI;

                String[] projectionColumns = {AvatarContract.AvatarEntry._ID};

                Cursor cursor = context.getContentResolver().query(
                        forecastQueryUri,
                        projectionColumns,
                        null,
                        null,
                        null);

                if (null == cursor || cursor.getCount() == 0) {
                    startImmediateSync(context);
                }

                cursor.close();

                return null;
            }
        }.execute();
    }

    public static void startImmediateSync(@NonNull final Context context) {

        Intent intentToSyncImmediately = new Intent(context, SyncIntentService.class);
        Log.i(LOGTAG, "intentToSyncImmediately: " + intentToSyncImmediately);

        context.startService(intentToSyncImmediately);
    }
}