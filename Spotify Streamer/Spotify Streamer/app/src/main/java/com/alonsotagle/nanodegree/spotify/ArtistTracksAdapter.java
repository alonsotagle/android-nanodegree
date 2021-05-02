package com.alonsotagle.nanodegree.spotify;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alonsotagle.nanodegree.R;
import com.alonsotagle.nanodegree.utils.UtilAdapter;
import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by AlonsoTagle on 18/06/15.
 */
public class ArtistTracksAdapter extends UtilAdapter<Track> {

    @Override
    public View getView(int i, View view, @Nullable ViewGroup viewGroup) {

        TopArtistSongsHolder topArtistSongsHolder = new TopArtistSongsHolder();
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spotify_list_item_tracks, viewGroup, false);
            topArtistSongsHolder.albumImage = (ImageView) view.findViewById(R.id.album_image);
            topArtistSongsHolder.songName = (TextView) view.findViewById(R.id.song_name);
            topArtistSongsHolder.albumName = (TextView) view.findViewById(R.id.album_name);

            view.setTag(topArtistSongsHolder);
        } else {
            topArtistSongsHolder = (TopArtistSongsHolder) view.getTag();
        }

        if (getItems().get(i).album.images != null && getItems().get(i).album.images.get(1).url != null) {
            Picasso.with(viewGroup.getContext())
                    .load(getItems().get(i).album.images.get(1).url)
                    .into(topArtistSongsHolder.albumImage);
        }
        topArtistSongsHolder.songName.setText(getItems().get(i).name);
        topArtistSongsHolder.albumName.setText(getItems().get(i).album.name);

        return view;

    }

    public static class TopArtistSongsHolder {
        public ImageView albumImage;
        public TextView songName;
        public TextView albumName;
    }
}