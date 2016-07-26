package com.pskehagias.fortytwomilliseconds.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


import com.pskehagias.fortytwomilliseconds.data.MoviesContract.MovieEntry;
import com.pskehagias.fortytwomilliseconds.data.MoviesContract.VideosEntry;
import com.pskehagias.fortytwomilliseconds.data.MoviesContract.ReviewEntry;

/**
 * Created by pkcyr on 3/18/2016.
 */
public class TestMoviesDb extends AndroidTestCase {
    private final String LOG_TAG = TestMoviesDb.class.getSimpleName();

    protected void setUp() throws Exception {
        super.setUp();
        mContext.deleteDatabase(MoviesDbHelper.DATABASE_NAME);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // Generate a HashSet<String> of the expected table names in the movies.db
    private HashSet<String> generateTableNamesSet(){
        HashSet<String> result = new HashSet<>();
        result.add(VideosEntry.TABLE_NAME);
        result.add(MovieEntry.TABLE_NAME);
        result.add(ReviewEntry.TABLE_NAME);
        result.add(MoviesContract.ReviewCountEntry.TABLE_NAME);
        return result;
    }

    // Generate a HashSet<String> of the expected column names in the movies.db movies table
    private HashSet<String> generateMovieEntryColumnSet(){
        HashSet<String> result = new HashSet<>();
        result.add(MovieEntry.COL_ORIGINAL_TITLE);
        result.add(MovieEntry.COL_POSTER_PATH);
        result.add(MovieEntry.COL_POPULARITY);
        result.add(MovieEntry.COL_RATING);
        result.add(MovieEntry.COL_RATING_COUNT);
        result.add(MovieEntry.COL_SYNOPSIS);
        result.add(MovieEntry.COL_USER_RATING);
        result.add(MovieEntry.COL_ADULT);
        result.add(MovieEntry.COL_VIDEO);
        result.add(MovieEntry.COL_BACKDROP_PATH);
        result.add(MovieEntry.COL_RELEASE_DATE);
        result.add(MovieEntry.COL_TITLE);
        result.add(MovieEntry.COL_ORIGINAL_LANGUAGE);
        result.add(MovieEntry._ID);
        return result;
    }

    // Generate a HashSet<String> of the expected column names in the movies.db reviews table
    private HashSet<String> generateReviewEntryColumnSet(){
        HashSet<String> result = new HashSet<>();
        result.add(ReviewEntry._ID);
        result.add(ReviewEntry.COL_AUTHOR);
        result.add(ReviewEntry.COL_MOVIEDB_ID);
        result.add(ReviewEntry.COL_REVIEW_ID);
        result.add(ReviewEntry.COL_REVIEW_TEXT);
        result.add(ReviewEntry.COL_WEBLINK);
        return result;
    }

    // Generate a HashSet<String> of the expected column names in the movies.db videos table
    private HashSet<String> generateVideoEntryColumnSet(){
        HashSet<String> result = new HashSet<>();
        result.add(VideosEntry._ID);
        result.add(VideosEntry.COL_ISO_639_1);
        result.add(VideosEntry.COL_ISO_3166_1);
        result.add(VideosEntry.COL_MOVIEDB_ID);
        result.add(VideosEntry.COL_NAME);
        result.add(VideosEntry.COL_SITE);
        result.add(VideosEntry.COL_SIZE);
        result.add(VideosEntry.COL_TYPE);
        result.add(VideosEntry.COL_VIDEO_ID);
        result.add(VideosEntry.COL_KEY);
        return result;
    }

    // Test to ensure all tables are created, and with all expected columns
    public void testCreateMoviesDb(){
        MoviesDbHelper dbHelper = new MoviesDbHelper(getContext());
        SQLiteDatabase moviesDb = dbHelper.getWritableDatabase();

        assertTrue(moviesDb.isOpen());

        Cursor cursor = moviesDb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: can't select table names", cursor.moveToFirst());

        HashSet<String> names = generateTableNamesSet();
        do{
            names.remove(cursor.getString(0));
        }while(cursor.moveToNext());

        assertTrue("Error: not all tables generated", names.isEmpty());
        cursor.close();

        TestUtilities.assertColumnNames(MovieEntry.TABLE_NAME, generateMovieEntryColumnSet(), moviesDb);
        TestUtilities.assertColumnNames(ReviewEntry.TABLE_NAME, generateReviewEntryColumnSet(), moviesDb);
        TestUtilities.assertColumnNames(VideosEntry.TABLE_NAME, generateVideoEntryColumnSet(), moviesDb);

        moviesDb.close();
    }

    private long assertContentValuesInsertion(String tableName, ContentValues values){
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase moviesDb = dbHelper.getWritableDatabase();

        long result = TestUtilities.assertContentValuesInsertion(moviesDb, tableName, values);

        moviesDb.close();
        return result;
    }

    public void testMoviesTable(){
        assertContentValuesInsertion(MovieEntry.TABLE_NAME, TestUtilities.generateMovieEntryValues());
    }

    public void testReviewsTable(){
        assertContentValuesInsertion(MovieEntry.TABLE_NAME, TestUtilities.generateMovieEntryValues());
        assertContentValuesInsertion(VideosEntry.TABLE_NAME, TestUtilities.generateVideoEntryValues());
    }

    public void testVideosTable(){
        assertContentValuesInsertion(MovieEntry.TABLE_NAME, TestUtilities.generateMovieEntryValues());
        assertContentValuesInsertion(ReviewEntry.TABLE_NAME, TestUtilities.generateReviewEntryValues());
    }
}
