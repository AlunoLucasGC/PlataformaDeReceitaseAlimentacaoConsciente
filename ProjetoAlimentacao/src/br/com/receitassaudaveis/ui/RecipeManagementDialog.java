package br.com.receitassaudaveis.ui;

import br.com.receitassaudaveis.model.Recipe;
import br.com.receitassaudaveis.model.RecipeFilter;
import br.com.receitassaudaveis.service.RecipeService;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.sql.SQLException;
import java.util.List;

/** Área de gerenciamento para cadastrar, editar e excluir receitas. */
public class RecipeManagementDialog extends JDialog {

    private final RecipeService service;
    private final Runnable onChanged;
    private final RecipeTableModel tableModel = new RecipeTableModel();
    private final JTable table = new JTable(tableModel);
    private List<Recipe> currentRecipes = List.of();

    public RecipeManagementDialog(Frame owner, RecipeService service, Runnable onChanged) throws SQLException {
        super(owner, "Gerenciar receitas", true);
        this.service = service;
        this.onChanged = onChanged;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(owner);
        buildInterface();
        refresh();
    }

    private void buildInterface() {
        setLayout(new BorderLayout(8, 8));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(27);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton addButton = new JButton("Cadastrar receita");
        JButton editButton = new JButton("Editar selecionada");
        JButton deleteButton = new JButton("Excluir selecionada");
        JButton refreshButton = new JButton("Atualizar");
        JButton closeButton = new JButton("Fechar");
        addButton.addActionListener(event -> openEditor(null));
        editButton.addActionListener(event -> openEditor(getSelectedRecipe()));
        deleteButton.addActionListener(event -> deleteSelected());
        refreshButton.addActionListener(event -> refresh());
        closeButton.addActionListener(event -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(refreshButton);
        actions.add(closeButton);
        add(actions, BorderLayout.SOUTH);
    }

    private void refresh() {
        try {
            currentRecipes = service.search(new RecipeFilter("", "", "", 0, false), 1);
            tableModel.setRecipes(currentRecipes);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível carregar as receitas: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Recipe getSelectedRecipe() {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma receita na tabela.",
                    "Nenhuma seleção", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int modelIndex = table.convertRowIndexToModel(selected);
        return tableModel.getRecipeAt(modelIndex);
    }

    private void openEditor(Recipe recipe) {
        try {
            RecipeEditorDialog editor = new RecipeEditorDialog((Frame) getOwner(), service, recipe);
            editor.setVisible(true);
            if (editor.isSaved()) {
                refresh();
                onChanged.run();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível abrir o formulário: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        Recipe recipe = getSelectedRecipe();
        if (recipe == null) return;
        int answer = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir a receita '" + recipe.getTitle() + "'?\n"
                + "Os favoritos e comentários dessa receita também serão removidos.",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            service.deleteRecipe(recipe.getId());
            refresh();
            onChanged.run();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível excluir a receita: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
