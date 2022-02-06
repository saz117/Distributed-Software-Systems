import utils.ComUtils;
import utils.Protocolo;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;

/**
 * Clase que contiene toda la lógica el juego (máquina de estados) para el Sever en modo 2 Cliente (Cliente-Servidor-Cliente)
 * @see Server
 * @see Game_CvsC_Server
 * @see Thread
 * @see Socket
 * @see Protocolo
 * @see ComUtils
 * @see IOException
 * @see NoSuchAlgorithmException
 * @see MalformedString
 */
public class Game_CvsC_Server extends Thread {
    /**
     * Variables
     * @see Logs
     * @see Protocolo
     */
    private Socket socket1;
    private Socket socket2;
    private int numClients=2;
    private Logs logs;
    private Protocolo protocolo1;
    private Protocolo protocolo2;
    private states estado1;
    private states estado2;
    private boolean finalizado=false;
    private String nomRecibido1;
    private String nomRecibido2;
    private Integer idRecibido1;
    private Integer idRecibido2;
    private String recibidoS1;
    private String recibidoS2;
    private String hashRecibido1;
    private String hashRecibido2;
    private String secretRecibido1;
    private String secretRecibido2;
    private String insultoRecibido1;
    private String insultoRecibido2;
    private String comebackRecibido1;
    private String comebackRecibido2;
    private String shoutRecibido1;
    private String shoutRecibido2;
    private int ganados1=0;
    private int ganados2=0;
    private boolean result_game;
    private int client1_win_count = 0;
    private int client2_win_count = 0;
    private int numJuego=1;
    boolean recibo1=true;
    private final int duelo=3;
    private final int puntos=2;

