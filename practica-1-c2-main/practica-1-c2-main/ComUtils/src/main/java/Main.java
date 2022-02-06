import utils.ComUtils;
import utils.Protocolo;

import java.io.*;
import java.security.NoSuchAlgorithmException;

/**
 * Esta clase define el main de ComUtils
 * @deprecated Esta clase al convertirse en libreria unicamente nos interesa el Protocolo y el ComUtils
 * @see Protocolo
 * @see ComUtils
 */
public class Main {
    /**
     * Metodo main que verificaba el funcionameinto del protocolo y el comutils para la comunicacion en ficheros
     * @param args Array de tipo String con los argumentos introducidos por consola
     * @deprecated Al pasar a sockets no se usan ficheros
     * @see Protocolo
     * @see ComUtils
     */
    public static void main(String[] args) {
        System.out.println("Hello word");
        //TODO: Put your code here
        File file = new File("test");

        try {
            file.createNewFile();
            ComUtils comutils = new ComUtils(new FileInputStream(file), new FileOutputStream(file));
            Protocolo protocolo = new Protocolo(comutils);
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Introducir protocolo");
            String linea = br.readLine();
            protocolo.write_mensage(linea);
            String s=protocolo.convertirByteStr();
            System.out.println(s);


        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("Error Found during Operation:" + e.getMessage());
            e.printStackTrace();
        }
    }

}