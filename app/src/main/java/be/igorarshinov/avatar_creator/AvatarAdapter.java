
package be.igorarshinov.avatar_creator;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.util.Date;

import be.igorarshinov.avatar_creator.model.Avatar;
import be.igorarshinov.avatar_creator.utils.DatetimeConverter;

class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.AvatarAdapterViewHolder> {

    private final Context context;
    private final String LOGTAG = "AvatarAdapter";
    final private AvatarAdapterOnClickHandler clickHandler;

    public interface AvatarAdapterOnClickHandler {
        void onClick(Avatar view);
    }

    private Cursor cursor;

    public AvatarAdapter(@NonNull Context context, AvatarAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;
    }

    @Override
    public AvatarAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater
                .from(context)
                .inflate(R.layout.avatar_list_content, viewGroup, false);

        view.setFocusable(true);

        return new AvatarAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AvatarAdapterViewHolder avatarAdapterViewHolder, int position) {
        cursor.moveToPosition(position);

        Avatar avatar = getAvatarFromCursor(cursor);

        avatarAdapterViewHolder.avatarName.setText(avatar.getName());
        avatarAdapterViewHolder.avatarImage.setImageBitmap(avatar.getImage());
    }

    @Override
    public int getItemCount() {
        if (null == cursor) return 0;
        return cursor.getCount();
    }

    public Avatar getFirstAvatarInList() {
        if (!cursor.moveToFirst()) {
            return new Avatar();
        }
        return getAvatarFromCursor(cursor);
    }

    void swapCursor(Cursor newCursor) {
        cursor = newCursor;
        notifyDataSetChanged();
    }

    class AvatarAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView avatarName;

        private final ImageView avatarImage;

        AvatarAdapterViewHolder(View view) {
            super(view);
            avatarName = (TextView) view.findViewById(R.id.avatar_name);

            avatarImage = (ImageView) view.findViewById(R.id.avatar_image);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            cursor.moveToPosition(adapterPosition);
            clickHandler.onClick(getAvatarFromCursor(cursor));
        }
    }

    private Avatar getAvatarFromCursor(Cursor cursor) {
        long avatarId = cursor.getLong(AvatarListActivity.INDEX_ID);
        Date avatarDateTime = DatetimeConverter.getDatetimeObjectFromCursorString(cursor.getString(AvatarListActivity.INDEX_DATETIME));
        String avatarName = cursor.getString(AvatarListActivity.INDEX_NAME);
        byte[] avatarImageBlob = cursor.getBlob(AvatarListActivity.INDEX_IMAGE);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(avatarImageBlob);
        Bitmap avatarImage = BitmapFactory.decodeStream(inputStream);

        return new Avatar(avatarId, avatarName, avatarDateTime, avatarImage);
    }
}