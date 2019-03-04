package be.igorarshinov.avatar_creator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import be.igorarshinov.avatar_creator.data.AppCache;
import be.igorarshinov.avatar_creator.model.Avatar;

public class AvatarDetailsActivity extends AppCompatActivity implements
        AvatarDetailsFragment.OnScreenOrientationListener {
    private final String LOGTAG = "AvatarDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Avatar currentAvatar = this.getIntent().getParcelableExtra("Avatar");

        setContentView(R.layout.activity_avatar_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {

            onBackPressed();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void getCurrentSelectedAvatarForList(Avatar currentSelectedAvatar) {

        Intent intent = new Intent(AvatarDetailsActivity.this, AvatarListActivity.class);

        AppCache appCache = AppCache.getInstance();

        appCache.put("temp", currentSelectedAvatar.getImage());

        intent.putExtra("Avatar", currentSelectedAvatar);
        startActivity(intent);
    }
}
