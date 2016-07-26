package com.pskehagias.fortytwomilliseconds.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;


import com.pskehagias.fortytwomilliseconds.data.MoviesContract.MovieEntry;
import com.pskehagias.fortytwomilliseconds.data.MoviesContract.ReviewEntry;
import com.pskehagias.fortytwomilliseconds.data.MoviesContract.VideosEntry;
import com.pskehagias.fortytwomilliseconds.data.MoviesContract.ReviewCountEntry;

/**
 * Created by pkcyr on 3/18/2016.
 */
public class MoviesProvider extends ContentProvider {
    private final String LOG_TAG = MoviesProvider.class.getSimpleName();


    static final int MOVIES = 1000;
    static final int MOVIES_WITH_ID = 1001;
    static final int MOVIES_WITH_ID_REVIEWS = 1002;
    static final int MOVIES_WITH_ID_VIDEOS = 1003;
    static final int MOVIES_WITH_ID_REVIEW_COUNTS = 1004;
    static final int MOVIES_WITH_ID_REVIEW_COUNTS_FAVORITES = 1005;
    static final int MOVIES_WITH_FAVORITES = 1006;
    static final int REVIEWS = 2000;
    static final int REVIEWS_WITH_ID = 2001;
    static final int REVIEW_COUNTS = 2100;
    static final int REVIEW_COUNTS_WITH_ID = 2101;
    static final int VIDEOS = 3000;
    static final int VIDEOS_WITH_ID = 3001;
    static final int FAVORITES = 4000;
    static final int FAVORITES_WITH_ID = 4001;

    private static final UriMatcher sUriMatcher = createUriMatcher();
    private static final SQLiteQueryBuilder sMoviesReviewsQueryBuilder = createMoviesReviewsQueryBuilder();
    private static final SQLiteQueryBuilder sMoviesVideosQueryBuilder = createMoviesVideosQueryBuilder();
    private static final SQLiteQueryBuilder sMoviesReviewCountsQueryBuilder = createMoviesReviewCountsQueryBuilder();
    private static final SQLiteQueryBuilder sMoviesReviewCountsFavoritesQueryBuilder = createMoviesReviewCountsFavoritesQueryBuilder();
    private static final SQLiteQueryBuilder sMoviesFavoritesQueryBuilder = createMoviesFavoritesQueryBuilder();

    private MoviesDbHelper mMoviesDbHelper;

