package com.pskehagias.fortytwomilliseconds;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.pskehagias.fortytwomilliseconds.data.MoviesContract;

/**
 * Created by Peter on 4/17/2016.
 */
public class VideoSpinnerCursorAdapter extends CursorAdapter {

    public VideoSpinnerCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View result = LayoutInflater.from(context).inflate(R.layout.videos_spinner_item, parent, false);
        return result;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.video_spinner_name);
        name.setText(cursor.getString(MovieActivityFragment.COL_VID_NAME));
    }
}
