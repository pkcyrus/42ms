package com.pskehagias.fortytwomilliseconds.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.test.AndroidTestCase;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.pskehagias.fortytwomilliseconds.data.MoviesContract.MovieEntry;
import com.pskehagias.fortytwomilliseconds.data.MoviesContract.ReviewEntry;
import com.pskehagias.fortytwomilliseconds.data.MoviesContract.VideosEntry;

import junit.framework.Test;

import java.util.Map;
import java.util.Set;

/**
 * Created by pkcyr on 3/18/2016.
 */
public class TestMoviesProvider extends AndroidTestCase {

    private SQLiteDatabase testDatabase;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        testDatabase = dbHelper.getWritableDatabase();

        deleteAllRows();
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        if(testDatabase.isOpen()) {
            testDatabase.close();
        }
        testDatabase = null;
    }

    private void deleteRowsAndAssert(SQLiteDatabase db, String table){
        db.delete(table, null, null);
        Cursor result = db.query(table, null, null, null, null, null, null);
        assertFalse("The rows of table " + table + " couldn't be deleted", result.moveToFirst());
        result.close();
    }

    public void deleteAllRows(){
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        deleteRowsAndAssert(db, VideosEntry.TABLE_NAME);
        deleteRowsAndAssert(db, ReviewEntry.TABLE_NAME);
        deleteRowsAndAssert(db, MovieEntry.TABLE_NAME);

        db.close();
    }

    public void testProviderRegistration(){
        PackageManager packageManager = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(), MoviesProvider.class.getName());
        try{
            ProviderInfo providerInfo = packageManager.getProviderInfo(componentName, 0);

            assertEquals("Error: MoviesProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MoviesContract.CONTENT_AUTHORITY,
                            providerInfo.authority, MoviesContract.CONTENT_AUTHORITY);

        }catch (PackageManager.NameNotFoundException e) {
            fail("Error: WeatherProvider not registered at " + mContext.getPackageName());
        }
    }

    public void testQueryMovies(){
        ContentValues values = TestUtilities.generateMovieEntryValues();
        TestUtilities.assertContentValuesInsertion(testDatabase, MovieEntry.TABLE_NAME, values);

        Cursor cursor = mContext.getContentResolver().query(MovieEntry.CONTENT_URI, null, null, null, null);
        assertTrue("Error: no records returned in query of table " + MovieEntry.TABLE_NAME, cursor.moveToFirst());

        TestUtilities.validateCurrentRecord(cursor, values);

        cursor.close();
    }

    public void testQueryMoviesWithID(){
        ContentValues values = TestUtilities.generateMovieEntryValues();
        long insertID = TestUtilities.assertContentValuesInsertion(testDatabase, MovieEntry.TABLE_NAME, values);

        Cursor cursor = mContext.getContentResolver().query(MovieEntry.buildMovieUri(insertID),
                null, null, null, null);

        assertTrue("Error: no records returned in query of table " + MovieEntry.TABLE_NAME, cursor.moveToFirst());

        TestUtilities.validateCurrentRecord(cursor, values);

        cursor.close();
    }

    public void testQueryMoviesWithIDReviews(){
        ContentValues movieValues = TestUtilities.generateMovieEntryValues();
        ContentValues reviewValues = TestUtilities.generateReviewEntryValues();
        long insertID = TestUtilities.assertContentValuesInsertion(testDatabase, MovieEntry.TABLE_NAME, movieValues);
        TestUtilities.assertContentValuesInsertion(testDatabase, ReviewEntry.TABLE_NAME, reviewValues);

        String[] projection = new String[]{};
        reviewValues.keySet().toArray(projection);

        Cursor cursor = mContext.getContentResolver().query(MovieEntry.buildMovieUri(insertID).buildUpon().appendPath(MoviesContract.PATH_REVIEWS).build(),
                projection, null, null, null);

        assertTrue("Error: no records returned in query", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord(cursor, reviewValues);

        cursor.close();
    }

    public void testQueryMoviesWithIDVideos(){
        ContentValues movieValues = TestUtilities.generateMovieEntryValues();
        ContentValues videosValues = TestUtilities.generateVideoEntryValues();
        long insertID = TestUtilities.assertContentValuesInsertion(testDatabase, MovieEntry.TABLE_NAME, movieValues);
        TestUtilities.assertContentValuesInsertion(testDatabase, VideosEntry.TABLE_NAME, videosValues);

        String[] projection = new String[]{};
        videosValues.keySet().toArray(projection);

        Cursor cursor = mContext.getContentResolver().query(MovieEntry.buildMovieUri(insertID).buildUpon().appendPath(MoviesContract.PATH_VIDEOS).build(),
                projection, null, null, null);

        assertTrue("Error: no records returned in query", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord(cursor, videosValues);

        cursor.close();
    }

    public void testQueryVideos(){
        ContentValues movieValues = TestUtilities.generateMovieEntryValues();
        ContentValues videosValues = TestUtilities.generateVideoEntryValues();
        long insertID = TestUtilities.assertContentValuesInsertion(testDatabase, MovieEntry.TABLE_NAME, movieValues);
        TestUtilities.assertContentValuesInsertion(testDatabase, VideosEntry.TABLE_NAME, videosValues);

        Cursor cursor = mContext.getContentResolver().query(VideosEntry.CONTENT_URI,
                null, null, null, null);

        assertTrue("Error: no records returned in query", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord(cursor, videosValues);

        cursor.close();
    }

    public void testQueryVideosWithID(){
        ContentValues movieValues = TestUtilities.generateMovieEntryValues();
        ContentValues videosValues = TestUtilities.generateVideoEntryValues();
        long insertID = TestUtilities.assertContentValuesInsertion(testDatabase, MovieEntry.TABLE_NAME, movieValues);
        TestUtilities.assertContentValuesInsertion(testDatabase, VideosEntry.TABLE_NAME, videosValues);

        Cursor cursor = mContext.getContentResolver().query(VideosEntry.CONTENT_URI.buildUpon().appendPath(videosValues.getAsString(VideosEntry.COL_VIDEO_ID)).build(),
                null, null, null, null);

        assertTrue("Error: no records returned in query", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord(cursor, videosValues);

        cursor.close();
    }

    public void testQueryReviews(){
        ContentValues movieValues = TestUtilities.generateMovieEntryValues();
        ContentValues reviewsValues = TestUtilities.generateReviewEntryValues();
        long insertID = TestUtilities.assertContentValuesInsertion(testDatabase, MovieEntry.TABLE_NAME, movieValues);
        TestUtilities.assertContentValuesInsertion(testDatabase, ReviewEntry.TABLE_NAME, reviewsValues);

        Cursor cursor = mContext.getContentResolver().query(ReviewEntry.CONTENT_URI,
                null, null, null, null);

        assertTrue("Error: no records returned in query", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord(cursor, reviewsValues);

        cursor.close();
    }

    public void testQueryReviewsWithID(){
        ContentValues movieValues = TestUtilities.generateMovieEntryValues();
        ContentValues reviewsValues = TestUtilities.generateReviewEntryValues();
        long insertID = TestUtilities.assertContentValuesInsertion(testDatabase, MovieEntry.TABLE_NAME, movieValues);
        TestUtilities.assertContentValuesInsertion(testDatabase, ReviewEntry.TABLE_NAME, reviewsValues);

        Cursor cursor = mContext.getContentResolver().query(ReviewEntry.CONTENT_URI.buildUpon().appendPath(reviewsValues.getAsString(ReviewEntry.COL_REVIEW_ID)).build(),
                null, null, null, null);

        assertTrue("Error: no records returned in query", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord(cursor, reviewsValues);

        cursor.close();
    }

    public void testInsertMovies(){
        ContentValues testValues = TestUtilities.generateMovieEntryValues();
        Uri insertResult = TestUtilities.assertProviderContentValuesInsertion(
                mContext, MovieEntry.CONTENT_URI, testDatabase, MovieEntry.TABLE_NAME, testValues);
    }

    public void testInsertVideos() {
        testInsertMovies();

        ContentValues testValues = TestUtilities.generateVideoEntryValues();
        Uri insertResult = TestUtilities.assertProviderContentValuesInsertion(
                mContext, VideosEntry.CONTENT_URI, testDatabase, VideosEntry.TABLE_NAME, testValues);
    }

    public void testInsertReviews() {
        testInsertMovies();

        ContentValues testValues = TestUtilities.generateReviewEntryValues();
        Uri insertResult = TestUtilities.assertProviderContentValuesInsertion(
                mContext, ReviewEntry.CONTENT_URI, testDatabase, ReviewEntry.TABLE_NAME, testValues);
    }


    public void testDeleteMovies(){
        TestUtilities.assertProviderRowDelete(mContext, MovieEntry.CONTENT_URI, testDatabase, MovieEntry.TABLE_NAME, TestUtilities.generateMovieEntryValues());
    }

    public void testDeleteVideos(){
        TestUtilities.assertContentValuesInsertion(testDatabase, MovieEntry.TABLE_NAME, TestUtilities.generateMovieEntryValues());

        TestUtilities.assertProviderRowDelete(mContext, VideosEntry.CONTENT_URI, testDatabase, VideosEntry.TABLE_NAME, TestUtilities.generateVideoEntryValues());
    }

    public void testDeleteReviews(){
        TestUtilities.assertContentValuesInsertion(testDatabase, MovieEntry.TABLE_NAME, TestUtilities.generateMovieEntryValues());

        TestUtilities.assertProviderRowDelete(mContext, ReviewEntry.CONTENT_URI, testDatabase, ReviewEntry.TABLE_NAME, TestUtilities.generateReviewEntryValues());
    }

    public void testUpdateMovies(){
        ContentValues initialValues = TestUtilities.generateMovieEntryValues();
        ContentValues updatedValues = TestUtilities.generateMovieEntryValues();
        updatedValues.put(MovieEntry.COL_POSTER_PATH, "AnewposterpatH");

        TestUtilities.assertProviderRowUpdate(mContext, MovieEntry.CONTENT_URI, testDatabase, MovieEntry.TABLE_NAME, initialValues, updatedValues);
    }

    public void testUpdateVideos(){
        TestUtilities.assertContentValuesInsertion(testDatabase, MovieEntry.TABLE_NAME, TestUtilities.generateMovieEntryValues());

        ContentValues initialValues = TestUtilities.generateVideoEntryValues();
        ContentValues updatedValues = TestUtilities.generateVideoEntryValues();
        updatedValues.put(VideosEntry.COL_TYPE, "AnewtypeofvideO");

        TestUtilities.assertProviderRowUpdate(mContext, VideosEntry.CONTENT_URI, testDatabase, VideosEntry.TABLE_NAME, initialValues, updatedValues);
    }

    public void testUpdateReviews(){
        TestUtilities.assertContentValuesInsertion(testDatabase, MovieEntry.TABLE_NAME, TestUtilities.generateMovieEntryValues());

        ContentValues initialValues = TestUtilities.generateReviewEntryValues();
        ContentValues updatedValues = TestUtilities.generateReviewEntryValues();
        updatedValues.put(ReviewEntry.COL_AUTHOR, "TheBestAuthorInTheUniverse");

        TestUtilities.assertProviderRowUpdate(mContext, ReviewEntry.CONTENT_URI, testDatabase, ReviewEntry.TABLE_NAME, initialValues, updatedValues);
    }
}
