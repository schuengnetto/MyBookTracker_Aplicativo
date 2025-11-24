package com.bookTracker.model;

/**
 * Enumeração que representa os possíveis estados de leitura de um livro.
 * Este enum é utilizado em várias partes do sistema, incluindo:
 * - Filtragem de livros na tela principal ({@code MainFrame}).
 * - Definição do estado ao cadastrar ou editar um livro.
 * - Salvamento e carregamento de dados.
 * * @author Netto
 */
public enum BookStatus {
    TO_READ ("A Ler"),
    READING ("Lendo"),
    READ ("Lido");

    /**
     * O texto amigável que será exibido para o usuário na interface gráfica (JComboBox, JTable, etc).
     */
    private final String displayName;
	
    /**
     * Construtor privado do Enum.
     * @param displayName O nome legível para exibição.
     */
    BookStatus(String displayName) {
    this.displayName = displayName;
    }
    
    /**
     * Retorna a representação textual do status para exibição na GUI.
     * Este método é fundamental para que componentes como {@code JComboBox} mostrem "Lendo" em vez de "READING".
     * @return O nome amigável do status (ex: "Lendo").
     */
    @Override
    public String toString() {
        return displayName;
    }
}
