package br.com.receitassaudaveis.model;

/** Representa uma receita exibida no catálogo. */
public class Recipe {

    private final int id;
    private final String title;
    private final String description;
    private final String category;
    private final int prepMinutes;
    private final String instructions;
    private final int baseServings;
    private final boolean favorite;

    public Recipe(int id, String title, String description, String category,
                  int prepMinutes, String instructions, int baseServings, boolean favorite) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.prepMinutes = prepMinutes;
        this.instructions = instructions;
        this.baseServings = baseServings;
        this.favorite = favorite;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public int getPrepMinutes() { return prepMinutes; }
    public String getInstructions() { return instructions; }
    public int getBaseServings() { return baseServings; }
    public boolean isFavorite() { return favorite; }
}
