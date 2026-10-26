package br.com.receitassaudaveis.dao;

import br.com.receitassaudaveis.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Busca as classificações alimentares associadas a uma receita. */
public class RestrictionDAO {

    public List<String> findByRecipeId(int recipeId) throws SQLException {
        String sql = """
                SELECT rs.name
                FROM recipe_restrictions rr
                INNER JOIN restrictions rs ON rs.id = rr.restriction_id
                WHERE rr.recipe_id = ?
                ORDER BY rs.name
                """;
        List<String> restrictions = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, recipeId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    restrictions.add(result.getString("name"));
                }
            }
        }
        return restrictions;
    }
}
