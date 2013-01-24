package me.elvishew.puzzle;

import java.util.ArrayList;
import java.util.List;

import me.elvishew.puzzle.Puzzle.Achievements;
import me.elvishew.puzzle.Puzzle.Bests;
import me.elvishew.puzzle.Puzzle.Games;
import me.elvishew.puzzle.Puzzle.Historys;
import me.elvishew.puzzle.Puzzle.Ranks;
import me.elvishew.puzzle.Puzzle.Recents;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class GamesRecorder {
    private static final String TAG = GamesRecorder.class.getSimpleName();

    private static final int MAX_HISTORYS_COUNT = 10;

    private static final String GAMES_WHERE = Games.GAME_ID + "=?";

    private static final String[] SIMPLE_HISTORYS_PROJECTION = new String[] {
        Historys._ID,
        Historys.DATE,
        Historys.SCORE
    };

    private static final int SIMPLE_HISTORY_INDEX_OF_DATE = 1;
    private static final int SIMPLE_HISTORY_INDEX_OF_SCORE = 2;

    private static final String[] RANKS_PROJECTION = new String[] {
        Historys._ID,
        Ranks.RANK,
        Ranks.PLAYER,
        Historys.GAME_ID,
        Historys.DATE,
        Achievements.TIME,
        Achievements.STEPS,
        Achievements.SCORE
    };

    private static final int RANK_INDEX_OF_RANK = 1;
    private static final int RANK_INDEX_OF_PLAYER = 2;
    private static final int RANK_INDEX_OF_GAME_ID = 3;
    private static final int RANK_INDEX_OF_DATE = 4;
    private static final int RANK_INDEX_OF_TIME_COST = 5;
    private static final int RANK_INDEX_OF_STEPS_COST = 6;
    private static final int RANK_INDEX_OF_SCORE = 7;

    private static final String[] HISTORYS_PROJECTION = new String[] {
        Historys._ID,
        Historys.GAME_ID,
        Historys.DATE,
        Achievements.TIME,
        Achievements.STEPS,
        Achievements.SCORE
    };

    private static final int HISTORY_INDEX_OF_GAME_ID = 1;
    private static final int HISTORY_INDEX_OF_DATE = 2;
    private static final int HISTORY_INDEX_OF_TIME_COST = 3;
    private static final int HISTORY_INDEX_OF_STEPS_COST = 4;
    private static final int HISTORY_INDEX_OF_SCORE = 5;

    private static final String SIMPLE_HISTORYS_SELECTION = Historys.GAME_ID + "=?";

    public static void updateGameDataFromDatabase(Context context, GameData gameData) {
        if (gameData == null) {
            return;
        }

        Cursor cursor = context.getContentResolver().query(Games.CONTENT_URI,
                null, GAMES_WHERE,
                new String[] { String.valueOf(gameData.gameId()) }, null);
        try {
            long time = cursor.getLong(cursor.getColumnIndex(Games.TIME));
            int steps = cursor.getInt(cursor.getColumnIndex(Games.STEPS));
            int score = cursor.getInt(cursor.getColumnIndex(Games.SCORE));
            gameData.updateAchievement(time, steps, score);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public static void initGamesData(Context context, List<GameData> gamesData) {
        ContentResolver resolver = context.getContentResolver();

        // Delete all rows first.
        resolver.delete(Games.CONTENT_URI, null, null);

        // Write them to db.
        int count = gamesData.size();
        ContentValues[] values = new ContentValues[count];
        GameData gameData;
        for (int i = 0; i < count; i++) {
            gameData = gamesData.get(i);
            values[i] = gameData.createValues();
        }
        resolver.bulkInsert(Games.CONTENT_URI, values);
    }

    public static void updateGameData(Context context, long gameId, long time, int steps, int score) {
        ContentValues values = GameData.createAchievementValues(time, steps, score);
        context.getContentResolver().update(
                Games.CONTENT_URI,
                values,
                GAMES_WHERE,
                new String[] { String.valueOf(gameId) });
    }

    public static void deleteHistorys(Context context, Uri uri, long gameId) {
        context.getContentResolver().delete(uri, SIMPLE_HISTORYS_SELECTION, new String[] {String.valueOf(gameId)});
        if (uri.equals(Bests.CONTENT_URI)) {
         // Delete best achievement.
            updateGameData(context, gameId, 0, 0, 0);
        }
    }

    public static void deleteData(Context context, long gameId) {
        // Delete historys.
        ContentResolver resolver = context.getContentResolver();
        String[] selectionArgs = new String[] { String.valueOf(gameId) };
        resolver.delete(Recents.CONTENT_URI, SIMPLE_HISTORYS_SELECTION, selectionArgs);
        resolver.delete(Bests.CONTENT_URI, SIMPLE_HISTORYS_SELECTION, selectionArgs);
        resolver.delete(Ranks.CONTENT_URI, SIMPLE_HISTORYS_SELECTION, selectionArgs);

        // Delete best achievement.
        updateGameData(context, gameId, 0, 0, 0);
    }

    public static void deleteAllData(Context context) {
        // Delete all historys.
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(Recents.CONTENT_URI, null, null);
        resolver.delete(Bests.CONTENT_URI, null, null);
        resolver.delete(Ranks.CONTENT_URI, null, null);

        // Reset all games achievements.
        ContentValues values = new ContentValues();
        GameData.createAchievementValues(0, 0, 0);
        resolver.update(Games.CONTENT_URI, values, null, null);
    }

    public static void recordAchievement(Context context, HistoryData historyData) {
        // Update recent.
        updateRecentsLimitedly(context, historyData);

        // Update best.
        updateBestsLimitedly(context, historyData);
    }

    private static void updateRecentsLimitedly(Context context,
            HistoryData historyData) {
        ContentResolver resolver = context.getContentResolver();

        // Query all recents.
        Cursor cursor = resolver.query(Recents.CONTENT_URI,
                SIMPLE_HISTORYS_PROJECTION, SIMPLE_HISTORYS_SELECTION,
                new String[] { String.valueOf(historyData.gameId()) }, Historys.DATE);
        long idToUpdate = -1;
        boolean needInsert = false;

        try {
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.getCount() >= MAX_HISTORYS_COUNT) {
                    if (cursor.moveToFirst()
                            && historyData.date() > cursor
                                    .getLong(SIMPLE_HISTORY_INDEX_OF_DATE)) {
                        // Update it.
                        idToUpdate = cursor.getLong(0);
                    } else {
                        // Do nothing.
                    }
                } else {
                    // Insert
                    needInsert = true;
                }
            } else {
                needInsert = true;
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        // Update if needed.
        if (idToUpdate >= 0) {
            Log.i(TAG, "Update: " + idToUpdate);
            resolver.update(Recents.CONTENT_URI, historyData.createValues(),
                    Historys._ID + "=" + idToUpdate, null);
        }

        // Insert if needed.
        if (needInsert) {
            resolver.insert(Recents.CONTENT_URI, historyData.createValues());
        }
    }

    private static void updateBestsLimitedly(Context context,
            HistoryData historyData) {
        ContentResolver resolver = context.getContentResolver();

        // Query all bests.
        Cursor cursor = resolver.query(Bests.CONTENT_URI,
                SIMPLE_HISTORYS_PROJECTION, SIMPLE_HISTORYS_SELECTION,
                new String[] { String.valueOf(historyData.gameId()) }, Historys.SCORE);
        long idToUpdate = -1;
        boolean needInsert = false;
        boolean isBest = false;

        try {
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.getCount() >= MAX_HISTORYS_COUNT) {

                    if (cursor.moveToFirst()
                            && historyData.score() > cursor
                                    .getInt(SIMPLE_HISTORY_INDEX_OF_SCORE)) {
                        // Update it.
                        idToUpdate = cursor.getLong(0);
                        if (cursor.moveToLast()
                                && historyData.score() >= cursor
                                        .getInt(SIMPLE_HISTORY_INDEX_OF_SCORE)) {
                            isBest = true;
                        }
                    } else {
                        // Do nothing.
                    }
                } else {
                    // Insert
                    needInsert = true;
                    if (cursor.moveToLast()
                            && historyData.score() >= cursor
                                    .getInt(SIMPLE_HISTORY_INDEX_OF_SCORE)) {
                        isBest = true;
                    }
                }
            } else {
                needInsert = true;
                isBest = true;
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        // Update if needed.
        if (idToUpdate >= 0) {
            Log.i(TAG, "Update: " + idToUpdate);
            resolver.update(Bests.CONTENT_URI, historyData.createValues(),
                    Historys._ID + "=" + idToUpdate, null);
        }

        // Insert if needed.
        if (needInsert) {
            resolver.insert(Bests.CONTENT_URI, historyData.createValues());
        }

        if (isBest) {
            updateGameData(context, historyData.gameId(), historyData.time(),
                    historyData.steps(), historyData.score());
        }
    }

    public static List<HistoryData> loadHistorys(Context context, Uri uri,
            long gameId, String orderBy, boolean desc) {
        List<HistoryData> historysData = new ArrayList<HistoryData>();
        String sortOrder = desc ? (orderBy + " DESC") : orderBy;

        Cursor cursor = context.getContentResolver().query(uri, HISTORYS_PROJECTION,
                SIMPLE_HISTORYS_SELECTION,
                new String[] { String.valueOf(gameId) }, sortOrder);
        try {
            if (cursor != null) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    historysData.add(new HistoryData(
                            cursor.getLong(HISTORY_INDEX_OF_GAME_ID),
                            cursor.getLong(HISTORY_INDEX_OF_DATE),
                            cursor.getLong(HISTORY_INDEX_OF_TIME_COST),
                            cursor.getInt(HISTORY_INDEX_OF_STEPS_COST),
                            cursor.getInt(HISTORY_INDEX_OF_SCORE)
                            ));
                }
            }
            return historysData;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public static void recordRanks(Context context, long gameId,
            List<RankData> ranksData, boolean notify) {
        if (gameId < 0 || ranksData == null) {
            return;
        }

        ContentResolver resolver = context.getContentResolver();

        // Delete all rows first.
        resolver.delete(Ranks.CONTENT_URI_NO_NOTIFICATION, SIMPLE_HISTORYS_SELECTION, new String[] { String.valueOf(gameId) });

        // Write them to db.
        int count = Math.min(ranksData.size(), MAX_HISTORYS_COUNT);
        ContentValues[] values = new ContentValues[count];
        RankData rankData;
        for (int i = 0; i < count; i++) {
            rankData = ranksData.get(i);
            values[i] = rankData.createValues();
        }
        resolver.bulkInsert(notify ? Ranks.CONTENT_URI
                : Ranks.CONTENT_URI_NO_NOTIFICATION, values);
    }

    public static List<RankData> loadRanks(Context context, long gameId) {
        List<RankData> ranksData = new ArrayList<RankData>();
        Cursor cursor = context.getContentResolver().query(Ranks.CONTENT_URI, RANKS_PROJECTION,
                SIMPLE_HISTORYS_SELECTION,
                new String[] { String.valueOf(gameId) }, Ranks.RANK);
        try {
            if (cursor != null) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    ranksData.add(new RankData(
                            cursor.getInt(RANK_INDEX_OF_RANK),
                            cursor.getString(RANK_INDEX_OF_PLAYER),
                            cursor.getLong(RANK_INDEX_OF_GAME_ID),
                            cursor.getLong(RANK_INDEX_OF_DATE),
                            cursor.getLong(RANK_INDEX_OF_TIME_COST),
                            cursor.getInt(RANK_INDEX_OF_STEPS_COST),
                            cursor.getInt(RANK_INDEX_OF_SCORE)
                            ));
                }
            }
            return ranksData;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }
}
