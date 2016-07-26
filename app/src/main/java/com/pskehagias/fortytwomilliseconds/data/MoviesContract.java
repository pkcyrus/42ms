package com.pskehagias.fortytwomilliseconds.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.widget.BaseAdapter;

/**
 * Created by pkcyr on 3/18/2016.
 */
public class MoviesContract {
    private final String LOG_TAG = MoviesContract.class.getSimpleName();

    public static final String CONTENT_AUTHORITY = "com.pskehagias.fortytwomilliseconds";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_REVIEWS = "reviews";
    public static final String PATH_VIDEOS = "videos";
    public static final String PATH_REVIEW_COUNT = "review_count";
    public static final String PATH_FAVORITES = "favorites";


    public static final class MovieEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";

        public static final String COL_ORIGINAL_TITLE = "original_title";
        public static final String COL_SYNOPSIS = "synopsis";
        public static final String COL_POPULARITY = "popularity";
        public static final String COL_RATING = "rating";
        public static final String COL_RATING_COUNT = "rating_count";
        public static final String COL_POSTER_PATH = "poster_path";
        public static final String COL_BACKDROP_PATH = "backdrop_path";
        public static final String COL_RELEASE_DATE = "release_date";
        public static final String COL_TITLE = "title";
        public static final String COL_VIDEO = "video";
        public static final String COL_ADULT = "adult";
        public static final String COL_ORIGINAL_LANGUAGE = "original_language";
        public static final String COL_USER_RATING = "user_rating";

        public static Uri buildMovieUri(long movie_id){
            return ContentUris.withAppendedId(CONTENT_URI, movie_id);
        }

        public static Uri buildMovieReviewsUri(long movie_id){
            return buildMovieUri(movie_id).buildUpon().appendPath(PATH_REVIEWS).build();
        }

        public static Uri buildMovieVideosUri(long movie_id){
            return buildMovieUri(movie_id).buildUpon().appendPath(PATH_VIDEOS).build();
        }

        public static Uri buildMovieReviewCountsUri(long movie_id){
            return buildMovieUri(movie_id).buildUpon().appendPath(PATH_REVIEW_COUNT).build();
        }

        public static Uri buildMovieReviewCountsFavorites(long movie_id){
            return buildMovieReviewCountsUri(movie_id).buildUpon().appendPath(PATH_FAVORITES).build();
        }

        public static Uri buildMovieFavoritesUri(){
            return CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();
        }
    }

    public static final class FavoritesEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;

        public static final String TABLE_NAME = "favorites";

        public static final String COL_IS_FAVORITE = "is_favorite";

        public static Uri buildFavoritesUri(long movie_id){
            return ContentUris.withAppendedId(CONTENT_URI, movie_id);
        }
    }

    public static final class ReviewCountEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW_COUNT).build();
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW_COUNT;
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW_COUNT;

        public static final String TABLE_NAME = "review_count";

        public static final String COL_COUNT = "count";

        public static Uri buildReviewCountUri(long movie_id){
            return ContentUris.withAppendedId(CONTENT_URI, movie_id);
        }
    }

    public static final class ReviewEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        public static final String TABLE_NAME = "reviews";

        public static final String COL_MOVIEDB_ID = "moviedb_id";
        public static final String COL_REVIEW_ID = "review_id";
        public static final String COL_AUTHOR = "author";
        public static final String COL_REVIEW_TEXT = "review_text";
        public static final String COL_WEBLINK = "weblink";

        public static Uri buildReviewUri(String review_id){
            return CONTENT_URI.buildUpon().appendPath(review_id).build();
        }
    }

    public static final class VideosEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEOS).build();
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEOS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEOS;

        public static final String TABLE_NAME = "videos";

        public static final String COL_MOVIEDB_ID = "moviedb_id";
        public static final String COL_VIDEO_ID = "video_id";
        public static final String COL_ISO_639_1 = "iso_639_1";
        public static final String COL_ISO_3166_1 = "iso_3166_1";
        public static final String COL_TYPE = "type";
        public static final String COL_NAME = "name";
        public static final String COL_SIZE = "size";
        public static final String COL_SITE = "site";
        public static final String COL_KEY = "key";

        public static Uri buildVideoUri(String video_id){
            return CONTENT_URI.buildUpon().appendPath(video_id).build();
        }
    }
}
