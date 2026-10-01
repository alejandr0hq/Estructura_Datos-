package airctrl.gui;

/** Pantallas que aceptan un dato al abrirse desde otra pantalla. */
public interface Navigable {
    void onNavigate(String argument);
}
