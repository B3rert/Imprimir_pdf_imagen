package miercoles.dsl.bluetoothprintprueba.utilidades;

import android.os.Environment;

import java.io.File;

public class Constantes {

    public static void crearRutaCarpetaImg(){
        File dir = new File(Environment.getExternalStorageDirectory().toString()+"/Pictures/ImagenesImprimir");

        if(!dir.exists()){
            dir.mkdir();
        }
    }

    public static File getRutaDestinoImg(String nombreImg){
        return new File(Environment.getExternalStorageDirectory().toString()+"/Pictures/ImagenesImprimir/"+nombreImg+".png");
    }

    public static File getRutaDestinoPDF(){
        return new File(Environment.getExternalStorageDirectory().toString()+"/Pictures/ImagenesImprimir/prueba.pdf");
    }

    public static File getRutaDestinocrearPDF(){
        return new File(Environment.getExternalStorageDirectory().toString()+"/Pictures/ImagenesImprimir/prueba.pdf");
    }

    public static File getRutaDestinoPDFIMG(String nombrePDF){
        return new File(Environment.getExternalStorageDirectory().toString()+"/Pictures/ImagenesImprimir/"+nombrePDF+".png");
    }


    public static File getRutaDestinoImgPDFDef(){
        return new File(Environment.getExternalStorageDirectory().toString()+"/Pictures/ImagenesImprimir/prueba.png");
    }


}
