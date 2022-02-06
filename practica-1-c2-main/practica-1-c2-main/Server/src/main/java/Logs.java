import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Clase principal que creará un registro de toda la comunicación
 * @see Logger
 * @see FileHandler
 */
public class Logs {
    Logger logger = Logger.getLogger("Log_Sockets");
    FileHandler fh;

    /**
     * Método que almacenará en un fichero todos los datos introducidos
     * @param fichero nombre del fichero donde se guardan los datos
     * @see Logger
     * @see SimpleFormatter
     * @see IOException
     */
    public Logs(String fichero){
        try{
            this.fh = new FileHandler(fichero,true);
            fh.setEncoding("UTF-8");
            this.logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            this.fh.setFormatter(formatter);
        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Método que extrae y añade la información en el fichero
     * @param mensaje mensaje enviado para ser añadido en el log
     * @see SecurityException
     */
    void mensaje(String mensaje){
        try {
            logger.info(mensaje);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para cerrar el fichero
     * @see SecurityException
     */
    void cerrar(){
        try{
            this.fh.close();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


}
