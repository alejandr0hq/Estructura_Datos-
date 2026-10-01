package airctrl.gui;

import airctrl.model.Baggage;
import airctrl.service.AirCtrlSystem;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Ventana principal: barra lateral de navegacion, pantallas y avisos. */
public final class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private record Section(String key, String label, Function<AirCtrlSystem, Integer> badge, boolean alarm) {
    }

    private final AppContext ctx;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, JComponent> views = new LinkedHashMap<>();
    private final List<NavItem> navItems = new ArrayList<>();
    private final Toast toast = new Toast();
    private final JLabel source = Ui.label("", Theme.SMALL, Theme.NAVY_TEXT);
    private String current = "dashboard";

    public MainFrame(AppContext ctx) {
        super("AIRCTRL - Centro de Operaciones Aeroportuarias");
        this.ctx = ctx;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (ctx.repository() != null) {
                    ctx.repository().close();
                }
                System.exit(0);
            }
        });

        List<Section> sections = List.of(
                new Section("dashboard", "Panel de control", s -> null, false),
                new Section("operations", "Operaciones", s -> s.getPending().size(), false),
                new Section("flights", "Vuelos", s -> null, false),
                new Section("gates", "Puertas", s -> null, false),
                new Section("aircraft", "Aeronaves", s -> null, false),
                new Section("baggage", "Equipaje", s -> (int) s.getBaggage().stream()
                        .filter(b -> "MISSING".equals(b.status())).count(), true),
                new Section("incidents", "Incidentes", s -> (int) s.getIncidents().stream()
                        .filter(i -> !i.isResolved()).count(), true),
                new Section("routes", "Rutas internas", s -> null, false),
                new Section("reports", "Reportes", s -> null, false),
                new Section("warnings", "Advertencias de datos", s -> s.getWarnings().size(), false));

        views.put("dashboard", new DashboardView(ctx));
        views.put("operations", new OperationsView(ctx));
        views.put("flights", new FlightsView(ctx));
        views.put("gates", new GatesView(ctx));
        views.put("aircraft", new AircraftView(ctx));
        views.put("baggage", new BaggageView(ctx));
        views.put("incidents", new IncidentsView(ctx));
        views.put("routes", new RoutesView(ctx));
        views.put("reports", new ReportsView(ctx));
        views.put("warnings", new WarningsView(ctx));
        views.forEach((key, view) -> content.add(view, key));

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.NAVY);
        sidebar.setPreferredSize(new Dimension(236, 100));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(new EmptyBorder(22, 20, 22, 20));
        JLabel name = Ui.label("AIRCTRL", Theme.CODE_LARGE.deriveFont(22f), Color.WHITE);
        JLabel tag = Ui.label("Centro de operaciones", Theme.SMALL, Theme.NAVY_MUTED);
        brand.add(name);
        brand.add(Box.createVerticalStrut(2));
        brand.add(tag);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(0, 10, 0, 10));
        int shortcut = 1;
        for (Section section : sections) {
            NavItem item = new NavItem(section, shortcut <= 9 ? shortcut : 0);
            navItems.add(item);
            nav.add(item);
            nav.add(Box.createVerticalStrut(2));
            if (shortcut <= 9) {
                int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
                getRootPane().registerKeyboardAction(event -> showSection(section.key(), null),
                        KeyStroke.getKeyStroke(KeyEvent.VK_0 + shortcut, menuMask), JComponent.WHEN_IN_FOCUSED_WINDOW);
            }
            shortcut++;
        }
        sidebar.add(nav, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(new EmptyBorder(12, 20, 18, 20));
        JLabel dot = Ui.label(ctx.isPersistent() ? "Guardando en PostgreSQL" : "Solo lectura de CSV",
                Theme.SMALL_BOLD, ctx.isPersistent() ? new Color(0x7FD1B0) : new Color(0xF2C66D));
        source.setText("<html><div style='width:180px'>" + ctx.sourceDescription() + "</div></html>");
        footer.add(dot);
        footer.add(Box.createVerticalStrut(2));
        footer.add(source);
        footer.add(Box.createVerticalStrut(12));
        JButton reload = sidebarButton("Recargar datos");
        reload.setToolTipText("Vuelve a leer la informacion (F5)");
        reload.addActionListener(event -> reload());
        footer.add(reload);
        getRootPane().registerKeyboardAction(event -> reload(), KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        if (ctx.isPersistent()) {
            footer.add(Box.createVerticalStrut(6));
            JButton reset = sidebarButton("Restablecer desde CSV");
            reset.setToolTipText("Borra los cambios de la base y vuelve a importar los archivos de data/");
            reset.addActionListener(event -> resetDatabase());
            footer.add(reset);
        }
        sidebar.add(footer, BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout());
        main.add(content, BorderLayout.CENTER);
        main.add(toast, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(main, BorderLayout.CENTER);

        ctx.setNotifier(toast::show);
        ctx.setNavigator(this::showSection);
        ctx.onChange(this::refreshNav);
        refreshNav();
        showSection("dashboard", null);

        setMinimumSize(new Dimension(1180, 720));
        setSize(new Dimension(1440, 900));
        setLocationRelativeTo(null);
    }

    private void showSection(String key, String argument) {
        current = key;
        cards.show(content, key);
        JComponent view = views.get(key);
        if (argument != null && view instanceof Navigable navigable) {
            navigable.onNavigate(argument);
        }
        for (NavItem item : navItems) {
            item.repaint();
        }
    }

    private void refreshNav() {
        for (NavItem item : navItems) {
            item.update();
        }
    }

    private void reload() {
        try {
            ctx.reload();
            toast.show("Datos recargados", true);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "No se pudo recargar: " + exception.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetDatabase() {
        int answer = JOptionPane.showConfirmDialog(this,
                "Se borraran vuelos, equipajes, incidentes e historial registrados desde la aplicacion,\n"
                        + "y se volveran a importar los CSV originales de la carpeta data/.\n\n¿Restablecer la base?",
                "Restablecer desde CSV", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            ctx.resetFromCsv();
            toast.show("Base restablecida con los CSV originales", true);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "No se pudo restablecer: " + exception.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JButton sidebarButton(String text) {
        JButton button = new JButton(text) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? Theme.NAVY_SELECTED : Theme.NAVY_HOVER);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                if (isFocusOwner()) {
                    g2.setColor(Theme.NAVY_TEXT);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setFont(Theme.SMALL_BOLD);
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setRolloverEnabled(true);
        button.setBorder(new EmptyBorder(8, 12, 8, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(0);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return button;
    }

    /** Elemento de navegacion con contador opcional. */
    private final class NavItem extends JComponent {
        private static final long serialVersionUID = 1L;
        private final Section section;
        private Integer badge;
        private boolean hover;

        NavItem(Section section, int shortcut) {
            this.section = section;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusable(true);
            if (shortcut > 0) {
                boolean mac = System.getProperty("os.name", "").toLowerCase().contains("mac");
                setToolTipText((mac ? "Cmd+" : "Ctrl+") + shortcut);
            }
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showSection(section.key(), null);
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
            getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "go");
            getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "go");
            getActionMap().put("go", new javax.swing.AbstractAction() {
                private static final long serialVersionUID = 1L;

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    showSection(section.key(), null);
                }
            });
            addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent e) { repaint(); }
                public void focusLost(java.awt.event.FocusEvent e) { repaint(); }
            });
        }

        void update() {
            Integer value = section.badge().apply(ctx.system());
            badge = value == null || value == 0 ? null : value;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(216, 38);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, 38);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            boolean selected = section.key().equals(current);
            if (selected || hover) {
                g2.setColor(selected ? Theme.NAVY_SELECTED : Theme.NAVY_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
            if (selected) {
                g2.setColor(new Color(0x7FB2F0));
                g2.fillRoundRect(0, 9, 3, getHeight() - 18, 3, 3);
            }
            if (isFocusOwner()) {
                g2.setColor(Theme.NAVY_TEXT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }
            g2.setFont(selected ? Theme.BODY_BOLD : Theme.BODY);
            g2.setColor(selected ? Color.WHITE : Theme.NAVY_TEXT);
            g2.drawString(section.label(), 14, 24);
            if (badge != null) {
                String text = String.valueOf(badge);
                g2.setFont(Theme.SMALL_BOLD);
                int w = Math.max(22, g2.getFontMetrics().stringWidth(text) + 12);
                int x = getWidth() - w - 10;
                g2.setColor(section.alarm() ? Theme.DANGER : new Color(0x3A5184));
                g2.fillRoundRect(x, 10, w, 18, 18, 18);
                g2.setColor(Color.WHITE);
                g2.drawString(text, x + (w - g2.getFontMetrics().stringWidth(text)) / 2, 23);
            }
            g2.dispose();
        }
    }

    /** Aviso inferior que desaparece solo. */
    private static final class Toast extends JPanel {
        private static final long serialVersionUID = 1L;
        private final JLabel label = Ui.label(" ", Theme.BODY_BOLD, Color.WHITE);
        private final Timer timer = new Timer(4500, event -> setVisible(false));
        private Color color = Theme.OK;

        Toast() {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(10, 28, 10, 28));
            add(label, BorderLayout.CENTER);
            timer.setRepeats(false);
            setVisible(false);
        }

        void show(String message, boolean ok) {
            color = ok ? new Color(0x1F6B55) : new Color(0xA32E25);
            label.setText(message);
            setVisible(true);
            revalidate();
            repaint();
            timer.restart();
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(color);
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }
    }
}
