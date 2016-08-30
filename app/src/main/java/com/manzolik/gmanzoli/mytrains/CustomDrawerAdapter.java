package com.manzolik.gmanzoli.mytrains;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter per il Drawer principale dell'applicazione
 * */
public class CustomDrawerAdapter extends ArrayAdapter<CustomDrawerItem> {

    Context context;
    List<CustomDrawerItem> drawerItemList;
    int layoutResID;

    public CustomDrawerAdapter(Context context, int layoutResourceID, List<CustomDrawerItem> listItems) {
        super(context, layoutResourceID, listItems);
        this.context = context;
        this.drawerItemList = listItems;
        this.layoutResID = layoutResourceID;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DrawerItemHolder drawerHolder;
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            drawerHolder = new DrawerItemHolder();

            view = inflater.inflate(layoutResID, parent, false);
            drawerHolder.itemName = (TextView)view.findViewById(R.id.drawer_itemName);
            drawerHolder.icon = (ImageView) view.findViewById(R.id.drawer_icon);

            view.setTag(drawerHolder);

        } else {
            drawerHolder = (DrawerItemHolder) view.getTag();
        }

        CustomDrawerItem dItem = this.drawerItemList.get(position);

        drawerHolder.icon.setImageDrawable(ContextCompat.getDrawable(context, dItem.getImgResID()));
        drawerHolder.itemName.setText(dItem.getItemName());

        return view;
    }

    private static class DrawerItemHolder {
        TextView itemName;
        ImageView icon;
    }
}