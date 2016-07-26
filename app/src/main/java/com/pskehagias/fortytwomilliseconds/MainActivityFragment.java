package com.pskehagias.fortytwomilliseconds;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.pskehagias.fortytwomilliseconds.data.MoviesContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final int LOADER_MAINACTIVITY = 0x6000;
    private static final String BUNDLE_POSITION_Y = "bundlerposy";

    private MovieTileCursorAdapter mTileAdapter;
    private SelectionCallback mSelectionCallback;
    private GridView mGridVeiw;
    private int mPositionY;
    private boolean mDisplayFavorites;
    private String mSorting;
    private String mSortingFull;
    private String mVoteThreshold;

    public static final String[] PROJECTION_MOVIEDETAILS = {
            MoviesContract.MovieEntry.COL_ORIGINAL_TITLE,
            MoviesContract.MovieEntry.COL_SYNOPSIS,
            MoviesContract.MovieEntry.COL_POPULARITY,
            MoviesContract.MovieEntry.COL_RATING,
            MoviesContract.MovieEntry.COL_RATING_COUNT,
            MoviesContract.MovieEntry.COL_BACKDROP_PATH,
            MoviesContract.MovieEntry.COL_RELEASE_DATE,
            MoviesContract.FavoritesEntry.COL_IS_FAVORITE,
            MoviesContract.MovieEntry.TABLE_NAME+"."+MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COL_POSTER_PATH
    };

    public static final int COL_ORIGINAL_TITLE = 0;
    public static final int COL_SYNOPSIS       = 1;
    public static final int COL_POPULARITY     = 2;
    public static final int COL_RATING         = 3;
    public static final int COL_RATING_COUNT   = 4;
    public static final int COL_BACKDROP_PATH    = 5;
    public static final int COL_RELEASE_DATE   = 6;
    public static final int COL_USER_RATING    = 7;
    public static final int COL_ID             = 8;
    public static final int COL_POSTER_PATH = 9;



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> result = null;

        switch(id){
            case LOADER_MAINACTIVITY:
                String sortColumn;
                String tieBreaker;
                String selection;
                boolean byPopularity;
                if (byPopularity = (mSorting.compareTo("popularity.desc") == 0)) {
                    sortColumn = MoviesContract.MovieEntry.COL_POPULARITY;
                    tieBreaker = "";
                }else{
                    sortColumn = MoviesContract.MovieEntry.COL_RATING;
                    tieBreaker = " , " + MoviesContract.MovieEntry.COL_RATING_COUNT + " DESC";
                }

                if(mDisplayFavorites){
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    selection = MoviesContract.FavoritesEntry.COL_IS_FAVORITE + " > 0" + (byPopularity? "" : " AND " + MoviesContract.MovieEntry.COL_RATING_COUNT + " >= " + preferences.getString("vote_threshold", "1"));
                }else{
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    selection = (byPopularity ? null : MoviesContract.MovieEntry.COL_RATING_COUNT + " >= " + preferences.getString(getString(R.string.setting_vote_thresh_key), "1"));
                }

                result = new CursorLoader(getContext(), MoviesContract.MovieEntry.buildMovieFavoritesUri(),
                        MainActivityFragment.PROJECTION_MOVIEDETAILS, selection, null,
                        "CAST ( " + sortColumn + " AS REAL )" + " DESC" + tieBreaker);
                break;
            default:
                throw new UnsupportedOperationException("Error: the given Loader id isn't handled by " + LOG_TAG);
        }

        return result;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mTileAdapter.swapCursor(data);
        if((mPositionY < mGridVeiw.getFirstVisiblePosition()) || (mPositionY > mGridVeiw.getLastVisiblePosition()))
            mGridVeiw.smoothScrollToPosition(mPositionY);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTileAdapter.swapCursor(null);
    }

    public interface SelectionCallback{
        public void onMovieSelected(Uri movie);
    }

    public class PageLoader implements AbsListView.OnScrollListener{
        private int threshold;
        private String sorting;
        private SharedPreferences preferences;
        private int downloadCount;
        private boolean useVoteThreshold;



        public PageLoader(int threshold, String sorting){
            this.threshold = threshold;
            this.sorting = sorting;
            useVoteThreshold = sorting.compareTo("popularity.desc") != 0;

            preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

            downloadCount = preferences.getInt(sorting+(useVoteThreshold?mVoteThreshold:""), 0);
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            return;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if(((firstVisibleItem + visibleItemCount + threshold) > downloadCount) || (downloadCount < threshold)){
                if(useVoteThreshold) {
                    new DownloadMovieDataAsync(getContext()).execute(sorting, Integer.toString((int) Math.floor(downloadCount / 20.0) + 1), mVoteThreshold);
                }else{
                    new DownloadMovieDataAsync(getContext()).execute(sorting, Integer.toString((int) Math.floor(downloadCount / 20.0) + 1));
                }
                downloadCount += 20;
                preferences.edit().putInt(sorting, downloadCount).commit();
            }
            mPositionY = firstVisibleItem;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);
        if(cursor != null){
            Uri movie = MoviesContract.MovieEntry.buildMovieReviewCountsFavorites(cursor.getLong(MovieActivityFragment.COL_ID));
            mSelectionCallback.onMovieSelected(movie);
        }
        mPositionY = position;
    }

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectionCallback = (SelectionCallback)getActivity();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sorting = preferences.getString(getString(R.string.setting_sort_by_key), "popularity.desc");
        if( mDisplayFavorites = (sorting.substring(0, 3).compareTo("fav") == 0 )){
            mSorting = sorting.substring(4);
        }else {
            mSorting = sorting;
        }
        mSortingFull = sorting;
        mVoteThreshold = preferences.getString(getString(R.string.setting_vote_thresh_key),"1");
    }

    @Override
    public void onResume() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sorting = preferences.getString(getString(R.string.setting_sort_by_key), "popularity.desc");
        String vote_threshold = preferences.getString(getString(R.string.setting_vote_thresh_key), "1");
        if(mSortingFull.compareTo(sorting)!= 0){
            if( mDisplayFavorites = (sorting.substring(0, 3).compareTo("fav") == 0)){
                mSorting = sorting.substring(4);
            }else {
                mSorting = sorting;
            }
            mSortingFull = sorting;
            mGridVeiw.setOnScrollListener(new PageLoader(10, mSorting));
            mPositionY = 0;
            mVoteThreshold = vote_threshold;
            getLoaderManager().restartLoader(LOADER_MAINACTIVITY, null, this);
        }else if((mVoteThreshold.compareTo(vote_threshold) != 0) && (mSorting.compareTo("popularity.desc") != 0)){
            mGridVeiw.setOnScrollListener(new PageLoader(10, mSorting));
            mPositionY = 0;
            mVoteThreshold = vote_threshold;
            getLoaderManager().restartLoader(LOADER_MAINACTIVITY, null, this);
        }
        super.onResume();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_MAINACTIVITY, null, this);
    }

    public String retrieveBaseImagePath(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(getString(R.string.setting_base_url_key), "http://image.tmdb.org/t/p/")
                + preferences.getString(getString(R.string.setting_image_size_key), "w92");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mTileAdapter = new MovieTileCursorAdapter(retrieveBaseImagePath(getContext()), getContext(), null, 0);

        View result = inflater.inflate(R.layout.fragment_main, container, false);

        mGridVeiw = ((GridView)result.findViewById(R.id.movie_grid));
        mGridVeiw.setAdapter(mTileAdapter);

        if(!mDisplayFavorites)
            mGridVeiw.setOnScrollListener(new PageLoader(10, mSorting));

        mGridVeiw.setOnItemClickListener(this);
        mGridVeiw.getViewTreeObserver().addOnGlobalLayoutListener(new GridViewColumnSizer(mGridVeiw, mTileAdapter));

        if(savedInstanceState != null) {
            mPositionY = savedInstanceState.getInt(BUNDLE_POSITION_Y);
        }
        else {
            mPositionY = 0;
        }

        return result;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(BUNDLE_POSITION_Y, mGridVeiw.getLastVisiblePosition());
        super.onSaveInstanceState(outState);
    }

    public class GridViewColumnSizer implements ViewTreeObserver.OnGlobalLayoutListener{
        private GridView gridView;
        private MovieTileCursorAdapter mAdapter;

        public GridViewColumnSizer(GridView view, MovieTileCursorAdapter adapter){
            gridView = view;
            mAdapter = adapter;
        }

        @Override
        public void onGlobalLayout() {
            int image_width = calculateColumnWidth(gridView.getWidth(), 100);
            mAdapter.setImageDimensions(image_width, (int)(image_width * 1.5));
            mAdapter.setPosterBasePath(calculateBasePath(image_width));

            gridView.setColumnWidth(image_width);
            // TODO: 4/14/2016 proper handling of need for deprecated call
            gridView.setAdapter(mAdapter); //This restarts the GridView and Re-binds any views that would stay incorrect until recycling
            gridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        private String calculateBasePath(int image_width){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String base = preferences.getString(getContext().getString(R.string.setting_base_url_key), "http://image.tmdb.org/t/p/");
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            String width;

            if(image_width > 500) {
                width = "w780";
            }
            else if(image_width > 342) {
                width = "w500";
            }
            else if(image_width > 185) {
                width = "w342";
            }
            else if(image_width > 154) {
                width = "w185";
            }
            else if(image_width > 92) {
                width = "w154";
            }
            else {
                width = "w92";
            }

            preferences.edit().putString(getContext().getString(R.string.setting_image_size_key), width).commit();

            return base + width;
        }

        private float calculateWidthInDip(){
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            return (displayMetrics.widthPixels / displayMetrics.density);
        }

        //Get a column count that allows for a columnWidth as close to targetColWidth as possible
        private int calculateColumnCount(int viewWidthPixels, int targetColDip){
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            float viewDip = viewWidthPixels / displayMetrics.density;

            return Math.round(viewDip / targetColDip);
        }

        private int calculateColumnWidth(int viewWidthPixels, int targetColDip){
            return viewWidthPixels / calculateColumnCount(viewWidthPixels, targetColDip);
        }
    }
}
