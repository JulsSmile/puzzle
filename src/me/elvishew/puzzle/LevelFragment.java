package me.elvishew.puzzle;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class LevelFragment extends Fragment {
    private static final String TAG = LevelFragment.class.getSimpleName();

    private static final String GAMES_DATA = "games_data";

    private ArrayList<GameData> mGamesData;
    private GridView mGridGameList;
    private GameListAdapter mAdapter;

    private OnGameItemClickListener mOnGameItemClickListener = new OnGameItemClickListener();
    private OnGameItemLongClickListener mOnGameItemClickLongListener = new OnGameItemLongClickListener();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.i(TAG, getTag() + "onCreateView().");
        View view = inflater.inflate(R.layout.level_fragment, container, false);

        mGridGameList = (GridView) view.findViewById(R.id.grid_view);
        mAdapter = new GameListAdapter();
        mAdapter.setItems(mGamesData);
        mGridGameList.setAdapter(mAdapter);
        mGridGameList.setOnItemClickListener(mOnGameItemClickListener);
        mGridGameList.setOnItemLongClickListener(mOnGameItemClickLongListener);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mGamesData = savedInstanceState.getParcelableArrayList(GAMES_DATA);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(GAMES_DATA, mGamesData);
    }

    /** Must be called before {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} is called. */
    public void bindGamesData(ArrayList<GameData> gamesData) {
        Log.i(TAG, getTag() + "bindGamesData()." + gamesData);
        mGamesData = gamesData;
    }

    public void updateGamesData(ArrayList<GameData> gamesData) {
        Log.i(TAG, getTag() + "updateGamesData()." + gamesData);
        mGamesData = gamesData;
        if (getView() == null) {
            Log.w(TAG, "View not created yet, do nothing.");
            return;
        }
        mAdapter.setItems(mGamesData);
        mAdapter.notifyDataSetChanged();
    }

    private void viewRecents(long id) {
        Intent intent = new Intent(getActivity(), HistoryListActivity.class);
        intent.putExtra(Constants.EXTRA_GAME_ID, id);
        intent.putExtra(Constants.EXTRA_HISTORY_TYPE, HistoryListActivity.HISTORY_TYPE_RECENTS);
        startActivity(intent);
    }

    private void viewBests(long id) {
        Intent intent = new Intent(getActivity(), HistoryListActivity.class);
        intent.putExtra(Constants.EXTRA_GAME_ID, id);
        intent.putExtra(Constants.EXTRA_HISTORY_TYPE, HistoryListActivity.HISTORY_TYPE_BESTS);
        startActivity(intent);
    }

    private void viewRanks(long id) {
        Intent intent = new Intent(getActivity(), RankListActivity.class);
        intent.putExtra(Constants.EXTRA_GAME_ID, id);
        startActivity(intent);
    }

    private class OnGameItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            GameData gameData = (GameData) arg1.getTag();
            Log.i(TAG, "onItemClick: " + gameData);
            Intent playIntent = new Intent(getActivity(), GameActivity.class);
            playIntent.putExtra(Constants.EXTRA_GAME_ID, gameData.gameId());
            playIntent.putExtra(Constants.EXTRA_GAME_LEVEL, gameData.level());
            playIntent.putExtra(Constants.EXTRA_GAME_IMAGE, gameData.image());
            startActivity(playIntent);
        }
    }

    private class OnGameItemLongClickListener implements OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                final int arg2, long arg3) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setItems(R.array.game_grid_context_menu_items, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final long gameId = mGamesData.get(arg2).gameId();
                    switch (which) {
                    case 0: // View recents
                        viewRecents(gameId);
                        break;
                    case 1: // View bests
                        viewBests(gameId);
                        break;
                    case 2: // View ranks
                        viewRanks(gameId);
                        break;
                    case 3: // Clear data
                        clearAllDataWithCheck(gameId);
                        break;
                    default:
                        break;
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
            return true;
        }
    }

    private void clearAllDataWithCheck(final long gameId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.tip);
        builder.setMessage(R.string.delete_all_msg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GamesRecorder.deleteData(getActivity(), gameId);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    private class GameListAdapter extends BaseAdapter {

        List<GameData> mItems;

        public void setItems(List<GameData> items) {
            mItems = items;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public GameData getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.i(TAG, "getView position = " + position);
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = getActivity().getLayoutInflater().inflate(R.layout.game_item, parent, false);
            }

            GameData gameData = getItem(position);

            ImageView imageField = (ImageView) view.findViewById(R.id.image_field);
            imageField.setImageDrawable(PuzzleApplication.getThumbnailDrawable(
                    getActivity(), gameData.image()));
            ImageView starField = (ImageView) view.findViewById(R.id.star_field);
            starField.getDrawable().setLevel(Utils.score2Star(gameData.score()));

            view.setTag(gameData);
            return view;
        }
        
    }
}