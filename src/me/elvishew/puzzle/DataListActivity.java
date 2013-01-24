package me.elvishew.puzzle;

import java.util.List;

import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Must containing 3 elements:
 * <p>
 * 1. android.R.id.list
 * <br>
 * 2. android.R.id.empty
 * <br>
 * 3. android.R.id.progress
 * 
 * @author person
 *
 */
public abstract class DataListActivity<P, D> extends BackAndActionActivity {

    protected static final int FRAME_TYPE_LIST = 0;
    protected static final int FRAME_TYPE_EMPTY = 1;
    protected static final int FRAME_TYPE_PROGRESS = 2;

    private View mListFrame;
    private View mEmptyFrame;
    private View mProgressrFrame;

    private DataAdapter mAdapter;
    private ListView mListView;

    private TextView mProgressText;
    private TextView mEmptyText;

    private LoadDataTask mLoadDataTask;

    @Override
    protected void onContentCreated(View view) {
        super.onContentCreated(view);
        mListFrame = view.findViewById(android.R.id.list);
        mProgressrFrame = view.findViewById(android.R.id.progress);
        mEmptyFrame = view.findViewById(android.R.id.empty);

        if (mListFrame == null || mEmptyFrame == null || mProgressrFrame == null) {
            throw new IllegalStateException("No list, empty or progress!");
        }

        mListView = getListView(mListFrame);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                onListItemClick(arg0, arg1, arg2, arg3);
            }});
        mAdapter = new DataAdapter();

        mProgressText = getProgressText(mProgressrFrame);
        mProgressText.setText(messageOfProgress());

        mEmptyText = getEmptyText(mEmptyFrame);
        mEmptyText.setText(messageOfEmpty());

    }

    protected void onListItemClick(AdapterView<?> arg0, View arg1, int arg2,
            long arg3) {
    }

    protected View getListFrame() {
        return mListFrame;
    }

    protected void showListFrame() {
        mListFrame.setVisibility(View.VISIBLE);
        mEmptyFrame.setVisibility(View.INVISIBLE);
        mProgressrFrame.setVisibility(View.INVISIBLE);
        onFrameChanged(FRAME_TYPE_LIST);
    }

    protected void showListFrameWithData(List<D> ranksData) {
        showListFrame();
        mAdapter.setRanksData(ranksData);

        // FIXME I don't know why notifyDataSetChanged() can not work,
        // maybe the size of listview does not change, so relayout()
        // does not make sense, and then getView() never be called?
        mListView.setAdapter(mAdapter);
        // mAdapter.notifyDataSetChanged();
    }

    protected void showEmptyFrame() {
        mAdapter.setRanksData(null);
        mAdapter.notifyDataSetChanged();

        mListFrame.setVisibility(View.INVISIBLE);
        mEmptyFrame.setVisibility(View.VISIBLE);
        mProgressrFrame.setVisibility(View.INVISIBLE);
        onFrameChanged(FRAME_TYPE_EMPTY);
    }

    protected void showProgressFrame() {
        mListFrame.setVisibility(View.INVISIBLE);
        mEmptyFrame.setVisibility(View.INVISIBLE);
        mProgressrFrame.setVisibility(View.VISIBLE);
        onFrameChanged(FRAME_TYPE_PROGRESS);
    }

    protected void onFrameChanged(int newFrameType) {
        if (newFrameType == FRAME_TYPE_LIST) {
            setActionEnabled(true);
        } else {
            setActionEnabled(false);
        }
    }

    protected void startLoad(P param) {
        mLoadDataTask = new LoadDataTask();
        mLoadDataTask.execute(createLoadDataParams(param));
    }

    protected void cancelLoad() {
        if (!mLoadDataTask.isCancelled()) {
            mLoadDataTask.cancel(true);
        }
    }

    protected abstract ListView getListView(View view);

    protected abstract TextView getProgressText(View view);

    protected int messageOfProgress() {
        return R.string.progress_message_loading;
    }

    protected abstract TextView getEmptyText(View view);

    protected int messageOfEmpty() {
        return R.string.empty_message_no_data;
    }

    protected abstract P[] createLoadDataParams(P param);

    protected void onLoadTaskPreExecute() {
        showProgressFrame();
    }

    protected abstract List<D> loadInBackground(P... params);

    protected void onLoadTaskPostExecute(List<D> result) {
        if (result == null || result.size() <= 0) {
            showEmptyFrame();
        } else {
            showListFrameWithData(result);
        }
    }

    protected void onLoadTaskCancelled() {
        showEmptyFrame();
    }

    protected abstract View getDataItemView(D dataItem, View convertView, ViewGroup parent);

    private class LoadDataTask extends AsyncTask<P, Integer, List<D>> {

        @Override
        protected void onPreExecute() {
            onLoadTaskPreExecute();
        }

        @Override
        protected List<D> doInBackground(P... params) {
            return loadInBackground(params);
        }

        @Override
        protected void onPostExecute(List<D> result) {
            onLoadTaskPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            onLoadTaskCancelled();
        }

    }

    private class DataAdapter extends BaseAdapter {

        private List<D> mDataItems;

        public void setRanksData(List<D> ranksData) {
            mDataItems = ranksData;
        }

        @Override
        public int getCount() {
            return mDataItems == null ? 0 : mDataItems.size();
        }

        @Override
        public D getItem(int position) {
            return mDataItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getDataItemView(getItem(position), convertView, parent);
        }
    }

}
