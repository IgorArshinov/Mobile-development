package be.igorarshinov.avatar_creator;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import be.igorarshinov.avatar_creator.data.AppCache;
import be.igorarshinov.avatar_creator.data.AvatarContract;
import be.igorarshinov.avatar_creator.createavatar.FaceSwapperActivity;
import be.igorarshinov.avatar_creator.model.Avatar;

public class AvatarListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener,
        AvatarListFragment.OnAvatarClickListener,
        AvatarListFragment.OnCursorLoadFinishedListener,
        AvatarDetailsFragment.OnScreenOrientationListener {

    private static final String LIFECYCLE_CALLBACKS_QUERY_TEXT_KEY = "callbacks";

    private final String LOGTAG = "AvatarListActivity";
    private boolean twoPane;

    private boolean searchQuerySubmitted;
    public static final int INDEX_ID = 0;
    public static final int INDEX_NAME = 1;
    public static final int INDEX_DATETIME = 2;
    public static final int INDEX_IMAGE = 3;

    private AvatarListFragment avatarListFragment;
    private AvatarDetailsFragment avatarDetailsFragment;
    private NavigationView navigationView;
    private DrawerLayout drawer;

    private boolean landscapeMode;
    private String queryText;

    private String previousLifecycleQueryText;

    public AvatarListActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_list);
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(LIFECYCLE_CALLBACKS_QUERY_TEXT_KEY)) {
                previousLifecycleQueryText = savedInstanceState
                        .getString(LIFECYCLE_CALLBACKS_QUERY_TEXT_KEY);
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        avatarListFragment = (AvatarListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list_fragment);

        if (findViewById(R.id.avatar_detail_container) != null) {

            twoPane = true;
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.drawer_avatars);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            landscapeMode = true;
            avatarDetailsFragment = new AvatarDetailsFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.avatar_detail_container, avatarDetailsFragment).commit();

            if (tabletSize) {
                avatarListFragment.setStaggeredGridSpanCount(2);
            } else {
                avatarListFragment.setStaggeredGridSpanCount(1);
            }
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            landscapeMode = false;
            if (tabletSize) {
                avatarListFragment.setStaggeredGridSpanCount(3);
            } else {
                avatarListFragment.setStaggeredGridSpanCount(2);
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String name) {
        try {

        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage());
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String name) {
        try {

            if (searchQuerySubmitted) {
                queryText = name;
                avatarListFragment.searchByName(AvatarContract.AvatarEntry.COLUMN_NAME + " LIKE '" + queryText + "%'");
            }

            searchQuerySubmitted = true;
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage());
        }
        return false;
    }

    @Override
    public boolean onClose() {
        try {
            if (searchQuerySubmitted) {

                avatarListFragment.searchByName("");
                searchQuerySubmitted = false;
            }
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage());
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.drawer_avatars);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_action_settings:
                Intent settingsIntent = new Intent(AvatarListActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.menu_action_create_new:
                Intent intent = new Intent(AvatarListActivity.this, FaceSwapperActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.drawer_avatars:

                break;
            case R.id.drawer_settings:
                Intent settingsIntent = new Intent(AvatarListActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAvatarClick(Avatar clickedAvatar) {
        if (landscapeMode) {

            avatarDetailsFragment.setCurrentSelectedAvatarForLandscapeMode(clickedAvatar);

            getSupportFragmentManager().beginTransaction().replace(R.id.avatar_detail_container, avatarDetailsFragment).commit();
        } else {

            Intent intent = new Intent(AvatarListActivity.this, AvatarDetailsActivity.class);

            AppCache appCache = AppCache.getInstance();

            appCache.put("temp", clickedAvatar.getImage());

            intent.putExtra("Avatar", clickedAvatar);
            startActivity(intent);
        }
    }

    public void onCreateNewAvatarButtonClick(View view) {
        Intent intent = new Intent(AvatarListActivity.this, FaceSwapperActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_items_menu, menu);
        final MenuItem searchField = menu.findItem(R.id.menu_search_field);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchField);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        if (previousLifecycleQueryText != null) {
            searchView.setQuery(previousLifecycleQueryText, true);
        }

        return true;
    }

    @Override
    public void onCursorLoadFinished(Avatar firstAvatarInList) {
        if (landscapeMode) {
            if (getIntent().getExtras() != null) {
                Avatar avatar = getIntent().getParcelableExtra("Avatar");
                AppCache appCache = AppCache.getInstance();
                avatar.setImage((Bitmap) appCache.get("temp"));
                getSupportFragmentManager().beginTransaction().replace(R.id.avatar_detail_container, avatarDetailsFragment).commit();
                avatarDetailsFragment.setCurrentSelectedAvatarForLandscapeMode(avatar);
                getIntent().removeExtra("Avatar");
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.avatar_detail_container, avatarDetailsFragment).commit();
                avatarDetailsFragment.setCurrentSelectedAvatarForLandscapeMode(firstAvatarInList);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(LIFECYCLE_CALLBACKS_QUERY_TEXT_KEY, queryText);
    }

    @Override
    public void getCurrentSelectedAvatarForList(Avatar currentSelectedAvatar) {

    }
}
