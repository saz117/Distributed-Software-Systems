import utils.ComUtils;
import utils.Protocolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Random;

/**
 * Clase que contiene toda la lógica el juego (máquina de estados) para el Sever en modo 1 Cliente (Cliente-Servidor)
 * @see Server
 * @see Game_Server
 * @see Thread
 * @see Socket
 * @see Protocolo
 * @see Random
 * @see ArrayList
 * @see ComUtils
 * @see IOException
 * @see NoSuchAlgorithmException
 * @see MalformedString
 */
public class Game_Server extends Thread {
    /**
     * Método principal de la clase
     * Método que controla el flujo de la partida global (contadores de victorias)  y el reset entre partidas
     * @see Server
     * @see Game_Server#resetEstado_newPartida
     */
    @Override
    public void run() {
        if (this.numClients==1){
            while (this.client_win_count < this.duelo && this.server_win_count < this.duelo){

                System.out.println("Partida: " + numJuego);//imprimipos al usuario en que partida esta
                try {
                    this.result_game = this.gamebucle();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (MalformedString malformedString) {
                    malformedString.printStackTrace();
                }
                if(result_game){
                    this.server_win_count += 1;
                }else{
                    this.client_win_count +=1;
                }
                this.resetEstado_newPartida();
                this.numJuego += 1;
            }
            logs.cerrar();
        }else{
            logs.cerrar();
            return;
        }
    }

    /**
     * Estados de la maquina de estados del Servidor
     */
    private  enum states{
        s_Hello_Espera,
        c_Hello_Enviado,
        c_Hello_Recibido,
        c_Hash_Enviado,
        c_Hash_Recibido,
        c_Secret_Enviado,
        c_Secret_Recibido,
        Insulto_Enviado,
        Comback_Recibido,
        Insulto_Esperar,
        Insulto_Enviar,
        Insulto_Recibido,
        Comback_Esperar,
        Comback_Enviado,
        Shout_Esperar
    }

    /**
     * Variables
     * @see Socket
     * @see Protocolo
     * @see Random
     * @see Logs
     * @see BufferedReader
     */
    private Socket socket;
    private Protocolo protocolo;
    private boolean manual;  //Si es falso entonces es automatico
    private states estado=states.s_Hello_Espera;
    private String hashRecibido;
    private String nomRecibido;
    private Integer idRecibido;
    private Integer miId;
    private String nomServer;
    private String secretRecibido;
    private String combackRecibido;
    private String comebackEnviar;
    private String insultoEnviado;
    private String insultoRecibido;
    private Integer miSecret=0;
    private int ganados=0;
    private int perdidos=0;
    private boolean finalizado=false;
    private int finDelJuego = 1;
    private Random rand = new Random();
    private Logs logs;
    private int numClients=1;
    private int server_win_count = 0;
    private int client_win_count = 0;
    private int numJuego=1;
    private boolean result_game;
    private final int duelo=3;
    private final int puntos=2;
    private Player_Servidor player_servidor;

    /**
     * Lista de insultos que será utilizada por clases superiores
     * @see Player_Servidor
     */
    public static ArrayList<String> listaInsultos=new ArrayList<String>(Arrays.asList(
            "¿Has dejado ya de usar pañales?",
            "¡No hay palabras para describir lo asqueroso que eres!",
            "¡He hablado con simios más educados que tu!",
            "¡Llevarás mi espada como si fueras un pincho moruno!",
            "¡Luchas como un ganadero!",
            "¡No pienso aguantar tu insolencia aquí sentado!",
            "¡Mi pañuelo limpiará tu sangre!",
            "¡Ha llegado tu HORA, palurdo de ocho patas!",
            "¡Una vez tuve un perro más listo que tu!",
            "¡Nadie me ha sacado sangre jamás, y nadie lo hará!",
            "¡Me das ganas de vomitar!",
            "¡Tienes los modales de un mendigo!",
            "¡He oído que eres un soplón despreciable!",
            "¡La gente cae a mis pies al verme llegar!",
            "¡Demasiado bobo para mi nivel de inteligencia!",
            "Obtuve esta cicatriz en una batalla a muerte!"));

    /**
     * Lista de comebacks que será utilizada por clases superiores
     * @see Player_Servidor
     */
    public static ArrayList<String> listaComback=new ArrayList<String>(Arrays.asList(
            "¿Por qué? ¿Acaso querías pedir uno prestado?",
            "Sí que las hay, sólo que nunca las has aprendido.",
            "Me alegra que asistieras a tu reunión familiar diaria.",
            "Primero deberías dejar de usarla como un plumero.",
            "Qué apropiado, tú peleas como una vaca.",
            "Ya te están fastidiando otra vez las almorranas, ¿Eh?",
            "Ah, ¿Ya has obtenido ese trabajo de barrendero?",
            "Y yo tengo un SALUDO para ti, ¿Te enteras?",
            "Te habrá enseñado todo lo que sabes.",
            "¿TAN rápido corres?",
            "Me haces pensar que alguien ya lo ha hecho.",
            "Quería asegurarme de que estuvieras a gusto conmigo.",
            "Qué pena me da que nadie haya oído hablar de ti",
            "¿Incluso antes de que huelan tu aliento?",
            "Estaría acabado si la usases alguna vez.",
            "Espero que ya hayas aprendido a no tocarte la nariz."));

    /**
     * Constructor que inicializa el protocolo y la comunicación por socket; ademas del registro Log
     * @param manual variable que indica el modo de juego, si manual=true o automatico (manual=false)
     * @param socket variable que indica el socket por el que se comuicará con el servidor
     * @see Protocolo
     * @see ComUtils
     * @see BufferedReader
     * @throws IOException  lanzamos excepción para arriba
     */
    public Game_Server(boolean manual, Socket socket) throws IOException {
        this.manual = manual;
        this.protocolo = new Protocolo(new ComUtils(socket));
        String nameThread=this.getName();
        this.logs=new Logs("Server"+nameThread+".log");
        this.socket=socket;
        this.player_servidor = new Player_Servidor();
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));



