package be.igorarshinov.avatar_creator.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import be.igorarshinov.avatar_creator.data.AvatarContract;

public class JsonUtils {

    private static final String LOGTAG = "JsonUtils";
    private static final String BASE_URL =
            "https://my.api.mockaroo.com/avatars";
    private static final String TYPE =
            ".json";
    private static final String API_KEY = ".json?key=b85e1df0";
    private static final String KEY = "key";

    public static URL buildUrl() {

        URL url = null;
        Uri uri = Uri.parse(BASE_URL).buildUpon().build();

        try {
            url = new URL(uri.toString().concat(API_KEY));
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage());
        }

        return url;
    }

    public static URL buildUrlWithName(String name) {

        URL url = null;
        Uri uri = Uri.parse(BASE_URL).buildUpon().appendPath(name).build();

        try {
            url = new URL(uri.toString().concat(API_KEY));
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage());
        }

        return url;
    }

    public static String getJSONFromUrl(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            InputStream stream = connection.getInputStream();
            Scanner scanner = new Scanner(stream);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            stream.close();
            return response;
        } finally {
            connection.disconnect();
        }
    }

    synchronized public static ContentValues[] parseAvatarJSONObjectFromUrl(String url, Context context) {
        final String ID = "id";

        ContentValues[] avatars = null;

        try {

            Object json = new JSONTokener(url).nextValue();
            if (json instanceof JSONObject) {

                JSONObject jsonAvatar = (JSONObject) json;
            } else if (json instanceof JSONArray) {
                JSONArray jsonArrayAvatars = (JSONArray) json;
                avatars = new ContentValues[jsonArrayAvatars.length()];

                for (int i = 0; i < jsonArrayAvatars.length(); i++) {
                    JSONObject avatarJSON = jsonArrayAvatars.getJSONObject(i);
                    URL urlOfImage = new URL(avatarJSON.getString(AvatarContract.AvatarEntry.COLUMN_IMAGE));

                    HttpURLConnection connection = (HttpURLConnection) urlOfImage.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap image = BitmapFactory.decodeStream(input);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte imageInByte[] = stream.toByteArray();

                    ContentValues avatarValue = new ContentValues();
                    avatarValue.put(AvatarContract.AvatarEntry._ID, avatarJSON.getString(ID));
                    avatarValue.put(AvatarContract.AvatarEntry.COLUMN_NAME, avatarJSON.getString(AvatarContract.AvatarEntry.COLUMN_NAME));
                    avatarValue.put(AvatarContract.AvatarEntry.COLUMN_DATETIME, avatarJSON.getString(AvatarContract.AvatarEntry.COLUMN_DATETIME));
                    avatarValue.put(AvatarContract.AvatarEntry.COLUMN_IMAGE, imageInByte);

                    avatars[i] = avatarValue;
                }
            }
        } catch (JSONException | IOException e) {
            Log.e(LOGTAG, e.getMessage());
        }

        return avatars;
    }
}
