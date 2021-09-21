package com.MacPollo.lectorfacturas.Actividades;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.MacPollo.lectorfacturas.General.Formatos;
import com.MacPollo.lectorfacturas.General.MySingleton;
import com.MacPollo.lectorfacturas.R;
import com.MacPollo.lectorfacturas.tablas.TablaFacRuta;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FacturasRutaActivity extends AppCompatActivity {

    TextView tvNumeroContrato;
    TextView tvErrorContrato;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facturas_ruta);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String codigo = extras.getString("codigo");
            tvNumeroContrato = (TextView) findViewById(R.id.textViewNroContrato);
            tvErrorContrato = (TextView) findViewById(R.id.textViewErrorContrato);
            if (isSoloNumero(codigo)) {
                tvNumeroContrato.setText("Contrato transporte \n No: " + devolverSinCeros(codigo));
                // productivo
                // String url = "http://ap2021.macpollo.com/apiv1/api/factura/consultacontrato";
                // pruebas
                String url = "http://ap2021.macpollo.com/apiprueba/api/factura/consultacontrato";
                // desarrollo
                //String url = "http://192.168.1.11:8000/api/factura/consultacontrato";
                //String url = "http://192.168.1.11:8081/consultacontrato.php";
                HashMap<String, String> data = new HashMap<>();
                data.put("contrato", codigo);
                JSONObject parameters = new JSONObject(data);
                tvErrorContrato.setText(Html.fromHtml("Procesando contrato No. <b>" + codigo  +"</b>, Por favor espere..."));

                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url,
                        parameters, response -> {
                    try{
                        if (!response.toString().contains("message")) {
                            // Get the JSON array
                            String tipo = response.getString("tipo");
                            if (tipo.equals("S")) {

                                JSONObject transporte = response.getJSONObject("TFactura");

                                TablaFacRuta tabla = new TablaFacRuta(this, (TableLayout)findViewById(R.id.tabla));
                                tabla.agregarCabecera(R.array.cabecera_tabla_fac_ruta);
                                if(transporte.get("item") instanceof JSONObject) {
                                    JSONObject item = transporte.getJSONObject("item");
                                    ArrayList<String> elementos = new ArrayList<>();
                                    elementos.add(item.getString("Xblnr"));
                                    elementos.add(Formatos.formatoValor(String.valueOf(item.getInt("Pago"))));
                                    tabla.agregarFilaTabla(elementos);
                                } else if (transporte.get("item") instanceof JSONArray) {
                                    JSONArray items = transporte.getJSONArray("item");
                                    String numFac = "";
                                    int sumatoria = 0;
                                    ArrayList<String> elementos = new ArrayList<>();
                                    for(int i = 0; i < items.length(); i++)
                                    {
                                        JSONObject item = items.getJSONObject(i);
                                        if (numFac.equals("")) { // primera vez
                                            numFac = item.getString("Xblnr");
                                            sumatoria = item.getInt("Pago");
                                            elementos.add(item.getString("Xblnr"));
                                        } else if (numFac.equals(item.getString("Xblnr"))) { // si sigue en la misma factura
                                            sumatoria += item.getInt("Pago");
                                        } else {
                                            elementos.add(Formatos.formatoValor(String.valueOf(sumatoria)));
                                            tabla.agregarFilaTabla(elementos);
                                            elementos = new ArrayList<>();
                                            numFac = item.getString("Xblnr");
                                            sumatoria = item.getInt("Pago");
                                            elementos.add(item.getString("Xblnr"));
                                        }
                                    }
                                    elementos.add(Formatos.formatoValor(String.valueOf(sumatoria)));
                                    tabla.agregarFilaTabla(elementos);
                                    //for (int i = 0; i<  30; i++) {
                                        //ArrayList<String> elementos = new ArrayList<String>();
                                        //elementos.add("prueba" + i);
                                        //elementos.add("texto" + i);
                                        //tabla.agregarFilaTabla(elementos);
                                    //}
                                }

                                tvErrorContrato.setVisibility(View.INVISIBLE);
                                ScrollView resultados = (ScrollView) findViewById(R.id.scrollVertical);
                                resultados.setVisibility(View.VISIBLE);
                            } else {
                                String mensaje = response.getString("mensaje");
                                tvErrorContrato.setText(mensaje);
                            }
                        } else {
                            String mensaje = response.getString("message");
                            tvErrorContrato.setText(mensaje);
                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }, error -> {
                    if (error instanceof NetworkError || error instanceof ServerError || error instanceof AuthFailureError ||
                        error instanceof ParseError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                        tvErrorContrato.setText("Error al consultar el contrato: " + getString(R.string.error_internet));
                    } else {
                        tvErrorContrato.setText("Error al consultar el contrato: " + error.getMessage());
                    }
                });

                // Add a request (in this example, called stringRequest) to your RequestQueue.
                MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonRequest);
            } else {
                tvNumeroContrato.setText("Contrato transporte \n No: " + codigo);
                tvErrorContrato.setText("No se encuentra el contrato, Por favor revise");
            }
        }
    }

    private boolean isSoloNumero(String valor) {
        return valor.matches("^\\d{1,10}$");
    }

    private String devolverSinCeros(String valor) {
        return valor.replaceFirst("^0+(?!$)", "");
    }
}