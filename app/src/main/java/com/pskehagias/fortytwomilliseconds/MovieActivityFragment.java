package com.pskehagias.fortytwomilliseconds;

import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.media.Image;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.ShareActionProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pskehagias.fortytwomilliseconds.data.MoviesContract;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import com.pskehagias.fortytwomilliseconds.data.MoviesContract.MovieEntry;
import com.pskehagias.fortytwomilliseconds.data.MoviesContract.ReviewCountEntry;
import com.pskehagias.fortytwomilliseconds.data.MoviesContract.VideosEntry;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnTouchListener{
    private static final String LOG_TAG = MovieActivityFragment.class.getSimpleName();

    public static final String YOUTUBE_URL = "http://www.youtube.com/watch?v=";
    public static final String ARGS_TAG = "movieuritag";
    private static final int LOADER_MOVIEDETAILS = 0x1234;
    private static final int LOADER_VIDEOS = 0x4321;

    public static final String[] PROJECTION_MOVIEDETAILS = {
            MovieEntry.COL_ORIGINAL_TITLE,
            MovieEntry.COL_SYNOPSIS,
            MovieEntry.COL_POPULARITY,
            MovieEntry.COL_RATING,
            MovieEntry.COL_RATING_COUNT,
            MovieEntry.COL_BACKDROP_PATH,
            MovieEntry.COL_RELEASE_DATE,
            MoviesContract.FavoritesEntry.COL_IS_FAVORITE, //MovieEntry.COL_USER_RATING,
            MovieEntry.TABLE_NAME+'.'+MovieEntry._ID,
            MovieEntry.COL_POSTER_PATH,
            ReviewCountEntry.COL_COUNT
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
    public static final int COL_REVIEW_COUNT = 10;

    public static final String[] PROJECTION_VIDEOS = {
            MovieEntry.TABLE_NAME+"."+MovieEntry._ID,
            VideosEntry.COL_NAME,
            VideosEntry.COL_KEY,
            VideosEntry.COL_SITE,
            VideosEntry.COL_SIZE,
            VideosEntry.COL_TYPE,
            VideosEntry.COL_ISO_639_1,
            VideosEntry.COL_ISO_3166_1
    };

    public static final int COL_VID_ID = 0;
    public static final int COL_VID_NAME = 1;
    public static final int COL_VID_KEY = 2;
    public static final int COL_VID_SITE = 3;
    public static final int COL_VID_SIZE = 4;
    public static final int COL_VID_TYPE = 5;
    public static final int COL_ISO_639_1 = 6;
    public static final int COL_ISO_3166_1 = 7;

    private TextView mTitleView;
    private TextView mReleaseView;
    private TextView mRatingView;
    private TextView mSynopsisView;
    private ImageView mPosterView;
    private TextView mReviewCount;
    private Button mReadReviews;
    private Spinner mVideosSpinner;
    private Button mWatchVideo;
    private LinearLayout mVideoLinear;

    private boolean mPosterLayout;
    private VideoSpinnerCursorAdapter mCursorAdapter;
    private ActionCallbacks mActionCallbacks;

    private long mMovieDbId;
    private int mFavoriteStatus;
    private MenuItem mActionFavorite;
    private ShareActionProvider mActionShare;

    private float mDownPositionX;
    private float mDownPositionY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        int action_index = event.getActionIndex();

        switch(action){
            case MotionEvent.ACTION_DOWN:
                mDownPositionX = event.getX(action_index);
                mDownPositionY = event.getY(action_index);
                return false;
            case MotionEvent.ACTION_UP:
                float movementX = event.getX(action_index) - mDownPositionX;
                float movementY = event.getY(action_index) - mDownPositionY;

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                float distanceX = movementX / displayMetrics.density;
                float distanceY = movementY / displayMetrics.density;

                if((Math.abs(distanceX) > 120.0f) && (Math.abs(distanceY) < 120.0f)){ // Check for approx 1inch of swipe horizontal.
                    if(distanceX < 0.0f){
                        mPosterView.setVisibility(View.GONE);
                    }else{
                        mPosterView.setVisibility(View.VISIBLE);
                    }
                }
                return true;
        }
        return false;
    }

    public interface ActionCallbacks{
        void onReadReviews(Uri reviewsUri);
    }

    public MovieActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionCallbacks = (ActionCallbacks)getActivity();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();
        getLoaderManager().initLoader(LOADER_MOVIEDETAILS, args, this);
        getLoaderManager().initLoader(LOADER_VIDEOS, args, this);

        new DownloadVideosDataAsync(getContext()).execute(Long.parseLong(((Uri)args.getParcelable(ARGS_TAG)).getPathSegments().get(1).toString()));
        new DownloadReviewsDataAsync(getContext()).execute(Long.parseLong(((Uri)args.getParcelable(ARGS_TAG)).getPathSegments().get(1).toString()));
    }

    public void notifyLoadFinished(){
        getLoaderManager().restartLoader(LOADER_MOVIEDETAILS, getArguments(), this);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_movie, menu);
        mActionFavorite = menu.findItem(R.id.action_favorite);
        MenuItem tempShare = menu.findItem(R.id.action_share);
        mActionShare = (ShareActionProvider)MenuItemCompat.getActionProvider(tempShare);

        if(mVideosSpinner != null){
            linkSpinnerToShareAction();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_favorite){
            Uri update_uri = MoviesContract.FavoritesEntry.CONTENT_URI;
            ContentValues update_values = new ContentValues();
            if(mFavoriteStatus > 0) {
                update_values.put(MoviesContract.FavoritesEntry.COL_IS_FAVORITE, -1);
                mFavoriteStatus = -1;
                mActionFavorite.setIcon(R.drawable.ic_action_notfavorite);
            }else{
                update_values.put(MoviesContract.FavoritesEntry.COL_IS_FAVORITE, 1);
                mFavoriteStatus = 1;
                mActionFavorite.setIcon(R.drawable.ic_action_isfavorite);
            }

            getActivity().getContentResolver().update(update_uri, update_values, MoviesContract.FavoritesEntry._ID + " = ?", new String[]{((Long)mMovieDbId).toString()});
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_movie, container, false);

        mTitleView = (TextView)result.findViewById(R.id.title_detail);
        mReleaseView = (TextView)result.findViewById(R.id.release_detail);
        mRatingView = (TextView)result.findViewById(R.id.rating_detail);
        mSynopsisView = (TextView)result.findViewById(R.id.synopsis_detail);
        mReviewCount = (TextView)result.findViewById(R.id.review_count_detail);
        mReadReviews = (Button)result.findViewById(R.id.read_reviews_detail);

        mVideosSpinner = (Spinner)result.findViewById(R.id.movie_details_video_spinner);
        mWatchVideo = (Button)result.findViewById(R.id.movie_details_launch_video);
        mVideoLinear = (LinearLayout)result.findViewById(R.id.movie_details_video_linear);

        mPosterView = (ImageView)result.findViewById(R.id.backdrop_detail);
        if(mPosterLayout = (mPosterView == null)){
            mPosterView = (ImageView) result.findViewById(R.id.poster_detail);
            mPosterView.setOnTouchListener(this);
            result.findViewById(R.id.scroll_detail).setOnTouchListener(this);
        }

        return result;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Loader result = null;
        Uri data = args.getParcelable(ARGS_TAG);
        if(data != null) {
            switch (id) {
                case LOADER_MOVIEDETAILS:
                    result = new CursorLoader(getActivity(), data,
                            PROJECTION_MOVIEDETAILS, null, null, null);
                    break;
                case LOADER_VIDEOS:
                    Uri vidData = MovieEntry.buildMovieVideosUri(Integer.parseInt(data.getPathSegments().get(1)));
                    result = new CursorLoader(getActivity(), vidData,PROJECTION_VIDEOS, null, null, null);
                    break;
                default:
                    throw new UnsupportedOperationException("Error: the given Loader id isn't handled by " + LOG_TAG);
            }
        }
        return result;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null) {
            int loaderId = loader.getId();
            switch (loaderId) {
                case LOADER_MOVIEDETAILS:
                    if(data.moveToFirst()) {
                        final long moviedb_id = data.getLong(COL_ID);
                        mMovieDbId = moviedb_id;

                        getActivity().setTitle(data.getString(COL_ORIGINAL_TITLE));

                        mTitleView.setText(data.getString(COL_ORIGINAL_TITLE));
                        mReleaseView.setText(String.format(getString(R.string.format_release_date), data.getString(COL_RELEASE_DATE)));
                        mSynopsisView.setText(data.getString(COL_SYNOPSIS));
                        mRatingView.setText(String.format(getString(R.string.format_rating), data.getString(COL_RATING), data.getInt(COL_RATING_COUNT)));
                        int review_count = data.getInt(COL_REVIEW_COUNT);
                        mReviewCount.setText(String.format(getString(R.string.format_review_count), review_count));

                        if (review_count <= 0)
                            mReadReviews.setVisibility(View.INVISIBLE);
                        else
                            mReadReviews.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mActionCallbacks.onReadReviews(MovieEntry.buildMovieReviewsUri(moviedb_id));
                                }
                            });

                        String baseURL = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.setting_base_url_key), "") + "w780/";
                        String fullURL = baseURL + (mPosterLayout ? data.getString(COL_POSTER_PATH) : data.getString(COL_BACKDROP_PATH));

                        Picasso.with(getContext()).load(fullURL).into(mPosterView);

                        mFavoriteStatus = data.getInt(COL_USER_RATING);
                        if(mActionFavorite != null && (mFavoriteStatus > 0)) {
                            mActionFavorite.setIcon(R.drawable.ic_action_isfavorite);
                        }

                        break;
                    }
                case LOADER_VIDEOS:
                    mCursorAdapter = new VideoSpinnerCursorAdapter(getContext(), data, 0);
                    mVideosSpinner.setAdapter(mCursorAdapter);

                    mWatchVideo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Cursor cursor = (Cursor)mVideosSpinner.getSelectedItem();
                            Intent sendToWeb = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_URL + cursor.getString(COL_VID_KEY)));
                            if(sendToWeb.resolveActivity(getActivity().getPackageManager()) != null){
                                startActivity(sendToWeb);
                            }
                        }
                    });
                    if(mActionShare != null){
                        linkSpinnerToShareAction();
                    }
                    break;
            }
        }
    }

    private Intent getYoutubeShareIntent(){
        Cursor cursor = (Cursor)mVideosSpinner.getSelectedItem();
        Intent result = new Intent(Intent.ACTION_SEND);
        result.setType("text/*");
        result.putExtra(Intent.EXTRA_TEXT,Uri.parse(YOUTUBE_URL + cursor.getString(COL_VID_KEY)).toString());
        return result;
    }

    private void linkSpinnerToShareAction(){
            mVideosSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mActionShare.setShareIntent(getYoutubeShareIntent());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    mActionShare.setShareIntent(null);
                }
            });
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
