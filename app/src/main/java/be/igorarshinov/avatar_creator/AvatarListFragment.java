package be.igorarshinov.avatar_creator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import be.igorarshinov.avatar_creator.data.AvatarContract;
import be.igorarshinov.avatar_creator.model.Avatar;
import be.igorarshinov.avatar_creator.sync.SyncUtils;

public class AvatarListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        AvatarAdapter.AvatarAdapterOnClickHandler, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int LOADER_ID = 666;
    private final String LOGTAG = "AvatarListActivity";

    private RecyclerView recyclerView;
    private AvatarAdapter avatarAdapter;
    private ProgressBar progressBar;
    private String itemListCurrentValuePreference;
    private String filterByName;

    private int mPosition = RecyclerView.NO_POSITION;

    private OnAvatarClickListener onAvatarClickListener;
    private OnCursorLoadFinishedListener onCursorLoadFinishedListener;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private boolean landscapeMode;
    private static final String LIFECYCLE_CALLBACKS_QUERY_TEXT_KEY = "callbacks";
    private boolean previousSearchQuerySubmitted;
    private boolean searchQuerySubmitted;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(LIFECYCLE_CALLBACKS_QUERY_TEXT_KEY)) {
                previousSearchQuerySubmitted = savedInstanceState
                        .getBoolean(LIFECYCLE_CALLBACKS_QUERY_TEXT_KEY);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_avatar_list, container, false);
        recyclerView = view.findViewById(R.id.item_list_recycler_view);
        avatarAdapter = new AvatarAdapter(getContext(), this);

        assert recyclerView != null;

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        recyclerView.setAdapter(avatarAdapter);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        showLoading();
        setupSharedPreferences();
        getLoaderManager().initLoader(LOADER_ID, null, this);

        SyncUtils.initialize(getContext());

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            landscapeMode = true;
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            landscapeMode = false;
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAvatarClickListener && context instanceof OnCursorLoadFinishedListener) {
            onAvatarClickListener = (OnAvatarClickListener) context;
            onCursorLoadFinishedListener = (OnCursorLoadFinishedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAvatarClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
        onAvatarClickListener = null;
        onCursorLoadFinishedListener = null;
    }

    public void searchByName(String name) {
        if (name.isEmpty()) {
            searchQuerySubmitted = false;
        } else {
            filterByName = name;
            searchQuerySubmitted = true;
        }
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    public void setStaggeredGridSpanCount(int count) {
        staggeredGridLayoutManager.setSpanCount(count);
    }

    public interface OnAvatarClickListener {
        void onAvatarClick(Avatar clickedAvatar);
    }

    public interface OnCursorLoadFinishedListener {
        void onCursorLoadFinished(Avatar firstAvatarInList);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle bundle) {

        switch (loaderId) {

            case LOADER_ID:

                Uri forecastQueryUri = AvatarContract.AvatarEntry.CONTENT_URI;
                String[] projectionColumns = {AvatarContract.AvatarEntry._ID,
                        AvatarContract.AvatarEntry.COLUMN_NAME,
                        AvatarContract.AvatarEntry.COLUMN_DATETIME,
                        AvatarContract.AvatarEntry.COLUMN_IMAGE};

                return new CursorLoader(getContext(),
                        forecastQueryUri,
                        projectionColumns,
                        filterByName,
                        null,
                        itemListCurrentValuePreference);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        avatarAdapter.swapCursor(cursor);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        recyclerView.smoothScrollToPosition(mPosition);
        if (landscapeMode) {
            Avatar avatar = avatarAdapter.getFirstAvatarInList();

            onCursorLoadFinishedListener.onCursorLoadFinished(avatar);
        }

        if (cursor.getCount() != 0 || previousSearchQuerySubmitted) showAvatarDataView();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        avatarAdapter.swapCursor(null);
    }

    @Override
    public void onClick(Avatar clickedAvatar) {

        onAvatarClickListener.onAvatarClick(clickedAvatar);
    }

    private void setupSharedPreferences() {

        SharedPreferences sharedPreferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(getContext());

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        setItemListCurrentValuePreference();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("preference_item_list_key")) {

            setItemListCurrentValuePreference();

            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }
    }

    private void setItemListCurrentValuePreference() {
        switch (Preferences.getItemListCurrentValuePreference(getActivity().getApplicationContext())) {
            case "Name":
                itemListCurrentValuePreference = AvatarContract.AvatarEntry.COLUMN_NAME + " ASC";
                break;
            case "Date":
                itemListCurrentValuePreference = AvatarContract.AvatarEntry.COLUMN_DATETIME + " DESC";
                break;
        }
    }

    private void showAvatarDataView() {

        progressBar.setVisibility(View.INVISIBLE);

        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showLoading() {

        recyclerView.setVisibility(View.INVISIBLE);

        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(LIFECYCLE_CALLBACKS_QUERY_TEXT_KEY, searchQuerySubmitted);
    }
}
