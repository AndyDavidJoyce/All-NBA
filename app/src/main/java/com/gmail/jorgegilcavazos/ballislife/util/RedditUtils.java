package com.gmail.jorgegilcavazos.ballislife.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadSummary;
import com.gmail.jorgegilcavazos.ballislife.features.posts.PostsAdapter;
import com.gmail.jorgegilcavazos.ballislife.features.shared.FullCardViewHolder;
import com.gmail.jorgegilcavazos.ballislife.features.shared.ThreadAdapter;

import java.util.List;

public final class RedditUtils {
    private final static String TAG = "RedditUtils";

    public final static String LIVE_GT_TYPE = "LIVE_GAME_THREAD";
    public final static String POST_GT_TYPE = "POST_GAME_THREAD";

    /**
     * Parses a given /r/NBA flair into a readable friendly string.
     * @param flair usually formatted as "Flair {cssClass='Celtics1', text='The Truth'}"
     * @return friendly string, e.g. "The Truth", or empty string if flair was null or not valid.
     */
    public static String parseNbaFlair(String flair) {
        final int EXPECTED_SECTIONS = 5;
        if (flair == null) {
            return "";
        }

        String[] sections = flair.split("'");
        if (sections.length == EXPECTED_SECTIONS) {
            return sections[sections.length - 2];
        }
        return "";
    }

    /**
     * Given a list of {@link GameThreadSummary}, a couple of teams and a type (LIVE or POST). Finds
     * and returns the id of the reddit thread for the corresponding game thread or
     * post game thread.
     */
    public static String findGameThreadId(List<GameThreadSummary> threadList,
                                          String type,
                                          String homeTeamAbbr,
                                          String awayTeamAbbr) {
        if (threadList == null) {
            return "";
        }

        String homeTeamFullName = null;
        String awayTeamFullName = null;

        for (TeamName teamName : TeamName.values()) {
            if (teamName.toString().equals(homeTeamAbbr)) {
                homeTeamFullName = teamName.getTeamName();
            }
            if (teamName.toString().equals(awayTeamAbbr)) {
                awayTeamFullName = teamName.getTeamName();
            }
        }

        if (homeTeamFullName == null || awayTeamFullName == null) {
            return "";
        }

        for (GameThreadSummary thread : threadList) {
            String capsTitle = thread.getTitle().toUpperCase();

            // Usually formatted as "GAME THREAD: Cleveland Cavaliers @ San Antonio Spurs".
            switch (type) {
                case LIVE_GT_TYPE:
                    if (capsTitle.contains("GAME THREAD") && !capsTitle.contains("POST")
                            && titleContainsTeam(capsTitle, homeTeamFullName)
                            && titleContainsTeam(capsTitle, awayTeamFullName)) {
                        return thread.getId();
                    }
                    break;
                case POST_GT_TYPE:
                    if ((capsTitle.contains("POST GAME THREAD")
                            || capsTitle.contains("POST-GAME THREAD"))
                            && titleContainsTeam(capsTitle, homeTeamFullName)
                            && titleContainsTeam(capsTitle, awayTeamFullName)) {
                        return thread.getId();
                    }
                    break;
            }
        }

        return "";
    }

    /**
     * Checks that the title contains at least the team name, e.g "Spurs".
     */
    public static boolean titleContainsTeam(String title, String fullTeamName) {
        String capsTitle = title.toUpperCase();
        String capsTeam = fullTeamName.toUpperCase(); // Ex. "SAN ANTONIO SPURS".
        String capsName = capsTeam.substring(capsTeam.lastIndexOf(" ") + 1); // Ex. "SPURS".
        return capsTitle.contains(capsName);
    }

    public static void setUpvotedColors(Context context, final FullCardViewHolder holder) {
        DrawableCompat.setTint(holder.btnUpvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentUpvoted));
        DrawableCompat.setTint(holder.btnDownvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        holder.tvPoints.setTextColor(ContextCompat.getColor(context, R.color.commentUpvoted));
    }

    public static void setDownvotedColors(Context context, final FullCardViewHolder holder) {
        DrawableCompat.setTint(holder.btnUpvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        DrawableCompat.setTint(holder.btnDownvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentDownvoted));
        holder.tvPoints.setTextColor(ContextCompat.getColor(context, R.color.commentDownvoted));
    }

    public static void setNoVoteColors(Context context, final FullCardViewHolder holder) {
        DrawableCompat.setTint(holder.btnUpvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        DrawableCompat.setTint(holder.btnDownvote.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
        holder.tvPoints.setTextColor(ContextCompat.getColor(context, R.color.commentNeutral));
    }

    public static void setSavedColors(Context context, final FullCardViewHolder holder) {
        DrawableCompat.setTint(holder.btnSave.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.amber));
    }

    public static void setUnsavedColors(Context context, final FullCardViewHolder holder) {
        DrawableCompat.setTint(holder.btnSave.getDrawable().mutate(),
                ContextCompat.getColor(context, R.color.commentNeutral));
    }
}
