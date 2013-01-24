package me.elvishew.puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.elvishew.puzzle.Puzzle.Ranks;
import android.app.Dialog;
import android.content.DialogInterface.OnDismissListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

class LoadRanksParam {
    static final int LOAD_FROM_CACHE = 1;
    static final int LOAD_FROM_INTERNET = 2;

    int loadType;
    long gameId;
    String player;
    HistoryData historyData;

    public LoadRanksParam(int loadType, long gameId, String player, HistoryData historyData) {
        this.loadType = loadType;
        this.gameId = gameId;
        this.player = player;
        this.historyData = historyData;
    }
}

public class RankListActivity extends
        AbstractHistoryListActivity<LoadRanksParam, RankData> implements
        DialogHost {
    //    private static final String TAG = RankListActivity.class.getSimpleName();

    // Only make sense when come from GameActivity.
    private HistoryData mHistoryData; 
    private FrameLayout mMyAchievementFrame;
    private TextView mPlayer;
    private TextView mScore;
    private TextView mRank;
    private TextView mTime;
    private TextView mSteps;
    private ImageView mStar;

    private int myRank = Constants.INVALID_RANK;

    private TextView mReloadFromInternet;

    private Dialog mConnectDialog;

    private boolean isAchievementUploaded = false;

    private Handler mPostToastHandler = new Handler();

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        super.beforeCreate(savedInstanceState);
        mHistoryData = getIntent().getParcelableExtra(Constants.EXTRA_HISTORY_DATA);
    }

    @Override
    protected View onCreateContent(ViewStub contentStub) {
        contentStub.setLayoutResource(R.layout.rank_list_activity);
        View view = contentStub.inflate();

        mMyAchievementFrame = (FrameLayout) view.findViewById(R.id.my_achievement);

        if (mHistoryData != null) {
            mMyAchievementFrame.setVisibility(View.VISIBLE);

            // Find all achievement fields.
            mPlayer = (TextView) view.findViewById(R.id.achievement_player);
            mScore = (TextView) view.findViewById(R.id.achievement_score);
            mRank = (TextView) view.findViewById(R.id.achievement_rank);
            mTime = (TextView) view.findViewById(R.id.achievement_time);
            mSteps = (TextView) view.findViewById(R.id.achievement_steps);
            mStar = (ImageView) view.findViewById(R.id.achievement_star);

            // Set all achievement fields except rank field.
            String player = new PreferenceHelper(this).getPlayerName();
            mPlayer.setText(player);
            mScore.setText(String.valueOf(mHistoryData.score()));
            mTime.setText(Utils.long2Time(mHistoryData.time()));
            mSteps.setText(String.valueOf(mHistoryData.steps()));
            mStar.getDrawable().setLevel(Utils.score2Star(mHistoryData.score()));
        } else {
            mMyAchievementFrame.setVisibility(View.GONE);
        }

        mReloadFromInternet = (TextView) view.findViewById(R.id.refresh_ranks);
        mReloadFromInternet.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRankFromInternetWithCheck(mHistoryData);
            }
        });

        return view;
    }

    /**
     * Add list title when view created.
     */
    @Override
    protected void onContentCreated(View view) {
        super.onContentCreated(view);
        ViewStub listTitleStub = (ViewStub) getListFrame().findViewById(R.id.list_title_stub);
        listTitleStub.setLayoutResource(R.layout.rank_list_title);
        listTitleStub.inflate();
    }

    @Override
    protected ListView getListView(View view) {
        return (ListView) view.findViewById(R.id.internal_list);
    }

    @Override
    protected TextView getProgressText(View view) {
        return (TextView) view.findViewById(R.id.progress_text);
    }

    @Override
    protected int messageOfProgress() {
        return R.string.progress_message_loading_ranks;
    }

    @Override
    protected TextView getEmptyText(View view) {
        return (TextView) view.findViewById(android.R.id.empty);
    }

    @Override
    protected int messageOfEmpty() {
        return R.string.empty_message_no_ranks;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        super.afterCreate(savedInstanceState);
        loadRanks(HttpHelper.isNetworkAvailable(this) ? LoadRanksParam.LOAD_FROM_INTERNET
                : LoadRanksParam.LOAD_FROM_CACHE);
    }

    private void loadRanks(int loadType) {
        LoadRanksParam param = new LoadRanksParam(loadType, mGameId,
                new PreferenceHelper(this).getPlayerName(), mHistoryData);
        startLoad(param);
    }

    private void loadRankFromInternetWithCheck(HistoryData historyData) {
        if (HttpHelper.isNetworkAvailable(this)) {
            loadRanks(LoadRanksParam.LOAD_FROM_INTERNET);
        } else {
            HttpHelper.showNoConnectionDialog(this, this, 0);
        }
    }

    protected LoadRanksParam[] createLoadDataParams(LoadRanksParam param) {
        return (new LoadRanksParam[] {param});
    }

    protected void onLoadTaskPreExecute() {
        super.onLoadTaskPreExecute();
        mReloadFromInternet.setEnabled(false);
    }

    protected List<RankData> loadInBackground(LoadRanksParam... params) {
        if (params == null || params.length != 1) {
            return null;
        }

        LoadRanksParam param = params[0];
        switch (param.loadType) {
        case LoadRanksParam.LOAD_FROM_CACHE:
            return GamesRecorder.loadRanks(RankListActivity.this, param.gameId);
        case LoadRanksParam.LOAD_FROM_INTERNET:
            boolean insert = false;
            final int rank;
            if (param.historyData != null && !isAchievementUploaded) {
                insert = true;
                // Don't upload achievement for ever.
                isAchievementUploaded = true;
            }
            List<RankData> ranksData = new ArrayList<RankData>();
            rank = HttpHelper.loadRanksFromInternet(param.gameId, param.player,
                    param.historyData, ranksData, insert);

            if (rank < Constants.INVALID_RANK) {
                // Now, rank is an errorCode.
                // Post a toast to user friendly.
                final int toastMsg = HttpHelper.toastMsgOf(rank);
                mPostToastHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RankListActivity.this, toastMsg,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return null;
            } else {
                int count = ranksData.size();
                RankData[] array = new RankData[count];
                ranksData.toArray(array);
                Arrays.sort(array, new RankData.DescComparator());
                ranksData = Arrays.asList(array);

                int preScore = Integer.MAX_VALUE, preRank = 0;
                for (RankData rankData : ranksData) {
                    if (rankData.score() < preScore) {
                        preScore = rankData.score();
                        preRank++;
                    }
                    rankData.updateRank(preRank);
                }

                // Record ranks data to database.
                // Here we don't notify the change of database, since we will bind data to list
                // when onLoadTaskPostExecute(result).
                GamesRecorder.recordRanks(RankListActivity.this, param.gameId, ranksData, false);

                // Update rank if has.
                if (rank > Constants.INVALID_RANK) {
                    myRank = rank;
                } else {
                    myRank = Constants.INVALID_RANK;
                }
                return ranksData;
            }
        default:
            // Can not be here.
            return null;
        }
    }

    protected void onLoadTaskPostExecute(List<RankData> result) {
        super.onLoadTaskPostExecute(result);

        // Update rank if exists.
        if (mRank != null) {
            if (myRank > Constants.INVALID_RANK) {
                // Here handle the "+" simple.
                String rankString = String.valueOf(myRank / 2);
                if (myRank % 2 != 0) {
                    rankString += "+";
                }
                mRank.setText(rankString);
            } else {
                mRank.setText(R.string.no_rank);
            }
        }
        mReloadFromInternet.setEnabled(true);
    }

    protected void onLoadTaskCancelled() {
        super.onLoadTaskCancelled();
        mReloadFromInternet.setEnabled(true);
    }

    @Override
    protected int messageOfActionPrompt() {
        return R.string.action_prompt_delete_ranks;
    }

    @Override
    protected Uri getHistoryUri() {
        return Ranks.CONTENT_URI;
    }

    @Override
    protected void onHistoryChanged() {
        loadRanks(LoadRanksParam.LOAD_FROM_CACHE);
    }

    @Override
    protected View getDataItemView(RankData dataItem, View convertView,
            ViewGroup parent) {
        View view;
        RankItemViewHolder holder;
        if (convertView != null) {
            view = convertView;
            holder = (RankItemViewHolder) view.getTag();
        } else {
            view = getLayoutInflater().inflate(R.layout.rank_item, parent, false);
            holder = new RankItemViewHolder();
            holder.randField = (TextView) view.findViewById(R.id.rank_item_rank_field);
            holder.palyerField = (TextView) view.findViewById(R.id.rank_item_player_field);
            holder.timeField = (TextView) view.findViewById(R.id.rank_item_time_field);
            holder.stepsField = (TextView) view.findViewById(R.id.rank_item_steps_field);
            holder.scoreField = (TextView) view.findViewById(R.id.rank_item_score_field);
            holder.starField = (ImageView) view.findViewById(R.id.rank_item_star_field);
            holder.dateField = (TextView) view.findViewById(R.id.rank_item_date_field);
            view.setTag(holder);
        }

        holder.randField.setText(String.valueOf(dataItem.rank()));
        holder.palyerField.setText(dataItem.player());
        holder.timeField.setText(Utils.long2Time(dataItem.time()));
        holder.stepsField.setText(String.valueOf(dataItem.steps()));
        holder.scoreField.setText(String.valueOf(dataItem.score()));
        holder.starField.setImageLevel(Utils.score2Star(dataItem.score()));
        holder.dateField.setText(Utils.long2Date(dataItem.date()));
        return view;
    }

    static class RankItemViewHolder extends HistoryItemViewHolder {
        TextView randField;
        TextView palyerField;
    }

    @Override
    public Dialog getDialogById(int dialogId) {
        return mConnectDialog;
    }

    @Override
    public void setDialogById(int dialogId, Dialog dialog) {
        mConnectDialog = dialog;
    }

    @Override
    public void setOnDialogDissmissListener(int dialogId, OnDismissListener listener) {
        mConnectDialog.setOnDismissListener(listener);
    }

    @Override
    public void showDialogById(int dialogId) {
        mConnectDialog.show();
    }

    @Override
    public void dismissDialogById(int dialogId) {
        mConnectDialog.dismiss();
    }
}
