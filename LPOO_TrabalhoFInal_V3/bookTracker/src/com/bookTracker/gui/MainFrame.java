/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.bookTracker.gui;

// Backend
import com.bookTracker.model.Book;
import com.bookTracker.model.BookStatus;
import com.bookTracker.model.Genre;
import com.bookTracker.service.BookService;

// Swing/AWT
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JDialog;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Janela Principal (JFrame) da aplicação Book Tracker.
 * Esta classe serve como o ponto de entrada da interface gráfica e o painel de controle central.
 * 
 * Ela é responsável por:
 * Inicializar e manter a conexão com o {@link BookService} (backend).
 * Exibir a lista de todos os livros cadastrados em uma tabela.
 * Fornecer filtros de pesquisa por nome, gênero e status de leitura.
 * Gerenciar a navegação para outras telas (Cadastro de Livro, Cadastro de Gênero, Detalhes).
 * Exibir citações (quotes) de livros selecionados.
 * 
 * * @author Netto
 */
public class MainFrame extends javax.swing.JFrame {
    
    /** 
     * Ícone da aplicação que será exibido na barra de título e na barra de tarefas.
     */
    private java.awt.Image appIcon;
    
    /**
     * Serviço de lógica de negócios.
     * É a ponte entre esta interface gráfica e a persistência de dados (backend).
     */
    private BookService bookService;
    
