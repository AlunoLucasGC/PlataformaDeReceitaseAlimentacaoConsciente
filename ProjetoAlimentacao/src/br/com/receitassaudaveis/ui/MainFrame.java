package br.com.receitassaudaveis.ui;

import br.com.receitassaudaveis.service.RecipeService;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;

/** Janela principal da aplicação. */
public class MainFrame extends JFrame {

    private final RecipeService service = new RecipeService();
    private RecipePanel catalogPanel;
    private RecipePanel favoritesPanel;

    public MainFrame() throws SQLException {
        super("Receitas Saudáveis");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new java.awt.Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        catalogPanel = new RecipePanel(this, service, false, this::refreshAllPanels);
        favoritesPanel = new RecipePanel(this, service, true, this::refreshAllPanels);
        tabs.addTab("Catálogo de receitas", catalogPanel);
        tabs.addTab("Meus favoritos", favoritesPanel);
        add(tabs, BorderLayout.CENTER);

        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(38, 105, 72));
        header.setBorder(BorderFactory.createEmptyBorder(17, 22, 17, 22));
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
        return header;
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
