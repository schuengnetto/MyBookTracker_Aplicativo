package com.bookTracker.persistence;

import com.bookTracker.model.Book;
import com.bookTracker.model.BookStatus;
import com.bookTracker.model.Ebook;
import com.bookTracker.model.Genre;
import com.bookTracker.model.PhysicalBook;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerencia a persistência de dados da aplicação utilizando arquivos de texto (.txt).
 * Esta classe é responsável por converter os objetos em memória ({@link Book}, {@link Genre})
 * para um formato de texto legível e vice-versa.
 * 
 * Estratégia de Arquivos:
 * genres.txt: Formato simples de linha única (ID ; Nome).
 * books.txt: Formato estruturado com tags (BOOK_START, ID:, TITLE:, etc.) para suportar textos longos e multilinhas.
 * 
 * * @author Netto
 */

public class DataManager {
    private final String booksFilename;
    private final String genresFilename;
    
    /** Separador utilizado apenas no arquivo de gêneros. */
    private static final String SEPARATOR = " ; ";

    // --- TAGS PARA IDENTIFICAÇÃO NO ARQUIVO TXT ---
    // Usamos constantes para evitar erros de digitação e facilitar mudanças futuras no formato.
    // Tags para Gêneros
    private static final String TAG_GENRE = "GENRE: ";

    // Tags para Livros
    private static final String TAG_BOOK_START = "BOOK_START: ";
    private static final String TAG_BOOK_END = "BOOK_END";
    
    // Tags de campos simples (uma linha)
    private static final String TAG_ID = "ID: ";
    private static final String TAG_TITLE = "TITLE: ";
    private static final String TAG_AUTHOR = "AUTHOR: ";
    private static final String TAG_PUBLISHER = "PUBLISHER: ";
    private static final String TAG_TOTAL_PAGES = "TOTAL_PAGES: ";
    private static final String TAG_CURRENT_PAGE = "CURRENT_PAGE: ";
    private static final String TAG_RATING = "RATING: ";
    private static final String TAG_STATUS = "STATUS: ";
    private static final String TAG_GENRE_ID = "GENRE_ID: ";
    private static final String TAG_LOCAL = "LOCAL: "; // Específico de Ebook
    
    // Tags de blocos de texto (multilinhas)
    private static final String TAG_DESCRIPTION_START = "DESCRIPTION_START";
    private static final String TAG_DESCRIPTION_END = "DESCRIPTION_END";
    private static final String TAG_QUOTE_START = "QUOTE_START";
    private static final String TAG_QUOTE_END = "QUOTE_END";
    private static final String TAG_NOTE_START = "NOTE_START";
    private static final String TAG_NOTE_END = "NOTE_END";
	
    /**
     * Construtor do gerenciador de dados.
     * @param booksFilename Caminho/Nome do arquivo onde os livros serão salvos.
     * @param genresFilename Caminho/Nome do arquivo onde os gêneros serão salvos.
     */
    public DataManager(String booksFilename, String genresFilename) {
        this.booksFilename = booksFilename;
        this.genresFilename = genresFilename;
    }
    
    // ========================================================================
    // == MÉTODOS DE GÊNEROS
    // ========================================================================

    /**
     * Carrega a lista de gêneros do arquivo de texto.
     * O formato esperado por linha é: {@code GENRE: <UUID> ; <NOME>}
     * @return Uma lista de objetos {@link Genre}. Se o arquivo não existir, retorna lista vazia.
     */
    public List<Genre> loadGenres() {
        List<Genre> genreList = new ArrayList<>();
        File file = new File(genresFilename);

        if (!file.exists()) {
            return genreList;
        }

        // Usa try-with-resources para garantir que o arquivo seja fechado
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(TAG_GENRE)) {
                    // Remove a tag inicial ("GENRE: ")
                    String data = line.substring(TAG_GENRE.length());
                    // Quebra a linha no separador " ; "
                    String[] parts = data.split(SEPARATOR, -1);
                    if (parts.length == 2) {
                        // parts[0] = ID, parts[1] = Nome
                        // Usa o construtor (id, name)
                        genreList.add(new Genre(parts[0], parts[1])); 
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar gêneros: " + e.getMessage());
        }
        return genreList;
    }

