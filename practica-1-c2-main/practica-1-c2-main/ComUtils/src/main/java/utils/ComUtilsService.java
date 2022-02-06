package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
/**
 * Esta clase unicamente hace referencia a los tests. Hace referencia a ComUtils
 * @deprecated Tan solo la usamos para los tests
 * @see ComUtils
 */
public class ComUtilsService {
    private ComUtils comUtils;

    /**
     * Método consructor para la comunicación en el ComUtils
     * @param inputStream desde donde leeremos
     * @param outputStream donde escribiremos
     * @see InputStream
     * @see OutputStream
     * @see ComUtils#ComUtils(InputStream, OutputStream)
     */
    public ComUtilsService(InputStream inputStream, OutputStream outputStream) throws IOException {
        comUtils = new ComUtils(inputStream, outputStream);
    }

    /**
     * Método para verificar el test al escribir un string de tamaño variable
     * @see ComUtils#write_string_variable
     * @return retorna el tamaño que corresponderá a los bytes para escribir o -1 en caso de error
     */
    public int writeTest(String str) throws IOException {
        //TODO: put your code here
        //verificar notnull
        if (str!=null){
            //calcular longitud str
            int lon=str.length();
            //mandarlo por el soket
            int tam=0;
            if (lon > 999){
                tam=4;
            }else if (lon > 99){
                tam=3;
            }else if (lon > 9){
                tam=2;
            }else {
                tam=1;
            }

            comUtils.write_string_variable(tam,str);
            //comUtils.write_string(linea);   //Pregunta1: peta porque los dos primeros los espera como numeros
            return tam;
        }
        return -1;
    }

    /**
     * Metodo para leer un Test con un tamaño variable
     * @param lon Longitud variable para leer
     * @see ComUtils#read_string_variable
     * @return el string correspondiente al tamaño indicado
     */
    public String readTest(int lon) throws IOException{
        String result = "";
        //TODO: put your code here
        result=comUtils.read_string_variable(lon);
        return result;
    }

    /**
     * Metodo para escribir un único caracter
     * @deprecated No es utilizado en ningún momento pues es suplida por otras funciones superiores
     * @param c caracter char a leer
     * @see ComUtils#write_string_variable
     */
    public void writeChar (char c) throws IOException {
        //lo pasamos a string porque exige un string
        String linea=c+"";
        comUtils.write_string_variable(1,linea);
    }

    /**
     * Metodo para leer un único caracter
     * @deprecated No es utilizado en ningún momento pues es suplida por otras funciones superiores
     * @see ComUtils#read_string_variable
     */
    public char readChar () throws IOException {
        String result= "";
        result=comUtils.read_string_variable(1);
        return result.charAt(0);
    }


}
