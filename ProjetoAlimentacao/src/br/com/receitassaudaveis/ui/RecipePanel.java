package br.com.receitassaudaveis.ui;

import br.com.receitassaudaveis.model.Recipe;
import br.com.receitassaudaveis.model.RecipeFilter;
import br.com.receitassaudaveis.model.User;
import br.com.receitassaudaveis.service.RecipeService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.sql.SQLException;
import java.util.List;

/** Painel reutilizável para o catálogo e para os favoritos do usuário conectado. */
public class RecipePanel extends JPanel {

    private final RecipeService service;
    private final User currentUser;
    private final boolean favoritesOnly;
    private final Runnable onFavoriteChanged;
    private final Frame owner;

    private final JTextField searchField = new JTextField(19);
    private final JComboBox<String> categoryCombo = new JComboBox<>();
    private final JComboBox<String> restrictionCombo = new JComboBox<>();
    private final JComboBox<String> timeCombo = new JComboBox<>(new String[]{
        "Qualquer tempo", "Até 15 minutos", "Até 30 minutos", "Até 45 minutos", "Até 60 minutos"
    });
    private final RecipeTableModel tableModel = new RecipeTableModel();
    private final JTable table = new JTable(tableModel);

    public RecipePanel(Frame owner, RecipeService service, User currentUser,
            boolean favoritesOnly, Runnable onFavoriteChanged) throws SQLException {
        this.owner = owner;
        this.service = service;
        this.currentUser = currentUser;
        this.favoritesOnly = favoritesOnly;
        this.onFavoriteChanged = onFavoriteChanged;
        buildInterface();
        loadFilterOptions();
        refreshRecipes();
    }

    private void buildInterface() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        filters.add(new JLabel("Buscar:"));
        filters.add(searchField);
        filters.add(new JLabel("Categoria:"));
        filters.add(categoryCombo);
        filters.add(new JLabel("Restrição:"));
        filters.add(restrictionCombo);
        filters.add(new JLabel("Tempo:"));
        filters.add(timeCombo);
        JButton searchButton = new JButton("Buscar");
        JButton clearButton = new JButton("Limpar filtros");
        filters.add(searchButton);
        filters.add(clearButton);
        add(filters, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(29);
        table.setFont(table.getFont().deriveFont(13f));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton detailsButton = new JButton("Ver detalhes e comentários");
        JButton favoriteButton = new JButton("Adicionar / remover favorito");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(favoriteButton);
        actions.add(detailsButton);
        add(actions, BorderLayout.SOUTH);

        searchButton.addActionListener(event -> refreshRecipes());
        searchField.addActionListener(event -> refreshRecipes());
        clearButton.addActionListener(event -> clearFilters());
        detailsButton.addActionListener(event -> openSelectedRecipe());
        favoriteButton.addActionListener(event -> toggleSelectedFavorite());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) openSelectedRecipe();
            }
        });
    }

    private void loadFilterOptions() throws SQLException {
        categoryCombo.addItem("Todas");
        for (String category : service.getCategories()) categoryCombo.addItem(category);
        restrictionCombo.addItem("Todas");
        for (String restriction : service.getRestrictions()) restrictionCombo.addItem(restriction);
    }

    public void refreshRecipes() {
        try {
            RecipeFilter filter = new RecipeFilter(searchField.getText(), selectedFilterValue(categoryCombo),
                    selectedFilterValue(restrictionCombo), getMaxMinutes(), favoritesOnly);
            List<Recipe> recipes = service.search(filter, currentUser.getId());
            tableModel.setRecipes(recipes);
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private String selectedFilterValue(JComboBox<String> combo) {
        Object selected = combo.getSelectedItem();
        return selected == null || "Todas".equals(selected) ? "" : selected.toString();
    }

    private int getMaxMinutes() {
        return switch (timeCombo.getSelectedIndex()) {
            case 1 -> 15;
            case 2 -> 30;
            case 3 -> 45;
            case 4 -> 60;
            default -> 0;
        };
    }

    private void clearFilters() {
        searchField.setText("");
        categoryCombo.setSelectedIndex(0);
        restrictionCombo.setSelectedIndex(0);
        timeCombo.setSelectedIndex(0);
        refreshRecipes();
    }

    private Recipe getSelectedRecipe() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma receita na tabela.",
                    "Nenhuma receita selecionada", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return tableModel.getRecipeAt(table.convertRowIndexToModel(selectedRow));
    }

    private void openSelectedRecipe() {
        Recipe recipe = getSelectedRecipe();
        if (recipe == null) return;
        try {
            RecipeDetailDialog dialog = new RecipeDetailDialog(owner, recipe, service, currentUser);
            dialog.setVisible(true);
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void toggleSelectedFavorite() {
        Recipe recipe = getSelectedRecipe();
        if (recipe == null) return;
        try {
            boolean added = service.toggleFavorite(currentUser.getId(), recipe.getId());
            JOptionPane.showMessageDialog(this,
                    added ? "Receita adicionada aos favoritos desta conta."
                            : "Receita removida dos favoritos desta conta.",
                    "Favoritos", JOptionPane.INFORMATION_MESSAGE);
            onFavoriteChanged.run();
            refreshRecipes();
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void showDatabaseError(SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Não foi possível consultar o banco de dados.\n\nDetalhe: " + ex.getMessage(),
                "Erro de banco de dados", JOptionPane.ERROR_MESSAGE);
    }
}
