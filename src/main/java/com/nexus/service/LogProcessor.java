package com.nexus.service;

import com.nexus.model.*;
import com.nexus.exception.NexusValidationException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

/**
 * Processador de logs que executa operações no sistema Nexus.
 * 
 * Lê arquivos de log e executa comandos para criar usuários, tarefas,
 * atribuir usuários, alterar status e gerar relatórios.
 */
public class LogProcessor {

    /**
     * Processa um arquivo de log e executa os comandos nele definidos.
     * 
     * Comandos suportados:
     * - CREATE_USER;username;email
     * - CREATE_TASK;title;deadline;effort
     * - ASSIGN_USER;taskId;username
     * - CHANGE_STATUS;taskId;status
     * - REPORT_STATUS
     * 
     * @param fileName o nome do arquivo de log a processar
     * @param workspace o workspace para adicionar tarefas
     * @param users a lista de usuários do sistema
     */
    public void processLog(String fileName, Workspace workspace, List<User> users, List<Project> projects) {
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
                        executeCommand(action, p, workspace, users, projects, line);
                    } catch (NexusValidationException e) {
                        System.err.println("[ERRO DE REGRAS] Falha no comando '" + line + "': " + e.getMessage());
                    } catch (NumberFormatException e) {
                        System.err.println("[ERRO DE FORMATO] ID ou esforço inválido no comando '" + line + "': " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        System.err.println("[ERRO DE DADOS] Falha no comando '" + line + "': " + e.getMessage());
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
    private void executeCommand(String action, String[] p, Workspace workspace, List<User> users, List<Project> projects, String line) {
        switch (action) {
            case "CREATE_USER" -> processCreateUser(p, users);
            case "CREATE_TASK" -> processCreateTask(p, workspace, projects);
            case "CREATE_PROJECT" -> processCreateProject(p, projects);
            case "ASSIGN_USER" -> processAssignUser(p, workspace, users);
            case "CHANGE_STATUS" -> processChangeStatus(p, workspace);
            case "REPORT_STATUS" -> processReportStatus(workspace, users, projects);
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
    private void processCreateTask(String[] p, Workspace workspace, List<Project> projects) {
        String taskName = p[1];
        LocalDate deadline = LocalDate.parse(p[2]);
        int effort = Integer.parseInt(p[3]);
        String projectName = p[4];

        Project project = projects.stream()
        .filter(e -> e.consultName().equals(projectName))
        .findFirst()
        .orElseThrow(() -> {
            Task.incrementValidationErrors();
            return new NexusValidationException("Projeto não encontrado no log: " + projectName);
        });

        Task t = new Task(taskName, deadline, effort);

        project.addTask(t);
        workspace.addTask(t);

        System.out.println("[LOG] Tarefa '" + taskName + "' criada e vinculada ao projeto: " + projectName);
    }

    /**
     * Processa comando CREATE_PROJECT.
     * 
     * @param p parâmetros [title, totalBudget]
     * @param projects lista de projetos
     */
    private void processCreateProject(String[] p, List<Project> projects) {
        String title = p[1];
        int totalBudget = Integer.parseInt(p[2]);

        Project project = new Project(title, totalBudget);
        projects.add(project);

        System.out.println("[LOG] Projeto criado: " + title + " (Orçamento: " + totalBudget + "horas)");
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
            .orElseThrow(() -> {
                Task.incrementValidationErrors();
                return new NexusValidationException("Tarefa não encontrada: " + taskId);
            });

        User owner = users.stream()
            .filter(u -> Objects.equals(u.consultUsername(), username))
            .findFirst()
            .orElseThrow(() -> {
                Task.incrementValidationErrors();
                return new NexusValidationException("Usuário não encontrado: " + username);
            });

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
            .orElseThrow(() -> {
                Task.incrementValidationErrors();
                return new NexusValidationException("Tarefa não encontrada: " + taskId);
            });

        switch(status) {
            case "IN_PROGRESS" -> {
                task.moveToInProgress();
                System.out.println("[LOG] Status da tarefa " + taskId + " alterado para IN_PROGRESS");
            }
            case "DONE" -> {
                task.markAsDone();
                System.out.println("[LOG] Status da tarefa " + taskId + " alterado para DONE");
            }
            case "BLOCKED" -> {
                task.setBlocked();
                System.out.println("[LOG] Status da tarefa " + taskId + " alterado para BLOCKED");
            }
            default -> System.err.println("[WARN] Status desconhecido: " + status);
        }
    }

    /**
     * Processa comando REPORT_STATUS e exibe um relatório completo.
     * 
     * @param workspace o workspace
     * @param users lista de usuários
     * @param projects lista de projetos
     */
    private void processReportStatus(Workspace workspace, List<User> users, List<Project> projects) {
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
        for (Project p : projects) {
            float health = workspace.projectHealth(p);
            System.out.printf("- %s: %.2f%%%n", p.consultName(), health * 100);
        }

        System.out.println("\nGlobal Bottlenecks:");
        String bottleneck = workspace.globalBottlenecks();
        System.out.printf("%s%n", bottleneck);
    }
}