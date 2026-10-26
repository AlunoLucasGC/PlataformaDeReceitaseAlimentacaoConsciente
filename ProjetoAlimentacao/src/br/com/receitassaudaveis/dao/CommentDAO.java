package br.com.receitassaudaveis.dao;

import br.com.receitassaudaveis.database.DatabaseConnection;
import br.com.receitassaudaveis.model.Comment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Persiste e consulta comentários das receitas. */
public class CommentDAO {

    public List<Comment> findByRecipeId(int recipeId) throws SQLException {
        String sql = """
                SELECT c.id, c.user_id, u.name AS user_name, c.content,
                       DATE_FORMAT(c.created_at, '%d/%m/%Y %H:%i') AS created_at
                FROM comments c
                INNER JOIN users u ON u.id = c.user_id
                WHERE c.recipe_id = ?
                ORDER BY c.created_at DESC, c.id DESC
                """;
        List<Comment> comments = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, recipeId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    comments.add(new Comment(result.getInt("id"), result.getInt("user_id"),
                            result.getString("user_name"), result.getString("content"),
                            result.getString("created_at")));
                }
            }
        }
        return comments;
    }

    public void add(int recipeId, int userId, String content) throws SQLException {
        String sql = "INSERT INTO comments (recipe_id, user_id, content) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, recipeId);
            statement.setInt(2, userId);
            statement.setString(3, content.trim());
            statement.executeUpdate();
        }
    }

    /** Exclui somente um comentário pertencente ao próprio usuário. */
    public boolean deleteOwnComment(int commentId, int userId) throws SQLException {
        String sql = "DELETE FROM comments WHERE id = ? AND user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, commentId);
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        }
    }
}
