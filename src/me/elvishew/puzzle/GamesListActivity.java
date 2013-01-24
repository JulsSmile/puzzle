package me.elvishew.puzzle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.elvishew.puzzle.Puzzle.Games;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewStub;
import android.widget.TextView;
import android.widget.Toast;

public class GamesListActivity extends CustomTitleActivity {

    private static final String TAG = GamesListActivity.class.getSimpleName();

    private PreferenceHelper mPreferenceHelper;

    private List<GameData> mGamesData;

    private HashMap<Integer, ArrayList<GameData>> mLevels = new HashMap<Integer, ArrayList<GameData>>();

    private TextView mTitleText;

    private ViewPager mListPager;

    private int mCurrentPage = 0;

    ArrayList<LevelFragment> mPages = new ArrayList<LevelFragment>();

    private PagerAdapter mPagerAdapter = new GamePagerAdapter(getSupportFragmentManager());

    private class GamePagerAdapter extends FragmentPagerAdapter {

        public GamePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0) {
            return mPages.get(arg0);
        }

        @Override
        public int getCount() {
            return mPages.size();
        }

    };

    private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int arg0) {
            Log.i(TAG, "onPageSelected(): arg0 = " + arg0);
            mCurrentPage = arg0;
            mTitleText.setText(createTitle());
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            mGamesData = loadGameData();
            Log.i(TAG, "onChange(): mGamesData " + mGamesData);
            arrangeGamesDataToLevels();
            int levelCount = mLevels.size();

            ArrayList<GameData> levelData;
            for (int i = 0; i < levelCount; i++) {
                levelData = mLevels.get(i);
                mPages.get(i).updateGamesData(levelData);
            }
        }

    };

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        super.beforeCreate(savedInstanceState);
        final PreferenceHelper ph = new PreferenceHelper(this);
        if (!ph.isPlayerNameEverSet()) {
            // TODO Show nickname dialog
            ph.setPlayerNameEverSet(true);
        }
    }

    @Override
    protected View onCreateCustomTitle(ViewStub customTitleStub) {
        customTitleStub.setLayoutResource(R.layout.games_list_title);
        View view = customTitleStub.inflate();
        View settings = view.findViewById(R.id.settings);
        settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GamesListActivity.this, SettingsActivity.class);
                startActivity(intent);
            }});
        settings.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast toast = Toast.makeText(GamesListActivity.this, R.string.settings, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 0);
                toast.show();
                return true;
            }
        });
        return view;
    }

    @Override
    protected void onCustomTitleCreated(View customTitle) {
        super.onCustomTitleCreated(customTitle);
        mTitleText = ((TextView) customTitle.findViewById(R.id.title));
        mTitleText.setText(createTitle());
    }

    //FIXME No hard code.
    private String createTitle() {
        int resId;
        switch (mCurrentPage) {
        case 0:
            resId = R.string.level_easy;
            break;
        case 1:
            resId = R.string.level_normal;
            break;
        case 2:
            resId = R.string.level_hard;
            break;
        case 3:
            resId = R.string.level_custom;
        default:
            resId = R.string.level_default;
            break;
        }
        return getString(resId);
    }

    @Override
    protected View onCreateContent(ViewStub contentStub) {
        contentStub.setLayoutResource(R.layout.games_list);
        View content = contentStub.inflate();

        mListPager = (ViewPager) content.findViewById(R.id.list_pager);

        return content;
    }

/*    private void updateFragmentsVisibility(int showPos) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        int count = mPages.size();
        for (int i = 0; i < count; i++) {
            Fragment f = mPages.get(i);
            if (i == showPos) {
                Log.i(TAG, "showFragment " + showPos);
                showFragment(ft, f);
            } else {
                Log.i(TAG, "hideFragment " + i);
                hideFragment(ft, f);
            }
        }
        ft.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();

    }

    private void showFragment(FragmentTransaction ft, Fragment f) {
        if (!f.isVisible()) {
            ft.show(f);
            Log.i(TAG, "f visible ? " + f.isVisible());
        }
    }

    private void hideFragment(FragmentTransaction ft, Fragment f) {
        if (!f.isHidden()) {
            ft.hide(f);
        }
    }
*/
    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        super.afterCreate(savedInstanceState);
        mPreferenceHelper = new PreferenceHelper(this);
        mGamesData = loadGameData();

        Log.i(TAG, "mGamesData " + mGamesData);

        arrangeGamesDataToLevels();

        int levelCount = mLevels.size();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        for (int i = 0; i < levelCount; i++) {
            LevelFragment fragment = new LevelFragment();
            fragment.bindGamesData(mLevels.get(i));
            mPages.add(fragment);
        }
        ft.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();

        mListPager.setAdapter(mPagerAdapter);

        mListPager.setOnPageChangeListener(mPageChangeListener);

        mListPager.setCurrentItem(0);

        // Register listener.
        getContentResolver().registerContentObserver(Games.CONTENT_URI, false, mContentObserver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mContentObserver);
    }

    private List<GameData> loadGameData() {
        List<GameData> games = null;

        if (!mPreferenceHelper.isGamesInitialized()) {
            // Load games data from xml and write them to database.
            Log.i(TAG, "First load games data from xml.");
            try {
                games = GameData.readXML(this.getAssets().open("games.xml"));
                GamesRecorder.initGamesData(this, games);

                // Mark as initialized.
                mPreferenceHelper.setGamesInitialized(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Load games data from database.
            games = new ArrayList<GameData>();
            String orderBy = Games.LEVEL + ", " + Games.NAME;
            Cursor cursor = getContentResolver().query(Games.CONTENT_URI, null, null, null, orderBy);

            try {
                cursor.moveToFirst();
                int gameIdIndex = cursor.getColumnIndex(Games.GAME_ID);
                int levelIndex = cursor.getColumnIndex(Games.LEVEL);
                int nameIndex = cursor.getColumnIndex(Games.NAME);
                int imageIndex = cursor.getColumnIndex(Games.IMAGE);
                int scoreIndex = cursor.getColumnIndex(Games.SCORE);
                int timeCostIndex = cursor.getColumnIndex(Games.TIME);
                int stepsCostIndex = cursor.getColumnIndex(Games.STEPS);

                do {
                    GameData game = new GameData(
                            cursor.getLong(gameIdIndex),
                            cursor.getInt(levelIndex),
                            cursor.getString(nameIndex),
                            cursor.getString(imageIndex),
                            cursor.getInt(scoreIndex),
                            cursor.getLong(timeCostIndex),
                            cursor.getInt(stepsCostIndex));
                    games.add(game);
                } while (cursor.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
        return games;
    }

    private void arrangeGamesDataToLevels() {
        if (mGamesData == null || mGamesData.size() == 0) {
            return;
        }

        // Clear all games data first.
        int levelCount = mLevels.size();
        ArrayList<GameData> levelData;
        for (int i = 0; i < levelCount; i++) {
            levelData = mLevels.get(i);
            if (levelData != null) {
                levelData.clear();
            }
        }

        for (GameData gameData : mGamesData) {
            int level = gameData.level();
            levelData = mLevels.get(level);
            if (levelData == null) {
                levelData = new ArrayList<GameData>();
                mLevels.put(level, levelData);
            }
            levelData.add(gameData);
        }
    }
}
