package airctrl.gui;

import airctrl.model.Baggage;
import airctrl.model.Flight;
import airctrl.model.Gate;
import airctrl.model.Incident;
import airctrl.model.Operation;
import airctrl.service.AirCtrlSystem;
import airctrl.structure.PriorityOperationQueue;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Vista general: lo urgente arriba, indicadores y distribucion de estados. */
public final class DashboardView extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AppContext ctx;
    private final JLabel stamp = Ui.muted("");
    private final JPanel metrics = new JPanel(new GridLayout(1, 6, 12, 0));
    private final JPanel nextBox = new JPanel(new BorderLayout(0, 12));
    private final JPanel recent = new JPanel();
    private final BarChart queueChart = new BarChart(
            key -> Theme.priority(Integer.parseInt(key)),
            key -> key + "  " + Theme.priorityName(Integer.parseInt(key)));
    private final BarChart flightChart = new BarChart(Theme::status, Theme::statusLabel);
    private final BarChart gateChart = new BarChart(Theme::status, Theme::statusLabel);
    private final JButton processButton = Ui.primary("Atender siguiente");

    public DashboardView(AppContext ctx) {
        super(new BorderLayout());
        this.ctx = ctx;
        setBackground(Theme.BG);

        processButton.addActionListener(event -> processNext());
        JPanel page = Ui.page();
        JPanel header = Ui.pageHeader("Panel de control",
                "Lo mas urgente primero, y como esta la operacion del aeropuerto en este momento.",
                processButton);
        page.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 16, 0);
        metrics.setOpaque(false);
        body.add(Ui.shrinkable(metrics), c);

        nextBox.setOpaque(false);
        recent.setOpaque(false);
        recent.setLayout(new BoxLayout(recent, BoxLayout.Y_AXIS));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(Ui.surface("Siguiente operacion", nextBox));
        left.add(Box.createVerticalStrut(16));
        left.add(Ui.surface("Atendidas recientemente", recent));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(Ui.surface("Cola por prioridad", queueChart));
        right.add(Box.createVerticalStrut(16));
        right.add(Ui.surface("Vuelos por estado", flightChart));
        right.add(Box.createVerticalStrut(16));
        right.add(Ui.surface("Puertas por estado", gateChart));

        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 0.62;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 16);
        body.add(Ui.shrinkable(left), c);
        c.gridx = 1;
        c.weightx = 0.38;
        c.insets = new Insets(0, 0, 0, 0);
        body.add(Ui.shrinkable(right), c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.weighty = 1;
        body.add(Box.createGlue(), c);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(body, BorderLayout.NORTH);
        JPanel stampRow = new JPanel(new BorderLayout());
        stampRow.setOpaque(false);
        stampRow.setBorder(new EmptyBorder(14, 0, 0, 0));
        stampRow.add(stamp, BorderLayout.WEST);
        content.add(stampRow, BorderLayout.CENTER);
        page.add(Ui.scroll(content), BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);

        ctx.onChange(this::refresh);
        refresh();
    }

    private void processNext() {
        ctx.attempt(this, null, () -> {
            Operation done = ctx.system().processNext();
            ctx.success("Operacion " + done.getReferenceId() + " atendida");
        });
    }

    public void refresh() {
        AirCtrlSystem system = ctx.system();
        List<Flight> flights = system.getFlights();
        List<Gate> gates = system.getGates();
        List<Baggage> baggage = system.getBaggage();
        long activeFlights = flights.stream().filter(Flight::isPending).count();
        long freeGates = gates.stream().filter(g -> "AVAILABLE".equals(g.getStatus())).count();
        long tracking = baggage.stream().filter(Baggage::isPending).count();
        long missing = baggage.stream().filter(b -> "MISSING".equals(b.status())).count();
        long openIncidents = system.getIncidents().stream().filter(i -> !i.isResolved()).count();
        int queued = system.getPending().size();

        metrics.removeAll();
        metrics.add(new Metric(String.valueOf(queued), "Operaciones en cola",
                queued == 1 ? "1 por atender" : queued + " por atender", Theme.ACCENT, "operations"));
        metrics.add(new Metric(String.valueOf(activeFlights), "Vuelos activos",
                "de " + flights.size() + " en el itinerario", Theme.ACCENT, "flights"));
        metrics.add(new Metric(String.valueOf(freeGates), "Puertas libres",
                "de " + gates.size() + " puertas", freeGates == 0 ? Theme.DANGER : Theme.OK, "gates"));
        metrics.add(new Metric(String.valueOf(tracking), "Equipaje en seguimiento",
                "aun no entregado", Theme.WARN, "baggage"));
        metrics.add(new Metric(String.valueOf(missing), "Equipaje extraviado",
                missing == 0 ? "sin extravios" : "requiere busqueda", missing > 0 ? Theme.DANGER : Theme.OK, "baggage"));
        metrics.add(new Metric(String.valueOf(openIncidents), "Incidentes abiertos",
                system.getIncidents().size() + " registrados", openIncidents > 0 ? Theme.DANGER : Theme.OK, "incidents"));
        metrics.revalidate();
        metrics.repaint();

        nextBox.removeAll();
        processButton.setEnabled(queued > 0);
        if (queued == 0) {
            nextBox.add(Ui.label("No hay operaciones pendientes. Registra un incidente o un vuelo para agregar trabajo a la cola.",
                    Theme.BODY, Theme.MUTED), BorderLayout.CENTER);
        } else {
            Operation next = system.nextOperation();
            nextBox.add(new OperationStrip(next, true, false), BorderLayout.NORTH);
            JLabel why = Ui.label("Prioridad " + next.getPriority() + " (" + Theme.priorityName(next.getPriority())
                    + "): la mas alta en la cola y, en su nivel, la que llego primero.",
                    Theme.SMALL, Theme.MUTED);
            why.setBorder(new EmptyBorder(10, 2, 10, 0));
            JButton now = Ui.primary("Atender ahora");
            now.addActionListener(event -> processNext());
            JButton all = Ui.secondary("Ver cola completa");
            all.addActionListener(event -> ctx.navigate("operations"));
            JPanel foot = new JPanel(new BorderLayout());
            foot.setOpaque(false);
            foot.add(why, BorderLayout.NORTH);
            foot.add(Ui.row(all, now), BorderLayout.WEST);
            nextBox.add(foot, BorderLayout.CENTER);
        }
        nextBox.revalidate();
        nextBox.repaint();

        recent.removeAll();
        List<Operation> history = system.recentHistory(5);
        if (history.isEmpty()) {
            recent.add(Ui.label("Todavia no se atiende ninguna operacion en esta base.", Theme.BODY, Theme.MUTED));
        }
        for (Operation operation : history) {
            recent.add(new OperationStrip(operation, false, true));
        }
        recent.revalidate();
        recent.repaint();

        Map<String, Integer> byPriority = new LinkedHashMap<>();
        PriorityOperationQueue<Operation> pending = system.getPending();
        for (int p = PriorityOperationQueue.MAX_PRIORITY; p >= PriorityOperationQueue.MIN_PRIORITY; p--) {
            byPriority.put(String.valueOf(p), pending.countAt(p));
        }
        queueChart.setData(byPriority);
        Map<String, Map<String, Integer>> reports = system.reports();
        flightChart.setData(reports.get("Vuelos por estado"));
        gateChart.setData(reports.get("Puertas por estado"));
        stamp.setText("Actualizado " + STAMP.format(LocalDateTime.now()) + ". Fuente: " + ctx.sourceDescription());
    }

    /** Indicador compacto; al hacer clic lleva a la pantalla correspondiente. */
    private final class Metric extends JComponent {
        private static final long serialVersionUID = 1L;
        private final String value;
        private final String label;
        private final String note;
        private final Color color;
        private boolean hover;

        Metric(String value, String label, String note, Color color, String target) {
            this.value = value;
            this.label = label;
            this.note = note;
            this.color = color;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Abrir " + label.toLowerCase());
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    ctx.navigate(target);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(140, 96);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            g2.setColor(hover ? Theme.SURFACE_ALT : Theme.SURFACE);
            g2.fillRoundRect(0, 0, w, h, 10, 10);
            g2.setColor(hover ? Theme.ACCENT : Theme.LINE);
            g2.drawRoundRect(0, 0, w, h, 10, 10);
            g2.setColor(color);
            g2.fillRect(14, 16, 3, 28);
            g2.setFont(Theme.METRIC);
            g2.setColor(Theme.INK);
            g2.drawString(value, 24, 42);
            g2.setFont(Theme.SMALL_BOLD);
            g2.drawString(label, 14, 66);
            g2.setFont(Theme.SMALL);
            g2.setColor(Theme.MUTED);
            g2.drawString(note, 14, 83);
            g2.dispose();
        }
    }
}
