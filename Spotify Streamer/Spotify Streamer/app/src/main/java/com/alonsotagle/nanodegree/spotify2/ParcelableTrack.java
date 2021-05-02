package com.alonsotagle.nanodegree.spotify2;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by AlonsoTagle on 09/08/15.
 */
public class ParcelableTrack extends Track implements Parcelable {

    private String thumbnailImage;
    private String playbackImage;
    private String albumName;
    private String artistName;
    private List<String> mParcelableString;

    private enum ParcelableTrackIndex {
        TRACK_ID(0), TRACK_NAME(1), TRACK_ALBUM_NAME(2), TRACK_THUMBNAIL(3), TRACK_PLAYBACKIMAGE(4), TRACK_PREVIEWURL(5), TRACK_ARTISTNAME(6);

        private int value;

        ParcelableTrackIndex(int value)
        {
            this.value = value;
        }
    }

    ParcelableTrack(Track track) {
        this.id = track.id;
        this.name = track.name;
        this.album = track.album;
        this.preview_url = track.preview_url;
        this.artists = track.artists;
    }

    ParcelableTrack(Parcel in) {
        mParcelableString = new ArrayList<String>();
        in.readStringList(this.mParcelableString);
        this.id = mParcelableString.get(ParcelableTrackIndex.TRACK_ID.ordinal());
        this.name = mParcelableString.get(ParcelableTrackIndex.TRACK_NAME.ordinal());
        this.albumName = mParcelableString.get(ParcelableTrackIndex.TRACK_ALBUM_NAME.ordinal());
        this.thumbnailImage = mParcelableString.get(ParcelableTrackIndex.TRACK_THUMBNAIL.ordinal());
        this.playbackImage = mParcelableString.get(ParcelableTrackIndex.TRACK_PLAYBACKIMAGE.ordinal());
        this.preview_url = mParcelableString.get(ParcelableTrackIndex.TRACK_PREVIEWURL.ordinal());
        this.artistName = mParcelableString.get(ParcelableTrackIndex.TRACK_ARTISTNAME.ordinal());
    }

    public void setAlbumName(String value) {
        this.albumName = value;
    }

    public String getalbumName() {

        if(this.albumName == null) {
            return GetAlbumNameFromTrack();
        } else {
            return this.albumName;
        }
    }

    private String GetAlbumNameFromTrack() {

        if(this.album == null) {
            return null;
        } else {
            return this.album.name;
        }
    }

    public void setThumbnailImage(String value) {
        this.thumbnailImage = value;
    }

    public void setPlaybackImage(String value) {
        this.playbackImage = value;
    }

    public String getThumbnailImage() {

        if(this.thumbnailImage == null) {
            return GetThumbnailImageFromTrack();
        } else {
            return this.thumbnailImage;
        }
    }

    public String getPlaybackImage() {

        if(this.playbackImage == null) {
            return GetPlaybackImageFromTrack();
        } else {
            return this.playbackImage;
        }
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistName() {
        if(this.artistName == null) {
            return this.artists.get(0).name;
        } else {
            return this.artistName;
        }
    }

    private String GetThumbnailImageFromTrack() {
        if(this.album != null && this.album.images.size() > 0) {
            if(this.album.images.size() == 1) {
                return this.album.images.get(0).url;
            } else if(this.album.images.size() >= 3) {
                return this.album.images.get(2).url;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private String GetPlaybackImageFromTrack() {
        if(this.album != null && this.album.images.size() > 0) {
            return this.album.images.get(0).url;
        } else {
            return null;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        ArrayList<String> values = new ArrayList<String>();
        values.add(ParcelableTrackIndex.TRACK_ID.ordinal(),this.id);
        values.add(ParcelableTrackIndex.TRACK_NAME.ordinal(),this.name);
        values.add(ParcelableTrackIndex.TRACK_ALBUM_NAME.ordinal(),getalbumName());
        values.add(ParcelableTrackIndex.TRACK_THUMBNAIL.ordinal(),getThumbnailImage());
        values.add(ParcelableTrackIndex.TRACK_PLAYBACKIMAGE.ordinal(),getPlaybackImage());
        values.add(ParcelableTrackIndex.TRACK_PREVIEWURL.ordinal(),this.preview_url);
        values.add(ParcelableTrackIndex.TRACK_ARTISTNAME.ordinal(),this.artistName);
        dest.writeStringList(values);
    }

    public static final Parcelable.Creator<ParcelableTrack> CREATOR = new Parcelable.Creator<ParcelableTrack>() {

        public ParcelableTrack createFromParcel(Parcel in) {
            return new ParcelableTrack(in);
        }

        public ParcelableTrack[] newArray(int size) {
            return new ParcelableTrack[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
