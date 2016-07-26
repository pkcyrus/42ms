package com.pskehagias.fortytwomilliseconds;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by pkcyr on 1/22/2016.
 */
public class DownloadTextHelper{
    public  final String LOG_TAG = DownloadTextHelper.class.getSimpleName();

    private BufferedReader reader;
    private HttpURLConnection httpURLConnection;
    private HttpsURLConnection httpsURLConnection;

    public DownloadTextHelper(){
        reader = null;
        httpURLConnection = null;
        httpsURLConnection = null;
    }

    public String downloadHTTP(String query){
        URL url = null;
        try{
            url = new URL(query);
        }catch(IOException e){
            Log.e(LOG_TAG, "Error", e);
            return "";
        }
        return downloadHTTP(url);
    }

    public String downloadHTTPS(String query){
        URL url = null;
        try{
            url = new URL(query);
        }catch(IOException e){
            Log.e(LOG_TAG, "Error", e);
            return "";
        }
        return downloadHTTPS(url);
    }

    public String downloadHTTP(URL url){
        String result = null;
        try{
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            InputStream inputStream = httpURLConnection.getInputStream();
            if(inputStream == null){
                Log.e(LOG_TAG, "Error reading file, no InputStream returned");
                return "";
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            result = readCompleteStream();
        }catch(IOException e){
            Log.e(LOG_TAG, "Error", e);
            result = "";
        }finally{
            closeConnections();
        }
        return result;
    }

    public String downloadHTTPS(URL url){
        String result = null;
        try{
            httpsURLConnection = (HttpsURLConnection)url.openConnection();
            httpsURLConnection.setRequestMethod("GET");
            httpsURLConnection.connect();

            InputStream inputStream = httpsURLConnection.getInputStream();
            if(inputStream == null){
                Log.e(LOG_TAG, "Error reading file, no InputStream returned");
                return "";
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            result = readCompleteStream();
        }catch(IOException e){
            Log.e(LOG_TAG, "Error", e);
            result = "";
        }finally{
            closeConnections();
        }
        return result;
    }

    private void closeConnections(){
        if(httpURLConnection != null){
            httpURLConnection.disconnect();
            httpURLConnection = null;
        }
        if(httpsURLConnection != null){
            httpsURLConnection.disconnect();
            httpsURLConnection = null;
        }
        if(reader != null){
            try{
                reader.close();
                reader = null;
            }catch (IOException e){
                Log.e(LOG_TAG, "Error", e);
            }
        }
    }

    private String readCompleteStream() throws IOException{
        StringBuffer readBuffer = new StringBuffer(0x8000);
        String line;
        while((line = reader.readLine())!= null){
            readBuffer.append(line+'\n');
        }
        return readBuffer.toString();
    }
}