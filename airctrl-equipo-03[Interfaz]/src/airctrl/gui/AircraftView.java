package airctrl.gui;

import airctrl.model.Aircraft;
import airctrl.model.Flight;
import airctrl.service.AirCtrlSystem;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** Flota: capacidad, estado y cuantos vuelos usa cada aeronave. */
public final class AircraftView extends JPanel {
    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final Map<String, Integer> usage = new HashMap<>();
    private final JTextField search = Ui.field(16);
    private final JLabel count = Ui.muted("");
    private final Details details = new Details("Selecciona una aeronave para ver los vuelos que la usan.");
    private final DataTable<Aircraft> table;

    public AircraftView(AppContext ctx) {
        super(new BorderLayout());
        this.ctx = ctx;
        setBackground(Theme.BG);
        table = new DataTable<>(
                new DataTable.Column<>("Aeronave", Aircraft::aircraftId, DataTable.Style.CODE, 90),
                new DataTable.Column<>("Modelo", Aircraft::model, DataTable.Style.TEXT, 180),
                new DataTable.Column<>("Capacidad", Aircraft::capacity, DataTable.Style.NUMBER, 80),
                new DataTable.Column<>("Estado", Aircraft::status, DataTable.Style.STATUS, 130),
                new DataTable.Column<>("Vuelos", a -> usage.getOrDefault(a.aircraftId(), 0), DataTable.Style.NUMBER, 70));

        JButton add = Ui.primary("Registrar aeronave");
        add.addActionListener(event -> openRegisterDialog());
        JPanel page = Ui.page();
        page.add(Ui.pageHeader("Aeronaves",
                "Flota disponible. Una aeronave sin vuelos asignados es candidata para un vuelo extra.", add),
                BorderLayout.NORTH);

        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.add(Ui.leftRow(8, Ui.label("Filtrar", Theme.SMALL_BOLD, Theme.INK), search,
                Ui.muted("por identificador o modelo")), BorderLayout.WEST);
        toolbar.add(count, BorderLayout.EAST);

        table.setEmptyMessage("Ninguna aeronave coincide con el filtro.");
        table.onSelect(this::showDetails);
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
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
        usage.clear();
        for (Flight flight : ctx.system().getFlights()) {
            usage.merge(flight.getAircraftId(), 1, Integer::sum);
        }
        applyFilter();
    }

    private void applyFilter() {
        String text = search.getText().trim().toUpperCase();
        List<Aircraft> all = ctx.system().getAircraft();
        List<Aircraft> rows = new ArrayList<>();
        for (Aircraft aircraft : all) {
            if (text.isEmpty() || aircraft.aircraftId().toUpperCase().contains(text)
                    || aircraft.model().toUpperCase().contains(text)) {
                rows.add(aircraft);
            }
        }
        table.setRows(rows);
        count.setText(rows.size() + " de " + all.size() + " aeronaves");
        showDetails();
    }

    private void showDetails() {
        Aircraft aircraft = table.selected();
        if (aircraft == null) {
            details.clear();
            return;
        }
        List<String> flights = new ArrayList<>();
        for (Flight flight : ctx.system().getFlights()) {
            if (flight.getAircraftId().equals(aircraft.aircraftId())) {
                flights.add(flight.getFlightId() + " " + Theme.statusLabel(flight.getStatus()).toLowerCase());
            }
        }
        details.begin(aircraft.aircraftId(), aircraft.model())
                .status("Estado", aircraft.status())
                .row("Capacidad", aircraft.capacity() + " asientos")
                .row("Vuelos asignados", flights.isEmpty() ? "Ninguno" : String.join("<br>", flights));
        if (flights.isEmpty() && "ACTIVE".equals(aircraft.status())) {
            details.note("Activa y sin vuelos: disponible para un vuelo adicional.", Theme.OK);
        }
        details.done();
    }

    private void openRegisterDialog() {
        AirCtrlSystem system = ctx.system();
        JTextField id = FormDialog.codeField(nextId(system));
        JComboBox<String> model = new JComboBox<>();
        model.setEditable(true);
        new TreeSet<>(system.getAircraft().stream().map(Aircraft::model).toList()).forEach(model::addItem);
        JSpinner capacity = new JSpinner(new SpinnerNumberModel(150, 1, 900, 1));
        JComboBox<String> status = new JComboBox<>(new String[]{"ACTIVE", "MAINTENANCE"});
        status.setRenderer(FlightsView.statusRenderer());
        new FormDialog(this, "Registrar aeronave", "La capacidad debe ser mayor que cero.")
                .field("Identificador", id)
                .field("Modelo", model, "Elige uno existente o escribe uno nuevo")
                .field("Capacidad", capacity, "Asientos")
                .field("Estado", status)
                .onSubmit("Registrar aeronave", () -> {
                    String aircraftId = FormDialog.required(id, "El identificador");
                    String modelName = FormDialog.selected(model);
                    if (modelName.isEmpty()) {
                        throw new IllegalArgumentException("El modelo es obligatorio");
                    }
                    Aircraft aircraft = new Aircraft(aircraftId, modelName, (Integer) capacity.getValue(),
                            FormDialog.selected(status));
                    system.registerAircraft(aircraft);
                    ctx.refreshAll();
                    ctx.success("Aeronave " + aircraftId + " registrada");
                    table.select(aircraft);
                })
                .open();
    }

    private static String nextId(AirCtrlSystem system) {
        List<String> ids = new ArrayList<>(system.getStore().getAircraft().keySet());
        for (Flight flight : system.getFlights()) {
            ids.add(flight.getAircraftId());
        }
        return FormDialog.nextId("AC", 3, ids);
    }
}
