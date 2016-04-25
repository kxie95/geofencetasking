package com.app.gfour.geofencetasker.data;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import com.google.android.gms.location.Geofence;

public class Ag {

    List<Object> ol;

    public Ag(String str1, String str2) {
        ol = new  ArrayList<Object>();
        ol.add(str1);
        ol.add(str2);
    }

    public Ag(String str1) {
        ol = new  ArrayList<Object>();
        ol.add(str1);
    }

    public Ag(int int1) {
        ol = new  ArrayList<Object>();
        ol.add(int1);
    }

    public Ag(List<String> str) {
        ol = new  ArrayList<Object>();
        ol.add(str);
    }

    public Ag(int int1, List<Geofence> lsg1) {
        ol = new  ArrayList<Object>();
        ol.add(int1);
        ol.add(lsg1);
    }

    public Ag(Intent int1) {
        ol = new  ArrayList<Object>();
        ol.add(int1);
    }

    public Ag(Intent intent1, int int1, int int2) {
        ol = new  ArrayList<Object>();
        ol.add(intent1);
        ol.add(int1);
        ol.add(int2);
    }

    public Ag(Task t){
        ol = new ArrayList<Object>();
        ol.add(t);
    }

    public Ag(int i, double d1, double d2){
        ol = new ArrayList<Object>();
        ol.add(i);
        ol.add(d1);
        ol.add(d2);
    }

    public Ag(double d1, double d2, double d3, double d4) {
        ol = new  ArrayList<Object>();
        ol.add(d1);
        ol.add(d2);
        ol.add(d3);
        ol.add(d4);
    }

    public Object getArg(int i) {
        return ol.get(i);
    }
}
