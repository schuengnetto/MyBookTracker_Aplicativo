/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.bookTracker.gui;

// Backend
import com.bookTracker.exception.ValidationException;
import com.bookTracker.model.Book;
import com.bookTracker.model.BookStatus;
import com.bookTracker.model.Ebook;
import com.bookTracker.model.Genre;
import com.bookTracker.model.PhysicalBook;
import com.bookTracker.service.BookService;

// Swing/AWT
import java.awt.Window;
import java.util.Objects;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Painel responsável por exibir e editar os detalhes completos de um livro.
 * 
 * Esta classe é acessada quando o usuário dá um clique duplo em um livro na tabela principal.
 * Ela permite:
 * Visualizar todos os dados (título, autor, páginas, etc).
 * Editar as informações do livro.
 * Mudar o status de leitura e a página atual.
 * Excluir o livro do sistema.
 * Adicionar novas citações (quotes).
 * 
 * * @author Netto
 */
public class BookDetails extends javax.swing.JPanel {
    
    /** Serviço para realizar operações de persistência (salvar, excluir). */
    private BookService bookService;
    
    /** O objeto livro que está sendo visualizado/editado nesta tela. */
    private Book book;

    /**
     * Construtor do painel de detalhes.
     * Inicializa os componentes visuais e carrega os dados do livro nos campos.
     * * @param bookService A instância do serviço de livros (para salvar alterações).
     * @param book O objeto {@link Book} selecionado na tela principal.
     */
    public BookDetails(BookService bookService, Book book) {
        initComponents();
        this.bookService = bookService;
        this.book = book; // Armazena o livro que estamos editando
        
        populateComboBoxes(); // 1. Popula os ComboBoxes (Gênero, Status, Tipo)
        populateFields();     // 2. Preenche os campos da tela com os dados do livro
        populateQuotesTable(); // 3. Preenche a tabela de citações
        addListeners();       // 4. Adiciona os cliques dos botões
    }
    
    /**
     * Preenche todos os campos da interface gráfica com os dados atuais do objeto {@code book}.
     * Este método é chamado no construtor para mostrar o estado atual do livro.
     * Ele converte tipos numéricos para String e seleciona os itens corretos nos ComboBoxes.
     */
    private void populateFields() {
        // Textos
        idBook.setText(book.getId().substring(0, 8) + "..."); // Exibe ID encurtado
        nameBook.setText(book.getTitle()); // Título grande no topo
        
        infoNameBook.setText(book.getTitle());
        infoAuthorBook.setText(book.getAuthor());
        infoPublisherBook.setText(book.getPublisher());
        infoDescriptionBookTable.setText(book.getDescription());
        
        // Conversão de int para String
        // Números
        infoTotalPages.setText(String.valueOf(book.getTotalPages()));
        infoCurrentPage.setText(String.valueOf(book.getCurrentPage()));
        
        // --- Seleção de Gênero no ComboBox ---
        // É necessário percorrer o modelo para encontrar o objeto Genre que seja igual (equals)
        // ao gênero do livro, pois são instâncias diferentes na memória.
        DefaultComboBoxModel<Genre> genreModel = (DefaultComboBoxModel<Genre>) infoTypesGenre.getModel();
        for (int i = 0; i < genreModel.getSize(); i++) {
            if (genreModel.getElementAt(i) != null && genreModel.getElementAt(i).equals(book.getGenre())) {
                infoTypesGenre.setSelectedIndex(i);
                break;
            }
        }
        
        // ComboBox: Status - Seleção de Status
        infoTypesStatus.setSelectedItem(book.getStatus());
        
        // --- Seleção de Tipo (Polimorfismo) ---
        // Verifica a classe real do objeto (instanceof) para definir o tipo na tela.
        if (book instanceof PhysicalBook) {
            infoTypesBook.setSelectedItem("Físico");
        } else if (book instanceof Ebook) {
            infoTypesBook.setSelectedItem("Ebook");
        }
        // O tipo não pode ser alterado após a criação (mudaria a classe do objeto)
        infoTypesBook.setEnabled(false);
    }
    
