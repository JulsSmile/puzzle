package me.elvishew.puzzle;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.elvishew.puzzle.UpdateManager.UpdateCheckingListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
//import android.widget.RadioGroup;
//import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class SettingsActivity extends BackOnlyActivity implements AdapterView.OnItemClickListener, DialogHost, UpdateCheckingListener {

    private static final int SETTING_ID_SKILL = 0;
    private static final int SETTING_ID_NICKNAME = 1;
    private static final int SETTING_ID_FEEDBACK = 2;
    private static final int SETTING_ID_UPDATE = 3;
    private static final int SETTING_ID_ABOUT = 4;

    private ListView mSettingsList;

    private SettingsAdapter mAdapter;

    // Nickname.
    private Pattern mNickNamePattern = Pattern.compile("[a-z0-9A-Z_]+");
    private AlertDialog mNickNameDialog;
    private Button mNickNamePositiveBtn;

    // Update.
    private Dialog mNoConnectionDialog;
    private UpdateManager mUpdateManager;

    @Override
    protected View onCreateContent(ViewStub contentStub) {
        contentStub.setLayoutResource(R.layout.settings_activity);
        View content = contentStub.inflate();
        return content;
    }

    @Override
    protected void onContentCreated(View content) {
        super.onContentCreated(content);
        mSettingsList = (ListView) content.findViewById(R.id.settings_list);

        List<SettingItem> settings = new ArrayList<SettingItem>();
//        settings.add(new SettingItem(SETTING_ID_SKILL, R.string.setting_skill,
//                R.string.setting_description_skill, true, false));
        settings.add(new SettingItem(SETTING_ID_NICKNAME, R.string.setting_nickname,
                R.string.setting_description_nickname, false, false));
        settings.add(new SettingItem(SETTING_ID_FEEDBACK, R.string.setting_feedback,
                R.string.setting_description_feedback, true, false));
        settings.add(new SettingItem(SETTING_ID_UPDATE, R.string.setting_update,
                R.string.setting_description_update, false, false));
        settings.add(new SettingItem(SETTING_ID_ABOUT, R.string.setting_about,
                R.string.setting_description_about, false, false));
        mAdapter = new SettingsAdapter();
        mAdapter.setItems(settings);
        mSettingsList.setAdapter(mAdapter);
        mSettingsList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        SettingItem setting = mAdapter.getItem(arg2);
        Log.i("--------------", "onItemClick arg2 " + arg2 + " enabled?" + setting.enabled);
        if (!setting.enabled) {
            return;
        }

        switch (setting.settingId) {
        case SETTING_ID_SKILL:
//            showSkillDialog();
            break;
        case SETTING_ID_NICKNAME:
            showNicknameDialog();
            break;
        case SETTING_ID_FEEDBACK:
            Intent intent = new Intent(this, FeedbackActivity.class);
            startActivity(intent);
            break;
        case SETTING_ID_UPDATE:
            if (HttpHelper.isNetworkAvailable(this)) {
                mUpdateManager = new UpdateManager(this, this);
                mUpdateManager.checkUpdate();
            } else {
                    HttpHelper
                            .showNoConnectionDialog(this,
                                    this, 0);
            }
            break;
        case SETTING_ID_ABOUT:
            showAboutDialog();
            break;
        default:
            break;
        }
    };

    private void showNicknameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.nickname_title);

        View view = getLayoutInflater().inflate(
                R.layout.nickname_dialog, null, false);
        final TextView warning = (TextView) view
                .findViewById(R.id.nickname_warning);
        final EditText content = (EditText) view
                .findViewById(R.id.nickname_content);
        final PreferenceHelper ph = new PreferenceHelper(SettingsActivity.this);
        content.setText(ph.getPlayerName());
        content.setSelection(content.getText().length());

        content.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if (text != null) {
                    text.trim();
                }
                if (TextUtils.isEmpty(text)) {
                    warning.setText(R.string.nickname_empty);
                    warning.setVisibility(View.VISIBLE);
                    mNickNamePositiveBtn.setEnabled(false);
                } else {
                    Matcher matcher = mNickNamePattern.matcher(text);
                    if (!matcher.matches()) {
                        warning.setText(R.string.nickname_illegal);
                        warning.setVisibility(View.VISIBLE);
                        mNickNamePositiveBtn.setEnabled(false);
                    } else {
                        warning.setVisibility(View.GONE);
                        mNickNamePositiveBtn.setEnabled(true);
                    }
                }
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        builder.setView(view);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nickname = content.getText().toString();
                Matcher matcher = mNickNamePattern.matcher(nickname);
                if (matcher.matches()) {
                    ph.setPlayerName(nickname);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        mNickNameDialog = builder.show();
        mNickNamePositiveBtn = (Button) mNickNameDialog.getWindow().findViewById(android.R.id.button1);
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setIcon(R.drawable.icon);
        builder.setTitle(R.string.app_name);

        View view = getLayoutInflater().inflate(
                R.layout.about_dialog, null, false);
//
//        // Setup views.
//        RadioGroup group = (RadioGroup) view.findViewById(R.id.radioGroup1);
//        final View version = view.findViewById(R.id.about_version);
//        final View updateInfo = view.findViewById(R.id.about_update_info);
//        group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                Log.i(".....", "check id " + checkedId);
//                if (checkedId == R.id.about_version_radio) {
//                    version.setVisibility(View.VISIBLE);
//                    updateInfo.setVisibility(View.INVISIBLE);
//                } else {
//                    version.setVisibility(View.INVISIBLE);
//                    updateInfo.setVisibility(View.VISIBLE);
//                }
//            }
//        });

        builder.setView(view);
        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    @Override
    public void onStartChecking() {
        setUpdateItemEnabled(false);
    }

    private void setUpdateItemEnabled(boolean enabled) {
        for (SettingItem item : mAdapter.mItems) {
            if (item.settingId == SETTING_ID_UPDATE) {
                item.enabled = enabled;
                item.hasProgress = !enabled;
                mAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    @Override
    public void onCheckingFinish(boolean hasNewVersion) {
        setUpdateItemEnabled(true);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUpdateManager != null) {
            mUpdateManager.cancelCheckUpdate();
            mUpdateManager = null;
        }
    }

    // Implements DialogHost just for checking the connection.
    @Override
    public Dialog getDialogById(int dialogId) {
        return mNoConnectionDialog;
    }

    @Override
    public void setDialogById(int dialogId, Dialog dialog) {
        mNoConnectionDialog = dialog;
    }

    @Override
    public void showDialogById(int dialogId) {
        mNoConnectionDialog.show();
    }

    @Override
    public void dismissDialogById(int dialogId) {
        mNoConnectionDialog.dismiss();
    }

    @Override
    public void setOnDialogDissmissListener(int dialogId,
            OnDismissListener listener) {
        mNoConnectionDialog.setOnDismissListener(listener);
    }

    private class SettingItem {
        int settingId;
        int primaryText;
        int secondaryText;
        boolean enabled = true;
        boolean hasIndicate;
        boolean hasProgress;

        public SettingItem(int settingId, int primaryText, int secondaryText,
                boolean hasIndicate, boolean hasProgress) {
            this.settingId = settingId;
            this.primaryText = primaryText;
            this.secondaryText = secondaryText;
            this.hasIndicate = hasIndicate;
            this.hasProgress = hasProgress;
        }
    }

    private class SettingsAdapter extends BaseAdapter {

        List<SettingItem> mItems;

        LayoutInflater mLayoutInflater;

        public void setItems(List<SettingItem> items) {
            mItems = items;
        }

        @Override
        public int getCount() {
            return mItems == null ? 0 : mItems.size();
        }

        @Override
        public SettingItem getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (mLayoutInflater == null) {
                mLayoutInflater = getLayoutInflater();
            }

            SettingItem item = getItem(position);
            View view = mLayoutInflater.inflate(R.layout.setting_item, parent, false);
            ((TextView) view.findViewById(R.id.setting_item_primary_text))
                    .setText(item.primaryText);
            ((TextView) view.findViewById(R.id.setting_item_secondary_text))
                    .setText(item.secondaryText);
            ImageView indicateNext = (ImageView) view.findViewById(R.id.setting_item_indicate_next);
            ProgressBar indicateProgress = (ProgressBar) view.findViewById(R.id.setting_item_indicate_progress);
            if (item.hasIndicate) {
                indicateNext.setVisibility(View.VISIBLE);
            } else {
                indicateNext.setVisibility(View.INVISIBLE);
            }
            if (item.hasProgress) {
                indicateProgress.setVisibility(View.VISIBLE);
            } else {
                indicateProgress.setVisibility(View.INVISIBLE);
            }
            view.setEnabled(item.enabled);
            return view;
        }

    }
}
