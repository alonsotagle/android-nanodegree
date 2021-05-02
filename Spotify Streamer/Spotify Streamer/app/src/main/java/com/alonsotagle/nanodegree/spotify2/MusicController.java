package com.alonsotagle.nanodegree.spotify2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.widget.MediaController;

import com.alonsotagle.nanodegree.Application;
import com.alonsotagle.nanodegree.spotify.SpotifyActivity;

/**
 * Created by AlonsoTagle on 09/08/15.
 */
public class MusicController extends MediaController {

    private Context mContext;
    private boolean mTwoPane;

    public MusicController(Context c, boolean twoPane){
        super(c);
        this.mContext = c;
        this.mTwoPane = twoPane;
    }

    public void hide(){}

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK  && !mTwoPane) {
            ((Activity) mContext).finish();

            if(Application.isLastActivity(SpotifyPlayerActivity.class.getName())) {
                Intent intent = new Intent(mContext, SpotifyActivity.class);
                mContext.startActivity(intent);
            }
        }

        return super.dispatchKeyEvent(event);
    }
}
