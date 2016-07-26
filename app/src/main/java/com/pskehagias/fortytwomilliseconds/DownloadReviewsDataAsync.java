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

/**
 * Created by Peter on 4/9/2016.
 */
public class DownloadReviewsDataAsync extends AsyncTask<Long, Void, Void> {
    private static final String LOG_TAG = DownloadReviewsDataAsync.class.getSimpleName();

    public static final String PARAM_API_KEY = "api_key";
    public final String KEY_RESULTS = "results";
    public final String KEY_REVIEW_COUNT = "total_results";
    public final String API_BASE = "https://api.themoviedb.org/3/movie/";

    public static class ReviewKeys{
        public static final String ID = "id";
        public static final String AUTHOR = "author";
        public static final String CONTENT = "content";
        public static final String WEBLINK = "url";
    }

    private Context mContext;

    public DownloadReviewsDataAsync(Context context){
        mContext = context;
    }

    private ContentValues getReviewValuesFromJSON(JSONObject data) throws JSONException{
        ContentValues result = new ContentValues();

        result.put(MoviesContract.ReviewEntry.COL_AUTHOR, data.getString(ReviewKeys.AUTHOR));
        result.put(MoviesContract.ReviewEntry.COL_REVIEW_ID, data.getString(ReviewKeys.ID));
        result.put(MoviesContract.ReviewEntry.COL_REVIEW_TEXT, data.getString(ReviewKeys.CONTENT));
        result.put(MoviesContract.ReviewEntry.COL_WEBLINK, data.getString(ReviewKeys.WEBLINK));

        return result;
    }

    @Override
    protected Void doInBackground(Long... params) {
        DownloadTextHelper downloadTextHelper = new DownloadTextHelper();
        for(Long moviedb_id : params){
            try{
                String request = Uri.parse(API_BASE).buildUpon()
                        .appendPath(moviedb_id.toString())
                        .appendPath("reviews")
                        .appendQueryParameter(PARAM_API_KEY, BuildConfig.THEMOVIEDB_API_KEY)
                        .build().toString();

                String rawJSON = downloadTextHelper.downloadHTTPS(request);
                JSONObject reply = new JSONObject(rawJSON);
                JSONArray results = reply.getJSONArray(KEY_RESULTS);
                int review_count = reply.getInt(KEY_REVIEW_COUNT);

                for(int idx = 0; idx < results.length(); idx++){
                    ContentValues values = getReviewValuesFromJSON(results.getJSONObject(idx));
                    values.put(MoviesContract.ReviewEntry.COL_MOVIEDB_ID, moviedb_id.longValue());

                    mContext.getContentResolver().insert(MoviesContract.ReviewEntry.CONTENT_URI, values);
                }

                ContentValues reviewCountValues = new ContentValues();
                reviewCountValues.put(MoviesContract.ReviewCountEntry.COL_COUNT, review_count);
                reviewCountValues.put(MoviesContract.ReviewCountEntry._ID, moviedb_id.longValue());
                mContext.getContentResolver().insert(MoviesContract.ReviewCountEntry.CONTENT_URI, reviewCountValues);

            }catch(JSONException e){
                Log.e(LOG_TAG, "Error", e);
            }

        }

        return null;
    }
}



//MoviesContract.ReviewEntry
//    public static final String COL_MOVIEDB_ID = "moviedb_id";
//    public static final String COL_REVIEW_ID = "review_id";
//    public static final String COL_AUTHOR = "author";
//    public static final String COL_REVIEW_TEXT = "review_text";
//    public static final String COL_WEBLINK = "weblink";