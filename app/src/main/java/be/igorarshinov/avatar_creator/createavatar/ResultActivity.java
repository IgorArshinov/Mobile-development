package be.igorarshinov.avatar_creator.createavatar;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;

import be.igorarshinov.avatar_creator.AvatarListActivity;
import be.igorarshinov.avatar_creator.R;
import be.igorarshinov.avatar_creator.data.AppCache;
import be.igorarshinov.avatar_creator.data.AvatarContract;
import be.igorarshinov.avatar_creator.model.Avatar;

@SuppressWarnings("UnusedParameters")
public class ResultActivity extends AppCompatActivity {

    private final String LOGTAG = "ResultActivity";

    Avatar newAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        newAvatar = getIntent().getParcelableExtra("Avatar");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button createButton = findViewById(R.id.create_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AppCache appCache = AppCache.getInstance();
                newAvatar.setImage((Bitmap) appCache.get("temp"));

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                newAvatar.getImage().compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte imageInByte[] = stream.toByteArray();

                ContentValues contentValues = new ContentValues();

                contentValues.put(AvatarContract.AvatarEntry.COLUMN_NAME, newAvatar.getName());
                contentValues.put(AvatarContract.AvatarEntry.COLUMN_IMAGE, imageInByte);

                Uri uri = getContentResolver().insert(AvatarContract.AvatarEntry.CONTENT_URI, contentValues);

                Intent intent = new Intent(ResultActivity.this, AvatarListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent();
        intent.putExtra("Avatar", newAvatar);

        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}