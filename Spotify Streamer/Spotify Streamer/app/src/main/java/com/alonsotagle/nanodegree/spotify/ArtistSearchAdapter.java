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

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by AlonsoTagle on 12/06/15.
 */
public class ArtistSearchAdapter extends UtilAdapter<Artist> {

    @Override
    public View getView(int i, View view, @Nullable ViewGroup viewGroup) {

        ArtistsSearchHolder ArtistsSearchHolder = new ArtistsSearchHolder();
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spotify_list_item_search, viewGroup, false);
            ArtistsSearchHolder.artistsImage = (ImageView) view.findViewById(R.id.album_image);
            ArtistsSearchHolder.artistsName = (TextView) view.findViewById(R.id.artist_name);
            view.setTag(ArtistsSearchHolder);
        }else{
            ArtistsSearchHolder = (ArtistsSearchHolder)view.getTag();
        }

        if(getItems().get(i).images.size() > 0 && getItems().get(i).images.get(1).url != null) {
            Picasso.with(viewGroup.getContext())
                    .load(getItems().get(i).images.get(1).url)
                    .resize(200, 200)
                    .into(ArtistsSearchHolder.artistsImage);
        }
        
        ArtistsSearchHolder.artistsName.setText(getItems().get(i).name);

        return view;
    }

    public static class ArtistsSearchHolder {
        public ImageView artistsImage;
        public TextView artistsName;
    }
}
