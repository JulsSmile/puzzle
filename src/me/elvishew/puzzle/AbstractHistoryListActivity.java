package me.elvishew.puzzle;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class AbstractHistoryListActivity<P, D> extends DataListActivity<P, D> {

    protected long mGameId;

    private ContentObserver mHistoryObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            onHistoryChanged();
        }

    };

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        super.beforeCreate(savedInstanceState);
        mGameId = getIntent().getLongExtra(Constants.EXTRA_GAME_ID, -1);
        if (mGameId == -1) {
            throw new IllegalArgumentException("Game id should be providered!");
        }
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        super.afterCreate(savedInstanceState);
        getContentResolver().registerContentObserver(getHistoryUri(), false, mHistoryObserver);
    }

    protected abstract Uri getHistoryUri();

    protected abstract void onHistoryChanged();

    @Override
    protected int getActionImage() {
        return R.drawable.delete;
    }

    @Override
    protected boolean isActionNeedPrompt() {
        return true;
    }

    @Override
    protected final void onAction() {
        doDeleteHistory();
    }

    @Override
    protected int getActionDescription() {
        return R.string.action_delete;
    }

    private void doDeleteHistory() {
        GamesRecorder.deleteHistorys(this, getHistoryUri(), mGameId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mHistoryObserver);
    }

    static class HistoryItemViewHolder {
        TextView scoreField;
        TextView timeField;
        TextView stepsField;
        ImageView starField;
        TextView dateField;
    }

}