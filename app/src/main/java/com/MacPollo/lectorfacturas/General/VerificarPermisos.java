package com.MacPollo.lectorfacturas.General;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class VerificarPermisos {

    static String[]  permissions = {
            Manifest.permission.CAMERA
    };
    static int PERM_CODE = 11;

    public static boolean checkpermissions(Activity activity){
        List<String> listofpermisssions = new ArrayList<>();
        for (String perm: permissions){
            if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), perm) != PackageManager.PERMISSION_GRANTED){
                listofpermisssions.add(perm);
            }
        }
        if (!listofpermisssions.isEmpty()){
            ActivityCompat.requestPermissions(activity, listofpermisssions.toArray(new String[listofpermisssions.size()]), PERM_CODE);
            return false;
        }
        return true;
    }
}
