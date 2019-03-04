package be.igorarshinov.avatar_creator;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import be.igorarshinov.avatar_creator.data.AppCache;
import be.igorarshinov.avatar_creator.databinding.FragmentAvatarDetailBinding;
import be.igorarshinov.avatar_creator.model.Avatar;

public class AvatarDetailsFragment extends Fragment {

    private final String LOGTAG = "AvatarDetailsFragment";

    private boolean landscapeMode;
    private FragmentAvatarDetailBinding binding;
    private Avatar currentAvatar;
    private OnScreenOrientationListener onScreenOrientationListener;

    public AvatarDetailsFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AvatarDetailsFragment.OnScreenOrientationListener) {
            onScreenOrientationListener = (AvatarDetailsFragment.OnScreenOrientationListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAvatarClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        onScreenOrientationListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            landscapeMode = true;
            if (this.getActivity().getIntent().getExtras() != null) {
                Avatar currentAvatar = this.getActivity().getIntent().getParcelableExtra("Avatar");

                onScreenOrientationListener.getCurrentSelectedAvatarForList(currentAvatar);
            }
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            landscapeMode = false;
            Activity activity = this.getActivity();
            if (activity.getIntent().getExtras() != null) {

                currentAvatar = activity.getIntent().getParcelableExtra("Avatar");

                AppCache appCache = AppCache.getInstance();
                currentAvatar.setImage((Bitmap) appCache.get("temp"));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_avatar_detail, container, false);
        View view = binding.getRoot();
        if (!landscapeMode) {
            binding.setAvatar(currentAvatar);
        }

        return view;
    }

    public interface OnScreenOrientationListener {
        void getCurrentSelectedAvatarForList(Avatar currentSelectedAvatar);
    }

    public void setCurrentSelectedAvatarForLandscapeMode(Avatar clickedAvatar) {
        binding.setAvatar(clickedAvatar);
    }
}
