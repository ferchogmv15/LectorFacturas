package com.MacPollo.lectorfacturas.Actividades;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.MacPollo.lectorfacturas.General.ImageAdapter;
import com.MacPollo.lectorfacturas.General.MySingleton;
import com.MacPollo.lectorfacturas.General.Parametros;
import com.MacPollo.lectorfacturas.R;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.TimerTask;

public class CambioClaveActivity extends AppCompatActivity {

    EditText txtCedula, txtPass, txtrepitapass;
    Button btnRegistrarse;
    ProgressBar loadingProgressBar;
    ViewPager mViewPager;
    String textoBtnAnterior;
    TextView textoUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambio_clave);

        txtCedula = (EditText) findViewById(R.id.txtCedula);
        txtPass = (EditText) findViewById(R.id.txtPassword);
        txtrepitapass = (EditText) findViewById(R.id.txtRepertirPassword);
        btnRegistrarse = (Button) findViewById(R.id.btnRegistrarse);
        loadingProgressBar = (ProgressBar) findViewById(R.id.loading);

        btnRegistrarse.setOnClickListener(c -> registrarse());
        textoUsuario = (TextView) findViewById(R.id.textoParaUsuario);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("cedula") != null && !extras.getString("cedula").equals("")) { // se bloquea cedula
                String cedula = extras.getString("cedula");
                txtCedula.setText(cedula);
                txtCedula.setEnabled(false);
            }
            if (extras.getString("boton") != null) {
                textoBtnAnterior = extras.getString("boton");
                if (extras.getString("boton").equals("cambioClave")) { // si viene de cambiar clave
                    textoUsuario.setText(getString(R.string.texto_cambio_clave));
                    btnRegistrarse.setText("Cambiar Contraseña");
                }
            }
        }

        mViewPager = (ViewPager) findViewById(R.id.viewPage);
        ImageAdapter adapterView = new ImageAdapter(this);
        mViewPager.setAdapter(adapterView);

        // The_slide_timer
        java.util.Timer timer = new java.util.Timer();
        timer.scheduleAtFixedRate(new CambioClaveActivity.The_Slide_Timer(), 5000, 10000);
    }

    private void registrarse() {
        if (txtCedula.getText().toString() != null && !txtCedula.getText().toString().equals("")) {
            if (txtPass.getText().toString() != null && !txtPass.getText().toString().equals("")) {
                if (txtrepitapass.getText().toString() != null && !txtrepitapass.getText().toString().equals("")) {
                    if (txtPass.getText().toString().equals(txtrepitapass.getText().toString())) {
                        // productivo
                        // String url = "http://ap2021.macpollo.com/apiv1/api/conductor/nuevapasswordcconductor";
                        // pruebas
                        String url = "http://ap2021.macpollo.com/apiprueba/api/conductor/nuevapasswordcconductor";
                        mostrarComponentes(false, false, false, true, false);
                        String password = txtPass.getText().toString();

                        HashMap<String, String> data = new HashMap<>();
                        data.put("documento", txtCedula.getText().toString());
                        data.put("password", password);
                        data.put("version", String.valueOf(Parametros.getCodeVersionApp()));
                        JSONObject parameters = new JSONObject(data);
                        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url,
                                parameters, response -> {
                            try{
                                if (!response.toString().contains("message")) {
                                    // Get the JSON array
                                    String tipo = response.getString("tipo");
                                    String mensaje = response.getString("mensaje");
                                    if (tipo.equals("S")) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                        builder.setTitle("Cambio Exitoso!").setMessage(mensaje).setPositiveButton("Entendido", null);
                                        Intent intent = new Intent(this, LoginActivity.class);
                                        startActivity(intent);
                                    } else {
                                        showLoginFailed(mensaje, 1);
                                    }
                                    mostrarComponentes(true, true, true, false, true);
                                } else {
                                    String mensaje = response.getString("message");
                                    showLoginFailed(mensaje, 1);
                                    mostrarComponentes(true, true, true, false, true);
                                }
                            }catch (JSONException e){
                                e.printStackTrace();
                                mostrarComponentes(true, true, true, false, true);
                            }
                        }, error -> {
                            showLoginFailed(error.getMessage(), 1);
                            mostrarComponentes(true, true, true, false, true);
                        });

                        // Add a request (in this example, called stringRequest) to your RequestQueue.
                        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonRequest);

                    } else {
                        showLoginFailed("Los campos contraseña y repetir contraseña no coinciden, por favor verifique", 4);
                    }
                } else {
                    txtrepitapass.setError("Por favor repita la contraseña");
                }
            } else {
                txtPass.setError("Por favor digite el contraseña");
            }
        } else {
            txtCedula.setError("Por favor digite el número de cedula");
        }
    }

    /**
     * Muestra alert de error cuando esta verificando cedula o codigo de verificación
     * @param errorString mensaje de error a mostrar
     * @param option 1 = cedula, 2 = password, 3 = repetir password
     */
    private void showLoginFailed(String errorString, int option) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.estilo_alerta);
        builder.setTitle("Advertencia").setMessage(errorString)
                .setNegativeButton("Entendido", (dialog, id) -> {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    switch (option) {
                        case 1 :
                            txtCedula.requestFocus();
                            inputMethodManager.showSoftInput(txtCedula, InputMethodManager.SHOW_IMPLICIT);
                            break;
                        case 2:
                            txtPass.requestFocus();
                            inputMethodManager.showSoftInput(txtPass, InputMethodManager.SHOW_IMPLICIT);
                            break;
                        case 3 :
                            txtrepitapass.requestFocus();
                            inputMethodManager.showSoftInput(txtrepitapass, InputMethodManager.SHOW_IMPLICIT);
                            break;
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void mostrarComponentes(boolean mostrarCedula, boolean mostrarPassword, boolean mostrarRepetirPass, boolean mostrarProgreso,
                                    boolean mostrarBtnRegistrarse) {
        txtCedula.setVisibility(mostrarCedula ? View.VISIBLE : View.INVISIBLE);
        txtPass.setVisibility(mostrarPassword ? View.VISIBLE : View.INVISIBLE);
        txtrepitapass.setVisibility(mostrarRepetirPass ? View.VISIBLE : View.INVISIBLE);
        loadingProgressBar.setVisibility(mostrarProgreso ? View.VISIBLE : View.INVISIBLE);
        btnRegistrarse.setVisibility(mostrarBtnRegistrarse ? View.VISIBLE : View.INVISIBLE);
    }

    private class The_Slide_Timer extends TimerTask {
        @Override
        public void run() {

            CambioClaveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mViewPager.getCurrentItem()< ImageAdapter.sliderImageId.length-1) {
                        mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1);
                    }
                    else
                        mViewPager.setCurrentItem(0);
                }
            });
        }
    }

}