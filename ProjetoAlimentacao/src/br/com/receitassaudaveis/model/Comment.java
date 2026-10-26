package br.com.receitassaudaveis.model;

/** Comentário publicado por um usuário em uma receita. */
public class Comment {

    private final int id;
    private final int userId;
    private final String userName;
    private final String content;
    private final String createdAt;

    public Comment(int id, int userId, String userName, String content, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getContent() { return content; }
    public String getCreatedAt() { return createdAt; }
}
