package br.com.receitassaudaveis.ui;

import br.com.receitassaudaveis.model.User;
import br.com.receitassaudaveis.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.awt.Color;

/** Tela inicial para entrar, criar conta ou acessar como visitante. */
public class AuthDialog extends JDialog {

    private final AuthService authService = new AuthService();
    private User authenticatedUser;

    private final JTextField loginEmail = new JTextField(22);
    private final JPasswordField loginPassword = new JPasswordField(22);
    private final JTextField registerName = new JTextField(22);
    private final JTextField registerEmail = new JTextField(22);
    private final JPasswordField registerPassword = new JPasswordField(22);
    private final JPasswordField confirmPassword = new JPasswordField(22);

    public AuthDialog(java.awt.Frame owner) {
        super(owner, "Receitas Saudáveis - Acesso", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(470, 390);
        setResizable(false);
        setLocationRelativeTo(owner);
        buildInterface();
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(UITheme.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 16, 22));
        JLabel title = new JLabel("Bem-vindo ao Receitas Saudáveis");
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 21f));
        title.setForeground(UITheme.GREEN_900);
        root.add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(tabs.getFont().deriveFont(java.awt.Font.BOLD, 12.5f));
        tabs.addTab("Entrar", createLoginPanel());
        tabs.addTab("Criar conta", createRegisterPanel());
        root.add(tabs, BorderLayout.CENTER);

        JButton guestButton = new JButton("Continuar como visitante");
        UITheme.styleSecondaryButton(guestButton);
        guestButton.addActionListener(event -> {
            authenticatedUser = new User(1, "Visitante", "visitante@local");
            dispose();
        });
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(guestButton);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);
        getRootPane().setDefaultButton((JButton) ((JPanel) tabs.getComponentAt(0)).getClientProperty("loginButton"));
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel fields = new JPanel(new GridLayout(2, 2, 8, 12));
        fields.setBackground(UITheme.SURFACE);
        fields.setBorder(BorderFactory.createEmptyBorder(24, 12, 12, 12));
        fields.add(new JLabel("E-mail:"));
        UITheme.styleTextField(loginEmail);
        fields.add(loginEmail);
        fields.add(new JLabel("Senha:"));
        UITheme.styleTextField(loginPassword);
        fields.add(loginPassword);
        panel.add(fields, BorderLayout.CENTER);
        JButton loginButton = new JButton("Entrar");
        styleActionButton(loginButton);
        loginButton.addActionListener(event -> login());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(loginButton);
        panel.add(buttons, BorderLayout.SOUTH);
        panel.putClientProperty("loginButton", loginButton);
        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel fields = new JPanel(new GridLayout(4, 2, 8, 10));
        fields.setBackground(UITheme.SURFACE);
        fields.setBorder(BorderFactory.createEmptyBorder(16, 12, 12, 12));
        fields.add(new JLabel("Nome:"));
        UITheme.styleTextField(registerName);
        fields.add(registerName);
        fields.add(new JLabel("E-mail:"));
        UITheme.styleTextField(registerEmail);
        fields.add(registerEmail);
        fields.add(new JLabel("Senha (mínimo 8 caracteres):"));
        UITheme.styleTextField(registerPassword);
        fields.add(registerPassword);
        fields.add(new JLabel("Confirmar senha:"));
        UITheme.styleTextField(confirmPassword);
        fields.add(confirmPassword);
        panel.add(fields, BorderLayout.CENTER);
        JButton registerButton = new JButton("Criar minha conta");
        styleActionButton(registerButton);
        registerButton.addActionListener(event -> register());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(registerButton);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void login() {
        char[] password = loginPassword.getPassword();
        try {
            User user = authService.login(loginEmail.getText(), password);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "E-mail ou senha incorretos.",
                        "Não foi possível entrar", JOptionPane.WARNING_MESSAGE);
                return;
            }
            authenticatedUser = user;
            dispose();
        } catch (SQLException ex) {
            showError(ex);
        } finally {
            java.util.Arrays.fill(password, '\0');
            loginPassword.setText("");
        }
    }

    private void register() {
        char[] password = registerPassword.getPassword();
        char[] confirmation = confirmPassword.getPassword();
        try {
            if (!java.util.Arrays.equals(password, confirmation)) {
                JOptionPane.showMessageDialog(this, "As senhas não coincidem.",
                        "Verifique a senha", JOptionPane.WARNING_MESSAGE);
                return;
            }
            authenticatedUser = authService.register(registerName.getText(),
                    registerEmail.getText(), password);
            JOptionPane.showMessageDialog(this, "Conta criada com sucesso! Você já está conectado.",
                    "Cadastro concluído", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dados inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showError(ex);
        } finally {
            java.util.Arrays.fill(password, '\0');
            java.util.Arrays.fill(confirmation, '\0');
            registerPassword.setText("");
            confirmPassword.setText("");
        }
    }

    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Não foi possível concluir a operação.\n\nDetalhe: " + ex.getMessage(),
                "Erro de banco de dados", JOptionPane.ERROR_MESSAGE);
    }
    
    private void styleActionButton(JButton button) {

    button.setEnabled(true);
    button.setOpaque(true);
    button.setContentAreaFilled(true);
    button.setFocusPainted(false);

    button.setBackground(new Color(35, 105, 76));
    button.setForeground(Color.WHITE);

    button.setFont(
            button.getFont().deriveFont(
                    java.awt.Font.BOLD, 13f
            )
    );

    button.setBorder(
            BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            new Color(27, 82, 59)
                    ),
                    BorderFactory.createEmptyBorder(
                            8, 20, 8, 20
                    )
            )
    );
}
}