    /**
     * Carrega os dados do sistema para dentro dos componentes JComboBox.
     * Busca todos os gêneros e status disponíveis.
     */
    private void populateComboBoxes() {
        // --- Popula Gêneros ---
        DefaultComboBoxModel<Genre> genreModel = new DefaultComboBoxModel<>();
        genreModel.addElement(null); // Adiciona opção nula/vazia
        try {
            // Busca gêneros do serviço e adiciona ao modelo
             bookService.getAllGenres().forEach(genreModel::addElement);
        } catch (Exception e) {
             JOptionPane.showMessageDialog(this, "Erro ao carregar gêneros.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
        infoTypesGenre.setModel(genreModel); 
       
        // --- Popula Status ---
        DefaultComboBoxModel<BookStatus> statusModel = new DefaultComboBoxModel<>();
        for (BookStatus status : BookStatus.values()) {
            statusModel.addElement(status);
        }
        infoTypesStatus.setModel(statusModel);
        
        // --- Popula Tipo de Livro ---
        DefaultComboBoxModel<String> typeModel = new DefaultComboBoxModel<>();
        typeModel.addElement("Físico");
        typeModel.addElement("Ebook");
        infoTypesBook.setModel(typeModel);
    }
    
    /**
     * Preenche a tabela de citações (quotes) com a lista armazenada no livro.
     */
    private void populateQuotesTable() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0); // Limpa a tabela antes de adicionar

        if (book != null && book.getQuotes() != null) {
            for (String quote : book.getQuotes()) {
                model.addRow(new Object[]{quote});
            }
        }
    }
    
    /**
     * Centraliza a configuração dos ouvintes de eventos (listeners) dos botões.
     */
    private void addListeners() {
        saveButton.addActionListener(e -> onSave());
        backHomeButton.addActionListener(e -> onCancel());
        trashButton.addActionListener(e -> onDelete());
        newQuoteButton.addActionListener(e -> onNewQuote());
    }
    
