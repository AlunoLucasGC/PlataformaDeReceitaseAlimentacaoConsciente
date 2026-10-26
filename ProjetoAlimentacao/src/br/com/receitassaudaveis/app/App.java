package br.com.receitassaudaveis.app;

import br.com.receitassaudaveis.database.DatabaseConnection;
import br.com.receitassaudaveis.model.User;
import br.com.receitassaudaveis.ui.AuthDialog;
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
            // Mantém o tema padrão do Swing se o tema do sistema não estiver disponível.
        }

        try (Connection ignored = DatabaseConnection.getConnection()) {
            // Verifica o banco antes de abrir a tela de autenticação.
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Não foi possível conectar ao MySQL.\n\n"
                    + "1. Confirme se o serviço do MySQL está iniciado.\n"
                    + "2. Execute database/atualizacao_v2.sql no banco receitas_saudaveis.\n"
                    + "3. Confira o arquivo src/db.properties e o driver MySQL Connector/J.\n\n"
                    + "Detalhe: " + ex.getMessage(),
                    "Falha na conexão", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(App::showLogin);
    }

    private static void showLogin() {
        AuthDialog login = new AuthDialog(null);
        login.setVisible(true);
        User user = login.getAuthenticatedUser();
        if (user == null) {
            System.exit(0);
            return;
        }
        try {
            MainFrame mainFrame = new MainFrame(user, App::showLogin);
            mainFrame.setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Não foi possível iniciar a aplicação: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
