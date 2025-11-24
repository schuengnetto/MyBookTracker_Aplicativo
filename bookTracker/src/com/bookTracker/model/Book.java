package com.bookTracker.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Classe base abstrata que representa um Livro genérico no sistema.
 * Conceitos de POO aplicados:
 * Abstração: Esta classe não pode ser instanciada diretamente (ninguém cria apenas um "Livro").
 * Ela serve de modelo para {@link PhysicalBook} e {@link Ebook}.
 * Herança: Contém todos os atributos comuns (título, autor, etc.) que as subclasses herdam.
 * Serialização: Implementa {@link Serializable} para permitir que objetos desta classe (e subclasses)
 * sejam transformados em bytes para salvamento (embora agora estejamos usando .txt, manter Serializable é boa prática).
 * * @author Netto
 */

public abstract class Book implements Serializable {
    // Versionamento da classe para garantir compatibilidade durante a serialização.
    private static final long serialVersionUID = 1L; 

    /**
     * Identificador único universal (UUID). 
     * Essencial para diferenciar livros com mesmo título ou para operações de update/delete.
     */
    private final String id;
    
    private String title;
    private String author;
    private int totalPages;
    private int currentPage;
    private String publisher;
    private Genre genre; 
    
    /**
     * Descrição ou sinopse do livro. Pode conter texto longo.
     */
    private String description;
    
    /**
     * Estado atual da leitura (Lendo, Lido, A Ler).
     */
    private BookStatus status;
    
    /**
     * Avaliação do usuário de 0 a 5 estrelas.
     */
    private int rating; 
    
    // Listas para armazenar múltiplos textos associados ao livro.
    private List<String> notes;
    private List<String> quotes;
	
    /**
     * Construtor "Completo" (11 args)
     * Este construtor é utilizado exclusivamente pelo {@code DataManager} quando
     * está lendo os dados do arquivo de texto (backup). Ele permite recriar um objeto
     * {@code Book} com um ID já existente e todas as suas propriedades preenchidas.
     * @param id O UUID recuperado do arquivo (não gera um novo).
     * @param title Título do livro.
     * @param author Autor do livro.
     * @param totalPages Número total de páginas.
     * @param publisher Editora.
     * @param description Descrição.
     * @param genre Objeto Gênero associado.
     * @param status Enum do status de leitura.
     * @param rating Nota atribuída (0-5). [AINDA NÃO UTILIZADA]
     * @param currentPage Página onde o usuário parou.
     * @param notes Lista de anotações pessoais. [AINDA NÃO UTILIZADA]
     * @param quotes Lista de citações favoritas.
     */
    public Book(String id, String title, String author, int totalPages, String publisher, String description, Genre genre, BookStatus status, int rating, int currentPage, List<String> notes, List<String> quotes) {
        super();
        this.id = id; // Usa o ID vindo do arquivo (preservando a identidade)
        this.title = title;
        this.author = author;
        this.totalPages = totalPages;
        this.publisher = publisher;
        this.description = description;
        this.genre = genre;
        this.rating = rating;
        this.currentPage = currentPage;
        // Validação básica para evitar NullPointerException ao carregar dados antigos ou corrompidos
        this.status = (status != null) ? status : BookStatus.TO_READ;
        this.notes = (notes != null) ? notes : new ArrayList<>();
        this.quotes = (quotes != null) ? quotes : new ArrayList<>();
    }
        
    /**
     * Construtor "Simples" (6 args)
     * Utilizado pela interface gráfica ({@code NewBook.java}) quando o usuário está
     * cadastrando um novo livro.
     * 
     * Esse método:
     * Gera um novo ID único automaticamente via {@code UUID.randomUUID()}.
     * Define valores padrão para campos opcionais (Status = A Ler, Página Atual = 0, etc.).
     * 
     * @param title Título do livro.
     * @param author Autor do livro.
     * @param totalPages Número total de páginas.
     * @param publisher Editora.
     * @param description Descrição.
     * @param genre Gênero selecionado no ComboBox.
     */
    public Book(String title, String author, int totalPages, String publisher, String description, Genre genre) {
        super();
        this.id = UUID.randomUUID().toString(); // Gera um NOVO ID do livro
        this.title = title;
        this.author = author;
        this.totalPages = totalPages;
        this.publisher = publisher;
        this.description = description;
        this.genre = genre;
        // Valores padrão para um registro novo
        this.status = BookStatus.TO_READ; 
        this.rating = 0;                  
        this.currentPage = 0;           
        // Listas para Notas [AINDA NÃO UTILIZADA] e citações
        this.notes = new ArrayList<>();   
        this.quotes = new ArrayList<>();  
    }

    // Getters
    public String getId() {
        return id;
    }
	
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getDescription() {
        return description;
    }

    public Genre getGenre() {
        return genre;
    }

    public BookStatus getStatus() {
        return status;
    }

    public int getRating() {
        return rating;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public List<String> getNotes() {
        return notes;
    }

    public List<String> getQuotes() {
        return quotes;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public void setQuotes(List<String> quotes) {
        this.quotes = quotes;
    }

    /**
     * Retorna uma representação textual simplificada do livro.
     * Utilizado principalmente pelos componentes de UI (como JComboBox) 
     * para exibir o nome do livro na lista.
     */
    @Override
    public String toString() {
            return this.title + " / " + this.author + " / Total páginas:" + this.totalPages;
    }
	
    /**
     * Verifica se dois livros são iguais.
     * A igualdade é baseada exclusivamente no ID do livro.
     * Isso garante que, mesmo se editarmos o título ou a página atual, 
     * o sistema ainda reconheça que é o mesmo objeto.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        // Verifica se o objeto comparado é uma instância de Book (ou subclasses)
        if (!(o instanceof Book)) {
            return false;
        }
        Book book = (Book) o;
        return id.equals(book.id);
    }

    /**
     * Gera um código hash único para o livro.
     * Este método é obrigatório sempre que sobrescrevemos o {@code equals}.
     * A regra é: se dois objetos são iguais segundo {@code equals()}, 
     * eles DEVEM ter o mesmo {@code hashCode}. Por isso, usamos o ID aqui também.
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
	
	
	

	
	
	
	
}