    /**
     * Método principal de la clase
     * Método que controla el flujo de la partida global (contadores de victorias)  y el reset entre partidas
     * @see Server
     * @see Game_Server#resetEstado_newPartida
     */
    @Override
    public void run() {
        if (this.numClients==2){
            while (this.client1_win_count < this.duelo && this.client2_win_count < this.duelo){
                System.out.println("Partida: " + numJuego);//imprimipos al usuario en que partida esta
                try {
                    this.gamebucle();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (MalformedString malformedString) {
                    malformedString.printStackTrace();
                }
                System.out.println("Ganados1: "+this.ganados1+" Ganados2: "+this.ganados2);
                if(ganados1==this.puntos){
                    this.client1_win_count += 1;
                }else{
                    this.client2_win_count +=1;
                }
                this.resetEstado_newPartida();
                this.numJuego += 1;
            }
            System.out.println("91 Salgo del run");
            System.out.println("Client1: "+this.client1_win_count+" Client2: "+this.client2_win_count);
            logs.cerrar();
        }else{
            logs.cerrar();
            return;
        }
    }

    /**
     * Estados de la máquina de estados del Servidor2Clients
     */
    private  enum states{
        Hello_Espera,
        Hello_Recibido,
        Hash_Espera,
        Hash_Recibido,
        Secret_Espera,
        Secret_Recibido,
        Insulto_Espera,
        Insulto_Recibido,
        Insulto_Envia,
        Comback_Espera,
        Comback_Recibido,
        Comeback_Envia,
        Shout_Espera,
        Shout_Recibido,
        Error
    }

    /**
     * Constructor que inicializa el protocolo y la comunicación por socket; ademas del registro Log
     * @see Protocolo
     * @see ComUtils
     * @param s1 Socket para el cliente 1
     * @param s2 Socket para el cliente 2
     * @throws IOException lanzamos excepción para arriba
     */
    public Game_CvsC_Server(Socket s1, Socket s2) throws IOException {
        this.protocolo1 = new Protocolo(new ComUtils(s1));
        this.protocolo2 = new Protocolo(new ComUtils(s2));
        this.socket1=s1;
        this.socket2=s2;
        String nameThread=this.getName();
        this.logs=new Logs("Server2"+nameThread+".log");
        estado1=states.Hello_Espera;
        estado2=states.Hello_Espera;
    }

    /**
     * Método que reseta la partida para no tener que introducir el HELLO de nuevo sino ir directamente a
     * introducir el SECRET y generar y enviar el HASH
     * @see Game_CvsC_Server#run()
     */
    public void resetEstado_newPartida (){
        estado1=states.Hash_Espera;
        estado2=states.Hash_Espera;
        this.ganados1=0;
        this.ganados2=0;
        this.finalizado=false;
        this.recibo1=true;
    }

    /**
     * Método que envia a ambos sockets la cadena de error s
     * @param s String que contiene la cadena de error a enviar
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    private void error_msg(String s) throws IOException, NoSuchAlgorithmException {
        protocolo1.write_mensage(s);
        protocolo2.write_mensage(s);
        this.finalizado=true;
        estado1=states.Error;
        estado2=states.Error;
        socket1.close();
        socket2.close();
    }

    /**
     * Método encargado de leer el HELLO
     * @param s String recibido
     * @param s2 se corresponde con la cadena HELLO
     * @param n hace referencia a si es el cliente 1 o el cliente 2
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    public void leer_Hello (String s, String s2, int n) throws IOException, NoSuchAlgorithmException {
        //s2="HELLO";
        if (s.substring(0,s2.length()).equals(s2)){
            //Aqui tenemos el "HASH ID NOMBRE"
            //Lo separamos
            String h=s.substring(s.indexOf(' ')+1);
            //System.out.println("h{"+h+"}");
            String id=h.substring(0,h.indexOf(' '));
            //System.out.println("ID:{"+id+"}");
            String name=h.substring(h.indexOf(' ')+1);
            //System.out.println("NAME:{"+name+"}");
            if (n==1){
                this.idRecibido1=Integer.parseInt(id);
                this.nomRecibido1=name;
            }else{
                this.idRecibido2=Integer.parseInt(id);
                this.nomRecibido2=name;
            }
            return;
        }else{
            //Lanzar excpcion
            this.error_msg("ERROR 108 ¡Código de operación inválido, marinero de agua dulce! ¡Hasta la vista!");
        }
    }

    /**
     * Método encargado de leer el HASH
     * @param s String recibido
     * @param s2 se corresponde con la cadena HASH
     * @param n hace referencia a si es el cliente 1 o el cliente 2
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    public void leer_Hash (String s, String s2, int n) throws IOException, NoSuchAlgorithmException {
        //s2="HASH";
        if (s.substring(0,s2.length()).equals(s2)){
            if (n==1){
                System.out.println("121");
                this.hashRecibido1=s.substring(s2.length()+1);
            }else{
                System.out.println("124");
                this.hashRecibido2=s.substring(s2.length()+1);
            }
            return;
        }else{
            //Lanzar excpcion
            System.out.println("128");
            this.error_msg("ERROR 123 ¡Código de operación inválido, marinero de agua dulce! ¡Hasta la vista!");
        }
    }

    /**
     * Método encargado de leer el SECRET
     * @param s String recibido
     * @param s2 se corresponde con la cadena SECRET
     * @param n hace referencia a si es el cliente 1 o el cliente 2
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    public void leer_Secret (String s, String s2,int n) throws IOException, NoSuchAlgorithmException {
        //s2="SECRET";
        if (s.substring(0,s2.length()).equals(s2)){
            //coger su secret y valorar
            if (n==1){
                this.secretRecibido1=s.substring(s2.length()+1);
            }else{
                this.secretRecibido2=s.substring(s2.length()+1);
            }

            return;
        }else{
            //Lanzar excpcion
            this.error_msg("ERROR 140 ¡Código de operación inválido, marinero de agua dulce! ¡Hasta la vista!");
        }
    }

    /**
     * Método encargado de leer el INSULT
     * @param s String recibido
     * @param s2 se corresponde con la cadena INSULT
     * @param n hace referencia a si es el cliente 1 o el cliente 2
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    public void leer_Insulto (String s, String s2, int n) throws IOException, NoSuchAlgorithmException {
        //s2="INSULT"
        if (s.substring(0,s2.length()).equals(s2)){
            if (n==1){
                this.insultoRecibido1=s.substring(s2.length()+1);
            }else{
                this.insultoRecibido2=s.substring(s2.length()+1);
            }
            return;
        }else{
            //Lanzar excpcion
            this.error_msg("ERROR 156 ¡Código de operación inválido, marinero de agua dulce! ¡Hasta la vista!");
        }
    }

    /**
     * Método encargado de leer el COMEBACK
     * @param s String recibido
     * @param s2 se corresponde con la cadena COMEBACK
     * @param n hace referencia a si es el cliente 1 o el cliente 2
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    private void leer_Comeback(String s, String s2, int n) throws IOException, NoSuchAlgorithmException {
        //s2="COMEBACK"
        System.out.println("162 leer_Comeback: "+s);
        if (s.substring(0,s2.length()).equals(s2)){
            if (n==1){
                this.comebackRecibido1=s.substring(s2.length()+1);
            }else{
                this.comebackRecibido2=s.substring(s2.length()+1);
            }
            return;
        }else{
            //Lanzar excpcion
            this.error_msg("ERROR 171 ¡Código de operación inválido, marinero de agua dulce! ¡Hasta la vista!");
        }
    }

    /**
     * Método encargado de leer el SHOUT
     * @param s String recibido
     * @param s2 se corresponde con la cadena SHOUT
     * @param n hace referencia a si es el cliente 1 o el cliente 2
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    private void leer_Shout (String s, String s2, int n) throws IOException, NoSuchAlgorithmException {
        //s2="SHOUT";
        if (s.substring(0,s2.length()).equals(s2)){
            if (n==1){
                this.shoutRecibido1=s.substring(s2.length()+1);
            }else{
                this.shoutRecibido1=s.substring(s2.length()+1);
            }
            return;
        }else{
            //Lanzar excpcion
            this.error_msg("ERROR 186 ¡Código de operación inválido, marinero de agua dulce! ¡Hasta la vista!");
        }
    }

    /**
     * Metodo encargado de la maquina de estados, es quien tiene la responabilidad de
     * acceder a gestionar los diferentes estados, o decidir en que socket espera la entrada
     * @see Game_CvsC_Server#gestionEstado()
     * @see Game_CvsC_Server#recibirEntrada1()
     * @see Game_CvsC_Server#recibirEntrada2()
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     * @throws MalformedString lanzamos excepción para arriba
     */
    public void gamebucle() throws IOException, NoSuchAlgorithmException, MalformedString {
        while (!this.finalizado){
            System.out.println("Estado1: "+this.estado1+" Estado2 "+this.estado2);
            System.out.println("Voy a gestionEstado");
            gestionEstado();
            if(!finalizado){
                if(recibo1){
                    System.out.println("Voy a recibirEntrada1");
                    recibirEntrada1();
                    System.out.println("Salgo de recibirEntrada1");
                }else{
                    System.out.println("Voy a recibirEntrada2");
                    recibirEntrada2();
                    System.out.println("Salgo de recibirEntrada2");
                }
            }
        }
    }

    /**
     * Metodo encargado de la máquina de estados en la entrada de datos para el socket del Cliente1
     * @see Protocolo#convertirByteStr()
     * @see Game_CvsC_Server#gamebucle
     * @see Game_CvsC_Server#leer_Hello
     * @see Game_CvsC_Server#leer_Hash
     * @see Game_CvsC_Server#leer_Secret
     * @see Game_CvsC_Server#leer_Insulto
     * @see Game_CvsC_Server#leer_Comeback
     * @see Game_CvsC_Server#leer_Shout
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    private void recibirEntrada1() throws IOException, NoSuchAlgorithmException {
        //this.recibidoS1=protocolo1.convertirByteStr();
        //this.socket1.setSoTimeout(5000); //5000 ms = 5 seg   //exit por timeout
        try{
            this.socket1.setSoTimeout(15000); //15000 ms = 15 seg   //exit por timeout
            this.recibidoS1=protocolo1.convertirByteStr();
        }catch (SocketTimeoutException e) {
            System.out.println(e.getMessage());
            String s="ERROR ¡Me he candado de esperar tus mensajes, mequetrefe! ¡Hasta la vista!";
            System.out.println("Enviando por ambos sockets: "+s);
            protocolo1.write_mensage(s);
            protocolo2.write_mensage(s);
            logs.mensaje(this.nomRecibido1+ "-> "+s);
            logs.mensaje(this.nomRecibido2+ "-> "+s);
            this.socket1.close();
            this.socket2.close();
        }

        logs.mensaje(this.nomRecibido1+ "-> "+this.recibidoS1);
        System.out.println("Recibido por socket: "+this.recibidoS1);
        String s2="";
        //estado de enviar hello
        //estado de leer Hello
        if (estado1==states.Hello_Espera){
            s2="HELLO";
            leer_Hello(this.recibidoS1,s2,1); //recuperamos el nombre
            estado1=states.Hello_Recibido;
            recibo1=false;
        }else if (estado1==states.Hash_Espera){
            s2="HASH";
            System.out.println("ENTRA HASH");
            leer_Hash(this.recibidoS1,s2,1);
            estado1=states.Hash_Recibido;
            recibo1=false;
        }else if (estado1==states.Secret_Espera){
            s2="SECRET";
            leer_Secret(this.recibidoS1,s2,1);
            estado1=states.Secret_Recibido;
            recibo1=false;
        }else if(estado1==states.Insulto_Espera){
            //recibir insulto
            s2="INSULT";
            leer_Insulto(this.recibidoS1,s2,1);
            estado1=states.Insulto_Recibido;
            recibo1=false;
        }else if (estado1==states.Comback_Espera){
            s2="COMEBACK";
            leer_Comeback(this.recibidoS1,s2,1);
            estado1=states.Comback_Recibido;
            recibo1=false;
        }else if (estado1==states.Shout_Espera){
            s2="SHOUT";
            leer_Shout(this.recibidoS1,s2,1);
            estado1=states.Shout_Recibido;
            recibo1=false;
        }
    }

    /**
     * Metodo encargado de la máquina de estados en la entrada de datos para el socket del Cliente2
     * @see Protocolo#convertirByteStr()
     * @see Game_CvsC_Server#gamebucle
     * @see Game_CvsC_Server#leer_Hello
     * @see Game_CvsC_Server#leer_Hash
     * @see Game_CvsC_Server#leer_Secret
     * @see Game_CvsC_Server#leer_Insulto
     * @see Game_CvsC_Server#leer_Comeback
     * @see Game_CvsC_Server#leer_Shout
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    private void recibirEntrada2() throws IOException, NoSuchAlgorithmException {
        //this.recibidoS2=protocolo2.convertirByteStr();
        //this.socket2.setSoTimeout(5000); //5000 ms = 5 seg   //exit por timeout
        try{
            this.socket2.setSoTimeout(15000); //15000 ms = 15 seg   //exit por timeout
            this.recibidoS2=protocolo2.convertirByteStr();
        }catch (SocketTimeoutException e) {
            System.out.println(e.getMessage());
            String s="ERROR ¡Me he candado de esperar tus mensajes, mequetrefe! ¡Hasta la vista!";
            System.out.println("Enviando por ambos sockets: "+s);
            protocolo1.write_mensage(s);
            protocolo2.write_mensage(s);
            logs.mensaje(this.nomRecibido1+ "-> "+s);
            logs.mensaje(this.nomRecibido2+ "-> "+s);
            this.socket1.close();
            this.socket2.close();
        }

        logs.mensaje(this.nomRecibido2+ "-> "+this.recibidoS2);
        System.out.println("Recibido por socket: "+this.recibidoS2);
        String s2="";
        //estado de enviar hello
        //estado de leer Hello
        if (estado2==states.Hello_Espera){
            s2="HELLO";
            leer_Hello(this.recibidoS2,s2,2); //recuperamos el nombre
            estado2=states.Hello_Recibido;
            recibo1=true;
        }else if (estado2==states.Hash_Espera){
            s2="HASH";
            leer_Hash(this.recibidoS2,s2,2);
            estado2=states.Hash_Recibido;
            recibo1=true;
        }else if (estado2==states.Secret_Espera){
            s2="SECRET";
            leer_Secret(this.recibidoS2,s2,2);
            estado2=states.Secret_Recibido;
            recibo1=true;
        }else if(estado2==states.Insulto_Espera){
            //recibir insulto
            s2="INSULT";
            leer_Insulto(this.recibidoS2,s2,2);
            estado2=states.Insulto_Recibido;
            recibo1=true;
        }else if (estado2==states.Comback_Espera){
            s2="COMEBACK";
            leer_Comeback(this.recibidoS2,s2,2);
            estado2=states.Comback_Recibido;
            recibo1=true;
        }else if (estado2==states.Shout_Espera){
            s2="SHOUT";
            leer_Shout(this.recibidoS2,s2,2);
            estado2=states.Shout_Recibido;
            recibo1=true;
        }
    }

    /**
     * Método de la máquina de estados encargado de mandar los mensajes correspondientes
     * a cada sockect ademas de actualizar los estados para ambos clientes
     * @see Game_CvsC_Server#gestionSumaSecret
     * @see Game_CvsC_Server#valorarComeback_Estado_VictoriasDerrotas
     * @see Protocolo#write_mensage
     * @see Protocolo#send_hash
     * @see Logs#mensaje
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    private void gestionEstado() throws IOException, NoSuchAlgorithmException {
        if (estado1==states.Hello_Recibido && estado2==states.Hello_Recibido){
            //enviar los mensages
            protocolo1.write_mensage(this.recibidoS2);
            protocolo2.write_mensage(this.recibidoS1);
            estado1=states.Hash_Espera;
            estado2=states.Hash_Espera;

        }else if (estado1==states.Hash_Recibido && estado2==states.Hash_Recibido){
            protocolo1.send_hash(this.recibidoS2);
            protocolo2.send_hash(this.recibidoS1);
            estado1=states.Secret_Espera;
            estado2=states.Secret_Espera;

        }else if (estado1==states.Secret_Recibido && estado2==states.Secret_Recibido){
            //enviar
            System.out.println("Envio secret2 a client1"+this.recibidoS2);
            protocolo1.write_mensage(this.recibidoS2);
            System.out.println("Envio secret1 a client2"+this.recibidoS1);
            protocolo2.write_mensage(this.recibidoS1);
            //Valorar y cambiar los estados correspondientes
            this.gestionSumaSecret(Integer.parseInt(this.secretRecibido1),Integer.parseInt(this.secretRecibido2),
                    this.idRecibido1,this.idRecibido2);

        }else if (estado1==states.Insulto_Envia && estado2==states.Insulto_Recibido){
            //2 envia insult a 1
            protocolo1.write_mensage(this.recibidoS2);
            estado1=states.Comback_Espera;
            estado2=states.Comeback_Envia;
        }else if (estado2==states.Insulto_Envia && estado1==states.Insulto_Recibido){
            //1 envia insult a 2
            protocolo2.write_mensage(this.recibidoS1);
            estado1=states.Comeback_Envia;
            estado2=states.Comback_Espera;
        }else if (estado1==states.Comback_Recibido && estado2==states.Comeback_Envia){
            //enviar al 2
            protocolo2.write_mensage(this.recibidoS1);
            //calcular quien gana -> actualizar estados
            valorarComeback_Estado_VictoriasDerrotas(1);
        }else if (estado2==states.Comback_Recibido && estado1==states.Comeback_Envia){
            //enviar al 1
            protocolo1.write_mensage(this.recibidoS2);
            //calcular quien gana -> actualizar estados
            valorarComeback_Estado_VictoriasDerrotas(2);
        }else if (estado1==states.Shout_Recibido && estado2==states.Shout_Recibido){
            //enviar al 2
            protocolo2.write_mensage(this.recibidoS1);
            protocolo1.write_mensage(this.recibidoS2);
            this.finalizado=true;
        }
    }

    /**
     * Método encargado de valorar la lógica de victoria-derrota ante un comeback
     * y de actualizar los estados correspondientes para el cliente1 y el cliente2
     * @see Game_CvsC_Server#gestionEstado()
     * @param n_comebak indice que indica el comeback en la lista
     */
    private void valorarComeback_Estado_VictoriasDerrotas(int n_comebak){
        int i_insult;
        int i_come;
        if (n_comebak==1){
            //insulto del 2 combecak del 1
            i_insult=Game_Server.listaInsultos.indexOf(this.insultoRecibido2);
            i_come=Game_Server.listaComback.indexOf(this.comebackRecibido1);
        }else{
            //insulto del 1 comeback del 2
            i_insult=Game_Server.listaInsultos.indexOf(this.insultoRecibido1);
            i_come=Game_Server.listaComback.indexOf(this.comebackRecibido2);
        }
        if(i_insult==i_come){
            System.out.println("369 Entra en valorarComeback_Estado_VictoriasDerrotas");
            //gana el del n_comeback (quien ha hecho el comeback)
            if (n_comebak==1){
                ganados1++;
                if(ganados1 < this.puntos){
                    // el 1 insulta de nuevo y el 2 debe esperar el insulto
                    System.out.println("393");
                    estado1=states.Insulto_Espera;
                    estado2=states.Insulto_Envia;
                    recibo1=true;
                }else{
                    System.out.println("397");
                    //SHOUT
                    estado1=states.Shout_Espera;
                    estado2=states.Shout_Espera;
                }
            }else{
                ganados2++;
                if (ganados2 < this.puntos){
                    System.out.println("405");
                    // el 1 insulta de nuevo y el 2 debe esperar el insulto
                    estado1=states.Insulto_Envia;
                    estado2=states.Insulto_Espera;
                    recibo1=false;
                }else{
                    System.out.println("411");
                    //SHOUT
                    estado1=states.Shout_Espera;
                    estado2=states.Shout_Espera;
                }
            }
        }else{
            System.out.println("418 Entra en valorarComeback_Estado_VictoriasDerrotas");
            //gana el otro del n_comeback (quien ha insultado)
            if (n_comebak==1){
                ganados2++;
                if(ganados2 < this.puntos){
                    // el 1 insulta de nuevo y el 2 debe esperar el insulto
                    System.out.println("424");
                    estado2=states.Insulto_Espera;
                    estado1=states.Insulto_Envia;
                    recibo1=false;
                }else{
                    System.out.println("428");
                    //SHOUT
                    estado2=states.Shout_Espera;
                    estado1=states.Shout_Espera;
                }
            }else{
                ganados1++;
                if (ganados1 < this.puntos){
                    System.out.println("436");
                    // el 1 insulta de nuevo y el 2 debe esperar el insulto
                    estado2=states.Insulto_Envia;
                    estado1=states.Insulto_Espera;
                    recibo1=true;
                }else{
                    System.out.println("442");
                    //SHOUT
                    estado2=states.Shout_Espera;
                    estado1=states.Shout_Espera;
                }
            }
        }
    }

    /**
     * Método que implementa la logica de dados ambos secretos, a quien voy a enviar el insulto y espear su comeback
     * @param secret1 integer que corresponde al secreto del cliente1
     * @param secret2 integer que corresponde al secreto del cliente2
     * @param idRecibido1 integer correspondiente al id recibido del cliente 1
     * @param idRecibido2 integer correspondiente al id recibido del cliente 2
     * @see Game_CvsC_Server#gamebucle
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    private void gestionSumaSecret(Integer secret1, Integer secret2, Integer idRecibido1, Integer idRecibido2) throws IOException, NoSuchAlgorithmException {
        Integer sumaSecret=secret1+secret2;

        if (secret1==secret2){
            this.error_msg("ERROR ¡Tu cara me suena!");
        }
        if (sumaSecret%2==0){
            if(idRecibido1<idRecibido2){
                System.out.println("395");
                System.out.println("Estado1 insulta");
                //escirbir insulto //Validado
                //estado de enviar un nuevo insulto
                estado2=states.Insulto_Envia;
                estado1=states.Insulto_Espera;
                recibo1=true;
            }else{
                System.out.println("402");
                System.out.println("Estado2 insulta");
                //leer insulto //Validado
                estado1=states.Insulto_Envia;
                estado2=states.Insulto_Espera;
                recibo1=false;
            }
        }else{
            if(idRecibido1<idRecibido2){
                System.out.println("410");
                System.out.println("Estado2 insulta");
                //leer insulto //VALIDADO
                estado1=states.Insulto_Envia;
                estado2=states.Insulto_Espera;
                recibo1=false;
            }else{
                System.out.println("416");
                System.out.println("Estado1 insulta");
                //escibir insulto //VALIDADO
                estado1=states.Insulto_Espera;
                estado2=states.Insulto_Envia;
                recibo1=true;
            }
        }
    }

}