    /**
     * Método que reseta la partida para no tener que introducir el HELLO de nuevo sino ir directamente a
     * introducir el SECRET y generar y enviar el HASH
     */
    public void resetEstado_newPartida (){
        estado=states.c_Hello_Enviado;
        this.ganados=0;
        this.perdidos=0;
        this.finalizado=false;
        this.player_servidor.init2_1Insults2_1Comebacks(1);
    }

    /**
     * Método que se encarga de mandar el HELLO. Para ello, hay que introducir/generar un ID y un nombre
     * El string generado lo enviará por el protocolo hacia el Servidor
     * @see BufferedReader#readLine()
     * @see Protocolo#write_mensage
     * @see Logs#mensaje
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    public void mandarHello () throws IOException, NoSuchAlgorithmException {
        String linea=""; //"SERVER";
        String ids="";
        Integer id;
        if(this.manual){
            System.out.println("Introducir id");
            ids = br.readLine();
            id=Integer.parseInt(ids);
            System.out.println("Introducir nombre");
            linea = ids +" "+ br.readLine();
        }else{
        //int rango='z'-'a' + 1;    //nos vale una porque tienen el mismo rango
            id=rand.nextInt(10000);
            linea+=id.toString()+" Server ";

            int rango='Z'-'A' + 1;
            for(int i=0;i<5;i++){
                int aleatorio=rand.nextInt(rango);
                linea+= (char) ('A'+aleatorio);
            }
        }
        this.miId=id;
        protocolo.write_mensage("HELLO "+linea);
        this.nomServer=linea.substring(linea.indexOf(' '));
        logs.mensaje(this.nomServer+ "-> "+"HELLO "+linea);
        //Cambiar estado
        estado=states.c_Hello_Enviado;
        System.out.println("estado "+estado);
        System.out.println("Enviado: "+"HELLO "+linea);

    }


    /**
     * Metodo que retorna un boleano indicando si ha perdido o ganado el Servidor
     * @return retorna un boleano indicando si ha ganado el Servidor (true) o si ha perdido (false)
     * @see Game_Server#gestionEstado()
     * @see Game_Server#recibirEntrada()
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     * @throws MalformedString lanzamos excepción para arriba
     */
    public boolean gamebucle() throws IOException, NoSuchAlgorithmException, MalformedString {
        while (!this.finalizado){
            System.out.println("Voy a gestionEstado");
            gestionEstado();
            System.out.println("Voy a recibirEntrada");
            recibirEntrada();
        }
        if (this.ganados==this.puntos) {
            return true;
        }
        return false;
    }

