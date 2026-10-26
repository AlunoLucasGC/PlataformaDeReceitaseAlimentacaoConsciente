package br.com.receitassaudaveis.dao;

import br.com.receitassaudaveis.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Persiste a inclusão ou remoção de uma receita dos favoritos do usuário. */
public class FavoriteDAO {

    public boolean toggleFavorite(int userId, int recipeId) throws SQLException {
        String checkSql = "SELECT 1 FROM favorites WHERE user_id = ? AND recipe_id = ?";
        String deleteSql = "DELETE FROM favorites WHERE user_id = ? AND recipe_id = ?";
        String insertSql = "INSERT INTO favorites (user_id, recipe_id) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                boolean alreadyFavorite;
                try (PreparedStatement check = connection.prepareStatement(checkSql)) {
                    check.setInt(1, userId);
                    check.setInt(2, recipeId);
                    try (ResultSet result = check.executeQuery()) {
                        alreadyFavorite = result.next();
                    }
                }

                String sql = alreadyFavorite ? deleteSql : insertSql;
                try (PreparedStatement change = connection.prepareStatement(sql)) {
                    change.setInt(1, userId);
                    change.setInt(2, recipeId);
                    change.executeUpdate();
                }
                connection.commit();
                return !alreadyFavorite;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
