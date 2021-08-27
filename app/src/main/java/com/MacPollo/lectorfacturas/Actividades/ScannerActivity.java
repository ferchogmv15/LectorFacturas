package com.MacPollo.lectorfacturas.Actividades;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.MacPollo.lectorfacturas.General.Formatos;
import com.MacPollo.lectorfacturas.General.MySingleton;
import com.MacPollo.lectorfacturas.R;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScannerActivity extends AppCompatActivity {

    TextView txt;
    CodeScanner codeScanner;
    CodeScannerView codeScannerView;
    Button btnDigitar;
    CheckBox checkBoxVerificarFac;
    String cedula;
    ConstraintLayout layoutResultados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        txt = (TextView) findViewById(R.id.textView);
        codeScannerView =  (CodeScannerView) findViewById(R.id.scannerView);
        codeScanner = new CodeScanner(this, codeScannerView);
        List<BarcodeFormat> b = new ArrayList<>();
        b.add(BarcodeFormat.QR_CODE);
        codeScanner.setFormats(b);
        //codeScanner.setFormats(CodeScanner.ALL_FORMATS);

        codeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            String texto = result.getText();
            consultaFactura(texto, true);
        }));

        btnDigitar = (Button) findViewById(R.id.btnDigitar);
        btnDigitar.setOnClickListener(v -> showAlertDigitar());

        checkBoxVerificarFac = (CheckBox) findViewById(R.id.checkBoxVerificarFac);
        checkBoxVerificarFac.setOnClickListener(v -> enviarVerificacion());

        SharedPreferences preferencias = getSharedPreferences("user-data.xml", MODE_PRIVATE);
        cedula = preferencias.getString("cedula", "");

        layoutResultados = (ConstraintLayout) findViewById(R.id.LayoutResultados);
        //Toast.makeText(getApplicationContext(), cedula, Toast.LENGTH_SHORT).show();
    }

    /**
     * Metodo para hacer la consulta del numero de factura
     * @param texto numero de factura
     * @param escaneado true = viene escaneado y encriptado, false = viene digitado y solo numero
     */
    private void consultaFactura(String texto, boolean escaneado) {
        // productivo
        // String url = "http://ap2021.macpollo.com/apiv1/api/factura/consultafactura";
        // pruebas
        //String url = "http://ap2021.macpollo.com/apiprueba/api/factura/consultafactura";
        // desarrollo
        String url = "http://192.168.254.164:8000/api/factura/consultafactura";
        if (esValido(texto, escaneado)) {
            HashMap<String, String> data = new HashMap<>();
            String numero = texto.substring(texto.indexOf("=") + 1);
            data.put("factura", numero);
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
                            TextView txtMotivoRechazo = (TextView) findViewById(R.id.textViewtxtMotivoRechazo);

                            JSONObject factura = response.getJSONObject("TFactura");
                            //StringBuilder mensaje = new StringBuilder("La factura Nro. ");
                            //mensaje.append("<b>").append(numero).append("</b>").append("<br> por Valor de $");
                            txtNumFactura.setText(factura.getString("Xblnr"));
                            String valor = Formatos.formatoValor(String.valueOf(factura.getInt("Valor")));
                            txtValFactura.setText(valor);
                            //mensaje.append("<b>").append(valor).append("</b>");
                            //mensaje.append("<br>a nombre de ");
                            //mensaje.append("<b>").append(factura.getString("Name1")).append("</b><br>");
                            txtCliente.setText(factura.getString("Name1"));
                            String abono = Formatos.formatoValor(String.valueOf(factura.getInt("Abono")));
                            //mensaje.append("Valor pagado $").append("<b>").append(abono).append("</b>");
                            txtvalPago.setText(abono);
                            String saldo = Formatos.formatoValor(String.valueOf(factura.getInt("Saldo")));
                            //mensaje.append("<br> Saldo $").append("<b>").append(saldo).append("</b>");
                            txtValSaldo.setText(saldo);
                            txtMotivoRechazo.setText(factura.getString("Textoerror"));
                            mostrarTabla(true);
                        } else {
                            txt.setText(Html.fromHtml("Factura Nro. <b>" + numero  +"</b><br>"));
                            String mensaje = response.getString("mensaje");
                            txt.append(mensaje);
                            mostrarTabla(false);
                        }
                    } else {
                        txt.setText(Html.fromHtml("Factura Nro. <b>" + numero  +"</b><br>"));
                        String mensaje = response.getString("message");
                        txt.append(mensaje);
                        mostrarTabla(false);
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }, error -> {
                txt.setText(Html.fromHtml("Factura Nro. <b>" + numero  +"</b><br>"));
                txt.append("Error al consultar la factura: " + error.getMessage());
                mostrarTabla(false);
            });

            // Add a request (in this example, called stringRequest) to your RequestQueue.
            MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonRequest);
        } else {
            txt.setText(Html.fromHtml("Texto: <b>" + texto  +"</b><br>"));
            txt.append("No se encuentra el numero de factura, Por favor revise");
            mostrarTabla(false);
        }
    }

    /**
     * Verifica que el numero de factura venga en formato completo
     * @param str Nro de factura a verificar
     * @param escaneado true = viene escaneado y encriptado, false = viene digitado y solo numero
     * @return
     */
    public static boolean esValido(String str, boolean escaneado) {
        if (escaneado) {
            if(str.indexOf("=") != -1 && str.indexOf("=") + 1 < str.length() - 1) {
                str = str.substring(str.indexOf("=") + 1);
                return str.matches("(\\w){10}");  //match a number entero de 1 al 10 digitos
            } else {
                return false;
            }
        } else {
            return str.matches("(\\d){1,10}");  //match a number entero de 1 al 10 digitos
        }

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

    private void enviarVerificacion() {
        if (checkBoxVerificarFac.isChecked()) {
            Toast.makeText(getApplicationContext(), "checked", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "not checked", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAlertDigitar() {
        TextInputLayout textInputLayout = new TextInputLayout(ScannerActivity.this);
        EditText input = new EditText(ScannerActivity.this);
        input.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(10)});
        textInputLayout.setHint("Nro Factura");
        textInputLayout.addView(input);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_Consulta)
               .setView(textInputLayout)
               .setMessage(R.string.digite_nro_factura)
               .setPositiveButton(R.string.option_Consultar, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       String texto = String.valueOf(input.getText());
                       consultaFactura(texto, false);
                   }})
                .setNegativeButton(R.string.option_cancelar, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Metodo para mostar tabla de resultados de consulta a factura o mostrar texto de error
     * @param mostrar = true muestra tabla con datos, false = muestra
     */
    private void mostrarTabla(boolean mostrar) {
        if (mostrar) {
            txt.setVisibility(View.INVISIBLE);
            layoutResultados.setVisibility(View.VISIBLE);
        } else {
            txt.setVisibility(View.VISIBLE);
            layoutResultados.setVisibility(View.INVISIBLE);
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