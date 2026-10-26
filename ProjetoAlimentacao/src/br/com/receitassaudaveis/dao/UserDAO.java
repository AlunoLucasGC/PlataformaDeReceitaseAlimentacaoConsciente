package br.com.receitassaudaveis.dao;

import br.com.receitassaudaveis.database.DatabaseConnection;
import br.com.receitassaudaveis.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Operações de persistência de contas de usuário. */
public class UserDAO {

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT id, name, email, password_hash FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                return new User(result.getInt("id"), result.getString("name"),
                        result.getString("email"));
            }
        }
    }

    public String findPasswordHashByEmail(String email) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString("password_hash") : null;
            }
        }
    }

    public User create(String name, String email, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name.trim());
            statement.setString(2, email.trim().toLowerCase());
            statement.setString(3, passwordHash);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(keys.getInt(1), name.trim(), email.trim().toLowerCase());
                }
            }
        }
        throw new SQLException("O banco não retornou o ID do novo usuário.");
    }
}
