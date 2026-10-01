package airctrl.gui;

import airctrl.model.Incident;
import airctrl.service.AirCtrlSystem;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Incidentes: se registran aqui y se resuelven al atenderlos en la cola. */
public final class IncidentsView extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final String[] TYPES = {"SEGURIDAD", "MEDICO", "EQUIPAJE", "FALLA_TECNICA", "CLIMA", "PISTA", "OTRO"};

    private final AppContext ctx;
    private final JLabel count = Ui.muted("");
    private final Details details = new Details("Selecciona un incidente para ver su descripcion completa.");
    private final DataTable<Incident> table = new DataTable<>(
            new DataTable.Column<>("Incidente", Incident::getIncidentId, DataTable.Style.CODE, 90),
            new DataTable.Column<>("Tipo", Incident::getType, DataTable.Style.TEXT, 110),
            new DataTable.Column<>("Descripcion", Incident::getDescription, DataTable.Style.TEXT, 260),
            new DataTable.Column<>("Prioridad", Incident::getPriority, DataTable.Style.PRIORITY, 90),
            new DataTable.Column<>("Registrado", i -> TIME.format(i.getCreatedAt()), DataTable.Style.TEXT, 90),
            new DataTable.Column<>("Estado", i -> i.isResolved() ? "RESUELTO" : "ABIERTO", DataTable.Style.STATUS, 110));

    public IncidentsView(AppContext ctx) {
        super(new BorderLayout());
        this.ctx = ctx;
        setBackground(Theme.BG);

        JButton add = Ui.primary("Registrar incidente");
        add.addActionListener(event -> openRegisterDialog(this, ctx));
        JPanel page = Ui.page();
        page.add(Ui.pageHeader("Incidentes",
                "Cada incidente entra a la cola de operaciones con su prioridad y se resuelve al atenderlo.", add),
                BorderLayout.NORTH);

        table.setEmptyMessage("Sin incidentes. Usa Registrar incidente cuando ocurra algo que atender.");
        table.onSelect(this::showDetails);
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.add(count, BorderLayout.EAST);
        center.add(toolbar, BorderLayout.NORTH);
        center.add(table.scroll(), BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);
        body.add(center, BorderLayout.CENTER);
        body.add(details, BorderLayout.EAST);
        page.add(body, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);

        ctx.onChange(this::refresh);
        refresh();
    }

    public void refresh() {
        List<Incident> incidents = new ArrayList<>(ctx.system().getIncidents());
        incidents.sort(Comparator.comparing(Incident::isResolved)
                .thenComparing(Comparator.comparingInt(Incident::getPriority).reversed())
                .thenComparing(Incident::getCreatedAt));
        table.setRows(incidents);
        long open = incidents.stream().filter(i -> !i.isResolved()).count();
        count.setText(open + " abiertos de " + incidents.size());
        showDetails();
    }

    private void showDetails() {
        Incident incident = table.selected();
        if (incident == null) {
            details.clear();
            return;
        }
        details.begin(incident.getIncidentId(), incident.getType())
                .status("Estado", incident.isResolved() ? "RESUELTO" : "ABIERTO")
                .row("Prioridad", incident.getPriority() + " (" + Theme.priorityName(incident.getPriority()) + ")",
                        Theme.mix(Theme.priority(incident.getPriority()), Theme.INK, 0.3))
                .row("Registrado", TIME.format(incident.getCreatedAt()))
                .note(incident.getDescription(), Theme.INK);
        if (!incident.isResolved()) {
            JButton go = Ui.secondary("Ver en la cola de operaciones");
            go.addActionListener(event -> ctx.navigate("operations"));
            details.action(go);
        }
        details.done();
    }

    static void openRegisterDialog(Component parent, AppContext ctx) {
        AirCtrlSystem system = ctx.system();
        JTextField id = FormDialog.codeField(FormDialog.nextId("INC", 3, system.getStore().getIncidents().keySet()));
        JComboBox<String> type = new JComboBox<>(TYPES);
        type.setEditable(true);
        JTextArea description = new JTextArea(4, 28);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setFont(Theme.BODY);
        JScrollPane descriptionScroll = new JScrollPane(description);
        descriptionScroll.setPreferredSize(new Dimension(300, 90));
        JLabel priorityText = Ui.label("", Theme.SMALL_BOLD, Theme.INK);
        JSpinner priority = FormDialog.prioritySpinner(3, priorityText);

        new FormDialog(parent, "Registrar incidente",
                "5 es emergencia (se atiende antes que todo), 1 es una operacion rutinaria.")
                .field("Identificador", id)
                .field("Tipo", type, "Elige uno o escribe otro")
                .field("Descripcion", (JComponent) descriptionScroll)
                .field("Prioridad", FormDialog.withTrailing(priority, priorityText))
                .onSubmit("Registrar incidente", () -> {
                    String incidentId = FormDialog.required(id, "El identificador");
                    String typeName = FormDialog.selected(type).toUpperCase().replace(' ', '_');
                    if (typeName.isEmpty()) {
                        throw new IllegalArgumentException("El tipo es obligatorio");
                    }
                    String text = description.getText().trim();
                    if (text.isEmpty()) {
                        description.requestFocusInWindow();
                        throw new IllegalArgumentException("Describe brevemente que paso");
                    }
                    system.registerIncident(new Incident(incidentId, typeName, text, (Integer) priority.getValue()));
                    ctx.refreshAll();
                    ctx.success("Incidente " + incidentId + " registrado y agregado a la cola");
                })
                .open();
    }
}
