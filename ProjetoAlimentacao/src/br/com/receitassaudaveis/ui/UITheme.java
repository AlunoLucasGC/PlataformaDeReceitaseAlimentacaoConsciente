package br.com.receitassaudaveis.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

/**
 * Centraliza cores e estilos visuais da aplicação Receitas Saudáveis.
 * Mantém a interface em Swing puro, sem bibliotecas visuais externas.
 */
public final class UITheme {

    public static final Color GREEN_900 = new Color(25, 78, 57);
    public static final Color GREEN_800 = new Color(35, 105, 76);
    public static final Color GREEN_700 = new Color(47, 127, 91);
    public static final Color GREEN_100 = new Color(229, 241, 233);
    public static final Color GREEN_050 = new Color(243, 248, 244);
    public static final Color BACKGROUND = new Color(246, 248, 246);
    public static final Color SURFACE = Color.WHITE;
    public static final Color TEXT = new Color(38, 49, 43);
    public static final Color MUTED = new Color(104, 116, 108);
    public static final Color BORDER = new Color(223, 230, 225);
    public static final Color DANGER = new Color(171, 61, 57);

    private UITheme() {
    }

    public static void install() {
        Font base = new Font("Segoe UI", Font.PLAIN, 13);
        UIManager.put("defaultFont", base);
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("TabbedPane.background", BACKGROUND);
        UIManager.put("TabbedPane.foreground", TEXT);
        UIManager.put("TabbedPane.selected", SURFACE);
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", GREEN_100);
        UIManager.put("Table.selectionForeground", GREEN_900);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("TableHeader.background", GREEN_900);
        UIManager.put("TableHeader.foreground", Color.WHITE);
    }

    public static void stylePrimaryButton(JButton button) {
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12.5f));
        button.setForeground(Color.WHITE);
        button.setBackground(GREEN_800);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GREEN_800),
                new EmptyBorder(8, 15, 8, 15)));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    public static void styleSecondaryButton(JButton button) {
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        button.setForeground(GREEN_900);
        button.setBackground(SURFACE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 14, 8, 14)));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    public static void styleTextField(JTextField field) {
        field.setFont(field.getFont().deriveFont(13f));
        field.setForeground(TEXT);
        field.setBackground(SURFACE);
        field.setCaretColor(GREEN_800);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 9, 7, 9)));
    }

    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFont(combo.getFont().deriveFont(12.5f));
        combo.setForeground(TEXT);
        combo.setBackground(SURFACE);
        combo.setBorder(BorderFactory.createLineBorder(BORDER));
    }

    public static void styleTextArea(JTextArea area) {
        area.setFont(area.getFont().deriveFont(13.5f));
        area.setForeground(TEXT);
        area.setBackground(SURFACE);
        area.setCaretColor(GREEN_800);
        area.setBorder(new EmptyBorder(10, 11, 10, 11));
    }

    public static void styleTable(JTable table) {
        table.setFont(table.getFont().deriveFont(13f));
        table.setForeground(TEXT);
        table.setBackground(SURFACE);
        table.setSelectionBackground(GREEN_100);
        table.setSelectionForeground(GREEN_900);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setRowHeight(42);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new java.awt.Dimension(header.getPreferredSize().width, 42));
        header.setFont(header.getFont().deriveFont(Font.BOLD, 12f));
        header.setBackground(GREEN_900);
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder());

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value,
                    boolean selected, boolean focused, int row, int column) {
                Component component = super.getTableCellRendererComponent(
                        source, value, selected, focused, row, column);
                if (selected) {
                    component.setBackground(GREEN_100);
                    component.setForeground(GREEN_900);
                } else {
                    component.setBackground(row % 2 == 0 ? SURFACE : GREEN_050);
                    component.setForeground(TEXT);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (column == 4) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setFont(getFont().deriveFont(Font.BOLD, 16f));
                    setForeground(selected ? GREEN_900 : GREEN_700);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setFont(getFont().deriveFont(Font.PLAIN, 13f));
                }
                return component;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
    }

    public static void styleCard(JComponent component) {
        component.setBackground(SURFACE);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 14, 12, 14)));
        component.setOpaque(true);
    }
}
