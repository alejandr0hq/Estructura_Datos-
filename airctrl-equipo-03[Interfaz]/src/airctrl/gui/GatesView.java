package airctrl.gui;

import airctrl.model.Flight;
import airctrl.model.Gate;
import airctrl.service.AirCtrlSystem;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Mapa de puertas por terminal con sus reglas de asignacion. */
public final class GatesView extends JPanel implements Navigable {
    private static final long serialVersionUID = 1L;
    private static final String ALL = "Todas";

    private final AppContext ctx;
    private final JPanel terminals = new JPanel();
    private final JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
    private final JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final Details details = new Details("Selecciona una puerta para asignarla, liberarla o cambiar su estado.");
    private String terminalFilter = ALL;
    private String selectedGate;

    public GatesView(AppContext ctx) {
        super(new BorderLayout());
        this.ctx = ctx;
        setBackground(Theme.BG);

        JPanel page = Ui.page();
        page.add(Ui.pageHeader("Puertas",
                "Solo una puerta disponible acepta un vuelo. Mantenimiento y cerradas no se pueden asignar."),
                BorderLayout.NORTH);

        filterRow.setOpaque(false);
        legend.setOpaque(false);
        JPanel toolbar = new JPanel(new BorderLayout(0, 6));
        toolbar.setOpaque(false);
        toolbar.add(filterRow, BorderLayout.NORTH);
        toolbar.add(legend, BorderLayout.SOUTH);

        terminals.setOpaque(false);
        terminals.setLayout(new BoxLayout(terminals, BoxLayout.Y_AXIS));
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 12));
        wrapper.add(terminals, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(toolbar, BorderLayout.NORTH);
        center.add(Ui.scroll(wrapper), BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);
        body.add(center, BorderLayout.CENTER);
        body.add(details, BorderLayout.EAST);
        page.add(body, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);

        ctx.onChange(this::refresh);
        refresh();
    }

    @Override
    public void onNavigate(String argument) {
        if (argument != null) {
            selectedGate = argument;
            terminalFilter = ALL;
            refresh();
        }
    }

    public void refresh() {
        AirCtrlSystem system = ctx.system();
        List<Gate> gates = system.getGates();
        Map<String, List<Gate>> byTerminal = new TreeMap<>();
        Map<String, Integer> byStatus = new TreeMap<>();
        for (Gate gate : gates) {
            byTerminal.computeIfAbsent(gate.getTerminal(), key -> new ArrayList<>()).add(gate);
            byStatus.merge(gate.getStatus(), 1, Integer::sum);
        }

        filterRow.removeAll();
        ButtonGroup group = new ButtonGroup();
        List<String> options = new ArrayList<>();
        options.add(ALL);
        options.addAll(byTerminal.keySet());
        for (String option : options) {
            JToggleButton toggle = new Chip(ALL.equals(option) ? "Todas las terminales" : "Terminal " + option);
            toggle.setSelected(option.equals(terminalFilter));
            toggle.addActionListener(event -> {
                terminalFilter = option;
                refresh();
            });
            group.add(toggle);
            filterRow.add(toggle);
        }
        filterRow.revalidate();

        legend.removeAll();
        for (String status : AirCtrlSystem.GATE_STATUSES) {
            legend.add(legendItem(status, byStatus.getOrDefault(status, 0)));
        }
        legend.revalidate();

        terminals.removeAll();
        for (Map.Entry<String, List<Gate>> entry : byTerminal.entrySet()) {
            if (!ALL.equals(terminalFilter) && !terminalFilter.equals(entry.getKey())) {
                continue;
            }
            List<Gate> list = entry.getValue();
            list.sort(Comparator.comparing(Gate::getGateId));
            long used = list.stream().filter(g -> "OCCUPIED".equals(g.getStatus())).count();
            long free = list.stream().filter(g -> "AVAILABLE".equals(g.getStatus())).count();
            JPanel head = new JPanel(new BorderLayout());
            head.setOpaque(false);
            head.add(Ui.h2("Terminal " + entry.getKey()), BorderLayout.WEST);
            head.add(Ui.muted(used + " ocupadas, " + free + " libres de " + list.size()), BorderLayout.EAST);
            head.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            head.setAlignmentX(0);
            JPanel grid = new JPanel(new GridLayout(0, 5, 10, 10));
            grid.setOpaque(false);
            grid.setAlignmentX(0);
            for (Gate gate : list) {
                grid.add(new GateTile(gate));
            }
            terminals.add(head);
            terminals.add(Box.createVerticalStrut(8));
            terminals.add(grid);
            terminals.add(Box.createVerticalStrut(22));
        }
        terminals.revalidate();
        terminals.repaint();
        showDetails();
    }

    private JComponent legendItem(String status, int count) {
        JLabel label = Ui.label(Theme.statusLabel(status) + " " + count, Theme.SMALL_BOLD, Theme.INK);
        label.setIcon(new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.status(status));
                g2.fillRoundRect(x, y + 1, 10, 10, 3, 3);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 12;
            }

            @Override
            public int getIconHeight() {
                return 12;
            }
        });
        return label;
    }

    private void showDetails() {
        AirCtrlSystem system = ctx.system();
        Gate gate = selectedGate == null ? null : system.getStore().getGates().get(selectedGate);
        if (gate == null) {
            details.clear();
            return;
        }
        details.begin(gate.getGateId(), "Terminal " + gate.getTerminal())
                .status("Estado", gate.getStatus())
                .row("Vuelo actual", gate.getCurrentFlight());
        switch (gate.getStatus()) {
            case "OCCUPIED" -> {
                Flight flight = findFlight(gate.getCurrentFlight());
                if (flight != null) {
                    details.row("Ruta", flight.getOrigin() + " → " + flight.getDestination())
                            .row("Estado del vuelo", Theme.statusLabel(flight.getStatus()));
                    JButton view = Ui.secondary("Ver vuelo " + flight.getFlightId());
                    view.addActionListener(event -> ctx.navigate("flights", flight.getFlightId()));
                    details.action(view);
                } else if (gate.getCurrentFlight() != null) {
                    details.note("El vuelo " + gate.getCurrentFlight() + " no existe en el itinerario.", Theme.DANGER);
                }
                JButton release = Ui.button("Liberar puerta", Ui.Kind.DANGER);
                release.addActionListener(event -> {
                    int answer = JOptionPane.showConfirmDialog(this,
                            "La puerta " + gate.getGateId() + " quedara disponible. ¿Continuar?",
                            "Liberar puerta", JOptionPane.OK_CANCEL_OPTION);
                    if (answer == JOptionPane.OK_OPTION) {
                        ctx.attempt(this, "Puerta " + gate.getGateId() + " liberada",
                                () -> system.releaseGate(gate.getGateId()));
                    }
                });
                details.action(release);
            }
            case "AVAILABLE" -> {
                JButton assign = Ui.primary("Asignar a un vuelo");
                assign.addActionListener(event -> openAssignDialog(gate));
                JButton maintenance = Ui.secondary("Poner en mantenimiento");
                maintenance.addActionListener(event -> ctx.attempt(this,
                        "Puerta " + gate.getGateId() + " en mantenimiento",
                        () -> system.setGateStatus(gate.getGateId(), "MAINTENANCE")));
                JButton close = Ui.secondary("Cerrar puerta");
                close.addActionListener(event -> ctx.attempt(this, "Puerta " + gate.getGateId() + " cerrada",
                        () -> system.setGateStatus(gate.getGateId(), "CLOSED")));
                details.action(assign).action(maintenance).action(close);
            }
            default -> {
                details.note(gate.getStatus().equals("MAINTENANCE")
                        ? "Fuera de servicio programado. No acepta vuelos hasta habilitarla."
                        : "Cerrada por operacion o emergencia. No acepta vuelos hasta habilitarla.", Theme.MUTED);
                JButton enable = Ui.primary("Habilitar puerta");
                enable.addActionListener(event -> ctx.attempt(this, "Puerta " + gate.getGateId() + " disponible",
                        () -> system.setGateStatus(gate.getGateId(), "AVAILABLE")));
                String other = gate.getStatus().equals("MAINTENANCE") ? "CLOSED" : "MAINTENANCE";
                JButton switchStatus = Ui.secondary(other.equals("CLOSED") ? "Cerrar puerta" : "Poner en mantenimiento");
                switchStatus.addActionListener(event -> ctx.attempt(this, null,
                        () -> system.setGateStatus(gate.getGateId(), other)));
                details.action(enable).action(switchStatus);
            }
        }
        details.done();
    }

    private Flight findFlight(String id) {
        if (id == null) {
            return null;
        }
        try {
            return ctx.system().findFlight(id);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private void openAssignDialog(Gate gate) {
        AirCtrlSystem system = ctx.system();
        List<Flight> candidates = new ArrayList<>();
        for (Flight flight : system.getFlights()) {
            if (flight.isPending()) {
                candidates.add(flight);
            }
        }
        candidates.sort(Comparator.comparing((Flight f) -> f.getGate() != null && !f.getGate().isBlank())
                .thenComparing(Comparator.comparingInt(Flight::getPriority).reversed()));
        if (candidates.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay vuelos activos para asignar.");
            return;
        }
        JComboBox<String> flight = new JComboBox<>();
        for (Flight f : candidates) {
            String gateText = f.getGate() == null || f.getGate().isBlank() ? "sin puerta" : "hoy en " + f.getGate();
            flight.addItem(f.getFlightId() + "  " + f.getAirline() + ", " + f.getOrigin() + "-" + f.getDestination()
                    + " (" + gateText + ")");
        }
        new FormDialog(this, "Asignar " + gate.getGateId(),
                "Vuelos activos; primero los que no tienen puerta. Si el vuelo ya tenia puerta, esa se libera.")
                .field("Vuelo", flight)
                .onSubmit("Asignar puerta", () -> {
                    String id = FlightsView.firstToken(flight);
                    system.assignGate(id, gate.getGateId());
                    ctx.refreshAll();
                    ctx.success("Puerta " + gate.getGateId() + " asignada a " + id);
                })
                .open();
    }

    /** Casilla de puerta: color por estado, vuelo actual y seleccion. */
    private final class GateTile extends JComponent {
        private static final long serialVersionUID = 1L;
        private final Gate gate;
        private boolean hover;

        GateTile(Gate gate) {
            this.gate = gate;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(gate.getGateId() + ": " + Theme.statusLabel(gate.getStatus())
                    + (gate.getCurrentFlight() == null ? "" : " con " + gate.getCurrentFlight()));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedGate = gate.getGateId();
                    terminals.repaint();
                    showDetails();
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
            return new Dimension(110, 72);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Color color = Theme.status(gate.getStatus());
            boolean selected = gate.getGateId().equals(selectedGate);
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            boolean blocked = "MAINTENANCE".equals(gate.getStatus()) || "CLOSED".equals(gate.getStatus());
            g2.setColor(hover ? Theme.SURFACE_ALT : Theme.SURFACE);
            g2.fillRoundRect(0, 0, w, h, 8, 8);
            if (blocked) {
                g2.setClip(0, 0, w, h);
                g2.setColor(Theme.tint(color, 28));
                g2.setStroke(new BasicStroke(6f));
                for (int x = -h; x < w; x += 16) {
                    g2.drawLine(x, h, x + h, 0);
                }
                g2.setClip(null);
            }
            g2.setColor(color);
            g2.fillRoundRect(0, 0, w, 6, 6, 6);
            g2.fillRect(0, 3, w, 3);
            g2.setStroke(new BasicStroke(selected ? 2.5f : 1f));
            g2.setColor(selected ? Theme.ACCENT : Theme.LINE);
            g2.drawRoundRect(0, 0, w, h, 8, 8);
            g2.setFont(Theme.CODE_LARGE);
            g2.setColor(Theme.INK);
            g2.drawString(gate.getGateId(), 12, 34);
            g2.setFont(Theme.SMALL);
            g2.setColor(Theme.mix(color, Theme.INK, 0.35));
            String sub = "OCCUPIED".equals(gate.getStatus()) && gate.getCurrentFlight() != null
                    ? gate.getCurrentFlight() : Theme.statusLabel(gate.getStatus());
            if ("OCCUPIED".equals(gate.getStatus())) {
                g2.setFont(Theme.CODE_SMALL.deriveFont(java.awt.Font.BOLD));
            }
            g2.drawString(sub, 12, 56);
            g2.dispose();
        }
    }

    /** Boton de filtro con aspecto de pastilla. */
    static final class Chip extends JToggleButton {
        private static final long serialVersionUID = 1L;

        Chip(String text) {
            super(text);
            setFont(Theme.SMALL_BOLD);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(6, 12, 6, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setUI(new javax.swing.plaf.basic.BasicToggleButtonUI());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean on = isSelected();
            g2.setColor(on ? Theme.NAVY : getModel().isRollover() ? Theme.SURFACE_ALT : Theme.SURFACE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            if (!on) {
                g2.setColor(Theme.LINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            }
            if (isFocusOwner()) {
                g2.setColor(Theme.ACCENT);
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, getHeight(), getHeight());
            }
            g2.dispose();
            setForeground(on ? Color.WHITE : Theme.INK);
            super.paintComponent(g);
        }
    }
}
