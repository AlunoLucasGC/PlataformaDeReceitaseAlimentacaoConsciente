package br.com.receitassaudaveis.ui;

import br.com.receitassaudaveis.model.Comment;
import br.com.receitassaudaveis.model.Recipe;
import br.com.receitassaudaveis.model.RecipeIngredient;
import br.com.receitassaudaveis.model.User;
import br.com.receitassaudaveis.service.RecipeService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.SQLException;
import java.util.List;

/** Detalhes da receita, cálculo de porções e comentários persistidos no banco. */
public class RecipeDetailDialog extends JDialog {

    private final Recipe recipe;
    private final RecipeService service;
    private final User currentUser;
    private final List<RecipeIngredient> ingredients;
    private final JTextArea ingredientsArea = new JTextArea();
    private final JTextArea commentsArea = new JTextArea();
    private final JTextField commentField = new JTextField(45);
    private final JSpinner servingsSpinner;

    public RecipeDetailDialog(java.awt.Frame owner, Recipe recipe, RecipeService service,
            User currentUser) throws SQLException {
        super(owner, "Detalhes da receita", true);
        this.recipe = recipe;
        this.service = service;
        this.currentUser = currentUser;
        this.ingredients = service.getIngredients(recipe.getId());
        List<String> restrictions = service.getRestrictionsForRecipe(recipe.getId());
        this.servingsSpinner = new JSpinner(new SpinnerNumberModel(recipe.getBaseServings(), 1, 100, 1));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(790, 800);
        setMinimumSize(new Dimension(650, 650));
        setLocationRelativeTo(owner);
        buildInterface(restrictions);
        refreshComments();
        servingsSpinner.addChangeListener(event -> updateIngredients());
        updateIngredients();
    }

    private void buildInterface(List<String> restrictions) {
        JPanel content = new JPanel();
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(18, 20, 16, 20));

        JLabel title = new JLabel(recipe.getTitle());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setAlignmentX(LEFT_ALIGNMENT);
        content.add(title);
        content.add(javax.swing.Box.createVerticalStrut(8));

        JLabel description = new JLabel("<html><div style='width:700px'>"
                + escapeHtml(recipe.getDescription())
                + "<br><b>Categoria:</b> " + escapeHtml(recipe.getCategory())
                + " &nbsp; | &nbsp; <b>Tempo:</b> " + recipe.getPrepMinutes() + " minutos"
                + "<br><b>Classificações:</b> "
                + (restrictions.isEmpty() ? "Não informadas" : escapeHtml(String.join(", ", restrictions)))
                + "</div></html>");
        description.setAlignmentX(LEFT_ALIGNMENT);
        content.add(description);
        content.add(javax.swing.Box.createVerticalStrut(12));

        JPanel ingredientPanel = new JPanel(new BorderLayout(8, 8));
        ingredientPanel.setBorder(BorderFactory.createTitledBorder("Ingredientes"));
        JPanel servingsPanel = new JPanel(new BorderLayout(5, 5));
        servingsPanel.add(new JLabel("Porções:"), BorderLayout.WEST);
        servingsPanel.add(servingsSpinner, BorderLayout.CENTER);
        servingsPanel.setPreferredSize(new Dimension(150, 32));
        ingredientPanel.add(servingsPanel, BorderLayout.NORTH);
        ingredientsArea.setEditable(false);
        ingredientsArea.setLineWrap(true);
        ingredientsArea.setWrapStyleWord(true);
        ingredientsArea.setFont(ingredientsArea.getFont().deriveFont(14f));
        ingredientsArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        ingredientPanel.add(new JScrollPane(ingredientsArea), BorderLayout.CENTER);
        ingredientPanel.setPreferredSize(new Dimension(720, 150));
        ingredientPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        ingredientPanel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(ingredientPanel);
        content.add(javax.swing.Box.createVerticalStrut(10));

