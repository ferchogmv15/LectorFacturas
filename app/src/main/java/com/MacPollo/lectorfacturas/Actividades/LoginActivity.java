package com.MacPollo.lectorfacturas.Actividades;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.MacPollo.lectorfacturas.BuildConfig;
import com.MacPollo.lectorfacturas.General.MySingleton;
import com.MacPollo.lectorfacturas.General.Parametros;
import com.MacPollo.lectorfacturas.R;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    EditText cedulaEditText;
    Button loginButton;
    ProgressBar loadingProgressBar;
    final int codeVersion = Parametros.getCodeVersionApp();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        cedulaEditText = (EditText) findViewById(R.id.loginCedula);
        loginButton = (Button) findViewById(R.id.btnLogin);
        loadingProgressBar = (ProgressBar) findViewById(R.id.loading);

        cedulaEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loguear(cedulaEditText.getText().toString());
            }

            return false;
        });

        loginButton.setOnClickListener(v -> loguear(cedulaEditText.getText().toString()));
        SharedPreferences preferencias = getSharedPreferences("user-data.xml", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.clear();
        editor.apply();

        //Toast.makeText(getApplicationContext(), String.valueOf(versionCode), Toast.LENGTH_SHORT).show();
    }

    private void loguear(String cedula) {
        loginButton.setVisibility(View.INVISIBLE);
        cedulaEditText.setVisibility(View.INVISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        if (!isCCValid(cedula)) {
            showLoginFailed(getString(R.string.invalid_username));
            loginButton.setVisibility(View.VISIBLE);
            cedulaEditText.setVisibility(View.VISIBLE);
            loadingProgressBar.setVisibility(View.INVISIBLE);
        } else {
            // productivo
            // String url = "http://ap2021.macpollo.com/apiv1/api/factura/consultafactura";
            // pruebas
            String url = "http://ap2021.macpollo.com/apiprueba/api/factura/ingresoconductor";

            HashMap<String, String> data = new HashMap<>();
            data.put("documento", cedula);
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
                        } else {
                            showLoginFailed(getString(R.string.login_failed));
                        }
                        loginButton.setVisibility(View.VISIBLE);
                        cedulaEditText.setVisibility(View.VISIBLE);
                        loadingProgressBar.setVisibility(View.INVISIBLE);
                    } else {
                        String mensaje = response.getString("message");
                        showLoginFailed(mensaje);
                        loginButton.setVisibility(View.VISIBLE);
                        cedulaEditText.setVisibility(View.VISIBLE);
                        loadingProgressBar.setVisibility(View.INVISIBLE);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }, error -> {
                showLoginFailed(error.getMessage());
                loginButton.setVisibility(View.VISIBLE);
                cedulaEditText.setVisibility(View.VISIBLE);
                loadingProgressBar.setVisibility(View.INVISIBLE);
            });

            // Add a request (in this example, called stringRequest) to your RequestQueue.
            MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonRequest);
        }
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
        builder.setTitle("Error al ingresar").setMessage(errorString)
                .setNegativeButton("Entendido", (dialog, id) -> {
                    cedulaEditText.requestFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(cedulaEditText, InputMethodManager.SHOW_IMPLICIT);});
        AlertDialog dialog = builder.create();
        dialog.show();
        //Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}