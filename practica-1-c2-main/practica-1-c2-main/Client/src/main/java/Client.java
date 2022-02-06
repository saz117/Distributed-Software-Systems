import utils.ComUtils;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Clase principal Cliente
 * @see Client
 */
public class Client {

    private HashMap<String,String> options = new HashMap();
    private int portServidor=1234;
    private String hostname="localhost";
    private boolean manual=true; //si no se especifica, es manual
    private boolean continuarPartidaMismoPlayer=true;
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    /**
     * Clase main que recogerá los argumentos de entrada y decidirá a que IP del servidor y puerto conectarse
     * como Cliente a un determinado server, ademas de especificar el modo automatico o manual de ejecución
     * @param args Array de tipo String con los argumentos introducidos por consola
     * @see Socket
     * @see Game_Client
     * @see ComUtils
     * @see InetAddress
     * @see IOException
     * @see NoSuchAlgorithmException
     * @see MalformedString
     */
    public static void main(String[] args) {
        //String nomMaquina, str;
        int numPort, value;

        InetAddress maquinaServidora;
        Socket socket = null;
        ComUtils comUtils;

        Game_Client game;
        Player_Client player_client = new Player_Client();

        //Truco para quitarse las estaticas
        Client c=new Client();
        c.parametros(args);

        //nomMaquina = args[0];
        //numPort    = Integer.parseInt(args[1]);

        try{
            /* Obtenim la IP de la maquina servidora */
            maquinaServidora = InetAddress.getByName(c.hostname);

            /* Obrim una connexio amb el servidor */
            while (c.continuarPartidaMismoPlayer) {
                c.continuarPartidaMismoPlayer= false;
                socket = new Socket(maquinaServidora, c.portServidor);
                game = new Game_Client(c.manual, socket, player_client);
                game.Run();
                System.out.println("¿Quieres volver a a jugar otra partida? s/N");
                String respuesta = c.br.readLine();
                if (respuesta.equalsIgnoreCase("s")) {
                    c.continuarPartidaMismoPlayer = true;
                }

                game=null;
                System.gc();
            }

        }catch (IOException e){
            System.out.println("Els errors han de ser tractats correctament en el vostre programa.");

        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        }catch (MalformedString malformedString) {
            malformedString.printStackTrace();

        }finally{
            try {
                if(socket != null) socket.close();

            }catch (IOException ex) {
                System.out.println("Els errors han de ser tractats correctament pel vostre programa");
            } // fi del catch
    }
    } // fi del main

    /**
     * Método que verificará si los parametros introducidos en la ejecución son correctos
     * ademas de guardarlos en un hashMap para su consulta posterior
     * @param args Array de tipo String con los argumentos introducidos por consola
     * @see Exception
     */
    private void parametros(String[] args){
        if (args.length < 4){
            System.out.println("Us: java -jar client -s <maquina\\_servidora> -p <port>  [-i 0|1]");
            System.exit(-1);
        }
        try{
            for (int i=0; i<args.length; i=i+2) {
                options.put(args[i],args[i+1]);
            }
            hostname = options.get("-s");
            portServidor = Integer.parseInt(options.get("-p"));
            if (options.containsKey("-i")){
                manual=Integer.parseInt(options.get("-i"))==0?true:false;
            }
        } catch (Exception e){
            System.out.println("Us: java -jar client -s <maquina\\_servidora> -p <port>  [-i 0|1]");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }


} // fi de la classe

