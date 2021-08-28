package com.MacPollo.lectorfacturas.Actividades;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class LoginActivity extends AppCompatActivity {

    EditText cedulaEditText, txtPassword;
    Button loginButton, btnCambioPass, btnRegistrarse;
    ProgressBar loadingProgressBar;
    final int codeVersion = Parametros.getCodeVersionApp();
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        cedulaEditText = (EditText) findViewById(R.id.loginCedula);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        loginButton = (Button) findViewById(R.id.btnLogin);
        loadingProgressBar = (ProgressBar) findViewById(R.id.loading);
        btnCambioPass = (Button) findViewById(R.id.btnCambioPass);
        btnRegistrarse = (Button) findViewById(R.id.btnRegistrarse);

        //cedulaEditText.setOnEditorActionListener((v, actionId, event) -> {
        //    if (actionId == EditorInfo.IME_ACTION_DONE) {
        //        loguear(cedulaEditText.getText().toString());
        //    }
        //    return false;
        //});

        loginButton.setOnClickListener(v -> loguear(cedulaEditText.getText().toString()));
        btnRegistrarse.setOnClickListener(r -> registrarse());
        btnCambioPass.setOnClickListener(c -> cambioClave());
        SharedPreferences preferencias = getSharedPreferences("user-data.xml", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.clear();
        editor.apply();

        mViewPager = (ViewPager) findViewById(R.id.viewPage);
        ImageAdapter adapterView = new ImageAdapter(this);
        mViewPager.setAdapter(adapterView);

        // The_slide_timer
        java.util.Timer timer = new java.util.Timer();
        timer.scheduleAtFixedRate(new The_Slide_Timer(), 5000, 10000);

        //Toast.makeText(getApplicationContext(), String.valueOf(versionCode), Toast.LENGTH_SHORT).show();
    }

    private void loguear(String cedula) {
        mostrarComponentes(false, false, true, false, false, false);
        loginButton.setVisibility(View.INVISIBLE);
        cedulaEditText.setVisibility(View.INVISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        txtPassword.setVisibility(View.INVISIBLE);
        btnCambioPass.setVisibility(View.INVISIBLE);
        btnRegistrarse.setVisibility(View.INVISIBLE);
        if (!isCCValid(cedula)) {
            cedulaEditText.setError(getString(R.string.invalid_username));
            mostrarComponentes(true, true, false, true, true, true);
        } else {
            String password = txtPassword.getText().toString();
            if (password != null && !password.equals("")) {
                // productivo
                // String url = "http://ap2021.macpollo.com/apiv1/api/conductor/ingresoconductor";
                // pruebas
                String url = "http://ap2021.macpollo.com/apiprueba/api/conductor/ingresoconductor";

                HashMap<String, String> data = new HashMap<>();
                data.put("documento", cedula);
                data.put("password", password);
                data.put("version", String.valueOf(codeVersion));
                JSONObject parameters = new JSONObject(data);
                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url,
                        parameters, response -> {
                    try{
                        if (!response.toString().contains("message")) {
                            // Get the JSON array
                            String tipo = response.getString("tipo");
                            String mensaje = response.getString("mensaje");
                            if (tipo.equals("S")) {
                                SharedPreferences preferencias = getSharedPreferences("user-data.xml", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferencias.edit();
                                editor.putString("cedula", cedula);
                                editor.apply();
                                Intent intent = new Intent(this, MainActivity.class);
                                intent.putExtra("mensaje", mensaje);
                                startActivity(intent);
                                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputMethodManager.hideSoftInputFromWindow(cedulaEditText.getWindowToken(), 0);

                                cedulaEditText.setText("", TextView.BufferType.EDITABLE);
                                txtPassword.setText("");
                            } else {
                                showLoginFailed(mensaje);
                            }
                            mostrarComponentes(true, true, false, true, true, true);
                        } else {
                            String mensaje = response.getString("message");
                            showLoginFailed(mensaje);
                            mostrarComponentes(true, true, false, true, true, true);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                        mostrarComponentes(true, true, false, true, true, true);
                    }
                }, error -> {
                    showLoginFailed(error.getMessage());
                    mostrarComponentes(true, true, false, true, true, true);
                });

                // Add a request (in this example, called stringRequest) to your RequestQueue.
                MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonRequest);
            } else {
                txtPassword.setError("Por favor digite la contraseña");
                mostrarComponentes(true, true, false, true, true, true);
            }
        }
    }

    private void registrarse() {
        Intent intent = new Intent(this, VerificarUsuarioActivity.class);
        intent.putExtra("boton", "registrarse");
        startActivity(intent);
    }

    private void cambioClave() {
        Intent intent = new Intent(this, VerificarUsuarioActivity.class);
        intent.putExtra("boton", "cambioClave");
        startActivity(intent);
    }

    // A placeholder username validation check
    private boolean isCCValid(String username) {
        if (username == null) {
            return false;
        }
        return !username.trim().isEmpty();
    }

    private void showLoginFailed(String errorString) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.estilo_alerta);
        builder.setTitle("Ingreso Denegado").setMessage(errorString)
                .setNegativeButton("Entendido", (dialog, id) -> {
                    cedulaEditText.requestFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(cedulaEditText, InputMethodManager.SHOW_IMPLICIT);});
        AlertDialog dialog = builder.create();
        dialog.show();
        //Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void mostrarComponentes(boolean mostrarCedula, boolean mostrarPassword, boolean mostrarProgreso, boolean mostrarBtningresar,
                                    boolean mostrarBtnCambioClave, boolean mostrarBtnRegistrarse) {
        cedulaEditText.setVisibility(mostrarCedula ? View.VISIBLE : View.INVISIBLE);
        txtPassword.setVisibility(mostrarPassword ? View.VISIBLE : View.INVISIBLE);
        loadingProgressBar.setVisibility(mostrarProgreso ? View.VISIBLE : View.INVISIBLE);
        loginButton.setVisibility(mostrarBtningresar ? View.VISIBLE : View.INVISIBLE);
        btnCambioPass.setVisibility(mostrarBtnCambioClave ? View.VISIBLE : View.INVISIBLE);
        btnRegistrarse.setVisibility(mostrarBtnRegistrarse ? View.VISIBLE : View.INVISIBLE);
    }

    private class The_Slide_Timer extends TimerTask {
        @Override
        public void run() {

            LoginActivity.this.runOnUiThread(new Runnable() {
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