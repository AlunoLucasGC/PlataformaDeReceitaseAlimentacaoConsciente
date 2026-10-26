package br.com.receitassaudaveis.model;

/** Critérios usados na busca do catálogo. */
public class RecipeFilter {

    private final String search;
    private final String category;
    private final String restriction;
    private final int maxMinutes;
    private final boolean favoritesOnly;

    public RecipeFilter(String search, String category, String restriction,
                        int maxMinutes, boolean favoritesOnly) {
        this.search = search == null ? "" : search.trim();
        this.category = category == null ? "" : category.trim();
        this.restriction = restriction == null ? "" : restriction.trim();
        this.maxMinutes = maxMinutes;
        this.favoritesOnly = favoritesOnly;
    }

    public String getSearch() { return search; }
    public String getCategory() { return category; }
    public String getRestriction() { return restriction; }
    public int getMaxMinutes() { return maxMinutes; }
    public boolean isFavoritesOnly() { return favoritesOnly; }
}
