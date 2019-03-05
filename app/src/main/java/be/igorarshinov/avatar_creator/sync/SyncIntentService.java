
package be.igorarshinov.avatar_creator.sync;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class SyncIntentService extends IntentService {
    private final String LOGTAG = "SyncIntentService";

    public SyncIntentService() {
        super("SyncIntentService");
        Log.i(LOGTAG, "SyncIntentService started");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SyncTask.syncAvatar(this);
    }
}