import airctrl.data.CsvRepository;
import airctrl.data.DataStore;
import airctrl.service.AirCtrlSystem;
import airctrl.ui.ConsoleInterface;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && "--gui".equals(args[0])) {
            String[] rest = new String[args.length - 1];
            System.arraycopy(args, 1, rest, 0, rest.length);
            airctrl.gui.AirCtrlApp.main(rest);
            return;
        }
        try {
            DataStore store = new CsvRepository(Path.of("data")).load();
            AirCtrlSystem system = new AirCtrlSystem(store);
            ConsoleInterface console = new ConsoleInterface(system);
            if (args.length > 0 && "--demo".equals(args[0])) {
                console.runDemo();
            } else {
                console.runInteractive();
            }
        } catch (IOException exception) {
            System.err.println("No fue posible cargar los datos: " + exception.getMessage());
        }
    }
}
