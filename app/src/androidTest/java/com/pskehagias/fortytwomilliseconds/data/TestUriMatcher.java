package com.pskehagias.fortytwomilliseconds.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pkcyr on 3/18/2016.
 */
public class TestUriMatcher extends AndroidTestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private static Map<Uri, Integer> buildTestUris(){
        HashMap<Uri, Integer> result = new HashMap<>();
        result.put(MoviesContract.MovieEntry.CONTENT_URI, MoviesProvider.MOVIES);
        result.put(MoviesContract.MovieEntry.buildMovieUri(454L), MoviesProvider.MOVIES_WITH_ID);
        result.put(MoviesContract.MovieEntry.buildMovieReviewsUri(545L), MoviesProvider.MOVIES_WITH_ID_REVIEWS);
        result.put(MoviesContract.MovieEntry.buildMovieVideosUri(666L), MoviesProvider.MOVIES_WITH_ID_VIDEOS);
        result.put(MoviesContract.ReviewEntry.CONTENT_URI, MoviesProvider.REVIEWS);
        result.put(MoviesContract.ReviewEntry.buildReviewUri("abc123"), MoviesProvider.REVIEWS_WITH_ID);
        result.put(MoviesContract.VideosEntry.CONTENT_URI, MoviesProvider.VIDEOS);
        result.put(MoviesContract.VideosEntry.buildVideoUri("123CBA"), MoviesProvider.VIDEOS_WITH_ID);
        return result;
    }

    public void testMoviesProviderMatcher(){
        TestUtilities.assertUriMatcherResults(MoviesProvider.createUriMatcher(), buildTestUris());
    }

}
