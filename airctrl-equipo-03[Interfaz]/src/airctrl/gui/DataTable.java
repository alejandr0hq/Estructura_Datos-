package airctrl.gui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Tabla tipada: cada columna se define con una funcion sobre el registro. */
public final class DataTable<T> {
    public enum Style { TEXT, CODE, STATUS, PRIORITY, NUMBER }

    public record Column<T>(String title, Function<T, Object> value, Style style, int width) {
    }

    private final List<Column<T>> columns;
    private final Model model = new Model();
    private final JTable table;
    private final TableRowSorter<Model> sorter;
    private final JLabel emptyLabel = Ui.muted("");

    @SafeVarargs
    public DataTable(Column<T>... columns) {
        this.columns = List.of(columns);
        table = new JTable(model) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getRowCount() == 0 && !emptyLabel.getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setFont(Theme.BODY);
                    g2.setColor(Theme.MUTED);
                    int w = g2.getFontMetrics().stringWidth(emptyLabel.getText());
                    g2.drawString(emptyLabel.getText(), Math.max(12, (getVisibleRect().width - w) / 2), 40);
                    g2.dispose();
                }
            }
        };
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(Theme.SURFACE);
        table.setSelectionBackground(Theme.SELECTION);
        table.setSelectionForeground(Theme.INK);
        table.setFont(Theme.BODY);
        table.setAutoCreateRowSorter(false);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 34));
        header.setDefaultRenderer(new HeaderRenderer());

        for (int i = 0; i < this.columns.size(); i++) {
            Column<T> column = this.columns.get(i);
            TableColumn tableColumn = table.getColumnModel().getColumn(i);
            if (column.width() > 0) {
                tableColumn.setPreferredWidth(column.width());
            }
            tableColumn.setCellRenderer(rendererFor(column.style()));
            if (column.style() == Style.PRIORITY || column.style() == Style.NUMBER) {
                sorter.setComparator(i, (a, b) -> Integer.compare(
                        ((Number) a).intValue(), ((Number) b).intValue()));
            }
        }
    }

    public JTable table() {
        return table;
    }

    public JScrollPane scroll() {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new MatteBorder(1, 1, 1, 1, Theme.LINE));
        scroll.getViewport().setBackground(Theme.SURFACE);
        return scroll;
    }

    public void setRows(List<T> rows) {
        T selected = selected();
        model.rows = new ArrayList<>(rows);
        model.fireTableDataChanged();
        if (selected != null) {
            select(selected);
        }
    }

    public void setEmptyMessage(String message) {
        emptyLabel.setText(message);
    }

    public List<T> rows() {
        return model.rows;
    }

    public T selected() {
        int view = table.getSelectedRow();
        if (view < 0) {
            return null;
        }
        return model.rows.get(table.convertRowIndexToModel(view));
    }

    public void select(T row) {
        int index = model.rows.indexOf(row);
        if (index >= 0) {
            int view = table.convertRowIndexToView(index);
            if (view >= 0) {
                table.setRowSelectionInterval(view, view);
                table.scrollRectToVisible(table.getCellRect(view, 0, true));
            }
        }
    }

    public void onSelect(Runnable action) {
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                action.run();
            }
        });
    }

    private TableCellRenderer rendererFor(Style style) {
        return switch (style) {
            case STATUS -> new PaintedRenderer((g2, value, h) ->
                    Ui.paintBadge(g2, Theme.statusLabel(String.valueOf(value)),
                            Theme.status(String.valueOf(value)), 10, h / 2, Theme.SMALL_BOLD));
            case PRIORITY -> new PaintedRenderer((g2, value, h) ->
                    Ui.paintPriority(g2, ((Number) value).intValue(), 12, h / 2, true));
            default -> new TextRenderer(style);
        };
    }

    private final class Model extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private List<T> rows = new ArrayList<>();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public String getColumnName(int column) {
            return columns.get(column).title();
        }

        @Override
        public Class<?> getColumnClass(int column) {
            Style style = columns.get(column).style();
            return style == Style.PRIORITY || style == Style.NUMBER ? Integer.class : String.class;
        }

        @Override
        public Object getValueAt(int row, int column) {
            Object value = columns.get(column).value().apply(rows.get(row));
            return value == null ? "" : value;
        }
    }

    private static Color rowBackground(JTable table, boolean selected, int row) {
        if (selected) {
            return Theme.SELECTION;
        }
        return row % 2 == 0 ? Theme.SURFACE : Theme.SURFACE_ALT;
    }

    private static final class TextRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        private final Style style;

        TextRenderer(Style style) {
            this.style = style;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, false, row, column);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setBackground(rowBackground(table, selected, row));
            setForeground(Theme.INK);
            setFont(style == Style.CODE ? Theme.CODE_SMALL.deriveFont(java.awt.Font.BOLD) : Theme.BODY);
            setHorizontalAlignment(style == Style.NUMBER ? SwingConstants.RIGHT : SwingConstants.LEFT);
            if (value == null || String.valueOf(value).isBlank()) {
                setText("—");
                setForeground(new Color(0xA5AFBD));
            }
            setToolTipText(value == null ? null : String.valueOf(value));
            return this;
        }
    }

    @FunctionalInterface
    private interface Painter {
        void paint(Graphics2D g2, Object value, int height);
    }

    private static final class PaintedRenderer extends JComponent implements TableCellRenderer {
        private static final long serialVersionUID = 1L;
        private final Painter painter;
        private Object value;
        private Color background;

        PaintedRenderer(Painter painter) {
            this.painter = painter;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focus, int row, int column) {
            this.value = value;
            this.background = rowBackground(table, selected, row);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(background);
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (value != null && !String.valueOf(value).isEmpty()) {
                painter.paint(g2, value, getHeight());
            }
            g2.dispose();
        }
    }

    private static final class HeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setFont(Theme.SMALL_BOLD);
            setForeground(Theme.MUTED);
            setBackground(new Color(0xF1F4F8));
            setOpaque(true);
            setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, Theme.LINE), new EmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }
}
