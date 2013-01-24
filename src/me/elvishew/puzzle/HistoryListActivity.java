package me.elvishew.puzzle;

import java.util.List;

import me.elvishew.puzzle.Puzzle.Bests;
import me.elvishew.puzzle.Puzzle.Historys;
import me.elvishew.puzzle.Puzzle.Recents;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class HistoryListActivity extends AbstractHistoryListActivity<Long, HistoryData> {

    public static final int HISTORY_TYPE_INVALID = 0;
    public static final int HISTORY_TYPE_RECENTS = 1;
    public static final int HISTORY_TYPE_BESTS = 2;

    private int mHistoryType;

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        super.beforeCreate(savedInstanceState);
        mHistoryType = getIntent().getIntExtra(Constants.EXTRA_HISTORY_TYPE, HISTORY_TYPE_INVALID);
        if (mHistoryType == HISTORY_TYPE_INVALID) {
            throw new IllegalArgumentException("History type must be providered!");
        }
    }

    @Override
    protected void onCustomTitleCreated(View customTitle) {
        super.onCustomTitleCreated(customTitle);
        TextView title = (TextView) customTitle.findViewById(R.id.title);
        if (mHistoryType == HISTORY_TYPE_RECENTS) {
            title.setText(R.string.recents);
        } else { //mHistoryType == HISTORY_TYPE_BESTS
            title.setText(R.string.bests);
        }
    }

    @Override
    protected View onCreateContent(ViewStub contentStub) {
        contentStub.setLayoutResource(R.layout.history_list_activity);
        return contentStub.inflate();
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        super.afterCreate(savedInstanceState);
        startLoad(mGameId);
    }

    /**
     * Add list title when view created.
     */
    @Override
    protected void onContentCreated(View view) {
        super.onContentCreated(view);
        ViewStub listTitleStub = (ViewStub) getListFrame().findViewById(
                R.id.list_title_stub);
        if (mHistoryType == HISTORY_TYPE_RECENTS) {
            listTitleStub.setLayoutResource(R.layout.recent_list_title);
        } else { // mHistoryType == HISTORY_TYPE_BESTS
            listTitleStub.setLayoutResource(R.layout.best_list_title);
        }
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
        if (mHistoryType == HISTORY_TYPE_RECENTS) {
            return R.string.progress_message_loading_recents;
        } else { //mHistoryType == HISTORY_TYPE_BESTS
            return R.string.progress_message_loading_bests;
        }
    }

    @Override
    protected TextView getEmptyText(View view) {
        return (TextView) view.findViewById(android.R.id.empty);
    }

    @Override
    protected int messageOfEmpty() {
        if (mHistoryType == HISTORY_TYPE_RECENTS) {
            return R.string.empty_message_no_recents;
        } else { //mHistoryType == HISTORY_TYPE_BESTS
            return R.string.empty_message_no_bests;
        }
    }

    @Override
    protected int messageOfActionPrompt() {
        if (mHistoryType == HISTORY_TYPE_RECENTS) {
            return R.string.action_prompt_delete_recents;
        } else { //mHistoryType == HISTORY_TYPE_BESTS
            return R.string.action_prompt_delete_bests;
        }
    }

    @Override
    protected Uri getHistoryUri() {
        if (mHistoryType == HISTORY_TYPE_RECENTS) {
            return Recents.CONTENT_URI;
        } else { //mHistoryType == HISTORY_TYPE_BESTS
            return Bests.CONTENT_URI;
        }
    }

    @Override
    protected void onHistoryChanged() {
        startLoad(mGameId);
    }

    @Override
    protected Long[] createLoadDataParams(Long param) {
        return (new Long[] { param });
    }

    @Override
    protected List<HistoryData> loadInBackground(Long... params) {
        if (params == null || params.length != 1) {
            return null;
        }
        Long param = params[0];
        if (mHistoryType == HISTORY_TYPE_RECENTS) {
            return GamesRecorder.loadHistorys(HistoryListActivity.this, Recents.CONTENT_URI, param, Historys.DATE, true);
        } else { //mHistoryType == HISTORY_TYPE_BESTS
            return GamesRecorder.loadHistorys(HistoryListActivity.this, Bests.CONTENT_URI, param, Historys.SCORE, true);
        }
    }

    @Override
    protected View getDataItemView(HistoryData dataItem, View convertView,
            ViewGroup parent) {
        View view;
        HistoryItemViewHolder holder;
        if (convertView != null) {
            view = convertView;
            holder = (HistoryItemViewHolder) view.getTag();
        } else {
            if (mHistoryType == HISTORY_TYPE_RECENTS) {
                view = getLayoutInflater().inflate(R.layout.recent_item, parent, false);
                holder = new HistoryItemViewHolder();
                holder.dateField = (TextView) view.findViewById(R.id.recent_item_date_field);
                holder.scoreField = (TextView) view.findViewById(R.id.recent_item_score_field);
                holder.starField = (ImageView) view.findViewById(R.id.recent_item_star_field);
                holder.timeField = (TextView) view.findViewById(R.id.recent_item_time_field);
                holder.stepsField = (TextView) view.findViewById(R.id.recent_item_steps_field);
            } else { //mHistoryType == HISTORY_TYPE_BESTS
                view = getLayoutInflater().inflate(R.layout.best_item, parent, false);
                holder = new HistoryItemViewHolder();
                holder.scoreField = (TextView) view.findViewById(R.id.best_item_score_field);
                holder.starField = (ImageView) view.findViewById(R.id.best_item_star_field);
                holder.dateField = (TextView) view.findViewById(R.id.best_item_date_field);
                holder.timeField = (TextView) view.findViewById(R.id.best_item_time_field);
                holder.stepsField = (TextView) view.findViewById(R.id.best_item_steps_field);
            }

            view.setTag(holder);
        }

        holder.dateField.setText(Utils.long2Date(dataItem.date()));
        holder.scoreField.setText(String.valueOf(dataItem.score()));
        holder.timeField.setText(Utils.long2Time(dataItem.time()));
        holder.stepsField.setText(String.valueOf(dataItem.steps()));
        holder.starField.setImageLevel(Utils.score2Star(dataItem.score()));
        return view;
    }
}