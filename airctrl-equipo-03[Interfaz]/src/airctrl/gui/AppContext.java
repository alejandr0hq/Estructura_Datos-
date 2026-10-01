package airctrl.gui;

import airctrl.data.CsvRepository;
import airctrl.data.DataStore;
import airctrl.data.PostgresRepository;
import airctrl.service.AirCtrlException;
import airctrl.service.AirCtrlSystem;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/** Estado compartido por todas las pantallas: sistema, repositorio y notificaciones. */
public final class AppContext {
    private AirCtrlSystem system;
    private final PostgresRepository repository;
    private final Path dataDirectory;
    private final List<Runnable> listeners = new ArrayList<>();
    private BiConsumer<String, Boolean> notifier = (message, ok) -> { };
    private BiConsumer<String, String> navigator = (key, argument) -> { };

    public AppContext(DataStore store, PostgresRepository repository, Path dataDirectory) {
        this.repository = repository;
        this.dataDirectory = dataDirectory;
        installSystem(store);
    }

    private void installSystem(DataStore store) {
        system = new AirCtrlSystem(store);
        if (repository != null) {
            system.setPersistence(repository);
        }
    }

    public AirCtrlSystem system() {
        return system;
    }

    public PostgresRepository repository() {
        return repository;
    }

    public boolean isPersistent() {
        return repository != null;
    }

    public String sourceDescription() {
        return system.getPersistence().describe();
    }

    public void onChange(Runnable listener) {
        listeners.add(listener);
    }

    public void refreshAll() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    public void setNotifier(BiConsumer<String, Boolean> notifier) {
        this.notifier = notifier;
    }

    public void setNavigator(BiConsumer<String, String> navigator) {
        this.navigator = navigator;
    }

    public void navigate(String key) {
        navigator.accept(key, null);
    }

    /** Navega a una pantalla pasandole un dato inicial (por ejemplo, un vuelo para filtrar). */
    public void navigate(String key, String argument) {
        navigator.accept(key, argument);
    }

    public void success(String message) {
        notifier.accept(message, true);
    }

    public void failure(String message) {
        notifier.accept(message, false);
    }

    /**
     * Ejecuta una accion del sistema. Si falla por una regla de negocio se muestra el
     * motivo; si tiene exito se refrescan todas las pantallas y se avisa.
     */
    public boolean attempt(Component parent, String successMessage, Runnable action) {
        try {
            action.run();
            refreshAll();
            if (successMessage != null) {
                success(successMessage);
            }
            return true;
        } catch (AirCtrlException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(parent, exception.getMessage(),
                    "No se pudo completar", JOptionPane.WARNING_MESSAGE);
            failure(exception.getMessage());
            return false;
        }
    }

    /** Vuelve a leer la fuente de datos (PostgreSQL o CSV). */
    public void reload() throws Exception {
        DataStore store = repository != null
                ? repository.load()
                : new CsvRepository(dataDirectory).load();
        installSystem(store);
        refreshAll();
    }

    /** Borra la base y vuelve a importar los CSV originales. Solo en modo PostgreSQL. */
    public void resetFromCsv() throws Exception {
        if (repository == null) {
            reload();
            return;
        }
        repository.resetFromCsv(dataDirectory);
        reload();
    }
}
