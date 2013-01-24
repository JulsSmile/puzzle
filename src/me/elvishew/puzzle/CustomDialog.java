package me.elvishew.puzzle;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link CustomAlertBuilder.show()} must be called instead of AlertDialog's show();
 * @author Administrator
 *
 */
public class CustomDialog extends Dialog {

    private View mParentPanel;

    private View mTopPanel;
    private View mContentPanel;
    private View mCustomPanel;
    private View mButtonPanel;

    private FrameLayout mCustom;

    private ImageView mIcon;
    private TextView mTitle;
    private View mTitleDivider;
    private TextView mMessage;
    private Button mPositive;
    private Button mNegative;
    private Button mNeutral;

    public CustomDialog(Context context) {
        super(new ContextThemeWrapper(context, android.R.style.Theme_Light_NoTitleBar));
        getWindow().getDecorView().setBackgroundDrawable(null);
        setCanceledOnTouchOutside(true);

        mParentPanel = LayoutInflater.from(context).inflate(R.layout.custom_alert_dialog, null, false);

        mTopPanel = mParentPanel.findViewById(R.id.topPanel);
        mContentPanel = mParentPanel.findViewById(R.id.contentPanel);
        mCustomPanel = mParentPanel.findViewById(R.id.customPanel);
        mButtonPanel = mParentPanel.findViewById(R.id.buttonPanel);

        mCustom = (FrameLayout) mCustomPanel.findViewById(R.id.custom);

        mIcon = (ImageView) mTopPanel.findViewById(R.id.icon);
        mTitle = (TextView) mTopPanel.findViewById(R.id.alertTitle);
        mTitleDivider = mTopPanel.findViewById(R.id.titleDivider);
        mMessage = (TextView) mContentPanel.findViewById(R.id.message);
        mPositive = (Button) mButtonPanel.findViewById(R.id.button1);
        mNegative = (Button) mButtonPanel.findViewById(R.id.button2);
        mNeutral = (Button) mButtonPanel.findViewById(R.id.button3);
    }

    public void setIcon(int iconId) {
        mIcon.setImageResource(iconId);
        if (mTopPanel.getVisibility() != View.VISIBLE) {
            mTopPanel.setVisibility(View.VISIBLE);
        }
    }

    public void setMessage(int messageId) {
        mMessage.setText(messageId);
        if (mContentPanel.getVisibility() != View.VISIBLE) {
            mContentPanel.setVisibility(View.VISIBLE);
        }
    }

    public void setNegativeButton(int textId, final OnClickListener listener) {
        mNegative.setText(textId);
        mNegative.setVisibility(View.VISIBLE);
        mNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onClick(null, 0);
            }
        });
        if (mButtonPanel.getVisibility() != View.VISIBLE) {
            mButtonPanel.setVisibility(View.VISIBLE);
        }
    }

    public void setNeutralButton(int textId, final OnClickListener listener) {
        mNeutral.setText(textId);
        mNeutral.setVisibility(View.VISIBLE);
        mNeutral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onClick(null, 0);
            }
        });
        if (mButtonPanel.getVisibility() != View.VISIBLE) {
            mButtonPanel.setVisibility(View.VISIBLE);
        }
    }

    public void setPositiveButton(int textId, final OnClickListener listener) {
        mPositive.setText(textId);
        mPositive.setVisibility(View.VISIBLE);
        mPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onClick(null, 0);
            }
        });
        if (mButtonPanel.getVisibility() != View.VISIBLE) {
            mButtonPanel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setTitle(int titleId) {
        mTitle.setText(titleId);
        if (mTopPanel.getVisibility() != View.VISIBLE) {
            mTopPanel.setVisibility(View.VISIBLE);
            mTitleDivider.setVisibility(View.VISIBLE);
        }
    }

    public void setView(View view) {
        mCustom.removeAllViews();
        mCustom.addView(view);
        if (mCustomPanel.getVisibility() != View.VISIBLE) {
            mCustomPanel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void show() {
        setContentView(mParentPanel);
        super.show();
    }

}
