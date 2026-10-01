package airctrl.gui;

import airctrl.data.CsvRepository;
import airctrl.data.DataStore;
import airctrl.data.DbConfig;
import airctrl.data.PostgresRepository;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/** Primera pantalla: conectar a PostgreSQL local o trabajar solo con los CSV. */
public final class ConnectDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    public record Session(DataStore store, PostgresRepository repository, String notice) {
    }

    private final Path dataDirectory;
    private final DbConfig config;
    private final JTextField host = Ui.field(16);
    private final JSpinner port = new JSpinner(new SpinnerNumberModel(5432, 1, 65535, 1));
    private final JTextField database = Ui.field(16);
    private final JTextField user = Ui.field(16);
    private final JPasswordField password = new JPasswordField(16);
    private final JCheckBox remember = new JCheckBox("Recordar contrasena en este equipo");
    private final JLabel status = Ui.label(" ", Theme.SMALL, Theme.MUTED);
    private final JProgressBar progress = new JProgressBar();
    private final JButton connect = Ui.primary("Conectar");
    private final JButton csvOnly = Ui.secondary("Usar solo CSV");
    private Session session;

    public ConnectDialog(Path dataDirectory) {
        super((java.awt.Frame) null, "AIRCTRL", true);
        this.dataDirectory = dataDirectory;
        this.config = DbConfig.load(DbConfig.DEFAULT_FILE);

        JPanel side = new JPanel();
        side.setBackground(Theme.NAVY);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(28, 24, 28, 24));
        side.setPreferredSize(new Dimension(250, 10));
        side.add(Ui.label("AIRCTRL", Theme.CODE_LARGE.deriveFont(24f), Color.WHITE));
        side.add(Box.createVerticalStrut(4));
        side.add(Ui.label("Centro de operaciones", Theme.SMALL, Theme.NAVY_MUTED));
        side.add(Box.createVerticalStrut(28));
        side.add(Ui.label("<html><div style='width:190px; line-height:1.4'>"
                + "Los cambios se guardan en tu PostgreSQL local. La primera vez se crea la base "
                + "y se importan los archivos de la carpeta <b>data</b>.<br><br>"
                + "Sin PostgreSQL puedes usar solo los CSV; en ese modo nada se guarda al cerrar."
                + "</div></html>", Theme.SMALL, Theme.NAVY_TEXT));

        host.setText(config.getHost());
        port.setValue(config.getPort());
        database.setText(config.getDatabase());
        user.setText(config.getUser());
        password.setText(config.getPassword());
        password.setFont(Theme.BODY);
        remember.setSelected(config.isRememberPassword());
        remember.setOpaque(false);
        remember.setFont(Theme.SMALL);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(port, "#");
        port.setEditor(editor);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.SURFACE);
        form.setBorder(new EmptyBorder(28, 28, 22, 28));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 4, 0);
        form.add(Ui.label("Conectar a PostgreSQL", Theme.H2, Theme.INK), c);
        c.gridy++;
        c.insets = new Insets(0, 0, 16, 0);
        form.add(Ui.muted("Servidor local; los valores se guardan en config/database.properties"), c);
        c.gridwidth = 1;
        addRow(form, c, "Servidor", host);
        addRow(form, c, "Puerto", port);
        addRow(form, c, "Base de datos", database);
        addRow(form, c, "Usuario", user);
        addRow(form, c, "Contrasena", password);
        c.gridy++;
        c.gridx = 1;
        c.insets = new Insets(2, 0, 10, 0);
        form.add(remember, c);
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);
        progress.setIndeterminate(true);
        progress.setVisible(false);
        form.add(progress, c);
        c.gridy++;
        form.add(status, c);
        c.gridy++;
        c.weighty = 1;
        form.add(Box.createVerticalGlue(), c);
        c.gridy++;
        c.weighty = 0;
        c.insets = new Insets(14, 0, 0, 0);
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setOpaque(false);
        buttons.add(csvOnly, BorderLayout.WEST);
        buttons.add(connect, BorderLayout.EAST);
        form.add(buttons, c);

        connect.addActionListener(event -> connect());
        csvOnly.addActionListener(event -> useCsv());
        getRootPane().setDefaultButton(connect);

        JPanel root = new JPanel(new BorderLayout());
        root.add(side, BorderLayout.WEST);
        root.add(form, BorderLayout.CENTER);
        setContentPane(root);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setSize(new Dimension(760, Math.max(getHeight(), 500)));
        setResizable(false);
        setLocationRelativeTo(null);

        if (!Files.isDirectory(dataDirectory)) {
            showError("No se encontro la carpeta " + dataDirectory.toAbsolutePath()
                    + ". Ejecuta la aplicacion desde la raiz del proyecto.");
        }
    }

    private static void addRow(JPanel form, GridBagConstraints c, String label, java.awt.Component field) {
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(5, 0, 5, 14);
        form.add(Ui.label(label, Theme.BODY_BOLD, Theme.INK), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 5, 0);
        form.add(field, c);
    }

    public Session open() {
        setVisible(true);
        return session;
    }

    /** Conexion sin mostrar el dialogo (opcion --auto). Devuelve null si falla. */
    public Session tryAutoConnect() {
        try {
            return openSession(config);
        } catch (Exception exception) {
            showError(explain(exception));
            return null;
        }
    }

    private void connect() {
        config.setHost(host.getText());
        config.setPort((Integer) port.getValue());
        config.setDatabase(database.getText());
        config.setUser(user.getText());
        config.setPassword(new String(password.getPassword()));
        config.setRememberPassword(remember.isSelected());
        setBusy(true, "Conectando a " + config.getHost() + ":" + config.getPort() + "...");
        new SwingWorker<Session, Void>() {
            @Override
            protected Session doInBackground() throws Exception {
                return openSession(config);
            }

            @Override
            protected void done() {
                try {
                    session = get();
                    try {
                        config.save(DbConfig.DEFAULT_FILE);
                    } catch (Exception ignored) {
                        // No es grave si no se puede guardar la configuracion.
                    }
                    dispose();
                } catch (ExecutionException exception) {
                    setBusy(false, null);
                    showError(explain(exception.getCause()));
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private Session openSession(DbConfig cfg) throws Exception {
        PostgresRepository repository = PostgresRepository.open(cfg);
        try {
            boolean imported = repository.seedIfEmpty(dataDirectory);
            DataStore store = repository.load();
            String notice = imported
                    ? "Base " + cfg.getDatabase() + " creada e importada desde los CSV"
                    : "Conectado a " + cfg.getDatabase();
            return new Session(store, repository, notice);
        } catch (Exception exception) {
            repository.close();
            throw exception;
        }
    }

    private void useCsv() {
        try {
            DataStore store = new CsvRepository(dataDirectory).load();
            session = new Session(store, null, "Modo CSV: los cambios no se guardaran");
            dispose();
        } catch (Exception exception) {
            showError("No se pudieron leer los CSV: " + exception.getMessage());
        }
    }

    private void setBusy(boolean busy, String message) {
        progress.setVisible(busy);
        connect.setEnabled(!busy);
        csvOnly.setEnabled(!busy);
        status.setForeground(Theme.MUTED);
        status.setText(message == null ? " " : message);
    }

    private void showError(String message) {
        status.setForeground(Theme.DANGER);
        status.setText("<html><div style='width:400px'>" + message + "</div></html>");
        pack();
        setSize(new Dimension(760, Math.max(getHeight(), 500)));
    }

    private String explain(Throwable error) {
        String message = error.getMessage() == null ? error.toString() : error.getMessage();
        if (error instanceof SQLException sql) {
            String state = sql.getSQLState() == null ? "" : sql.getSQLState();
            if (message.contains("driver JDBC")) {
                return message;
            }
            if (state.equals("28P01") || state.equals("28000")) {
                return "Usuario o contrasena incorrectos para " + config.getUser() + ".";
            }
            if (state.startsWith("08") || message.contains("Connection refused")) {
                return "No hay un servidor PostgreSQL escuchando en " + config.getHost() + ":" + config.getPort()
                        + ". Inicia el servicio (macOS: brew services start postgresql; Linux: sudo service postgresql start) "
                        + "o revisa el puerto.";
            }
            if (state.equals("42501")) {
                return "El usuario no tiene permiso para crear la base " + config.getDatabase()
                        + ". Creala a mano con CREATE DATABASE " + config.getDatabase() + "; o usa otro usuario.";
            }
        }
        return "No se pudo conectar: " + message;
    }
}
