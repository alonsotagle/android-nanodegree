package com.alonsotagle.nanodegree.spotify2;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.alonsotagle.nanodegree.Application;
import com.alonsotagle.nanodegree.R;
import com.alonsotagle.nanodegree.spotify.SpotifyActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

public class SpotifyPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private final String LOG_TAG = SpotifyPlayerService.class.getSimpleName();
    public static final int NOTIFICATION_ID = 77;

    private SpotifyPlayerBinder spotifyPlayerBinder = new SpotifyPlayerBinder();
    private MediaPlayer spotifyPlayer;
    private ImageView mPlayerFragmentBackgroundImage;
    private ArrayList<ParcelableTrack> trackList;
    private int songIndex;
    private MusicController mMusicController;
    private Notification playerNotification;
    private boolean mTwoPane = false;
    private TextView mTrackNameTextView;
    private TextView mAlbumNameTextView;
    private TextView mArtistTextView;
    private boolean mIsShowNotification = true;
    private boolean mCancellable = false;

    // Class to communicate service with fragment
    public class SpotifyPlayerBinder extends Binder {
        public SpotifyPlayerService getService () { return SpotifyPlayerService.this;}
    }

    @Override
    public IBinder onBind(Intent intent) {
        return spotifyPlayerBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        songIndex = 0;

        spotifyPlayer = new MediaPlayer();

        initSpotifyPlayer();

        mIsShowNotification = IsShowNotificationFromPreference();
    }

    public void initSpotifyPlayer() {

        if(spotifyPlayer == null) {
            spotifyPlayer = new MediaPlayer();
        }

        spotifyPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        spotifyPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        spotifyPlayer.setOnCompletionListener(this);
        spotifyPlayer.setOnPreparedListener(this);
        spotifyPlayer.setOnErrorListener(this);
    }

    public void setTwoPane(boolean twoPane) {
        this.mTwoPane=twoPane;
    }

    public void setSongs(ArrayList<ParcelableTrack> theSongs) {
        trackList=theSongs;
    }

    public ArrayList<ParcelableTrack> getSongs()
    {
        return this.trackList;
    }

    public void setPlayerFragmentBackgroundImage(ImageView backgroundImageView) {
        this.mPlayerFragmentBackgroundImage = backgroundImageView;
    }

    public void setMusicController(MusicController mc)
    {
        this.mMusicController = mc;
    }

    public void setTrackDescriptionView(TextView artistName, TextView albumName, TextView trackName) {
        this.mTrackNameTextView = trackName;
        this.mAlbumNameTextView = albumName;
        this.mArtistTextView = artistName;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(spotifyPlayer.getCurrentPosition() > 0) {
            mediaPlayer.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        //start playback
        Application.setIsPlayingNow(true);
        spotifyPlayer.start();

        if(mMusicController != null) {

            try {
                mMusicController.show(0);
            } catch (Exception ex) {
                Log.v(LOG_TAG, "Error displaying media controller : " + ex.getMessage());
            }
        }

        mIsShowNotification = IsShowNotificationFromPreference();

        if(mIsShowNotification) {
            BuildNotification(android.R.drawable.ic_media_pause);
        } else {
            stopForeground(true);
        }
    }

    public void BuildNotification(final int playPauseControlResourceId) {

        final ParcelableTrack song = trackList.get(songIndex);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        //notification
        Intent notIntent = new Intent(this, SpotifyPlayerActivity.class);

        if(mTwoPane) {
            notIntent = new Intent(this, SpotifyActivity.class);
        }

        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendInt)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setTicker(song.getArtistName() + " - " + song.name)
                .setOngoing(true)
                .setContentTitle(song.getArtistName())
                .setContentText(song.name);

        String thumbnailImage = song.getThumbnailImage();

        if(thumbnailImage != null && (!thumbnailImage.isEmpty())) {

            Picasso.with(getApplicationContext())
                    .load(thumbnailImage)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            // referencing to the dimension resource XML just created
                            int bitmapWidth = Math.round(getResources().getDimension(R.dimen.notification_large_icon_width));
                            int bitmapHeight = Math.round(getResources().getDimension(R.dimen.notification_large_icon_height));
                            builder.setLargeIcon(bitmap);

                            playerNotification = builder.build();
                            playerNotification.bigContentView = getPlayerNotificationView(Bitmap.createScaledBitmap(bitmap, bitmapWidth, bitmapHeight, false), song.getArtistName(), song.name, playPauseControlResourceId);
                            playerNotification.flags |= NotificationCompat.FLAG_ONGOING_EVENT | NotificationCompat.FLAG_NO_CLEAR;
                            playerNotification.priority = Notification.PRIORITY_HIGH;
                            startForeground(NOTIFICATION_ID, playerNotification);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
        } else {
            playerNotification = builder.build();
            playerNotification.bigContentView = getPlayerNotificationView(null, song.getArtistName(), song.name, playPauseControlResourceId);
            playerNotification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
            startForeground(NOTIFICATION_ID, playerNotification);
        }
    }

    public void setSong(int songIndex) {
        this.songIndex = songIndex;
    }

    public int getPosn() {
        return spotifyPlayer.getCurrentPosition();
    }

    public int getDur() {
        return spotifyPlayer.getDuration();
    }

    public boolean isPng() {

        boolean playing = false;

        try {
            playing = spotifyPlayer.isPlaying();
            Application.setIsPlayingNow(playing);
        } catch (Exception ex) {
            Application.setIsPlayingNow(playing);
        } finally {
            return playing;
        }
    }

    public void pausePlayer() {
        Application.setIsPlayingNow(false);
        spotifyPlayer.pause();
        BuildNotification(android.R.drawable.ic_media_play);
    }

    public void stopPlayer() {
        Application.setIsPlayingNow(false);
        spotifyPlayer.stop();
        spotifyPlayer.release();
        stopForeground(true);
    }

    public void seek(int posn) {
        spotifyPlayer.seekTo(posn);
    }

    public void go() {
        Application.setIsPlayingNow(true);
        spotifyPlayer.start();
        BuildNotification(android.R.drawable.ic_media_pause);
    }

    public void playSong() {
        //play a song
        spotifyPlayer.reset();

        //get song
        ParcelableTrack playSong = trackList.get(songIndex);

        //set uri
        Uri trackUri = Uri.parse(playSong.preview_url);

        try {
            spotifyPlayer.setDataSource(getApplicationContext(), trackUri);
            UpdatePlayerBackgroundImage(playSong);
            UpdatePlayerTextDescription(playSong);
            Application.setCurrentTrackUrl(playSong.preview_url);
        } catch(Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        spotifyPlayer.prepareAsync();
    }

    //skip to previous track
    public void playPrev() {
        songIndex--;
        if(songIndex < 0) {
            songIndex = trackList.size() - 1;
        }
        playSong();
    }

    //skip to next
    public void playNext() {
        songIndex++;
        if (songIndex >= trackList.size()) {
            songIndex = 0;
        }
        playSong();
    }

    public void UpdatePlayerFragmentView() {
        if(this.trackList != null && this.trackList.size() > 0 && this.trackList.size() > this.songIndex) {
            ParcelableTrack playSong = trackList.get(songIndex);

            UpdatePlayerBackgroundImage(playSong);

            UpdatePlayerTextDescription(playSong);

            mMusicController.setVisibility(View.VISIBLE);
        }
    }

    private RemoteViews getPlayerNotificationView(Bitmap trackThumbnail, String artistName, String trackTitle, int playPauseControlResourceId) {

        final RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.service_player_notification);

        if(trackThumbnail != null) {
            notificationView.setImageViewBitmap(R.id.notification_track_thumbnail, trackThumbnail);
        } else {
            notificationView.setImageViewResource(R.id.notification_track_thumbnail, android.R.drawable.ic_media_play);
        }

        notificationView.setImageViewResource(R.id.notification_play_button, playPauseControlResourceId);

        notificationView.setTextViewText(R.id.notification_artist_name, artistName);
        notificationView.setTextViewText(R.id.notification_track_title, trackTitle);

        //this is the intent that is supposed to be called when the button is clicked
        Intent notificationPlayIntent = new Intent(this, NotificationPlayPauseButtonListener.class);
        PendingIntent pendingNotificationPlayIntent = PendingIntent.getBroadcast(this, 0, notificationPlayIntent, 0);

        Intent notificationPrevIntent = new Intent(this, NotificationPrevButtonListener.class);
        PendingIntent pendingNotificationPrevIntent = PendingIntent.getBroadcast(this, 0, notificationPrevIntent, 0);

        Intent notificationNextIntent = new Intent(this, NotificationNextButtonListener.class);
        PendingIntent pendingNotificationNextIntent = PendingIntent.getBroadcast(this, 0, notificationNextIntent, 0);

        Intent notificationCancelIntent = new Intent(this, NotificationCancelButtonListener.class);
        notificationCancelIntent.putExtra("isTablet", mTwoPane);
        PendingIntent pendingNotificationCancelIntent = PendingIntent.getBroadcast(this, 0, notificationCancelIntent, Intent.FILL_IN_DATA);

        notificationView.setOnClickPendingIntent(R.id.notification_play_button, pendingNotificationPlayIntent);
        notificationView.setOnClickPendingIntent(R.id.notification_prev_button, pendingNotificationPrevIntent);
        notificationView.setOnClickPendingIntent(R.id.notification_next_button, pendingNotificationNextIntent);
        notificationView.setOnClickPendingIntent(R.id.notification_cancel_button, pendingNotificationCancelIntent);

        return notificationView;
    }

    private void UpdatePlayerBackgroundImage(ParcelableTrack playSong) {
        if(playSong.getPlaybackImage() != null && this.mPlayerFragmentBackgroundImage != null) {
            Picasso.with(this).load(playSong.getPlaybackImage()).fit().centerCrop().into(this.mPlayerFragmentBackgroundImage);
        }
    }

    private void UpdatePlayerTextDescription(ParcelableTrack playSong) {
        if(this.mTrackNameTextView != null && this.mAlbumNameTextView != null && this.mArtistTextView != null) {
            this.mTrackNameTextView.setText(playSong.name);
            this.mAlbumNameTextView.setText(playSong.getalbumName());
            this.mArtistTextView.setText(playSong.getArtistName());
        }
    }

    private boolean IsShowNotificationFromPreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean showNotificationPreference = sharedPrefs.getBoolean(SettingsActivity.SHOW_NOTIFICATION_PREFERENCE_ID, true);

        return showNotificationPreference;
    }

    public void setmCancellable(boolean mCancellable) {
        this.mCancellable = mCancellable;
    }

    public boolean getmCancellable() {
        return mCancellable;
    }
}