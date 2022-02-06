import utils.ComUtils;
import utils.Protocolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Clase que contiene toda la lógica el juego (máquina de estados) para el Cliente
 * @see Client
 * @see Game_Client
 * @see Socket
 * @see Protocolo
 * @see Random
 * @see Player_Client
 * @see ArrayList
 * @see ComUtils
 * @see IOException
 * @see NoSuchAlgorithmException
 * @see MalformedString
 */
public class Game_Client {
    /**
     * Variables
     * @see Socket
     * @see Protocolo
     * @see Random
     */
    private Socket socket;
    private Protocolo protocolo;
    private boolean manual;  //Si es falso entonces es automatico
    private states estado=states.c_Inicial; //estado 0 del pdf
    private String hashRecibido;
    private String nomRecibido;
    private Integer idRecibido;
    private Integer miId;
    private String secretRecibido;
    private String combackRecibido;
    private String comebackEnviar;
    private String insultoEnviado;
    private String insultoRecibido;
    private Integer miSecret=0;
    private int ganados=0;
    private int perdidos=0;
    private boolean finalizado=false;
    private int client_win_count = 0;
    private int server_win_count = 0;
    private boolean result_game;
    private int numJuego=1;
    private Random rand = new Random();
    private Player_Client player_client;
    private final int duelo=3;
    private final int puntos=2;

    /**
     * Estados de la maquina de estados del Cliente
     */
    private enum states{
        c_Inicial,
        c_Hello_Enviado,
        c_Hello_Recibido,
        c_Hash_Enviado,
        c_Hash_Recibido,
        c_Secret_Enviado,
        c_Secret_Recibido,
        Insulto_Enviado,
        Insulto_Enviar,
        Comback_Recibido,
        Insulto_Esperar,
        Insulto_Recibido,
        Comback_Esperar,
        Comback_Enviado,
        Shout_Esperar
    }

    /**
     * Lista de insultos que será utilizada por clases superiores
     * @see Player_Client
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
     * @see Player_Client
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
     * Constructor que inicializa el protocolo y la comunicación por socket
     * @param manual variable que indica el modo de juego, si manual=true o automatico (manual=false)
     * @param socket variable que indica el socket por el que se comuicará con el servidor
     * @param player_client variable que guadará el player en el game
     * @see Protocolo
     * @see Player_Client
     * @see ComUtils
     * @see BufferedReader
     * @throws IOException lanzamos excepción para arriba
     */
    public Game_Client(boolean manual, Socket socket,Player_Client player_client) throws IOException {
        this.manual = manual;
        this.socket=socket;
        protocolo = new Protocolo(new ComUtils(socket));
        this.player_client= player_client;

    }
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Método principal de la clase
     * Método que controla el flujo de la partida global (contadores de victorias)  y el reset entre partidas
     * @see Game_Client#gamebucle()
     * @see Game_Client#resetEstado_newPartida()
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     * @throws MalformedString lanzamos excepción para arriba
     * @throws IOException lanzamos excepción para arriba
     */
    public void Run() throws NoSuchAlgorithmException, MalformedString, IOException {
        while (client_win_count < this.duelo && server_win_count < this.duelo) {
            System.out.println("Partida: " + numJuego);//imprimipos al usuario en que partida esta
            result_game = this.gamebucle();
            if (result_game) {
                client_win_count += 1;
            } else {
                server_win_count += 1;
            }
            this.resetEstado_newPartida();
            numJuego += 1;
        }
        client_win_count = 0;
        server_win_count = 0;
        numJuego = 1;
    }

    /**
     * Método que reseta la partida para no tener que introducir el HELLO de nuevo sino ir directamente a
     * introducir el SECRET y generar y enviar el HASH
     * @see Player_Client#init2_1Insults2_1Comebacks
     */
    public void resetEstado_newPartida (){
        estado=states.c_Hello_Recibido;
        this.ganados=0;
        this.perdidos=0;
        this.finalizado=false;
        this.player_client.init2_1Insults2_1Comebacks(1);
    }

