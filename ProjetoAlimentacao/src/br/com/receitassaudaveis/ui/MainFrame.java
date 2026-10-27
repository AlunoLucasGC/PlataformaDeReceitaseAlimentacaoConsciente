
package br.com.receitassaudaveis.ui;

import br.com.receitassaudaveis.model.User;
import br.com.receitassaudaveis.service.RecipeService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;

/**
 * Janela principal da aplicação.
 * Apresenta o catálogo, favoritos e acesso ao gerenciamento.
 */
public class MainFrame extends JFrame {

    private final RecipeService service = new RecipeService();
    private final User currentUser;
    private final Runnable onLogout;

    private RecipePanel catalogPanel;
    private RecipePanel favoritesPanel;

    // Cores principais da aplicação.
    private static final Color GREEN_DARK = new Color(35, 105, 76);
    private static final Color GREEN_MEDIUM = new Color(51, 132, 95);
    private static final Color GREEN_LIGHT = new Color(235, 245, 238);
    private static final Color GRID_COLOR = new Color(220, 230, 224);

    public MainFrame(User currentUser, Runnable onLogout)
            throws SQLException {

        super("Receitas Saudáveis");

        this.currentUser = currentUser;
        this.onLogout = onLogout;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1250, 790);
        setMinimumSize(new Dimension(980, 650));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);

        // Abas principais.
        JTabbedPane tabs = new JTabbedPane();

        catalogPanel = new RecipePanel(
                this,
                service,
                currentUser,
                false,
                this::refreshAllPanels
        );

        favoritesPanel = new RecipePanel(
                this,
                service,
                currentUser,
                true,
                this::refreshAllPanels
        );

        tabs.addTab("Catálogo de receitas", catalogPanel);
        tabs.addTab("Meus favoritos", favoritesPanel);

        add(tabs, BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        // Aplica o padrão visual em todas as tabelas da janela.
        applyTableStyles(this);
    }

    /**
     * Cria o cabeçalho da aplicação.
     */
    private JPanel createHeader() {

        JPanel header = new JPanel(
                new BorderLayout(12, 8)
        );

        header.setBackground(GREEN_DARK);

        header.setBorder(
                BorderFactory.createEmptyBorder(14, 20, 14, 18)
        );

        JLabel title = new JLabel("Receitas Saudáveis");

        title.setForeground(Color.WHITE);

        title.setFont(
                title.getFont().deriveFont(Font.BOLD, 28f)
        );

        JLabel subtitle = new JLabel(
                "Encontre receitas por ingrediente, refeição, "
                + "tempo e preferência alimentar"
        );

        subtitle.setForeground(GREEN_LIGHT);

        subtitle.setFont(
                subtitle.getFont().deriveFont(13f)
        );

        JPanel text = new JPanel(
                new BorderLayout(2, 4)
        );

        text.setOpaque(false);

        text.add(title, BorderLayout.NORTH);
        text.add(subtitle, BorderLayout.CENTER);

        header.add(text, BorderLayout.WEST);

        // Área dos botões.
        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 5)
        );

        actions.setOpaque(false);

        JLabel account = new JLabel(
                "Conectado: " + currentUser.getName()
        );

        account.setForeground(Color.WHITE);
        account.setFont(
                account.getFont().deriveFont(Font.BOLD, 12f)
        );

        actions.add(account);

        JButton manageButton = new JButton(
                "Gerenciar receitas"
        );

        manageButton.setEnabled(!currentUser.isGuest());

       manageButton.setEnabled(true);

        manageButton.setToolTipText(
        "Cadastrar, editar ou excluir receitas"
        );

        manageButton.addActionListener(event -> {

       if (currentUser.isGuest()) {

        JOptionPane.showMessageDialog(
                this,
                "Para gerenciar receitas, você precisa "
                + "entrar em uma conta cadastrada.",
                "Acesso restrito",
                JOptionPane.INFORMATION_MESSAGE
        );

        return;
    }

    openRecipeManagement();
});

        actions.add(manageButton);

        JButton switchUserButton = new JButton(
                "Trocar usuário"
        );

        styleButton(switchUserButton, false);

        switchUserButton.addActionListener(event -> {
            dispose();
            onLogout.run();
        });

        actions.add(switchUserButton);

        header.add(actions, BorderLayout.EAST);

        return header;
    }

    /**
     * Aplica o estilo dos botões.
     */
    private void styleButton(JButton button, boolean primary) {

        button.setFont(
                button.getFont().deriveFont(Font.BOLD, 12f)
        );

        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);

        if (primary) {

            button.setBackground(GREEN_MEDIUM);
            button.setForeground(Color.WHITE);

            button.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(
                                    new Color(100, 165, 128)
                            ),
                            BorderFactory.createEmptyBorder(
                                    7, 12, 7, 12
                            )
                    )
            );

        } else {

            button.setBackground(Color.WHITE);
            button.setForeground(GREEN_DARK);

            button.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(
                                    GRID_COLOR
                            ),
                            BorderFactory.createEmptyBorder(
                                    7, 12, 7, 12
                            )
                    )
            );
        }
    }

    /**
     * Abre a janela de gerenciamento de receitas.
     */
    private void openRecipeManagement() {

        try {

            RecipeManagementDialog dialog =
                    new RecipeManagementDialog(
                            this,
                            service,
                            this::refreshAllPanels
                    );

            // Permite utilizar a janela principal
            // enquanto o gerenciamento está aberto.
            dialog.setModalityType(
                    Dialog.ModalityType.MODELESS
            );

            // Aplica o mesmo visual às tabelas do gerenciamento.
            applyTableStyles(dialog);

            dialog.setLocationRelativeTo(this);

            dialog.setVisible(true);

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível abrir o gerenciamento:\n"
                    + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Aplica o padrão visual em todas as tabelas
     * encontradas dentro de um componente.
     */
    private void applyTableStyles(Container container) {

        for (Component component : container.getComponents()) {

            if (component instanceof JTable table) {

                styleTable(table);
            }

            if (component instanceof Container child) {

                applyTableStyles(child);
            }
        }
    }

    /**
     * Configura aparência, seleção e cabeçalho de uma tabela.
     */
    private void styleTable(JTable table) {

        table.setRowHeight(32);

        table.setFont(
                table.getFont().deriveFont(13f)
        );

        table.setShowGrid(true);
        table.setGridColor(GRID_COLOR);
        table.setFillsViewportHeight(true);

        table.setSelectionBackground(GREEN_MEDIUM);
        table.setSelectionForeground(Color.WHITE);

        table.setAutoCreateRowSorter(true);

        table.getTableHeader().setReorderingAllowed(false);

        // Personalização do cabeçalho.
        DefaultTableCellRenderer headerRenderer =
                new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {

                JLabel label = (JLabel)
                        super.getTableCellRendererComponent(
                                table,
                                value,
                                isSelected,
                                hasFocus,
                                row,
                                column
                        );

                label.setOpaque(true);
                label.setBackground(GREEN_DARK);
                label.setForeground(Color.WHITE);

                label.setFont(
                        table.getFont().deriveFont(
                                Font.BOLD, 13f
                        )
                );

                label.setHorizontalAlignment(
                        SwingConstants.LEFT
                );

                label.setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(
                                        0, 0, 1, 1,
                                        GRID_COLOR
                                ),
                                BorderFactory.createEmptyBorder(
                                        5, 10, 5, 10
                                )
                        )
                );

                return label;
            }
        };

        table.getTableHeader().setDefaultRenderer(
                headerRenderer
        );

        table.getTableHeader().setPreferredSize(
                new Dimension(
                        table.getTableHeader()
                                .getPreferredSize().width,
                        38
                )
        );
    }

    /**
     * Rodapé com aviso informativo.
     */
    private JPanel createFooter() {

        JPanel footer = new JPanel(
                new BorderLayout()
        );

        footer.setBackground(Color.WHITE);

        footer.setBorder(
                BorderFactory.createEmptyBorder(
                        9, 16, 9, 16
                )
        );

        JLabel warning = new JLabel(
                "Aviso: as informações são gerais e não "
                + "substituem orientação de nutricionista ou médico.",
                SwingConstants.CENTER
        );

        warning.setForeground(
                new Color(90, 90, 90)
        );

        footer.add(warning, BorderLayout.CENTER);

        return footer;
    }

    /**
     * Atualiza o catálogo e os favoritos.
     */
    private void refreshAllPanels() {

        catalogPanel.refreshRecipes();
        favoritesPanel.refreshRecipes();
    }
}
