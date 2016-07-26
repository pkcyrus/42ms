package com.pskehagias.fortytwomilliseconds.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by pkcyr on 4/6/2016.
 */
public class TestUtilities extends AndroidTestCase {
    // Generate a ContentValues for a movies table row based on themoviedb data
    public static ContentValues generateMovieEntryValues(){
        ContentValues result = new ContentValues();
        result.put(MoviesContract.MovieEntry._ID, 135397);
        result.put(MoviesContract.MovieEntry.COL_ORIGINAL_TITLE, "Jurassic World");
        result.put(MoviesContract.MovieEntry.COL_POSTER_PATH, "/uXZYawqUsChGSj54wcuBtEdUJbh.jpg");
        result.put(MoviesContract.MovieEntry.COL_POPULARITY, "88.551849");
        result.put(MoviesContract.MovieEntry.COL_RATING, 7.1);
        result.put(MoviesContract.MovieEntry.COL_RATING_COUNT, 435);
        result.put(MoviesContract.MovieEntry.COL_SYNOPSIS, "Twenty-two years after the events of Jurassic Park, Isla Nublar now features a fully functioning dinosaur theme park, Jurassic World, as originally envisioned by John Hammond.");
        result.put(MoviesContract.MovieEntry.COL_BACKDROP_PATH, "/dkMD5qlogeRMiEixC4YNPUvax2T.jpg");
        result.put(MoviesContract.MovieEntry.COL_RELEASE_DATE, "2015-06-12");
        result.put(MoviesContract.MovieEntry.COL_TITLE, "Jurassic World");
        result.put(MoviesContract.MovieEntry.COL_VIDEO, 0);
        result.put(MoviesContract.MovieEntry.COL_ADULT, 0);
        result.put(MoviesContract.MovieEntry.COL_ORIGINAL_LANGUAGE, "en");

        result.put(MoviesContract.MovieEntry.COL_USER_RATING, 0);
        return result;
    }

    public static ContentValues generateVideoEntryValues(){
        ContentValues result = new ContentValues();
        result.put(MoviesContract.VideosEntry.COL_MOVIEDB_ID, 135397);
        result.put(MoviesContract.VideosEntry.COL_ISO_639_1, "en");
        result.put(MoviesContract.VideosEntry.COL_ISO_3166_1, "US");
        result.put(MoviesContract.VideosEntry.COL_NAME, "Teaser");
        result.put(MoviesContract.VideosEntry.COL_SITE, "YouTube");
        result.put(MoviesContract.VideosEntry.COL_SIZE, 1080);
        result.put(MoviesContract.VideosEntry.COL_TYPE, "Teaser");
        result.put(MoviesContract.VideosEntry.COL_VIDEO_ID, "54749bea9251414f41001b58");
        result.put(MoviesContract.VideosEntry.COL_KEY, "bvu-zlR5A8Q");
        return result;
    }

    public static ContentValues generateReviewEntryValues(){
        ContentValues result = new ContentValues();
        result.put(MoviesContract.ReviewEntry.COL_MOVIEDB_ID, 135397);
        result.put(MoviesContract.ReviewEntry.COL_AUTHOR, "jonlikesmoviesthatdontsuck");
        result.put(MoviesContract.ReviewEntry.COL_REVIEW_ID, "55910381c3a36807f900065d");
        result.put(MoviesContract.ReviewEntry.COL_REVIEW_TEXT, "I was a huge fan of the original 3 movies, they were out when I was younger, and I grew up loving dinosaurs because of them. This movie was awesome, and I think it can stand as a testimonial piece towards the capabilities that Christopher Pratt has. He nailed it. The graphics were awesome, the supporting cast did great and the t rex saved the child in me. 10/5 stars, four thumbs up, and I hope that star wars episode VII doesn't disappoint,");
        result.put(MoviesContract.ReviewEntry.COL_WEBLINK, "https://www.themoviedb.org/review/55910381c3a36807f900065d");
        return result;
    }

