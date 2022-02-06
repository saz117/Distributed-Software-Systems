/**
 * Clase utilizado para la gestión de los errores y excepciones
 * @see Exception
 */
public class MalformedString extends Exception {
    /**
     * Método utilizado para mostrar el error introducido por parametro
     * @param message mensage de error
     * @see Exception#Exception(String)
     */
    public MalformedString (String message) {
        super(message);
    }
}