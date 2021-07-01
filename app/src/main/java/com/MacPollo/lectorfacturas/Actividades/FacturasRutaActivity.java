package com.MacPollo.lectorfacturas.Actividades;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;

import com.MacPollo.lectorfacturas.R;
import com.MacPollo.lectorfacturas.tablas.TablaFacRuta;

import java.util.ArrayList;

public class FacturasRutaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facturas_ruta);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String codigo = extras.getString("codigo");
            Log.i(codigo, "INFOrmacion");

            TablaFacRuta tabla = new TablaFacRuta(this, (TableLayout)findViewById(R.id.tabla));
            tabla.agregarCabecera(R.array.cabecera_tabla_fac_ruta);
            for(int i = 0; i < 15; i++)
            {
                ArrayList<String> elementos = new ArrayList<String>();
                elementos.add(Integer.toString(i));
                elementos.add("Casilla [" + i + ", 0]");
                tabla.agregarFilaTabla(elementos);
            }

        }
    }
}