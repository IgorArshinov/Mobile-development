package be.igorarshinov.avatar_creator.createavatar;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import be.igorarshinov.avatar_creator.R;
import be.igorarshinov.avatar_creator.data.AppCache;
import be.igorarshinov.avatar_creator.databinding.FragmentAvatarInputFieldBinding;
import be.igorarshinov.avatar_creator.model.Avatar;

public class AvatarInputFieldFragment extends Fragment {

    private Avatar newAvatar;

    private final String LOGTAG = "AvatarInputFragment";
    private FragmentAvatarInputFieldBinding binding;

    public AvatarInputFieldFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.getActivity().getIntent().getExtras() != null) {

            newAvatar = this.getActivity().getIntent().getParcelableExtra("Avatar");
            AppCache appCache = AppCache.getInstance();
            newAvatar.setImage((Bitmap) appCache.get("temp"));
        } else {
            newAvatar = new Avatar();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_avatar_input_field, container, true);
        View view = binding.getRoot();

        binding.setAvatar(newAvatar);

        return view;
    }

    public Avatar getNewAvatar() {

        return binding.getAvatar();
    }

    public void setNewAvatar(Avatar newAvatar) {
        this.newAvatar = newAvatar;
        binding.setAvatar(this.newAvatar);
    }

    public void setImageViewEnabled(boolean enabled) {
        ImageView imageView = getView().findViewById(R.id.image_view);
        imageView.setEnabled(enabled);
    }
}
