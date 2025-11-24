package com.bookTracker.exception;

/**
 * Exceção personalizada para representar erros de validação de dados e regras de negócio.
 * Esta classe é usada para diferenciar erros causados por entradas inválidas do usuário
 * (como deixar um campo obrigatório vazio ou inserir um número negativo) de erros 
 * inesperados do sistema (como falha no arquivo ou NullPointerException).
 * * @author Netto
 */
public class ValidationException extends Exception {
    
        /**
         * Construtor padrão da exceção de validação.
         * * @param message A mensagem explicativa do erro. Esta mensagem deve ser 
         * clara e amigável, pois será exibida diretamente para o usuário final.
         */
	public ValidationException (String message) {
		super(message);
	}
}
