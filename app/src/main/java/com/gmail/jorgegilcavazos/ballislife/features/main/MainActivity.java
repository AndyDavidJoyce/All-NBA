package com.gmail.jorgegilcavazos.ballislife.features.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.features.highlights.HighlightsFragment;
import com.gmail.jorgegilcavazos.ballislife.features.profile.ProfileActivity;
import com.gmail.jorgegilcavazos.ballislife.features.standings.StandingsFragment;
import com.gmail.jorgegilcavazos.ballislife.features.login.LoginActivity;
import com.gmail.jorgegilcavazos.ballislife.features.settings.SettingsActivity;
import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.util.ActivityUtils;
import com.gmail.jorgegilcavazos.ballislife.data.RedditAuthentication;
import com.gmail.jorgegilcavazos.ballislife.features.games.GamesFragment;
import com.gmail.jorgegilcavazos.ballislife.features.posts.PostsFragment;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.SchedulerProvider;
import com.google.firebase.messaging.FirebaseMessaging;

import net.dean.jraw.RedditClient;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;

import static com.gmail.jorgegilcavazos.ballislife.data.RedditAuthentication.REDDIT_AUTH_PREFS;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final String MY_PREFERENCES = "MyPrefs";
    public static final String FIRST_TIME = "firstTime";

    private static final String SELECTED_FRAGMENT_KEY = "selected_fragment";

    private static final int GAMES_FRAGMENT_ID = 1;
    private static final int STANDINGS_FRAGMENT_ID = 2;
    private static final int POSTS_FRAGMENT_ID = 3;
    private static final int HIGHLIGHTS_FRAGMENT_ID = 4;

    Toolbar toolbar;
    ActionBar actionBar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    int selectedFragment;
    private SharedPreferences myPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceListener;

    private CompositeDisposable disposables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setUpToolbar();
        setUpNavigationView();
        setUpDrawerContent();
        setUpPreferences();
        loadNavigationHeaderContent();
        checkGooglePlayServicesAvailable();

        SharedPreferences preferences = getSharedPreferences(REDDIT_AUTH_PREFS, MODE_PRIVATE);

        BaseSchedulerProvider schedulerProvider = SchedulerProvider.getInstance();

        disposables = new CompositeDisposable();
        disposables.add(RedditAuthentication.getInstance().authenticate(preferences)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        loadRedditUsername();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                })
        );

        // Set default to GamesFragment.
        selectedFragment = GAMES_FRAGMENT_ID;
        if (savedInstanceState != null) {
            selectedFragment = savedInstanceState.getInt(SELECTED_FRAGMENT_KEY);
        }

        switch (selectedFragment) {
            case GAMES_FRAGMENT_ID:
                setGamesFragment();
                break;
            case STANDINGS_FRAGMENT_ID:
                setStandingsFragment();
                break;
            case POSTS_FRAGMENT_ID:
                setPostsFragment();
                break;
            case HIGHLIGHTS_FRAGMENT_ID:
                setHighlightsFragment();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_FRAGMENT_KEY, selectedFragment);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkGooglePlayServicesAvailable();
        loadRedditUsername();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void setUpToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            actionBar = getSupportActionBar();
            if (actionBar != null) {
                // Show menu icon
                actionBar.setHomeAsUpIndicator(R.mipmap.ic_menu_white);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void setUpNavigationView() {
        if (toolbar != null) {
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            navigationView = (NavigationView) findViewById(R.id.navigation);
            if (navigationView != null) {
                NavigationMenuView navMenuView = (NavigationMenuView) navigationView.getChildAt(0);
                if (navMenuView != null) {
                    navMenuView.setVerticalScrollBarEnabled(false);
                }
            }
        }
    }

    private void setUpDrawerContent() {
        navigationView.setNavigationItemSelectedListener(new NavigationView
                .OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {
                    case R.id.navigation_item_1:
                        setGamesFragment();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.navigation_item_2:
                        setStandingsFragment();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.navigation_item_3:
                        setPostsFragment();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.navigation_item_4:
                        setHighlightsFragment();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.navigation_item_7:
                        // Start LoginActivity if no user is already logged in.
                        if (!RedditAuthentication.getInstance().isUserLoggedIn()) {
                            Intent loginIntent = new Intent(getApplicationContext(),
                                    LoginActivity.class);
                            startActivity(loginIntent);
                        } else {
                            Intent profileIntent = new Intent(getApplicationContext(),
                                    ProfileActivity.class);
                            startActivity(profileIntent);
                        }
                        return true;
                    case R.id.navigation_item_9:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        Intent settingsIntent = new Intent(getApplicationContext(),
                                SettingsActivity.class);
                        startActivity(settingsIntent);
                        return true;
                    default:
                        setGamesFragment();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                }
            }
        });
    }

    public void setGamesFragment() {
        setTitle("Games");
        getSupportActionBar().setSubtitle(null);

        GamesFragment gamesFragment = null;
        if (selectedFragment == GAMES_FRAGMENT_ID) {
            gamesFragment = (GamesFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment);
        }

        if (gamesFragment == null) {
            gamesFragment = GamesFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    gamesFragment, R.id.fragment);
        }

        selectedFragment = GAMES_FRAGMENT_ID;
    }

    public void setStandingsFragment() {
        setTitle("Standings");
        getSupportActionBar().setSubtitle(null);

        StandingsFragment standingsFragment = null;
        if (selectedFragment == STANDINGS_FRAGMENT_ID) {
            standingsFragment = (StandingsFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment);
        }

        if (standingsFragment == null) {
            standingsFragment = StandingsFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    standingsFragment, R.id.fragment);
        }

        selectedFragment = STANDINGS_FRAGMENT_ID;
    }

    public void setPostsFragment() {
        setTitle("r/NBA");
        getSupportActionBar().setSubtitle(null);

        PostsFragment postsFragment = null;
        if (selectedFragment == POSTS_FRAGMENT_ID) {
            postsFragment = (PostsFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment);
        }

        if (postsFragment == null) {
            postsFragment = PostsFragment.newInstance(PostsFragment.ViewType.FULL_CARD);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    postsFragment, R.id.fragment);
        }

        selectedFragment = POSTS_FRAGMENT_ID;
    }

    public void setHighlightsFragment() {
        setTitle("Highlights");
        getSupportActionBar().setSubtitle(null);

        HighlightsFragment highlightsFragment = null;
        if (selectedFragment == HIGHLIGHTS_FRAGMENT_ID) {
            highlightsFragment = (HighlightsFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fragment);
        }

        if (highlightsFragment == null) {
            highlightsFragment = HighlightsFragment.newInstance(HighlightsFragment.VIEW_CARD);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                    highlightsFragment, R.id.fragment);
        }

        selectedFragment = HIGHLIGHTS_FRAGMENT_ID;
    }

    /**
     * Sets the logo and username from shared preferences.
     */
    private void loadNavigationHeaderContent() {
        loadRedditUsername();
        loadTeamLogo();
    }

    private void loadRedditUsername() {
        if (navigationView == null) {
            return;
        }

        View headerView = navigationView.getHeaderView(0);
        TextView redditUsername = (TextView) headerView.findViewById(R.id.redditUsername);
        RedditClient redditClient = RedditAuthentication.getInstance().getRedditClient();
        if (redditClient.isAuthenticated() && redditClient.hasActiveUserContext()) {
            redditUsername.setText(redditClient.getAuthenticatedUser());
        } else {
            redditUsername.setText(R.string.not_logged);
        }
    }

    private void loadTeamLogo() {
        if (Constants.NBA_MATERIAL_ENABLED) {
            View headerView = navigationView.getHeaderView(0);
            ImageView favTeamLogo = (ImageView) headerView.findViewById(R.id.favTeamLogo);
            favTeamLogo.setImageResource(getFavTeamLogoResource());
        }
    }

    /**
     * Looks for a favorite team saved in shared preferences and returns the resource id for its
     * logo. If there is no favorite team, the app icon is returned.
     */
    private int getFavTeamLogoResource() {
        String favoriteTeam = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("teams_list", null);

        int resourceId;
        if (favoriteTeam != null && !favoriteTeam.equals("noteam")) {
            resourceId = getResources().getIdentifier(favoriteTeam, "drawable", getPackageName());
        } else {
            resourceId = R.mipmap.ic_launcher;
        }
        return resourceId;
    }

    private void setUpPreferences() {
        myPreferences = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        // Set default preferences if not set yet
        if (myPreferences.getBoolean(FIRST_TIME, true)) {
            SharedPreferences.Editor editor = myPreferences.edit();
            editor.putBoolean(FIRST_TIME, false);
            editor.apply();
            subscribeToDefaultTopics();
        }

        // Listen for changes to update team logo.
        mPreferenceListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                switch (key) {
                    case "teams_list":
                        loadTeamLogo();
                        break;
                }

            }
        };

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(mPreferenceListener);
    }

    private void subscribeToDefaultTopics() {
        // Subscribe to all CGA topics
        String[] topics = getResources().getStringArray(R.array.pref_cga_values);
        for (String topic : topics) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic);
        }
    }

    public void setToolbarSubtitle(String subtitle) {
        if (toolbar != null) {
            toolbar.setSubtitle(subtitle);
        }
    }

    public boolean checkGooglePlayServicesAvailable() {
        final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

        /*
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported");
                finish();
            }
            return false;
        }
        */
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        switch (selectedFragment) {
            case GAMES_FRAGMENT_ID:
                // Exit application.
                super.onBackPressed();
                break;
            default:
                // Return to games fragment.
                setGamesFragment();
                navigationView.getMenu().getItem(0).setChecked(true);
                break;
        }
    }
}
