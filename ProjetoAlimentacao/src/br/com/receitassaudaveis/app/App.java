package br.com.receitassaudaveis.app;

import br.com.receitassaudaveis.database.DatabaseConnection;
import br.com.receitassaudaveis.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.Connection;
import java.sql.SQLException;

/** Ponto de entrada da aplicação. */
public class App {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Mantém o tema padrão do Swing caso o tema do sistema não esteja disponível.
        }

        try (Connection ignored = DatabaseConnection.getConnection()) {
            // Testa a conexão antes de abrir a janela.
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Não foi possível conectar ao MySQL.\n\n"
                    + "1. Confirme se o serviço do MySQL está iniciado.\n"
                    + "2. Execute o arquivo database/receitas_saudaveis.sql.\n"
                    + "3. Confira src/main/resources/db.properties.\n\n"
                    + "Detalhe: " + e.getMessage(),
                    "Falha na conexão", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame().setVisible(true);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Não foi possível iniciar a aplicação: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