    public static void validateCurrentRecord(Cursor valueCursor, ContentValues expectedValues){
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for(Map.Entry<String, Object> entry : valueSet){
            String name = entry.getKey();
            int index = valueCursor.getColumnIndex(name);
            assertTrue("Error: couldn't find column " + name, index != -1);

            String expectedValue = entry.getValue().toString();
            assertEquals("Error: the value in column " + name + " didn't match the expected value"
                    , expectedValue, valueCursor.getString(index));
        }
    }

    public static Uri assertProviderContentValuesInsertion(Context context, Uri uri, SQLiteDatabase db, String tableName, ContentValues values){
        Uri insertResult = context.getContentResolver().insert(uri, values);
        long _id = ContentUris.parseId(insertResult);

        assertTrue("Error: the row ID of the returned Uri is invalid: " + _id, _id > 0);

        Cursor cursor = db.query(tableName, null, tableName + "._id = ?", new String[]{((Long)_id).toString()},
                                 null, null, null);
        assertTrue("Error: no record returned in query of table " + tableName, cursor.moveToFirst());

        validateCurrentRecord(cursor, values);

        cursor.close();
        return insertResult;
    }

    public static long assertContentValuesInsertion(SQLiteDatabase db, String tableName, ContentValues values){
        long result = db.insert(tableName, null, values);
        assertTrue("Error: couldn't insert test row for " + tableName + " table", result != -1);

        Cursor cursor = db.query(tableName, null, null, null, null, null, null);
        assertTrue("Error: no records returned in query of table " + tableName, cursor.moveToFirst());

        validateCurrentRecord(cursor, values);

        assertFalse("Error: more than one record returned by query", cursor.moveToNext());

        cursor.close();
        return result;
    }

    // Assert that the columns in db->tableName match the HashSet of names
    public static void assertColumnNames(String tableName, HashSet<String> names, SQLiteDatabase db){
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        assertTrue("Error: Can't select table info for " + tableName, cursor.moveToFirst());

        int nameIndex = cursor.getColumnIndex("name");
        do{
            names.remove(cursor.getString(nameIndex));
        }while(cursor.moveToNext());

        assertTrue("Error: Columns for table " + tableName + " don't match given set", names.isEmpty());
        cursor.close();
    }

    public static void assertProviderRowUpdate(Context context, Uri uri, SQLiteDatabase db, String tableName, ContentValues initialValues, ContentValues updatedValues){
        long insertID = TestUtilities.assertContentValuesInsertion(db, tableName, initialValues);
        int updateResult = context.getContentResolver().update(uri, updatedValues, tableName + "._ID = ?", new String[]{((Long) insertID).toString()});

        assertTrue("Error: the number of rows updated should = 1, returned: " + updateResult, updateResult == 1);

        Cursor cursor = db.query(tableName, null, null, null, null, null, null);
        assertTrue("Error: the table is empty", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord(cursor, updatedValues);
        assertFalse("Error: there should only be one row in the table after the update.", cursor.moveToNext());

        cursor.close();
    }

    public static void assertProviderRowDelete(Context context, Uri uri, SQLiteDatabase db, String tableName, ContentValues testValues){
        long insertId = TestUtilities.assertContentValuesInsertion(
                db, tableName, testValues);

        int deleteResult = context.getContentResolver().delete(uri, tableName + "._ID = ?", new String[]{((Long) insertId).toString()});
        assertTrue("Error: the number or rows deleted should = 1, returned: " + deleteResult, deleteResult == 1);

        Cursor cursor = db.query(tableName, null, null, null, null, null, null);
        assertFalse("Error: the table is not empty after deleting test row", cursor.moveToFirst());

        cursor.close();
    }

    public static void assertUriMatcherResults(UriMatcher matcher, Map<Uri, Integer> values){
        for(Map.Entry<Uri, Integer> e : values.entrySet()){
            assertEquals("Error: " + e.getKey().toString() + " could not be matched",
                    matcher.match(e.getKey()), (int)e.getValue());
        }
    }
}
