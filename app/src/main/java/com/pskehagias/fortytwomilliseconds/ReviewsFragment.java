package com.pskehagias.fortytwomilliseconds;

import android.content.Intent;
import android.database.Cursor;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.hardware.display.DisplayManagerCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Text;

import com.pskehagias.fortytwomilliseconds.data.MoviesContract;
import com.pskehagias.fortytwomilliseconds.data.MoviesContract.MovieEntry;
import com.pskehagias.fortytwomilliseconds.data.MoviesContract.ReviewEntry;

/**
 * Created by Peter on 4/14/2016.
 */
public class ReviewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> , View.OnClickListener, View.OnTouchListener, TextView.OnEditorActionListener{
    public static final String LOG_TAG = ReviewsFragment.class.getSimpleName();

    public static final String ARGS_TAG = "reviewargs";
    public static final int LOADER_REVIEWS = 0x1111;

    public static final String[] PROJECTION_REVIEWS = {
            MovieEntry.TABLE_NAME + '.' + MovieEntry._ID,
            MovieEntry.COL_ORIGINAL_TITLE,
            ReviewEntry.COL_AUTHOR,
            ReviewEntry.COL_REVIEW_ID,
            ReviewEntry.COL_REVIEW_TEXT,
            ReviewEntry.COL_WEBLINK
    };

    public static final int COL_MOVIEDB_ID = 0;
    public static final int COL_TITLE = 1;
    public static final int COL_AUTHOR = 2;
    public static final int COL_REVIEW_ID = 3;
    public static final int COL_REVIEW_TEXT = 4;
    public static final int COL_WEBLINK = 5;

    private int mIndex;
    private Cursor mData;

    private Button mGotoFirst;
    private Button mGotoLast;
    private Button mOpenBrowser;
    private TextView mReviewerName;
    private TextView mReviewContent;
    private TextView mReviewsTotal;
    private EditText mCurrentIndex;

    private ScrollView mReviewBodyScroller;

    public ReviewsFragment() {
        // Required empty public constructor
        mIndex = -1;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        getLoaderManager().initLoader(LOADER_REVIEWS, args, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_reviews, container, false);

        mGotoFirst = (Button)result.findViewById(R.id.read_reviews_button_goto_first);
        mGotoLast = (Button)result.findViewById(R.id.read_reviews_button_goto_last);
        mOpenBrowser = (Button)result.findViewById(R.id.read_reviews_button_open_browser);
        mReviewerName = (TextView)result.findViewById(R.id.read_reviews_reviewer_name);
        mReviewContent = (TextView)result.findViewById(R.id.read_reviews_review_content);
        mReviewsTotal = (TextView)result.findViewById(R.id.read_reviews_total);
        mCurrentIndex = (EditText)result.findViewById(R.id.read_reviews_editable_index);

        mReviewBodyScroller = (ScrollView)result.findViewById(R.id.read_reviews_scroller);

        mReviewBodyScroller.setOnTouchListener(this);

        mGotoFirst.setOnClickListener(this);
        mGotoLast.setOnClickListener(this);
        mOpenBrowser.setOnClickListener(this);
        mCurrentIndex.setOnClickListener(this);
        mCurrentIndex.setOnEditorActionListener(this);

        return result;
    }

    private void openReviewIndex(int index){
        if(index == mIndex)
            return;

        if(mData.moveToPosition(index)) {
            mReviewerName.setText(String.format(getString(R.string.format_review_by), mData.getString(COL_AUTHOR)));
            mReviewsTotal.setText("" + mData.getCount());
            mReviewContent.setText(mData.getString(COL_REVIEW_TEXT));
            mCurrentIndex.setText("" + (index + 1));

            mReviewBodyScroller.scrollTo(0,0);
            mIndex = index;
        }
    }

    private boolean validateRequestedPage(int page){
        return (page > 0) && page <= mData.getCount();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(actionId == EditorInfo.IME_ACTION_DONE){
            EditText input = (EditText)v;
            int requestedPage = Integer.parseInt(input.getText().toString()); //data is 0 indexed
            if(validateRequestedPage(requestedPage)){
                openReviewIndex(--requestedPage);
                v.setCursorVisible(false);
                return false;
            }else{
                input.setText(""+(mIndex+1));
            }
        }
        return true;
    }

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
                        if((mIndex+1) < mData.getCount()) {
                            openReviewIndex(mIndex + 1);
                        }
                    }else{
                        if(mIndex > 0) {
                            openReviewIndex(mIndex - 1);
                        }
                    }
                }
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.read_reviews_button_goto_first:
                openReviewIndex(0);
                break;
            case R.id.read_reviews_button_goto_last:
                openReviewIndex(mData.getCount() - 1);
                break;
            case R.id.read_reviews_button_open_browser:
                Intent sendToWeb = new Intent(Intent.ACTION_VIEW, Uri.parse(mData.getString(COL_WEBLINK)));
                if(sendToWeb.resolveActivity(getActivity().getPackageManager()) != null){
                    startActivity(sendToWeb);
                }
                break;
            case R.id.read_reviews_editable_index:
                ((EditText)v).setCursorVisible(true);
                break;
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Loader result = null;

        switch(id) {
            case LOADER_REVIEWS:
                if (args != null) {
                    Uri reviewUri = args.getParcelable(ARGS_TAG);
                    result = new CursorLoader(getActivity(), reviewUri, PROJECTION_REVIEWS, null, null, ReviewEntry.COL_REVIEW_ID + " ASC");
                }
                break;
            default:
                throw new UnsupportedOperationException("Error: the given Loader id isn't handled by " + LOG_TAG);
        }
        return result;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null && data.moveToFirst()){
            mData = data;
            openReviewIndex(0);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
