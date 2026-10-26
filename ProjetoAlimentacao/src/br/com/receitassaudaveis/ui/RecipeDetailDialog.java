package br.com.receitassaudaveis.ui;

import br.com.receitassaudaveis.model.Recipe;
import br.com.receitassaudaveis.model.RecipeIngredient;
import br.com.receitassaudaveis.service.RecipeService;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import java.sql.SQLException;
import java.util.List;

/** Janela de detalhes da receita, incluindo ajuste proporcional das quantidades. */
public class RecipeDetailDialog extends JDialog {

    private final Recipe recipe;
    private final List<RecipeIngredient> ingredients;
    private final JTextArea ingredientsArea = new JTextArea();
    private final JSpinner servingsSpinner;

    public RecipeDetailDialog(java.awt.Frame owner, Recipe recipe, RecipeService service) throws SQLException {
        super(owner, "Detalhes da receita", true);
        this.recipe = recipe;
        this.ingredients = service.getIngredients(recipe.getId());
        List<String> restrictions = service.getRestrictionsForRecipe(recipe.getId());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(680, 650);
        setMinimumSize(new Dimension(560, 520));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(12, 12));

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 14, 18));

        JLabel title = new JLabel(recipe.getTitle());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 23f));
        content.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        JLabel description = new JLabel("<html><div style='width:590px'>" + escapeHtml(recipe.getDescription())
                + "<br><b>Categoria:</b> " + escapeHtml(recipe.getCategory())
                + " &nbsp; | &nbsp; <b>Tempo:</b> " + recipe.getPrepMinutes() + " minutos"
                + "<br><b>Classificações:</b> "
                + (restrictions.isEmpty() ? "Não informadas" : escapeHtml(String.join(", ", restrictions)))
                + "</div></html>");
        center.add(description, BorderLayout.NORTH);

        JPanel ingredientsPanel = new JPanel(new BorderLayout(6, 6));
        JPanel ingredientHeading = new JPanel(new BorderLayout());
        ingredientHeading.add(new JLabel("Ingredientes"), BorderLayout.WEST);
        JPanel servingsPanel = new JPanel();
        servingsPanel.add(new JLabel("Porções:"));
        servingsSpinner = new JSpinner(new SpinnerNumberModel(recipe.getBaseServings(), 1, 30, 1));
        servingsPanel.add(servingsSpinner);
        ingredientHeading.add(servingsPanel, BorderLayout.EAST);
        ingredientsPanel.add(ingredientHeading, BorderLayout.NORTH);

        ingredientsArea.setEditable(false);
        ingredientsArea.setLineWrap(true);
        ingredientsArea.setWrapStyleWord(true);
        ingredientsArea.setFont(ingredientsArea.getFont().deriveFont(14f));
        ingredientsArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        ingredientsPanel.add(new JScrollPane(ingredientsArea), BorderLayout.CENTER);
        center.add(ingredientsPanel, BorderLayout.CENTER);

        JTextArea instructionsArea = new JTextArea(recipe.getInstructions());
        instructionsArea.setEditable(false);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setFont(instructionsArea.getFont().deriveFont(14f));
        instructionsArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel preparationPanel = new JPanel(new BorderLayout(6, 6));
        preparationPanel.add(new JLabel("Modo de preparo"), BorderLayout.NORTH);
        preparationPanel.add(new JScrollPane(instructionsArea), BorderLayout.CENTER);
        preparationPanel.setPreferredSize(new Dimension(600, 190));

        JPanel lower = new JPanel(new BorderLayout(6, 6));
        lower.add(preparationPanel, BorderLayout.CENTER);
        JLabel disclaimer = new JLabel("<html><div style='width:590px;color:#666'>Atenção: estas receitas são informações gerais e não substituem a orientação de nutricionista ou médico. "
                + "Em caso de alergias, intolerâncias ou condições de saúde, confirme os ingredientes com um profissional.</div></html>");
        lower.add(disclaimer, BorderLayout.SOUTH);
        center.add(lower, BorderLayout.SOUTH);

        content.add(center, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        servingsSpinner.addChangeListener(event -> updateIngredients());
        updateIngredients();
    }

    private void updateIngredients() {
        int servings = (Integer) servingsSpinner.getValue();
        StringBuilder text = new StringBuilder();
        for (RecipeIngredient ingredient : ingredients) {
            text.append("• ")
                    .append(ingredient.formatForServings(servings, recipe.getBaseServings()))
                    .append('\n');
        }
        ingredientsArea.setText(text.toString());
        ingredientsArea.setCaretPosition(0);
    }

    private String escapeHtml(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
