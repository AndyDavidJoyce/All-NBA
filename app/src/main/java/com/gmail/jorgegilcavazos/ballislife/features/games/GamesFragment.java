package com.gmail.jorgegilcavazos.ballislife.features.games;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.API.NbaGamesService;
import com.gmail.jorgegilcavazos.ballislife.data.firebase.MyMessagingService;
import com.gmail.jorgegilcavazos.ballislife.features.gamethread.CommentsActivity;
import com.gmail.jorgegilcavazos.ballislife.features.model.NbaGame;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.SchedulerProvider;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Displays a list of {@link NbaGame}s for the selected date.
 */
public class GamesFragment extends Fragment implements GamesView,
        SwipeRefreshLayout.OnRefreshListener {
    public final static String TAG = "GamesFragment";

    public final static String GAME_THREAD_HOME = "GAME_THREAD_HOME";
    public final static String GAME_THREAD_AWAY = "GAME_THREAD_AWAY";
    public final static String GAME_ID = "GAME_ID";
    public final static String GAME_DATE = "GAME_DATE";

    @BindView(R.id.navigator_button_left) ImageButton btnPrevDay;
    @BindView(R.id.navigator_button_right) ImageButton btnNextDay;
    @BindView(R.id.navigator_text) TextView tvNavigatorDate;
    @BindView(R.id.no_games_text) TextView tvNoGames;
    @BindView(R.id.games_rv) RecyclerView rvGames;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;

    private View view;
    private RecyclerView.LayoutManager lmGames;
    private GameAdapter gameAdapter;
    private Snackbar snackbar;
    private Unbinder unbinder;

    private GamesPresenter presenter;

    public GamesFragment() {
        // Required empty public constructor.
    }

    public static GamesFragment newInstance() {
        return new GamesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(scoresUpdateReceiver,
                new IntentFilter(MyMessagingService.FILTER_SCORES_UPDATED));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_games, container, false);

        getActivity().setTitle(R.string.games_fragment_title);
        unbinder = ButterKnife.bind(this, view);

        swipeRefreshLayout.setOnRefreshListener(this);

        gameAdapter = new GameAdapter(new ArrayList<NbaGame>(0), gameItemListener);
        lmGames = new LinearLayoutManager(getActivity());
        rvGames.setLayoutManager(lmGames);
        rvGames.setAdapter(gameAdapter);

        btnPrevDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.addOrSubstractDay(-1);
            }
        });

        btnNextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.addOrSubstractDay(1);
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nba-app-ca681.firebaseio.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NbaGamesService nbaGamesService = retrofit.create(NbaGamesService.class);

        presenter = new GamesPresenter(nbaGamesService, SchedulerProvider.getInstance());
        presenter.attachView(this);
        presenter.loadGames();

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                presenter.loadGames();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.stop();
        presenter.detachView();
    }

    @Override
    public void onPause() {
        presenter.dismissSnackbar();
        super.onPause();
        getActivity().unregisterReceiver(scoresUpdateReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private GameItemListener gameItemListener = new GameItemListener() {
        @Override
        public void onGameClick(NbaGame clickedGame) {
            presenter.openGameDetails(clickedGame);
        }
    };

    @Override
    public void onRefresh() {
        presenter.loadGames();
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        swipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void setDateNavigatorText(String dateText) {
        tvNavigatorDate.setText(dateText);
    }

    @Override
    public void hideGames() {
        rvGames.setVisibility(View.GONE);
    }

    @Override
    public void showGames(List<NbaGame> games) {
        gameAdapter.swap(games);
        rvGames.setVisibility(View.VISIBLE);
    }

    @Override
    public void showGameDetails(NbaGame game, Calendar selectedDate) {
        Intent intent = new Intent(getActivity(), CommentsActivity.class);
        intent.putExtra(GAME_THREAD_HOME, game.getHomeTeamAbbr());
        intent.putExtra(GAME_THREAD_AWAY, game.getAwayTeamAbbr());
        intent.putExtra(GAME_ID, game.getId());
        intent.putExtra(GAME_DATE, selectedDate.getTimeInMillis());
        startActivity(intent);
    }

    @Override
    public void updateScores(List<NbaGame> games) {
        gameAdapter.updateScores(games);
        rvGames.setVisibility(View.VISIBLE);
    }

    @Override
    public void setNoGamesIndicator(boolean active) {
        if (active) {
            tvNoGames.setVisibility(View.VISIBLE);
        } else {
            tvNoGames.setVisibility(View.GONE);
        }
    }

    @Override
    public void showSnackbar(boolean canReload) {
        snackbar = Snackbar.make(getView(), R.string.failed_game_data, Snackbar.LENGTH_INDEFINITE);
        if (canReload) {
            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.loadGames();
                }
            });
        }
        snackbar.show();
    }

    @Override
    public void dismissSnackbar() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    public interface GameItemListener {
        void onGameClick(NbaGame clickedGame);
    }

    private BroadcastReceiver scoresUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String gameData = intent.getStringExtra(MyMessagingService.KEY_SCORES_UPDATED);
            presenter.updateGames(gameData);
        }
    };
}
