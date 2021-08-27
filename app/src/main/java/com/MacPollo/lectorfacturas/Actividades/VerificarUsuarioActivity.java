package com.MacPollo.lectorfacturas.Actividades;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.MacPollo.lectorfacturas.Enums.EEstadosVerificacion;
import com.MacPollo.lectorfacturas.General.MySingleton;
import com.MacPollo.lectorfacturas.General.Parametros;
import com.MacPollo.lectorfacturas.R;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class VerificarUsuarioActivity extends AppCompatActivity {

    EditText txtCedula, txtCodVerificacion;
    Button btnVerificar;
    /** codigo de verificación que devuelve la BAPI para la cedula */
    int codVerificacion = -1;
    EEstadosVerificacion estado;
    ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificar_usuario);
        codVerificacion = -1;

        txtCedula = (EditText) findViewById(R.id.txtCedula);
        txtCodVerificacion = (EditText) findViewById(R.id.txtCodigoVerificacion);
        btnVerificar = (Button) findViewById(R.id.btnVerificar);
        btnVerificar.setOnClickListener(v -> verificar());

        estado = EEstadosVerificacion.VERIFICARCEDULA;
        loadingProgressBar = (ProgressBar) findViewById(R.id.loading);
    }

    private void verificar() {
        if (estado.compareTo(EEstadosVerificacion.VERIFICARCEDULA) == 0) { // esta verificando cedula
            if (txtCedula.getText().toString() != null && !txtCedula.getText().toString().equals("")) { //
                // productivo
                // String url = "http://ap2021.macpollo.com/apiv1/api/conductor/verificardocconductor";
                // pruebas
                String url = "http://ap2021.macpollo.com/apiprueba/api/conductor/verificardocconductor";
                mostrarComponentes(false, false, true, false);

                HashMap<String, String> data = new HashMap<>();
                data.put("documento", txtCedula.getText().toString());
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
                                codVerificacion = Integer.parseInt(mensaje);
                                mostrarComponentes(false, true, false, true);
                                estado = EEstadosVerificacion.VERIFICARCODIGO;
                            } else {
                                mostrarComponentes(true, false, false, true);
                                showAlertFailed(mensaje, 1);
                            }
                        } else {
                            mostrarComponentes(true, false, false, true);
                            String mensaje = response.getString("message");
                            showAlertFailed(mensaje, 1);
                        }
                    }catch (JSONException e){
                        mostrarComponentes(true, false, false, true);
                        e.printStackTrace();
                    }
                }, error -> {
                    mostrarComponentes(true, false, false, true);
                    showAlertFailed(error.getMessage(), 1);
                });

                // Add a request (in this example, called stringRequest) to your RequestQueue.
                MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonRequest);

            } else {
                txtCedula.setError("Digite su número de cedula para verificación" );
            }
        } else if (estado.compareTo(EEstadosVerificacion.VERIFICARCODIGO) == 0){ // esta verificando Codigo
            if (txtCodVerificacion.getText().toString() != null && !txtCodVerificacion.getText().toString().equals("")
                && codVerificacion != -1 && codVerificacion == Integer.parseInt(txtCodVerificacion.getText().toString())) { // verificando codigo verificacion enviado
                Intent intent = new Intent(this, CambioClaveActivity.class);
                intent.putExtra("cedula", txtCedula.getText().toString());
                startActivity(intent);

                mostrarComponentes(true, false, false, true);
                txtCedula.setText("");
            } else {
                txtCodVerificacion.setError("El código de verificación no coincide con el enviado, por favor revise" );
            }
        }
    }

    /**
     * Muestra alert de error cuando esta verificando cedula o codigo de verificación
     * @param errorString mensaje de error a mostrar
     * @param option 1 = cedula, 2 = codigo de verificación
     */
    private void showAlertFailed(String errorString, int option) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.estilo_alerta);
        builder.setTitle("Advertencia").setMessage(errorString)
                .setNegativeButton("Entendido", (dialog, id) -> {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (option == 1) {
                        txtCedula.requestFocus();
                        inputMethodManager.showSoftInput(txtCedula, InputMethodManager.SHOW_IMPLICIT);
                    } else if (option == 2) {
                        txtCodVerificacion.requestFocus();
                        inputMethodManager.showSoftInput(txtCodVerificacion, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void mostrarComponentes(boolean mostrarCedula, boolean mostrarCodigo, boolean mostrarProgressBar, boolean mostrarBoton) {
        txtCedula.setVisibility(mostrarCedula ? View.VISIBLE : View.INVISIBLE);
        txtCodVerificacion.setVisibility(mostrarCodigo ? View.VISIBLE : View.INVISIBLE);
        loadingProgressBar.setVisibility(mostrarProgressBar ? View.VISIBLE : View.INVISIBLE);
        btnVerificar.setVisibility(mostrarBoton ? View.VISIBLE : View.INVISIBLE);
    }


}