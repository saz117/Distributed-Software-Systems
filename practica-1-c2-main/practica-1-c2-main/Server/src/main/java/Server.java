import utils.ComUtils;

import java.io.*;
import java.net.*;
import java.util.HashMap;

/**
 * Clase Servidor
 * @see Server
 */
public class Server {

    private HashMap<String,String> options = new HashMap();
    private int portServidor=1234;
    private Integer numPlayers;

    /**
     * Clase main que recogerá los argumentos de entrada y decidirá que tipo de servidor arrancar
     * (Client-Server o Client-Server-Client) ademas de iniciar el Log y esperar a que los clientes se conecten
     * @param args Array de tipo String con los argumentos introducidos por consola
     * @see ServerSocket
     * @see Game_Server
     * @see Game_CvsC_Server
     * @see ComUtils
     * @see Logs
     * @see MalformedString
     * @see IOException
     */
    public static void main(String[] args) {
        ServerSocket serverSocket=null;

        Socket socket1=null;
        Socket socket2=null;
        boolean manual = false; //Siempre será automatico, pero para hacer pruebas-tests, va bien el manual

        Game_Server game;
        Game_CvsC_Server game2;
        ComUtils comUtils;
        int numJuego=1;

        //int portServidor = 1234;
        int value;

        //Truco para quitarse las estaticas
        Server s=new Server();
        s.parametros(args);


        try {
          /* Creem el servidor */
            serverSocket = new ServerSocket(s.portServidor);
            System.out.println("Servidor socket preparat en el port " + s.portServidor);
            if (s.numPlayers==1){
                while (true) {
                    System.out.println("Esperant una connexió d'un client.");

                    /* Esperem a que un client es connecti amb el servidor */
                    socket1 = serverSocket.accept();
                    System.out.println("Connexió acceptada d'un client.");

                    /* Associem un flux d'entrada/sortida amb el client */
                    //comUtils = new ComUtils(socket);
                    game= new Game_Server(manual,socket1);
                    game.start();
                    //new Game_Server(manual,socket,logs).start();
                    game=null;
                    System.gc();

                } // fi del while infinit
            }else if (s.numPlayers==2){
                while (true) {
                    System.out.println("Esperant una connexió de dos clients.");
                    socket1 = serverSocket.accept();
                    System.out.println("Connexió acceptada d'un client.");
                    socket2 = serverSocket.accept();
                    System.out.println("Connexió acceptada de l'altre client.");
                    game2= new Game_CvsC_Server(socket1,socket2);
                    game2.start();
                    game2=null;
                    System.gc();
                }
            }else{
                System.out.println("Us: java -jar server.jar -p <port> -m <1|2>");
                throw new MalformedString("Numero incorrecto de clientes m solo admite 1 o 2");
            }

        } // fi del try
            catch (IOException | MalformedString ex) {
                System.out.println("Els errors han de ser tractats correctament pel vostre programa");
        } // fi del catch
        finally{
            /* Tanquem la comunicacio amb el client */
            try {
                if(serverSocket != null) serverSocket.close();
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
        if (args.length < 4 ){
            System.out.println("Us: java -jar server.jar -p <port> -m <1|2>");
            System.exit(-1);
        }
        try{
            for (int i=0; i<args.length; i=i+2) {
                options.put(args[i],args[i+1]);
            }
            portServidor = Integer.parseInt(options.get("-p"));
            numPlayers = Integer.parseInt(options.get("-m"));

        } catch (Exception e){
            System.out.println("Us: java -jar server.jar -p <port> -m <1|2>");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }



} // fi de la classe
