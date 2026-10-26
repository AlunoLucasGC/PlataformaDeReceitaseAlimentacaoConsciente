
package br.com.receitassaudaveis.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {

    private static final Properties PROPERTIES = loadProperties();

    private DatabaseConnection() {
    }

    private static Properties loadProperties() {

        Properties properties = new Properties();

        // Primeiro tenta localizar o arquivo no classpath.
        try (InputStream input =
                DatabaseConnection.class.getResourceAsStream("/db.properties")) {

            if (input != null) {
                properties.load(input);
                return properties;
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Erro ao carregar db.properties: " + e.getMessage(), e);
        }

        // Caso não encontre, procura nos caminhos do projeto.
        Path[] caminhos = {
            Path.of("src", "db.properties"),
            Path.of("src", "main", "resources", "db.properties")
        };

        for (Path caminho : caminhos) {

            if (Files.exists(caminho)) {

                try (InputStream input = Files.newInputStream(caminho)) {

                    properties.load(input);
                    return properties;

                } catch (IOException e) {

                    throw new IllegalStateException(
                            "Erro ao ler db.properties: " + e.getMessage(), e);
                }
            }
        }

        throw new IllegalStateException(
                "Arquivo db.properties não encontrado. "
                + "Coloque-o dentro da pasta src do projeto."
        );
    }

    public static Connection getConnection() throws SQLException {

        String url = PROPERTIES.getProperty("db.url");
        String user = PROPERTIES.getProperty("db.user");
        String password = PROPERTIES.getProperty("db.password", "");

        return DriverManager.getConnection(url, user, password);
    }
}
