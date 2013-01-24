
package me.elvishew.puzzle;

import java.io.IOException;

import cn.domob.android.ads.DomobInterstitialAd;
import cn.domob.android.ads.DomobInterstitialAdListener;
import cn.domob.android.ads.DomobAdManager.ErrorCode;

import me.elvishew.puzzle.Game.GameListener;
import me.elvishew.puzzle.Game.State;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class GameActivity extends BackAndActionActivity implements OnClickListener, GameListener{

    private static final String TAG = GameActivity.class.getSimpleName();

    private static final int SHINK_INTERVAL = 200; // ms
    private static final int SHINK_TIMES_MAX = 6; // 3 round.

    private Game mGame;

    private long mGameId;

    private HistoryData mHistoryData;

    // TODO Should be remove.
    private int mGameLevel;
    private String mGameImage;

    private FrameLayout mGameWindow;
//    private ImageView mPreview;
    private ImageView mMiniPreview;

    AbsoluteLayout mGamePanel;
    private TextView mStateController;

    private boolean mPausedBeforeActionPrompt;

    private Drawable mShinkDrawable;
    private int mShinkTimes;
    private boolean mSuccessDialogPending;
    private AlertDialog mSuccessDialog;

    private View mStepsView;
    private TextView mStepsText;

    private View mTimeView;
    private TextView mTimeText;

    private NodeView[] mNodeVector;

    DomobInterstitialAd mInterstitialAd;

    private static final int UPDATE_TIME = 0;
    private static final int UPDATE_SHINK_STATE = 1;

    private Handler mUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case UPDATE_TIME:
                mTimeText.setText(mGame.getTimeStringSinceStarted());
                if (mGame.getCurrentState().equals(State.PLAYING)) {
                    sendEmptyMessageDelayed(UPDATE_TIME, 100);
                }
                break;
            case UPDATE_SHINK_STATE:
                int newLevel = mShinkDrawable.getLevel() <= 1 ? 2 : 1;
                mShinkDrawable.setLevel(newLevel);
                mShinkTimes++;

                if (mShinkTimes < SHINK_TIMES_MAX) {
                    sendEmptyMessageDelayed(UPDATE_SHINK_STATE, SHINK_INTERVAL);
                } else {
                    showSuccessDialog();
                }
                break;
            default:
                break;
            }
        };
    };

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        mInterstitialAd = new DomobInterstitialAd(this, "56OJzoB4uNYG0R9GoY",
                DomobInterstitialAd.INTERSITIAL_SIZE_300X250);
        mInterstitialAd.setInterstitialAdListener(new DomobInterstitialAdListener() {
            @Override
            public void onInterstitialAdReady() {
                Log.i("DomobSDKDemo", "onAdReady");
            }

            @Override
            public void onLandingPageOpen() {
                Log.i("DomobSDKDemo", "onLandingPageOpen");
            }

            @Override
            public void onLandingPageClose() {
                Log.i("DomobSDKDemo", "onLandingPageClose");
            }

            @Override
            public void onInterstitialAdPresent() {
                Log.i("DomobSDKDemo", "onInterstitialAdPresent");
            }

            @Override
            public void onInterstitialAdDismiss() {
                //在上一条广告关闭时,请求下一条广告。
                mInterstitialAd.loadInterstitialAd();
                Log.i("DomobSDKDemo", "onInterstitialAdDismiss");
            }

            @Override
            public void onInterstitialAdFailed(ErrorCode arg0) {
                Log.i("DomobSDKDemo", "onInterstitialAdFailed");                
            }

            @Override
            public void onInterstitialAdLeaveApplication() {
                Log.i("DomobSDKDemo", "onInterstitialAdLeaveApplication");
                
            }
        });
    };

    @Override
    protected View onCreateContent(ViewStub contentStub) {
        contentStub.setLayoutResource(R.layout.game_activity);
        View view = contentStub.inflate();

        mStateController = (TextView) view.findViewById(R.id.start);
        mStepsView = view.findViewById(R.id.steps);
        mStepsText = (TextView) view.findViewById(R.id.steps_value);
        mTimeView = view.findViewById(R.id.time);
        mTimeText = (TextView) view.findViewById(R.id.time_value);
        mMiniPreview = (ImageView) view.findViewById(R.id.mini_preview);

        mGameWindow = (FrameLayout) view.findViewById(R.id.game_window);
        mShinkDrawable = mGameWindow.getBackground();
//        mPreview = (ImageView) mGameWindow.findViewById(R.id.preview);
        mGamePanel = (AbsoluteLayout) mGameWindow.findViewById(R.id.game_panel);

        setActionEnabled(false);

        mGame = new Game(this);

        mGameId = getIntent().getLongExtra(Constants.EXTRA_GAME_ID, -1);
        if (mGameId == -1) {
            throw new IllegalArgumentException("Game id should be provider!");
        }

        initGameSize(getIntent());
        initGameImage(getIntent());

        mGame.init();
        mGame.ready();

        mStateController.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                State currentState = mGame.getCurrentState();
                if (currentState.equals(State.PLAYING)) {
                    mGame.pause();
                } else if (currentState.equals(State.PAUSE)) {
                    mGame.resume();
                } else {
                    mGame.start();
                    setActionEnabled(true);
                    // First start, load the ad.
                    mInterstitialAd.loadInterstitialAd();
                }
            }
        });