    /**
     * Método para introducir/generar un Secret y enviar por el protocolo el hash
     * (en el protocolo se hará el MessageDigest para generar y codificar el HASH)
     * @see Protocolo#write_mensage
     * @see BufferedReader#readLine()
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    public void introducirSecret_GenerarHash_EnviarHash() throws IOException, NoSuchAlgorithmException {
        //String linea="11111";
        String linea;
        if (this.manual) {
            System.out.println("Introducir numero secreto");
            linea = br.readLine();

        } else {
            //TODO implementar do while en caso que sea igual al de cliente -> casi imposible
            linea = String.valueOf(rand.nextInt(10000));
        }

        protocolo.write_mensage("HASH "+linea); //funcion en protocolo lo convertirá a hash correspondiente
        logs.mensaje(this.nomServer+ "-> "+"HASH "+linea);
        //System.out.println("Enviado: "+"HASH "+linea);
        estado=states.c_Hash_Enviado;
        System.out.println("estado "+estado);
        this.miSecret=Integer.parseInt(linea);
    }

    /**
     * Método para enviar el SECRET por el protocolo
     * @see Protocolo#write_mensage
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    public void mandarSecreto() throws IOException, NoSuchAlgorithmException {
        protocolo.write_mensage("SECRET "+this.miSecret.toString());
        logs.mensaje(this.nomServer+ "-> "+"SECRET "+this.miSecret.toString());
    }

    /**
     * Método de la máquina de estados encargado de mandar el hello, el hash, el secret y el insulto
     * @see Game_Server#mandarHello()
     * @see Game_Server#introducirSecret_GenerarHash_EnviarHash()
     * @see Protocolo#write_mensage
     * @see Logs#mensaje
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    private void gestionEstado() throws IOException, NoSuchAlgorithmException {
        if (estado==states.c_Hello_Recibido){
            //mandar hello
            mandarHello();
            estado=states.c_Hello_Enviado;
            System.out.println("estado "+estado);
        }else if(estado==states.c_Hash_Recibido){
            //introcudir secreto + generar hash + enviar hash
            introducirSecret_GenerarHash_EnviarHash();
        }else if(estado==states.Insulto_Enviar){
            //enviar el insulto
            ArrayList<String> array=this.player_servidor.ArrayInsultsparaElegir();
            int i=selectLista(array)-1;
            this.insultoEnviado=array.get(i);
            protocolo.write_mensage("INSULT "+this.insultoEnviado);
            logs.mensaje(this.nomServer+ "-> "+"INSULT "+this.insultoEnviado);
            estado=states.Insulto_Enviado;
        }
    }

    /**
     * Método encargado de leer el HELLO
     * @param s String recibido
     * @param s2 se corresponde con la cadena HELLO
     * @throws MalformedString lanzamos excepción para arriba
     */
    public void leer_Hello (String s, String s2) throws MalformedString {
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
            this.idRecibido=Integer.parseInt(id);
            this.nomRecibido=name;

            estado=states.c_Hello_Recibido;
            System.out.println("estado "+estado);
            return;
        }else{
            //Lanzar excpcion
            throw new MalformedString("Secuencia erronea, esperado: HELLO; recibido: " + s );
        }
    }

    /**
     * Método encargado de leer el HASH
     * @param s String recibido
     * @param s2 se corresponde con la cadena HASH
     * @throws MalformedString lanzamos excepción para arriba
     */
    public void leer_Hash (String s, String s2) throws MalformedString {
        //s2="HASH";
        if (s.substring(0,s2.length()).equals(s2)){
            this.hashRecibido=s.substring(s2.length()+1);
            estado=states.c_Hash_Recibido;
            System.out.println("estado "+estado);
            return;
        }else{
            //Lanzar excpcion
            throw new MalformedString("Secuencia erronea, esperado: HASH; recibido: " + s );
        }
    }

    /**
     * Método encargado de leer el SECRET
     * @param s String recibido
     * @param s2 se corresponde con la cadena SECRET
     * @see Game_Server#mandarSecreto()
     * @see Game_Server#valorar_estadobucle
     * @throws MalformedString lanzamos excepción para arriba
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    public void leer_Secret (String s, String s2) throws MalformedString, IOException, NoSuchAlgorithmException {
        //s2="SECRET";
        if (s.substring(0,s2.length()).equals(s2)){
            //coger su secret y valorar
            this.secretRecibido=s.substring(s2.length()+1);
            //enviar secret a client
            mandarSecreto();
            System.out.println("Enviado: "+"SECRET "+this.miSecret.toString());
            //estado=states.c_Secret_Enviado;
            //estado de comrobar la validez del secret (genera mismo hash)
            valorar_estadobucle();
            return;
        }else{
            //Lanzar excpcion
            throw new MalformedString("Secuencia erronea, esperado: SECRET; recibido: " + s );
        }
    }

    /**
     * Método encargado de leer el COMEBACK
     * @param s String recibido
     * @param s2 se corresponde con la cadena COMEBACK
     * @throws MalformedString lanzamos excepción para arriba
     */
    public void leer_Comeback (String s, String s2) throws MalformedString {
        //s2="COMEBACK";
        //recibir comback
        if (s.substring(0,s2.length()).equals(s2)){
            this.combackRecibido=s.substring(s2.length()+1);
        }else{
            //Lanzar excpcion
            throw new MalformedString("Secuencia erronea, esperado: COMEBACK; recibido: " + s );
        }
    }

    /**
     * Método encargado de valorar la lógica de victoria-derrota ante un comeback
     * y de enviar el correspondiente SHOUT por el protocolo
     * @see Protocolo#write_mensage
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    private void valorarComeback_Estado_VictoriasDerrotas () throws IOException, NoSuchAlgorithmException {
        //valoro el comback + cambiar estado + log victorios/derrotas
        int i=this.listaComback.indexOf(this.combackRecibido);
        this.player_servidor.addComeback(this.combackRecibido);
        String insultdelComeback=this.listaInsultos.get(i);
        if(this.insultoEnviado.equals(insultdelComeback)){
            //gana el
            this.perdidos++;
            if(this.perdidos==this.puntos){
                //estado de enviar un shout
                //protocolo.write_mensage(String.format("SHOUT ¡Has ganado, %s!",this.nomRecibido));
                estado=states.Shout_Esperar;
                System.out.println("estado "+estado);
                this.player_servidor.moverAListaCasadaConocidaPorAcierto(this.insultoEnviado,this.combackRecibido);
                return;
            }
            //estado de esperar el siguiente insulto
            this.player_servidor.moverAListaCasadaConocidaPorAcierto(this.insultoEnviado,this.combackRecibido);
            estado=states.Insulto_Esperar;
            System.out.println("estado "+estado);
            return;
        }else{
            //gano yo
            this.ganados++;
            if(this.ganados==this.puntos){
                //enviar un shout
                //protocolo.write_mensage(String.format("SHOUT ¡He ganado, %s!",this.nomRecibido));
                estado=states.Shout_Esperar;
                System.out.println("estado "+estado);
                return;
            }
            //estado de enviar un nuevo insulto
            estado=states.Insulto_Enviar;
            System.out.println("estado "+estado);
            return;
        }
    }

    /**
     * Método encargado de leer el INSULT y de valorar la lógica de victoria-derrota ante un insulto
     * ademas de enviar el correspondiente SHOUT por el protocolo
     * @param s String recibido
     * @param s2 se corresponde con la cadena INSULT
     * @see Game_Server#selectLista
     * @see Protocolo#write_mensage
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     * @throws MalformedString lanzamos excepción para arriba
     */
    public void leer_Insulto (String s, String s2) throws IOException, NoSuchAlgorithmException, MalformedString {
        //s2="INSULT";
        if (s.substring(0,s2.length()).equals(s2)){
            this.insultoRecibido=s.substring(s2.length()+1);
            this.player_servidor.addInsult(this.insultoRecibido);
            //seleccionar comback
            System.out.println("Recibido Insulto: "+this.insultoRecibido);
            //ArrayList<String> list=this.selccionar3Combacks();
            ArrayList<String> list=this.player_servidor.ArrayComebacksparaElegir(this.insultoRecibido);
            int i=this.selectLista(list);
            this.comebackEnviar=list.get(i-1);
            //enviar comback
            protocolo.write_mensage("COMEBACK "+this.comebackEnviar);
            logs.mensaje(this.nomServer+ "-> "+"COMEBACK "+this.comebackEnviar);
            System.out.println("Enviado: COMEBACK "+this.comebackEnviar);
            //valoro el comback + cambiar estado + log victorios/derrotas
            int indCom=this.listaComback.indexOf(this.comebackEnviar);
            int indIns=this.listaInsultos.indexOf(this.insultoRecibido);
            if(indCom!=indIns){
                //gana el
                this.perdidos++;
                if(this.perdidos==this.puntos){
                    //estado de enviar un shout y esperar su shout
                    //protocolo.write_mensage(String.format("SHOUT ¡Has ganado, %s!",this.nomRecibido));
                    estado=states.Shout_Esperar;
                    System.out.println("estado "+estado);
                    return;
                }
                estado=states.Insulto_Esperar;
                System.out.println("estado "+estado);
                return;
            }else{
                //gano yo
                this.ganados++;
                if(this.ganados==this.puntos){
                    //estado de enviar un shout y esperar su shout
                    //protocolo.write_mensage(String.format("SHOUT ¡He ganado, %s!",this.nomRecibido));
                    this.player_servidor.moverAListaCasadaConocidaPorAcierto(this.insultoRecibido,this.comebackEnviar);
                    estado=states.Shout_Esperar;
                    System.out.println("estado "+estado);
                    return;
                }
                //como hemos acertado, añadimos nuestro comeback a la lista conocida por ambos
                this.player_servidor.moverAListaCasadaConocidaPorAcierto(this.insultoRecibido,this.comebackEnviar);
                //estado de enviar un nuevo insulto
                estado=states.Insulto_Enviar;
                System.out.println("estado "+estado);
                return;
            }
        }else{
            //Lanzar excpcion
            throw new MalformedString("Secuencia erronea, esperado: INSULT; recibido: " + s );
        }
    }

    /**
     * Método encargado de leer el SHOUT
     * @param s String recibido
     * @param s2 se corresponde con la cadena SHOUT
     * @throws MalformedString lanzamos excepción para arriba
     */
    private void leer_Shout (String s, String s2) throws MalformedString {
        //s2="SHOUT";
        if (!s.substring(0,s2.length()).equals(s2)) {
            throw new MalformedString("Secuencia erronea, esperado: SHOUT; recibido: " + s);
        }
    }

    /**
     * Método para enviar el shout (el normal o el Mele) teniendo en cuenta la lógica de partidas y duelos ganados
     * @see Protocolo#write_mensage
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    public void enviar_Shout () throws IOException, NoSuchAlgorithmException {
        //enviar el shout
        if(this.perdidos==this.puntos){
            //estado de enviar un shout y esperar su shout
            if (this.client_win_count==(this.duelo-1)){
                String m=String.format("SHOUT ¡Has ganado, %s. Eres tan bueno que podrias luchar contra la Sword Master de la isla Mêlée!",this.nomRecibido);
                protocolo.write_mensage(m);
                logs.mensaje(this.nomServer+ "-> "+m);
                System.out.println("Mensaje enviado por Socket: "+m);
            }else{
                String m=String.format("SHOUT ¡Has ganado, %s!",this.nomRecibido);
                protocolo.write_mensage(m);
                logs.mensaje(this.nomServer+ "-> "+m);
                System.out.println("Mensaje enviado por Socket: "+ m);
            }
        } else{
            //estado de enviar un shout y esperar su shout
            protocolo.write_mensage(String.format("SHOUT ¡He ganado, %s!",this.nomRecibido));
            logs.mensaje(this.nomServer+ "-> "+String.format("SHOUT ¡He ganado, %s!",this.nomRecibido));
            System.out.println(String.format("Mensaje enviado por Socket: SHOUT ¡He ganado, %s!",this.nomRecibido));

        }
        this.finalizado=true;
        //fin partida
    }

    /**
     * Metodo encargado de la máquina de estados en la entrada de datos
     * @see Protocolo#convertirByteStr()
     * @see Game_Server#gamebucle
     * @see Game_Server#leer_Hello
     * @see Game_Server#leer_Hash
     * @see Game_Server#leer_Secret
     * @see Game_Server#leer_Insulto
     * @see Game_Server#leer_Comeback
     * @see Game_Server#valorarComeback_Estado_VictoriasDerrotas
     * @see Game_Server#leer_Shout
     * @see Game_Server#enviar_Shout
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     * @throws MalformedString lanzamos excepción para arriba
     */
    private void recibirEntrada () throws IOException, NoSuchAlgorithmException, MalformedString {
        String s="";
        try{
            this.socket.setSoTimeout(15000); //15000 ms = 15 seg   //exit por timeout
            s=protocolo.convertirByteStr();
        }catch (SocketTimeoutException e) {
            System.out.println(e.getMessage());
            s="ERROR ¡Me he candado de esperar tus mensajes, mequetrefe! ¡Hasta la vista!";
            protocolo.write_mensage(s);
            logs.mensaje(this.nomRecibido+ "-> "+s);
            this.socket.close();
        }

        logs.mensaje(this.nomRecibido+ "-> "+s);
        System.out.println("Recibido por socket: "+s);
        String s2="";
        //estado de enviar hello
        //estado de leer Hello
        if (estado==states.s_Hello_Espera){
            s2="HELLO";
            leer_Hello(s,s2);
         //estado de  escribir hash
         //estado de enviar hash
         //estado de recibir hash
        }else if(estado==states.c_Hello_Enviado){
            s2="HASH";
            leer_Hash(s,s2);
         //estado de enviar el secret
         //estado de leer el secret
        }else if(estado==states.c_Hash_Enviado){
            s2="SECRET";
            leer_Secret(s,s2);
        //estado de recibir el insulto y valorar el comback
        }else if(estado==states.Insulto_Esperar){
            //recibir insulto
            s2="INSULT";
            leer_Insulto(s,s2);
        }else if(estado==states.Insulto_Enviado){
            s2="COMEBACK";
            leer_Comeback(s,s2);
            //valoro el comback + cambiar estado + log victorios/derrotas
            valorarComeback_Estado_VictoriasDerrotas();

        }else if(estado==states.Shout_Esperar){
            //System.out.println(s);
            s2="SHOUT";
            leer_Shout(s,s2);
            enviar_Shout();
            //fin partida
        }
    }

    /**
     * Método que verifica que el secret recibido genera el mismo hash que el hash recibido.
     * En caso de que ambos hashes coincidan, llamará a gestionSumaSecret para ver quien empieza a insultar
     * @see MessageDigest
     * @see Game_Server#gestionSumaSecret
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     * @throws IOException lanzamos excepción para arriba
     * @throws MalformedString lanzamos excepción para arriba
     */
    private void valorar_estadobucle() throws NoSuchAlgorithmException, IOException, MalformedString {
        //comprobar que el secret genera el mismo hash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(this.secretRecibido.getBytes());
        String s="";
        for (int i=0;i<(32);i++){
            s+=String.format(" %02X", encodedhash[i]);    //conversion a hexadecimal
        }
        s=s.substring(1);
        System.out.println("ESTOY en valorar_estadobucle suyo calculado: ["+s+"] Suyo recibido: ["+this.hashRecibido+"]");
        if(s.equals(this.hashRecibido)){
            //Válido
            System.out.println("Son iguales voy a gestionSumaSecret con: "+this.miSecret.toString()+" y "+this.secretRecibido);
            gestionSumaSecret(this.miSecret,Integer.parseInt(this.secretRecibido),this.miId,this.idRecibido);
        }else{
            throw new MalformedString("El hash calculado no coincide con el recibido; Hash reibido:"
                    +this.hashRecibido +"; hash calculado: " +s);
        }
    }

    /**
     * Método que implementa la logica de dados ambos secretos, quien empieza a insultar
     * @param secretMio integer que corresponde a mi secreto
     * @param secretSuyo integer que corresponde al secreto que he recibido
     * @param idMio integer que corresponde a mi id
     * @param idSuyo integer que corresponde al id del cliente
     * @see Game_Server#valorar_estadobucle()
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    private void gestionSumaSecret(Integer secretMio, Integer secretSuyo, Integer idMio, Integer idSuyo) throws IOException, NoSuchAlgorithmException {
        Integer sumaSecret=secretMio+secretSuyo;
        System.out.println("ESTOY en gestionSumaSecret mio: "+secretMio.toString()+" suyo: "+secretSuyo.toString());
        if (sumaSecret%2==0){
            if(idMio<idSuyo){
                //empiezo yo a insultar
                //escirbir insulto
                System.out.println("Voy a enviar insulto");
                //estado de enviar un nuevo insulto
                estado=states.Insulto_Enviar;
                System.out.println("estado "+estado);
            }else{
                //empieza el
                //leer insulto
                System.out.println("Recibir insulto");
                estado=states.Insulto_Esperar;
                System.out.println("estado "+estado);
            }
        }else{
            if(idMio>idSuyo){
                //empiezo yo a insultar
                //escirbir insulto
                System.out.println("Voy a enviar insulto");
                //estado de enviar un nuevo insulto
                estado=states.Insulto_Enviar;
                System.out.println("estado "+estado);
            }else{
                //empieza el
                //leer insulto
                System.out.println("Recibir insulto");
                estado=states.Insulto_Esperar;
                System.out.println("estado "+estado);
            }
        }
    }

    /**
     * Metodo que retorna el indice del insulto o comeback elegido
     * @param list lista de insultos o comebacks
     * @see Game_Server#gestionEstado
     * @see Game_Server#leer_Insulto
     * @return retorna el indice correspondiente al insulto o comeback escogido
     * @throws IOException lanzamos excepción para arriba
     */
    private int selectLista(ArrayList<String> list) throws IOException {
        int i=1;
        int res = 0;
        String linea;
        for (String s:list){
            System.out.println(String.format("%2d - %s",i++,s));
        }

        if (this.manual){
            System.out.println("Introduzca numero de la opción");
            linea = br.readLine();
            res=Integer.parseInt(linea);
        }else {
            //Random rand = new Random();

            res = 1 + rand.nextInt(list.size()); // si size es 3, me da en tre 0 y 2, por eso +1
            // debe generar numero entre 1  size of list
            System.out.println("Opción automatica: " + res);
        }

        return res;
    }

    /**
     * Método para obtener el secret del Server. Unicamente es utilizado en el GameTest
     * @param miSecret integer asociado al secreto del Server
     */
    public void setMiSecret(Integer miSecret) {
        this.miSecret = miSecret;
    }
}
