package com.bookTracker.service;

import com.bookTracker.model.Book;
import com.bookTracker.model.BookStatus;
import com.bookTracker.model.Genre;
import com.bookTracker.exception.ValidationException;
import com.bookTracker.persistence.DataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe de Serviço (Controller) responsável pela lógica de negócios da aplicação.
 * 
 * O {@code BookService} atua como um intermediário entre a interface gráfica (GUI)
 * e a camada de persistência (arquivos). Ele mantém o estado atual da aplicação em memória
 * (listas de livros e gêneros) e garante que as regras de validação sejam cumpridas
 * antes de salvar qualquer dado.
 * 
 * * @author Netto
 */
public class BookService {
    /** Lista em memória contendo todos os livros cadastrados. */
    private List<Book> bookList;
    
     /** Lista em memória contendo todos os gêneros cadastrados. */
    private List<Genre> genreList;
    
    /** Gerenciador responsável por ler e escrever nos arquivos físicos (.txt). */
    private DataManager dataManager;

    // Arquivos TXT
    // Nomes dos arquivos de persistência
    private static final String BOOKS_FILE = "books.txt";
    private static final String GENRES_FILE = "genres.txt";

    /**
     * Construtor do serviço.
     * Inicializa o {@code DataManager} apontando para os arquivos corretos e
     * carrega os dados imediatamente para a memória.
     */
    public BookService() {
        // Aponta para os arquivos .txt
        this.dataManager = new DataManager(BOOKS_FILE, GENRES_FILE);
        loadData();
    }

    /**
     * Carrega os dados dos arquivos de texto para as listas em memória.
     * Ordem de Carregamento:
     * 
     * Primeiro carrega os Gêneros.
     * Depois carrega os Livros, passando a lista de gêneros. Isso é necessário
     * para que o {@code DataManager} possa vincular cada livro ao seu objeto {@code Genre} correto
     * através do ID salvo no arquivo.
     */
    private void loadData() {
        // 1. Carrega Gêneros
        this.genreList = dataManager.loadGenres();
        if (this.genreList == null) {
            this.genreList = new ArrayList<>();
        }

        // 2. Carrega Livros (com a referência dos gêneros)
        this.bookList = dataManager.loadBooks(this.genreList);
        if (this.bookList == null) {
            this.bookList = new ArrayList<>();
        }
    }

    /**
     * Persiste o estado atual das listas em memória para os arquivos de texto.
     * Deve ser chamado após qualquer operação de adição, edição ou remoção.
     * 
     * Salva os arquivos .txt
     */
    public void saveData() {
        dataManager.saveGenres(this.genreList);
        dataManager.saveBooks(this.bookList);
    }

    /**
     * Adiciona um novo livro ao sistema.
     * * @param book O objeto livro a ser adicionado.
     * @throws ValidationException Se o título do livro for nulo ou vazio.
     */
    public void addBook(Book book) throws ValidationException {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new ValidationException("O título do livro não pode estar vazio.");
        }
        this.bookList.add(book);
        saveData(); // Persiste a alteração
    }

    /**
     * Adiciona um novo gênero ao sistema.
     * * @param genre O objeto gênero a ser adicionado.
     * @throws ValidationException Se o nome for vazio ou se já existir um gênero com o mesmo nome (case-insensitive).
     */
    public void addGenre(Genre genre) throws ValidationException {
        if (genre.getName() == null || genre.getName().trim().isEmpty()) {
                throw new ValidationException("O nome do gênero não pode estar vazio");
        }
        // Regra de negócio: Não permitir duplicados
        if (this.genreList.stream().anyMatch(g -> g.getName().equalsIgnoreCase(genre.getName()))) {
                throw new ValidationException("Esse gênero já existe");
        }
        this.genreList.add(genre);
        saveData(); // Persiste a alteração
    }

    /**
     * Filtra a lista de livros por um gênero específico.
     * * @param genre O gênero para filtrar. Se for {@code null}, retorna todos os livros.
     * @return Uma nova lista contendo apenas os livros do gênero especificado.
     */
    public List<Book> filterBooksByGenre(Genre genre) {
        if (genre == null) {
            return new ArrayList<>(this.bookList); 
        }
        return this.bookList.stream().filter(book -> book.getGenre() != null && book.getGenre().equals(genre)).collect(Collectors.toList());
    }

    /**
     * Retorna todos os gêneros cadastrados.
     * @return Uma cópia da lista de gêneros.
     */
    public List<Genre> getAllGenres() {
        return new ArrayList<>(this.genreList);
    }

    /**
     * Retorna todos os livros cadastrados.
     * @return Uma cópia da lista de livros.
     */
    public List<Book> getAllBooks() {
        return new ArrayList<>(this.bookList);
    }

    /**
     * Atualiza os dados de um livro existente.
     * O método busca o livro na lista pelo ID. Se encontrado, substitui
     * o objeto antigo pelo novo (que contém as edições) e salva o arquivo.
     * @param updateBook O objeto livro com os dados atualizados (deve ter o mesmo ID do original).
     */
    public void updateBook(Book updateBook) {	    
        if (updateBook == null || updateBook.getId() == null) {
            System.out.println("Erro: Tentativa de atualizar um livro nulo ou sem ID.");
            return;
        }

        int index = -1;
        for (int i = 0; i < bookList.size(); i++) {
            // Busca pelo ID para garantir que estamos alterando o livro certo
            if (bookList.get(i).getId().equals(updateBook.getId())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            bookList.set(index, updateBook);
            saveData(); // Persiste a alteração
            System.out.println("Livro atualizado: " + updateBook.getTitle());
        } else {
            System.out.println("Tentativa de atualizar um livro que não existe na lista (ID: " + updateBook.getId() + ")");
        }
    }

    /**
     * Remove um livro do sistema.
     * @param bookToRemove O livro a ser removido.
     */
    public void deleteBook(Book bookToRemove) {
    if (bookToRemove == null) {
        System.err.println("Erro: Tentativa de remover um livro nulo.");
        return;
    }

    // Remove da lista se o ID corresponder
    boolean removed = this.bookList.removeIf(book -> book.getId().equals(bookToRemove.getId()));

    if (removed) {
        saveData(); // Persiste a alteração - Salva a lista modificada
        System.out.println("Livro removido: " + bookToRemove.getTitle());
    } else {
        System.err.println("Tentativa de remover um livro que não foi encontrado (ID: " + bookToRemove.getId() + ")");
    }
   }
}
