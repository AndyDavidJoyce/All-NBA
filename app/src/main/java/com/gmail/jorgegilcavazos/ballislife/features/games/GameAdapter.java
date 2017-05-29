package com.gmail.jorgegilcavazos.ballislife.features.games;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.model.NbaGame;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil;
import com.gmail.jorgegilcavazos.ballislife.util.Utilities;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * RecyclerView Adapter used by the {@link GamesFragment} to display a list of games.
 */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolderWithBars> {
    private Context context;
    private List<NbaGame> nbaGameList;
    private GamesFragment.GameItemListener gameItemListener;

    public GameAdapter(List<NbaGame> nbaGames,
                       GamesFragment.GameItemListener itemListener) {
        nbaGameList = nbaGames;
        gameItemListener = itemListener;
    }

    @Override
    public GameViewHolderWithBars onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (Constants.NBA_MATERIAL_ENABLED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_game_logos,
                    parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_game_bars,
                    parent, false);
        }
        context = parent.getContext();
        return new GameViewHolderWithBars(view);
    }

    @Override
    public void onBindViewHolder(final GameViewHolderWithBars holder, int position) {
        NbaGame nbaGame = nbaGameList.get(position);

        int resKeyHome = context.getResources().getIdentifier(nbaGame.getHomeTeamAbbr()
                .toLowerCase(), "color", context.getPackageName());
        int resKeyAway = context.getResources().getIdentifier(nbaGame.getAwayTeamAbbr()
                .toLowerCase(), "color", context.getPackageName());

        /*
        ((GradientDrawable) holder.ivHomeLogo.getBackground()).setColor(
                ContextCompat.getColor(context, resKeyHome));
        ((GradientDrawable) holder.ivAwayLogo.getBackground()).setColor(
                ContextCompat.getColor(context, resKeyAway));
                */

        ViewCompat.setBackgroundTintList(holder.barAway, context.getResources().getColorStateList(resKeyAway));
        ViewCompat.setBackgroundTintList(holder.barHome, context.getResources().getColorStateList(resKeyHome));

        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3,
                context.getResources().getDisplayMetrics());
        float awayPct;
        float homePct;
        try {
            int awayScore = Integer.valueOf(nbaGame.getAwayTeamScore());
            int homeScore = Integer.valueOf(nbaGame.getHomeTeamScore());
            awayPct = (float) (awayScore) / (float) (awayScore + homeScore);
            homePct = (float) (homeScore) / (float) (awayScore + homeScore);

            if (awayScore == 0 && homeScore == 0) {
                awayPct = 0.5f;
                homePct = 0.5f;
            }
        } catch (NumberFormatException e) {
            awayPct = 0.5f;
            homePct = 0.5f;
        }
        holder.barHome.setLayoutParams(new TableLayout.LayoutParams(0, height, awayPct));
        holder.barAway.setLayoutParams(new TableLayout.LayoutParams(0, height, homePct));

        holder.tvHomeTeam.setText(nbaGame.getHomeTeamAbbr());
        holder.tvAwayTeam.setText(nbaGame.getAwayTeamAbbr());
        holder.tvHomeScore.setText(nbaGame.getHomeTeamScore());
        holder.tvAwayScore.setText(nbaGame.getAwayTeamScore());
        holder.tvClock.setText(nbaGame.getGameClock());
        holder.tvPeriod.setText(Utilities.getPeriodString(nbaGame.getPeriodValue(),
                nbaGame.getPeriodName()));

        holder.tvHomeScore.setVisibility(View.GONE);
        holder.tvAwayScore.setVisibility(View.GONE);
        holder.tvClock.setVisibility(View.GONE);
        holder.tvPeriod.setVisibility(View.GONE);
        holder.tvFinal.setVisibility(View.GONE);
        holder.tvTime.setVisibility(View.GONE);

        switch (nbaGame.getGameStatus()) {
            case NbaGame.PRE_GAME:
                holder.tvTime.setVisibility(View.VISIBLE);
                holder.tvTime.setText(DateFormatUtil.localizeGameTime(nbaGame.getPeriodStatus()));
                break;
            case NbaGame.IN_GAME:
                holder.tvHomeScore.setVisibility(View.VISIBLE);
                holder.tvAwayScore.setVisibility(View.VISIBLE);
                holder.tvClock.setVisibility(View.VISIBLE);
                holder.tvPeriod.setVisibility(View.VISIBLE);
                break;
            case NbaGame.POST_GAME:
                holder.tvHomeScore.setVisibility(View.VISIBLE);
                holder.tvAwayScore.setVisibility(View.VISIBLE);
                holder.tvFinal.setVisibility(View.VISIBLE);
                holder.tvFinal.setText("FINAL");
                break;
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameItemListener.onGameClick(nbaGameList.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return null != nbaGameList ? nbaGameList.size() : 0;
    }

    public void swap(List<NbaGame> data) {
        nbaGameList.clear();
        nbaGameList.addAll(data);
        notifyDataSetChanged();
    }

    public void updateScores(List<NbaGame> data) {
        for (NbaGame newGame : data) {
            String id = newGame.getId();
            String homeScore = newGame.getHomeTeamScore();
            String awayScore = newGame.getAwayTeamScore();
            String clock = newGame.getGameClock();
            String periodValue = newGame.getPeriodValue();
            String periodStatus = newGame.getPeriodStatus();
            String gameStatus = newGame.getGameStatus();

            for (NbaGame oldGame : nbaGameList) {
                if (oldGame.getId().equals(id)) {
                    oldGame.setHomeTeamScore(homeScore);
                    oldGame.setAwayTeamScore(awayScore);
                    oldGame.setGameClock(clock);
                    oldGame.setPeriodValue(periodValue);
                    oldGame.setPeriodStatus(periodStatus);
                    oldGame.setGameStatus(gameStatus);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layout_content) RelativeLayout container;
        @BindView(R.id.homelabel) TextView tvHomeTeam;
        @BindView(R.id.awaylabel) TextView tvAwayTeam;
        @BindView(R.id.homescore) TextView tvHomeScore;
        @BindView(R.id.awayscore) TextView tvAwayScore;
        @BindView(R.id.clock) TextView tvClock;
        @BindView(R.id.period) TextView tvPeriod;
        @BindView(R.id.text_time) TextView tvTime;
        @BindView(R.id.text_final) TextView tvFinal;
        @BindView(R.id.homeicon) ImageView ivHomeLogo;
        @BindView(R.id.awayicon) ImageView ivAwayLogo;

        public GameViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class GameViewHolderWithBars extends RecyclerView.ViewHolder {

        @BindView(R.id.layout_content) RelativeLayout container;
        @BindView(R.id.homelabel) TextView tvHomeTeam;
        @BindView(R.id.awaylabel) TextView tvAwayTeam;
        @BindView(R.id.homescore) TextView tvHomeScore;
        @BindView(R.id.awayscore) TextView tvAwayScore;
        @BindView(R.id.clock) TextView tvClock;
        @BindView(R.id.period) TextView tvPeriod;
        @BindView(R.id.text_time) TextView tvTime;
        @BindView(R.id.text_final) TextView tvFinal;
        @BindView(R.id.away_bar) View barAway;
        @BindView(R.id.home_bar) View barHome;

        public GameViewHolderWithBars(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
