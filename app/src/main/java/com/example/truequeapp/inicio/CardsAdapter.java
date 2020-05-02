package com.example.truequeapp.inicio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.truequeapp.R;
import com.example.truequeapp.inicio.CardItem;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CardsAdapter  extends BaseAdapter {

    private Activity activity;
    private final static int AVATAR_WIDTH = 150;
    private final static int AVATAR_HEIGHT = 300;
    private List<CardItem> data;

    public CardsAdapter(Activity activity, List<CardItem> data) {
        this.data = data;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public CardItem getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        // If holder not exist then locate all view from UI file.
        if (convertView == null) {
            // inflate UI from XML file
            convertView = inflater.inflate(R.layout.item, parent, false);
            // get all UI view
            holder = new ViewHolder(convertView);
            // set tag for holder
            convertView.setTag(holder);
        } else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }

        //setting data to views
        holder.name.setText(getItem(position).getName());
        holder.descripcion.setText(getItem(position).getDescripcion());
        holder.precio.setText("$ " + getItem(position).getPrecio());
        //holder.avatar.setImageBitmap(decodeSampledBitmapFromResource(activity.getResources(),
          //      getItem(position).getDrawableId(), AVATAR_WIDTH, AVATAR_HEIGHT));
        Picasso.get().load(data.get(position).getImagen()).into(holder.avatar);
        return convertView;
    }

    private class ViewHolder{
        private ImageView avatar;
        private TextView name;
        private TextView descripcion;
        private  TextView precio;

        @SuppressLint("CutPasteId")
        public ViewHolder(View view) {
            avatar = (ImageView)view.findViewById(R.id.avatar);
            name = (TextView)view.findViewById(R.id.name);
            descripcion = (TextView)view.findViewById(R.id.descripcion);
            precio = (TextView)view.findViewById(R.id.precio);
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}