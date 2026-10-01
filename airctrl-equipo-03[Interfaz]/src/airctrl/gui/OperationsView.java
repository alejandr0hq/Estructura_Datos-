package airctrl.gui;

import airctrl.model.Operation;
import airctrl.service.AirCtrlSystem;
import airctrl.structure.PriorityOperationQueue;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.List;

/** Cola de prioridad completa y pila de historial, lado a lado. */
public final class OperationsView extends JPanel {
    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final JPanel queueList = new JPanel();
    private final JPanel historyList = new JPanel();
    private final JPanel levels = new JPanel(new GridLayout(1, 5, 8, 0));
    private final JLabel queueTitle = Ui.h2("");
    private final JLabel historyTitle = Ui.h2("");
    private final JButton processButton = Ui.primary("Atender siguiente");
    private final JSpinner batch = new JSpinner(new SpinnerNumberModel(3, 1, 50, 1));
    private final JButton batchButton = Ui.secondary("Atender varias");

    public OperationsView(AppContext ctx) {
        super(new BorderLayout());
        this.ctx = ctx;
        setBackground(Theme.BG);

        JButton incident = Ui.secondary("Registrar incidente");
        incident.addActionListener(event -> IncidentsView.openRegisterDialog(this, ctx));
        processButton.addActionListener(event -> process(1));
        batchButton.addActionListener(event -> process((Integer) batch.getValue()));
        batch.setPreferredSize(new Dimension(64, 34));
        batch.setToolTipText("Cuantas operaciones atender seguidas");

        JPanel page = Ui.page();
        page.add(Ui.pageHeader("Operaciones",
                "Se atiende primero la prioridad mas alta; dentro de un mismo nivel, la que llego antes.",
                incident, batch, batchButton, processButton), BorderLayout.NORTH);

        levels.setOpaque(false);
        queueList.setOpaque(false);
        queueList.setLayout(new BoxLayout(queueList, BoxLayout.Y_AXIS));
        historyList.setOpaque(false);
        historyList.setLayout(new BoxLayout(historyList, BoxLayout.Y_AXIS));

        JPanel queuePanel = new Ui.Surface(new BorderLayout(0, 12));
        JPanel queueHead = new JPanel(new BorderLayout(0, 10));
        queueHead.setOpaque(false);
        queueHead.add(queueTitle, BorderLayout.NORTH);
        queueHead.add(levels, BorderLayout.CENTER);
        queuePanel.add(queueHead, BorderLayout.NORTH);
        queuePanel.add(Ui.scroll(wrapTop(queueList)), BorderLayout.CENTER);

        JPanel historyPanel = new Ui.Surface(new BorderLayout(0, 12));
        JPanel historyHead = new JPanel(new BorderLayout(0, 4));
        historyHead.setOpaque(false);
        historyHead.add(historyTitle, BorderLayout.NORTH);
        historyHead.add(Ui.muted("La ultima atendida aparece arriba (pila)."), BorderLayout.CENTER);
        historyPanel.add(historyHead, BorderLayout.NORTH);
        historyPanel.add(Ui.scroll(wrapTop(historyList)), BorderLayout.CENTER);
        historyPanel.setPreferredSize(new Dimension(420, 400));

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);
        body.add(queuePanel, BorderLayout.CENTER);
        body.add(historyPanel, BorderLayout.EAST);
        page.add(body, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);

        ctx.onChange(this::refresh);
        refresh();
    }

    private static JComponent wrapTop(JComponent list) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(list, BorderLayout.NORTH);
        return wrapper;
    }

    private void process(int count) {
        ctx.attempt(this, null, () -> {
            AirCtrlSystem system = ctx.system();
            int done = 0;
            String last = "";
            while (done < count && !system.getPending().isEmpty()) {
                last = system.processNext().getReferenceId();
                done++;
            }
            if (done == 0) {
                system.processNext(); // lanza el mensaje "No hay operaciones pendientes"
            }
            ctx.success(done == 1 ? "Operacion " + last + " atendida" : done + " operaciones atendidas");
        });
    }

    public void refresh() {
        AirCtrlSystem system = ctx.system();
        PriorityOperationQueue<Operation> pending = system.getPending();
        List<Operation> queue = system.pendingOperations();

        queueTitle.setText(queue.isEmpty() ? "Cola vacia" : "En cola (" + queue.size() + ")");
        levels.removeAll();
        for (int p = PriorityOperationQueue.MAX_PRIORITY; p >= PriorityOperationQueue.MIN_PRIORITY; p--) {
            levels.add(new Level(p, pending.countAt(p)));
        }
        levels.revalidate();

        queueList.removeAll();
        if (queue.isEmpty()) {
            JLabel empty = Ui.label("<html>No hay nada por atender. Cuando registres un incidente o un vuelo activo "
                    + "aparecera aqui en el lugar que le corresponde.</html>", Theme.BODY, Theme.MUTED);
            empty.setBorder(new EmptyBorder(12, 4, 12, 4));
            queueList.add(empty);
        }
        boolean first = true;
        for (Operation operation : queue) {
            queueList.add(new OperationStrip(operation, first, false));
            first = false;
        }
        queueList.revalidate();
        queueList.repaint();

        List<Operation> history = system.recentHistory(Integer.MAX_VALUE);
        historyTitle.setText("Historial (" + history.size() + ")");
        historyList.removeAll();
        if (history.isEmpty()) {
            historyList.add(Ui.label("Aun no se atiende ninguna operacion.", Theme.BODY, Theme.MUTED));
        }
        for (Operation operation : history) {
            historyList.add(new OperationStrip(operation, false, true));
            historyList.add(Box.createVerticalStrut(0));
        }
        historyList.revalidate();
        historyList.repaint();

        processButton.setEnabled(!queue.isEmpty());
        batchButton.setEnabled(queue.size() > 1);
    }

    /** Conteo de un nivel de prioridad. */
    private static final class Level extends JComponent {
        private static final long serialVersionUID = 1L;
        private final int priority;
        private final int count;

        Level(int priority, int count) {
            this.priority = priority;
            this.count = count;
            setToolTipText(count + " en prioridad " + priority + " (" + Theme.priorityName(priority) + ")");
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(100, 46);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            var color = Theme.priority(priority);
            g2.setColor(count > 0 ? Theme.tint(color, 30) : Theme.SURFACE_ALT);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, 5, getHeight() - 1, 4, 4);
            g2.setFont(Theme.H2);
            g2.setColor(Theme.INK);
            g2.drawString(String.valueOf(count), 14, 21);
            g2.setFont(Theme.SMALL);
            g2.setColor(Theme.MUTED);
            g2.drawString(priority + " " + Theme.priorityName(priority), 14, 38);
            g2.dispose();
        }
    }
}
