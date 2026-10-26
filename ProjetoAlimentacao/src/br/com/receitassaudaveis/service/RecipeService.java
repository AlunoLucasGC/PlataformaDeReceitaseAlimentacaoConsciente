package br.com.receitassaudaveis.service;

import br.com.receitassaudaveis.dao.FavoriteDAO;
import br.com.receitassaudaveis.dao.IngredientDAO;
import br.com.receitassaudaveis.dao.RecipeDAO;
import br.com.receitassaudaveis.dao.RestrictionDAO;
import br.com.receitassaudaveis.model.Recipe;
import br.com.receitassaudaveis.model.RecipeFilter;
import br.com.receitassaudaveis.model.RecipeIngredient;

import java.sql.SQLException;
import java.util.List;

/** Camada de serviço: concentra as operações usadas pela interface. */
public class RecipeService {

    private final RecipeDAO recipeDAO = new RecipeDAO();
    private final IngredientDAO ingredientDAO = new IngredientDAO();
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();
    private final RestrictionDAO restrictionDAO = new RestrictionDAO();

    public List<Recipe> search(RecipeFilter filter, int userId) throws SQLException {
        return recipeDAO.findRecipes(filter, userId);
    }

    public List<RecipeIngredient> getIngredients(int recipeId) throws SQLException {
        return ingredientDAO.findByRecipeId(recipeId);
    }

    public List<String> getRestrictionsForRecipe(int recipeId) throws SQLException {
        return restrictionDAO.findByRecipeId(recipeId);
    }

    public List<String> getCategories() throws SQLException {
        return recipeDAO.findCategories();
    }

    public List<String> getRestrictions() throws SQLException {
        return recipeDAO.findRestrictions();
    }

    public boolean toggleFavorite(int userId, int recipeId) throws SQLException {
        return favoriteDAO.toggleFavorite(userId, recipeId);
    }
}
