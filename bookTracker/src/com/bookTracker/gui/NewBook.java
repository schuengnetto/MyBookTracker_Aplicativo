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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Painel de formulário para o cadastro de novos livros no sistema.
 * 
 * Esta classe é responsável por:
 * Coletar dados do usuário via campos de texto e caixas de seleção.
 * Carregar dados dinâmicos (Gêneros) do serviço para preencher os ComboBoxes.
 * Validar as entradas (campos vazios, números inválidos).
 * Aplicar Polimorfismo: Instanciar a classe correta ({@link PhysicalBook} ou {@link Ebook})
 * baseada na escolha do usuário no campo "Tipo".
 * * @author Netto
 */
public class NewBook extends javax.swing.JPanel {

    /** Serviço para realizar operações de persistência e busca de gêneros. */
    private BookService bookService;
    
    /**
     * Construtor do painel de Novo Livro.
     * Inicializa os componentes visuais, configura o comportamento das caixas de texto
     * e carrega os dados necessários para os menus suspensos.      
     * * @param bookService A instância do BookService (vinda do MainFrame) para salvar o novo livro.
     */
    public NewBook(BookService bookService) {
        initComponents();
        this.bookService = bookService;
        
        // Esconde o "IDAqui" (só deve ser visível na tela de detalhes)
        idBook.setVisible(false);
        
        populateComboBoxes(); // 1. Preenche os menus (Gênero, Status, Tipo)
        addListeners();       // 2. Configura as ações dos botões
    }
    
