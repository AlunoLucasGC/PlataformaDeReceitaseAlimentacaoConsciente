package br.com.receitassaudaveis.dao;

import br.com.receitassaudaveis.database.DatabaseConnection;
import br.com.receitassaudaveis.model.Recipe;
import br.com.receitassaudaveis.model.RecipeFilter;
import br.com.receitassaudaveis.model.RecipeIngredient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Acesso ao catálogo e operações CRUD de receitas. */
public class RecipeDAO {

    public List<Recipe> findRecipes(RecipeFilter filter, int userId) throws SQLException {
        String sql = """
                SELECT r.id, r.title, r.description, c.name AS category,
                       r.prep_minutes, r.instructions, r.base_servings,
                       CASE WHEN f.recipe_id IS NULL THEN 0 ELSE 1 END AS is_favorite
                FROM recipes r
                INNER JOIN categories c ON c.id = r.category_id
                LEFT JOIN favorites f ON f.recipe_id = r.id AND f.user_id = ?
                WHERE (? = '' OR LOWER(r.title) LIKE LOWER(?)
                       OR EXISTS (
                           SELECT 1 FROM recipe_ingredients ri
                           INNER JOIN ingredients i ON i.id = ri.ingredient_id
                           WHERE ri.recipe_id = r.id AND LOWER(i.name) LIKE LOWER(?)
                       ))
                  AND (? = '' OR c.name = ?)
                  AND (? = 0 OR r.prep_minutes <= ?)
                  AND (? = '' OR EXISTS (
                       SELECT 1 FROM recipe_restrictions rr
                       INNER JOIN restrictions rs ON rs.id = rr.restriction_id
                       WHERE rr.recipe_id = r.id AND rs.name = ?
                  ))
                  AND (? = 0 OR f.recipe_id IS NOT NULL)
                ORDER BY r.title
                """;
        List<Recipe> recipes = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String search = filter.getSearch();
            String pattern = "%" + search + "%";
            String category = filter.getCategory();
            String restriction = filter.getRestriction();
            int maxMinutes = filter.getMaxMinutes();
            statement.setInt(1, userId);
            statement.setString(2, search);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setString(5, category);
            statement.setString(6, category);
            statement.setInt(7, maxMinutes);
            statement.setInt(8, maxMinutes);
            statement.setString(9, restriction);
            statement.setString(10, restriction);
            statement.setInt(11, filter.isFavoritesOnly() ? 1 : 0);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    recipes.add(new Recipe(result.getInt("id"), result.getString("title"),
                            result.getString("description"), result.getString("category"),
                            result.getInt("prep_minutes"), result.getString("instructions"),
                            result.getInt("base_servings"), result.getBoolean("is_favorite")));
                }
            }
        }
        return recipes;
    }

    public int createRecipe(String title, String description, String category, int prepMinutes,
            int baseServings, String instructions, List<RecipeIngredient> ingredients,
            List<String> restrictions) throws SQLException {
        String sql = "INSERT INTO recipes (category_id, title, description, prep_minutes, base_servings, instructions)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int categoryId = getOrCreateName(connection, "categories", category);
                int recipeId;
                try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, categoryId);
                    statement.setString(2, title.trim());
                    statement.setString(3, description.trim());
                    statement.setInt(4, prepMinutes);
                    statement.setInt(5, baseServings);
                    statement.setString(6, instructions.trim());
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("O banco não retornou o ID da receita.");
                        }
                        recipeId = keys.getInt(1);
                    }
                }
                replaceIngredients(connection, recipeId, ingredients);
                replaceRestrictions(connection, recipeId, restrictions);
                connection.commit();
                return recipeId;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void updateRecipe(int recipeId, String title, String description, String category,
            int prepMinutes, int baseServings, String instructions,
            List<RecipeIngredient> ingredients, List<String> restrictions) throws SQLException {
        String sql = "UPDATE recipes SET category_id = ?, title = ?, description = ?, prep_minutes = ?,"
                + " base_servings = ?, instructions = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int categoryId = getOrCreateName(connection, "categories", category);
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, categoryId);
                    statement.setString(2, title.trim());
                    statement.setString(3, description.trim());
                    statement.setInt(4, prepMinutes);
                    statement.setInt(5, baseServings);
                    statement.setString(6, instructions.trim());
                    statement.setInt(7, recipeId);
                    if (statement.executeUpdate() == 0) {
                        throw new SQLException("A receita selecionada não existe mais.");
                    }
                }
                replaceIngredients(connection, recipeId, ingredients);
                replaceRestrictions(connection, recipeId, restrictions);
                connection.commit();
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void deleteRecipe(int recipeId) throws SQLException {
        String sql = "DELETE FROM recipes WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, recipeId);
            statement.executeUpdate();
        }
    }

    private int getOrCreateName(Connection connection, String table, String name) throws SQLException {
        // table é definido exclusivamente por chamadas internas (categories/ingredients).
        String sql = "INSERT INTO " + table + " (name) VALUES (?) ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name.trim());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Não foi possível localizar ou criar " + name + ".");
    }

    private void replaceIngredients(Connection connection, int recipeId,
            List<RecipeIngredient> ingredients) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM recipe_ingredients WHERE recipe_id = ?")) {
            delete.setInt(1, recipeId);
            delete.executeUpdate();
        }
        String insert = "INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit)"
                + " VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insert)) {
            for (RecipeIngredient ingredient : ingredients) {
                int ingredientId = getOrCreateName(connection, "ingredients", ingredient.getName());
                statement.setInt(1, recipeId);
                statement.setInt(2, ingredientId);
                statement.setBigDecimal(3, ingredient.getQuantity());
                statement.setString(4, ingredient.getUnit() == null ? "" : ingredient.getUnit());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void replaceRestrictions(Connection connection, int recipeId,
            List<String> restrictions) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM recipe_restrictions WHERE recipe_id = ?")) {
            delete.setInt(1, recipeId);
            delete.executeUpdate();
        }
        String insert = "INSERT IGNORE INTO recipe_restrictions (recipe_id, restriction_id)"
                + " SELECT ?, id FROM restrictions WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(insert)) {
            for (String restriction : restrictions) {
                statement.setInt(1, recipeId);
                statement.setString(2, restriction);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public List<String> findCategories() throws SQLException {
        return findNames("SELECT name FROM categories ORDER BY name");
    }

    public List<String> findRestrictions() throws SQLException {
        return findNames("SELECT name FROM restrictions ORDER BY name");
    }

    private List<String> findNames(String sql) throws SQLException {
        List<String> names = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                names.add(result.getString("name"));
            }
        }
        return names;
    }
}
