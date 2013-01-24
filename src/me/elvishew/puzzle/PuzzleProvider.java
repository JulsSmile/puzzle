
package me.elvishew.puzzle;

import me.elvishew.puzzle.Puzzle.Tables;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

public class PuzzleProvider extends ContentProvider {

    private static final String TAG = PuzzleProvider.class.getSimpleName();

    // Database instance
    private static SQLiteDatabase mSQLiteDatabase = null;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int GAMES = 1;
    private static final int RECENTS = 2;
    private static final int BESTS = 3;
    private static final int RANKS = 4;

    static {
        sURIMatcher.addURI(Puzzle.AUTHORITY, "games", GAMES);
        sURIMatcher.addURI(Puzzle.AUTHORITY, "recents", RECENTS);
        sURIMatcher.addURI(Puzzle.AUTHORITY, "bests", BESTS);
        sURIMatcher.addURI(Puzzle.AUTHORITY, "ranks", RANKS);
    }

    @Override
    public boolean onCreate() {
        init();
        return mSQLiteDatabase != null;
    }

    public void init() throws SQLException {
        if (mSQLiteDatabase == null) {
            try {
                mSQLiteDatabase = getContext().openOrCreateDatabase(Puzzle.DB_FILE, SQLiteDatabase.OPEN_READONLY, null);
            } catch (SQLiteException e) {
                Log.e(TAG, "number area database open fail.");
            }
        }
        Cursor cursor = null;
        try {
            cursor = mSQLiteDatabase.query(Puzzle.Tables.GAMES, null, null, null, null, null, null);
        } catch (SQLException e) {
            Log.i(TAG, "Tables not exists, so creat it.");
            mSQLiteDatabase.execSQL(Puzzle.Games.SQL_CREATE_TABLE);
            mSQLiteDatabase.execSQL(Puzzle.Recents.SQL_CREATE_TABLE);
            mSQLiteDatabase.execSQL(Puzzle.Bests.SQL_CREATE_TABLE);
            mSQLiteDatabase.execSQL(Puzzle.Ranks.SQL_CREATE_TABLE);
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (mSQLiteDatabase == null) {
            return null;
        }

        String table = uri2Table(uri);
        if (table == null) {
            return null;
        }

        return mSQLiteDatabase.query(table, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (mSQLiteDatabase == null) {
            return null;
        }

        String table = uri2Table(uri);
        if (table == null) {
            return null;
        }

        long rowId = mSQLiteDatabase.insert(table, null, values);
        if (rowId < 0) {
            Log.e(TAG, "insert failded!");
            return null;
        }

        uri = ContentUris.withAppendedId(uri, rowId);
        sendNotify(uri);

        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (mSQLiteDatabase == null) {
            return 0;
        }

        String table = uri2Table(uri);
        if (table == null) {
            return 0;
        }
        mSQLiteDatabase.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                if (mSQLiteDatabase.insert(table, null, values[i]) < 0) {
                    return 0;
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } finally {
            mSQLiteDatabase.endTransaction();
        }

        sendNotify(uri);
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (mSQLiteDatabase == null) {
            return 0;
        }

        String table = uri2Table(uri);
        if (table == null) {
            return 0;
        }

        int count = mSQLiteDatabase.delete(table, selection, selectionArgs);
        if (count > 0) {
            sendNotify(uri);
        }

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (mSQLiteDatabase == null) {
            return 0;
        }

        String table = uri2Table(uri);
        if (table == null) {
            return 0;
        }

        int count = mSQLiteDatabase.update(table, values, selection, selectionArgs);
        if (count > 0) {
            sendNotify(uri);
        }

        return count;
    }

    private String uri2Table(Uri uri) {
        int match = sURIMatcher.match(uri);
        String table = null;
        switch (match) {
            case GAMES:
                table = Tables.GAMES;
                break;
            case RECENTS:
                table = Tables.RECENTS;
                break;
            case BESTS:
                table = Tables.BESTS;
                break;
            case RANKS:
                table = Tables.RANKS;
                break;
            default:
                break;
        }
        return table;
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(Puzzle.PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

}
