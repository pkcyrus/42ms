package com.pskehagias.fortytwomilliseconds;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.SelectionCallback, MovieActivityFragment.ActionCallbacks {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String MASTER_FRAGMENT_TAG = "masterfragment";
    public static final String DETAIL_FRAGMENT_TAG = "detailfragment";

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        new DownloadConfigureDataAsync(preferences,getString(R.string.setting_base_url_key)).execute();

        if(mTwoPane = (findViewById(R.id.fragment_movie_details) != null)){
            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_movie_details, new WelcomeFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_about:
                if(mTwoPane){
                    Fragment fragment = new WelcomeFragment();
                    getSupportFragmentManager().beginTransaction()
                            .addToBackStack(DETAIL_FRAGMENT_TAG)
                            .replace(R.id.fragment_movie_details, fragment, DETAIL_FRAGMENT_TAG)
                            .commit();
                }else{
                    intent = new Intent(this, AboutActivity.class);
                    startActivity(intent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMovieSelected(Uri movie) {
        if(mTwoPane){
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG);
            Bundle oldArgs = fragment.getArguments();
            if(oldArgs != null){
                Uri lastMovieSelected = oldArgs.getParcelable(MovieActivityFragment.ARGS_TAG);
                if((lastMovieSelected != null) && (lastMovieSelected.compareTo(movie) == 0) )
                    return;
            }

            Bundle movieBundle = new Bundle();
            movieBundle.putParcelable(MovieActivityFragment.ARGS_TAG, movie);

            MovieActivityFragment movieActivityFragment = new MovieActivityFragment();
            movieActivityFragment.setArguments(movieBundle);

            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(DETAIL_FRAGMENT_TAG)
                    .replace(R.id.fragment_movie_details, movieActivityFragment, DETAIL_FRAGMENT_TAG)
                    .commit();

        }else{
            Intent movieIntent = new Intent(this, MovieActivity.class);
            movieIntent.setData(movie);
            startActivity(movieIntent);
        }
    }

    @Override
    public void onReadReviews(Uri reviewsUri) {
        //Should already be in master->detail mode if MainActivity contains a MovieActivityFragment,
        //but it doesn't hurt to check.  We won't be starting any new activities though.
        if(mTwoPane){
            Bundle reviewsBundle = new Bundle();
            reviewsBundle.putParcelable(ReviewsFragment.ARGS_TAG, reviewsUri);

            ReviewsFragment reviewsFragment = new ReviewsFragment();
            reviewsFragment.setArguments(reviewsBundle);

            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(DETAIL_FRAGMENT_TAG)
                    .replace(R.id.fragment_movie_details, reviewsFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        }
    }
}
