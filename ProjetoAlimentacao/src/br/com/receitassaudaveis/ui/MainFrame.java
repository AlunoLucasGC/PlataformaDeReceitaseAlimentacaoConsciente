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
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;

/** Janela principal, associada à conta atualmente conectada. */
public class MainFrame extends JFrame {

    private final RecipeService service = new RecipeService();
    private final User currentUser;
    private final Runnable onLogout;
    private RecipePanel catalogPanel;
    private RecipePanel favoritesPanel;

    public MainFrame(User currentUser, Runnable onLogout) throws SQLException {
        super("Receitas Saudáveis");
        this.currentUser = currentUser;
        this.onLogout = onLogout;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1250, 790);
        setMinimumSize(new java.awt.Dimension(980, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        catalogPanel = new RecipePanel(this, service, currentUser, false, this::refreshAllPanels);
        favoritesPanel = new RecipePanel(this, service, currentUser, true, this::refreshAllPanels);
        tabs.addTab("Catálogo de receitas", catalogPanel);
        tabs.addTab("Meus favoritos", favoritesPanel);
        add(tabs, BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setBackground(new Color(38, 105, 72));
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 18));
        JLabel title = new JLabel("Receitas Saudáveis");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        JLabel subtitle = new JLabel("Encontre receitas por ingrediente, refeição, tempo e preferência alimentar");
        subtitle.setForeground(new Color(235, 245, 238));
        subtitle.setFont(subtitle.getFont().deriveFont(13f));
        JPanel text = new JPanel(new BorderLayout(2, 4));
        text.setOpaque(false);
        text.add(title, BorderLayout.NORTH);
        text.add(subtitle, BorderLayout.CENTER);
        header.add(text, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        actions.setOpaque(false);
        JLabel account = new JLabel("Conectado: " + currentUser.getName());
        account.setForeground(Color.WHITE);
        actions.add(account);
        JButton manageButton = new JButton("Gerenciar receitas");
        manageButton.setEnabled(!currentUser.isGuest());
        manageButton.setToolTipText(currentUser.isGuest()
                ? "Crie uma conta ou entre para cadastrar e editar receitas."
                : "Cadastrar, editar ou excluir receitas");
        manageButton.addActionListener(event -> openRecipeManagement());
        actions.add(manageButton);
        JButton switchUserButton = new JButton("Trocar usuário");
        switchUserButton.addActionListener(event -> {
            dispose();
            onLogout.run();
        });
        actions.add(switchUserButton);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private void openRecipeManagement() {
        try {
            RecipeManagementDialog dialog = new RecipeManagementDialog(this, service, this::refreshAllPanels);
            dialog.setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível abrir o gerenciamento: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        JLabel warning = new JLabel(
                "Aviso: as informações são gerais e não substituem orientação de nutricionista ou médico.",
                SwingConstants.CENTER);
        warning.setForeground(new Color(90, 90, 90));
        footer.add(warning, BorderLayout.CENTER);
        return footer;
    }

    private void refreshAllPanels() {
        catalogPanel.refreshRecipes();
        favoritesPanel.refreshRecipes();
    }
}
