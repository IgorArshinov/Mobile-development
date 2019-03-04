package be.igorarshinov.avatar_creator.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;

import be.igorarshinov.avatar_creator.BR;
import be.igorarshinov.avatar_creator.utils.DatetimeConverter;

public class Avatar extends BaseObservable implements Parcelable {

    private static final String LOGTAG = "Avatar";
    private long id;
    private String name;
    private Date datetime;
    private Bitmap image;

    public Avatar() {
    }

    public Avatar(long id, String name, Bitmap image) {
        this.id = id;
        this.name = name;
        this.datetime = new Date();
        this.image = image;
    }

    public Avatar(long id, String name, Date datetime, Bitmap image) {
        this.id = id;
        this.name = name;
        this.datetime = datetime;
        this.image = image;
    }

    protected Avatar(Parcel in) {
        id = in.readLong();
        name = in.readString();

        datetime = DatetimeConverter.getDatetimeObjectFromString(in.readString());
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {

        this.image = image;
    }

    public Date getDatetime() {
        return datetime;
    }

    public String getDatetimeString() {
        return DatetimeConverter.getDatetimeStringFromDate(datetime);
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public static final Creator<Avatar> CREATOR = new Creator<Avatar>() {
        @Override
        public Avatar createFromParcel(Parcel in) {
            return new Avatar(in);
        }

        @Override
        public Avatar[] newArray(int size) {
            return new Avatar[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this.name != name) {
            this.name = name;
            notifyPropertyChanged(BR.name);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(this.getDatetimeString());
    }

    @BindingAdapter("android:src")
    public static void setImageViewResource(ImageView imageView, Bitmap resource) {
        if (resource != null) {
            imageView.setImageBitmap(resource);
        }
    }

    @BindingAdapter("android:datetime")
    public static void setDatetimeTextViewResource(TextView textView, Date resource) {
        if (resource != null) {
            textView.setText(DatetimeConverter.getDatetimeStringFromDate(resource));
        }
    }
}
