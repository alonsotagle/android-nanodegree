package com.alonsotagle.nanodegree.spotify2;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alonsotagle.nanodegree.Application;
import com.alonsotagle.nanodegree.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SpotifyPlayerFragment extends DialogFragment implements MediaPlayerControl {

    private boolean mTwoPane = false;
    static final String TRACKINDEX = "TRACKINDEX";
    static final String TOPTENTRACKS_PARCELABLE = "TOPTENTRACKSPARCELABLE";

    protected TextView artistName;
    protected TextView albumName;
    protected TextView trackName;
    protected ImageView mBackgroundImage;
    protected RelativeLayout mTrackDescriptionRelativeLayout;
    protected FrameLayout mControllerAnchorFrameLayout;
    protected ImageView share_button;

    private ArrayList<ParcelableTrack> mTrackList;
    private int mTrackIndex;
    private String TRACK_KEY = "track_list";
    private View fragmentView;
    private SpotifyPlayerService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private MusicController controller;
    private boolean playbackPaused = false;
    public static DialogFragment mDialogFragment;

    public void setTwoPane(boolean twoPane) {
        this.mTwoPane = twoPane;
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            SpotifyPlayerService.SpotifyPlayerBinder binder = (SpotifyPlayerService.SpotifyPlayerBinder) service;

            //get service
            musicSrv = binder.getService();

            musicSrv.setTwoPane(mTwoPane);

            if(mBackgroundImage != null) {
                musicSrv.setPlayerFragmentBackgroundImage(mBackgroundImage);
            }

            if(controller != null) {
                musicSrv.setMusicController(controller);
            }

            if(artistName != null && albumName != null && trackName != null) {
                musicSrv.setTrackDescriptionView(artistName, albumName, trackName);
            }

            if(mTrackList != null) {
                musicSrv.setSongs(mTrackList);
                songPicked();
            } else {
                musicSrv.UpdatePlayerFragmentView();
                fragmentView.post(new Runnable() {

                    @Override
                    public void run() {
                        controller.show(1000);
                    }
                });
            }
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicSrv.setMusicController(null);
            musicBound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if(playIntent == null) {
            playIntent = new Intent(getActivity(), SpotifyPlayerService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_ADJUST_WITH_ACTIVITY);
            getActivity().startService(playIntent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {

        if(controller.isShowing()) {
            controller.hide();
        }

        getActivity().unbindService(musicConnection);

        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.spotify_player_layout, container, false);

        artistName = (TextView)fragmentView.findViewById(R.id.player_artist_name);
        albumName = (TextView)fragmentView.findViewById(R.id.player_album_name);
        trackName = (TextView)fragmentView.findViewById(R.id.player_track_name);
        mBackgroundImage = (ImageView)fragmentView.findViewById(R.id.player_album_image);
        mControllerAnchorFrameLayout = (FrameLayout)fragmentView.findViewById(R.id.controllerAnchor);
        mTrackDescriptionRelativeLayout = (RelativeLayout)fragmentView.findViewById(R.id.track_description);
        share_button = (ImageView)fragmentView.findViewById(R.id.share_button);

        share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String trackUrl = Application.getCurrentTrackUrl();

                if (trackUrl != null && (!trackUrl.isEmpty())) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, trackUrl);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_with)));
                }
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(TRACK_KEY)) {
            mTrackList = savedInstanceState.getParcelableArrayList(TRACK_KEY);
        } else {

            Bundle arguments = getArguments();

            if(arguments != null) {
                Gson gson = new GsonBuilder().create();
                Type parcelableTrack = new TypeToken<List<ParcelableTrack>>() {}.getType();
                mTrackList = gson.fromJson(arguments.getString(SpotifyPlayerFragment.TOPTENTRACKS_PARCELABLE), parcelableTrack);
                mTrackIndex = arguments.getInt(SpotifyPlayerFragment.TRACKINDEX);
            }
        }

        //setup controller
        setController();

        return fragmentView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        getActivity().onWindowFocusChanged(true);
        mDialogFragment = this;

        return dialog;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (!musicSrv.getmCancellable()) {
            return musicSrv.getPosn();
        } else {
            return 0;
        }
    }

    @Override
    public int getDuration() {
        if (!musicSrv.getmCancellable()) {
            return musicSrv.getDur();
        } else {
            return 0;
        }
    }

    @Override
    public boolean isPlaying() {
        return musicSrv != null && musicBound && musicSrv.isPng();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicSrv.pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    //set the controller up
    private void setController() {

        if(mTwoPane && getDialog() != null) {
            controller = new MusicController(getDialog().getContext(), mTwoPane);
        } else {
            controller = new MusicController(getActivity(), mTwoPane);
        }

        //set previous and next button listeners
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setAnchorView(mTrackDescriptionRelativeLayout);
        controller.setMediaPlayer(this);
        controller.setEnabled(true);

        if(mTwoPane) {
            ((ViewGroup) controller.getParent()).removeView(controller);
            mControllerAnchorFrameLayout.addView(controller);
        }
    }

    private void playNext() {
        musicSrv.playNext();
        if(playbackPaused) {
            setController();
            playbackPaused = false;
        }

    }

    private void playPrev() {
        musicSrv.playPrev();
        if(playbackPaused) {
            setController();
            playbackPaused = false;
        }

    }

    //user song select
    public void songPicked() {
        musicSrv.setSong(mTrackIndex);
        musicSrv.playSong();
        if(playbackPaused) {
            setController();
            playbackPaused = false;
        }
    }

    public void onPlayTracks(String tracks, int trackSelectedPosition) {

        if (tracks != null) {
            Gson gson = new GsonBuilder().create();
            Type trackAdapterType = new TypeToken<List<ParcelableTrack>>() {
            }.getType();
            mTrackList = gson.fromJson(tracks, trackAdapterType);
            mTrackIndex = trackSelectedPosition;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        getActivity().onWindowFocusChanged(true);
    }
}
