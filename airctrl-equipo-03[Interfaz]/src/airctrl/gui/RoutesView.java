package airctrl.gui;

import airctrl.model.Connection;
import airctrl.model.Route;
import airctrl.service.AirCtrlException;
import airctrl.service.AirCtrlSystem;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** Rutas internas: Dijkstra para la mas corta y BFS para ver lo alcanzable. */
public final class RoutesView extends JPanel {
    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final JComboBox<String> origin = new JComboBox<>();
    private final JComboBox<String> destination = new JComboBox<>();
    private final JPanel result = new JPanel();
    private final GraphPanel graph = new GraphPanel();
    private final Map<String, Integer> distances = new HashMap<>();
    private boolean clickSetsDestination;

    public RoutesView(AppContext ctx) {
        super(new BorderLayout());
        this.ctx = ctx;
        setBackground(Theme.BG);

        JPanel page = Ui.page();
        page.add(Ui.pageHeader("Rutas internas",
                "Distancias entre puertas, bandas, salas de equipaje y mostradores del aeropuerto."),
                BorderLayout.NORTH);

        JButton swap = Ui.button("Invertir", Ui.Kind.GHOST);
        swap.addActionListener(event -> {
            Object a = origin.getSelectedItem();
            origin.setSelectedItem(destination.getSelectedItem());
            destination.setSelectedItem(a);
        });
        JButton shortest = Ui.primary("Calcular ruta mas corta");
        shortest.addActionListener(event -> computeRoute());
        JButton reach = Ui.secondary("Ver todo lo alcanzable desde el origen");
        reach.addActionListener(event -> computeReach());
        JButton clear = Ui.button("Limpiar", Ui.Kind.GHOST);
        clear.addActionListener(event -> {
            result.removeAll();
            result.revalidate();
            result.repaint();
            graph.setHighlight(null, null, null, null);
        });

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(fieldRow("Origen", origin));
        form.add(Box.createVerticalStrut(8));
        form.add(fieldRow("Destino", destination));
        form.add(Box.createVerticalStrut(4));
        form.add(left(swap));
        form.add(Box.createVerticalStrut(10));
        form.add(stretch(shortest));
        form.add(Box.createVerticalStrut(8));
        form.add(stretch(reach));
        form.add(Box.createVerticalStrut(4));
        form.add(left(clear));
        JLabel hint = Ui.label("<html>Tambien puedes hacer clic en el mapa: el primer clic elige el origen y el "
                + "segundo el destino.</html>", Theme.SMALL, Theme.MUTED);
        hint.setBorder(new EmptyBorder(8, 0, 0, 0));
        form.add(left(hint));

        result.setOpaque(false);
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));

        JPanel controls = new JPanel(new BorderLayout(0, 16));
        controls.setOpaque(false);
        controls.add(form, BorderLayout.NORTH);
        JPanel resultWrap = new JPanel(new BorderLayout());
        resultWrap.setOpaque(false);
        resultWrap.add(result, BorderLayout.NORTH);
        controls.add(Ui.scroll(resultWrap), BorderLayout.CENTER);
        Ui.Surface side = new Ui.Surface(new BorderLayout());
        side.add(controls);
        side.setPreferredSize(new Dimension(330, 400));

        graph.setOnNodeClick(this::onNodeClick);
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        legend.setOpaque(false);
        legend.add(legendItem("Puerta", GraphPanel.kindColor("G01")));
        legend.add(legendItem("Banda", GraphPanel.kindColor("BELT01")));
        legend.add(legendItem("Sala de equipaje", GraphPanel.kindColor("BAGROOM-1")));
        legend.add(legendItem("Mostrador", GraphPanel.kindColor("CHECKIN-A")));
        legend.add(legendItem("Plataforma", GraphPanel.kindColor("RAMP-S")));
        legend.add(Ui.muted("Linea punteada: sin distancia registrada, no se usa para rutas"));
        Ui.Surface map = new Ui.Surface(new BorderLayout(0, 10));
        map.add(Ui.h2("Mapa de conexiones"), BorderLayout.NORTH);
        map.add(graph, BorderLayout.CENTER);
        map.add(legend, BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);
        body.add(side, BorderLayout.WEST);
        body.add(map, BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);

        ctx.onChange(this::refresh);
        refresh();
    }

    public void refresh() {
        AirCtrlSystem system = ctx.system();
        List<String> locations = system.locations();
        Object a = origin.getSelectedItem();
        Object b = destination.getSelectedItem();
        origin.setModel(new DefaultComboBoxModel<>(locations.toArray(String[]::new)));
        destination.setModel(new DefaultComboBoxModel<>(locations.toArray(String[]::new)));
        origin.setSelectedItem(a != null ? a : locations.contains("G19") ? "G19" : null);
        destination.setSelectedItem(b != null ? b : locations.contains("BELT02") ? "BELT02" : null);
        distances.clear();
        for (Connection c : system.getStore().getConnections()) {
            if (c.distanceMeters() != null && c.distanceMeters() > 0) {
                String x = c.sourceLocation().trim().toUpperCase();
                String y = c.targetLocation().trim().toUpperCase();
                distances.merge(x + "|" + y, c.distanceMeters(), Math::min);
                distances.merge(y + "|" + x, c.distanceMeters(), Math::min);
            }
        }
        graph.setConnections(system.getStore().getConnections());
    }

    private void onNodeClick(String node) {
        if (!clickSetsDestination) {
            origin.setSelectedItem(node);
        } else {
            destination.setSelectedItem(node);
            computeRoute();
        }
        clickSetsDestination = !clickSetsDestination;
        if (clickSetsDestination) {
            graph.setHighlight(null, null, node, null);
        }
    }

    private void computeRoute() {
        String from = FormDialog.selected(origin);
        String to = FormDialog.selected(destination);
        result.removeAll();
        try {
            Route route = ctx.system().findShortestRoute(from, to);
            result.add(Ui.label(route.distanceMeters() + " m", Theme.METRIC, Theme.INK));
            int stops = route.locations().size() - 1;
            result.add(Ui.muted(stops == 0 ? "Origen y destino son el mismo punto"
                    : stops + (stops == 1 ? " tramo" : " tramos") + " por la ruta mas corta"));
            result.add(Box.createVerticalStrut(12));
            List<String> path = route.locations();
            for (int i = 0; i < path.size(); i++) {
                Integer segment = i + 1 < path.size() ? distances.get(path.get(i) + "|" + path.get(i + 1)) : null;
                result.add(new Step(path.get(i), segment, i == 0, i == path.size() - 1));
            }
            graph.setHighlight(path, null, from, to);
            ctx.success("Ruta " + from + " a " + to + ": " + route.distanceMeters() + " m");
        } catch (AirCtrlException exception) {
            JLabel message = Ui.label("<html>" + exception.getMessage()
                    + ". Puede que no esten conectados o que el unico camino use tramos sin distancia.</html>",
                    Theme.BODY, Theme.DANGER);
            result.add(left(message));
            graph.setHighlight(null, null, from, to);
        }
        result.revalidate();
        result.repaint();
    }

    private void computeReach() {
        String from = FormDialog.selected(origin);
        result.removeAll();
        try {
            List<String> order = ctx.system().traverseConnections(from);
            result.add(Ui.label(String.valueOf(order.size()), Theme.METRIC, Theme.INK));
            result.add(Ui.muted("ubicaciones alcanzables desde " + from + ", en orden de recorrido (BFS)"));
            result.add(Box.createVerticalStrut(10));
            JLabel list = Ui.label("<html><div style='width:250px'>" + String.join(", ", order) + "</div></html>",
                    Theme.CODE_SMALL, Theme.INK);
            result.add(left(list));
            graph.setHighlight(null, new LinkedHashSet<>(order), from, null);
        } catch (AirCtrlException exception) {
            result.add(left(Ui.label(exception.getMessage(), Theme.BODY, Theme.DANGER)));
        }
        result.revalidate();
        result.repaint();
    }

    private static JComponent fieldRow(String label, JComboBox<String> combo) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        row.add(Ui.label(label, Theme.SMALL_BOLD, Theme.INK), BorderLayout.NORTH);
        row.add(combo, BorderLayout.CENTER);
        row.setAlignmentX(0);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        return row;
    }

    private static JComponent left(JComponent component) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(component, BorderLayout.WEST);
        row.setAlignmentX(0);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height + 4));
        return row;
    }

    private static JComponent stretch(JComponent component) {
        component.setAlignmentX(0);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
        return component;
    }

    private static JComponent legendItem(String text, Color color) {
        JLabel label = Ui.label(text, Theme.SMALL, Theme.INK);
        label.setIcon(new javax.swing.Icon() {
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(x, y + 1, 12, 10, 4, 4);
                g2.dispose();
            }

            public int getIconWidth() {
                return 14;
            }

            public int getIconHeight() {
                return 12;
            }
        });
        return label;
    }

    /** Paso de la ruta: punto, linea vertical y distancia del tramo siguiente. */
    private static final class Step extends JComponent {
        private static final long serialVersionUID = 1L;
        private final String node;
        private final Integer segment;
        private final boolean first;
        private final boolean last;

        Step(String node, Integer segment, boolean first, boolean last) {
            this.node = node;
            this.segment = segment;
            this.first = first;
            this.last = last;
            setAlignmentX(0);
            setBorder(new MatteBorder(0, 0, 0, 0, Theme.LINE));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(260, last ? 26 : 46);
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
            if (!last) {
                g2.setColor(Theme.ACCENT);
                g2.fillRect(9, 13, 3, getHeight() - 13);
            }
            Color color = GraphPanel.kindColor(node);
            g2.setColor(first || last ? Theme.INK : color);
            g2.fillOval(4, 6, 13, 13);
            g2.setColor(Color.WHITE);
            g2.fillOval(8, 10, 5, 5);
            g2.setFont(Theme.CODE);
            g2.setColor(Theme.INK);
            g2.drawString(node, 28, 18);
            if (segment != null) {
                g2.setFont(Theme.SMALL);
                g2.setColor(Theme.MUTED);
                g2.drawString(segment + " m", 28, 37);
            }
            g2.dispose();
        }
    }
}
