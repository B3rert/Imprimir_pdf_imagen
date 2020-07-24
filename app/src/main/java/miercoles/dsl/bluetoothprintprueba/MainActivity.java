package miercoles.dsl.bluetoothprintprueba;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import miercoles.dsl.bluetoothprintprueba.utilidades.PrintBitmap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_DISPOSITIVO = 425;
    private static final String TAG_DEBUG = "tag_debug";
    private final int ANCHO_IMG_58_MM = 384;
    private static final int MODE_PRINT_IMG = 0;

    private TextView txtLabel;
    private Button btnCerrarConexion;

    // Para la operaciones con dispositivos bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice dispositivoBluetooth;
    private BluetoothSocket bluetoothSocket;

    // identificador unico default
    private UUID aplicacionUUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    //Flujo de datos de entrada y salida del socket bluetooth
    private OutputStream outputStream;
    private InputStream inputStream;

    private String rutaPDF;
    private Button btnTomarPDF;
    private  Button btnImprimirPDF;
    private ImageView img_PDF;
    private Bitmap imageWithBG;
    private int indexingpages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rutaPDF = null;
        txtLabel = (TextView) findViewById(R.id.txt_label);
        btnCerrarConexion = (Button) findViewById(R.id.btn_cerrar_conexion);
        btnImprimirPDF = (Button) findViewById(R.id.btn_imprimir_pdf);
        img_PDF = (ImageView) findViewById(R.id.imgpdfView);
        btnTomarPDF = (Button) findViewById(R.id.btn_tomar_pdf);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btnCerrarConexion.setOnClickListener(this);
        btnImprimirPDF.setOnClickListener(this);
        btnTomarPDF.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_tomar_pdf:
                //URL Del PDF que se va a descargar
                String urlPDF = "http://ds.demosoftonline.com:83/host/ilgua/BusinessAdvantage/Reporte/DownloadFile/idpr0urm24klpov0l2qsarla165936.pdf";
                //Llama al metodo que recibe la direccion URL para efectuar la descarga
                DecargarPDF(urlPDF);
            break;

            case R.id.btn_imprimir_pdf:
                    if (bluetoothSocket != null) {
                        //Buscar PDF
                        String PdfR = getFilesDir()+"/prueba.pdf";
                        PDDocument pd = null;
                        try {
                            //carga el documento pdf
                            pd = PDDocument.load (new File(PdfR));
                            //obtiene el numero de paginas del documento
                            indexingpages = pd.getNumberOfPages();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //se repite dependiendo de las hojas del documento
                        for(int count= 0;count<indexingpages;count++){
                            try {
                                //Busca la imagen que se guardo del pdf, la convierte a bitmap y la imprime
                                rutaPDF = getFilesDir()+"/prueba("+count+").png";

                                //conversion a bitmap
                                Bitmap bitmap = BitmapFactory.decodeFile(rutaPDF);
                                outputStream.write(PrintBitmap.POS_PrintBMP(bitmap, ANCHO_IMG_58_MM, MODE_PRINT_IMG));
                            } catch (IOException e) {
                                Toast.makeText(this, "Error al intentar imprimir PDF", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Log.e(TAG_DEBUG, "Socket nulo");
                        txtLabel.setText("Impresora no conectada");
                    }
                break;

            case R.id.btn_cerrar_conexion:
                cerrarConexion();
                break;
        }
    }

    public void clickBuscarDispositivosSync(View btn) {
        // Cerramos la conexion antes de establecer otra
        cerrarConexion();

        Intent intentLista = new Intent(this, ListaBluetoohtActivity.class);
        startActivityForResult(intentLista, REQUEST_DISPOSITIVO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_DISPOSITIVO:
                    txtLabel.setText("Cargando...");

                    final String direccionDispositivo = data.getExtras().getString("DireccionDispositivo");
                    final String nombreDispositivo = data.getExtras().getString("NombreDispositivo");

                    // Obtenemos el dispositivo con la direccion seleccionada en la lista
                    dispositivoBluetooth = bluetoothAdapter.getRemoteDevice(direccionDispositivo);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // Conectamos los dispositivos

                                // Creamos un socket
                                bluetoothSocket = dispositivoBluetooth.createRfcommSocketToServiceRecord(aplicacionUUID);
                                bluetoothSocket.connect();// conectamos el socket
                                outputStream = bluetoothSocket.getOutputStream();
                                inputStream = bluetoothSocket.getInputStream();

                                //empezarEscucharDatos();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtLabel.setText(nombreDispositivo + " conectada");
                                        Toast.makeText(MainActivity.this, "Dispositivo Conectado", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } catch (IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtLabel.setText("");
                                        Toast.makeText(MainActivity.this, "No se pudo conectar el dispositivo", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Log.e(TAG_DEBUG, "Error al conectar el dispositivo bluetooth");

                                e.printStackTrace();
                            }
                        }
                    }).start();
                    break;
            }
        }
    }

    private void cerrarConexion() {
        try {
            if (bluetoothSocket != null) {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
                bluetoothSocket.close();
                txtLabel.setText("Conexion terminada");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cerrarConexion();
    }

    void DecargarPDF(String Url){

        //inicializamos progressdialog para verificar el proceso de descaga y conversion
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Generando Documento...");

        //nueva instancia para la clase "DescargarPDFAsyncTAsk", descarga el docuemnto PDF
        new DescargarPDFAsyncTask(progressDialog).execute(Url);
    }

    class DescargarPDFAsyncTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        //instanciamos el progresdialog
        DescargarPDFAsyncTask(ProgressDialog progressDialog){
            this.progressDialog = progressDialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... url) {

            //carga la direccion url
            String urlADescargar = url[0];

            //variables para la conexion
            HttpURLConnection conexion = null;
            InputStream input = null;
            OutputStream output = null;

            try {

                //se instancia una nueva URL
                URL url1 = new URL(urlADescargar);

                //se conecta al sitio
                conexion = (HttpURLConnection) url1.openConnection();
                conexion.connect();

                //verifica si la conexion es posible
                if (conexion.getResponseCode() != HttpURLConnection.HTTP_OK){
                    return "Error, verifique su conexion a internet";
                }

                //bytes disponibles para descarga
                byte[] data = new byte[1024];

                //abre una conexion al sitio en internet
                input = conexion.getInputStream();

                //ruta del docuemnto
                String rutaPDFGuardar = getFilesDir()+"/prueba.pdf";

                //crea el nuevo docuemnto
                output = new FileOutputStream(rutaPDFGuardar);
                int count;

                while ((count = input.read(data)) != -1){
                    output.write(data, 0,count);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Error, compruebe su conexion a internet";
            } catch (IOException e) {
                e.printStackTrace();
                return "Error, compruebe su conexion a internet";
            } finally {

                //si el proceso de conexion es correcto se cierran las conexiones
                try {
                    if (input != null) {
                        input.close();
                    }
                    if (output != null){
                        output.close();
                    }
                    if (conexion != null){
                        conexion.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            //se instancia la libreria para lograr la "impresion del pdf"
            PDFBoxResourceLoader.init(getApplicationContext());

            //Busca el documento PDF
            rutaPDF = getFilesDir()+"/prueba.pdf";
            PDDocument pd = null;
            try {
                //carga el documento pdf
                pd = PDDocument.load (new File(rutaPDF));
                //obtiene el numero de paginas del documento
                indexingpages = pd.getNumberOfPages();

            } catch (IOException e) {
                e.printStackTrace();
                return "Documento no encontrado";
            }

            for (int count =0; count<indexingpages; count++){
                //Renderiza el documento PDF, (Conversion PDF a Bitmap)
                PDFRenderer pr = new PDFRenderer (pd);
                try {
                    Bitmap bitmap = pr.renderImageWithDPI(count, 300);
                    imageWithBG = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),bitmap.getConfig());  //Crea otra imagen, renderiza el documento

                    imageWithBG.eraseColor(Color.WHITE);  //cambia el fondo transparente a blanco
                    Canvas canvas = new Canvas(imageWithBG);  //crea imagen canvas con el bitmap
                    canvas.drawBitmap(bitmap, 0f, 0f, null); // dimenciones
                    bitmap.recycle(); //cierra y cambia las caracteristicas que se usaron

                    //ruta del pdf
                    String rutaPDFimg = getFilesDir()+"/";

                    //Llama al metodo para guardar el Bitmap em formato PNG
                    //Recibe el nombre con el que se va a guardar la imagen y el Bitmap del PDF
                    BitmapToPNG.saveBitmap("prueba("+count+").png",imageWithBG,rutaPDFimg);

                } catch (IOException e) {
                    e.printStackTrace();
                    return "No se pudo generar el documento";
                }
            }
            return "Documento listo para imprimir ";
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String mensaje) {
            super.onPostExecute(mensaje);
            //al terminar la descarga y conversion
            //carga vista previa
            img_PDF.setImageBitmap(imageWithBG);
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(),mensaje,Toast.LENGTH_LONG).show();
        }
    }
}