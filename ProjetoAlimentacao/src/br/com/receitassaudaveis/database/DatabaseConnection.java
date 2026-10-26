package br.com.receitassaudaveis.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Centraliza a configuração e a abertura de conexões com o MySQL. */
public final class DatabaseConnection {

    private static final Properties PROPERTIES = loadProperties();

    private DatabaseConnection() { }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConnection.class.getResourceAsStream("/db.properties")) {
            if (input != null) {
                properties.load(input);
                return properties;
            }
        } catch (IOException ex) {
            throw new ExceptionInInitializerError("Não foi possível ler db.properties: " + ex.getMessage());
        }

        // Fallback para projetos Ant do NetBeans, nos quais db.properties fica diretamente em src.
        Path[] paths = {
            Path.of("src", "db.properties"),
            Path.of("src", "main", "resources", "db.properties")
        };
        for (Path path : paths) {
            if (Files.isRegularFile(path)) {
                try (InputStream input = Files.newInputStream(path)) {
                    properties.load(input);
                    return properties;
                } catch (IOException ex) {
                    throw new ExceptionInInitializerError("Erro ao ler " + path + ": " + ex.getMessage());
                }
            }
        }
        throw new ExceptionInInitializerError(
                "Arquivo db.properties não encontrado. Coloque-o diretamente dentro de src.");
    }

    public static Connection getConnection() throws SQLException {
        String url = PROPERTIES.getProperty("db.url");
        String user = PROPERTIES.getProperty("db.user");
        String password = PROPERTIES.getProperty("db.password", "");
        if (url == null || user == null) {
            throw new SQLException("db.properties precisa definir db.url e db.user.");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
