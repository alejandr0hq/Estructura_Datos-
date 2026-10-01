package airctrl.gui;

import airctrl.model.Connection;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

/** Mapa del grafo de conexiones internas con distribucion por fuerzas. */
public final class GraphPanel extends JComponent {
    private static final long serialVersionUID = 1L;
    private static final Stroke DASHED = new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            1f, new float[]{5f, 5f}, 0f);

    private record Edge(String a, String b, Integer distance) {
    }

    private final Map<String, Point2D.Double> layout = new LinkedHashMap<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Map<String, Rectangle> hitBoxes = new HashMap<>();
    private List<String> route = List.of();
    private Set<String> reached = Set.of();
    private String origin;
    private String destination;
    private String hover;
    private Consumer<String> onNodeClick = id -> { };

    public GraphPanel() {
        setOpaque(false);
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String node = nodeAt(e.getX(), e.getY());
                if (node != null) {
                    onNodeClick.accept(node);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                String node = nodeAt(e.getX(), e.getY());
                if (!java.util.Objects.equals(node, hover)) {
                    hover = node;
                    setCursor(Cursor.getPredefinedCursor(node == null ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
                    repaint();
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setOnNodeClick(Consumer<String> onNodeClick) {
        this.onNodeClick = onNodeClick;
    }

    public void setConnections(List<Connection> connections) {
        edges.clear();
        Set<String> nodes = new java.util.TreeSet<>();
        for (Connection c : connections) {
            String a = c.sourceLocation().trim().toUpperCase();
            String b = c.targetLocation().trim().toUpperCase();
            if (a.isEmpty() || b.isEmpty()) {
                continue;
            }
            nodes.add(a);
            nodes.add(b);
            Integer d = c.distanceMeters() != null && c.distanceMeters() > 0 ? c.distanceMeters() : null;
            edges.add(new Edge(a, b, d));
        }
        computeLayout(new ArrayList<>(nodes));
        repaint();
    }

    public void setHighlight(List<String> route, Set<String> reached, String origin, String destination) {
        this.route = route == null ? List.of() : route;
        this.reached = reached == null ? Set.of() : reached;
        this.origin = origin;
        this.destination = destination;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(640, 520);
    }

    /** Fruchterman-Reingold con gravedad hacia el centro; semilla fija para que el mapa sea estable. */
    private void computeLayout(List<String> nodes) {
        layout.clear();
        int n = nodes.size();
        if (n == 0) {
            return;
        }
        Random random = new Random(42);
        double[] x = new double[n];
        double[] y = new double[n];
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < n; i++) {
            index.put(nodes.get(i), i);
            double angle = 2 * Math.PI * i / n;
            x[i] = 0.5 + 0.35 * Math.cos(angle) + random.nextDouble() * 0.02;
            y[i] = 0.5 + 0.35 * Math.sin(angle) + random.nextDouble() * 0.02;
        }
        double k = 0.9 * Math.sqrt(1.0 / n);
        double temperature = 0.08;
        for (int iteration = 0; iteration < 500; iteration++) {
            double[] dx = new double[n];
            double[] dy = new double[n];
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    double ddx = x[i] - x[j];
                    double ddy = y[i] - y[j];
                    double dist = Math.max(0.005, Math.hypot(ddx, ddy));
                    double force = k * k / dist;
                    dx[i] += ddx / dist * force;
                    dy[i] += ddy / dist * force;
                    dx[j] -= ddx / dist * force;
                    dy[j] -= ddy / dist * force;
                }
            }
            for (Edge edge : edges) {
                int a = index.get(edge.a());
                int b = index.get(edge.b());
                double ddx = x[a] - x[b];
                double ddy = y[a] - y[b];
                double dist = Math.max(0.005, Math.hypot(ddx, ddy));
                double force = dist * dist / k;
                dx[a] -= ddx / dist * force;
                dy[a] -= ddy / dist * force;
                dx[b] += ddx / dist * force;
                dy[b] += ddy / dist * force;
            }
            for (int i = 0; i < n; i++) {
                dx[i] += (0.5 - x[i]) * 0.9;
                dy[i] += (0.5 - y[i]) * 0.9;
                double len = Math.max(1e-9, Math.hypot(dx[i], dy[i]));
                double step = Math.min(len, temperature);
                x[i] += dx[i] / len * step;
                y[i] += dy[i] / len * step;
            }
            temperature *= 0.992;
        }
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            minX = Math.min(minX, x[i]);
            maxX = Math.max(maxX, x[i]);
            minY = Math.min(minY, y[i]);
            maxY = Math.max(maxY, y[i]);
        }
        double spanX = Math.max(1e-6, maxX - minX);
        double spanY = Math.max(1e-6, maxY - minY);
        for (int i = 0; i < n; i++) {
            layout.put(nodes.get(i), new Point2D.Double((x[i] - minX) / spanX, (y[i] - minY) / spanY));
        }
    }

    private Point2D.Double screen(String node) {
        Point2D.Double p = layout.get(node);
        int marginX = 60;
        int marginY = 30;
        return new Point2D.Double(marginX + p.x * (getWidth() - 2 * marginX),
                marginY + p.y * (getHeight() - 2 * marginY));
    }

    private String nodeAt(int x, int y) {
        for (Map.Entry<String, Rectangle> entry : hitBoxes.entrySet()) {
            if (entry.getValue().contains(x, y)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Color kindColor(String node) {
        if (node.startsWith("BELT")) {
            return new Color(0xB7791F);
        }
        if (node.startsWith("BAGROOM")) {
            return new Color(0x7B5EA7);
        }
        if (node.startsWith("CHECKIN")) {
            return Theme.OK;
        }
        if (node.startsWith("RAMP")) {
            return new Color(0x4A5566);
        }
        return Theme.ACCENT;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (layout.isEmpty()) {
            g2.setFont(Theme.BODY);
            g2.setColor(Theme.MUTED);
            g2.drawString("No hay conexiones cargadas.", 20, 30);
            g2.dispose();
            return;
        }
        Set<String> routeEdges = new HashSet<>();
        for (int i = 0; i + 1 < route.size(); i++) {
            routeEdges.add(route.get(i) + "|" + route.get(i + 1));
            routeEdges.add(route.get(i + 1) + "|" + route.get(i));
        }
        boolean dimming = !reached.isEmpty() || !route.isEmpty();

        for (Edge edge : edges) {
            Point2D.Double a = screen(edge.a());
            Point2D.Double b = screen(edge.b());
            boolean onRoute = routeEdges.contains(edge.a() + "|" + edge.b()) && edge.distance() != null;
            boolean lit = !dimming || onRoute || (reached.contains(edge.a()) && reached.contains(edge.b()));
            if (onRoute) {
                g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(Theme.ACCENT);
            } else {
                g2.setStroke(edge.distance() == null ? DASHED : new BasicStroke(1.5f));
                g2.setColor(lit ? new Color(0xA3AEBD) : new Color(0xE1E6ED));
            }
            g2.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
            if (edge.distance() != null && lit) {
                String text = edge.distance() + " m";
                g2.setFont(Theme.SMALL);
                FontMetrics fm = g2.getFontMetrics();
                int mx = (int) ((a.x + b.x) / 2);
                int my = (int) ((a.y + b.y) / 2);
                int w = fm.stringWidth(text) + 6;
                g2.setColor(new Color(255, 255, 255, 220));
                g2.fillRoundRect(mx - w / 2, my - 8, w, 15, 6, 6);
                g2.setColor(onRoute ? Theme.ACCENT_DARK : Theme.MUTED);
                g2.drawString(text, mx - w / 2 + 3, my + 4);
            }
        }

        hitBoxes.clear();
        Set<String> onRoute = new HashSet<>(route);
        for (String node : layout.keySet()) {
            Point2D.Double p = screen(node);
            g2.setFont(Theme.SMALL_BOLD);
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(node) + 16;
            int h = 22;
            int x = (int) p.x - w / 2;
            int y = (int) p.y - h / 2;
            hitBoxes.put(node, new Rectangle(x - 2, y - 2, w + 4, h + 4));
            Color kind = kindColor(node);
            boolean lit = !dimming || reached.contains(node) || onRoute.contains(node);
            boolean strong = onRoute.contains(node) || node.equals(origin) || node.equals(destination);
            Color fill = strong ? kind : lit ? Theme.mix(kind, Color.WHITE, 0.82) : new Color(0xF3F5F8);
            g2.setColor(fill);
            g2.fillRoundRect(x, y, w, h, 8, 8);
            g2.setStroke(new BasicStroke(node.equals(hover) ? 2f : 1f));
            g2.setColor(lit ? kind : new Color(0xD5DBE3));
            g2.drawRoundRect(x, y, w, h, 8, 8);
            if (node.equals(origin) || node.equals(destination)) {
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(Theme.INK);
                g2.drawRoundRect(x - 3, y - 3, w + 6, h + 6, 10, 10);
            }
            g2.setColor(strong ? Color.WHITE : lit ? Theme.mix(kind, Theme.INK, 0.45) : new Color(0xB3BCC8));
            g2.drawString(node, x + 8, y + 15);
        }
        g2.dispose();
    }
}
