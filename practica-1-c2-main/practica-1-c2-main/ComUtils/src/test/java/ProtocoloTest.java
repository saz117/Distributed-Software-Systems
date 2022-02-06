import org.junit.Test;
import static org.junit.Assert.*;
import utils.ComUtils;
import utils.Protocolo;

import java.io.*;
import java.security.NoSuchAlgorithmException;

public class ProtocoloTest {

    @Test
    public void example_test() {
        File file = new File("test");
        try {
            ComUtils comutils = new ComUtils(new FileInputStream(file), new FileOutputStream(file));
            Protocolo protocolo = new Protocolo(comutils);
            //BufferedReader br;
            //br = new BufferedReader(new InputStreamReader(System.in));

            //String linea = br.readLine();
            /*String linea = "HELLO JOE";
            protocolo.write_mensage(linea);
            String s=protocolo.convertirByteStr();
            System.out.println(s);
            assertEquals(linea, s);
*/
            //String linea = br.readLine();
            /*String linea2 = "HASH esta es mi función hash";
            String hashLinea2="HASH B9 6E 2B 9D 54 EF B6 6A 3E 70 D6 30 60 95 56 DD D4 A9 A2 8F FB 91 0F CC C3 2A F3 6F 89 EC 58 56";
            protocolo.write_mensage(linea2);
            String s2=protocolo.convertirByteStr();
            System.out.println(s2);
            assertEquals(hashLinea2, s2);*/

            //String linea = br.readLine();
            String linea3 = "SECRET este es mi secreto";
            protocolo.write_mensage(linea3);
            String s3=protocolo.convertirByteStr();
            System.out.println(s3);
            assertEquals(linea3, s3);

            //String linea = br.readLine();
            String linea4 = "INSULT este es mi insulto";
            protocolo.write_mensage(linea4);
            String s4=protocolo.convertirByteStr();
            System.out.println(s4);
            assertEquals(linea4, s4);

            //String linea = br.readLine();
            String linea5 = "COMEBACK esta es mi respuesta";
            protocolo.write_mensage(linea5);
            String s5=protocolo.convertirByteStr();
            System.out.println(s5);
            assertEquals(linea5, s5);

            //String linea = br.readLine();
            String linea6 = "SHOUT esta es mi end shout congrats";
            protocolo.write_mensage(linea6);
            String s6=protocolo.convertirByteStr();
            System.out.println(s6);
            assertEquals(linea6, s6);

            //String linea = br.readLine();
            String linea7 = "ERROR hay un error";
            protocolo.write_mensage(linea7);
            String s7=protocolo.convertirByteStr();
            System.out.println(s7);
            assertEquals(linea7, s7);
            /*
            //String linea = br.readLine();
            String linea8 = "TODO controlar excepción"; //TODO
            protocolo.write_mensage(linea8);
            String s8=protocolo.convertirByteStr();
            System.out.println(s8);
            assertEquals(linea8, s8);
            */
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
