package br.com.receitassaudaveis.dao;

import br.com.receitassaudaveis.database.DatabaseConnection;
import br.com.receitassaudaveis.model.Recipe;
import br.com.receitassaudaveis.model.RecipeFilter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acesso ao catálogo de receitas no banco de dados. */
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
                           SELECT 1
                           FROM recipe_ingredients ri
                           INNER JOIN ingredients i ON i.id = ri.ingredient_id
                           WHERE ri.recipe_id = r.id AND LOWER(i.name) LIKE LOWER(?)
                       ))
                  AND (? = '' OR c.name = ?)
                  AND (? = 0 OR r.prep_minutes <= ?)
                  AND (? = '' OR EXISTS (
                       SELECT 1
                       FROM recipe_restrictions rr
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
            String searchPattern = "%" + search + "%";
            String category = filter.getCategory();
            String restriction = filter.getRestriction();
            int maxMinutes = filter.getMaxMinutes();

            statement.setInt(1, userId);
            statement.setString(2, search);
            statement.setString(3, searchPattern);
            statement.setString(4, searchPattern);
            statement.setString(5, category);
            statement.setString(6, category);
            statement.setInt(7, maxMinutes);
            statement.setInt(8, maxMinutes);
            statement.setString(9, restriction);
            statement.setString(10, restriction);
            statement.setInt(11, filter.isFavoritesOnly() ? 1 : 0);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    recipes.add(new Recipe(
                            result.getInt("id"),
                            result.getString("title"),
                            result.getString("description"),
                            result.getString("category"),
                            result.getInt("prep_minutes"),
                            result.getString("instructions"),
                            result.getInt("base_servings"),
                            result.getBoolean("is_favorite")
                    ));
                }
            }
        }
        return recipes;
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
