package airctrl.gui;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/** Grafica de barras horizontales sencilla, pintada a mano. */
public final class BarChart extends JComponent {
    private static final long serialVersionUID = 1L;
    private static final int ROW = 28;

    private Map<String, Integer> data = new LinkedHashMap<>();
    private final Function<String, Color> colors;
    private final Function<String, String> labels;

    public BarChart(Function<String, Color> colors, Function<String, String> labels) {
        this.colors = colors;
        this.labels = labels;
        setOpaque(false);
    }

    public void setData(Map<String, Integer> data) {
        this.data = new LinkedHashMap<>(data);
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(260, Math.max(ROW, data.size() * ROW) + 4);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (data.isEmpty()) {
            g2.setFont(Theme.SMALL);
            g2.setColor(Theme.MUTED);
            g2.drawString("Sin registros todavia", 0, 18);
            g2.dispose();
            return;
        }
        int max = 1;
        for (int value : data.values()) {
            max = Math.max(max, value);
        }
        g2.setFont(Theme.SMALL);
        FontMetrics fm = g2.getFontMetrics();
        int labelWidth = 0;
        for (String key : data.keySet()) {
            labelWidth = Math.max(labelWidth, fm.stringWidth(labels.apply(key)));
        }
        labelWidth = Math.min(labelWidth + 12, getWidth() / 2);
        int valueWidth = 34;
        int barArea = Math.max(20, getWidth() - labelWidth - valueWidth);
        int y = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int center = y + ROW / 2;
            g2.setColor(Theme.INK);
            g2.setFont(Theme.SMALL);
            g2.drawString(labels.apply(entry.getKey()), 0, center + fm.getAscent() / 2 - 2);
            g2.setColor(new Color(0xEDF0F4));
            g2.fillRoundRect(labelWidth, center - 7, barArea, 14, 6, 6);
            int width = (int) Math.round(barArea * (entry.getValue() / (double) max));
            g2.setColor(colors.apply(entry.getKey()));
            g2.fillRoundRect(labelWidth, center - 7, Math.max(4, width), 14, 6, 6);
            g2.setFont(Theme.SMALL_BOLD);
            g2.setColor(Theme.INK);
            String value = String.valueOf(entry.getValue());
            g2.drawString(value, getWidth() - g2.getFontMetrics().stringWidth(value), center + fm.getAscent() / 2 - 2);
            y += ROW;
        }
        g2.dispose();
    }
}
