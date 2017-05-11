package com.manzolik.gmanzoli.mytrains.adapters;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.manzolik.gmanzoli.mytrains.R;

/**
 * Adapter per il Drawer principale dell'applicazione
 * */
public class CustomDrawerAdapter extends ArrayAdapter<CustomDrawerItem> {

    private Context mCcontext;
    private List<CustomDrawerItem> mDrawerItemList;
    private int mLayoutResID;

    public CustomDrawerAdapter(Context context, int layoutResourceID, List<CustomDrawerItem> listItems) {
        super(context, layoutResourceID, listItems);
        this.mCcontext = context;
        this.mDrawerItemList = listItems;
        this.mLayoutResID = layoutResourceID;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        DrawerItemHolder drawerHolder;
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = ((Activity) mCcontext).getLayoutInflater();
            drawerHolder = new DrawerItemHolder();

            view = inflater.inflate(mLayoutResID, parent, false);
            drawerHolder.itemName = (TextView)view.findViewById(R.id.drawer_itemName);
            drawerHolder.icon = (ImageView) view.findViewById(R.id.drawer_icon);

            view.setTag(drawerHolder);

        } else {
            drawerHolder = (DrawerItemHolder) view.getTag();
        }

        CustomDrawerItem dItem = this.mDrawerItemList.get(position);

        drawerHolder.icon.setImageDrawable(ContextCompat.getDrawable(mCcontext, dItem.getImgResID()));
        drawerHolder.itemName.setText(dItem.getItemName());

        return view;
    }

    private static class DrawerItemHolder {
        TextView itemName;
        ImageView icon;
    }
}