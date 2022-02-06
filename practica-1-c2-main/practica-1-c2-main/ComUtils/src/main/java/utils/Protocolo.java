package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
* Esta clase define el Protocolo que se utilizará para la comuicación entre los distintos sockets
*/
public class Protocolo {
    ComUtils comUtils;

    /**
     * Constructor de la clase
     * @param comUtils se le pasa la clase comUtils debido a que conteine los métodos para trabajar a nivel de byte
     * @see ComUtils métodos de la clase que serán usados por el Protcolo
     */
    public Protocolo(ComUtils comUtils) {
        this.comUtils = comUtils;
    }

    /**
     * Método que leerá el byte correspondiente del mensaje enviado por el socket
     * @see ComUtils#leer_byte
     * @return retorna el byte correspondiente al opcode del mensaje
     */
    public byte leer_cabecera() throws IOException {
        return this.comUtils.leer_byte();
    }

    /**
     * Método que  escribirá el byte correspondiente del mensaje por el socket
     * @param b Byte que debe escribir
     * @see ComUtils#write_byte
     */
    public void write_cabecera(byte b) throws IOException{
        this.comUtils.write_byte(b);
    }

    /**
     * Método que leerá los bytes y los escribirá en el buffer hasta que detecte un 0,
     * que es nuestra parada de lectura según el protocolo. Convertirá el array de bytes del
     * buffer en un string y lo retornará
     * @see ByteArrayOutputStream
     * @see ComUtils#leer_byte
     * @return Retorna todo el mensaje hasta el 0 que es nuestra parada según el protocolo
     */
    public String leer_str () throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte b;
        while ((b=this.comUtils.leer_byte()) != 0){
            buffer.write(b);
        }
        return new String(buffer.toByteArray(),StandardCharsets.ISO_8859_1);    //yo puedo leer en codificación cualquiera
    }

    /**
     * Método que escribirá los bytes por el socket y añadirá un 0 como final de transmision
     * @param s String que hay que transformar a secuencia de bytes para enviar
     * @see ComUtils#write_byte
     * @see ComUtils#flush
     */
    public void write_str (String s) throws IOException{
        byte[] b = s.getBytes();
        for (byte c:b){
            this.comUtils.write_byte(c);
        }
        this.comUtils.write_byte((byte) 0);
        //Verifica que se hayan enviado
        this.comUtils.flush();
    }

    /**
     * Método que leerá el hash mediante la llamada de leer el hash en el comUtils
     * @see ComUtils#leer_hash
     * @return Retorna el hash como un array de bytes
     */
    public byte[] leer_hash () throws IOException {
        return comUtils.leer_hash();
    }

    /**
     * Método que escribirá el hash mediante la llamada de escribir el hash en el comUtils
     * @param b Array de bytes que se tienen que enviar
     * @see ComUtils#write_hash
     */
    public void write_hash (byte[] b) throws IOException{
        this.comUtils.write_hash(b);
    }

    /**
     * Método especial para la versión Cliente-VS-Cliente (Servidor modo 2 jugaores)
     * Dado un String que será todo el mensaje, debe enviar el opcode (cabecera)
     * y despues pasar el mensaje String a bytes para enviarlo por el socket
     * Para ello, definimos nuestro servidor en hexadecimal, y en consecuancia
     * hacemos el split por los separadores de espacio
     * @param s String que debe ser transformado a bytes para poderlo enviar
     * @see Protocolo#write_cabecera
     * @see Protocolo#write_hash
     */
    public void send_hash(String s) throws IOException {
        this.write_cabecera((byte) 2);
        byte[] b=new byte[32];
        String[] desglosado = s.split(" "); //TODO nuestro servidor lo lee en hexadecimal
        for (int i=1;i<33;i++){
            //pasar del desglosado [i] al b [i-1]
            b[i-1]= (byte) (Integer.parseInt(desglosado[i], 16));
        }
        this.write_hash(b);
    }

    /**
     * Método auxiliar de leer_mensage para el tratamiento especial del HASH
     * @see Protocolo#leer_mensage()
     */
    public String convertirByteStr () throws IOException, NoSuchAlgorithmException {
        byte[] b=this.leer_mensage();
        if (b[3] == 'H'){
            String s="HASH";
            for (int i=5;i<(32+5);i++){
                s+=String.format(" %02X", b[i],StandardCharsets.ISO_8859_1);    //yo puedo leer en codificación cualquiera
            }
            return s;
        }
        return new String(b,StandardCharsets.ISO_8859_1);   //yo puedo leer en codificación cualquiera
    }


    /**
     * Método que separará y clasificará los mensajes según el tipo al que pertenezcan
     * (HELLO,HASH,SECRET,INSULT,COMEBACK,SHOUT,ERROR)
     * Irá leyendo bytes hasta encontrar un 0 de parada o en caso de tener un tamaño concrepto (HASH) llegar al maximo
     * @see Protocolo#leer_str
     * @see ComUtils#read_int32
     * @return Retorna el array de bytes corespondientes al mensaje
     */
    public byte[] leer_mensage() throws IOException, NoSuchAlgorithmException {
        //determinar cabezera

        byte c = this.leer_cabecera();
        byte[] resu = null;
        String s;

        if(c == 1) {
            s ="HELLO ";
            Integer id=this.comUtils.read_int32();
            s+= id.toString() + " " +this.leer_str();;
            resu=s.getBytes(StandardCharsets.ISO_8859_1);   //yo puedo leer en codificación cualquiera
        }

        else if(c == 2) {
            byte[] b= "HASH ".getBytes(StandardCharsets.ISO_8859_1);    //yo puedo leer en codificación cualquiera
            byte[] d=this.leer_hash();
            resu = new byte[b.length + d.length];
            System.arraycopy(b, 0, resu, 0, b.length);
            System.arraycopy(d, 0, resu, b.length, d.length);
        }

        else if(c == 3) {
            s ="SECRET "+this.leer_str();
            resu=s.getBytes(StandardCharsets.ISO_8859_1);   //yo puedo leer en codificación cualquiera
        }

        else if(c == 4) {
            s ="INSULT "+this.leer_str();
            resu=s.getBytes(StandardCharsets.ISO_8859_1);   //yo puedo leer en codificación cualquiera
        }

        else if(c == 5) {
            s ="COMEBACK "+this.leer_str();
            resu=s.getBytes(StandardCharsets.ISO_8859_1);   //yo puedo leer en codificación cualquiera
        }

        else if(c == 6) {
            s ="SHOUT "+this.leer_str();
            resu=s.getBytes(StandardCharsets.ISO_8859_1);   //yo puedo leer en codificación cualquiera
        }

        else if(c == 7) {
            s ="ERROR "+this.leer_str();
            resu=s.getBytes(StandardCharsets.ISO_8859_1);   //yo puedo leer en codificación cualquiera
        }
        else {
            System.out.println("Caso excepción revisar Protocol.java swicth-case leer_mensage");
            s ="ERROR ¡Barco hundido, abandonen el barco!";
            resu=s.getBytes(StandardCharsets.ISO_8859_1);   //yo puedo leer en codificación cualquiera
        }

        return resu;
    }

    /**
     * Método que separará y clasificará los mensajes según el tipo al que pertenezcan
     * (HELLO,HASH,SECRET,INSULT,COMEBACK,SHOUT,ERROR)
     * Irá escribiendo el mensaje introducido por parámetro.
     * En el caso del HASH, se tendrá que llagar al MessageDigest para generar y codificarlo
     * @param m String correspondiente al mensaje a enviar
     * @see Protocolo#write_cabecera
     * @see Protocolo#write_str
     * @see ComUtils#write_int32
     * @see MessageDigest
     * @see Protocolo#write_hash
     */
    public void write_mensage (String m) throws IOException, NoSuchAlgorithmException {
        //determinar cabezera
        String[] desglosado = m.split(" ");
        //Si el mensaje es un HELLO
        if(desglosado[0].equals("HELLO")){
            this.write_cabecera((byte) 1);
            //Obtengo todo el mensaje
            String msg=m.substring("HELLO".length()+1);
            //Obtengo el indice
            String ids=msg.substring(0,msg.indexOf(' '));
            msg=msg.substring(msg.indexOf(' ')+1);
            Integer idi=Integer.parseInt(ids);
            this.comUtils.write_int32(idi);
            this.write_str(msg);
        //Si el mensaje es un HASH
        }else if (desglosado[0].equals("HASH")){
            this.write_cabecera((byte) 2);
            String secret=m.substring("HASH".length()+1);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(secret.getBytes());    //maven dependencies
            this.write_hash(encodedhash);
        //Si el mensaje es un SECRET
        }else if (desglosado[0].equals("SECRET")){
            this.write_cabecera((byte) 3);
            String msg=m.substring("SECRET".length()+1);
            this.write_str(msg);
        //Si el mensaje es un INSULT
        }else if (desglosado[0].equals("INSULT")){
            this.write_cabecera((byte) 4);
            String msg=m.substring("INSULT".length()+1);
            this.write_str(msg);
        //Si el mensaje es un COMEBACK
        }else if (desglosado[0].equals("COMEBACK")){
            this.write_cabecera((byte) 5);
            String msg=m.substring("COMEBACK".length()+1);
            this.write_str(msg);
        //Si el mensaje es un SHOUT
        }else if (desglosado[0].equals("SHOUT")){
            this.write_cabecera((byte) 6);
            String msg=m.substring("SHOUT".length()+1);
            this.write_str(msg);
        //Si el mensaje es un ERROR
        }else if (desglosado[0].equals("ERROR")){
            this.write_cabecera((byte) 7);
            String msg=m.substring("ERROR".length()+1);
            this.write_str(msg);
        //Caso excepcion default
        }else{
            //ERROR excepcion
            System.out.println("Caso excepción revisar Protocol.java swicth-case write_message");
        }
    }



}
