package airctrl.gui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

/** Fabricas de componentes con el estilo de la aplicacion. */
public final class Ui {
    public enum Kind { PRIMARY, SECONDARY, DANGER, GHOST }

    private Ui() {
    }

    public static void installLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.put("control", Theme.BG);
                    UIManager.put("nimbusBase", Theme.NAVY);
                    UIManager.put("nimbusBlueGrey", new Color(0xB9C3D1));
                    UIManager.put("nimbusFocus", Theme.ACCENT);
                    UIManager.put("nimbusSelectionBackground", Theme.ACCENT);
                    UIManager.put("nimbusSelectedText", Color.WHITE);
                    UIManager.put("text", Theme.INK);
                    UIManager.put("defaultFont", Theme.BODY);
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Si Nimbus no esta disponible se conserva el look por defecto.
        }
        UIManager.put("ToolTip.font", Theme.SMALL);
        UIManager.put("OptionPane.messageFont", Theme.BODY);
        UIManager.put("OptionPane.buttonFont", Theme.BODY);
    }

    // ---------------------------------------------------------------- texto

    public static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    public static JLabel muted(String text) {
        return label(text, Theme.SMALL, Theme.MUTED);
    }

    public static JLabel h2(String text) {
        return label(text, Theme.H2, Theme.INK);
    }

    /** Encabezado de pantalla: titulo, descripcion y acciones a la derecha. */
    public static JPanel pageHeader(String title, String description, JComponent... actions) {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        JLabel titleLabel = label(title, Theme.H1, Theme.INK);
        JLabel descriptionLabel = label(description, Theme.BODY, Theme.MUTED);
        titleLabel.setAlignmentX(0);
        descriptionLabel.setAlignmentX(0);
        texts.add(titleLabel);
        texts.add(Box.createVerticalStrut(4));
        texts.add(descriptionLabel);
        header.add(texts, BorderLayout.CENTER);
        if (actions.length > 0) {
            header.add(row(actions), BorderLayout.EAST);
        }
        return header;
    }

    public static JPanel row(JComponent... items) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);
        for (JComponent item : items) {
            panel.add(item);
        }
        return panel;
    }

    public static JPanel leftRow(int gap, JComponent... items) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, 0));
        panel.setOpaque(false);
        for (JComponent item : items) {
            panel.add(item);
        }
        return panel;
    }

    // ---------------------------------------------------------------- botones

    public static JButton button(String text, Kind kind) {
        return new FlatButton(text, kind);
    }

    public static JButton primary(String text) {
        return button(text, Kind.PRIMARY);
    }

    public static JButton secondary(String text) {
        return button(text, Kind.SECONDARY);
    }

    public static final class FlatButton extends JButton {
        private static final long serialVersionUID = 1L;
        private final Kind kind;

        public FlatButton(String text, Kind kind) {
            super(text);
            this.kind = kind;
            setUI(new BasicButtonUI());
            setFont(Theme.BODY_BOLD);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 14, 8, 14));
            setForeground(switch (kind) {
                case PRIMARY, DANGER -> Color.WHITE;
                case SECONDARY -> Theme.INK;
                case GHOST -> Theme.ACCENT;
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = getModel().isRollover();
            boolean pressed = getModel().isPressed();
            Color fill;
            Color border = null;
            switch (kind) {
                case PRIMARY -> fill = pressed ? Theme.ACCENT_DARK.darker() : hover ? Theme.ACCENT_DARK : Theme.ACCENT;
                case DANGER -> fill = pressed ? Theme.DANGER.darker().darker() : hover ? Theme.DANGER.darker() : Theme.DANGER;
                case SECONDARY -> {
                    fill = pressed ? new Color(0xE3E8EF) : hover ? Theme.SURFACE_ALT : Theme.SURFACE;
                    border = Theme.LINE;
                }
                default -> fill = hover ? Theme.tint(Theme.ACCENT, 24) : new Color(0, 0, 0, 0);
            }
            if (!isEnabled()) {
                fill = kind == Kind.GHOST ? new Color(0, 0, 0, 0) : new Color(0xE6EAF0);
                border = null;
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }
            if (isFocusOwner()) {
                g2.setColor(Theme.tint(Theme.ACCENT, 140));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
            }
            g2.dispose();
            setForeground(isEnabled() ? switch (kind) {
                case PRIMARY, DANGER -> Color.WHITE;
                case SECONDARY -> Theme.INK;
                case GHOST -> Theme.ACCENT;
            } : new Color(0x9AA5B4));
            super.paintComponent(g);
        }
    }

    // ---------------------------------------------------------------- contenedores

    /** Superficie blanca con borde fino. */
    public static class Surface extends JPanel {
        private static final long serialVersionUID = 1L;

        public Surface(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(new EmptyBorder(16, 16, 16, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.SURFACE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.setColor(Theme.LINE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static Surface surface(String title, JComponent content) {
        Surface surface = new Surface(new BorderLayout(0, 12));
        if (title != null) {
            surface.add(h2(title), BorderLayout.NORTH);
        }
        surface.add(content, BorderLayout.CENTER);
        return surface;
    }

    public static JScrollPane scroll(Component view) {
        JScrollPane scroll = new JScrollPane(new WidthTracking(view));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /**
     * Envuelve un componente para que GridBagLayout pueda angostarlo sin colapsar su alto
     * (GridBag usa el tamano minimo cuando no cabe el preferido).
     */
    public static JPanel shrinkable(Component view) {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(0, getPreferredSize().height);
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                return new Dimension(Math.min(size.width, 200), size.height);
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(view, BorderLayout.CENTER);
        return wrapper;
    }

    /** Contenedor que se ajusta al ancho visible: solo hay desplazamiento vertical. */
    static final class WidthTracking extends JPanel implements javax.swing.Scrollable {
        private static final long serialVersionUID = 1L;

        WidthTracking(Component view) {
            super(new BorderLayout());
            setOpaque(false);
            add(view, BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visible, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visible, int orientation, int direction) {
            return Math.max(16, visible.height - 32);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() != null && getParent().getHeight() > getPreferredSize().height;
        }
    }

    public static JPanel page() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(Theme.BG);
        page.setBorder(new EmptyBorder(24, 28, 20, 28));
        return page;
    }

    // ---------------------------------------------------------------- campos

    public static Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE), new EmptyBorder(6, 8, 6, 8));
    }

    public static JTextField field(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(Theme.BODY);
        return field;
    }

    /** Pastilla de color con texto, usada para estados. */
    public static void paintBadge(Graphics2D g2, String text, Color color, int x, int centerY, Font font) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int width = fm.stringWidth(text) + 18;
        int height = fm.getHeight() + 4;
        int y = centerY - height / 2;
        g2.setColor(Theme.tint(color, 34));
        g2.fillRoundRect(x, y, width, height, height, height);
        g2.setColor(color);
        g2.fillOval(x + 7, centerY - 3, 6, 6);
        g2.setColor(Theme.mix(color, Theme.INK, 0.35));
        g2.drawString(text, x + 16, centerY + fm.getAscent() / 2 - 2);
    }

    /** Barra de prioridad de cinco segmentos. */
    public static void paintPriority(Graphics2D g2, int priority, int x, int centerY, boolean withNumber) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color color = Theme.priority(priority);
        for (int i = 0; i < 5; i++) {
            g2.setColor(i < priority ? color : new Color(0xE1E6ED));
            g2.fillRoundRect(x + i * 9, centerY - 6, 7, 12, 3, 3);
        }
        if (withNumber) {
            g2.setFont(Theme.SMALL_BOLD);
            g2.setColor(Theme.mix(color, Theme.INK, 0.3));
            g2.drawString(String.valueOf(priority), x + 50, centerY + 5);
        }
    }

    public static Insets insets(int all) {
        return new Insets(all, all, all, all);
    }

    public static Dimension size(int w, int h) {
        return new Dimension(w, h);
    }
}
