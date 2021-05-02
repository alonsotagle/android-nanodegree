package barqsoft.footballscores.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by alonsotagle on 14.01.16.
 */
public class FootballScoreSyncService extends Service {
    public static final Object sSyncAdapterLock = new Object();
    private static FootballScoreSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("FootballSyncService", "onCreate - FootballScoreSyncService");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new FootballScoreSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
