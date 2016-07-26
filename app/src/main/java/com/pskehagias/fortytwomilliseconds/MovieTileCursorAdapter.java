package com.pskehagias.fortytwomilliseconds;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by pkcyr on 4/8/2016.
 */
public class MovieTileCursorAdapter extends CursorAdapter {
    private static final String LOG_TAG = MovieTileCursorAdapter.class.getSimpleName();
    private static final int VIEW_TYPE_COUNT = 1;
    private static final int VIEW_TYPE_POSTER_TILE = 0;

    private String mPosterBasePath;
    private int mImageWidth;
    private int mImageHeight;

    public static class ViewHolder{
        public final ImageView posterView;

        ViewHolder(View view){
            posterView = (ImageView)view.findViewById(R.id.movie_tile);
        }
    }

    public MovieTileCursorAdapter(String posterBase, Context context, Cursor c, int flags) {
        super(context, c, flags);
        mPosterBasePath = posterBase;
        mImageWidth = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
            .getString(context.getString(R.string.setting_image_size_key), "w92").substring(1));
        mImageHeight = (int)(mImageWidth * 1.5);

    }

    public void setPosterBasePath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Base path cannot be null.");
        }
        mPosterBasePath = path;
    }

    public void setImageDimensions(int width, int height){
        if((width < 0) || (height < 0)){
            throw new IllegalArgumentException("Image size cannot have negative dimensions.");
        }
        mImageWidth = width;
        mImageHeight = height;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layout_id = -1;
        switch(getItemViewType(cursor.getPosition())){
            case VIEW_TYPE_POSTER_TILE:
                layout_id = R.layout.movie_tile;
                break;

        }
        View result = LayoutInflater.from(context).inflate(layout_id, parent, false);
        ViewHolder holder = new ViewHolder(result);
        result.setTag(holder);

        result.setLayoutParams(new AbsListView.LayoutParams(mImageWidth, mImageHeight));

        return result;

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        switch(getItemViewType(cursor.getPosition())){
            case VIEW_TYPE_POSTER_TILE:
                Picasso.with(context).load(mPosterBasePath + cursor.getString(MainActivityFragment.COL_POSTER_PATH)).
                        into(holder.posterView);
                holder.posterView.setScaleType(ImageView.ScaleType.FIT_XY);
                holder.posterView.setLayoutParams(new AbsListView.LayoutParams(mImageWidth, mImageHeight));
                holder.posterView.setContentDescription(cursor.getString(MainActivityFragment.COL_ORIGINAL_TITLE));
                break;
        }

    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_POSTER_TILE;
    }
}
