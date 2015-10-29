package mrkm4ntr.twitterclient.views;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
        String text = cursor.getString(cursor.getColumnIndex(TwitterContract.StatusEntry.COLUMN_TEXT));
        viewHolder.textView.setText(text);
    }
}
