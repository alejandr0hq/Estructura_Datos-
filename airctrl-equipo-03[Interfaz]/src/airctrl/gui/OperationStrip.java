package airctrl.gui;

import airctrl.model.Flight;
import airctrl.model.Incident;
import airctrl.model.Operation;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.format.DateTimeFormatter;

/**
 * Tira de operacion, al estilo de las fichas de progreso de vuelo de una torre de control:
 * banda de prioridad a la izquierda, identificador grande y los datos que el operador necesita.
 */
public final class OperationStrip extends JComponent {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Operation operation;
    private final boolean next;
    private final boolean showProcessed;

    public OperationStrip(Operation operation, boolean next, boolean showProcessed) {
        this.operation = operation;
        this.next = next;
        this.showProcessed = showProcessed;
        setToolTipText(tooltip());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(420, showProcessed ? 52 : 60);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth() - 1;
        int h = getHeight() - 5;
        int priority = operation.getPriority();
        Color band = Theme.priority(priority);

        g2.setColor(next ? new Color(0xF3F8FE) : Theme.SURFACE);
        g2.fillRoundRect(0, 0, w, h, 8, 8);
        g2.setColor(band);
        g2.fillRoundRect(0, 0, 34, h, 8, 8);
        g2.fillRect(20, 0, 14, h);
        g2.setColor(next ? Theme.ACCENT : Theme.LINE);
        g2.setStroke(new BasicStroke(next ? 2f : 1f));
        g2.drawRoundRect(0, 0, w, h, 8, 8);

        g2.setColor(Color.WHITE);
        g2.setFont(Theme.CODE_LARGE);
        FontMetrics fm = g2.getFontMetrics();
        String number = String.valueOf(priority);
        g2.drawString(number, 17 - fm.stringWidth(number) / 2, h / 2 + fm.getAscent() / 2 - 3);

        int x = 48;
        int top = h / 2 - 4;
        int bottom = h / 2 + 14;
        g2.setFont(Theme.CODE_LARGE);
        g2.setColor(Theme.INK);
        g2.drawString(operation.getReferenceId(), x, top + 2);
        g2.setFont(Theme.SMALL);
        g2.setColor(Theme.MUTED);
        String kind = operation.isFlightOperation() ? "Vuelo" : "Incidente " + operation.getIncident().getType();
        g2.drawString(fit(g2, kind, 120), x, bottom);

        int available = w - 48;
        int col2 = 48 + Math.max(130, (int) (available * 0.24));
        int col3 = 48 + Math.max(300, (int) (available * 0.52));
        int right = w - 14;

        if (operation.isFlightOperation()) {
            Flight flight = operation.getFlight();
            g2.setFont(Theme.CODE);
            g2.setColor(Theme.INK);
            g2.drawString(flight.getOrigin() + " → " + flight.getDestination(), col2, top + 2);
            g2.setFont(Theme.SMALL);
            g2.setColor(Theme.MUTED);
            g2.drawString(fit(g2, flight.getAirline(), col3 - col2 - 10), col2, bottom);
            if (col3 + 150 < right - 110) {
                g2.setFont(Theme.BODY);
                g2.setColor(Theme.INK);
                String gate = flight.getGate() == null || flight.getGate().isBlank()
                        ? "Sin puerta" : "Puerta " + flight.getGate();
                g2.drawString(gate, col3, top + 2);
                g2.setFont(Theme.SMALL);
                g2.setColor(Theme.MUTED);
                g2.drawString("Aeronave " + flight.getAircraftId(), col3, bottom);
            }
        } else {
            Incident incident = operation.getIncident();
            g2.setFont(Theme.BODY);
            g2.setColor(Theme.INK);
            g2.drawString(fit(g2, incident.getDescription(), right - col2 - 130), col2, top + 2);
            g2.setFont(Theme.SMALL);
            g2.setColor(Theme.MUTED);
            g2.drawString("Registrado " + TIME.format(incident.getCreatedAt()), col2, bottom);
        }

        g2.setFont(Theme.SMALL);
        g2.setColor(Theme.MUTED);
        String meta;
        if (showProcessed && operation.getProcessedAt() != null) {
            meta = "Atendida " + TIME.format(operation.getProcessedAt());
        } else if (next) {
            meta = "Siguiente en atender";
        } else {
            meta = "Llegada #" + operation.getArrivalOrder();
        }
        if (next) {
            g2.setFont(Theme.SMALL_BOLD);
            g2.setColor(Theme.ACCENT);
        }
        int metaWidth = g2.getFontMetrics().stringWidth(meta);
        g2.drawString(meta, right - metaWidth, bottom);

        if (operation.isFlightOperation()) {
            String status = operation.getFlight().getStatus();
            String label = Theme.statusLabel(status);
            g2.setFont(Theme.SMALL_BOLD);
            int badgeWidth = g2.getFontMetrics().stringWidth(label) + 18;
            Ui.paintBadge(g2, label, Theme.status(status), right - badgeWidth, top - 3, Theme.SMALL_BOLD);
        }
        g2.dispose();
    }

    private String tooltip() {
        String base = "Prioridad " + operation.getPriority() + ": " + Theme.priorityName(operation.getPriority());
        if (operation.isFlightOperation()) {
            Flight f = operation.getFlight();
            return base + ". " + f.getAirline() + " " + f.getFlightId() + " de " + f.getOrigin()
                    + " a " + f.getDestination();
        }
        return base + ". " + operation.getIncident().getDescription();
    }

    private static String fit(Graphics2D g2, String text, int width) {
        if (text == null) {
            return "";
        }
        FontMetrics fm = g2.getFontMetrics();
        if (width <= 20 || fm.stringWidth(text) <= width) {
            return width <= 20 ? "" : text;
        }
        String value = text;
        while (!value.isEmpty() && fm.stringWidth(value + "…") > width) {
            value = value.substring(0, value.length() - 1);
        }
        return value + "…";
    }
}