//        AdManager.init(this);

        return view;
    }

    /**
     * According to device screen size, calculate for game size.
     */
    private void initGameSize(Intent intent) {
        mGameLevel = getIntent().getIntExtra(Constants.EXTRA_GAME_LEVEL, Constants.MIN_GAME_LEVEL);
        int square = Utils.level2Square(mGameLevel);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int height = dm.heightPixels;
        int width = dm.widthPixels;
        if (width > height) {
            int temp = width;
            width = height;
            height = temp;
        }
        width = width * 8 / 10;
        mGame.initPreviewAndPanelSize(width / square * square, width / square * square);
        mGame.changeSize(square);
    }

    private void initGameImage(Intent intent) {
        Bitmap bitmap = null;
        mGameImage = intent.getStringExtra(Constants.EXTRA_GAME_IMAGE);

        if (mGameImage != null) {
            bitmap = loadImageFromAsset(mGameImage);
        }

        if (bitmap == null) {
            throw new IllegalArgumentException("Please at least provide image name!");
        }

        mGame.changeImage(bitmap);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGame.getCurrentState().equals(State.PLAYING)) {
            mGame.pause();
        }
        if (mSuccessDialogPending) {
            // Stop shinking and show success dialog when onResume.
            mUpdateHandler.removeMessages(UPDATE_SHINK_STATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSuccessDialogPending) {
            showSuccessDialog();
        }
    }

    @Override
    protected int getActionImage() {
        return R.drawable.ic_refresh;
    }

    @Override
    protected boolean isActionNeedPrompt() {
        State currentState = mGame.getCurrentState();
        return (mGame.getStepsSinceStarted() != 0
                && (currentState.equals(State.PLAYING) || currentState
                        .equals(State.PAUSE)));
    }

    @Override
    protected void beforeActionPromptDialogShow() {
        super.beforeActionPromptDialogShow();
        mPausedBeforeActionPrompt = mGame.getCurrentState().equals(State.PAUSE);
        if (!mPausedBeforeActionPrompt) {
            mGame.fastPause();
        }
    }

    @Override
    protected int messageOfActionPrompt() {
        return R.string.sure_to_restart;
    }

    /**
     * Restart the game.
     * <p>
     * Puzzle will be disordered, and time and steps be reset.
     */
    @Override
    protected void onAction() {
        mGame.restart();
    }

    @Override
    protected void onActionPromptDialogDismiss() {
        super.onActionPromptDialogDismiss();
        if (mGame.getCurrentState().equals(State.PAUSE) && !mPausedBeforeActionPrompt) {
            mGame.fastResume();
        }
    }

    @Override
    protected int getActionDescription() {
        return R.string.action_restart;
    }

    private Bitmap loadImageFromAsset(String name) {
        try {
            return BitmapFactory.decodeStream(getAssets().open(name+".jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onClick(View v) {
        if (!mGame.getCurrentState().equals(State.PLAYING)) {
            Log.w(TAG, "Click is not allowed when the game is not started.");
            return;
        }

        if ((v instanceof NodeView) && mGame.isNearByLuckySlot((NodeView) v)) {

            // Update game steps.
            mGame.increseSteps();

            // Get the author and target.
            NodeView anchor = (NodeView) v;
            NodeView target = mNodeVector[mGame.getLuckySlotPosition()];

            // Exchange content and image between anchor and target.
            NodeView.exchangeContent(anchor, target);

            mGame.setExitNodePosition(anchor.getNode().getSlotPosition());

            mGame.checkSuccess();

            anchor.invalidate();
            target.invalidate();
        }
    }

    private void showSuccessDialog() {
        mSuccessDialogPending = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.success_dialog_title);

        View view = getLayoutInflater().inflate(
                R.layout.success_dialog, null, false);

        ((TextView) view.findViewById(R.id.score_value)).setText(String.valueOf(mHistoryData.score()));
        ((TextView) view.findViewById(R.id.time_value)).setText(Utils.long2Time(mHistoryData.time()));
        ((TextView) view.findViewById(R.id.steps_value)).setText(String.valueOf(mHistoryData.steps()));

        ImageView star = (ImageView) view.findViewById(R.id.success_dialog_star);
        star.setImageLevel(Utils.score2Star(mHistoryData.score()));

        view.findViewById(R.id.success_dialog_view_recents).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewRecents(mGameId);
                        mSuccessDialog.dismiss();
                    }
                });
        view.findViewById(R.id.success_dialog_view_bests).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewBests(mGameId);
                        mSuccessDialog.dismiss();
                    }
                });
        view.findViewById(R.id.success_dialog_view_ranks).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSuccessDialog.dismiss();
                        viewRanks(mHistoryData);
                    }
                });

        builder.setView(view);

        builder.setPositiveButton(R.string.success_dialog_play_again, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mGame.restart();
            }
        });

        builder.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        mSuccessDialog = builder.create();
        mSuccessDialog.setOnDismissListener(new OnDismissListener(){

            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.i(TAG, "success dialog dismissing, and activity will finish...");
                // If game not restarted, than finish itself.
                if (!mGame.getCurrentState().equals(State.PLAYING)) {
                    finish();
                }
            }});
        mSuccessDialog.show();
    }

    private void showPreview() {
//        mPreview.setVisibility(View.VISIBLE);
//        mGamePanel.setVisibility(View.INVISIBLE);
        setAllSlotsEnabled(false);
    }

    private void showGamePanel() {
//        mPreview.setVisibility(View.INVISIBLE);
//        mGamePanel.setVisibility(View.VISIBLE);
        setAllSlotsEnabled(true);
    }

    private void setAllSlotsEnabled (boolean enabled) {
        int count = mGamePanel.getChildCount();
        for (int i = 0; i < count; i++) {
            mGamePanel.getChildAt(i).setEnabled(enabled);
        }
    }

    private void viewRecents(long id) {
        Intent intent = new Intent(this, HistoryListActivity.class);
        intent.putExtra(Constants.EXTRA_GAME_ID, id);
        intent.putExtra(Constants.EXTRA_HISTORY_TYPE, HistoryListActivity.HISTORY_TYPE_RECENTS);
        startActivity(intent);
    }

    private void viewBests(long id) {
        Intent intent = new Intent(this, HistoryListActivity.class);
        intent.putExtra(Constants.EXTRA_GAME_ID, id);
        intent.putExtra(Constants.EXTRA_HISTORY_TYPE, HistoryListActivity.HISTORY_TYPE_BESTS);
        startActivity(intent);
    }

    private void viewRanks(HistoryData historyData) {
        Intent intent = new Intent(this, RankListActivity.class);
        intent.putExtra(Constants.EXTRA_GAME_ID, mGameId);
        intent.putExtra(Constants.EXTRA_HISTORY_DATA, historyData);
        startActivity(intent);
    }

    @Override
    public void onPreviewChanged(Bitmap preview) {
//        mPreview.setImageBitmap(preview);
        mMiniPreview.setImageBitmap(preview);
    }

    @Override
    public void onNodesCreated(Node[] nodes, int size, int gridSize) {
        mGamePanel.removeAllViews();
        mNodeVector = new NodeView[size * size];

        int position;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                position = x + y * size;
                mNodeVector[position] = new NodeView(this);
                mNodeVector[position].bindNode(nodes[position]);
                mNodeVector[position].setOnClickListener(this);
                // Add node to layout
                mGamePanel.addView(mNodeVector[position],
                        new AbsoluteLayout.LayoutParams(gridSize, gridSize,
                                (gridSize) * x, (gridSize) * y));
            }
        }
    }

    @Override
    public void onStepsChanged(int steps) {
        mStepsText.setText(String.valueOf(steps));
    }

    @Override
    public void onNodesDisordered() {
        mGamePanel.invalidate();
    }

    @Override
    public void onGameReady() {
        mTimeText.setText(mGame.getTimeStringSinceStarted());

        setAllSlotsEnabled(false);
    }

    @Override
    public void onGameStart() {
        // Start to count time.
        mUpdateHandler.sendEmptyMessage(UPDATE_TIME);
        mShinkTimes = 0;

        mStateController.setText(R.string.pause);

        // Hide preview.
        showGamePanel();
    }

    @Override
    public void onGamePause() {
        // Not update the time.
        mUpdateHandler.removeMessages(UPDATE_TIME);

        mStateController.setText(R.string.resume);

        // Show preview.
        showPreview();

        // Show ad.
        if (mInterstitialAd.isInterstitialAdReady()) {
            mInterstitialAd.showInterstitialAd(this);
        } else {
            mInterstitialAd.loadInterstitialAd();
        }
    }

    @Override
    public void onGameFastPause() {
        // Not update the time.
        mUpdateHandler.removeMessages(UPDATE_TIME);
    }

    @Override
    public void onGameResume() {
        // Continue to count time.
        mUpdateHandler.sendEmptyMessage(UPDATE_TIME);

        mStateController.setText(R.string.pause);

        // Hide preview.
        showGamePanel();
    }

    @Override
    public void onGameFastResume() {
        // Continue to count time.
        mUpdateHandler.sendEmptyMessage(UPDATE_TIME);
    }

    @Override
    public void onGameSuccess() {
        // Show preview.
        showPreview();

        long date = System.currentTimeMillis();
        long time = mGame.getTimeSinceStarted();

        // Update time and remove pending updating.
        mTimeText.setText(Utils.long2Time(time));
        mUpdateHandler.removeMessages(UPDATE_TIME);

        // Record result.
        int steps = mGame.getStepsSinceStarted();
        int score = Utils.timeSteps2Score(mGameLevel, time, steps);

        mHistoryData = new HistoryData(mGameId, date, time, steps, score);
        mUpdateHandler.post(new Runnable() {
            
            @Override
            public void run() {
                GamesRecorder.recordAchievement(GameActivity.this, mHistoryData);
                
            }
        });

        // Shink first to celebrate, when shink end, then show success dialog.
        mSuccessDialogPending = true;
        mShinkDrawable.setLevel(2);
        mShinkTimes++;
        mUpdateHandler.sendEmptyMessageDelayed(UPDATE_SHINK_STATE, SHINK_INTERVAL);
    }
//    /**
//     * Create a bitmap base on the image.
//     * 
//     * @param imageUri uri of image
//     * @return the decoded bitmap, null if fail
//     */
//    private Bitmap loadImageFromUri(Uri imageUri) {
//        InputStream is;
//        Bitmap image = null;
//        try {
//            is = getContentResolver().openInputStream(imageUri);
//            image = BitmapFactory.decodeStream(is);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        return image;
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//
//            Uri imageUri = data.getData();
//            if (imageUri == null) {
//                Log.e(TAG, "Uri returned is null!");
//                return;
//            }
//
//            Bitmap oriImage = loadImageFromUri(imageUri);
//            if (oriImage != null) {
//                mGame.changeImage(oriImage);
//
//                mGame.ready();
//            } else {
//                //error
//            }
//        }
//    }

}
