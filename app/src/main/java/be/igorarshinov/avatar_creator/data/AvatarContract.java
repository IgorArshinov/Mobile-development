package be.igorarshinov.avatar_creator.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class AvatarContract {

    public static final String CONTENT_AUTHORITY = "be.igorarshinov.avatar_creator";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_AVATARS = "avatars";

    public static final class AvatarEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_AVATARS)
                .build();

        public static final String TABLE_NAME = "avatars";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DATETIME = "datetime";
        public static final String COLUMN_IMAGE = "image";
    }
}
