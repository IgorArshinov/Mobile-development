
package be.igorarshinov.avatar_creator.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import java.net.URL;

import be.igorarshinov.avatar_creator.data.AvatarContract;
import be.igorarshinov.avatar_creator.utils.JsonUtils;

public class SyncTask {
    private static final String LOGTAG = "SyncTask";

    synchronized public static void syncAvatar(Context context) {

        try {

            URL avatarRequestUrl = JsonUtils.buildUrl();

            String jsonAvatarResponse = JsonUtils.getJSONFromUrl(avatarRequestUrl);

            ContentValues[] avatarValues = JsonUtils.parseAvatarJSONObjectFromUrl(jsonAvatarResponse, context);
            if (avatarValues != null && avatarValues.length != 0) {

                ContentResolver sunshineContentResolver = context.getContentResolver();

                sunshineContentResolver.delete(
                        AvatarContract.AvatarEntry.CONTENT_URI,
                        null,
                        null);

                sunshineContentResolver.bulkInsert(
                        AvatarContract.AvatarEntry.CONTENT_URI,
                        avatarValues);
            }
        } catch (Exception e) {

            Log.e(LOGTAG, e.getMessage());
        }
    }
}