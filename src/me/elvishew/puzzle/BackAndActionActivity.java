package me.elvishew.puzzle;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.Toast;

public abstract class BackAndActionActivity extends CustomTitleActivity {

    protected static final int INVALID_RES_ID = -1;
    private ImageView mActionView;

    private AlertDialog mActionPromptDialog;

    @Override
    protected View onCreateCustomTitle(ViewStub customTitleStub) {
        customTitleStub.setLayoutResource(R.layout.back_and_action_activity_title);
        View customTitleBar = customTitleStub.inflate();

        customTitleBar.findViewById(R.id.back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });

        mActionView = (ImageView) customTitleBar.findViewById(R.id.action);

        int imageRes = getActionImage();
        if (imageRes != INVALID_RES_ID) {
            mActionView.setImageResource(imageRes);
        }

        mActionView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isActionNeedPrompt()) {
                    beforeActionPromptDialogShow();
                    showActionPromptDialog();
                } else {
                    onAction();
                }
            }
        });

        mActionView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return onActionLongClick();
            }
        });
        return customTitleBar;
    }

    protected void beforeActionPromptDialogShow() {}

    private void showActionPromptDialog() {
        // Prompt a dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.tip);
        builder.setMessage(messageOfActionPrompt());
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onAction();
                    }
                });

        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        mActionPromptDialog = builder.create();
        mActionPromptDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                onActionPromptDialogDismiss();
            }});
        mActionPromptDialog.show();
    }

    protected void onBack() {
        finish();
    }

    protected void setActionEnabled(boolean enabled) {
        mActionView.setEnabled(enabled);
    }

    protected int getActionImage() {
        return INVALID_RES_ID;
    }

    /**
     * Whether need to prompt a dialog before action taking place.
     * 
     * @return true if necessary, false otherwise
     */
    protected boolean isActionNeedPrompt() {
        return false;
    }

    /**
     * Return the message of action prompt dialog.
     * 
     * @return the message string id
     */
    protected int messageOfActionPrompt() {
        return R.string.action_prompt_default_msg;
    }

    protected void onAction() {}

    protected void onActionPromptDialogDismiss() {}

    protected boolean onActionLongClick() {
        Toast toast = Toast.makeText(this, getActionDescription(), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 0);
        toast.show();
        return true;
    }

    protected abstract int getActionDescription();
}