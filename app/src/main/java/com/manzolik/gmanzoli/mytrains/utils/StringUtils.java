package com.manzolik.gmanzoli.mytrains.utils;

/**
 * Classe con i metodi d'utilità per lavorare con le stringhe
 */

public class StringUtils {


    public static String capitalizeString(String str){
        String[] words = str.split(" ");
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < words.length; i++) {
            if (!words[i].equals("")){ // Serve se ci sono due spazi attaccati
                ret.append(Character.toUpperCase(words[i].charAt(0)));
                ret.append(words[i].substring(1).toLowerCase());
                if (i < words.length - 1) {
                    ret.append(' ');
                }
            }
        }
        return ret.toString().trim();
    }
}
