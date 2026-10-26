package br.com.receitassaudaveis.ui;

import br.com.receitassaudaveis.model.Recipe;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Modelo da tabela Swing que apresenta as receitas. */
public class RecipeTableModel extends AbstractTableModel {

    private final String[] columns = {"Receita", "Categoria", "Preparo", "Porções", "Favorito"};
    private List<Recipe> recipes = new ArrayList<>();

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = new ArrayList<>(recipes);
        fireTableDataChanged();
    }

    public Recipe getRecipeAt(int row) {
        if (row < 0 || row >= recipes.size()) {
            return null;
        }
        return recipes.get(row);
    }

    @Override
    public int getRowCount() { return recipes.size(); }

    @Override
    public int getColumnCount() { return columns.length; }

    @Override
    public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Recipe recipe = recipes.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> recipe.getTitle();
            case 1 -> recipe.getCategory();
            case 2 -> recipe.getPrepMinutes() + " min";
            case 3 -> recipe.getBaseServings();
            case 4 -> recipe.isFavorite() ? "★" : "☆";
            default -> "";
        };
    }
}