    /**
     * Popula os componentes JComboBox com dados do sistema.
     * Gêneros: Busca a lista atualizada do {@code bookService}.
     * Status: Carrega os valores do Enum {@link BookStatus}.
     * Tipo:Define opções fixas ("Físico", "Ebook") para controlar a criação do objeto.
     */
    private void populateComboBoxes() {
        // --- Popula Gêneros ---
        // Usamos DefaultComboBoxModel para adicionar os OBJETOS Genre reais, não apenas Strings.
        // Isso permite recuperar o ID do gênero selecionado mais tarde.
        DefaultComboBoxModel<Genre> genreModel = new DefaultComboBoxModel<>();
        genreModel.addElement(null); // Adiciona opção "Vazia" inicial
        try {
             bookService.getAllGenres().forEach(genreModel::addElement);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar gêneros.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
        typesGenre.setModel(genreModel); 
       
        // --- Popula Status ---
        // Usamos DefaultComboBoxModel para adicionar os OBJETOS BookStatus
        DefaultComboBoxModel<BookStatus> statusModel = new DefaultComboBoxModel<>();
        typesStatus.setModel(statusModel);
        
        for (BookStatus status : BookStatus.values()) {
            statusModel.addElement(status);
        }
        
        // --- Popula Tipo de Livro ---
        DefaultComboBoxModel<String> typeModel = new DefaultComboBoxModel<>();
        typeModel.addElement("Físico");
        typeModel.addElement("Ebook");
        typesBook.setModel(typeModel);
    }
    
    /**
     * Centraliza a configuração dos ouvintes de eventos (listeners) dos botões.
     */
    private void addListeners() {
        saveNewBookButton.addActionListener(e -> onSave());
        backHomeButton.addActionListener(e -> onCancel());
    }
    
    /**
     * Lógica principal de salvamento executada ao clicar no botão "Salvar".
     * 
     * Fluxo:
     * Valida campos obrigatórios de texto.
     * Converte e valida campos numéricos (páginas).
     * Verifica se um gênero foi selecionado.
     * Verifica o "Tipo" selecionado para decidir qual construtor chamar ({@code PhysicalBook} ou {@code Ebook}).
     * Preenche os atributos comuns (status, página atual).
     * Envia o objeto criado para o {@code bookService.addBook()}.
     */
    private void onSave() {
        try {
            // 1. Validar e Coletar Dados (Strings)
            String title = inputNameBook.getText().trim();
            String author = inputAuthorBook.getText().trim();
            String publisher = inputPublisherBook.getText().trim();
            String description = jTextArea1.getText().trim();
            
            if (title.isEmpty() || author.isEmpty() || publisher.isEmpty()) {
                throw new ValidationException("Campos com * (asterisco) não podem estar vazios.");
            }
            
            // 2. Validar e Coletar Dados (Números)
            int totalPages;
            int currentPage;
            try {
                totalPages = Integer.parseInt(inputTotalPages.getText().trim());
                currentPage = Integer.parseInt(inputCurrentPage.getText().trim());
            } catch (NumberFormatException e) {
                throw new ValidationException("Os campos de páginas devem conter apenas números.");
            }
            
            if (totalPages <= 0) {
                 throw new ValidationException("Total de Páginas deve ser maior que zero.");
            }
            if (currentPage < 0 || currentPage > totalPages) {
                throw new ValidationException("Página Atual deve estar entre 0 e " + totalPages);
            }
            
            // 3. Validar e Coletar Dados (ComboBoxes)
            Genre selectedGenre = (Genre) typesGenre.getSelectedItem();
            BookStatus selectedStatus = (BookStatus) typesStatus.getSelectedItem();
            String selectedType = (String) typesBook.getSelectedItem();

            if (selectedGenre == null) {
                throw new ValidationException("Por favor, selecione um Gênero. (Cadastre um primeiro se não houver)");
            }
            
            // 4. Criar o Objeto Livro (POLIMORFISMO)
            Book newBook;
            
            if ("Físico".equals(selectedType)) {
                // Cria uma instância de PhysicalBook
                newBook = new PhysicalBook(
                    title, author, totalPages, publisher, description, selectedGenre
                );
            } else if ("Ebook".equals(selectedType)) {
                // Cria uma instância de Ebook
                // "PDF" como local padrão, pois não há campo específico no formulário ainda
                newBook = new Ebook(
                    title, author, totalPages, publisher, description, selectedGenre, "PDF"
                );
            } else {
                // Segurança
                throw new ValidationException("Tipo de livro inválido.");
            }
            
            // 5. Definir os campos que não estão no construtor
            newBook.setCurrentPage(currentPage);
            newBook.setStatus(selectedStatus);
            
            // 6. Salvar no Service
            bookService.addBook(newBook);
            
            // 7. Sucesso
            JOptionPane.showMessageDialog(this, 
                    "Livro \"" + title + "\" salvo com sucesso!", 
                    "Sucesso", 
                    JOptionPane.INFORMATION_MESSAGE);
            
            closeWindow();
            
        } catch (ValidationException | NumberFormatException ex) {
            // 8. Tratar erros de validação
            JOptionPane.showMessageDialog(this, 
                    ex.getMessage(), // Exibe a mensagem amigável definida na ValidationException
                    "Erro de Validação", 
                    JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Ação do botão "Voltar" (Cancelar). Fecha a janela sem salvar.
     */
    private void onCancel() {
        closeWindow();
    }

    /**
     * Helper para encontrar e fechar a janela (JDialog) que contém este painel.
     * Utiliza {@code SwingUtilities.getWindowAncestor} para achar o componente pai.
     */
    private void closeWindow() {
        Window window = SwingUtilities.getWindowAncestor(this);
        window.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        registerBookText = new javax.swing.JLabel();
        iconPlusRegisterBook = new javax.swing.JLabel();
        backHomeButton = new javax.swing.JButton();
        saveNewBookButton = new javax.swing.JButton();
        idBook = new javax.swing.JLabel();
        textInput1 = new javax.swing.JLabel();
        inputNameBook = new javax.swing.JTextField();
        textInput2 = new javax.swing.JLabel();
        inputAuthorBook = new javax.swing.JTextField();
        textInput3 = new javax.swing.JLabel();
        inputPublisherBook = new javax.swing.JTextField();
        textInput4 = new javax.swing.JLabel();
        inputTotalPages = new javax.swing.JTextField();
        textInput5 = new javax.swing.JLabel();
        inputCurrentPage = new javax.swing.JTextField();
        textInput6 = new javax.swing.JLabel();
        typesGenre = new javax.swing.JComboBox<>();
        textInput7 = new javax.swing.JLabel();
        typesStatus = new javax.swing.JComboBox<>();
        textInput8 = new javax.swing.JLabel();
        typesBook = new javax.swing.JComboBox<>();
        textInput9 = new javax.swing.JLabel();
        inputDescriptionBook = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        registerBookText.setFont(new java.awt.Font("Geist", 0, 24)); // NOI18N
        registerBookText.setText("Cadastro Livro");

        iconPlusRegisterBook.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/register.png"))); // NOI18N

        backHomeButton.setText("Voltar");

        saveNewBookButton.setText("Salvar");

        idBook.setFont(new java.awt.Font("Geist", 0, 24)); // NOI18N
        idBook.setText("IDAqui");

        textInput1.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        textInput1.setText("*Nome do Livro:");

        textInput2.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        textInput2.setText("*Autor:");

        textInput3.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        textInput3.setText("*Editora:");

        textInput4.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        textInput4.setText("*Total Páginas");

        textInput5.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        textInput5.setText("*Página Atual:");

        textInput6.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        textInput6.setText("*Genêro");

        textInput7.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        textInput7.setText("*Status");

        textInput8.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        textInput8.setText("*Tipo:");

        textInput9.setFont(new java.awt.Font("Geist", 0, 14)); // NOI18N
        textInput9.setText("*Descrição");

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setWrapStyleWord(true);
        inputDescriptionBook.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(iconPlusRegisterBook)
                                .addGap(12, 12, 12)
                                .addComponent(registerBookText)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(idBook))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(inputNameBook, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                                    .addComponent(textInput1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(inputAuthorBook, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(textInput2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(31, 31, 31)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(saveNewBookButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(backHomeButton))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(inputPublisherBook, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(textInput3, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(inputDescriptionBook, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textInput9, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(inputTotalPages)
                                    .addComponent(textInput4, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(inputCurrentPage)
                                    .addComponent(textInput5, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(textInput6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(typesGenre, 0, 150, Short.MAX_VALUE))
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(textInput7, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(typesStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(textInput8)
                                    .addComponent(typesBook, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(iconPlusRegisterBook)
                    .addComponent(registerBookText)
                    .addComponent(backHomeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveNewBookButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(idBook))
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textInput3)
                    .addComponent(textInput2)
                    .addComponent(textInput1))
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(inputNameBook, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inputAuthorBook, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inputPublisherBook, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textInput5)
                            .addComponent(textInput4)
                            .addComponent(textInput6)
                            .addComponent(textInput7))
                        .addGap(5, 5, 5)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(inputCurrentPage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(inputTotalPages, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(typesGenre, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(typesStatus, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(textInput8)
                        .addGap(5, 5, 5)
                        .addComponent(typesBook, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(30, 30, 30)
                .addComponent(textInput9)
                .addGap(5, 5, 5)
                .addComponent(inputDescriptionBook, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backHomeButton;
    private javax.swing.JLabel iconPlusRegisterBook;
    private javax.swing.JLabel idBook;
    private javax.swing.JTextField inputAuthorBook;
    private javax.swing.JTextField inputCurrentPage;
    private javax.swing.JScrollPane inputDescriptionBook;
    private javax.swing.JTextField inputNameBook;
    private javax.swing.JTextField inputPublisherBook;
    private javax.swing.JTextField inputTotalPages;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel registerBookText;
    private javax.swing.JButton saveNewBookButton;
    private javax.swing.JLabel textInput1;
    private javax.swing.JLabel textInput2;
    private javax.swing.JLabel textInput3;
    private javax.swing.JLabel textInput4;
    private javax.swing.JLabel textInput5;
    private javax.swing.JLabel textInput6;
    private javax.swing.JLabel textInput7;
    private javax.swing.JLabel textInput8;
    private javax.swing.JLabel textInput9;
    private javax.swing.JComboBox<String> typesBook;
    private javax.swing.JComboBox<Genre> typesGenre;
    private javax.swing.JComboBox<BookStatus> typesStatus;
    // End of variables declaration//GEN-END:variables
}
