public class Book {
    private String title;
    private String author;
    private String code;

    public Book(String title, String author, String code) {
        this.title = title;
        this.author = author;
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "Código: " + code
                + " | Título: " + title
                + " | Autor: " + author;
    }
}