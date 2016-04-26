package com.app.gfour.geofencetasker.data;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.List;

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

    public Ag(String str1, String str2, PendingIntent pi){
        ol = new ArrayList<Object>();
        ol.add(str1);
        ol.add(str2);
        ol.add(pi);
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

    public Ag(int i1, int i2, int i3, int i4, SharedPreferences.Editor e){
        ol = new ArrayList<Object>();
        ol.add(i1);
        ol.add(i2);
        ol.add(i3);
        ol.add(i4);
        ol.add(e);
    }

    public Object getArg(int i) {
        return ol.get(i);
    }
}
