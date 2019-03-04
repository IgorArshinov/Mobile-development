package be.igorarshinov.avatar_creator.createavatar;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import be.igorarshinov.avatar_creator.BuildConfig;
import be.igorarshinov.avatar_creator.R;
import be.igorarshinov.avatar_creator.data.AppCache;
import be.igorarshinov.avatar_creator.model.Avatar;
import be.igorarshinov.avatar_creator.utils.ImageUtils;

public class FaceSwapperActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int BACK_PRESSED_REQUEST_CODE = 300;
    private static final int RESULT_LOAD_IMAGE = 200;
    private static final int CAMERA_API_LEVEL_LIMIT = 24;
    private static final String PHOTO_FILENAME = "photo.jpg";
    private final String LOGTAG = "FaceSwapperActivity";
    private static final String TAG_SAVE_BITMAP_A = "SaveBitmapA";
    private static final String TAG_SAVE_BITMAP_B = "SaveBitmapB";

    private Button swapFacesButton;

    private FaceSwap faceSwap;

    private SectionsPagerAdapter sectionsPagerAdapter;

    private Bitmap bitmap1;

    private Bitmap bitmap2;

    private TabLayout tabLayout;

    private boolean image1Changed;

    private boolean image2Changed;

    private Toast infoToast;

    private ViewPager viewPager;

    private Uri saveUriBitmapA;

    private Uri saveUriBitmapB;
    private Avatar newAvatar;
    private AvatarInputFieldFragment avatarInputFieldFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swap_faces);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        if (getResources().getBoolean(R.bool.portrait_only))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        bitmap1 = null;
        bitmap2 = null;
        image1Changed = false;
        image2Changed = false;
        saveUriBitmapA = null;
        saveUriBitmapB = null;
        editBitmap = null;
        faceSwap = new FaceSwap(getApplicationContext());

        swapFacesButton = findViewById(R.id.swap_faces_button);

        avatarInputFieldFragment = (AvatarInputFieldFragment) getSupportFragmentManager().findFragmentById(R.id.avatar_details_fragment);
        avatarInputFieldFragment.setImageViewEnabled(false);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) fetchImage(intent);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                fetchMultipleImages(intent);
            }
        }

        setupTabs();

        checkPermissions();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_close_white_24);
    }

    private void setupTabs() {

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.baseline_face_black_24, null));

        tabLayout.getTabAt(1).setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.baseline_face_black_24, null));

        tabLayout.addOnTabSelectedListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (saveUriBitmapA != null && bitmap1 != null) {

            outState.putString(TAG_SAVE_BITMAP_A, saveUriBitmapA.toString());
        }

        if (saveUriBitmapB != null && bitmap2 != null) {

            outState.putString(TAG_SAVE_BITMAP_B, saveUriBitmapB.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String strUriBitmapA = savedInstanceState.getString(TAG_SAVE_BITMAP_A);
        String strUriBitmapB = savedInstanceState.getString(TAG_SAVE_BITMAP_B);

        if (strUriBitmapA != null) {
            saveUriBitmapA = Uri.parse(strUriBitmapA);
            Bitmap bmp1 = null;
            try {
                bmp1 = MediaStore.Images.Media.getBitmap(getContentResolver(), saveUriBitmapA);
            } catch (IOException e) {
                Log.e(LOGTAG, e.getMessage());
            }

            if (bmp1 != null) {
                bitmap1 = bmp1;
                if (tabLayout.getSelectedTabPosition() == 0) {
                    setBitmap(bitmap1, saveUriBitmapA);
                }
            }
        }

        if (strUriBitmapB != null) {
            saveUriBitmapB = Uri.parse(strUriBitmapB);
            Bitmap bmp2 = null;
            try {
                bmp2 = MediaStore.Images.Media.getBitmap(getContentResolver(), saveUriBitmapB);
            } catch (IOException e) {
                Log.e(LOGTAG, e.getMessage());
            }

            if (bmp2 != null) {
                bitmap2 = bmp2;
                if (tabLayout.getSelectedTabPosition() == 1) {
                    setBitmap(bitmap2, saveUriBitmapB);
                }
            }
        }

        if (bitmap1 != null && bitmap2 != null) {
            swapFacesButton.setEnabled(true);
        } else {
            swapFacesButton.setEnabled(false);
        }

        savedInstanceState.clear();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.d(this.getLocalClassName(), "On new intent requested");

        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                fetchImage(intent);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                fetchMultipleImages(intent);
            }
        }
    }

    private void fetchImage(Intent intent) {
        final Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {

            try {

                final Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Bitmap bmp = ImageUtils.resizeBitmap(bitmap);
                        setBitmap(bmp, imageUri);
                    }
                }, 500);
            } catch (IOException e) {
                Log.e(LOGTAG, e.getMessage());
            }
        }
    }

    private void fetchMultipleImages(Intent intent) {
        Log.d(this.getLocalClassName(), "Handling multiple images.");

        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris.size() >= 2) {

            try {
                int len = imageUris.size();
                final Uri uri1 = imageUris.get(len - 1);
                final Uri uri2 = imageUris.get(len - 2);
                final Bitmap bmp1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri1);
                final Bitmap bmp2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri2);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (bmp1 != null && bmp2 != null) {

                            bitmap1 = ImageUtils.resizeBitmap(bmp1);
                            bitmap2 = ImageUtils.resizeBitmap(bmp2);

                            saveUriBitmapA = uri1;
                            saveUriBitmapB = uri2;

                            if (tabLayout == null || sectionsPagerAdapter == null) {
                                setupTabs();
                            }

                            ((FaceSwapFragments.FaceFragmentA) sectionsPagerAdapter.getItem(0)).setImage(bitmap1);
                            ((FaceSwapFragments.FaceFragmentB) sectionsPagerAdapter.getItem(1)).setImage(bitmap2);

                            image1Changed = true;
                            image2Changed = true;

                            swapFacesButton.setEnabled(true);
                        }
                    }
                }, 500);
            } catch (IOException e) {
                Log.e(LOGTAG, e.getMessage());
            }
        }
    }

    private static final int REQUEST_INVITE = 1123;

    @SuppressWarnings("UnusedParameters")
    public void onTakePhotoButtonClick(View view) {
        if (checkPermissions()) {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (Build.VERSION.SDK_INT >= CAMERA_API_LEVEL_LIMIT) {

                File file = createImageFile();
                if (file != null) {

                    Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                }
            } else {

                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri());

                if (intent.resolveActivity(getPackageManager()) != null) {

                    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                }
            }

            if (intent.resolveActivity(getPackageManager()) != null) {

                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        } else {
            if (infoToast != null) {
                infoToast.cancel();
            }
            infoToast = showInfoToast(getString(R.string.err_permission));
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void onChoosePhotoButtonClick(View view) {
        if (checkPermissions()) {

            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(i, RESULT_LOAD_IMAGE);
        } else {
            if (infoToast != null) {
                infoToast.cancel();
            }
            infoToast = showInfoToast(getString(R.string.err_permission));
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void onSwapPhotosButtonClick(final View view) {

        newAvatar = avatarInputFieldFragment.getNewAvatar();
        final AppCache appCache = AppCache.getInstance();

        if (!image1Changed && !image2Changed && appCache.get("temp") != null) {

            Intent intent = new Intent(FaceSwapperActivity.this, ResultActivity.class);
            intent.putExtra("Avatar", newAvatar);

            startActivityForResult(intent, BACK_PRESSED_REQUEST_CODE);
        } else if (bitmap1 != null && bitmap2 != null) {

            final Bitmap[] bmps = ImageUtils.makeEqualProps(bitmap1, bitmap2);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    Bitmap swappedBitmap = null;
                    FaceSwap.fsStatus status = FaceSwap.fsStatus.FACE_NOT_FOUND_IMAGE1;
                    int rot1 = 90;
                    int rot2 = 90;
                    String errorMessage = "";

                    while (status != FaceSwap.fsStatus.FACE_SWAP_OK) {

                        try {
                            status = faceSwap.selfieSwap(bmps[0], bmps[1]);
                            swappedBitmap = faceSwap.getRes();
                        } catch (NullPointerException e) {
                            Log.e(LOGTAG, e.getMessage());
                            status = FaceSwap.fsStatus.FACE_SWAP_NOK;
                        }

                        switch (status) {
                            case FACE_NOT_FOUND_IMAGE1:

                                bmps[0] = ImageUtils.rotateBitmap(bmps[0], 90.0f);
                                Bitmap[] bms1 = ImageUtils.makeEqualProps(bmps[0], bmps[1]);
                                bmps[0] = bms1[0];
                                bmps[1] = bms1[1];

                                rot1 += 90;
                                if (rot1 == 360) {
                                    errorMessage = getString(R.string.err_could_not_swap_x) + " 1";
                                    status = FaceSwap.fsStatus.FACE_SWAP_NOK;
                                }
                                break;

                            case FACE_NOT_FOUND_IMAGE2:

                                bmps[1] = ImageUtils.rotateBitmap(bmps[1], 90.0f);
                                Bitmap[] bms2 = ImageUtils.makeEqualProps(bmps[0], bmps[1]);
                                bmps[0] = bms2[0];
                                bmps[1] = bms2[1];

                                rot2 += 90;
                                if (rot2 == 360) {
                                    errorMessage = getString(R.string.err_could_not_swap_x) + " 2";
                                    status = FaceSwap.fsStatus.FACE_SWAP_NOK;
                                }
                                break;

                            case FACE_SWAP_INSUFFICIENT_LANDMARKS_IMAGE1:
                                errorMessage = getString(R.string.err_could_not_swap_x) + " 1";
                                status = FaceSwap.fsStatus.FACE_SWAP_NOK;
                                break;

                            case FACE_SWAP_INSUFFICIENT_LANDMARKS_IMAGE2:
                                errorMessage = getString(R.string.err_could_not_swap_x) + " 2";
                                status = FaceSwap.fsStatus.FACE_SWAP_NOK;
                                break;
                        }

                        if (status == FaceSwap.fsStatus.FACE_SWAP_NOK) {
                            break;
                        }
                    }

                    if (status == FaceSwap.fsStatus.FACE_SWAP_NOK) {
                        showInfoToast(errorMessage);
                    }

                    if (status == FaceSwap.fsStatus.FACE_SWAP_OK) {

                        if (rot2 == 180) {
                            swappedBitmap = ImageUtils.rotateBitmap(swappedBitmap, 270.0f);
                        }
                        if (rot2 == 270) {
                            swappedBitmap = ImageUtils.rotateBitmap(swappedBitmap, 180.0f);
                        }

                        Bitmap dest = Bitmap.createBitmap(swappedBitmap, 0, 0, bitmap2.getWidth(), bitmap2.getHeight());

                        if (swappedBitmap != null) {
                            image1Changed = false;
                            image2Changed = false;

                            Intent intent = new Intent(FaceSwapperActivity.this, ResultActivity.class);

                            intent.putExtra("Avatar", newAvatar);
                            appCache.put("temp", dest);

                            startActivityForResult(intent, BACK_PRESSED_REQUEST_CODE);
                        }
                    }
                }
            }, 500);
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        FaceSwapFragments.FaceFragmentA phA = (FaceSwapFragments.FaceFragmentA) sectionsPagerAdapter.getItem(0);
        FaceSwapFragments.FaceFragmentB phB = (FaceSwapFragments.FaceFragmentB) sectionsPagerAdapter.getItem(1);

        editBitmap = null;

        if (tabLayout.getSelectedTabPosition() == 0 && bitmap1 != null) {
            phA.setImage(bitmap1);
            viewPager.setCurrentItem(0);
        } else if (tabLayout.getSelectedTabPosition() == 1 && bitmap2 != null) {
            phB.setImage(bitmap2);
            viewPager.setCurrentItem(1);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    private Bitmap editBitmap = null;

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 0) {
                return FaceSwapFragments.FaceFragmentA.newInstance(position);
            }

            if (position == 1) {
                return FaceSwapFragments.FaceFragmentB.newInstance(position);
            }

            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private String mCurrentPhotoPath;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String path;

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            if (Build.VERSION.SDK_INT >= CAMERA_API_LEVEL_LIMIT) {

                if (mCurrentPhotoPath == null) return;
                Uri imageUri = Uri.parse(mCurrentPhotoPath);
                if (imageUri == null) return;
                Bitmap bitmap = ImageUtils.makeBitmap(imageUri.getPath());
                bitmap = ImageUtils.rotatedExifBitmap(imageUri.getPath(), bitmap);

                setBitmap(bitmap, imageUri);
            } else {

                Uri takenPhotoUri = getPhotoFileUri();

                path = takenPhotoUri.getPath();
                Bitmap bitmap = ImageUtils.makeBitmap(path);
                bitmap = ImageUtils.rotatedExifBitmap(path, bitmap);

                setBitmap(bitmap, takenPhotoUri);
            }
        }

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {

            Uri selectedImage = data.getData();
            Bitmap bitmap = null;

            if (selectedImage.toString().startsWith("content://com.google.android.apps.photos.content")) {

                try {
                    InputStream is = getContentResolver().openInputStream(selectedImage);
                    if (is != null) {
                        Bitmap bitmapTemp = BitmapFactory.decodeStream(is);
                        bitmap = ImageUtils.resizeBitmap(bitmapTemp);
                    }
                } catch (FileNotFoundException e) {
                    Log.e(LOGTAG, e.getMessage());
                }
            } else {

                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                path = cursor.getString(columnIndex);
                cursor.close();

                bitmap = ImageUtils.makeBitmap(path);
                bitmap = ImageUtils.rotatedExifBitmap(path, bitmap);
            }

            if (bitmap != null) {
                setBitmap(bitmap, selectedImage);
            }
        }

        updateSwapButton();

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {

                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(this.getLocalClassName(), "onActivityResult: sent invitation " + id);
                }
            }
        }

        if (requestCode == BACK_PRESSED_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {

                newAvatar = data.getParcelableExtra("Avatar");
                avatarInputFieldFragment.setNewAvatar(newAvatar);
            }
        }
    }

    private File createImageFile() {

        String imageFileName = "face_swap_plus_image";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,
                    "jpg.",
                    storageDir

            );
        } catch (IOException e) {
            Log.e(LOGTAG, e.getMessage());
        }

        mCurrentPhotoPath = image != null ? image.getAbsolutePath() : null;
        return image;
    }

    private Uri getPhotoFileUri() {

        if (isExternalStorageAvailable()) {

            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FaceSwap");

            return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + FaceSwapperActivity.PHOTO_FILENAME));
        }
        return null;
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    private void updateSwapButton() {
        if (bitmap1 != null && bitmap2 != null) {
            swapFacesButton.setEnabled(true);
        } else {
            swapFacesButton.setEnabled(false);
        }
    }

    private void setBitmap(Bitmap bitmap, Uri uri) {

        if (tabLayout.getSelectedTabPosition() == 0) {
            bitmap1 = bitmap;
            saveUriBitmapA = uri;

            ((FaceSwapFragments.FaceFragmentA) sectionsPagerAdapter.getItem(0)).setImage(bitmap1);

            image1Changed = true;
        } else if (tabLayout.getSelectedTabPosition() == 1) {
            bitmap2 = bitmap;
            saveUriBitmapB = uri;

            ((FaceSwapFragments.FaceFragmentB) sectionsPagerAdapter.getItem(1)).setImage(bitmap2);

            image2Changed = true;
        }

        updateSwapButton();
    }

    private Toast showInfoToast(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        final Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 400);
        toast.show();

        return toast;
    }

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 123;
    private boolean granted = true;

    private boolean checkPermissions() {
        granted = true;

        String requests[] = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        for (String request : requests) {
            if (ContextCompat.checkSelfPermission(this, request) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
        }

        if (granted) {
            return true;
        }
        ActivityCompat.requestPermissions(this, requests, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        return granted;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {

                granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_action_create_new:
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                break;
            case android.R.id.home:
                onBackPressed();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_face_swapper_menu, menu);
        return true;
    }
}