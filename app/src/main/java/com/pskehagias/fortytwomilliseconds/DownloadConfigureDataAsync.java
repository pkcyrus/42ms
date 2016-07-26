package com.pskehagias.fortytwomilliseconds;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pkcyr on 1/22/2016.
 */
public class DownloadConfigureDataAsync extends AsyncTask<Void,Void,String> {
    public final String LOG_TAG = DownloadConfigureDataAsync.class.getSimpleName();

    public final String API_KEY = "api_key";
    public final String API_BASE = "https://api.themoviedb.org/3/configuration";
    public final String API_IMAGES_CONFIG = "images";
    public final String API_IMAGES_BASE_URL = "base_url";
    public final String API_IMAGES_SSL_BASE_URL = "secure_base_url";

    private SharedPreferences preferences;
    private String prefKey;

    public DownloadConfigureDataAsync(SharedPreferences preferences, String prefKey){
        this.preferences = preferences;
        this.prefKey = prefKey;
    }

    protected String doInBackground(Void... params) {
        String query = Uri.parse(API_BASE).buildUpon()
                .appendQueryParameter(API_KEY, BuildConfig.THEMOVIEDB_API_KEY).build().toString();

        return new DownloadTextHelper().downloadHTTPS(query);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try{
            JSONObject configData = new JSONObject(s);
            SharedPreferences.Editor ed = preferences.edit();
            ed.putString(prefKey, configData.getJSONObject(API_IMAGES_CONFIG).getString(API_IMAGES_BASE_URL));
            ed.apply();
        }catch(JSONException e){
            Log.e(LOG_TAG, "Error", e);
        }
    }
}
