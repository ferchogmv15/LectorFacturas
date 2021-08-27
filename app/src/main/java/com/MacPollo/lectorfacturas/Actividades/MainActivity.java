package com.MacPollo.lectorfacturas.Actividades;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import com.MacPollo.lectorfacturas.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btnScan, btnScanRuta;
    String[] permissions = {
            Manifest.permission.CAMERA
    };
    int PERM_CODE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkpermissions();
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String mensaje = extras.getString("mensaje");
            updateUiWithUser(mensaje);
        }
        btnScan = (Button) findViewById(R.id.btnScanFactura);
        btnScan.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ScannerActivity.class)));
        btnScanRuta = (Button) findViewById(R.id.btnScanRuta);
        btnScanRuta.setOnClickListener(c -> startActivity(new Intent(getApplicationContext(), ScannerRutaActivity.class)));
    }

    private void updateUiWithUser(String mensaje) {
        // TODO : initiate successful logged in experience
        //Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.welcome).setMessage(mensaje).setPositiveButton("Entendido", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean checkpermissions(){
        List<String> listofpermisssions = new ArrayList<>();
        for (String perm: permissions){
            if (ContextCompat.checkSelfPermission(getApplicationContext(), perm) != PackageManager.PERMISSION_GRANTED){
                listofpermisssions.add(perm);
            }
        }
        if (!listofpermisssions.isEmpty()){
            ActivityCompat.requestPermissions(this, listofpermisssions.toArray(new String[listofpermisssions.size()]), PERM_CODE);
            return false;
        }
        return true;
    }
}