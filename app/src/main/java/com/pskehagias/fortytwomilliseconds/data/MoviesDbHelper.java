package com.pskehagias.fortytwomilliseconds.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pskehagias.fortytwomilliseconds.data.MoviesContract.MovieEntry;
import com.pskehagias.fortytwomilliseconds.data.MoviesContract.ReviewEntry;
import com.pskehagias.fortytwomilliseconds.data.MoviesContract.VideosEntry;
import com.pskehagias.fortytwomilliseconds.data.MoviesContract.ReviewCountEntry;

/**
 * Created by pkcyr on 3/18/2016.
 */
public class MoviesDbHelper extends SQLiteOpenHelper {
    private final String LOG_TAG = MoviesDbHelper.class.getSimpleName();
    public static final String DATABASE_NAME = "movies.db";
    public static final int DATABASE_VERSION = 8;

    public MoviesDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CreateMoviesTableQuery =
                "CREATE TABLE " + MovieEntry.TABLE_NAME + "( "
                + MovieEntry._ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE, " // movies._id == moviedb_id
                + MovieEntry.COL_ORIGINAL_TITLE + " TEXT NOT NULL, "
                + MovieEntry.COL_POSTER_PATH + " TEXT NOT NULL, "
                + MovieEntry.COL_POPULARITY + " TEXT NOT NULL, "
                + MovieEntry.COL_RATING + " REAL NOT NULL, "
                + MovieEntry.COL_RATING_COUNT + " INTEGER NOT NULL, "
                + MovieEntry.COL_SYNOPSIS + " TEXT NOT NULL, "
                + MovieEntry.COL_BACKDROP_PATH + " TEXT NOT NULL, "
                + MovieEntry.COL_RELEASE_DATE + " TEXT NOT NULL, "
                + MovieEntry.COL_TITLE + " TEXT NOT NULL, "
                + MovieEntry.COL_VIDEO + " INTEGER NOT NULL, "
                + MovieEntry.COL_ADULT + " INTEGER NOT NULL, "
                + MovieEntry.COL_ORIGINAL_LANGUAGE + " TEXT NOT NULL);";

        final String CreateReviewsTableQuery =
                "CREATE TABLE " + ReviewEntry.TABLE_NAME + " ( "
                + ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ReviewEntry.COL_AUTHOR + " TEXT NOT NULL, "
                + ReviewEntry.COL_MOVIEDB_ID + " INTEGER NOT NULL, "
                + ReviewEntry.COL_REVIEW_ID + " TEXT NOT NULL, "
                + ReviewEntry.COL_REVIEW_TEXT + " TEXT NOT NULL, "
                + ReviewEntry.COL_WEBLINK + " TEXT NOT NULL, "
                + "FOREIGN KEY ( " + ReviewEntry.COL_MOVIEDB_ID + " ) REFERENCES "+
                MovieEntry.TABLE_NAME + " ( " + MovieEntry._ID + " ) , "
                + "UNIQUE ( " + ReviewEntry.COL_REVIEW_ID + " ) ON CONFLICT REPLACE );";

        final String CreateVideosTableQuery =
                "CREATE TABLE " + VideosEntry.TABLE_NAME + " ( "
                + VideosEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + VideosEntry.COL_MOVIEDB_ID + " INTEGER NOT NULL, "
                + VideosEntry.COL_VIDEO_ID + " TEXT NOT NULL, "
                + VideosEntry.COL_ISO_639_1 + " TEXT NOT NULL, "
                + VideosEntry.COL_ISO_3166_1 + " TEXT NOT NULL, "
                + VideosEntry.COL_TYPE + " TEXT NOT NULL, "
                + VideosEntry.COL_NAME + " TEXT NOT NULL, "
                + VideosEntry.COL_SIZE + " INTEGER NOT NULL, "
                + VideosEntry.COL_SITE + " TEXT NOT NULL, "
                + VideosEntry.COL_KEY + " TEXT NOT NULL, "
                + "FOREIGN KEY ( " + VideosEntry.COL_MOVIEDB_ID + " ) REFERENCES "+
                MovieEntry.TABLE_NAME + " ( " + MovieEntry._ID + " ) , "
                + "UNIQUE ( " + VideosEntry.COL_VIDEO_ID + " ) ON CONFLICT REPLACE );";

        final String CreateReviewCountTableQuery =
                "CREATE TABLE " + ReviewCountEntry.TABLE_NAME + " ( "
                + ReviewCountEntry._ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE, "
                + ReviewCountEntry.COL_COUNT + " INTEGER NOT NULL, "
                + "FOREIGN KEY ( " + ReviewCountEntry._ID + " ) REFERENCES "+
                MovieEntry.TABLE_NAME + " ( " + MovieEntry._ID + " ));";

        final String CreateFavoritesTableQuery =
                "CREATE TABLE " + MoviesContract.FavoritesEntry.TABLE_NAME + " ( "
                + MoviesContract.FavoritesEntry._ID + " INTEGER PRIMARY KEY ON CONFLICT IGNORE, "
                + MoviesContract.FavoritesEntry.COL_IS_FAVORITE + " INTEGER NOT NULL DEFAULT -1, "
                + "FOREIGN KEY ( " + MoviesContract.FavoritesEntry._ID + " ) REFERENCES " +
                MovieEntry.TABLE_NAME+" ( " + MovieEntry._ID + " ));";


        db.execSQL(CreateMoviesTableQuery);
        db.execSQL(CreateReviewsTableQuery);
        db.execSQL(CreateVideosTableQuery);
        db.execSQL(CreateReviewCountTableQuery);
        db.execSQL(CreateFavoritesTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + VideosEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReviewCountEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.FavoritesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);

        onCreate(db);
    }
}
