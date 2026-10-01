package airctrl.gui;

import airctrl.model.Baggage;
import airctrl.model.Flight;
import airctrl.service.AirCtrlException;
import airctrl.service.AirCtrlSystem;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Seguimiento de equipaje con su flujo de estados. */
public final class BaggageView extends JPanel implements Navigable {
    private static final long serialVersionUID = 1L;
    private static final String ALL = "ALL";
    private static final String PENDING = "PENDING";

    private final AppContext ctx;
    private final JTextField bagSearch = Ui.field(10);
    private final JTextField filterText = Ui.field(12);
    private final JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final JLabel count = Ui.muted("");
    private final Details details = new Details("Selecciona un equipaje para avanzar su estado.");
    private final DataTable<Baggage> table = new DataTable<>(
            new DataTable.Column<>("Equipaje", Baggage::bagId, DataTable.Style.CODE, 100),
            new DataTable.Column<>("Vuelo", Baggage::flightId, DataTable.Style.CODE, 90),
            new DataTable.Column<>("Pasajero", Baggage::passengerCode, DataTable.Style.CODE, 100),
            new DataTable.Column<>("Estado", Baggage::status, DataTable.Style.STATUS, 140));
    private String statusFilter = ALL;

    public BaggageView(AppContext ctx) {
        super(new BorderLayout());
        this.ctx = ctx;
        setBackground(Theme.BG);

        JButton add = Ui.primary("Registrar equipaje");
        add.addActionListener(event -> openRegisterDialog());
        JPanel page = Ui.page();
        page.add(Ui.pageHeader("Equipaje",
                "Documentado, cargado, en traslado y entregado. Lo entregado ya no cuenta como pendiente.", add),
                BorderLayout.NORTH);

        JButton find = Ui.secondary("Buscar");
        find.addActionListener(event -> findExact());
        FormDialog.bindEnter(bagSearch, this::findExact);
        filterText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        chips.setOpaque(false);

        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);
        row1.add(Ui.leftRow(8, Ui.label("Maleta", Theme.SMALL_BOLD, Theme.INK), bagSearch, find,
                FlightsView.spacer(12), Ui.label("Vuelo o pasajero", Theme.SMALL_BOLD, Theme.INK), filterText),
                BorderLayout.WEST);
        row1.add(count, BorderLayout.EAST);
        JPanel toolbar = new JPanel(new BorderLayout(0, 10));
        toolbar.setOpaque(false);
        toolbar.add(row1, BorderLayout.NORTH);
        toolbar.add(chips, BorderLayout.CENTER);

