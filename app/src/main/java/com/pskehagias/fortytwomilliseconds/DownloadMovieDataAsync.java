package com.pskehagias.fortytwomilliseconds;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import com.pskehagias.fortytwomilliseconds.data.MoviesContract;
import com.pskehagias.fortytwomilliseconds.data.MoviesContract.MovieEntry;

/**
 * Created by pkcyr on 1/21/2016.
 */
public class DownloadMovieDataAsync extends AsyncTask<String,Void,Void> {

    public final String LOG_TAG = DownloadMovieDataAsync.class.getSimpleName();

    public final String API_KEY = "api_key";
    public final String API_BASE = "https://api.themoviedb.org/3/discover/movie";

    public final String SORT_KEY = "sort_by";
    public final String PAGE_KEY = "page";
    public final String RESULT_KEY = "results";

    private Context mContext;

    public DownloadMovieDataAsync(Context context){
        mContext = context;
    }

    public static class MoviesKeys{
        public static final String KEY_ADULT = "adult";
        public static final String KEY_BACKDROP_PATH = "backdrop_path";
        public static final String KEY_GENRE_IDS = "genre_ids";
        public static final String KEY_ID = "id";
        public static final String KEY_ORIGINAL_LANGUAGE = "original_language";
        public static final String KEY_ORIGINAL_TITLE = "original_title";
        public static final String KEY_OVERVIEW = "overview";
        public static final String KEY_RELEASE_DATE = "release_date";
        public static final String KEY_POSTER_PATH = "poster_path";
        public static final String KEY_POPULARITY = "popularity";
        public static final String KEY_TITLE = "title";
        public static final String KEY_VIDEO = "video";
        public static final String KEY_VOTE_AVERAGE = "vote_average";
        public static final String KEY_VOTE_COUNT = "vote_count";
    }

    private ContentValues getMovieEntryValuesFromJSON(JSONObject movieJSON) throws JSONException{
        ContentValues result = new ContentValues();

        result.put(MovieEntry._ID, movieJSON.getLong(MoviesKeys.KEY_ID));
        result.put(MovieEntry.COL_ORIGINAL_TITLE, movieJSON.getString(MoviesKeys.KEY_ORIGINAL_TITLE));
        result.put(MovieEntry.COL_POPULARITY, movieJSON.getDouble(MoviesKeys.KEY_POPULARITY));
        result.put(MovieEntry.COL_POSTER_PATH, movieJSON.getString(MoviesKeys.KEY_POSTER_PATH));
        result.put(MovieEntry.COL_RATING, movieJSON.getDouble(MoviesKeys.KEY_VOTE_AVERAGE));
        result.put(MovieEntry.COL_RATING_COUNT, movieJSON.getLong(MoviesKeys.KEY_VOTE_COUNT));
        result.put(MovieEntry.COL_SYNOPSIS, movieJSON.getString(MoviesKeys.KEY_OVERVIEW));

        result.put(MovieEntry.COL_ADULT, movieJSON.getBoolean(MoviesKeys.KEY_ADULT));
        result.put(MovieEntry.COL_BACKDROP_PATH, movieJSON.getString(MoviesKeys.KEY_BACKDROP_PATH));
        result.put(MovieEntry.COL_ORIGINAL_LANGUAGE, movieJSON.getString(MoviesKeys.KEY_ORIGINAL_LANGUAGE));
        result.put(MovieEntry.COL_RELEASE_DATE, movieJSON.getString(MoviesKeys.KEY_RELEASE_DATE));
        result.put(MovieEntry.COL_VIDEO, movieJSON.getBoolean(MoviesKeys.KEY_VIDEO));
        result.put(MovieEntry.COL_TITLE, movieJSON.getString(MoviesKeys.KEY_TITLE));

        return result;
    }

    private ContentValues getFavoritesValues(JSONObject movieJSON) throws JSONException{
        ContentValues result = new ContentValues();

        result.put(MoviesContract.FavoritesEntry._ID, movieJSON.getLong(MoviesKeys.KEY_ID));

        return result;
    }

    @Override
    protected Void doInBackground(String... params) {
        String query = null;

        if(params.length == 2) {
            query = Uri.parse(API_BASE).buildUpon()
                    .appendQueryParameter(API_KEY, BuildConfig.THEMOVIEDB_API_KEY)
                    .appendQueryParameter(SORT_KEY, params[0])
                    .appendQueryParameter(PAGE_KEY, params[1])
                    .build().toString();
        }else if(params.length == 3) {
            query = Uri.parse(API_BASE).buildUpon()
                    .appendQueryParameter(API_KEY, BuildConfig.THEMOVIEDB_API_KEY)
                    .appendQueryParameter(SORT_KEY, params[0])
                    .appendQueryParameter(PAGE_KEY, params[1])
                    .appendQueryParameter("vote_count.gte", params[2])
                    .build().toString();
        }else{
                throw new IllegalArgumentException(DownloadMovieDataAsync.class.getSimpleName() + " usage {sort, page, vote_thresh:opt}");
        }

        String rawJSON = new DownloadTextHelper().downloadHTTPS(query);

        try{
            JSONObject result = new JSONObject(rawJSON);
            JSONArray data = result.getJSONArray(RESULT_KEY);
            ContentValues[] movies = new ContentValues[data.length()];
            ContentValues[] favorites = new ContentValues[data.length()];
            for(int i = 0; i < data.length(); i++) {
                movies[i] = getMovieEntryValuesFromJSON(data.getJSONObject(i));
                favorites[i] = getFavoritesValues(data.getJSONObject(i));
            }
            mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, movies);
            mContext.getContentResolver().bulkInsert(MoviesContract.FavoritesEntry.CONTENT_URI, favorites);
        }catch (JSONException e){
            Log.e(LOG_TAG, "Error", e);
        }

        return null;
    }
}
