package com.MacPollo.lectorfacturas.Actividades;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import com.MacPollo.lectorfacturas.General.VerificarPermisos;
import com.MacPollo.lectorfacturas.R;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;

import java.util.ArrayList;
import java.util.List;

public class ScannerRutaActivity extends AppCompatActivity {

    CodeScanner codeScanner;
    CodeScannerView codeScannerView;
    Button btnDigitar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_ruta);

        VerificarPermisos.checkpermissions(this);
        codeScannerView =  (CodeScannerView) findViewById(R.id.scannerView);
        codeScanner = new CodeScanner(this, codeScannerView);
        List<BarcodeFormat> b = new ArrayList<>();
        b.add(BarcodeFormat.CODE_128);
        codeScanner.setFormats(b);
        //codeScanner.setFormats(CodeScanner.ALL_FORMATS);

        codeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            String codigo = result.getText();

            consultarContrato(codigo, true);
        }));

        btnDigitar = (Button) findViewById(R.id.btnDigitar);
        btnDigitar.setOnClickListener(v -> showAlertDigitar());
    }

    /**
     * Metodo para hacer la consulta del numero de contrato
     * @param codigo numero de contrato
     * @param escaneado
     */
    private void consultarContrato(String codigo, boolean escaneado) {
        if (esValido(codigo)) {
            Intent intent = new Intent(this, FacturasRutaActivity.class);
            intent.putExtra("codigo", codigo);
            startActivity(intent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Advertencia")
                    .setMessage(escaneado ? "El texto escaneado no es un numero de contrato, por favor verifique" : "Digite un numero de contrato valido")
                    .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestCamera();
                        }})
                    .setNegativeButton(R.string.option_cancelar, null);

            AlertDialog dialog = builder.create();
            dialog.show();
            if (!escaneado) onResume();
        }
    }

    public static boolean esValido(String codigo) {
        return codigo.matches("(\\d){1,10}");  //match a number entero de 1 al 10 digitos
    }

    private void showAlertDigitar() {
        onPause();
        TextInputLayout textInputLayout = new TextInputLayout(ScannerRutaActivity.this);
        EditText input = new EditText(ScannerRutaActivity.this);
        input.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(10)});
        textInputLayout.setHint("Nro Contrato");
        TextInputLayout.LayoutParams textViewLayoutParams =  new TextInputLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textInputLayout.addView(input);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_Consulta)
                .setView(textInputLayout)
                .setMessage(R.string.digite_nro_contrato)
                .setPositiveButton(R.string.option_Consultar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String texto = String.valueOf(input.getText());
                        consultarContrato(texto, false);
                    }})
                .setNegativeButton(R.string.option_cancelar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onResume();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
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