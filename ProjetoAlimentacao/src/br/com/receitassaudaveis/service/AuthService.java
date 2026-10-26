package br.com.receitassaudaveis.service;

import br.com.receitassaudaveis.dao.UserDAO;
import br.com.receitassaudaveis.model.User;
import br.com.receitassaudaveis.security.PasswordUtil;

import java.sql.SQLException;

/** Regras de autenticação e criação de contas. */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User login(String email, char[] password) throws SQLException {
        if (email == null || email.isBlank() || password == null || password.length == 0) {
            return null;
        }
        String hash = userDAO.findPasswordHashByEmail(email);
        if (hash == null || !PasswordUtil.verify(password, hash)) {
            return null;
        }
        return userDAO.findByEmail(email);
    }

    public User register(String name, String email, char[] password) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Informe seu nome.");
        }
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("A senha precisa ter pelo menos 8 caracteres.");
        }
        if (userDAO.findByEmail(email) != null) {
            throw new IllegalArgumentException("Já existe uma conta cadastrada com esse e-mail.");
        }
        return userDAO.create(name, email, PasswordUtil.hash(password));
    }
}
