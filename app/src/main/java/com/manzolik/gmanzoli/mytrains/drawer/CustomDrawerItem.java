package com.manzolik.gmanzoli.mytrains.drawer;


/**
 * Classe che rappresenta un elemento del drawer dell'applicazione
 * */
public class CustomDrawerItem {

    String itemName;
    int imgResID;

    public CustomDrawerItem(String itemName, int imgResID) {
        super();
        this.itemName = itemName;
        this.imgResID = imgResID;
    }

    public String getItemName() {
        return itemName;
    }
    public void setItemName(String itemName) {
        itemName = itemName;
    }
    public int getImgResID() {
        return imgResID;
    }
    public void setImgResID(int imgResID) {
        this.imgResID = imgResID;
    }
}