    /**
     * Salva a lista de gêneros no arquivo de texto.
     * Sobrescreve o arquivo existente completamente.
     * @param genreList A lista de gêneros a ser persistida.
     */
    public void saveGenres(List<Genre> genreList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(genresFilename, StandardCharsets.UTF_8))) {
            for (Genre genre : genreList) {
                // Formato: GENRE: id ; nome
                writer.write(TAG_GENRE + genre.getId() + SEPARATOR + genre.getName());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar gêneros: " + e.getMessage());
        }
    }
    
    // ========================================================================
    // == MÉTODOS DE LIVROS
    // ========================================================================

    /**
     * Carrega a lista de livros do arquivo de texto.
     * Este método lê o arquivo linha por linha e usa uma máquina de estados (via flags
     * como {@code readingMode}) para processar blocos de texto multilinhas (descrição, quotes).
     * @param genres A lista de gêneros já carregada. Necessária para vincular o ID do gênero 
     * salvo no arquivo do livro ao objeto {@link Genre} real em memória.
     * @return Uma lista de objetos {@link Book} (podendo conter {@link PhysicalBook} e {@link Ebook}).
     */
    public List<Book> loadBooks(List<Genre> genres) {
        List<Book> bookList = new ArrayList<>();
        File file = new File(booksFilename);
        if (!file.exists()) {
            return bookList;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            // Variáveis temporárias para construir o livro
            BookBuilder builder = null;
            String readingMode = null; // Controla se estamos lendo um bloco de texto (DESCRIPTION, QUOTE, NOTE)
            StringBuilder textBlock = null;

            while ((line = reader.readLine()) != null) {
                
                // 1. Processamento de Blocos de Texto (multilinhas)
                if (readingMode != null) {
                    // Verifica se o bloco terminou
                    if (line.equals(TAG_DESCRIPTION_END)) {
                        builder.description = textBlock.toString();
                        readingMode = null;
                    } else if (line.equals(TAG_QUOTE_END)) {
                        builder.quotes.add(textBlock.toString());
                        readingMode = null;
                    } else if (line.equals(TAG_NOTE_END)) {
                        builder.notes.add(textBlock.toString());
                        readingMode = null;
                    } else {
                        // Se não terminou, adiciona a linha atual ao conteúdo
                        if (textBlock.length() > 0) {
                            textBlock.append("\n"); // Restaura a quebra de linha
                        }
                        textBlock.append(line);
                    }
                    continue; // Passa para a próxima linha do arquivo
                }

                // 2. Processamento de Tags Simples
                if (line.startsWith(TAG_BOOK_START)) {
                    builder = new BookBuilder(); // Inicia um novo livro
                    // Define se é Ebook ou Físico baseado no valor após a tag (ex: BOOK_START: EBOOK)
                    builder.isEbook = line.substring(TAG_BOOK_START.length()).equals("EBOOK");
                } 
                else if (line.startsWith(TAG_ID)) {
                    if (builder != null) builder.id = line.substring(TAG_ID.length());
                }
                else if (line.startsWith(TAG_TITLE)) {
                    if (builder != null) builder.title = line.substring(TAG_TITLE.length());
                }
                else if (line.startsWith(TAG_AUTHOR)) {
                    if (builder != null) builder.author = line.substring(TAG_AUTHOR.length());
                }
                else if (line.startsWith(TAG_PUBLISHER)) {
                    if (builder != null) builder.publisher = line.substring(TAG_PUBLISHER.length());
                }
                else if (line.startsWith(TAG_TOTAL_PAGES)) {
                    if (builder != null) builder.totalPages = Integer.parseInt(line.substring(TAG_TOTAL_PAGES.length()));
                }
                else if (line.startsWith(TAG_CURRENT_PAGE)) {
                    if (builder != null) builder.currentPage = Integer.parseInt(line.substring(TAG_CURRENT_PAGE.length()));
                }
                else if (line.startsWith(TAG_RATING)) {
                    if (builder != null) builder.rating = Integer.parseInt(line.substring(TAG_RATING.length()));
                }
                else if (line.startsWith(TAG_STATUS)) {
                    if (builder != null) builder.status = BookStatus.valueOf(line.substring(TAG_STATUS.length()));
                }
                else if (line.startsWith(TAG_GENRE_ID)) {
                    if (builder != null) {
                        String genreId = line.substring(TAG_GENRE_ID.length());
                        // Busca o objeto Genre na lista fornecida usando o ID
                        builder.genre = genres.stream()
                                            .filter(g -> g.getId().equals(genreId))
                                            .findFirst()
                                            .orElse(null);
                    }
                }
                else if (line.startsWith(TAG_LOCAL)) {
                     if (builder != null) builder.local = line.substring(TAG_LOCAL.length());
                }
                // 3. Detecção de Início de Bloco
                else if (line.equals(TAG_DESCRIPTION_START)) {
                    readingMode = "DESCRIPTION";
                    textBlock = new StringBuilder();
                }
                else if (line.equals(TAG_QUOTE_START)) {
                    readingMode = "QUOTE";
                    textBlock = new StringBuilder();
                }
                else if (line.equals(TAG_NOTE_START)) {
                    readingMode = "NOTE";
                    textBlock = new StringBuilder();
                }
                // 4. Finalização do Livro
                else if (line.equals(TAG_BOOK_END)) {
                    if (builder != null) {
                        bookList.add(builder.build()); // Constrói o objeto final e adiciona à lista
                        builder = null; // Limpa o construtor para o próximo livro
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Erro fatal ao carregar livros: " + e.getMessage());
            e.printStackTrace();
        }
        return bookList;
    }

    /**
     * Salva a lista de livros no arquivo de texto.
     * @param bookList Lista de livros a ser salva.
     */
    public void saveBooks(List<Book> bookList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(booksFilename, StandardCharsets.UTF_8))) {
            for (Book book : bookList) {
                // Identifica o tipo de livro para salvar na tag inicial (Polimofirsmo)
                if (book instanceof Ebook) {
                    writer.write(TAG_BOOK_START + "EBOOK");
                } else {
                    writer.write(TAG_BOOK_START + "PHYSICAL");
                }
                writer.newLine();

                // Salva campos simples
                writer.write(TAG_ID + book.getId()); writer.newLine();
                writer.write(TAG_TITLE + book.getTitle()); writer.newLine();
                writer.write(TAG_AUTHOR + book.getAuthor()); writer.newLine();
                writer.write(TAG_PUBLISHER + book.getPublisher()); writer.newLine();
                writer.write(TAG_TOTAL_PAGES + book.getTotalPages()); writer.newLine();
                writer.write(TAG_CURRENT_PAGE + book.getCurrentPage()); writer.newLine();
                writer.write(TAG_RATING + book.getRating()); writer.newLine();
                writer.write(TAG_STATUS + book.getStatus().name()); writer.newLine();
                // Salva apenas o ID do gênero para manter a integridade referencial
                writer.write(TAG_GENRE_ID + (book.getGenre() != null ? book.getGenre().getId() : "NULL_GENRE_ID")); writer.newLine();

                // Campo específico do Ebook
                if (book instanceof Ebook) {
                    writer.write(TAG_LOCAL + ((Ebook) book).getLocal());
                    writer.newLine();
                }

                // Salva Blocos de Texto (com tags de início e fim)
                writer.write(TAG_DESCRIPTION_START); writer.newLine();
                writer.write(book.getDescription()); writer.newLine(); // Salva o texto exatamente como está
                writer.write(TAG_DESCRIPTION_END); writer.newLine();

                // Bloco de Citações (um por um)
                for (String quote : book.getQuotes()) {
                    writer.write(TAG_QUOTE_START); writer.newLine();
                    writer.write(quote); writer.newLine();
                    writer.write(TAG_QUOTE_END); writer.newLine();
                }

                // Bloco de Notas (um por um)
                for (String note : book.getNotes()) {
                    writer.write(TAG_NOTE_START); writer.newLine();
                    writer.write(note); writer.newLine();
                    writer.write(TAG_NOTE_END); writer.newLine();
                }

                // Tag de Fim
                writer.write(TAG_BOOK_END);
                writer.newLine();
                writer.newLine(); // Linha em branco para separar os livros
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar livros: " + e.getMessage());
        }
    }


    /**
     * Classe auxiliar (Builder) interna para facilitar a construção do objeto Livro
     * durante o processo de leitura, pois os dados chegam linha a linha e fora de ordem.
     */
    private static class BookBuilder {
        boolean isEbook;
        String id, title, author, publisher, local;
        int totalPages, currentPage, rating;
        BookStatus status;
        Genre genre;
        String description = ""; // Garante que não seja nulo
        List<String> notes = new ArrayList<>();
        List<String> quotes = new ArrayList<>();

        /**
         * Finaliza a construção e retorna a instância correta (Ebook ou PhysicalBook).
         */
        Book build() {
            if (isEbook) {
                return new Ebook(id, title, author, totalPages, publisher, description, genre, status, rating, currentPage, notes, quotes, local);
            } else {
                return new PhysicalBook(id, title, author, totalPages, publisher, description, genre, status, rating, currentPage, notes, quotes);
            }
        }
    }
}
