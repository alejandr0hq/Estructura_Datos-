package airctrl.gui;

import javax.swing.SwingUtilities;
import java.nio.file.Path;

/**
 * Punto de entrada de la interfaz grafica.
 * Opciones: --data carpeta (por defecto data), --csv (sin base de datos), --auto (conecta con la config guardada).
 */
public final class AirCtrlApp {
    private AirCtrlApp() {
    }

    public static void main(String[] args) {
        Path data = Path.of("data");
        boolean csv = false;
        boolean auto = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--data" -> data = Path.of(args[++i]);
                case "--csv" -> csv = true;
                case "--auto" -> auto = true;
                default -> { }
            }
        }
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("apple.awt.application.name", "AIRCTRL");
        final Path dataDirectory = data;
        final boolean csvOnly = csv;
        final boolean autoConnect = auto;
        SwingUtilities.invokeLater(() -> {
            Ui.installLookAndFeel();
            ConnectDialog dialog = new ConnectDialog(dataDirectory);
            ConnectDialog.Session session = null;
            if (csvOnly) {
                try {
                    session = new ConnectDialog.Session(
                            new airctrl.data.CsvRepository(dataDirectory).load(), null,
                            "Modo CSV: los cambios no se guardaran");
                } catch (Exception exception) {
                    System.err.println("No se pudieron leer los CSV: " + exception.getMessage());
                }
            } else if (autoConnect) {
                session = dialog.tryAutoConnect();
            }
            if (session == null && !csvOnly) {
                session = dialog.open();
            }
            if (session == null) {
                System.exit(0);
                return;
            }
            AppContext ctx = new AppContext(session.store(), session.repository(), dataDirectory);
            MainFrame frame = new MainFrame(ctx);
            frame.setVisible(true);
            ctx.success(session.notice());
        });
    }
}
