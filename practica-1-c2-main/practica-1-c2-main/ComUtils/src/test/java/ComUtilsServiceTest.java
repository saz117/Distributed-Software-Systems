import org.junit.Test;
import utils.ComUtilsService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ComUtilsServiceTest {
    @Test
    public void example_test() {
        File file = new File("Maintest");
        try {
            file.createNewFile();

            //TODO-> REVISAR
            ComUtilsService comUtilsService = new ComUtilsService(new FileInputStream(file), new FileOutputStream(file));
            String cadena = "Prueba de texto";
            int lon = comUtilsService.writeTest(cadena);

            String str = comUtilsService.readTest(lon);
            assertEquals(cadena, str);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

