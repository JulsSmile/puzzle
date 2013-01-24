package me.elvishew.puzzle;

import android.net.Uri;

/**
 * Contains constants used in database.
 * @author yongan.qiu
 */
public class Puzzle {
    public static class Games {
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + Tables.GAMES);
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri
                .parse("content://" + AUTHORITY + "/" + Tables.GAMES + "?"
                        + PARAMETER_NOTIFY + "=false");
//        public static final String _ID = "_id";
        public static final String GAME_ID = "game_id";
        public static final String LEVEL = "level";
        public static final String NAME = "name";
        public static final String IMAGE = "image";
        public static final String TIME = "time";
        public static final String STEPS = "steps";
        public static final String SCORE = "score";
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + Tables.GAMES +" (" + 
//                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
                GAME_ID + " INTEGER PRIMARY KEY NOT NULL," + 
                LEVEL + " INTEGER NOT NULL," + 
                NAME + " TEXT NOT NULL," + 
                IMAGE + " TEXT NOT NULL," + 
                TIME + " INTEGER NOT NULL DEFAULT 0," + 
                STEPS + " INTEGER NOT NULL DEFAULT 0," + 
                SCORE + " INTEGER NOT NULL DEFAULT 0);";
    }

    public static interface Achievements {
        public static final String TIME = "time";
        public static final String STEPS = "steps";
        public static final String SCORE = "score";
    }

    public static class Historys implements Achievements {
        public static final String _ID = "_id";
        public static final String GAME_ID = "game_id";
        public static final String DATE = "date";
    }

    public static class Recents extends Historys {
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + Tables.RECENTS);
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri
                .parse("content://" + AUTHORITY + "/" + Tables.RECENTS + "?"
                        + PARAMETER_NOTIFY + "=false");
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + Tables.RECENTS +" (" + 
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
                GAME_ID + " INTEGER NOT NULL," + 
                TIME + " INTEGER NOT NULL DEFAULT 0," + 
                STEPS + " INTEGER NOT NULL DEFAULT 0," +
                SCORE + " INTEGER NOT NULL DEFAULT 0," + 
                DATE + " INTEGER NOT NULL DEFAULT 0);";
    }

    public static class Bests extends Historys {
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + Tables.BESTS);
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri
                .parse("content://" + AUTHORITY + "/" + Tables.BESTS + "?"
                        + PARAMETER_NOTIFY + "=false");
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + Tables.BESTS +" (" + 
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
                GAME_ID + " INTEGER NOT NULL," + 
                TIME + " INTEGER NOT NULL DEFAULT 0," + 
                STEPS + " INTEGER NOT NULL DEFAULT 0," +
                SCORE + " INTEGER NOT NULL DEFAULT 0," + 
                DATE + " INTEGER NOT NULL DEFAULT 0);";
    }

    public static class Ranks extends Historys {
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + Tables.RANKS);
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri
                .parse("content://" + AUTHORITY + "/" + Tables.RANKS + "?"
                            + PARAMETER_NOTIFY + "=false");
        public static final String RANK = "rank";
        public static final String PLAYER = "player";
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + Tables.RANKS +" (" + 
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
                GAME_ID + " INTEGER NOT NULL," + 
                RANK + " INTEGER NOT NULL DEFAULT 0," + 
                PLAYER + " TEXT NOT NULL," + 
                TIME + " INTEGER NOT NULL DEFAULT 0," + 
                STEPS + " INTEGER NOT NULL DEFAULT 0," +
                SCORE + " INTEGER NOT NULL DEFAULT 0," + 
                DATE + " INTEGER NOT NULL DEFAULT 0);";
    }

    public static class Tables {
        public static final String GAMES = "games";
        public static final String RECENTS = "recents";
        public static final String BESTS = "bests";
        public static final String RANKS = "ranks";
    }

    public static final String AUTHORITY = "me.elvishew.puzzle";

    public static final String PARAMETER_NOTIFY = "notify";

    public static final String DB_FILE = "puzzle.db";
}
