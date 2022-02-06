import org.junit.Test;
import utils.ComUtils;
import utils.Protocolo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;


public class GameTest {
    InetAddress maquinaServidora = InetAddress.getByName("localhost");
    Socket socket = new Socket(maquinaServidora, 1234);
    Protocolo protocolo = new Protocolo(new ComUtils(socket));
    Player_Client player_client = null;
    Game_Client game = new Game_Client (false,socket, player_client);
    public GameTest() throws IOException {
    }


    /*COMENTARIOS SOBRE LOS TESTS*/
    /*
     * Dado que hay que tener la imagen (en este caso el servidor) encendido para ir respondiendo,
     * porque sino los tests fallarán pues no se establece la comunicación,
     * prefiero dado que es manual, hacer los tests como una ejecución normal al hacer un run
     * debido a que alli puedo verificar y comprobar mas casos y opciones que yo quiera con libertad.
     * Por ello, hasta que no implemenetemos la opción automatica (que en teoria es lo siguiente que haremos)
     * hasta el momento hacía los tests a modo manual; que es lo que comenté en la clase de laboratorio.
     * No osbante, aqui están pero ya digo que fallarán a no ser que el servidor responda, es decir, que
     * en realidad es como si fuese una ejecución normal, pero con restricciones.
     */


    @Test
    public void hello_test() throws IOException, NoSuchAlgorithmException, MalformedString {
        game.mandarHello();
        String s=protocolo.convertirByteStr();
        game.leer_Hello(s,"HELLO");
    }
    @Test
    public void introducirsecret_enviarhash_test() throws IOException, NoSuchAlgorithmException, MalformedString {
        game.introducirSecret_GenerarHash_EnviarHash();
        String s=protocolo.convertirByteStr();
        game.leer_Hash(s,"HASH");
    }
    @Test
    public void secret_test() throws IOException, NoSuchAlgorithmException, MalformedString {
        game.setMiSecret(1234);
        game.mandarSecreto();
        String s=protocolo.convertirByteStr();
        game.leer_Secret(s,"SECRET");
    }
    @Test
    public void insult_test() throws IOException, NoSuchAlgorithmException, MalformedString {
        String s="INSULT insulto test";
        String s2="INSULT";
        if (s.substring(0,s2.length()).equals(s2)){
            /*TODO: descentralizar funcion*/
        }else{
            //Lanzar excpcion
            throw new MalformedString("Secuencia erronea, esperado: INSULT; recibido: " + s );
        }
    }
    @Test
    public void comeback_test() throws IOException, NoSuchAlgorithmException, MalformedString {
        protocolo.write_mensage(String.format("COMEBACK comeback de test"));
        String s=protocolo.convertirByteStr();
        game.leer_Comeback(s,"COMEBACK");
    }
    @Test
    public void shout_test() throws IOException, NoSuchAlgorithmException, MalformedString {
        String nomRecibido="SERV";
        protocolo.write_mensage(String.format("SHOUT ¡Has ganado, %s!",nomRecibido));
        String s=protocolo.convertirByteStr();
        game.leer_Insulto(s,"SHOUT");
    }

}
