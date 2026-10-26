package br.com.receitassaudaveis.dao;

import br.com.receitassaudaveis.database.DatabaseConnection;
import br.com.receitassaudaveis.model.RecipeIngredient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Busca a lista de ingredientes de uma receita. */
public class IngredientDAO {

    public List<RecipeIngredient> findByRecipeId(int recipeId) throws SQLException {
        String sql = """
                SELECT i.name, ri.quantity, ri.unit
                FROM recipe_ingredients ri
                INNER JOIN ingredients i ON i.id = ri.ingredient_id
                WHERE ri.recipe_id = ?
                ORDER BY i.name
                """;

        List<RecipeIngredient> ingredients = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, recipeId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    ingredients.add(new RecipeIngredient(
                            result.getString("name"),
                            result.getBigDecimal("quantity"),
                            result.getString("unit")
                    ));
                }
            }
        }
        return ingredients;
    }
}
