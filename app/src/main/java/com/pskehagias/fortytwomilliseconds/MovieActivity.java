package com.pskehagias.fortytwomilliseconds;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class MovieActivity extends AppCompatActivity implements MovieActivityFragment.ActionCallbacks {

    private static final String FRAGMENT_TAG = "mymoviefragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Uri movie = getIntent().getData();
        Bundle bundle = new Bundle();
        bundle.putParcelable(MovieActivityFragment.ARGS_TAG, movie);

        MovieActivityFragment movieActivityFragment = new MovieActivityFragment();
        movieActivityFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_movie_details, movieActivityFragment, FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onReadReviews(Uri reviewsUri) {
        Bundle reviewsBundle = new Bundle();
        reviewsBundle.putParcelable(ReviewsFragment.ARGS_TAG, reviewsUri);

        ReviewsFragment reviewsFragment = new ReviewsFragment();
        reviewsFragment.setArguments(reviewsBundle);

        getSupportFragmentManager().beginTransaction()
                .addToBackStack(FRAGMENT_TAG)
                .replace(R.id.fragment_movie_details, reviewsFragment, FRAGMENT_TAG)
                .commit();
    }
}