    /**
     * Lógica executada ao clicar no botão "Salvar".
     * 
     * Coleta os dados dos campos.
     * Realiza validações (campos vazios, números inválidos).
     * Atualiza o objeto {@code book} com os novos dados.
     * Chama o {@code bookService} para persistir as alterações.
     */
    private void onSave() {
        try {
            // 1. Validar e Coletar Dados (Strings)
            String title = infoNameBook.getText().trim();
            String author = infoAuthorBook.getText().trim();
            String publisher = infoPublisherBook.getText().trim();
            String description = infoDescriptionBookTable.getText().trim();
            
            if (title.isEmpty() || author.isEmpty() || publisher.isEmpty()) {
                throw new ValidationException("Campos com * (asterisco) não podem estar vazios.");
            }
            
            // 2. Validar e Coletar Dados (Números)
            int totalPages = Integer.parseInt(infoTotalPages.getText().trim());
            int currentPage = Integer.parseInt(infoCurrentPage.getText().trim());
            
            if (totalPages <= 0) throw new ValidationException("Total de Páginas deve ser > 0.");
            if (currentPage < 0 || currentPage > totalPages) {
                throw new ValidationException("Página Atual deve estar entre 0 e " + totalPages);
            }
            
            // 3. Validar e Coletar Dados (ComboBoxes)
            Genre selectedGenre = (Genre) infoTypesGenre.getSelectedItem();
            BookStatus selectedStatus = (BookStatus) infoTypesStatus.getSelectedItem();

            if (selectedGenre == null) {
                throw new ValidationException("Por favor, selecione um Gênero.");
            }

            // 4. Atualizar o objeto 'this.book' que já temos
            this.book.setTitle(title);
            this.book.setAuthor(author);
            this.book.setPublisher(publisher);
            this.book.setDescription(description);
            this.book.setTotalPages(totalPages);
            this.book.setCurrentPage(currentPage);
            this.book.setGenre(selectedGenre);
            this.book.setStatus(selectedStatus);
            
            // 5. Salvar no Service
            bookService.updateBook(this.book);
            
            // 6. Sucesso
            JOptionPane.showMessageDialog(this, "Livro atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            closeWindow();

        } catch (ValidationException | NumberFormatException ex) {
            // 7. Tratar erros
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Validação", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Lógica executada ao clicar no botão "Excluir".
     * Solicita confirmação antes de remover o livro definitivamente.
     */
    private void onDelete() {
        // 1. Confirmação
        int result = JOptionPane.showConfirmDialog(
            this,
            "Tem certeza que deseja excluir o livro \"" + book.getTitle() + "\"?",
            "Confirmar Exclusão",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // 2. Excluir
            bookService.deleteBook(this.book);
            JOptionPane.showMessageDialog(this, "Livro excluído com sucesso.", "Excluído", JOptionPane.INFORMATION_MESSAGE);
            closeWindow(); // Fecha a tela de detalhes após excluir
        }
    }
    
    /**
     * Abre a tela de cadastro de nova citação ({@code NewQuote}).
     */
    private void onNewQuote() {
        // 1. Cria o painel NewQuote, passando o service e o livro atual
        NewQuote quotePanel = new NewQuote(this.bookService, this.book);
        
        // 2. Cria o JDialog para hospedar o painel
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Nova Citação", JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(quotePanel);
        
        // 3. Ajusta e mostra
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        
        // 4. Após o diálogo fechar, atualize a tabela de citações nesta tela
        populateQuotesTable();
    }
    
    /**
     * Ação do botão "Voltar" (Cancelar).
     */
    private void onCancel() {
        closeWindow();
    }

    /**
     * Fecha a janela (JDialog) que contém este painel.
     */
    private void closeWindow() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        text1 = new javax.swing.JLabel();
        idBook = new javax.swing.JLabel();
        nameBook = new javax.swing.JLabel();
        trashButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        backHomeButton = new javax.swing.JButton();
        detailText1 = new javax.swing.JLabel();
        infoNameBook = new javax.swing.JTextField();
        detailText2 = new javax.swing.JLabel();
        infoAuthorBook = new javax.swing.JTextField();
        detailText3 = new javax.swing.JLabel();
        infoPublisherBook = new javax.swing.JTextField();
        detailText4 = new javax.swing.JLabel();
        infoTotalPages = new javax.swing.JTextField();
        detailText5 = new javax.swing.JLabel();
        infoCurrentPage = new javax.swing.JTextField();
        detailText6 = new javax.swing.JLabel();
        infoTypesGenre = new javax.swing.JComboBox<>();
        detailText7 = new javax.swing.JLabel();
        infoTypesStatus = new javax.swing.JComboBox<>();
        detailText8 = new javax.swing.JLabel();
        infoTypesBook = new javax.swing.JComboBox<>();
        detailText9 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        infoDescriptionBookTable = new javax.swing.JTextArea();
        detailText10 = new javax.swing.JLabel();
        newQuoteButton = new javax.swing.JButton();
        infoQuotesTable = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        text1.setFont(new java.awt.Font("Geist", 0, 12)); // NOI18N
        text1.setText("Detalhamento do Livro");

        idBook.setFont(new java.awt.Font("Geist", 0, 12)); // NOI18N
        idBook.setText("idAqui");

        nameBook.setFont(new java.awt.Font("Geist", 0, 24)); // NOI18N
        nameBook.setText("nomeDoLivroAqui");

        trashButton.setText("Excluir");

        saveButton.setText("Salvar");

        backHomeButton.setText("Voltar");

        detailText1.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        detailText1.setText("Nome do Livro:");

        detailText2.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        detailText2.setText("Autor:");

        detailText3.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        detailText3.setText("Editora:");

        detailText4.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        detailText4.setText("*Total Páginas");

        detailText5.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        detailText5.setText("*Página Atual:");

        detailText6.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        detailText6.setText("*Genêro");

        detailText7.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        detailText7.setText("*Status");

        detailText8.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        detailText8.setText("*Tipo:");

        detailText9.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        detailText9.setText("Descrição:");

        infoDescriptionBookTable.setColumns(20);
        infoDescriptionBookTable.setLineWrap(true);
        infoDescriptionBookTable.setRows(5);
        infoDescriptionBookTable.setWrapStyleWord(true);
        jScrollPane1.setViewportView(infoDescriptionBookTable);

        detailText10.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        detailText10.setText("Citações:");

        newQuoteButton.setText("Nova Citação");

        jTable1.setFont(new java.awt.Font("Geist", 0, 12)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Citação"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTable1.setColumnSelectionAllowed(true);
        infoQuotesTable.setViewportView(jTable1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(detailText10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(newQuoteButton))
                    .addComponent(detailText9)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(infoTotalPages)
                            .addComponent(detailText4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(infoCurrentPage)
                            .addComponent(detailText5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(detailText6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(infoTypesGenre, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(detailText7, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(infoTypesStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(detailText8)
                            .addComponent(infoTypesBook, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(detailText1)
                            .addComponent(infoNameBook, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(infoAuthorBook, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(detailText2))
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(detailText3)
                            .addComponent(infoPublisherBook)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(text1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(idBook))
                            .addComponent(nameBook, javax.swing.GroupLayout.PREFERRED_SIZE, 429, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(trashButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveButton)
                        .addGap(6, 6, 6)
                        .addComponent(backHomeButton))
                    .addComponent(jScrollPane1)
                    .addComponent(infoQuotesTable))
                .addGap(0, 46, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(idBook)
                            .addComponent(text1))
                        .addGap(0, 0, 0)
                        .addComponent(nameBook))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(backHomeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(trashButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(detailText1)
                    .addComponent(detailText2)
                    .addComponent(detailText3))
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(infoNameBook, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(infoAuthorBook, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(infoPublisherBook, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(detailText5)
                            .addComponent(detailText4)
                            .addComponent(detailText6)
                            .addComponent(detailText7))
                        .addGap(5, 5, 5)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(infoCurrentPage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(infoTotalPages, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(infoTypesGenre, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(infoTypesStatus, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(detailText8)
                        .addGap(5, 5, 5)
                        .addComponent(infoTypesBook, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(31, 31, 31)
                .addComponent(detailText9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(detailText10)
                    .addComponent(newQuoteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addComponent(infoQuotesTable, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(50, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backHomeButton;
    private javax.swing.JLabel detailText1;
    private javax.swing.JLabel detailText10;
    private javax.swing.JLabel detailText2;
    private javax.swing.JLabel detailText3;
    private javax.swing.JLabel detailText4;
    private javax.swing.JLabel detailText5;
    private javax.swing.JLabel detailText6;
    private javax.swing.JLabel detailText7;
    private javax.swing.JLabel detailText8;
    private javax.swing.JLabel detailText9;
    private javax.swing.JLabel idBook;
    private javax.swing.JTextField infoAuthorBook;
    private javax.swing.JTextField infoCurrentPage;
    private javax.swing.JTextArea infoDescriptionBookTable;
    private javax.swing.JTextField infoNameBook;
    private javax.swing.JTextField infoPublisherBook;
    private javax.swing.JScrollPane infoQuotesTable;
    private javax.swing.JTextField infoTotalPages;
    private javax.swing.JComboBox<String> infoTypesBook;
    private javax.swing.JComboBox<Genre> infoTypesGenre;
    private javax.swing.JComboBox<BookStatus> infoTypesStatus;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel nameBook;
    private javax.swing.JButton newQuoteButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel text1;
    private javax.swing.JButton trashButton;
    // End of variables declaration//GEN-END:variables
}
