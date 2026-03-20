package com.nexus.service;

import com.nexus.model.*;
import com.nexus.exception.NexusValidationException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

public class LogProcessor {

    public void processLog(String fileName, Workspace workspace, List<User> users) {
        try {
            // Busca o arquivo dentro da pasta de recursos do projeto (target/classes)
            var resource = getClass().getClassLoader().getResourceAsStream(fileName);
            
            if (resource == null) {
                throw new IOException("Arquivo não encontrado no classpath: " + fileName);
            }

            try (java.util.Scanner s = new java.util.Scanner(resource).useDelimiter("\\A")) {
                String content = s.hasNext() ? s.next() : "";
                List<String> lines = List.of(content.split("\\R"));
                
                for (String line : lines) {
                    if (line.isBlank() || line.startsWith("#")) continue;

                    String[] p = line.split(";");
                    String action = p[0];

                    try {
                        executeCommand(action, p, workspace, users, line);
                    } catch (NexusValidationException e) {
                        System.err.println("[ERRO DE REGRAS] Falha no comando '" + line + "': " + e.getMessage());
                    } catch (NumberFormatException e) {
                        System.err.println("[ERRO DE FORMATO] ID ou esforço inválido no comando '" + line + "': " + e.getMessage());
                    } catch (DateTimeParseException e) {
                        System.err.println("[ERRO DE FORMATO] Data inválida no comando '" + line + "': " + e.getMessage());
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.err.println("[ERRO DE FORMATO] Comando incompleto '" + line + "': argumentos faltando");
                    } catch (RuntimeException e) {
                        System.err.println("[ERRO DE FORMATO] Falha no comando '" + line + "': " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ERRO FATAL] " + e.getMessage());
        }
    }

    /**
     * Executa um comando baseado na ação fornecida.
     * 
     * @param action o tipo de comando
     * @param p os parâmetros do comando
     * @param workspace o workspace
     * @param users a lista de usuários
     * @param line a linha original para relatório de erro
     */
    private void executeCommand(String action, String[] p, Workspace workspace, List<User> users, String line) {
        switch (action) {
            case "CREATE_USER" -> processCreateUser(p, users);
            case "CREATE_TASK" -> processCreateTask(p, workspace);
            case "ASSIGN_USER" -> processAssignUser(p, workspace, users);
            case "CHANGE_STATUS" -> processChangeStatus(p, workspace);
            case "REPORT_STATUS" -> processReportStatus(workspace, users);
            default -> System.err.println("[WARN] Ação desconhecida: " + action);
        }
    }

    /**
     * Processa comando CREATE_USER.
     * 
     * @param p parâmetros [username, email]
     * @param users lista de usuários
     */
    private void processCreateUser(String[] p, List<User> users) {
        users.add(new User(p[1], p[2]));
        System.out.println("[LOG] Usuário criado: " + p[1]);
    }

    /**
     * Processa comando CREATE_TASK.
     * 
     * @param p parâmetros [title, deadline, effort]
     * @param workspace o workspace
     */
    private void processCreateTask(String[] p, Workspace workspace) {
        Task t = new Task(p[1], LocalDate.parse(p[2]), Integer.parseInt(p[3]));
        workspace.addTask(t);
        System.out.println("[LOG] Tarefa criada: " + p[1]);
    }

    /**
     * Processa comando ASSIGN_USER.
     * 
     * @param p parâmetros [taskId, username]
     * @param workspace o workspace
     * @param users lista de usuários
     */
    private void processAssignUser(String[] p, Workspace workspace, List<User> users) {
        int taskId = Integer.parseInt(p[1]);
        String username = p[2];

        Task task = workspace.getTasks().stream()
            .filter(t -> t.getId() == taskId)
            .findFirst()
            .orElseThrow(() -> new NexusValidationException("Tarefa não encontrada: " + taskId));

        User owner = users.stream()
            .filter(u -> Objects.equals(u.consultUsername(), username))
            .findFirst()
            .orElseThrow(() -> new NexusValidationException("Usuário não encontrado: " + username));

        task.assignOwner(owner);
        owner.assignTask(task);
        System.out.println("[LOG] Owner atribuído para task " + taskId + ": " + username);
    }

    /**
     * Processa comando CHANGE_STATUS.
     * 
     * @param p parâmetros [taskId, status]
     * @param workspace o workspace
     */
    private void processChangeStatus(String[] p, Workspace workspace) {
        int taskId = Integer.parseInt(p[1]);
        String status = p[2];

        Task task = workspace.getTasks().stream()
            .filter(t -> t.getId() == taskId)
            .findFirst()
            .orElseThrow(() -> new NexusValidationException("Tarefa não encontrada: " + taskId));

        switch(status) {
            case "IN_PROGRESS" -> task.moveToInProgress();
            case "DONE" -> task.markAsDone();
            case "BLOCKED" -> task.setBlocked();
            default -> System.err.println("[WARN] Status desconhecido: " + status);
        }
    }

    /**
     * Processa comando REPORT_STATUS e exibe um relatório completo.
     * 
     * @param workspace o workspace
     * @param users lista de usuários
     */
    private void processReportStatus(Workspace workspace, List<User> users) {
        System.out.println("\nTop Performers:");
        List<User> top = workspace.topPerformers(users);
        for(int i = 0; i < top.size(); i++) {
            User u = top.get(i);
            System.out.printf("%d - %s%n", i+1, u.consultUsername());
        }

        System.out.println("\nOverloaded Users:");
        List<User> over = workspace.overloadedUsers(users);
        for(int i = 0; i < over.size(); i++) {
            User u = over.get(i);
            System.out.printf("%d - %s%n", i+1, u.consultUsername());
        }

        System.out.println("\nProject Health:");
        float health = workspace.projectHealth();
        System.out.printf("%f%n", health);

        System.out.println("\nGlobal Bottlenecks:");
        String bottleneck = workspace.globalBottlenecks();
        System.out.printf("%s%n", bottleneck);
    }
}