package me.elvishew.puzzle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.elvishew.puzzle.HttpHelper.Response;

import android.app.Dialog;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class FeedbackActivity extends BackOnlyActivity implements DialogHost {

    private EditText mContent;
    private EditText mContact;
    private Spinner mFeedbackTypeSpinner;
    private Button mClearContent;
    private Button mSubmit;

    private Pattern mContentPattern = Pattern.compile("[a-zA-Z0-9_\u4e00-\u9fa5]+");
    private Pattern mContactPattern = Pattern.compile("[\\w.。,，:：!！?？\\-@\\s]*");

    private Dialog mNoConnectionDialog;

    private int mFeedbackType;

    private static final int FEEDBACK_TYPE_DEFAULT_TYPE = 0;
//    private static final int FEEDBACK_TYPE_FUNCTION_SUGGESTION = 1;
//    private static final int FEEDBACK_TYPE_UI_SUGGESTION = 2;
//    private static final int FEEDBACK_TYPE_BUG_REPORTING = 3;
//    private static final int FEEDBACK_TYPE_DEVICE_ADAPTION = 4;

    @Override
    protected View onCreateContent(ViewStub contentStub) {
        contentStub.setLayoutResource(R.layout.feedback_activity);
        return contentStub.inflate();
    }

    @Override
    protected void onContentCreated(View content) {
        super.onContentCreated(content);
        mContent = (EditText) content.findViewById(R.id.feedback_content);
        mContent.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mClearContent.setEnabled(!TextUtils.isEmpty(s));
                String content = null;
                if (s != null) {
                    content = s.toString().trim();
                }
                mSubmit.setEnabled(!TextUtils.isEmpty(content));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mContact = (EditText) content.findViewById(R.id.feedback_contact);
        mFeedbackTypeSpinner = (Spinner) content.findViewById(R.id.feedback_type);
        mFeedbackTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                mFeedbackType = arg2;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                mFeedbackType = FEEDBACK_TYPE_DEFAULT_TYPE;
            }
        });
        mClearContent = (Button) content.findViewById(R.id.feedback_clear_content);
        mClearContent.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mContent.setText("");
            }
        });
        mSubmit = (Button) content.findViewById(R.id.feedback_submit);
        mSubmit.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // Check every values first, MUST.
                String _content = filterString(mContent.getText().toString(), mContentPattern);
                if (TextUtils.isEmpty(_content)) {
                    Toast.makeText(FeedbackActivity.this,
                            R.string.feedback_no_effective_content,
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (HttpHelper.isNetworkAvailable(FeedbackActivity.this)) {
                        String _contact = filterString(mContact.getText()
                                .toString(), mContactPattern);
                        submit(_content, _contact, getFeedbackType());

                    } else {
                        HttpHelper
                                .showNoConnectionDialog(FeedbackActivity.this,
                                        FeedbackActivity.this, 0);
                    }
                }
            }
        });
    }

    private int getFeedbackType() {
        return mFeedbackType;
    }

    private void submit(final String content, final String contact, final int type) {
        new AsyncTask<Void, Integer, Integer>() {

            @Override
            protected Integer doInBackground(Void... params) {
                Log.i("---------", "content:"+content + "\ncontact:" +contact);
                return HttpHelper.submitFeedback(content, contact, type);
            }

            @Override
            protected void onPostExecute(Integer result) {
                super.onPostExecute(result);
                if (result < Response.ERROR_NOERROR) {
                    // Now, rank is an errorCode.
                    // Post a toast to user friendly.
                    Toast.makeText(FeedbackActivity.this,
                            HttpHelper.toastMsgOf(result), Toast.LENGTH_SHORT).show();
                } else {
                    // Success.
                    Toast.makeText(FeedbackActivity.this,
                            R.string.feedback_submit_success,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }.execute((Void)(null));
    }

    private String filterString(String input, Pattern pattern) {
        String output = "";
        if (input != null) {
            output = input.trim();
            if (output.length() > 0) {
                Matcher matcher = pattern.matcher(output);
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    sb.append(matcher.group()).append(' ');
                }
                output = sb.toString();
            }
        }
        return output.trim();
    }

    @Override
    public Dialog getDialogById(int dialogId) {
        return mNoConnectionDialog;
    }

    @Override
    public void setDialogById(int dialogId, Dialog dialog) {
        mNoConnectionDialog = dialog;
    }

    @Override
    public void setOnDialogDissmissListener(int dialogId,
            OnDismissListener listener) {
        mNoConnectionDialog.setOnDismissListener(listener);
    }

    @Override
    public void showDialogById(int dialogId) {
        mNoConnectionDialog.show();
    }

    @Override
    public void dismissDialogById(int dialogId) {
        mNoConnectionDialog.dismiss();
    }
}
