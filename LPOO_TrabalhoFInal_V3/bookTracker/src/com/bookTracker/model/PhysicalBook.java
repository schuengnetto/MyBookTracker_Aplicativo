package com.bookTracker.model;

import java.util.List;

/**
 * Representa um livro físico (impresso/papel) no sistema.
 * Esta classe é uma implementação concreta da classe abstrata {@link Book}.
 * Ela serve para diferenciar livros físicos de digitais ({@link Ebook}), permitindo
 * futuras expansões específicas para livros físicos (ex: tipo de capa, peso, localização na estante).
 * * @author Netto
 */
public class PhysicalBook extends Book {

    /**
     * onstrutor Completo (Carregamento):
     * Utilizado pelo {@code DataManager} ao ler o arquivo de texto e reconstruir
     * um objeto de livro físico já existente.
     * Recebe todos os dados históricos do livro (ID, notas, citações, status, etc.)
     * e os repassa para o construtor da superclasse {@code Book}.
     * @param id O UUID original do livro.
     * @param title Título do livro.
     * @param author Autor do livro.
     * @param totalPages Número total de páginas.
     * @param publisher Editora.
     * @param description Descrição.
     * @param genre Gênero associado.
     * @param status Status de leitura.
     * @param rating Nota de avaliação. [AINDA NÃO UTILIZADO]
     * @param currentPage Página atual.
     * @param notes Lista de anotações. [AINDA NÃO UTILIZADO]
     * @param quotes Lista de citações.
     */
	public PhysicalBook(String id, String title, String author, int totalPages, String publisher, String description, Genre genre,
			BookStatus status, int rating, int currentPage, List<String> notes, List<String> quotes) {
		super(id, title, author, totalPages, publisher, description, genre, status, rating, currentPage, notes, quotes);
	}
	
    /**
     * Construtor Simples (Criação):
     * Utilizado pela interface gráfica ({@code NewBook.java}) quando o usuário está
     * cadastrando um novo livro físico.
     * Este construtor recebe apenas os dados essenciais preenchidos no formulário
     * e delega a criação do ID e a definição de valores padrão para a superclasse.
     * @param title Título do livro.
     * @param author Autor do livro.
     * @param totalPages Número total de páginas.
     * @param publisher Editora.
     * @param description Sinopse.
     * @param genre Gênero selecionado.
     */
    public PhysicalBook(String title, String author, int totalPages, String publisher, String description, Genre genre) {
        super(title, author, totalPages, publisher, description, genre);
    }
	
}