    static UriMatcher createUriMatcher(){
        UriMatcher result = new UriMatcher(UriMatcher.NO_MATCH);

        result.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES, MOVIES);
        result.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES+"/#", MOVIES_WITH_ID);
        result.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES+"/#/"+MoviesContract.PATH_REVIEWS, MOVIES_WITH_ID_REVIEWS);
        result.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES+"/#/"+MoviesContract.PATH_VIDEOS, MOVIES_WITH_ID_VIDEOS);
        result.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES+"/#/"+MoviesContract.PATH_REVIEW_COUNT, MOVIES_WITH_ID_REVIEW_COUNTS);
        result.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES+"/#/"+MoviesContract.PATH_REVIEW_COUNT+"/"+MoviesContract.PATH_FAVORITES, MOVIES_WITH_ID_REVIEW_COUNTS_FAVORITES);
        result.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES+"/"+MoviesContract.PATH_FAVORITES, MOVIES_WITH_FAVORITES);
        result.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_REVIEWS, REVIEWS);
        result.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_REVIEWS+"/*", REVIEWS_WITH_ID);
        result.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_REVIEW_COUNT, REVIEW_COUNTS);
        result.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_REVIEW_COUNT+"/#", REVIEW_COUNTS_WITH_ID);
        result.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_VIDEOS, VIDEOS);
        result.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_VIDEOS+"/*", VIDEOS_WITH_ID);
        result.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_FAVORITES, FAVORITES);
        result.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_FAVORITES+"/#", FAVORITES_WITH_ID);

        return result;
    }

    static SQLiteQueryBuilder createMoviesReviewsQueryBuilder(){
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        builder.setTables(  MovieEntry.TABLE_NAME + " INNER JOIN " + ReviewEntry.TABLE_NAME + " ON "
                            + MovieEntry.TABLE_NAME + '.' + MovieEntry._ID + " = "
                            + ReviewEntry.TABLE_NAME + '.' + ReviewEntry.COL_MOVIEDB_ID );

        return builder;
    }

    static SQLiteQueryBuilder createMoviesVideosQueryBuilder(){
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        builder.setTables(  MovieEntry.TABLE_NAME + " INNER JOIN " + VideosEntry.TABLE_NAME + " ON "
                            + MovieEntry.TABLE_NAME + '.' + MovieEntry._ID + " = "
                            + VideosEntry.TABLE_NAME + '.' + VideosEntry.COL_MOVIEDB_ID );
        return builder;
    }

    static SQLiteQueryBuilder createMoviesReviewCountsQueryBuilder(){
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        builder.setTables( MovieEntry.TABLE_NAME + " INNER JOIN " + ReviewCountEntry.TABLE_NAME + " ON "
                            + MovieEntry.TABLE_NAME + '.' + MovieEntry._ID + " = "
                            + ReviewCountEntry.TABLE_NAME + '.' + ReviewCountEntry._ID);
        return builder;
    }

    static SQLiteQueryBuilder createMoviesReviewCountsFavoritesQueryBuilder(){
        SQLiteQueryBuilder builder = createMoviesReviewCountsQueryBuilder();

        builder.setTables( builder.getTables() + " INNER JOIN " + MoviesContract.FavoritesEntry.TABLE_NAME + " ON "
                            + MovieEntry.TABLE_NAME + '.' + MovieEntry._ID + " = "
                            + MoviesContract.FavoritesEntry.TABLE_NAME + '.' + MoviesContract.FavoritesEntry._ID);

        return builder;
    }

    static SQLiteQueryBuilder createMoviesFavoritesQueryBuilder(){
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        builder.setTables( MovieEntry.TABLE_NAME + " INNER JOIN " + MoviesContract.FavoritesEntry.TABLE_NAME + " ON "
                            + MovieEntry.TABLE_NAME + "." + MovieEntry._ID + " = "
                            + MoviesContract.FavoritesEntry.TABLE_NAME + "." + MoviesContract.FavoritesEntry._ID);

        return builder;
    }

    public boolean onCreate() {
        mMoviesDbHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int match = sUriMatcher.match(uri);
        Cursor result = null;
        SQLiteDatabase db = mMoviesDbHelper.getReadableDatabase();

        switch(match){
            case MOVIES:
                result = db.query(MovieEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MOVIES_WITH_ID:
                result = db.query(MovieEntry.TABLE_NAME, projection, MovieEntry._ID + " = " + uri.getLastPathSegment(), null, null, null, sortOrder);
                break;
            case MOVIES_WITH_ID_REVIEWS:
                result = sMoviesReviewsQueryBuilder.query(db, projection, MovieEntry.TABLE_NAME + "." + MovieEntry._ID + " = " + uri.getPathSegments().get(1), null, null, null, sortOrder);
                break;
            case MOVIES_WITH_ID_VIDEOS:
                result = sMoviesVideosQueryBuilder.query(db, projection, MovieEntry.TABLE_NAME + "." + MovieEntry._ID + " = " + uri.getPathSegments().get(1), null, null, null, sortOrder);
                break;
            case MOVIES_WITH_ID_REVIEW_COUNTS:
                result = sMoviesReviewCountsQueryBuilder.query(db, projection, MovieEntry.TABLE_NAME + "." + MovieEntry._ID + " = " + uri.getPathSegments().get(1), null, null, null, sortOrder);
                break;
            case MOVIES_WITH_ID_REVIEW_COUNTS_FAVORITES:
                result = sMoviesReviewCountsFavoritesQueryBuilder.query(db, projection, MovieEntry.TABLE_NAME + "." + MovieEntry._ID + " = " + uri.getPathSegments().get(1), null, null, null, sortOrder);
                break;
            case MOVIES_WITH_FAVORITES:
                result = sMoviesFavoritesQueryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case REVIEWS:
                result = db.query(ReviewEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case REVIEWS_WITH_ID:
                result = db.query(ReviewEntry.TABLE_NAME, projection, ReviewEntry.COL_REVIEW_ID + " = \"" + uri.getLastPathSegment() +"\"", null, null, null, sortOrder);
                break;
            case REVIEW_COUNTS:
                result = db.query(ReviewCountEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case REVIEW_COUNTS_WITH_ID:
                result = db.query(ReviewCountEntry.TABLE_NAME, projection, ReviewCountEntry._ID + " = " + uri.getLastPathSegment(), null, null, null, sortOrder);
                break;
            case VIDEOS:
                result = db.query(VideosEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case VIDEOS_WITH_ID:
                result = db.query(VideosEntry.TABLE_NAME, projection, VideosEntry.COL_VIDEO_ID + " = \"" + uri.getLastPathSegment() +"\"", null, null, null, sortOrder);
                break;
            case FAVORITES:
                result = db.query(MoviesContract.FavoritesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FAVORITES_WITH_ID:
                result = db.query(MoviesContract.FavoritesEntry.TABLE_NAME, projection, MoviesContract.FavoritesEntry._ID + " = ? ", new String[]{uri.getPathSegments().get(1)}, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch(match){
            case MOVIES:
                return MovieEntry.CONTENT_DIR_TYPE;
            case MOVIES_WITH_ID:
                return MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIES_WITH_ID_REVIEWS:
                return MovieEntry.CONTENT_DIR_TYPE;
            case MOVIES_WITH_ID_REVIEW_COUNTS:
                return MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIES_WITH_ID_REVIEW_COUNTS_FAVORITES:
                return MoviesContract.FavoritesEntry.CONTENT_ITEM_TYPE;
            case MOVIES_WITH_FAVORITES:
                return MovieEntry.CONTENT_DIR_TYPE;
            case REVIEWS:
                return ReviewEntry.CONTENT_DIR_TYPE;
            case REVIEWS_WITH_ID:
                return ReviewEntry.CONTENT_ITEM_TYPE;
            case REVIEW_COUNTS:
                return ReviewCountEntry.CONTENT_DIR_TYPE;
            case REVIEW_COUNTS_WITH_ID:
                return ReviewCountEntry.CONTENT_ITEM_TYPE;
            case VIDEOS:
                return VideosEntry.CONTENT_DIR_TYPE;
            case VIDEOS_WITH_ID:
                return VideosEntry.CONTENT_ITEM_TYPE;
            case FAVORITES:
                return MoviesContract.FavoritesEntry.CONTENT_DIR_TYPE;
            case FAVORITES_WITH_ID:
                return MoviesContract.FavoritesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException(FAIL_UNKNOWN + uri);
        }
    }

    private static final String FAIL_UNKNOWN = "Unknown uri: ";
    private static final String FAIL_INSERT_MSG = "Failed to insert row into ";

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final int match = sUriMatcher.match(uri);
        final SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();
        int result = 0;
        long _id;

        ContentResolver resolver = getContext().getContentResolver();

        switch(match){
            case MOVIES:
                for(ContentValues value : values) {
                    _id = db.insert(MovieEntry.TABLE_NAME, null, value);
                    if (_id > 0) {
                        resolver.notifyChange(MovieEntry.buildMovieUri(_id), null);
                        resolver.notifyChange(MovieEntry.buildMovieReviewsUri(_id), null);
                        resolver.notifyChange(MovieEntry.buildMovieVideosUri(_id), null);
                        resolver.notifyChange(MovieEntry.buildMovieReviewCountsUri(_id), null);
                    } else
                        throw new android.database.SQLException(FAIL_INSERT_MSG + uri);
                }
                resolver.notifyChange(MovieEntry.buildMovieFavoritesUri(), null);
                result = values.length;
                break;
            case REVIEWS:
                for(ContentValues value : values) {
                    _id = db.insert(ReviewEntry.TABLE_NAME, null, value);
                    if (_id > 0) {
                        resolver.notifyChange(MovieEntry.buildMovieReviewsUri(_id), null);
                    } else
                        throw new android.database.SQLException(FAIL_INSERT_MSG + uri);
                }
                result = values.length;
                break;
            case VIDEOS:
                for(ContentValues value : values) {
                    _id = db.insert(VideosEntry.TABLE_NAME, null, value);
                    if (_id > 0) {
                        resolver.notifyChange(MovieEntry.buildMovieVideosUri(value.getAsLong(VideosEntry.COL_MOVIEDB_ID)), null);
                    } else
                        throw new android.database.SQLException(FAIL_INSERT_MSG + uri);
                }
                result = values.length;
                break;
            case REVIEW_COUNTS:
                for(ContentValues value : values) {
                    _id = db.insert(ReviewCountEntry.TABLE_NAME, null, value);
                    if (_id > 0) {
                        resolver.notifyChange(MovieEntry.buildMovieReviewCountsUri(_id), null);
                    } else
                        throw new android.database.SQLException(FAIL_INSERT_MSG + uri);
                }
                break;
            case FAVORITES:
                for(ContentValues value : values) {
                    _id = db.insert(MoviesContract.FavoritesEntry.TABLE_NAME, null, value);
                    if (_id > 0) {
                        resolver.notifyChange(MovieEntry.buildMovieReviewCountsFavorites(_id), null);

                        // Return value should only count successful inserts, even if failed inserts were simple IGNORE
                        result++;
                    }
                    // DIR types only need to be notified once
                    if(result > 0)
                        resolver.notifyChange(MovieEntry.buildMovieFavoritesUri(), null);
                    // Haven't found a way to determine if ON CONFLICT IGNORE is triggered or not.
                    // -1's are expected, exceptions shouldn't be thrown
                }
                break;
            default:
                throw new UnsupportedOperationException(FAIL_UNKNOWN + uri);
        }

        if(result > 0)
            resolver.notifyChange(uri, null);
        return result;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        final SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();
        Uri result = null;
        long _id;

        ContentResolver resolver = getContext().getContentResolver();

        switch(match){
            case MOVIES:
                _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                if(_id > 0) {
                    result = ContentUris.withAppendedId(MovieEntry.CONTENT_URI, _id);
                    resolver.notifyChange(MovieEntry.buildMovieUri(_id), null);
                    resolver.notifyChange(MovieEntry.buildMovieReviewsUri(_id), null);
                    resolver.notifyChange(MovieEntry.buildMovieVideosUri(_id), null);
                    resolver.notifyChange(MovieEntry.buildMovieReviewCountsUri(_id), null);
                    resolver.notifyChange(MovieEntry.buildMovieFavoritesUri(), null);
                }
                else
                    throw new android.database.SQLException(FAIL_INSERT_MSG + uri);
                break;
            case REVIEWS:
                _id = db.insert(ReviewEntry.TABLE_NAME, null, values);
                if(_id > 0) {
                    result = ContentUris.withAppendedId(ReviewEntry.CONTENT_URI, _id);
                    resolver.notifyChange(MovieEntry.buildMovieReviewsUri(_id), null);
                }
                else
                    throw new android.database.SQLException(FAIL_INSERT_MSG + uri);
                break;
            case VIDEOS:
                _id = db.insert(VideosEntry.TABLE_NAME, null, values);
                if(_id > 0) {
                    result = ContentUris.withAppendedId(VideosEntry.CONTENT_URI, _id);
                    resolver.notifyChange(MovieEntry.buildMovieVideosUri(values.getAsLong(VideosEntry.COL_MOVIEDB_ID)), null);
                }
                else
                    throw new android.database.SQLException(FAIL_INSERT_MSG + uri);
                break;
            case REVIEW_COUNTS:
                _id = db.insert(ReviewCountEntry.TABLE_NAME, null, values);
                if(_id > 0) {
                    result = ContentUris.withAppendedId(ReviewCountEntry.CONTENT_URI, _id);
                    resolver.notifyChange(MovieEntry.buildMovieReviewCountsUri(_id), null);
                }
                else
                    throw new android.database.SQLException(FAIL_INSERT_MSG + uri);
                break;
            case FAVORITES:
                _id = db.insert(MoviesContract.FavoritesEntry.TABLE_NAME, null, values);
                if(_id > 0){
                    result = ContentUris.withAppendedId(MoviesContract.FavoritesEntry.CONTENT_URI, _id);
                    resolver.notifyChange(MovieEntry.buildMovieReviewCountsFavorites(_id), null);
                    resolver.notifyChange(MovieEntry.buildMovieFavoritesUri(), null);
                }
                // Haven't found a way to determine if ON CONFLICT IGNORE is triggered or not.
                // -1's are expected, exceptions shouldn't be thrown

                break;
            case MOVIES_WITH_ID:
            case MOVIES_WITH_ID_REVIEWS:
            case MOVIES_WITH_ID_VIDEOS:
            case REVIEWS_WITH_ID:
            case VIDEOS_WITH_ID:
                throw new UnsupportedOperationException("Uri " + uri + " doesn't address a table.");
            default:
                throw new UnsupportedOperationException(FAIL_UNKNOWN + uri);
        }

        resolver.notifyChange(uri, null);
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        final SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();
        int result = 0;

        if(selection == null)
            selection = "1";
        switch(match){
            case MOVIES:
                result = db.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEWS:
                result = db.delete(ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case VIDEOS:
                result = db.delete(VideosEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW_COUNTS_WITH_ID:
                result = db.delete(ReviewCountEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FAVORITES:
                result = db.delete(MoviesContract.FavoritesEntry.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(MovieEntry.buildMovieFavoritesUri(), null);
                break;
            case REVIEWS_WITH_ID:
            case MOVIES_WITH_ID:
            case MOVIES_WITH_ID_REVIEWS:
            case MOVIES_WITH_ID_VIDEOS:
            case VIDEOS_WITH_ID:
                throw new UnsupportedOperationException("Uri " + uri + " doesn't address a table.");
            default:
                throw new UnsupportedOperationException(FAIL_UNKNOWN + uri);
        }

        if(result != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        final SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();
        int result = 0;

        switch(match){
            case MOVIES:
                result = db.update(MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case REVIEWS:
                result = db.update(ReviewEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case VIDEOS:
                result = db.update(VideosEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case REVIEW_COUNTS:
                result = db.update(ReviewCountEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case FAVORITES:
                result = db.update(MoviesContract.FavoritesEntry.TABLE_NAME, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(MovieEntry.buildMovieFavoritesUri(), null);
                break;
            case REVIEWS_WITH_ID:
            case MOVIES_WITH_ID:
            case MOVIES_WITH_ID_REVIEWS:
            case MOVIES_WITH_ID_VIDEOS:
            case VIDEOS_WITH_ID:
                throw new UnsupportedOperationException("Uri " + uri + " doesn't address a table.");
            default:
                throw new UnsupportedOperationException(FAIL_UNKNOWN + uri);
        }

        if(result != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }
}
