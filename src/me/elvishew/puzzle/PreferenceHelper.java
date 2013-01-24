package me.elvishew.puzzle;
import android.content.Context;
import android.content.SharedPreferences;


public class PreferenceHelper {
    private static final String PREFERENCE_NAME = "puzzle";

    private static final String KEY_GAMES_DATA_INITIALIZED = "games_data_initialized";
    private static final String KEY_PLAYER_NAME_EVER_SET = "player_name_ever_set";
    private static final String KEY_PLAYER_NAME = "player_name";

    private SharedPreferences mPreferences;
    private Context mContext;

    public PreferenceHelper(Context context) {
        mContext = context;
        mPreferences = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

    }

    public boolean isGamesInitialized() {
        return mPreferences.getBoolean(KEY_GAMES_DATA_INITIALIZED, false);
    }

    public void setGamesInitialized(boolean initialized) {
        mPreferences.edit().putBoolean(KEY_GAMES_DATA_INITIALIZED, initialized).commit();
    }

    public boolean isPlayerNameEverSet() {
        return mPreferences.getBoolean(KEY_PLAYER_NAME_EVER_SET, false);
    }

    public void setPlayerNameEverSet(boolean everSet) {
        mPreferences.edit().putBoolean(KEY_PLAYER_NAME_EVER_SET, everSet).commit();
    }

    public String getPlayerName() {
        return mPreferences.getString(KEY_PLAYER_NAME, Constants.DEFAULT_PLAYER_NAME);
    }

    public void setPlayerName(String playerName) {
        mPreferences.edit().putString(KEY_PLAYER_NAME, playerName).commit();
    }

}
