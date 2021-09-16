package com.MacPollo.lectorfacturas.Actividades;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.MacPollo.lectorfacturas.General.Formatos;
import com.MacPollo.lectorfacturas.General.MySingleton;
import com.MacPollo.lectorfacturas.General.VerificarPermisos;
import com.MacPollo.lectorfacturas.R;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
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
    Button btnDigitar, btnEscaner;
    CheckBox checkBoxVerificarFac;
    String cedula, numeroFactura;
    ConstraintLayout layoutResultados;
    TableRow rowMotivo;
    int saldo = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        VerificarPermisos.checkpermissions(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

        btnEscaner = (Button) findViewById(R.id.btnVolverEscaner);
        btnEscaner.setOnClickListener(e -> showEscanerAgain());

        checkBoxVerificarFac = (CheckBox) findViewById(R.id.checkBoxVerificarFac);
        checkBoxVerificarFac.setOnClickListener(v -> enviarVerificacion());

        SharedPreferences preferencias = getSharedPreferences("user-data.xml", MODE_PRIVATE);
        cedula = preferencias.getString("cedula", "");

        layoutResultados = (ConstraintLayout) findViewById(R.id.LayoutResultados);
        rowMotivo = (TableRow) findViewById(R.id.rowMotivoRechazo);
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
        String url = "http://ap2021.macpollo.com/apiprueba/api/factura/consultafactura";
        // desarrollo
        //String url = "http://192.168.254.164:8000/api/factura/consultafactura";
        saldo = -1;
        if (esValido(texto, escaneado)) {
            HashMap<String, String> data = new HashMap<>();
            numeroFactura = texto.substring(texto.indexOf("=") + 1);
            data.put("factura", numeroFactura);
            if(escaneado) {
                data.put("encriptada", "X");
            }
            JSONObject parameters = new JSONObject(data);
            txt.setText(Html.fromHtml("Procesando Factura Nro. <b>" + numeroFactura  +"</b>, Por favor espere..."));

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
                            numeroFactura = factura.getString("Xblnr");
                            String valor = Formatos.formatoValor(String.valueOf(factura.getInt("Valor")));
                            txtValFactura.setText(valor);
                            //mensaje.append("<b>").append(valor).append("</b>");
                            //mensaje.append("<br>a nombre de ");
                            //mensaje.append("<b>").append(factura.getString("Name1")).append("</b><br>");
                            txtCliente.setText(factura.getString("Name1"));
                            String abono = Formatos.formatoValor(String.valueOf(factura.getInt("Abono")));
                            //mensaje.append("Valor pagado $").append("<b>").append(abono).append("</b>");
                            txtvalPago.setText(abono);
                            saldo = factura.getInt("Saldo");
                            String saldoStr = Formatos.formatoValor(String.valueOf(saldo));
                            //mensaje.append("<br> Saldo $").append("<b>").append(saldo).append("</b>");
                            txtValSaldo.setText(saldoStr);

                            mostrarTabla(true);
                            String motivo = factura.getString("Textoerror");
                            if (motivo != null && !motivo.equals("")) {
                                txtMotivoRechazo.setText(motivo);
                                rowMotivo.setVisibility(View.VISIBLE);
                            } else {
                                rowMotivo.setVisibility(View.INVISIBLE);
                            }

                        } else {
                            txt.setText(Html.fromHtml("Factura Nro. <b>" + numeroFactura  +"</b><br>"));
                            String mensaje = response.getString("mensaje");
                            txt.append(mensaje);
                            mostrarTabla(false);
                        }
                    } else {
                        txt.setText(Html.fromHtml("Factura Nro. <b>" + numeroFactura  +"</b><br>"));
                        String mensaje = response.getString("message");
                        txt.append(mensaje);
                        mostrarTabla(false);
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }, error -> {
                txt.setText(Html.fromHtml("Factura Nro. <b>" + numeroFactura  +"</b><br>"));
                if (error instanceof NetworkError || error instanceof ServerError || error instanceof AuthFailureError ||
                    error instanceof ParseError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                    txt.append("Error al consultar la factura: " + getString(R.string.error_internet));
                } else {
                    txt.append("Error al consultar la factura: " + error.getMessage());
                }
                mostrarTabla(false);
            });

            // Add a request (in this example, called stringRequest) to your RequestQueue.
            MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonRequest);
        } else {
            if(escaneado) {
                txt.setText(Html.fromHtml("Texto: <b>" + texto.substring(0, texto.length() > 10 ? 10 : texto.length())  +"</b><br>"));
                txt.append("En el código escaneado NO se encuentra el numero de factura, Por favor revise");
            } else {
                txt.setText(Html.fromHtml("Texto: <b>" + texto  +"</b><br>"));
                txt.append("No se encuentra el numero de factura, Por favor revise");
            }
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
            // productivo
            // String url = "http://ap2021.macpollo.com/apiv1/api/factura/verificarfacturaconductor";
            // pruebas
            String url = "http://ap2021.macpollo.com/apiprueba/api/factura/verificarfacturaconductor";

            HashMap<String, String> data = new HashMap<>();
            data.put("cedulacon", cedula);
            data.put("factura", numeroFactura);
            data.put("saldo", String.valueOf(saldo));
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
                            builder.setTitle("Verificación Exitosa").setMessage(mensaje).setPositiveButton("Entendido", null);

                            AlertDialog dialog = builder.create();
                            dialog.show();
                            checkBoxVerificarFac.setClickable(false);
                        } else {
                            showAlertFailed(mensaje);
                        }
                    } else {
                        String mensaje = response.getString("message");
                        showAlertFailed(mensaje);
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }, error -> {
                showAlertFailed(error.getMessage());
            });

            // Add a request (in this example, called stringRequest) to your RequestQueue.
            MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonRequest);
        }
    }

    private void showEscanerAgain() {
        onResume();
        codeScannerView.setVisibility(View.VISIBLE);
        btnEscaner.setVisibility(View.INVISIBLE);
    }

    private void showAlertDigitar() {
        onPause();
        codeScannerView.setVisibility(View.INVISIBLE);
        btnEscaner.setVisibility(View.VISIBLE);
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

    private void showAlertFailed(String errorString) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.estilo_alerta);
        builder.setTitle("Advertencia").setMessage(errorString)
                .setNegativeButton("Entendido", (dialog, id) -> {});
        AlertDialog dialog = builder.create();
        dialog.show();
        //Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    /**
     * Metodo para mostar tabla de resultados de consulta a factura o mostrar texto de error
     * @param mostrar = true muestra tabla con datos, false = muestra
     */
    private void mostrarTabla(boolean mostrar) {
        if (mostrar) {
            txt.setVisibility(View.INVISIBLE);
            layoutResultados.setVisibility(View.VISIBLE);
            checkBoxVerificarFac.setVisibility(View.VISIBLE);
            checkBoxVerificarFac.setChecked(false);
            checkBoxVerificarFac.setClickable(true);
        } else {
            txt.setVisibility(View.VISIBLE);
            layoutResultados.setVisibility(View.INVISIBLE);
            checkBoxVerificarFac.setVisibility(View.INVISIBLE);
            checkBoxVerificarFac.setChecked(false);
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