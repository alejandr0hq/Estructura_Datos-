package airctrl.gui;

import airctrl.model.Aircraft;
import airctrl.model.Baggage;
import airctrl.model.Flight;
import airctrl.model.Gate;
import airctrl.service.AirCtrlException;
import airctrl.service.AirCtrlSystem;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/** Itinerario: busqueda por identificador (tabla hash), aerolinea y estado. */
public final class FlightsView extends JPanel implements Navigable {
    private static final long serialVersionUID = 1L;
    private static final String ALL_AIRLINES = "Todas las aerolineas";
    private static final String ALL_STATUSES = "Todos los estados";

    private final AppContext ctx;
    private final JTextField search = Ui.field(12);
    private final JComboBox<String> airline = new JComboBox<>();
    private final JComboBox<String> status = new JComboBox<>();
    private final JLabel count = Ui.muted("");
    private final Details details = new Details("Selecciona un vuelo para ver su aeronave, puerta y equipaje.");
    private final DataTable<Flight> table = new DataTable<>(
            new DataTable.Column<>("Vuelo", Flight::getFlightId, DataTable.Style.CODE, 80),
            new DataTable.Column<>("Aerolinea", Flight::getAirline, DataTable.Style.TEXT, 100),
            new DataTable.Column<>("Origen", Flight::getOrigin, DataTable.Style.CODE, 60),
            new DataTable.Column<>("Destino", Flight::getDestination, DataTable.Style.CODE, 60),
            new DataTable.Column<>("Aeronave", Flight::getAircraftId, DataTable.Style.CODE, 80),
            new DataTable.Column<>("Puerta", Flight::getGate, DataTable.Style.CODE, 60),
            new DataTable.Column<>("Estado", Flight::getStatus, DataTable.Style.STATUS, 120),
            new DataTable.Column<>("Prioridad", Flight::getPriority, DataTable.Style.PRIORITY, 90));
    private boolean updatingFilters;

