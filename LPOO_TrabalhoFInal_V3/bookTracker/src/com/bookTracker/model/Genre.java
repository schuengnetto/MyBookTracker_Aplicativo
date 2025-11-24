package com.bookTracker.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Representa uma categoria ou gênero literário no sistema (ex: "Ficção", "Romance").
 * Esta classe é utilizada para categorizar os livros e permitir filtros na tela principal.
 * Implementa {@link Serializable} para consistência com o modelo de dados, embora a persistência
 * atual seja feita via arquivos de texto (.txt).
 * * @author Netto
 */
public class Genre implements Serializable {
    // Versionamento da classe para garantir compatibilidade durante a serialização.
    private static final long serialVersionUID = 1L;

    /**
     * Identificador único do gênero.
     * Essencial para vincular um livro a um gênero específico no arquivo de salvamento,
     * permitindo que o nome do gênero seja editado sem perder a referência nos livros.
     */
    private final String id;
    
    /**
     * O nome visível do gênero (ex: "Terror", "Biografia").
     */
    private String name;

    /**
     * Construtor Simples (Criação):
     * Utilizado pela interface gráfica ({@code NewGenre.java}) quando o usuário está
     * cadastrando um novo gênero.
     * Este construtor gera automaticamente um novo ID único via {@code UUID.randomUUID()}.
     * * @param name O nome do novo gênero.
     */
    public Genre(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    /**
     * Construtor Completo (Carregamento):
     * Utilizado pelo {@code DataManager} ao ler o arquivo {@code genres.txt}.
     * Permite recriar o objeto Gênero mantendo seu ID original, garantindo que
     * os livros que referenciam este ID continuem funcionando.
     * @param id O UUID recuperado do arquivo.
     * @param name O nome do gênero.
     */
    public Genre(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Retorna o nome do gênero.
     * Importante: Este método é utilizado pelos componentes visuais
     * do Java Swing (como {@code JComboBox} e {@code JList}) para determinar o que
     * será exibido na tela para o usuário. Sem isso, apareceria o código de memória do objeto.
     * @return O nome do gênero.
     */
    @Override
    public String toString() {
        return this.name;
    }
    
    /**
     * Verifica se dois gêneros são iguais.
     * A comparação é feita baseada estritamente no ID.
     * Isso impede que o sistema crie duplicatas lógicas e permite encontrar
     * o gênero correto dentro de listas, mesmo se o nome for alterado.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Genre genre = (Genre) obj;
        return id.equals(genre.id); // Compara pelo ID, que é único
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