    /**
     * Método que se encarga de mandar el HELLO. Para ello, hay que introducir/generar un ID y un nombre
     * El string generado lo enviará por el protocolo hacia el Servidor
     * @see BufferedReader#readLine()
     * @see Protocolo#write_mensage
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     * @throws MalformedString lanzamos excepción para arriba
     */
    public void mandarHello () throws IOException, NoSuchAlgorithmException, MalformedString {
        String linea=""; //si es automatico
        String name="";
        Integer id;
        int cuentaAtras=3;
        if(this.manual){ //si es manual
            System.out.println("Introducir id");
            do{
                try{
                    linea = br.readLine();
                    id=Integer.parseInt(linea);
                }catch (NumberFormatException ex){
                    id=-1;
                    System.out.println("El id debe ser un número válido. Intentos restantes: "+ cuentaAtras--);
                }
            }while(id<0 && cuentaAtras>=0);
            if(cuentaAtras<0){
                throw new MalformedString("Número de id invalido");
            }
            System.out.println("Introducir nombre");
            name=br.readLine();
            if(name.length()==0){
                //asignar uno random porque el introducido es nulo (es un enter)
                name="Rename_Client_";
                int rango='Z'-'A' + 1;
                for(int i=0;i<5;i++){
                    int aleatorio=rand.nextInt(rango);
                    name+= (char) ('A'+aleatorio);
                }
                System.out.println("Error al introducir el nombre, nuevo nombre asignado: "+name);
            }

            linea = linea +" "+ name;
            //preguntar si es el mimso player
            this.player_client=this.player_client.mismoPlayer(id,name);
            if(this.player_client==null){
                this.player_client=new Player_Client();
                this.player_client.setId(id);
                this.player_client.setName(name);
            }
            this.player_client.setId(id);
            this.player_client.setName(name);
        }else{  //si es automático
            //int rango='z'-'a' + 1;    //nos vale una porque tienen el mismo rango
            id=rand.nextInt(10000);
            linea+=id.toString()+" ";
            name="Client_";
            int rango='Z'-'A' + 1;
            for(int i=0;i<5;i++){
                int aleatorio=rand.nextInt(rango);
                name+= (char) ('A'+aleatorio);
            }
            //preguntar si es el mimso player
            this.player_client=this.player_client.mismoPlayer(id,name);
            if(this.player_client==null){
                this.player_client=new Player_Client();
                this.player_client.setId(id);
                this.player_client.setName(name);
            }
            linea+=name;
        }
        this.miId=player_client.getId();
        protocolo.write_mensage("HELLO "+linea); //escribimos hello
        //Cambiar estado
        estado=states.c_Hello_Enviado; //actualizamos estado
    }

