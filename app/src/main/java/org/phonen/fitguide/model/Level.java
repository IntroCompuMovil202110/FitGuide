package org.phonen.fitguide.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Level {
    // Declare the @ StringDef for these constants:
    @IntDef({DIAMANTE, BRONCE, ORO, PLATA,HIERRO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Season {}


    //Static Values
    public static final int DIAMANTE = 0x1F48E;
    public static final int BRONCE = 0x1F949;
    public static final int ORO = 0x1F947;
    public static final int PLATA = 0x1F948;
    public static final int HIERRO = 0x1F528;

    public static final String[] levels = {"HIERRO", "BRONCE", "PLATA", "ORO", "DIAMANTE"};

    private int unicode;
    public String Season(String s) {
        if ( s.equals("HIERRO"))
        {
            return new String(Character.toChars(HIERRO));
        }
        else if ( s.equals("BRONCE"))
        {
            return new String(Character.toChars(BRONCE));
        }
        else if ( s.equals("PLATA"))
        {
            return new String(Character.toChars(PLATA));
        }
        else if ( s.equals("ORO"))
        {
            return new String(Character.toChars(ORO));
        }
        else if ( s.equals("DIAMANTE"))
        {
            return new String(Character.toChars(DIAMANTE));
        }else{
            return new String(Character.toChars(HIERRO));
        }
    }

}
