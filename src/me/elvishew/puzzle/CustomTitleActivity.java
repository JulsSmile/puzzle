package me.elvishew.puzzle;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.widget.TextView;

public abstract class CustomTitleActivity extends FragmentActivity {

    private ViewStub mCustomTitleStub;
    private ViewStub mContentStub;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        beforeCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.custom_title_activity);

        mCustomTitleStub = (ViewStub) findViewById(R.id.custom_title_stub);
        View customTitle = onCreateCustomTitle(mCustomTitleStub);
        onCustomTitleCreated(customTitle);

        mContentStub = (ViewStub) findViewById(R.id.content_stub);
        View content = onCreateContent(mContentStub);
        onContentCreated(content);

        afterCreate(savedInstanceState);
    }

    protected void beforeCreate(Bundle savedInstanceState) {}

    protected abstract View onCreateCustomTitle(ViewStub customTitleStub);

    protected void onCustomTitleCreated(View customTitle) {
        TextView title = (TextView) customTitle.findViewById(R.id.title);
        title.setText(getTitle());
    }

    protected abstract View onCreateContent(ViewStub contentStub);

    protected void onContentCreated(View content) {}

    protected void afterCreate(Bundle savedInstanceState) {}

}