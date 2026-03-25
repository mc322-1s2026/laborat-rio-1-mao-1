package com.nexus.exception;

/**
 * Exceção de validação do sistema Nexus.
 * 
 * Lançada quando há violação de regras de negócio ou dados inválidos
 * durante operações no sistema (ex: criação de usuário com email inválido,
 * tarefa sem proprietário, etc).
 */
public class NexusValidationException extends RuntimeException {
    /**
     * Cria uma nova exceção de validação com mensagem descritiva.
     * 
     * @param message a mensagem de erro descrevendo o problema
     */
    public NexusValidationException(String message) {
        super(message);
        // Dica para o aluno: Incrementar contador global de erros aqui? 
        // Ou melhor deixar para a Task gerenciar.
    }
}