package com.MacPollo.lectorfacturas.Actividades;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.MacPollo.lectorfacturas.General.MySingleton;
import com.MacPollo.lectorfacturas.R;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;

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
        //List<BarcodeFormat> b = new ArrayList<>();
        //b.add(BarcodeFormat.CODE_128);
        //codeScanner.setFormats(b);
        codeScanner.setFormats(CodeScanner.ALL_FORMATS);

        codeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            // productivo
            // String url = "http://ap2021.macpollo.com/apiv1/api/factura/consultafactura";
            // pruebas
            String url = "http://ap2021.macpollo.com/apiprueba/api/factura/consultafactura";
            // desarrollo
            //String url = "http://192.168.1.7:8000/api/factura/consultafactura";
            String numero = result.getText();
            if (esValido(numero)) {
                HashMap<String, String> data = new HashMap<>();
                data.put("factura", completar(numero));
                JSONObject parameters = new JSONObject(data);
                txt.setText(Html.fromHtml("Procesando Factura Nro. <b>" + numero  +"</b>, Por favor espere..."));

                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url,
                        parameters, response -> {
                            try{
                                if (!response.toString().contains("message")) {
                                    // Get the JSON array
                                    String tipo = response.getString("tipo");
                                    if (tipo.equals("S")) {
                                        TextView txtNumFactura = (TextView) findViewById(R.id.textViewtxtFactura);
                                        TextView txtValFactura = (TextView) findViewById(R.id.textViewtxtValFac);
                                        TextView txtCliente = (TextView) findViewById(R.id.textViewtxtCli);
                                        TextView txtvalPago = (TextView) findViewById(R.id.textViewtxtValPago);
                                        TextView txtValSaldo = (TextView) findViewById(R.id.textViewtxtValSaldo);

                                        JSONObject factura = response.getJSONObject("TFactura");
                                        //StringBuilder mensaje = new StringBuilder("La factura Nro. ");
                                        //mensaje.append("<b>").append(numero).append("</b>").append("<br> por Valor de $");
                                        txtNumFactura.setText(numero);
                                        String valor = format(String.valueOf(factura.getInt("Valor")));
                                        txtValFactura.setText(valor);
                                        //mensaje.append("<b>").append(valor).append("</b>");
                                        //mensaje.append("<br>a nombre de ");
                                        //mensaje.append("<b>").append(factura.getString("Name1")).append("</b><br>");
                                        txtCliente.setText(factura.getString("Name1"));
                                        String abono = format(String.valueOf(factura.getInt("Abono")));
                                        //mensaje.append("Valor pagado $").append("<b>").append(abono).append("</b>");
                                        txtvalPago.setText(abono);
                                        String saldo = format(String.valueOf(factura.getInt("Saldo")));
                                        //mensaje.append("<br> Saldo $").append("<b>").append(saldo).append("</b>");
                                        txtValSaldo.setText(saldo);
                                        txt.setVisibility(View.INVISIBLE);
                                        ConstraintLayout layoutResultados = (ConstraintLayout) findViewById(R.id.LayoutResultados);
                                        layoutResultados.setVisibility(View.VISIBLE);
                                    } else {
                                        txt.setText(Html.fromHtml("Factura Nro. <b>" + numero  +"</b><br>"));
                                        String mensaje = response.getString("mensaje");
                                        txt.append(mensaje);
                                    }
                                } else {
                                    txt.setText(Html.fromHtml("Factura Nro. <b>" + numero  +"</b><br>"));
                                    String mensaje = response.getString("message");
                                    txt.append(mensaje);
                                }

                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }, error -> {
                            txt.setText(Html.fromHtml("Factura Nro. <b>" + numero  +"</b><br>"));
                            txt.append("Error al consultar la factura: " + error.getMessage());
                        });

                // Add a request (in this example, called stringRequest) to your RequestQueue.
                MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonRequest);
            } else {
                txt.setText(Html.fromHtml("Factura Nro. <b>" + numero  +"</b><br>"));
                txt.append("Nro. de factura no válido");
            }
        }));
    }

    public static boolean esValido(String str) {
        return str.matches("\\d{1,10}");  //match a number entero de 1 al 10 digitos
    }

    public static String format(String input){
        StringBuilder sb = new StringBuilder();
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
        sb.insert(0, "$");
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