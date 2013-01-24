package me.elvishew.puzzle;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class Game {
    private static final String TAG = Game.class.getSimpleName();

    public enum State {INVALID, READY, PLAYING, PAUSE, SUCCESS}

    private int mPanelSize = 210;
    private int mPreviewSize = 130;
    private int mSize;
    private int mNodeCount; // mSize * mSize
    private int mGridSize; // mPanelSize / mSize
    private int mLuckySlotPosition;

    // private int mRate;

//    private String mPicturePath;
    private Node[] mNodeVector;

//    private Bitmap mOriginBitmap;
    private Bitmap mPreviewBitmap;
    private Bitmap mGameBitmap;

    private int mSteps = 0;
    private State mState = State.INVALID;

    // The time game started.
    private long mStartTime;

    private long mTimeTillPause;

    public Game() {
    }

    public Game(GameListener listener) {
        mGameListener = listener;
    }

    public void initPreviewAndPanelSize(int previewSize, int panelSize) {
        mPreviewSize = previewSize;
        mPanelSize = panelSize;
    }

    public void changeSize(int size) {
        resetGameIfNeeded();
        mSize = size;
        mNodeCount = mSize * mSize;
        mGridSize = mPanelSize / mSize;
        Log.i(TAG, "mSize = " + mSize + "\n" +
                "mNodeCount = " + mNodeCount + "\n" +
                "mGridSize = " + mGridSize + "\n" +
                "mPanelSize = " + mPanelSize + "\n");
    }

    public void changeImage(Bitmap oriImage) {
        resetGameIfNeeded();

        createGameImage(oriImage);

        createPreviewImage(oriImage);
        if (mGameListener != null) {
            mGameListener.onPreviewChanged(mPreviewBitmap);
        }
    }

    private void resetGameIfNeeded() {
        
    }

//    private void saveOriginImage(Bitmap src) {
//        // Recycle bitmap.
//        if (mOriginBitmap != null) {
//            mOriginBitmap.recycle();
//        }
//
//        // Save bitmap.
//        mOriginBitmap = src;
//    }

    private void createPreviewImage(Bitmap src) {
        // Recycle bitmap.
        if (mPreviewBitmap != null) {
            mPreviewBitmap.recycle();
        }

        // Create bitmap.
        mPreviewBitmap = Bitmap.createScaledBitmap(src, mPreviewSize,
                mPreviewSize, true);
    }

    private void createGameImage(Bitmap src) {
        // Recycle bitmap.
        if (mGameBitmap != null) {
            mGameBitmap.recycle();
        }

        // Create bitmap.
        mGameBitmap = Bitmap.createScaledBitmap(src, mPanelSize, mPanelSize, true);
    }

    public void init() {
        createNodes();
        mLuckySlotPosition = mSize * mSize - 1;
    }

    public void ready() {
        mTimeTillPause = 0;
        resetSteps();

        mState = State.READY;
        if (mGameListener != null) {
            mGameListener.onGameReady();
        }
    }

    public void start() {
        if (!mState.equals(State.READY)) {
            ready();
        }

        internalStart();
    }

    private void internalStart() {
        disorderNodes();

        mStartTime = System.currentTimeMillis();

        mState = State.PLAYING;

        if (mGameListener != null) {
            mGameListener.onGameStart();
        }
    }

    public void pause() {
        mTimeTillPause = System.currentTimeMillis() - mStartTime;

        mState = State.PAUSE;
        if (mGameListener != null) {
            mGameListener.onGamePause();
        }
    }

    public void fastPause() {
        mTimeTillPause = System.currentTimeMillis() - mStartTime;

        mState = State.PAUSE;
        if (mGameListener != null) {
            mGameListener.onGameFastPause();
        }
    }

    public void resume() {
        // Offset start time.
        mStartTime = System.currentTimeMillis() - mTimeTillPause;

        mState = State.PLAYING;
        if (mGameListener != null) {
            mGameListener.onGameResume();
        }
    }

    public void fastResume() {
        // Offset start time.
        mStartTime = System.currentTimeMillis() - mTimeTillPause;

        mState = State.PLAYING;
        if (mGameListener != null) {
            mGameListener.onGameFastResume();
        }
    }

    public void restart() {
        init();
        ready();
        start();
    }

    private void createNodes() {
        Node[] nodes = createNodes(mSize * mSize);
        mNodeVector = nodes;
        if (mGameListener != null) {
            mGameListener.onNodesCreated(nodes, mSize, mGridSize);
        }
    }

    private Node[] createNodes(int num) {
        Node[] nodes = new Node[num];
        for (int i = 0; i < num; i++) {
            Node node = new Node(mSize,
                    mGridSize, mGameBitmap, Color.BLACK).setSlotPosition(i).setCurrentContent(i);
            nodes[i] = node;
        }
        return nodes;
    }

    private void disorderNodes() {
        int index;
        int i;
        for (i = 0; i < mNodeCount - 1; i++) {
            index = (int) (Math.random() * (mNodeCount - 1));
            if (i != index) {
                Node.exchangeContent(mNodeVector[i], mNodeVector[index]);
            } else
                i--;
        }

        if (mNodeCount % 2 == 0)
            Node.exchangeContent(mNodeVector[0], mNodeVector[1]);

        if (mGameListener != null) {
            mGameListener.onNodesDisordered();
        }
    }

    public boolean isNearByLuckySlot(NodeView node) {
        boolean result = false;
        int position = node.getNode().getSlotPosition();
        if (((position == mLuckySlotPosition - 1) && (mLuckySlotPosition % mSize != 0)) || // To the left
                ((position == mLuckySlotPosition + 1) && (position % mSize != 0)) || // To the right
                (position == mLuckySlotPosition - mSize) || // Above
                (position == mLuckySlotPosition + mSize)) // Below
            result = true;
        return result;
    }

    public int getLuckySlotPosition() {
        return mLuckySlotPosition;
    }

    public void setExitNodePosition(int position) {
        mLuckySlotPosition = position;
    }

    public void resetSteps() {
        mSteps = 0;
        if (mGameListener != null) {
            mGameListener.onStepsChanged(mSteps);
        }
    }

    public void increseSteps() {
        if (!mState.equals(State.PLAYING)) {
            //throw error. illegal state.
            mSteps = 0;
            return;
        }
        mSteps++;

        if (mGameListener != null) {
            mGameListener.onStepsChanged(mSteps);
        }
    }

    public int getStepsSinceStarted() {
        return mSteps;
    }

    public String getTimeStringSinceStarted() {
        return Utils.long2Time(getTimeSinceStarted());
    }

    public long getTimeSinceStarted() {
        if (mState.equals(State.READY)) {
            return 0;
        }
        long time = Math.max(((System.currentTimeMillis() - mStartTime) / 100), 0);
        time = Math.min(time, 60 * 60 * 10); // 60 * 60 * 1000 = 1h, should not be longer than 1h.
        return time;
    }

    /**
     * Check whether each node in the correct place, if yes,
     * it means success, then update the state of game.
     * 
     * @return true if success, false otherwise
     */
    public boolean checkSuccess() {
        boolean success = true;
        // Succeed only if all node in the current place.
        for (int i = 0; i < mNodeCount; i++) {
            if (mNodeVector[i].getCurrentContent() != i) {
                success = false;
                break;
            }
        }

        if (success) {
            mState = State.SUCCESS;
            if (mGameListener != null) {
                mGameListener.onGameSuccess();
            }
        }
        return success;
    }

    public State getCurrentState() {
        return mState;
    }

    private GameListener mGameListener;

    public void setGameListener(GameListener listener) {
        mGameListener = listener;
    }

    interface GameListener {
        public void onPreviewChanged(Bitmap preview);
        public void onNodesCreated(Node[] nodes, int size, int gridSize);
        public void onStepsChanged(int steps);
        public void onNodesDisordered();

        public void onGameReady();
        public void onGameStart();
        public void onGamePause();
        public void onGameFastPause();
        public void onGameResume();
        public void onGameFastResume();
        public void onGameSuccess();
    }
}