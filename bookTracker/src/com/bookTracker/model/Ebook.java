package com.bookTracker.model;

import java.util.List;

/**
 * Representa um livro digital (Ebook) no sistema.
 * Esta classe estende a classe base {@link Book}, herdando todas as suas características
 * (título, autor, etc.) e adicionando comportamentos específicos de livros digitais.
 * Diferenciais:
 * - Possui um atributo {@code local} para armazenar onde o arquivo digital está (ex: URL, Caminho no PC, Kindle).
 * - Pode ser instanciado diretamente, ao contrário da classe abstrata {@code Book}.
 * * @author Netto
 */
public class Ebook extends Book {
    /**
     * Localização do arquivo digital ou plataforma onde o ebook está acessível.
     * Ex: "C:/Livros/Java.pdf", "Kindle", "Google Books".
     */
    private String local;

    /**
     * Construtor Completo (Carregamento - 12 argumentos):
     * Utilizado pelo {@code DataManager} ao restaurar os dados do arquivo de texto.
     * Recebe todos os atributos, incluindo o ID original e as listas de citações.
     * @param id O UUID recuperado do arquivo.
     * @param title Título do livro.
     * @param author Autor do livro.
     * @param totalPages Número total de páginas.
     * @param publisher Editora.
     * @param description Sinopse.
     * @param genre Gênero do livro.
     * @param status Status de leitura.
     * @param rating Avaliação (0-5). [AINDA NÃO UTILIZADO]
     * @param currentPage Página atual.
     * @param notes Lista de anotações. [AINDA NÃO UTILIZADO]
     * @param quotes Lista de citações.
     * @param local Localização do arquivo digital.
     */
    public Ebook(String id, String title, String author, int totalPages, String publisher, String description, Genre genre, BookStatus status, int rating, int currentPage, List<String> notes, List<String> quotes, String local) {
        super(id, title, author, totalPages, publisher, description, genre, status, rating, currentPage, notes, quotes);
        this.local = local;
     }
       
   /**
     * Construtor Simples (Criação):
     * Utilizado pela interface gráfica ({@code NewBook.java}) quando o usuário cadastra um novo Ebook.
     * Este construtor delega a criação do ID e valores padrão para a superclasse {@code Book}.
     * @param title Título do livro.
     * @param author Autor do livro.
     * @param totalPages Número total de páginas.
     * @param publisher Editora.
     * @param description Sinopse.
     * @param genre Gênero selecionado.
     * @param local Localização do arquivo digital.
     */
   public Ebook(String title, String author, int totalPages, String publisher, String description, Genre genre, String local) {
       super(title, author, totalPages, publisher, description, genre);
       this.local = local;
   }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }
}
