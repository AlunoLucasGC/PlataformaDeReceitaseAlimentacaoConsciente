package br.com.receitassaudaveis.ui;

import br.com.receitassaudaveis.model.Recipe;
import br.com.receitassaudaveis.model.RecipeIngredient;
import br.com.receitassaudaveis.service.RecipeService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Formulário para cadastrar e editar receitas, ingredientes e restrições. */
public class RecipeEditorDialog extends JDialog {

    private final RecipeService service;
    private final Recipe existing;
    private final JTextField titleField = new JTextField(36);
    private final JTextField descriptionField = new JTextField(36);
    private final JComboBox<String> categoryCombo = new JComboBox<>();
    private final JSpinner prepSpinner = new JSpinner(new SpinnerNumberModel(15, 1, 600, 1));
    private final JSpinner servingsSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
    private final JTextArea instructionsArea = new JTextArea(6, 36);
    private final JTextArea ingredientsArea = new JTextArea(7, 36);
    private final JList<String> restrictionsList;
    private boolean saved;

    public RecipeEditorDialog(java.awt.Frame owner, RecipeService service, Recipe existing) throws SQLException {
        super(owner, existing == null ? "Cadastrar receita" : "Editar receita", true);
        this.service = service;
        this.existing = existing;
        List<String> categories = service.getCategories();
        for (String category : categories) categoryCombo.addItem(category);
        List<String> restrictions = service.getRestrictions();
        restrictionsList = new JList<>(restrictions.toArray(String[]::new));
        restrictionsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (existing != null) populateExisting();
        buildInterface();
    }

    public boolean isSaved() { return saved; }

    private void populateExisting() throws SQLException {
        titleField.setText(existing.getTitle());
        descriptionField.setText(existing.getDescription());
        categoryCombo.setSelectedItem(existing.getCategory());
        prepSpinner.setValue(existing.getPrepMinutes());
        servingsSpinner.setValue(existing.getBaseServings());
        instructionsArea.setText(existing.getInstructions());

        StringBuilder ingredientText = new StringBuilder();
        for (RecipeIngredient ingredient : service.getIngredients(existing.getId())) {
            ingredientText.append(ingredient.getName()).append(" | ")
                    .append(ingredient.getQuantity().stripTrailingZeros().toPlainString()).append(" | ")
                    .append(ingredient.getUnit() == null ? "" : ingredient.getUnit()).append('\n');
        }
        ingredientsArea.setText(ingredientText.toString());
        List<String> currentRestrictions = service.getRestrictionsForRecipe(existing.getId());
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < restrictionsList.getModel().getSize(); i++) {
            if (currentRestrictions.contains(restrictionsList.getModel().getElementAt(i))) selected.add(i);
        }
        restrictionsList.setSelectedIndices(selected.stream().mapToInt(Integer::intValue).toArray());
    }

    private void buildInterface() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(760, 760);
        setMinimumSize(new java.awt.Dimension(680, 650));
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        addRow(form, c, 0, "Nome da receita:", titleField);
        addRow(form, c, 1, "Descrição:", descriptionField);
        addRow(form, c, 2, "Categoria:", categoryCombo);
        addRow(form, c, 3, "Tempo de preparo (minutos):", prepSpinner);
        addRow(form, c, 4, "Porções da receita-base:", servingsSpinner);

        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        ingredientsArea.setLineWrap(true);
        ingredientsArea.setWrapStyleWord(true);
        JPanel instructionsPanel = new JPanel(new BorderLayout(4, 4));
        instructionsPanel.add(new JLabel("Modo de preparo:"), BorderLayout.NORTH);
        instructionsPanel.add(new JScrollPane(instructionsArea), BorderLayout.CENTER);
        JPanel ingredientsPanel = new JPanel(new BorderLayout(4, 4));
        ingredientsPanel.add(new JLabel("Ingredientes (uma linha por ingrediente):"), BorderLayout.NORTH);
        ingredientsPanel.add(new JScrollPane(ingredientsArea), BorderLayout.CENTER);
        JLabel format = new JLabel("Formato: Nome do ingrediente | Quantidade | Unidade   Ex.: Aveia | 6 | colheres de sopa");
        ingredientsPanel.add(format, BorderLayout.SOUTH);
        addFullRow(form, c, 5, instructionsPanel, 135);
        addFullRow(form, c, 6, ingredientsPanel, 165);

        JPanel restrictionPanel = new JPanel(new BorderLayout(4, 4));
        restrictionPanel.setBorder(BorderFactory.createTitledBorder("Restrições / classificações alimentares"));
        restrictionPanel.add(new JScrollPane(restrictionsList), BorderLayout.CENTER);
        addFullRow(form, c, 7, restrictionPanel, 100);

        add(new JScrollPane(form), BorderLayout.CENTER);
        JButton saveButton = new JButton("Salvar receita");
        JButton cancelButton = new JButton("Cancelar");
        saveButton.addActionListener(event -> save());
        cancelButton.addActionListener(event -> dispose());
        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(saveButton);
        add(buttons, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, java.awt.Component component) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(component, c);
    }

    private void addFullRow(JPanel panel, GridBagConstraints c, int row,
            java.awt.Component component, int height) {
        c.gridy = row;
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0;
        component.setPreferredSize(new java.awt.Dimension(680, height));
        panel.add(component, c);
        c.gridwidth = 1;
    }

    private void save() {
        try {
            List<RecipeIngredient> ingredients = parseIngredients();
            List<String> restrictions = restrictionsList.getSelectedValuesList();
            String category = (String) categoryCombo.getSelectedItem();
            if (existing == null) {
                service.createRecipe(titleField.getText(), descriptionField.getText(), category,
                        (Integer) prepSpinner.getValue(), (Integer) servingsSpinner.getValue(),
                        instructionsArea.getText(), ingredients, restrictions);
            } else {
                service.updateRecipe(existing.getId(), titleField.getText(), descriptionField.getText(), category,
                        (Integer) prepSpinner.getValue(), (Integer) servingsSpinner.getValue(),
                        instructionsArea.getText(), ingredients, restrictions);
            }
            saved = true;
            JOptionPane.showMessageDialog(this, "Receita salva com sucesso!",
                    "Cadastro de receita", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Verifique os dados", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Não foi possível salvar a receita.\n\nDetalhe: " + ex.getMessage(),
                    "Erro no banco de dados", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<RecipeIngredient> parseIngredients() {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        String[] lines = ingredientsArea.getText().split("\\R");
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index].trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\|", -1);
            if (parts.length < 3 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
                throw new IllegalArgumentException("Linha " + (index + 1)
                        + " dos ingredientes inválida. Use: Nome | Quantidade | Unidade.");
            }
            try {
                BigDecimal quantity = new BigDecimal(parts[1].trim().replace(',', '.'));
                if (quantity.signum() < 0) throw new NumberFormatException();
                ingredients.add(new RecipeIngredient(parts[0].trim(), quantity, parts[2].trim()));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Quantidade inválida na linha " + (index + 1) + ".");
            }
        }
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Cadastre pelo menos um ingrediente.");
        }
        return ingredients;
    }
}
