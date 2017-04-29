package com.manzolik.gmanzoli.mytrains.drawer;


/**
 * Classe che rappresenta un elemento del drawer dell'applicazione
 * */
public class CustomDrawerItem {

    private String mItemName;
    private int mImgResID;

    public CustomDrawerItem(String itemName, int imgResID) {
        super();
        this.mItemName = itemName;
        this.mImgResID = imgResID;
    }

    String getItemName() {
        return mItemName;
    }
    int getImgResID() {
        return mImgResID;
    }

}