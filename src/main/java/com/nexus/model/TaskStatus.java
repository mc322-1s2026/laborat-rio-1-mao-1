package com.nexus.model;

/**
 * Enumeração que representa os possíveis estados de uma tarefa.
 */
public enum TaskStatus {
    /** Tarefa não iniciada */
    TO_DO,
    /** Tarefa em execução */
    IN_PROGRESS,
    /** Tarefa bloqueada */
    BLOCKED,
    /** Tarefa concluída */
    DONE
}