        JTextArea instructionsArea = new JTextArea(recipe.getInstructions());
        instructionsArea.setEditable(false);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setFont(instructionsArea.getFont().deriveFont(14f));
        instructionsArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel preparationPanel = new JPanel(new BorderLayout(6, 6));
        preparationPanel.setBorder(BorderFactory.createTitledBorder("Modo de preparo"));
        preparationPanel.add(new JScrollPane(instructionsArea), BorderLayout.CENTER);
        preparationPanel.setPreferredSize(new Dimension(720, 190));
        preparationPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        preparationPanel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(preparationPanel);
        content.add(javax.swing.Box.createVerticalStrut(10));

        JPanel commentsPanel = new JPanel(new BorderLayout(6, 6));
        commentsPanel.setBorder(BorderFactory.createTitledBorder("Comentários"));
        commentsArea.setEditable(false);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        commentsArea.setFont(commentsArea.getFont().deriveFont(13f));
        commentsArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        commentsPanel.add(new JScrollPane(commentsArea), BorderLayout.CENTER);

        JPanel commentEntry = new JPanel(new BorderLayout(6, 6));
        JButton postButton = new JButton("Publicar comentário");
        commentEntry.add(commentField, BorderLayout.CENTER);
        commentEntry.add(postButton, BorderLayout.EAST);
        if (currentUser.isGuest()) {
            commentField.setEnabled(false);
            postButton.setEnabled(false);
            commentField.setText("Entre com uma conta para comentar.");
            commentEntry.setToolTipText("Comentários disponíveis para usuários cadastrados.");
        } else {
            commentField.setToolTipText("Escreva seu comentário (até 2.000 caracteres)");
            postButton.addActionListener(event -> postComment());
            commentField.addActionListener(event -> postComment());
        }
        commentsPanel.add(commentEntry, BorderLayout.SOUTH);
        commentsPanel.setPreferredSize(new Dimension(720, 230));
        commentsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        commentsPanel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(commentsPanel);
        content.add(javax.swing.Box.createVerticalStrut(10));

        JLabel disclaimer = new JLabel("<html><div style='width:700px;color:#666'>Atenção: estas receitas são informações gerais e não substituem a orientação de nutricionista ou médico. "
                + "Em caso de alergias, intolerâncias ou condições de saúde, confirme os ingredientes com um profissional.</div></html>");
        disclaimer.setAlignmentX(LEFT_ALIGNMENT);
        content.add(disclaimer);

        add(new JScrollPane(content), BorderLayout.CENTER);
        JButton closeButton = new JButton("Fechar");
        closeButton.addActionListener(event -> dispose());
        JPanel bottom = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        bottom.add(closeButton);
        add(bottom, BorderLayout.SOUTH);
    }

    private void updateIngredients() {
        int servings = (Integer) servingsSpinner.getValue();
        StringBuilder text = new StringBuilder();
        for (RecipeIngredient ingredient : ingredients) {
            text.append("• ").append(ingredient.formatForServings(servings, recipe.getBaseServings())).append('\n');
        }
        ingredientsArea.setText(text.toString());
        ingredientsArea.setCaretPosition(0);
    }

    private void refreshComments() {
        try {
            List<Comment> comments = service.getComments(recipe.getId());
            StringBuilder text = new StringBuilder();
            if (comments.isEmpty()) {
                text.append("Ainda não há comentários para esta receita. Seja o primeiro a comentar!\n");
            } else {
                for (Comment comment : comments) {
                    text.append(comment.getUserName()).append("  •  ")
                            .append(comment.getCreatedAt()).append('\n')
                            .append(comment.getContent()).append("\n\n");
                }
            }
            commentsArea.setText(text.toString());
            commentsArea.setCaretPosition(0);
        } catch (SQLException ex) {
            commentsArea.setText("Não foi possível carregar os comentários: " + ex.getMessage());
        }
    }

    private void postComment() {
        try {
            service.addComment(recipe.getId(), currentUser.getId(), commentField.getText());
            commentField.setText("");
            refreshComments();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Comentário inválido", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível publicar o comentário: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escapeHtml(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
