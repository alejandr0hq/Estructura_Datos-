package airctrl.gui;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Panel lateral de detalle con pares etiqueta/valor y acciones. */
public final class Details extends Ui.Surface {
    private static final long serialVersionUID = 1L;

    private final JPanel body = new JPanel();
    private final JPanel actions = new JPanel();
    private final String emptyText;

    public Details(String emptyText) {
        super(new BorderLayout(0, 12));
        this.emptyText = emptyText;
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        add(body, BorderLayout.NORTH);
        add(actions, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(310, 200));
        clear();
    }

    public void clear() {
        body.removeAll();
        actions.removeAll();
        JLabel label = Ui.label("<html>" + emptyText + "</html>", Theme.BODY, Theme.MUTED);
        label.setAlignmentX(0);
        body.add(label);
        refreshLayout();
    }

    public Details begin(String code, String subtitle) {
        body.removeAll();
        actions.removeAll();
        JLabel title = Ui.label(code, Theme.CODE_LARGE.deriveFont(22f), Theme.INK);
        title.setAlignmentX(0);
        body.add(title);
        if (subtitle != null) {
            JLabel sub = Ui.label("<html>" + subtitle + "</html>", Theme.BODY, Theme.MUTED);
            sub.setAlignmentX(0);
            body.add(Box.createVerticalStrut(2));
            body.add(sub);
        }
        body.add(Box.createVerticalStrut(12));
        return this;
    }

    public Details row(String label, String value) {
        return row(label, value, Theme.INK);
    }

    public Details row(String label, String value, Color color) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new MatteBorder(1, 0, 0, 0, new Color(0xE8ECF1)));
        JLabel key = Ui.label(label, Theme.SMALL, Theme.MUTED);
        key.setBorder(new EmptyBorder(8, 0, 8, 0));
        JLabel val = Ui.label("<html><div style='text-align:right'>" + (value == null || value.isBlank() ? "—" : value)
                + "</div></html>", Theme.BODY_BOLD, color);
        row.add(key, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        row.setAlignmentX(0);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        body.add(row);
        return this;
    }

    public Details status(String label, String status) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new MatteBorder(1, 0, 0, 0, new Color(0xE8ECF1)));
        JLabel key = Ui.label(label, Theme.SMALL, Theme.MUTED);
        key.setBorder(new EmptyBorder(8, 0, 8, 0));
        row.add(key, BorderLayout.WEST);
        row.add(new Badge(status), BorderLayout.EAST);
        row.setAlignmentX(0);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        body.add(row);
        return this;
    }

    public Details note(String text, Color color) {
        JLabel note = Ui.label("<html>" + text + "</html>", Theme.SMALL, color);
        note.setBorder(new EmptyBorder(10, 0, 0, 0));
        note.setAlignmentX(0);
        body.add(note);
        return this;
    }

    public Details action(JComponent button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        actions.add(button);
        actions.add(Box.createVerticalStrut(8));
        return this;
    }

    public void done() {
        refreshLayout();
    }

    private void refreshLayout() {
        revalidate();
        repaint();
    }

    /** Insignia de estado reutilizable fuera de tablas. */
    public static final class Badge extends JComponent {
        private static final long serialVersionUID = 1L;
        private final String status;

        public Badge(String status) {
            this.status = status;
        }

        @Override
        public Dimension getPreferredSize() {
            int width = getFontMetrics(Theme.SMALL_BOLD).stringWidth(Theme.statusLabel(status)) + 20;
            return new Dimension(width, 34);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Ui.paintBadge(g2, Theme.statusLabel(status), Theme.status(status), 1, getHeight() / 2, Theme.SMALL_BOLD);
            g2.dispose();
        }
    }
}
