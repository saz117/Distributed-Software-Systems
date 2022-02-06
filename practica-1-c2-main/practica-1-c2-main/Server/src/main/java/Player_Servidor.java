import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Player_Servidor {

    //aqui guardamos el registro de los instultos aprendidos y partidas ganadas

    //aqui guardamos el registro de los instultos aprendidos y partidas ganadas
    private Random rand = new Random();
    private int index=0;
    private ArrayList<String> matchedLearnedInsult; //listaInsultosAprendidaCasada
    private ArrayList<String> matchedLearnedComback; //listaComebackAprendidaCasada
    private ArrayList<String> learnedInsults; //listaInsultosAprendida
    private ArrayList<String> learnedCombacks; //listaComebackAprendida
    private ArrayList<String> matchedLearnedInsultKnownByBoth;  //ambos la conocemos
    private ArrayList<String> matchedLearnedCombackKnowByBoth;  //ambos la conocemos
    private final int initArray=2;

    //Constructor
    public Player_Servidor() {
        matchedLearnedInsult = new ArrayList();
        matchedLearnedComback = new ArrayList();
        learnedInsults = new ArrayList();
        learnedCombacks = new ArrayList();
        matchedLearnedInsultKnownByBoth= new ArrayList<>();
        matchedLearnedCombackKnowByBoth= new ArrayList<>();

        init2_1Insults2_1Comebacks(initArray);
    }


    //OKKEY
    public void init2_1Insults2_1Comebacks (int cantidad){
        //Coger de forma random 2 insultos de la lista total, y añadirlas a la lista de aprendidos
        //lo mismo para los comebaks

        int n;
        String insult;
        String comeback;
        int tam=Game_Server.listaInsultos.size();
        //verificar si el insulto ya está en la lista (para el primer caso, no lo estará)
        for (int i=0; i<cantidad; i++) {
            do {
                //coger un int random
                n = rand.nextInt(tam);
                //coger su string
                insult = Game_Server.listaInsultos.get(n);
                //verificar si el string está añadido en las aprendidas
            } while (matchedLearnedInsult.contains(insult) || matchedLearnedInsultKnownByBoth.contains(insult));
            //coger el comeback asociado
            comeback = Game_Server.listaComback.get(n);
            //añadir a las listas casadas
            moverAListaCasadaConocida(insult,comeback);
        }
    }

    private void moverAListaCasadaConocida (String insult, String comeback){
        //incluir en lista
        this.matchedLearnedInsult.add(insult);
        this.matchedLearnedComback.add(comeback);
        //mirar en las otras listas para borrarlo
        if(this.learnedInsults.contains(insult)){
            this.learnedInsults.remove(insult);
        }
        if(this.learnedCombacks.contains(comeback)){
            this.learnedCombacks.remove(comeback);
        }
    }

    //OKKEY
    public void moverAListaCasadaConocidaPorAcierto (String insult, String comeback){
        //incluir en lista
        this.matchedLearnedInsultKnownByBoth.add(insult);
        this.matchedLearnedCombackKnowByBoth.add(comeback);
        //mirar en las otras listas para borrarlo
        if(this.matchedLearnedInsult.contains(insult)){
            this.matchedLearnedInsult.remove(insult);
        }
        if(this.matchedLearnedComback.contains(comeback)){
            this.matchedLearnedComback.remove(comeback);
        }
        if(this.learnedInsults.contains(insult)){
            this.learnedInsults.remove(insult);
        }
        if(this.learnedCombacks.contains(comeback)){
            this.learnedCombacks.remove(comeback);
        }
    }

    public ArrayList<String> ArrayComebacksparaElegir (String insult) {
        ArrayList<String> array = new ArrayList<>();
        Integer idComeback;
        String comeback;
        //primero, verificamos si el insulto está en la lista de casados pues tendremos su comeback seguro
        if (matchedLearnedInsult.contains(insult)) {
            idComeback = matchedLearnedInsult.indexOf(insult);
            array.add(matchedLearnedComback.get(idComeback));
            return array;
        }
        if (matchedLearnedInsultKnownByBoth.contains(insult)) {
            idComeback = matchedLearnedInsultKnownByBoth.indexOf(insult);
            array.add(matchedLearnedCombackKnowByBoth.get(idComeback));
            return array;
        }
        //si no esta en una de las listas casadas, entonces no tenemos información
        //coger un comebcak de la lista de no casados pues habra alguna opción
        if (learnedCombacks.size()>0) {
            System.out.println("123 listaComebackAprendida");
            return learnedCombacks;  //retornamos toda la lista y ya se escogera una opción random
        }
        //en caso de estar vacia, miramos primero en la lista de casados por ambos
        //debido a que sabemos que fallara seguro y no queremos dar información de nuestra lista
        if(matchedLearnedCombackKnowByBoth.size()>0){
            System.out.println("129 listaComebackAprendidaCasadaPorAcierto");
            return matchedLearnedCombackKnowByBoth;
        }
        //en caso de estar vacia, unicamente nos queda la opcion de escoger entre las nuestras
        //auqnue ello signifique dar informaciñon al contrincante
        System.out.println("134 listaComebackAprendidaCasada");
        return matchedLearnedComback;
    }

    //coger el insulto
    public ArrayList<String> ArrayInsultsparaElegir (){
        ArrayList<String> array=new ArrayList<>();
        //en caso de no estar vacia, enviamos siempre el insulto 0 debido a que si lo acierta
        //lo quitaremos de casada y pasara a casadaPorAcierto y el elemento 1 pasará a ser el 0
        if(!matchedLearnedInsult.isEmpty()){
            array.add(this.matchedLearnedInsult.get(this.index));
            return array;
        }
        //en caso de no tener ningun insulto nuestro, dará igual pues el tendrá el comeback
        array.addAll(learnedInsults);
        array.addAll(matchedLearnedInsultKnownByBoth);
        return array;
    }

    public void addInsult(String i){
        if (!this.learnedInsults.contains(i))
            this.learnedInsults.add(i);
    }
    public void addComeback(String c){
        if (!this.learnedCombacks.contains(c))
            this.learnedCombacks.add(c);
    }

}
