package br.com.receitassaudaveis.service;

import br.com.receitassaudaveis.dao.CommentDAO;
import br.com.receitassaudaveis.dao.FavoriteDAO;
import br.com.receitassaudaveis.dao.IngredientDAO;
import br.com.receitassaudaveis.dao.RecipeDAO;
import br.com.receitassaudaveis.dao.RestrictionDAO;
import br.com.receitassaudaveis.model.Comment;
import br.com.receitassaudaveis.model.Recipe;
import br.com.receitassaudaveis.model.RecipeFilter;
import br.com.receitassaudaveis.model.RecipeIngredient;

import java.sql.SQLException;
import java.util.List;

/** Camada de serviço: concentra as regras e operações usadas pela interface. */
public class RecipeService {

    private final RecipeDAO recipeDAO = new RecipeDAO();
    private final IngredientDAO ingredientDAO = new IngredientDAO();
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();
    private final RestrictionDAO restrictionDAO = new RestrictionDAO();
    private final CommentDAO commentDAO = new CommentDAO();

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

    public int createRecipe(String title, String description, String category, int prepMinutes,
            int servings, String instructions, List<RecipeIngredient> ingredients,
            List<String> restrictions) throws SQLException {
        validateRecipe(title, description, category, prepMinutes, servings, instructions, ingredients);
        return recipeDAO.createRecipe(title, description, category, prepMinutes, servings,
                instructions, ingredients, restrictions);
    }

    public void updateRecipe(int id, String title, String description, String category,
            int prepMinutes, int servings, String instructions,
            List<RecipeIngredient> ingredients, List<String> restrictions) throws SQLException {
        validateRecipe(title, description, category, prepMinutes, servings, instructions, ingredients);
        recipeDAO.updateRecipe(id, title, description, category, prepMinutes, servings,
                instructions, ingredients, restrictions);
    }

    public void deleteRecipe(int id) throws SQLException {
        recipeDAO.deleteRecipe(id);
    }

    public List<Comment> getComments(int recipeId) throws SQLException {
        return commentDAO.findByRecipeId(recipeId);
    }

    public void addComment(int recipeId, int userId, String content) throws SQLException {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Escreva um comentário antes de publicar.");
        }
        if (content.trim().length() > 2000) {
            throw new IllegalArgumentException("O comentário pode ter no máximo 2.000 caracteres.");
        }
        commentDAO.add(recipeId, userId, content);
    }

    public boolean deleteOwnComment(int commentId, int userId) throws SQLException {
        return commentDAO.deleteOwnComment(commentId, userId);
    }

    private void validateRecipe(String title, String description, String category,
            int prepMinutes, int servings, String instructions,
            List<RecipeIngredient> ingredients) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Informe o nome da receita.");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Informe uma descrição.");
        if (category == null || category.isBlank()) throw new IllegalArgumentException("Selecione uma categoria.");
        if (prepMinutes < 1) throw new IllegalArgumentException("O tempo de preparo precisa ser maior que zero.");
        if (servings < 1) throw new IllegalArgumentException("Informe pelo menos uma porção.");
        if (instructions == null || instructions.isBlank()) throw new IllegalArgumentException("Informe o modo de preparo.");
        if (ingredients == null || ingredients.isEmpty()) throw new IllegalArgumentException("Cadastre pelo menos um ingrediente.");
    }
}
