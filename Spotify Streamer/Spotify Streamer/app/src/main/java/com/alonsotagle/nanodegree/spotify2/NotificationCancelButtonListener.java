package com.alonsotagle.nanodegree.spotify2;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.alonsotagle.nanodegree.spotify.SpotifyActivity;

/**
 * Created by AlonsoTagle on 22/08/15.
 */
public class NotificationCancelButtonListener extends BroadcastReceiver {

    private SpotifyPlayerService mPlayerService;

    public NotificationCancelButtonListener() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        IBinder binder = peekService(context, new Intent(context, SpotifyPlayerService.class));

        if (binder != null) {
            this.mPlayerService = ((SpotifyPlayerService.SpotifyPlayerBinder) binder).getService();
        }

        if(this.mPlayerService != null) {


            this.mPlayerService.setmCancellable(true);

            this.mPlayerService.stopPlayer();

            context.stopService(new Intent(context, SpotifyPlayerService.class));

            boolean alo = intent.getBooleanExtra("isTablet", false);
            if (alo) {
                SpotifyPlayerFragment.mDialogFragment.dismiss();
                SpotifyActivity.activity.onWindowFocusChanged(true);
            } else {
                SpotifyPlayerActivity.activity.finish();
            }

            NotificationManager notificationmanager = (NotificationManager) context.getSystemService(SpotifyPlayerService.NOTIFICATION_SERVICE);
            notificationmanager.cancel(SpotifyPlayerService.NOTIFICATION_ID);
        }
    }
}