    public FlightsView(AppContext ctx) {
        super(new BorderLayout());
        this.ctx = ctx;
        setBackground(Theme.BG);

        JButton add = Ui.primary("Nuevo vuelo");
        add.addActionListener(event -> openRegisterDialog());
        JPanel page = Ui.page();
        page.add(Ui.pageHeader("Vuelos", "Itinerario del dia. Busca un vuelo exacto o filtra por aerolinea y estado.", add),
                BorderLayout.NORTH);

        search.setToolTipText("Numero de vuelo exacto, por ejemplo AM101");
        search.putClientProperty("JTextField.placeholderText", "AM101");
        JButton find = Ui.secondary("Buscar vuelo");
        find.addActionListener(event -> findExact());
        FormDialog.bindEnter(search, this::findExact);
        JButton clear = Ui.button("Limpiar filtros", Ui.Kind.GHOST);
        clear.addActionListener(event -> {
            search.setText("");
            airline.setSelectedItem(ALL_AIRLINES);
            status.setSelectedItem(ALL_STATUSES);
        });
        airline.addActionListener(event -> { if (!updatingFilters) applyFilters(); });
        status.addActionListener(event -> { if (!updatingFilters) applyFilters(); });

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.add(Ui.leftRow(8, Ui.label("Numero de vuelo", Theme.SMALL_BOLD, Theme.INK), search, find,
                airline, status, clear), BorderLayout.WEST);
        toolbar.add(count, BorderLayout.EAST);

        table.setEmptyMessage("Ningun vuelo coincide con los filtros.");
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

    @Override
    public void onNavigate(String flightId) {
        if (flightId != null) {
            search.setText(flightId);
            findExact();
        }
    }

    public void refresh() {
        AirCtrlSystem system = ctx.system();
        updatingFilters = true;
        Object currentAirline = airline.getSelectedItem();
        Object currentStatus = status.getSelectedItem();
        TreeSet<String> airlines = new TreeSet<>();
        TreeSet<String> statuses = new TreeSet<>(AirCtrlSystem.FLIGHT_STATUSES);
        for (Flight flight : system.getFlights()) {
            airlines.add(flight.getAirline());
            statuses.add(flight.getStatus());
        }
        DefaultComboBoxModel<String> airlineModel = new DefaultComboBoxModel<>();
        airlineModel.addElement(ALL_AIRLINES);
        airlines.forEach(airlineModel::addElement);
        airline.setModel(airlineModel);
        airline.setSelectedItem(currentAirline == null ? ALL_AIRLINES : currentAirline);
        DefaultComboBoxModel<String> statusModel = new DefaultComboBoxModel<>();
        statusModel.addElement(ALL_STATUSES);
        statuses.forEach(statusModel::addElement);
        status.setModel(statusModel);
        status.setRenderer(new javax.swing.DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                String text = ALL_STATUSES.equals(value) ? ALL_STATUSES : Theme.statusLabel(String.valueOf(value));
                return super.getListCellRendererComponent(list, text, index, selected, focus);
            }
        });
        status.setSelectedItem(currentStatus == null ? ALL_STATUSES : currentStatus);
        updatingFilters = false;
        applyFilters();
    }

    /** Usa los metodos de busqueda del sistema: por aerolinea y por estado. */
    private void applyFilters() {
        AirCtrlSystem system = ctx.system();
        String selectedAirline = FormDialog.selected(airline);
        String selectedStatus = FormDialog.selected(status);
        List<Flight> result = ALL_AIRLINES.equals(selectedAirline) || selectedAirline.isEmpty()
                ? system.getFlights()
                : system.findFlightsByAirline(selectedAirline);
        if (!ALL_STATUSES.equals(selectedStatus) && !selectedStatus.isEmpty()) {
            List<Flight> byStatus = system.findFlightsByStatus(selectedStatus);
            result = new ArrayList<>(result);
            result.retainAll(byStatus);
        }
        table.setRows(result);
        count.setText(result.size() + " de " + system.getFlights().size() + " vuelos");
        showDetails();
    }

    /** Busqueda exacta en la tabla hash de vuelos. */
    private void findExact() {
        String id = search.getText().trim();
        if (id.isEmpty()) {
            return;
        }
        try {
            Flight flight = ctx.system().findFlight(id);
            if (!table.rows().contains(flight)) {
                updatingFilters = true;
                airline.setSelectedItem(ALL_AIRLINES);
                status.setSelectedItem(ALL_STATUSES);
                updatingFilters = false;
                applyFilters();
            }
            table.select(flight);
        } catch (AirCtrlException exception) {
            ctx.failure(exception.getMessage());
            javax.swing.JOptionPane.showMessageDialog(this, exception.getMessage(), "Vuelo no encontrado",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showDetails() {
        Flight flight = table.selected();
        if (flight == null) {
            details.clear();
            return;
        }
        AirCtrlSystem system = ctx.system();
        Aircraft aircraft = system.getStore().getAircraft().get(flight.getAircraftId());
        details.begin(flight.getFlightId(), flight.getAirline() + ", " + flight.getOrigin() + " → " + flight.getDestination())
                .status("Estado", flight.getStatus())
                .row("Prioridad", flight.getPriority() + " (" + Theme.priorityName(flight.getPriority()) + ")",
                        Theme.mix(Theme.priority(flight.getPriority()), Theme.INK, 0.3))
                .row("Puerta", flight.getGate());
        if (aircraft == null) {
            details.row("Aeronave", flight.getAircraftId(), Theme.DANGER)
                    .note("La aeronave " + flight.getAircraftId() + " no existe o tiene datos invalidos. "
                            + "Revisa la pantalla de advertencias.", Theme.DANGER);
        } else {
            details.row("Aeronave", aircraft.aircraftId() + ", " + aircraft.model())
                    .row("Capacidad", aircraft.capacity() + " asientos");
        }
        List<Baggage> bags = system.baggageForFlight(flight.getFlightId());
        Map<String, Integer> byStatus = new TreeMap<>();
        for (Baggage bag : bags) {
            byStatus.merge(Theme.statusLabel(bag.status()), 1, Integer::sum);
        }
        StringBuilder summary = new StringBuilder();
        byStatus.forEach((k, v) -> summary.append(summary.length() == 0 ? "" : "<br>").append(v).append(" ").append(k.toLowerCase()));
        details.row("Equipaje", bags.isEmpty() ? "Sin equipaje" : bags.size() + " piezas");
        if (!bags.isEmpty()) {
            details.row("Detalle", summary.toString());
        }

        JButton assign = Ui.primary(flight.getGate() == null || flight.getGate().isBlank() ? "Asignar puerta" : "Cambiar puerta");
        assign.addActionListener(event -> openAssignGateDialog(this, ctx, flight));
        JButton bagsButton = Ui.secondary("Ver su equipaje");
        bagsButton.addActionListener(event -> {
            ctx.navigate("baggage", flight.getFlightId());
        });
        bagsButton.setEnabled(!bags.isEmpty());
        details.action(assign).action(bagsButton).done();
    }

    // ---------------------------------------------------------------- dialogos

    private void openRegisterDialog() {
        AirCtrlSystem system = ctx.system();
        JTextField id = FormDialog.codeField(null);
        JComboBox<String> airlineBox = new JComboBox<>();
        airlineBox.setEditable(true);
        new TreeSet<>(system.getFlights().stream().map(Flight::getAirline).toList()).forEach(airlineBox::addItem);
        JTextField origin = FormDialog.codeField("MEX");
        JTextField destination = FormDialog.codeField(null);
        JComboBox<String> aircraft = new JComboBox<>();
        for (Aircraft a : system.getAircraft()) {
            aircraft.addItem(a.aircraftId() + "  " + a.model() + " (" + a.capacity() + ")"
                    + ("ACTIVE".equals(a.status()) ? "" : ", " + Theme.statusLabel(a.status()).toLowerCase()));
        }
        JComboBox<String> gate = new JComboBox<>();
        gate.addItem("Sin puerta por ahora");
        for (Gate g : system.availableGates()) {
            gate.addItem(g.getGateId() + "  Terminal " + g.getTerminal());
        }
        JComboBox<String> statusBox = new JComboBox<>(AirCtrlSystem.FLIGHT_STATUSES.toArray(String[]::new));
        statusBox.setRenderer(statusRenderer());
        JLabel priorityText = Ui.label("", Theme.SMALL_BOLD, Theme.INK);
        JSpinner priority = FormDialog.prioritySpinner(1, priorityText);

        FormDialog dialog = new FormDialog(this, "Nuevo vuelo",
                "Solo puedes elegir puertas disponibles. Si el vuelo esta activo entra a la cola de operaciones.");
        dialog.field("Numero de vuelo", id, "Por ejemplo AM730")
                .field("Aerolinea", airlineBox)
                .field("Origen", origin, "Codigo IATA de 3 letras")
                .field("Destino", destination, "Codigo IATA de 3 letras")
                .field("Aeronave", aircraft)
                .field("Puerta", gate)
                .field("Estado", statusBox)
                .field("Prioridad", FormDialog.withTrailing(priority, priorityText))
                .onSubmit("Registrar vuelo", () -> {
                    String flightId = FormDialog.required(id, "El numero de vuelo");
                    String airlineName = FormDialog.selected(airlineBox);
                    if (airlineName.isEmpty()) {
                        throw new IllegalArgumentException("La aerolinea es obligatoria");
                    }
                    String from = iata(origin, "El origen");
                    String to = iata(destination, "El destino");
                    if (from.equals(to)) {
                        throw new IllegalArgumentException("El origen y el destino deben ser distintos");
                    }
                    String aircraftId = firstToken(aircraft);
                    String gateId = gate.getSelectedIndex() <= 0 ? "" : firstToken(gate);
                    Flight flight = new Flight(flightId, airlineName, from, to, aircraftId, gateId,
                            FormDialog.selected(statusBox), (Integer) priority.getValue());
                    system.registerFlight(flight);
                    ctx.refreshAll();
                    ctx.success("Vuelo " + flightId + " registrado");
                    table.select(flight);
                });
        dialog.open();
    }

    static void openAssignGateDialog(Component parent, AppContext ctx, Flight flight) {
        AirCtrlSystem system = ctx.system();
        List<Gate> free = system.availableGates();
        if (free.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(parent,
                    "No hay puertas disponibles. Libera o habilita una puerta en la pantalla Puertas.",
                    "Sin puertas libres", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JComboBox<String> gate = new JComboBox<>();
        for (Gate g : free) {
            gate.addItem(g.getGateId() + "  Terminal " + g.getTerminal());
        }
        String current = flight.getGate() == null || flight.getGate().isBlank() ? "ninguna" : flight.getGate();
        FormDialog dialog = new FormDialog(parent, "Asignar puerta a " + flight.getFlightId(),
                "Puerta actual: " + current + ". Si la cambias, la anterior queda disponible.");
        dialog.field("Nueva puerta", gate, free.size() + " puertas disponibles")
                .onSubmit("Asignar puerta", () -> {
                    String gateId = firstToken(gate);
                    system.assignGate(flight.getFlightId(), gateId);
                    ctx.refreshAll();
                    ctx.success("Puerta " + gateId + " asignada a " + flight.getFlightId());
                });
        dialog.open();
    }

    static javax.swing.ListCellRenderer<Object> statusRenderer() {
        return new javax.swing.DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                return super.getListCellRendererComponent(list,
                        Theme.statusLabel(String.valueOf(value)) + "  (" + value + ")", index, selected, focus);
            }
        };
    }

    static String firstToken(JComboBox<String> combo) {
        String value = FormDialog.selected(combo);
        int space = value.indexOf(' ');
        return space < 0 ? value : value.substring(0, space);
    }

    private static String iata(JTextField field, String name) {
        String value = FormDialog.required(field, name);
        if (!value.matches("[A-Z]{3}")) {
            field.requestFocusInWindow();
            throw new IllegalArgumentException(name + " debe tener 3 letras, por ejemplo MEX");
        }
        return value;
    }

    static JComponent spacer(int width) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(width, 1));
        return panel;
    }
}
