package me.elvishew.puzzle;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;

public abstract class BackOnlyActivity extends CustomTitleActivity {

    @Override
    protected View onCreateCustomTitle(ViewStub customTitleStub) {
        customTitleStub.setLayoutResource(R.layout.back_only_activity_title);
        View customTitleBar = customTitleStub.inflate();

        customTitleBar.findViewById(R.id.back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });

        return customTitleBar;
    }

    protected void onBack() {
        finish();
    }
}
