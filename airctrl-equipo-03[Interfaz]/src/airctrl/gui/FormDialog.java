package airctrl.gui;

import airctrl.service.AirCtrlException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Dialogo de formulario: etiquetas a la izquierda, errores en linea y accion principal. */
public final class FormDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final JPanel fields = new JPanel(new GridBagLayout());
    private final JLabel error = Ui.label(" ", Theme.SMALL_BOLD, Theme.DANGER);
    private final JButton submit = Ui.primary("Guardar");
    private int rows;
    private Runnable action = () -> { };
    private boolean accepted;

    public FormDialog(Component parent, String title, String description) {
        super(parent == null ? null : SwingUtilities.getWindowAncestor(parent), title,
                ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.SURFACE);
        root.setBorder(new EmptyBorder(20, 22, 18, 22));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JLabel titleLabel = Ui.label(title, Theme.H2, Theme.INK);
        titleLabel.setAlignmentX(0);
        head.add(titleLabel);
        if (description != null) {
            JLabel text = Ui.label("<html><div style='width:360px'>" + description + "</div></html>",
                    Theme.SMALL, Theme.MUTED);
            text.setAlignmentX(0);
            head.add(Box.createVerticalStrut(4));
            head.add(text);
        }
        root.add(head, BorderLayout.NORTH);

        fields.setOpaque(false);
        root.add(fields, BorderLayout.CENTER);

        JButton cancel = Ui.secondary("Cancelar");
        cancel.addActionListener(event -> dispose());
        submit.addActionListener(event -> trySubmit());
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        footer.add(error, BorderLayout.CENTER);
        footer.add(Ui.row(cancel, submit), BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(submit);
        getRootPane().registerKeyboardAction(event -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public FormDialog field(String label, JComponent component) {
        return field(label, component, null);
    }

    public FormDialog field(String label, JComponent component, String hint) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = rows++;
        c.gridx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(9, 0, 6, 16);
        fields.add(Ui.label(label, Theme.BODY_BOLD, Theme.INK), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 6, 0);
        if (hint == null) {
            fields.add(component, c);
        } else {
            JPanel box = new JPanel(new BorderLayout(0, 3));
            box.setOpaque(false);
            box.add(component, BorderLayout.CENTER);
            box.add(Ui.muted(hint), BorderLayout.SOUTH);
            fields.add(box, c);
        }
        component.setPreferredSize(new Dimension(300, component.getPreferredSize().height));
        return this;
    }

    public FormDialog content(JComponent component) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = rows++;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 6, 0);
        fields.add(component, c);
        return this;
    }

    public FormDialog onSubmit(String label, Runnable action) {
        submit.setText(label);
        this.action = action;
        return this;
    }

    /** Muestra el dialogo y devuelve true si la accion se completo. */
    public boolean open() {
        pack();
        setMinimumSize(new Dimension(Math.max(getWidth(), 480), getHeight()));
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return accepted;
    }

    private void trySubmit() {
        try {
            action.run();
            accepted = true;
            dispose();
        } catch (AirCtrlException | IllegalArgumentException exception) {
            error.setText("<html><div style='width:260px'>" + exception.getMessage() + "</div></html>");
            pack();
        }
    }

    // ---------------------------------------------------------------- utilidades de entrada

    /** Campo que convierte a mayusculas mientras se escribe (identificadores). */
    public static JTextField codeField(String initial) {
        JTextField field = Ui.field(14);
        field.setFont(Theme.CODE_SMALL.deriveFont(13f));
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                super.insertString(fb, offset, text == null ? null : text.toUpperCase(), attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                super.replace(fb, offset, length, text == null ? null : text.toUpperCase(), attrs);
            }
        });
        if (initial != null) {
            field.setText(initial);
        }
        return field;
    }

    public static JSpinner prioritySpinner(int initial, JLabel description) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initial, 1, 5, 1));
        Runnable update = () -> {
            int value = (Integer) spinner.getValue();
            description.setText(value + " = " + Theme.priorityName(value));
            description.setForeground(Theme.mix(Theme.priority(value), Theme.INK, 0.25));
        };
        spinner.addChangeListener(event -> update.run());
        update.run();
        return spinner;
    }

    public static JPanel withTrailing(JComponent main, JComponent trailing) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.add(main, BorderLayout.CENTER);
        panel.add(trailing, BorderLayout.EAST);
        return panel;
    }

    public static String required(JTextField field, String name) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            field.requestFocusInWindow();
            throw new IllegalArgumentException(name + " es obligatorio");
        }
        return value;
    }

    public static String selected(JComboBox<?> combo) {
        Object value = combo.getSelectedItem();
        return value == null ? "" : String.valueOf(value).trim();
    }

    /** Siguiente identificador libre con el prefijo dado (por ejemplo BAG0141). */
    public static String nextId(String prefix, int digits, java.util.Collection<String> existing) {
        int max = 0;
        for (String id : existing) {
            if (id != null && id.startsWith(prefix)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(prefix.length())));
                } catch (NumberFormatException ignored) {
                    // identificador con otro formato
                }
            }
        }
        return prefix + String.format("%0" + digits + "d", max + 1);
    }

    static void bindEnter(JComponent component, Runnable action) {
        component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "run");
        component.getActionMap().put("run", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    static Window windowOf(Component component) {
        return component == null ? null : SwingUtilities.getWindowAncestor(component);
    }

    static javax.swing.border.Border line() {
        return BorderFactory.createLineBorder(Theme.LINE);
    }
}
