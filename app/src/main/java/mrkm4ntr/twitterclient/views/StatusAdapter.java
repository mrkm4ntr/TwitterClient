package mrkm4ntr.twitterclient.views;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import mrkm4ntr.twitterclient.R;
import mrkm4ntr.twitterclient.data.TwitterContract;

public class StatusAdapter extends CursorAdapter {

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView nameView;
        public final TextView textView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            nameView = (TextView) view.findViewById(R.id.list_item_name_textview);
            textView = (TextView) view.findViewById(R.id.list_item_text_textview);
        }
    }

    public StatusAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_status, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String profileImageUrl = cursor.getString(cursor.getColumnIndex(TwitterContract.StatusEntry.COLUMN_USER_PROFILE_IMAGE_URL));
        String userName = cursor.getString(cursor.getColumnIndex(TwitterContract.StatusEntry.COLUMN_USER_NAME));
        String text = cursor.getString(cursor.getColumnIndex(TwitterContract.StatusEntry.COLUMN_TEXT));
        viewHolder.nameView.setText(userName);
        viewHolder.textView.setText(text);
        new UpdateImageViewTask(viewHolder.iconView).execute(profileImageUrl);
    }

    public class UpdateImageViewTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView mImageView;

        public UpdateImageViewTask(ImageView imageView) {
            mImageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                InputStream input = new URL(params[0]).openStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mImageView.setImageBitmap(bitmap);
        }
    }
}