        table.setEmptyMessage("Ningun equipaje coincide con los filtros.");
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
            statusFilter = ALL;
            filterText.setText(flightId);
            refresh();
        }
    }

    public void refresh() {
        List<Baggage> all = ctx.system().getBaggage();
        Map<String, Integer> byStatus = new TreeMap<>();
        int pending = 0;
        for (Baggage bag : all) {
            byStatus.merge(bag.status(), 1, Integer::sum);
            if (bag.isPending()) {
                pending++;
            }
        }
        chips.removeAll();
        ButtonGroup group = new ButtonGroup();
        addChip(group, ALL, "Todo " + all.size());
        addChip(group, PENDING, "Pendiente " + pending);
        for (String status : AirCtrlSystem.BAGGAGE_STATUSES) {
            addChip(group, status, Theme.statusLabel(status) + " " + byStatus.getOrDefault(status, 0));
        }
        chips.revalidate();
        chips.repaint();
        applyFilters();
    }

    private void addChip(ButtonGroup group, String key, String text) {
        JToggleButton chip = new GatesView.Chip(text);
        chip.setSelected(key.equals(statusFilter));
        chip.addActionListener(event -> {
            statusFilter = key;
            applyFilters();
        });
        group.add(chip);
        chips.add(chip);
    }

    private void applyFilters() {
        String text = filterText.getText().trim().toUpperCase();
        List<Baggage> all = ctx.system().getBaggage();
        List<Baggage> rows = new ArrayList<>();
        for (Baggage bag : all) {
            boolean statusOk = ALL.equals(statusFilter)
                    || (PENDING.equals(statusFilter) && bag.isPending())
                    || bag.status().equals(statusFilter);
            boolean textOk = text.isEmpty() || bag.flightId().toUpperCase().contains(text)
                    || bag.passengerCode().toUpperCase().contains(text);
            if (statusOk && textOk) {
                rows.add(bag);
            }
        }
        table.setRows(rows);
        count.setText(rows.size() + " de " + all.size() + " piezas");
        showDetails();
    }

    private void findExact() {
        String id = bagSearch.getText().trim();
        if (id.isEmpty()) {
            return;
        }
        try {
            Baggage bag = ctx.system().findBaggage(id);
            if (!table.rows().contains(bag)) {
                statusFilter = ALL;
                filterText.setText("");
                refresh();
            }
            table.select(bag);
        } catch (AirCtrlException exception) {
            ctx.failure(exception.getMessage());
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Equipaje no encontrado",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showDetails() {
        Baggage bag = table.selected();
        if (bag == null) {
            details.clear();
            return;
        }
        AirCtrlSystem system = ctx.system();
        details.begin(bag.bagId(), "Pasajero " + bag.passengerCode())
                .status("Estado", bag.status());
        Flight flight;
        try {
            flight = system.findFlight(bag.flightId());
            details.row("Vuelo", flight.getFlightId() + ", " + flight.getOrigin() + " → " + flight.getDestination())
                    .row("Puerta del vuelo", flight.getGate());
        } catch (AirCtrlException exception) {
            details.row("Vuelo", bag.flightId(), Theme.DANGER)
                    .note("Este equipaje apunta a un vuelo que no existe.", Theme.DANGER);
        }
        List<String> next = AirCtrlSystem.allowedBaggageTransitions(bag.status());
        if (next.isEmpty()) {
            details.note("Entregado: su ciclo termino y ya no aparece como pendiente.", Theme.OK);
        } else {
            details.note("Siguiente paso", Theme.MUTED);
            for (String status : next) {
                JButton button = Ui.button(actionLabel(status),
                        "MISSING".equals(status) ? Ui.Kind.DANGER : Ui.Kind.PRIMARY);
                if (!"MISSING".equals(status) && next.indexOf(status) > 0) {
                    button = Ui.secondary(actionLabel(status));
                }
                button.addActionListener(event -> ctx.attempt(this,
                        bag.bagId() + " ahora esta " + Theme.statusLabel(status).toLowerCase(),
                        () -> system.updateBaggageStatus(bag.bagId(), status)));
                details.action(button);
            }
        }
        details.done();
    }

    private static String actionLabel(String status) {
        return switch (status) {
            case "LOADED" -> "Marcar como cargado";
            case "IN_TRANSIT" -> "Marcar en traslado";
            case "DELIVERED" -> "Marcar como entregado";
            case "MISSING" -> "Reportar extraviado";
            default -> Theme.statusLabel(status);
        };
    }

    private void openRegisterDialog() {
        AirCtrlSystem system = ctx.system();
        JTextField id = FormDialog.codeField(FormDialog.nextId("BAG", 4, system.getStore().getBaggage().keySet()));
        JComboBox<String> flight = new JComboBox<>();
        for (Flight f : system.getFlights()) {
            if (f.isPending()) {
                flight.addItem(f.getFlightId() + "  " + f.getOrigin() + "-" + f.getDestination()
                        + ", " + Theme.statusLabel(f.getStatus()).toLowerCase());
            }
        }
        JTextField passenger = FormDialog.codeField("PAX");
        JComboBox<String> status = new JComboBox<>(new String[]{"CHECKED", "LOADED", "IN_TRANSIT"});
        status.setRenderer(FlightsView.statusRenderer());
        new FormDialog(this, "Registrar equipaje", "Solo se muestran vuelos activos. Normalmente empieza como documentado.")
                .field("Identificador", id)
                .field("Vuelo", flight)
                .field("Pasajero", passenger, "Codigo del pasajero, por ejemplo PAX1234")
                .field("Estado", status)
                .onSubmit("Registrar equipaje", () -> {
                    String bagId = FormDialog.required(id, "El identificador");
                    String paxCode = FormDialog.required(passenger, "El codigo de pasajero");
                    if ("PAX".equals(paxCode)) {
                        throw new IllegalArgumentException("Completa el codigo de pasajero");
                    }
                    if (flight.getItemCount() == 0) {
                        throw new IllegalArgumentException("No hay vuelos activos a los cuales asociarlo");
                    }
                    Baggage bag = new Baggage(bagId, FlightsView.firstToken(flight), paxCode,
                            FormDialog.selected(status));
                    system.registerBaggage(bag);
                    ctx.refreshAll();
                    ctx.success("Equipaje " + bagId + " registrado");
                    table.select(bag);
                })
                .open();
    }
}