    /**
     * Metodo que retorna un boleano indicando si ha perdido o ganado el Cliente
     * @return retorna un boleano indicando si ha ganado el Cliente (true) o si ha perdido (false)
     * @see Game_Client#gestionEstado()
     * @see Game_Client#recibirEntrada()
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     * @throws MalformedString lanzamos excepción para arriba
     */
    public boolean gamebucle() throws IOException, NoSuchAlgorithmException, MalformedString {
        while (!this.finalizado){
            System.out.println("Voy a gestionEstado con estado "+estado);
            gestionEstado();
            System.out.println("Voy a recibirEntrada con estado "+estado);
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
     * @throws MalformedString lanzamos excepción para arriba
     */
    public void introducirSecret_GenerarHash_EnviarHash() throws IOException, NoSuchAlgorithmException, MalformedString {
        //String linea="951247";
        String linea="";
        int cuentaAtras=3;
        if(this.manual){
            System.out.println("Introducir numero secreto");
            do{
                try{
                    linea = br.readLine();
                    this.miSecret=Integer.parseInt(linea);
                    if(this.miSecret<0){
                        this.miSecret=-1;
                        System.out.println("El secreto debe ser un número válido. Intentos restantes: "+ cuentaAtras--);
                    }
                }catch (NumberFormatException ex){
                    this.miSecret=-1;
                    System.out.println("El secreto debe ser un número válido. Intentos restantes: "+ cuentaAtras--);
                }
            }while(this.miSecret<0 && cuentaAtras>=0);
            if(cuentaAtras<0){
                throw new MalformedString("Número de secret invalido");
            }
        }else{

            linea = String.valueOf(rand.nextInt(10000));
            this.miSecret=Integer.parseInt(linea);
        }

        protocolo.write_mensage("HASH "+linea);
        estado=states.c_Hash_Enviado; //actualizamos el estado
    }

    /**
     * Método para enviar el SECRET por el protocolo
     * @see Protocolo#write_mensage
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    public void mandarSecreto() throws IOException, NoSuchAlgorithmException {
        protocolo.write_mensage("SECRET "+this.miSecret.toString());
    }

    /**
     * Método de la máquina de estados encargado de mandar el hello, el hash, el secret y el insulto
     * @see Game_Client#mandarHello()
     * @see Game_Client#introducirSecret_GenerarHash_EnviarHash()
     * @see Game_Client#mandarSecreto()
     * @see Protocolo#write_mensage
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     * @throws MalformedString lanzamos excepción para arriba
     */
    private void gestionEstado() throws IOException, NoSuchAlgorithmException, MalformedString {
        if (estado==states.c_Inicial){
            //mandar hello
            mandarHello();
            estado=states.c_Hello_Enviado; //cambio el estado al hello enviado
        }else if(estado==states.c_Hello_Recibido){
            //introducir sectret, generar y enviar hash
            introducirSecret_GenerarHash_EnviarHash();
            estado=states.c_Hash_Enviado;
        }else if(estado==states.c_Hash_Recibido){
            //emandar secreto
            mandarSecreto();
            estado=states.c_Secret_Enviado;
        }else if(estado==states.Insulto_Enviar){
            //enviar el insulto
            ArrayList<String> array=this.player_client.ArrayInsultsparaElegir();
            int i=selectLista(array)-1;
            this.insultoEnviado=array.get(i);
            protocolo.write_mensage("INSULT "+this.insultoEnviado);
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
        if (s.substring(0,s2.length()).equals(s2)){ //si en el protocolo la cabecera es 0, entonces es un HELLO
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
        if (s.substring(0,s2.length()).equals(s2)){ // si la cabecera es == HASH
            this.hashRecibido=s.substring(s2.length()+1);
            estado=states.c_Hash_Recibido;
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
     * @see Game_Client#valorar_estadobucle
     * @throws MalformedString lanzamos excepción para arriba
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    public void leer_Secret (String s, String s2) throws MalformedString, IOException, NoSuchAlgorithmException {
        //s2="SECRET";
        if (s.substring(0,s2.length()).equals(s2)){
            //coger su secret y valorar
            this.secretRecibido=s.substring(s2.length()+1);
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
        this.player_client.addComeback(this.combackRecibido);
        String insultdelComeback=this.listaInsultos.get(i);
        if(this.insultoEnviado.equals(insultdelComeback)){
            //gana el
            this.perdidos++;
            if(this.perdidos==this.puntos){
                //estado de enviar un shout
                if (this.server_win_count==(this.duelo-1)){
                    String m=String.format("SHOUT ¡Has ganado, %s. Eres tan bueno que podrias luchar contra la Sword Master de la isla Mêlée!",this.nomRecibido);
                    protocolo.write_mensage(m);
                    System.out.println("Mensaje enviado por Socket: "+m);
                }else{
                    String m=String.format("SHOUT ¡Has ganado, %s!",this.nomRecibido);
                    protocolo.write_mensage(m);
                    System.out.println("Mensaje enviado por Socket: "+ m);
                }
                //como hemos acertado, añadimos nuestro comeback a la lista conocida por ambos
                this.player_client.moverAListaCasadaConocidaPorAcierto(this.insultoEnviado,this.combackRecibido);
                estado=states.Shout_Esperar;
                return;
            }
            //como hemos acertado, añadimos nuestro comeback a la lista conocida por ambos
            this.player_client.moverAListaCasadaConocidaPorAcierto(this.insultoEnviado,this.combackRecibido);
            //estado de esperar el siguiente insulto
            estado=states.Insulto_Esperar;
            return;
        }else{
            //gano yo
            this.ganados++;
            if(this.ganados==this.puntos){
                //enviar un shout
                protocolo.write_mensage(String.format("SHOUT ¡He ganado, %s!",this.nomRecibido));
                System.out.println(String.format("Mensaje enviado por Socket: SHOUT ¡He ganado, %s!",this.nomRecibido));

                estado=states.Shout_Esperar;
                return;
            }
            //estado de enviar un nuevo insulto
            estado=states.Insulto_Enviar;
            return;
        }
    }

    /**
     * Método encargado de leer el INSULT y de valorar la lógica de victoria-derrota ante un insulto
     * ademas de enviar el correspondiente SHOUT por el protocolo
     * @param s String recibido
     * @param s2 se corresponde con la cadena INSULT
     * @see Game_Client#selectLista
     * @see Protocolo#write_mensage
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     * @throws MalformedString lanzamos excepción para arriba
     */
    public void leer_Insulto (String s, String s2) throws IOException, NoSuchAlgorithmException, MalformedString {
        //s2="INSULT";
        if (s.substring(0,s2.length()).equals(s2)){
            this.insultoRecibido=s.substring(s2.length()+1);
            System.out.println("Recibido Insulto: "+this.insultoRecibido);
            //Verificar que lo recibido es correcto
            if(!listaInsultos.contains(this.insultoRecibido)){
                protocolo.write_mensage("ERROR ¡Mensaje incompleto, grumete! ¡Hasta la vista!");
                socket.close();
            }
            this.player_client.addInsult(this.insultoRecibido);
            //seleccionar comback
            //ArrayList<String> list=this.selccionar3Combacks();
            ArrayList<String> list=this.player_client.ArrayComebacksparaElegir(this.insultoRecibido);
            int i=this.selectLista(list);
            this.comebackEnviar=list.get(i-1);
            //enviar comback
            protocolo.write_mensage("COMEBACK "+this.comebackEnviar);
            System.out.println("Enviado: COMEBACK "+this.comebackEnviar);
            //valoro el comback + cambiar estado + log victorios/derrotas
            int indCom=this.listaComback.indexOf(this.comebackEnviar);
            int indIns=this.listaInsultos.indexOf(this.insultoRecibido);
            if(indCom!=indIns){
                //gana el
                this.perdidos++;
                if(this.perdidos==this.puntos){
                    //estado de enviar un shout y esperar su shout
                    if (this.server_win_count==(this.duelo-1)){
                        String m=String.format("SHOUT ¡Has ganado, %s. Eres tan bueno que podrias luchar contra la Sword Master de la isla Mêlée!",this.nomRecibido);
                        protocolo.write_mensage(m);
                        System.out.println("Mensaje enviado por Socket: "+m);
                    }else{
                        String m=String.format("SHOUT ¡Has ganado, %s!",this.nomRecibido);
                        protocolo.write_mensage(m);
                        System.out.println("Mensaje enviado por Socket: "+ m);
                    }
                    estado=states.Shout_Esperar;
                    return;
                }
                estado=states.Insulto_Esperar;
                return;
            }else{
                //gano yo
                this.ganados++;
                if(this.ganados==this.puntos){
                    //estado de enviar un shout y esperar su shout
                    protocolo.write_mensage(String.format("SHOUT ¡He ganado, %s!",this.nomRecibido));
                    System.out.println(String.format("Mensaje enviado por Socket: SHOUT ¡He ganado, %s!",this.nomRecibido));
                    this.player_client.moverAListaCasadaConocidaPorAcierto(this.insultoRecibido,this.comebackEnviar);
                    estado=states.Shout_Esperar;
                    return;
                }
                //como hemos acertado, añadimos nuestro comeback a la lista conocida por ambos
                this.player_client.moverAListaCasadaConocidaPorAcierto(this.insultoRecibido,this.comebackEnviar);
                //estado de enviar un nuevo insulto
                estado=states.Insulto_Enviar;
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
    public void leer_Shout (String s, String s2) throws MalformedString {
        //s2="SHOUT";
        if (!s.substring(0,s2.length()).equals(s2)) {
            throw new MalformedString("Secuencia erronea, esperado: SHOUT; recibido: " + s);
        }
        this.finalizado=true;
    }

    /**
     * Metodo encargado de la máquina de estados en la entrada de datos
     * @see Protocolo#convertirByteStr()
     * @see Game_Client#gamebucle
     * @see Game_Client#leer_Hello
     * @see Game_Client#leer_Hash
     * @see Game_Client#leer_Secret
     * @see Game_Client#leer_Insulto
     * @see Game_Client#leer_Comeback
     * @see Game_Client#valorarComeback_Estado_VictoriasDerrotas
     * @see Game_Client#leer_Shout
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
            protocolo.write_mensage("ERROR ¡Me he candado de esperar tus mensajes, mequetrefe! ¡Hasta la vista!");
            this.socket.close();
        }
        System.out.println("Recibido por socket: "+s);
        String s2="";
        //estado de enviar hello
        //estado de leer Hello
        if (estado==states.c_Hello_Enviado){
            s2="HELLO";
            leer_Hello(s,s2);
         //estado de  escribir hash
         //estado de enviar hash
         //estado de recibir hash
        }else if(estado==states.c_Hash_Enviado){
            s2="HASH";
            leer_Hash(s,s2);
         //estado de enviar el secret
         //estado de leer el secret
        }else if(estado==states.c_Secret_Enviado){
            s2="SECRET";
            leer_Secret(s,s2);
         //estado de recibir el insulto y valorar el comback
        } else if(estado==states.Insulto_Esperar){
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
            //fin partida
        }
    }

    /**
     * Método que verifica que el secret recibido genera el mismo hash que el hash recibido.
     * En caso de que ambos hashes coincidan, llamará a gestionSumaSecret para ver quien empieza a insultar
     * @see MessageDigest
     * @see Game_Client#gestionSumaSecret
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     * @throws IOException lanzamos excepción para arriba
     * @throws MalformedString lanzamos excepción para arriba
     */
    private void valorar_estadobucle() throws NoSuchAlgorithmException, IOException, MalformedString {
        //comprobar que el secret genera el mismo hash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(this.secretRecibido.getBytes()); //TODO usar ASCII
        String s="";
        for (int i=0;i<(32);i++){
            s+=String.format(" %02X", encodedhash[i]);    //conversion a hexadecimal
        }
        s=s.substring(1);
        System.out.println("402");
        System.out.println("Hash Combrobado {"+s+"} Hash Recibido {"+this.hashRecibido+"}");
        if(s.equals(this.hashRecibido)){
            //Válido
            System.out.println("405");
            gestionSumaSecret(this.miSecret,Integer.parseInt(this.secretRecibido),this.miId,this.idRecibido);
        }else{
            System.out.println("408");
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
     * @see Game_Client#valorar_estadobucle()
     * @throws IOException lanzamos excepción para arriba
     * @throws NoSuchAlgorithmException lanzamos excepción para arriba
     */
    private void gestionSumaSecret(Integer secretMio, Integer secretSuyo, Integer idMio, Integer idSuyo) throws IOException, NoSuchAlgorithmException {
        Integer sumaSecret=secretMio+secretSuyo;
        if (sumaSecret%2==0){
            if(idMio<idSuyo){
                //empiezo yo a insultar
                //estado de enviar un nuevo insulto
                estado=states.Insulto_Enviar;
            }else{
                //empieza el
                //leer insulto
                estado=states.Insulto_Esperar;
            }
        }else{
            if(idMio>idSuyo){
                //empiezo yo a insultar
                //estado de enviar un nuevo insulto
                estado=states.Insulto_Enviar;
            }else{
                //empieza el
                //leer insulto
                estado=states.Insulto_Esperar;
            }
        }
    }


    /**
     * Metodo que retorna el indice del insulto o comeback elegido
     * @param list lista de insultos o comebacks
     * @see Game_Client#gestionEstado
     * @see Game_Client#leer_Insulto
     * @return retorna el indice correspondiente al insulto o comeback escogido
     * @throws IOException lanzamos excepción para arriba
     * @throws MalformedString lanzamos excepción para arriba
     */
    private int selectLista(ArrayList<String> list) throws IOException, MalformedString {
        int i=1;
        int res = 0;
        int indexList = list.size();
        String linea;
        for (String s:list){
            System.out.println(String.format("%2d - %s",i++,s));
        }

        if (this.manual){
            System.out.println("Introduzca numero de la opción");

            //linea = br.readLine();
            linea = controlIndex(indexList);
            res=Integer.parseInt(linea);

        }else {
            res = 1 + rand.nextInt(list.size()); // si size es 3, me da en tre 0 y 2, por eso +1
            // debe generar numero entre 1  size of list
            System.out.println("Opción automatica: " + res);
        }

        return res;
    }

    /**
     * Método para obtener el secret del Client. Unicamente es utilizado en el GameTest
     * @param miSecret integer asociado al secreto del Cliente
     */
    public void setMiSecret(Integer miSecret) {
        this.miSecret = miSecret;
    }

    /**
     * Funcion para verificar que el indic introducido por teclado es un indice válido
     * @param indexList indice de la lista como un int
     * @return retorna el indice de la lista como un string. Si retorna -1 entonces saltara por excepción
     * @throws MalformedString lanzamos excepción para arriba
     */
    public String controlIndex(int indexList) throws MalformedString {
        String linea = "";
        Integer index;
        int cuentaAtras = 3;

        do{
            try{
                linea = br.readLine();
                index=Integer.parseInt(linea);
                if(index > indexList || index <= 0){
                    index=-1;
                    System.out.println("La opcion introducida debe ser un número válido. Intentos restantes: "+ cuentaAtras--);
                }
            }catch (NumberFormatException | IOException ex){
                index=-1;
                System.out.println("La opcion introducida debe ser un número válido. Intentos restantes: "+ cuentaAtras--);
            }
        }while(index<0 && cuentaAtras>=0);
        if(cuentaAtras<0){
            throw new MalformedString("Número invalido");
        }

        return linea;
    }
}
