package airctrl.gui;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Problemas encontrados en los datos; no detienen la operacion. */
public final class WarningsView extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final String ALL = "Todas";

    private final AppContext ctx;
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private String filter = ALL;

    public WarningsView(AppContext ctx) {
        super(new BorderLayout());
        this.ctx = ctx;
        setBackground(Theme.BG);

        JPanel page = Ui.page();
        page.add(Ui.pageHeader("Advertencias de datos",
                "Filas invalidas o referencias rotas. Se reportan y el resto del sistema sigue funcionando."),
                BorderLayout.NORTH);

        JList<String> list = new JList<>(model);
        list.setFont(Theme.BODY);
        list.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> l, Object value, int index,
                                                          boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(l, value, index, selected, false);
                label.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 1, 0, new java.awt.Color(0xE8ECF1)), new EmptyBorder(9, 14, 9, 14)));
                label.setForeground(Theme.INK);
                label.setBackground(selected ? Theme.SELECTION : Theme.SURFACE);
                label.setText("<html><b>" + category(String.valueOf(value)) + "</b>&nbsp;&nbsp;" + value + "</html>");
                return label;
            }
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(new MatteBorder(1, 1, 1, 1, Theme.LINE));

        chips.setOpaque(false);
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(chips, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        page.add(center, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);

        ctx.onChange(this::refresh);
        refresh();
    }

    public void refresh() {
        List<String> warnings = ctx.system().getWarnings();
        Map<String, Integer> byCategory = new LinkedHashMap<>();
        for (String warning : warnings) {
            byCategory.merge(category(warning), 1, Integer::sum);
        }
        chips.removeAll();
        ButtonGroup group = new ButtonGroup();
        addChip(group, ALL, "Todas " + warnings.size());
        byCategory.forEach((category, count) -> addChip(group, category, category + " " + count));
        chips.revalidate();
        chips.repaint();
        model.clear();
        for (String warning : warnings) {
            if (ALL.equals(filter) || category(warning).equals(filter)) {
                model.addElement(warning);
            }
        }
        if (warnings.isEmpty()) {
            model.addElement("Los datos no presentan problemas.");
        }
    }

    private void addChip(ButtonGroup group, String key, String text) {
        JToggleButton chip = new GatesView.Chip(text);
        chip.setSelected(key.equals(filter));
        chip.addActionListener(event -> {
            filter = key;
            refresh();
        });
        group.add(chip);
        chips.add(chip);
    }

    static String category(String warning) {
        if (warning.startsWith("aircraft.csv")) return "Aeronaves";
        if (warning.startsWith("flights.csv") || warning.startsWith("Vuelo")) return "Vuelos";
        if (warning.startsWith("gates.csv") || warning.startsWith("Puerta")) return "Puertas";
        if (warning.startsWith("baggage.csv") || warning.startsWith("Equipaje")) return "Equipaje";
        if (warning.startsWith("connections.csv")) return "Conexiones";
        return "Otros";
    }
}
