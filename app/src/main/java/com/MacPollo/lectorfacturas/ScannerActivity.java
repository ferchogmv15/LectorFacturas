package com.MacPollo.lectorfacturas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ScannerActivity extends AppCompatActivity {

    TextView txt;
    CodeScanner codeScanner;
    CodeScannerView codeScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        txt = (TextView) findViewById(R.id.textView);
        codeScannerView =  (CodeScannerView) findViewById(R.id.scannerView);
        codeScanner = new CodeScanner(this, codeScannerView);
        codeScanner.setFormats(CodeScanner.ALL_FORMATS);

        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // productivo
                        // String url = "http://ap2021.macpollo.com/apiv1/api/factura/consultafactura";
                        // pruebas
                        String url = "http://ap2021.macpollo.com/apiprueba/api/factura/consultafactura";
                        // desarrollo
                        //String url = "http://192.168.1.7:8000/api/factura/consultafactura";
                        String numero = result.getText();
                        if (esValido(numero)) {
                            HashMap data = new HashMap();
                            data.put("factura", completar(numero));
                            JSONObject parameters = new JSONObject(data);

                            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url,
                                    parameters, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try{
                                        if (response.toString().indexOf("message") == -1) {
                                            // Get the JSON array
                                            String tipo = response.getString("tipo");
                                            if (tipo.equals("S")) {
                                                JSONObject factura = response.getJSONObject("TFactura");
                                                StringBuilder mensaje = new StringBuilder("La factura Nro. ");
                                                mensaje.append(numero).append("\n por Valor de $");
                                                String valor = format(String.valueOf(factura.getInt("Valor")));
                                                mensaje.append(valor);
                                                mensaje.append("\n a nombre de ");
                                                mensaje.append(factura.getString("Name1")).append("\n");
                                                String abono = format(String.valueOf(factura.getInt("Abono")));
                                                mensaje.append("Valor pagado $").append(abono);
                                                String saldo = format(String.valueOf(factura.getInt("Saldo")));
                                                mensaje.append("\n Saldo $").append(saldo);
                                                txt.setText(mensaje.toString());
                                            } else {
                                                txt.setText("Factura Nro. " + numero  +"\n");
                                                String mensaje = response.getString("mensaje");
                                                txt.append(mensaje);
                                            }
                                        } else {
                                            txt.setText("Factura Nro. " + numero  +"\n");
                                            String mensaje = response.getString("message");
                                            txt.append(mensaje);
                                        }

                                    }catch (JSONException e){
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    txt.setText("Factura Nro. " + numero  +"\n");
                                    txt.append("Error al consultar la factura: " + error.getMessage());
                                }
                            });

                            // Add a request (in this example, called stringRequest) to your RequestQueue.
                            MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonRequest);
                        } else {
                            txt.setText("Factura Nro. " + numero  +"\n");
                            txt.append("Nro. de factura no válido");
                        }
                    }
                });
            }
        });
    }

    public static boolean esValido(String str) {
        return str.matches("\\d+");  //match a number entero de 1 al 10 digitos
    }

    public static String format(String input){
        StringBuilder sb = new StringBuilder();
        boolean sw = false;
        int i = input.length();
        while (i - 3 > 0) {
            sb.insert(0, input.substring(i-3, i));
            i -= 3;
            if (i > 0) {
                sb.insert(0, ".");
            }
        }
        if (i > 0) {
            sb.insert(0, input.substring(0, i));
        }
        return sb.toString();
    }

    public static String completar(String input){
        if (input.length() < 10){
            StringBuilder sb = new StringBuilder(input);
            int falta = 10 - input.length();
            for (int i = 0; i < falta; i++) {
                sb.insert(0, "0");
            }
            return sb.toString();
        } else {
            return input;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestCamera();
    }

    private void requestCamera() {
        codeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }
}