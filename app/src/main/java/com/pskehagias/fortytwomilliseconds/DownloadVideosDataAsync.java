package com.pskehagias.fortytwomilliseconds;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.pskehagias.fortytwomilliseconds.data.MoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;


/**
 * Created by Peter on 4/9/2016.
 */
public class DownloadVideosDataAsync extends AsyncTask<Long, Void, Void>{
    public static final String LOG_TAG = DownloadVideosDataAsync.class.getSimpleName();

    public static final String PARAM_API_KEY = "api_key";
    public static final String PARAM_LANGUAGE = "language";

    public final String KEY_RESULTS = "results";

    public final String API_BASE = "https://api.themoviedb.org/3/movie/";

    public static class VideosKeys{
        public static final String ID = "id";
        public static final String ISO_639 = "iso_639_1";
        public static final String ISO_3166 = "iso_3166_1";
        public static final String KEY = "key";
        public static final String NAME = "name";
        public static final String SITE = "site";
        public static final String SIZE = "size";
        public static final String TYPE = "type";
    }

    private Context mContext;

    public DownloadVideosDataAsync(Context context){
        mContext = context;
    }

    private ContentValues getVideosValuesFromJSON(JSONObject data) throws JSONException{
        ContentValues values = new ContentValues();

        values.put(MoviesContract.VideosEntry.COL_KEY, data.getString(VideosKeys.KEY));
        values.put(MoviesContract.VideosEntry.COL_ISO_639_1, data.getString(VideosKeys.ISO_639));
        values.put(MoviesContract.VideosEntry.COL_ISO_3166_1, data.getString(VideosKeys.ISO_3166));
        values.put(MoviesContract.VideosEntry.COL_NAME, data.getString(VideosKeys.NAME));
        values.put(MoviesContract.VideosEntry.COL_SITE, data.getString(VideosKeys.SITE));
        values.put(MoviesContract.VideosEntry.COL_SIZE, data.getInt(VideosKeys.SIZE));
        values.put(MoviesContract.VideosEntry.COL_TYPE, data.getString(VideosKeys.TYPE));
        values.put(MoviesContract.VideosEntry.COL_VIDEO_ID, data.getString(VideosKeys.ID));

        return values;
    }

    @Override
    protected Void doInBackground(Long... params) {
        DownloadTextHelper downloadTextHelper = new DownloadTextHelper();

        for(Long moviedb_id : params){
            try{
                String request = Uri.parse(API_BASE).buildUpon()
                        .appendPath(moviedb_id.toString())
                        .appendPath("videos")
                        .appendQueryParameter(PARAM_API_KEY, BuildConfig.THEMOVIEDB_API_KEY)
                        .build().toString();

                String rawJSON = downloadTextHelper.downloadHTTPS(request);

                JSONObject response = new JSONObject(rawJSON);
                JSONArray results = response.getJSONArray(KEY_RESULTS);

                for(int idx = 0; idx < results.length(); idx++){
                    ContentValues values = getVideosValuesFromJSON(results.getJSONObject(idx));
                    values.put(MoviesContract.VideosEntry.COL_MOVIEDB_ID, moviedb_id.longValue());

                    mContext.getContentResolver().insert(MoviesContract.VideosEntry.CONTENT_URI, values);
                }
            }catch(JSONException e){
                Log.e(LOG_TAG,"Error",  e);
            }
        }

        return null;
    }
}

//MoviesContract.VideosEntry
//    public static final String COL_MOVIEDB_ID = "moviedb_id";
//    public static final String COL_VIDEO_ID = "video_id";
//    public static final String COL_ISO_639_1 = "iso_639_1";
//    public static final String COL_ISO_3166_1 = "iso_3166_1";
//    public static final String COL_TYPE = "type";
//    public static final String COL_NAME = "name";
//    public static final String COL_SIZE = "size";
//    public static final String COL_SITE = "site";
//    public static final String COL_KEY = "key";