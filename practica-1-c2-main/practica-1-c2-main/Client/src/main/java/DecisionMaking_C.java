import java.util.Scanner;

/**
 * Metodo utilizado para la comprobación del test sobre el modo de ejecución
 * @deprecated  Ha sido suplido por un boleano para comprobar si es en automático o manual
 */
public class DecisionMaking_C {
    /**
     * Metodo que verifica si es manual o automatico
     * @deprecated Ha sido suplido por clases superiores
     * @param input int asociado al modo de partida
     * @return un boleano asociado a si es 1 el valor de entrada
     */
    public static boolean get_choice(int input){

        if(input == 1){
            return true;
        }
        else return false;

    }
}