    /**
     * Lista auxiliar que armazena os livros atualmente visíveis na tabela após a aplicação de filtros.
     * Utilizada para mapear a linha selecionada na tabela visual de volta para o objeto {@link Book} correto.
     */
    private List<Book> currentlyDisplayedBooks;    
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainFrame.class.getName());

    /**
     * Construtor da Janela Principal.
     * 
     * Realiza a configuração inicial completa da aplicação:
     * Inicia os componentes visuais (gerados pelo NetBeans).
     * Carrega e define o ícone da aplicação.
     * Inicializa o serviço de backend ({@code bookService}).
     * Configura o layout das tabelas (colunas, larguras).
     * Popula os filtros (ComboBoxes) com dados do banco.
     * Registra os ouvintes de eventos (botões, cliques na tabela).
     * Carrega a lista inicial de livros.
     */
    public MainFrame() {
        // 1. Inicializa os componentes desenhados no NetBeans
        initComponents();
        
        try {
            // 1. Carrega a imagem do seu pacote de imagens
            ImageIcon icon = new ImageIcon(getClass().getResource("/img/iconBookTracker.png"));
            this.appIcon = icon.getImage();
            
            // 2. Define o ícone da janela principal (MainFrame)
            setIconImage(this.appIcon);
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone da aplicação: " + e);
        }
        
        // 2. Inicializa backend
        this.bookService = new BookService();
        this.currentlyDisplayedBooks = new ArrayList<>();
        
        // 3. Centraliza a janela
        setLocationRelativeTo(null);
        
        // 4. Configura as tabelas (colunas, etc)
        configureTables();
        
        // 5. Carrega os dados nos filtros (ComboBoxes)
        populateFilters();
        
        // 6. Adiciona os "escutadores" de eventos (cliques, seleções)
        addListeners();
        
        // 7. Carrega os livros na tabela pela primeira vez
        refreshBookTable();
    }
    
    /**
     * Configura o modelo e a aparência inicial das tabelas (Livros e Citações).
     * Define os nomes das colunas, torna as células não-editáveis e ajusta
     * a largura preferencial das colunas para melhor visualização.
     */
    private void configureTables() {
        // Define o modelo da tabela de livros (torna células não-editáveis)
        booksTable.setModel(new DefaultTableModel(
            new Object [][] {},
            new String [] {"#ID", "Nome", "Status", "Gênero"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Impede a edição
            }
        });
        
        // Define o modelo da tabela de citações (torna células não-editáveis)
        quotesTable.setModel(new DefaultTableModel(
            new Object [][] {},
            new String [] {"Livro", "Citação"}
        ) {
             @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Impede a edição
            }
        });
        
        // Ajusta o tamanho da coluna de ID
        booksTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        booksTable.getColumnModel().getColumn(0).setMaxWidth(100);
        booksTable.getColumnModel().getColumn(1).setPreferredWidth(250);
    }
    
    /**
     * Busca os dados mais recentes do {@code bookService} para preencher os filtros.
     * Atualiza o ComboBox de Status com os valores do Enum {@link BookStatus} e
     * o ComboBox de Gêneros com a lista atual de {@link Genre}.
     * Deve ser chamado após cadastrar um novo gênero para atualizar a lista.
     */
    private void populateFilters() {
        // --- Popula ComboBox de Status ---
        comboBoxStatus.removeAllItems(); // Limpa itens antigos
        comboBoxStatus.addItem("Todos"); // Opção padrão
        for (BookStatus status : BookStatus.values()) {
            comboBoxStatus.addItem(status.toString());
        }

        // --- Popula ComboBox de Gênero ---
        comboBoxGenre.removeAllItems(); // Limpa itens antigos
        comboBoxGenre.addItem("Todos"); // Opção padrão
        List<Genre> genres = bookService.getAllGenres();
        for (Genre genre : genres) {
            comboBoxGenre.addItem(genre.getName());
        }
    }
    
    /**
     * Centraliza a configuração de todos os Listeners (ouvintes de eventos) da tela.
     * 
     * Configura o que acontece quando:
     * Botões são clicados (Cadastrar, Buscar, Refresh).
     * Opções de filtro são alteradas (ComboBoxes).
     * A tabela de livros é selecionada (clique simples) -> Atualiza citações.
     * A tabela de livros recebe clique duplo -> Abre detalhes.
     */
    private void addListeners() {
        // Botões de Cadastro
        buttonBookRegister.addActionListener(e -> onAddBook());
        buttonGenreRegister.addActionListener(e -> onAddGenre());

        // Ações de Filtro/Busca
        buttonSearchBookBar.addActionListener(e -> refreshBookTable());
        searchBookBar.addActionListener(e -> refreshBookTable()); // Permite "Enter" na busca
        comboBoxGenre.addActionListener(e -> refreshBookTable());
        comboBoxStatus.addActionListener(e -> refreshBookTable());
        
        // Botões de Refresh
        buttonRefreshBooks.addActionListener(e -> refreshBookTable());
        buttonRefreshQuote.addActionListener(e -> refreshQuotesTable(null)); // Re-carrega quotes do livro selecionado

        // Listener de Seleção da Tabela de Livros
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Evita ser chamado duas vezes
                onBookSelected();
            }
        });
        
        // Listener de clique (clique duplo) da Tabela de Livros
        booksTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Verifica se foi um clique duplo
                if (e.getClickCount() == 2) {
                    onBookDoubleClick();
                }
            }
        });
    }
    
    /**
     * Atualiza a tabela de livros aplicando todos os filtros ativos.
     * 
     * Lógica de Filtragem:
     * Obtém todos os livros do serviço.
     * Filtra por Status (se diferente de "Todos").
     * Filtra por Gênero (se diferente de "Todos").
     * Filtra por Termo de Busca (verifica se título ou autor contêm o texto).
     * Atualiza a lista {@code currentlyDisplayedBooks} com o resultado.
     * Limpa e repopula o modelo da tabela visual.
     */
    private void refreshBookTable() {
        // 1. Pega todos os livros do backend
        List<Book> allBooks = bookService.getAllBooks();
        
        // 2. Pega os valores dos filtros
        String selectedStatusStr = (String) comboBoxStatus.getSelectedItem();
        String selectedGenreStr = (String) comboBoxGenre.getSelectedItem();
        String searchTerm = searchBookBar.getText().toLowerCase().trim();
        
        // 3. Filtra a lista de livros em memória
        this.currentlyDisplayedBooks = allBooks.stream()
            .filter(book -> {
                // Filtro de Status
                if (selectedStatusStr != null && !"Todos".equals(selectedStatusStr)) {
                    return book.getStatus() != null && book.getStatus().toString().equals(selectedStatusStr);
                }
                return true; // Passa se for "Todos"
            })
            .filter(book -> {
                // Filtro de Gênero
                if (selectedGenreStr != null && !"Todos".equals(selectedGenreStr)) {
                    return book.getGenre() != null && book.getGenre().getName().equals(selectedGenreStr);
                }
                return true; // Passa se for "Todos"
            })
            .filter(book -> {
                // Filtro de Busca (Título ou Autor)
                if (!searchTerm.isEmpty()) {
                    return (book.getTitle() != null && book.getTitle().toLowerCase().contains(searchTerm)) ||
                           (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(searchTerm));
                }
                return true; // Passa se a busca estiver vazia
            })
            .collect(Collectors.toList()); // Coleta a lista filtrada
        
        // 4. Cria o modelo da tabela
        DefaultTableModel model = (DefaultTableModel) booksTable.getModel();
        model.setRowCount(0); // Limpa a tabela
        
        // 5. Popula o modelo com os dados filtrados
        for (Book book : this.currentlyDisplayedBooks) {
            model.addRow(new Object[]{
                book.getId().substring(0, 8) + "...", // Mostra só parte do ID
                book.getTitle(),
                book.getStatus(),
                (book.getGenre() != null) ? book.getGenre().getName() : "N/A"
            });
        }
        
        // 6. Limpa a tabela de quotes, pois a seleção de livros mudou
        refreshQuotesTable(null);           
    }
    
    /**
     * Ação executada ao selecionar (clique simples) uma linha na tabela de livros.
     * Identifica qual livro foi selecionado e atualiza a tabela de citações para exibir
     * apenas as citações daquele livro.
     */
    private void onBookSelected() {
        int viewRow = booksTable.getSelectedRow();
        if (viewRow == -1) {
            // Nenhuma linha selecionada
            refreshQuotesTable(null);
            return;
        }
        
        // Converte o índice da view para o índice do modelo (importante se houver ordenação)
        int modelRow = booksTable.convertRowIndexToModel(viewRow);
        
        // Pega o objeto Book correspondente da nossa lista filtrada
        Book selectedBook = this.currentlyDisplayedBooks.get(modelRow);
        
        // Popula a tabela de quotes
        refreshQuotesTable(selectedBook);
    }
    
    /**
     * Ação executada ao dar clique duplo em uma linha da tabela de livros.
     * Abre a janela de detalhes ({@link BookDetails}) para visualização e edição.
     */
    private void onBookDoubleClick() {
        int viewRow = booksTable.getSelectedRow();
        if (viewRow == -1) {
            return; // Nenhuma linha selecionada
        }
        
        // Pega o livro selecionado
        int modelRow = booksTable.convertRowIndexToModel(viewRow);
        Book selectedBook = this.currentlyDisplayedBooks.get(modelRow);
        
        // 1. Cria o painel de Detalhes, passando o livro
        BookDetails detailsPanel = new BookDetails(this.bookService, selectedBook);
        
        // 2. Cria o JDialog para hospedar o painel
        JDialog dialog = new JDialog(this, "Detalhes do Livro", true); // true = modal
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(detailsPanel);
        dialog.setIconImage(this.appIcon);
        
        // 3. Ajusta e mostra
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        
        // 4. Após o diálogo fechar (seja por salvar, excluir ou voltar),
        // atualizamos a tabela principal.
        refreshBookTable();
    }
    
    /**
     * Atualiza a tabela de citações com base em um contexto (livro selecionado ou geral).
     * * @param selectedBook O livro cujas citações devem ser exibidas. 
     * Se {@code null}, exibe todas as citações de todos os livros.
     */
    private void refreshQuotesTable(Book selectedBook) {
        DefaultTableModel model = (DefaultTableModel) quotesTable.getModel();
        model.setRowCount(0); // Limpa
        model.setColumnIdentifiers(new String[]{"Livro", "Citação"}); // Define colunas corretas

        if (selectedBook == null) {
            // --- MODO "MOSTRAR TUDO" ---
            subtitleQuote.setText("Todas as Citações");
            List<Book> allBooks = bookService.getAllBooks();
            
            for (Book book : allBooks) {
                if (book.getQuotes() != null && !book.getQuotes().isEmpty()) {
                    for (String quote : book.getQuotes()) {
                        model.addRow(new Object[]{
                            book.getTitle(), // Coluna "Livro"
                            quote            // Coluna "Citação"
                        });
                    }
                }
            }
            
            if (model.getRowCount() == 0) {
                 model.setColumnIdentifiers(new String[]{"Nenhuma citação cadastrada no sistema"});
            }
            
        } else {
            // --- MODO "FILTRADO" ---
            subtitleQuote.setText("Citações de: " + selectedBook.getTitle());
            
            if (selectedBook.getQuotes() != null && !selectedBook.getQuotes().isEmpty()) {
                for (String quote : selectedBook.getQuotes()) {
                    model.addRow(new Object[]{
                        selectedBook.getTitle(), // Coluna "Livro"
                        quote                    // Coluna "Citação"
                    });
                }
            }
            
            if (model.getRowCount() == 0) {
                model.setColumnIdentifiers(new String[]{"Nenhuma citação para este livro"});
            }
        }
    }
    
    /**
     * Abre a janela de cadastro de novo livro ({@link NewBook}).
     */
    private void onAddBook() {
        // 1. Cria o painel que você desenhou
        NewBook bookPanel = new NewBook(this.bookService);
        
        // 2. Cria uma janela de diálogo (JDialog) para "hospedar" o painel
        JDialog dialog = new JDialog(this, "Cadastrar Novo Livro", true); // 'true' = modal
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setIconImage(this.appIcon);
        
        // 3. Coloca o painel dentro do diálogo
        dialog.setContentPane(bookPanel);
        
        // 4. Ajusta o tamanho do diálogo ao tamanho do painel
        dialog.pack();
        
        // 5. Centraliza o diálogo em relação ao MainFrame
        dialog.setLocationRelativeTo(this);
        
        // 6. Mostra o diálogo
        dialog.setVisible(true);
        
        // 7. Após o diálogo fechar, atualize a tabela
        refreshBookTable();
    }
    
    /**
     * Abre a janela de cadastro de novo gênero ({@link NewGenre}).
     */
    private void onAddGenre() {
        // 1. Cria o painel que você desenhou
        NewGenre genrePanel = new NewGenre(this.bookService);
        
        // 2. Cria uma janela de diálogo (JDialog) para "hospedar" o painel
        JDialog dialog = new JDialog(this, "Cadastrar Novo Gênero", true); // 'this' é o MainFrame, 'true' o torna modal
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setIconImage(this.appIcon);
        
        // 3. Coloca o painel dentro do diálogo
        dialog.setContentPane(genrePanel);
        
        // 4. Ajusta o tamanho do diálogo ao tamanho do painel
        dialog.pack();
        
        // 5. Centraliza o diálogo em relação ao MainFrame
        dialog.setLocationRelativeTo(this);
        
        // 6. Mostra o diálogo. O código do MainFrame para aqui até o diálogo ser fechado.
        dialog.setVisible(true);
        
        // 7. Após o diálogo fechar, atualize os filtros
        populateFilters();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        iconMainPage = new javax.swing.JLabel();
        titleMainPage = new javax.swing.JLabel();
        buttonGenreRegister = new javax.swing.JButton();
        buttonBookRegister = new javax.swing.JButton();
        textFilters = new javax.swing.JLabel();
        textFilterGenre = new javax.swing.JLabel();
        comboBoxGenre = new javax.swing.JComboBox<>();
        textFilterStatus = new javax.swing.JLabel();
        comboBoxStatus = new javax.swing.JComboBox<>();
        textFilterSearchBook = new javax.swing.JLabel();
        searchBookBar = new javax.swing.JTextField();
        buttonSearchBookBar = new javax.swing.JButton();
        subtitleBook = new javax.swing.JLabel();
        buttonRefreshBooks = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        booksTable = new javax.swing.JTable();
        subtitleQuote = new javax.swing.JLabel();
        buttonRefreshQuote = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        quotesTable = new javax.swing.JTable();

        jLabel1.setText("jLabel1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Meu Book Tracker");

        iconMainPage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/book.png"))); // NOI18N

        titleMainPage.setFont(new java.awt.Font("Geist", 0, 24)); // NOI18N
        titleMainPage.setText("My Book Tracker");
        titleMainPage.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        buttonGenreRegister.setText("Cadastrar Gênero");

        buttonBookRegister.setText("Cadastrar Livro");

        textFilters.setFont(new java.awt.Font("Geist", 0, 18)); // NOI18N
        textFilters.setText("Filtros");

        textFilterGenre.setFont(new java.awt.Font("Geist", 0, 12)); // NOI18N
        textFilterGenre.setText("Gêneros");

        comboBoxGenre.setBorder(null);

        textFilterStatus.setFont(new java.awt.Font("Geist", 0, 12)); // NOI18N
        textFilterStatus.setText("Status");

        comboBoxStatus.setBorder(null);

        textFilterSearchBook.setFont(new java.awt.Font("Geist", 0, 12)); // NOI18N
        textFilterSearchBook.setText("Buscar Livro");

        searchBookBar.setFont(new java.awt.Font("Geist", 0, 12)); // NOI18N
        searchBookBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 10));
        searchBookBar.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        searchBookBar.setMinimumSize(new java.awt.Dimension(1, 16));
        searchBookBar.setName(""); // NOI18N

        buttonSearchBookBar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/search.png"))); // NOI18N
        buttonSearchBookBar.setBorder(null);

        subtitleBook.setFont(new java.awt.Font("Geist", 0, 18)); // NOI18N
        subtitleBook.setText("Livros");

        buttonRefreshBooks.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/refresh.png"))); // NOI18N
        buttonRefreshBooks.setBorder(null);

        booksTable.setFont(new java.awt.Font("Geist", 0, 12)); // NOI18N
        booksTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "#ID", "Nome", "Status", "Gênero"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(booksTable);

        subtitleQuote.setFont(new java.awt.Font("Geist", 0, 18)); // NOI18N
        subtitleQuote.setText("Citações");

        buttonRefreshQuote.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/refresh.png"))); // NOI18N
        buttonRefreshQuote.setBorder(null);

        quotesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "#ID", "Livro", "Citação"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(quotesTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(textFilters)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(subtitleQuote)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(buttonRefreshQuote, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(textFilterGenre)
                                    .addComponent(comboBoxGenre, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(25, 25, 25)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(textFilterStatus)
                                    .addComponent(comboBoxStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(buttonGenreRegister)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(buttonBookRegister))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(25, 25, 25)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(searchBookBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(textFilterSearchBook, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(buttonSearchBookBar, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(iconMainPage)
                                .addGap(12, 12, 12)
                                .addComponent(titleMainPage, javax.swing.GroupLayout.PREFERRED_SIZE, 643, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 255, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(subtitleBook)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(buttonRefreshBooks, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(30, 30, 30))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(titleMainPage)
                    .addComponent(iconMainPage)
                    .addComponent(buttonGenreRegister, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonBookRegister, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(textFilters)
                        .addGap(0, 0, 0)
                        .addComponent(textFilterGenre))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(textFilterStatus)
                        .addComponent(textFilterSearchBook)))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(searchBookBar, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboBoxStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboBoxGenre, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSearchBookBar, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(subtitleBook)
                    .addComponent(buttonRefreshBooks, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(buttonRefreshQuote, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(subtitleQuote))
                .addGap(10, 10, 10)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(30, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable booksTable;
    private javax.swing.JButton buttonBookRegister;
    private javax.swing.JButton buttonGenreRegister;
    private javax.swing.JButton buttonRefreshBooks;
    private javax.swing.JButton buttonRefreshQuote;
    private javax.swing.JButton buttonSearchBookBar;
    private javax.swing.JComboBox<String> comboBoxGenre;
    private javax.swing.JComboBox<String> comboBoxStatus;
    private javax.swing.JLabel iconMainPage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable quotesTable;
    private javax.swing.JTextField searchBookBar;
    private javax.swing.JLabel subtitleBook;
    private javax.swing.JLabel subtitleQuote;
    private javax.swing.JLabel textFilterGenre;
    private javax.swing.JLabel textFilterSearchBook;
    private javax.swing.JLabel textFilterStatus;
    private javax.swing.JLabel textFilters;
    private javax.swing.JLabel titleMainPage;
    // End of variables declaration//GEN-END:variables
}
