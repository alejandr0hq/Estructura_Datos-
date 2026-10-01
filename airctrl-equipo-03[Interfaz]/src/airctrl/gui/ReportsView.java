package airctrl.gui;

import airctrl.service.AirCtrlSystem;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Reportes agregados para el supervisor, exportables a CSV. */
public final class ReportsView extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final Map<String, String> SUMMARY_LABELS = Map.of(
            "flights", "Vuelos", "aircraft", "Aeronaves validas", "gates", "Puertas",
            "baggage", "Equipajes", "connections", "Conexiones", "locations", "Ubicaciones",
            "validRoutes", "Conexiones con distancia", "incidents", "Incidentes",
            "pendingOperations", "Operaciones pendientes", "dataWarnings", "Advertencias de datos");

    private final AppContext ctx;
    private final JPanel grid = new JPanel(new GridLayout(0, 2, 16, 16));
    private final DataTable<Map.Entry<String, Integer>> summary = new DataTable<>(
            new DataTable.Column<>("Indicador", e -> SUMMARY_LABELS.getOrDefault(e.getKey(), e.getKey()),
                    DataTable.Style.TEXT, 220),
            new DataTable.Column<>("Total", Map.Entry::getValue, DataTable.Style.NUMBER, 80));

    public ReportsView(AppContext ctx) {
        super(new BorderLayout());
        this.ctx = ctx;
        setBackground(Theme.BG);

        JButton export = Ui.secondary("Exportar a CSV");
        export.addActionListener(event -> exportCsv());
        JPanel page = Ui.page();
        page.add(Ui.pageHeader("Reportes", "Conteos por estado y por tipo, ordenados alfabeticamente.", export),
                BorderLayout.NORTH);
        grid.setOpaque(false);

        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.setOpaque(false);
        JPanel gridWrap = new JPanel(new BorderLayout());
        gridWrap.setOpaque(false);
        gridWrap.add(grid, BorderLayout.NORTH);
        content.add(gridWrap, BorderLayout.CENTER);
        Ui.Surface side = Ui.surface("Resumen general", summary.scroll());
        side.setPreferredSize(new java.awt.Dimension(340, 420));
        JPanel sideWrap = new JPanel(new BorderLayout());
        sideWrap.setOpaque(false);
        sideWrap.add(side, BorderLayout.NORTH);
        content.add(sideWrap, BorderLayout.EAST);
        page.add(Ui.scroll(content), BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);

        ctx.onChange(this::refresh);
        refresh();
    }

    public void refresh() {
        AirCtrlSystem system = ctx.system();
        grid.removeAll();
        for (Map.Entry<String, Map<String, Integer>> report : system.reports().entrySet()) {
            boolean byStatus = !report.getKey().contains("tipo");
            BarChart chart = new BarChart(
                    key -> byStatus ? Theme.status(key) : Theme.ACCENT,
                    key -> byStatus ? Theme.statusLabel(key) : key);
            chart.setData(report.getValue());
            grid.add(Ui.surface(report.getKey(), chart));
        }
        grid.revalidate();
        grid.repaint();
        summary.setRows(new ArrayList<>(system.summary().entrySet()));
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("reporte-airctrl-"
                + DateTimeFormatter.ofPattern("yyyyMMdd-HHmm").format(LocalDateTime.now()) + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        List<String> lines = new ArrayList<>();
        lines.add("reporte,categoria,total");
        for (Map.Entry<String, Integer> entry : ctx.system().summary().entrySet()) {
            lines.add("Resumen," + SUMMARY_LABELS.getOrDefault(entry.getKey(), entry.getKey()) + "," + entry.getValue());
        }
        for (Map.Entry<String, Map<String, Integer>> report : ctx.system().reports().entrySet()) {
            for (Map.Entry<String, Integer> entry : report.getValue().entrySet()) {
                lines.add(report.getKey() + "," + entry.getKey() + "," + entry.getValue());
            }
        }
        try {
            Files.write(chooser.getSelectedFile().toPath(), lines, StandardCharsets.UTF_8);
            ctx.success("Reporte guardado en " + chooser.getSelectedFile().getName());
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar: " + exception.getMessage());
        }
    }
}
