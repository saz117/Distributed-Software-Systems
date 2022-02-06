import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;


public class DecisionMakingTest_C {
    @Test
    public void example_test() {
        File file = new File("Hashtest");
        try {
            file.createNewFile();

            //TODO-> REVISAR

            //Scanner sc = new Scanner(System.in);
            System.out.println("Enter game mode: \n" +
                    "0: manual\n" +
                    "1: automatic\n");

            //int input = sc.nextInt();
            int input = 0;
            DecisionMaking_C.get_choice(input);


        } catch (IOException e) {
            System.out.println("Trump oso");

            e.printStackTrace();
        }
    }
}
