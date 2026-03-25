package com.nexus;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.nexus.exception.NexusValidationException;
import com.nexus.model.Project;
import com.nexus.model.Task;
import com.nexus.model.User;
import com.nexus.service.LogProcessor;
import com.nexus.service.Workspace;

/**
 * Ponto de entrada para a aplicação Nexus.
 * *
 * Esta classe fornece uma interface simples baseada em console usada no
 * trabalho de laboratório. Gerencia uma coleção de {@link User usuários}
 * e um {@link Workspace} onde as {@link Task tarefas} são armazenadas. As
 * operações são realizadas através de um laço de menu e delegadas a métodos
 * auxiliares.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Workspace workspace = new Workspace();
    private static final List<User> users = new ArrayList<>();
    private static final LogProcessor logProcessor = new LogProcessor();
    private static final List<Project> projects = new ArrayList<>();

    /**
     * Inicia a aplicação e processa comandos do usuário até a terminação.
     *
     * @param args argumentos de linha de comando (ignorados)
     */
    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            displayMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "0" -> {
                    System.out.println("Encerrando Nexus Motor...");
                    running = false;
                }
                case "1" -> addUser();
                case "2" -> addTask();
                case "3" -> addProject();
                case "4" -> assignTaskToUser();
                case "5" -> listUsers();
                case "6" -> listTasks();
                case "7" -> listProjects();
                case "8" -> changeTaskStatus();
                case "9" -> {
                    // New method to dynamic choose log
                    // System.out.print("Digite o número da versão do log para carregar (ex: 1 para log_v1.txt): ");
                    // String logVersion = scanner.nextLine();
                    // String fileName = "log_v" + logVersion + ".txt";
                    // System.out.println("Carregando " + fileName + "...");
                    // logProcessor.processLog(fileName, workspace, users, projects);
                    System.out.println("1. Carregar Log V1 (Básico)\n2. Carregar Log V2 (Desafio)");
                    String logChoice = scanner.nextLine();
                    String file = (logChoice.equals("1")) ? "log_v1.txt" : "log_v2.txt";
                    logProcessor.processLog(file, workspace, users, projects);
                }
                default -> System.out.println("\n[!] Opção inválida.");
            }
        }
    }

    /**
     * Imprime o menu principal na saída padrão.
     * <p>As opções do menu correspondem às escolhas tratadas em
     * {@link #main(String[])}.
     * </p>
     */
    private static void displayMenu() {
        System.out.print("""
            
            ======= NEXUS CORE: MENU =======
            1. Adicionar Usuário
            2. Adicionar Tarefa
            3. Adicionar Projeto
            4. Atribuir Usuário à Tarefa
            5. Listar Usuários
            6. Listar Todas as Tarefas
            7. Listar Todos os Projetos
            8. Mudar Status da Tarefa
            9. Processar Log de Ações
            0. Sair
            Escolha uma opção:\s""");
    }

    /**
     * Solicita ao usuário nome de usuário e email, cria um novo {@link User} e
     * adiciona-o à lista interna. Exceções de validação são relatadas no
     * fluxo de erro.
     */
    private static void addUser() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();

            User newUser = new User(username, email);
            users.add(newUser);
            System.out.println("[OK] Usuário cadastrado.");
        } catch (NexusValidationException e) {
            System.err.println("[ERRO] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("[ERRO] " + e.getMessage());
        }
    }

    /**
     * Coleta detalhes da tarefa do usuário, constrói uma {@link Task} e
     * a acrescenta ao workspace. Erros de parsing de data são informados no
     * stderr.
     */
    private static void addTask() {
        try {
            System.out.print("Título da Tarefa: ");
            String title = scanner.nextLine();
            System.out.print("Prazo (AAAA-MM-DD): ");
            LocalDate deadline = LocalDate.parse(scanner.nextLine());
            System.out.print("Estimated Effort (horas): ");
            int estimatedEffort = Integer.parseInt(scanner.nextLine());
            System.out.print("Nome do Projeto: ");
            String projectTitle = scanner.nextLine();

            Project project = projects.stream()
                .filter(p -> p.consultName().equals(projectTitle))
                .findFirst()
                .orElseThrow(() -> new NexusValidationException("Projeto '" + projectTitle + "' não encontrado."));

            Task newTask = new Task(title, deadline, estimatedEffort);
            project.addTask(newTask);
            workspace.addTask(newTask);

            System.out.println("[OK] Tarefa adicionada ao backlog.");
        } catch (DateTimeParseException e) {
            System.err.println("[ERRO] Formato de data inválido. Use AAAA-MM-DD.");
        } catch (NumberFormatException e) {
            System.err.println("[ERRO] Estimated Effort inválido. Informe um número inteiro.");
        } catch (NexusValidationException e) {
            System.err.println("[ERRO] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("[ERRO] " + e.getMessage());
        }
    }

    /**
     * Solicita ao usuário o nome e orçamento do projeto, cria um novo {@link Project}
     * e o adiciona à lista interna. Exceções de validação são relatadas no
     * fluxo de erro.
     */
    private static void addProject() {
        try {
            System.out.print("Título do Projeto: ");
            String title = scanner.nextLine();
            System.out.print("Total Budget (horas)");
            int totalBudget = Integer.parseInt(scanner.nextLine());

            Project newProject = new Project(title, totalBudget);
            projects.add(newProject);

            System.out.println("[OK] Projeto adicionado");
        } catch (NumberFormatException e) {
            System.err.println("[ERRO] Total Budget inválido. Informe um número inteiro.");
        } catch (NexusValidationException e) {
            System.err.println(" [ERRO] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println(" [ERRO " + e.getMessage());
        }
    }

    /**
     * Solicita ao usuário o ID da tarefa e o username do responsável.
     * Atribui o usuário à tarefa e vice-versa, criando uma associação bidirecional.
     * Exceções de localização e validação são reportadas no stderr.
     */
    private static void assignTaskToUser() {
        try {
            System.out.print("ID da Tarefa: ");
            int taskId = Integer.parseInt(scanner.nextLine());

            Task task = workspace.getTasks().stream()
                    .filter(t -> t.getId() == taskId)
                    .findFirst()
                    .orElseThrow(() -> new NexusValidationException("Tarefa não encontrada: " + taskId));

            System.out.print("Username do responsável: ");
            String username = scanner.nextLine();

            User owner = users.stream()
                    .filter(u -> u.consultUsername().equals(username))
                    .findFirst()
                    .orElseThrow(() -> new NexusValidationException("Usuário não encontrado: " + username));

            task.assignOwner(owner);
            owner.assignTask(task);
            System.out.println("[OK] Usuário atribuído à tarefa.");
        } catch (NumberFormatException e) {
            System.err.println("[ERRO] ID da tarefa inválido.");
        } catch (NexusValidationException e) {
            System.err.println("[ERRO] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("[ERRO] " + e.getMessage());
        }
    }

    /**
     * Solicita ao usuário o ID da tarefa e o novo status desejado.
     * Atualiza o status da {@link Task} através dos métodos apropriados
     * (moveToInProgress, markAsDone, setBlocked). Exceções são reportadas no stderr.
     */
    private static void changeTaskStatus() {
        try {
            System.out.print("ID da Tarefa: ");
            int taskId = Integer.parseInt(scanner.nextLine());

            Task task = workspace.getTasks().stream()
                    .filter(t -> t.getId() == taskId)
                    .findFirst()
                    .orElseThrow(() -> new NexusValidationException("Tarefa não encontrada: " + taskId));

            System.out.print("Novo status (IN_PROGRESS, DONE, BLOCKED): ");
            String newStatus = scanner.nextLine().trim().toUpperCase();

            switch (newStatus) {
                case "IN_PROGRESS" -> task.moveToInProgress();
                case "DONE" -> task.markAsDone();
                case "BLOCKED" -> task.setBlocked();
                default -> throw new NexusValidationException("Status inválido: " + newStatus);
            }

            System.out.println("[OK] Status da tarefa atualizado para " + newStatus + ".");
        } catch (NumberFormatException e) {
            System.err.println("[ERRO] ID da tarefa inválido.");
        } catch (NexusValidationException e) {
            System.err.println("[ERRO] " + e.getMessage());
        }
    }

    /**
     * Exibe todas as tarefas atualmente armazenadas no {@link Workspace} em
     * formato de tabela simples. Se não existirem tarefas, imprime uma mensagem
     * de notificação.
     */
    private static void listTasks() {
        List<Task> tasks = workspace.getTasks();
        if (tasks.isEmpty()) {
            System.out.println("\n[!] Nenhuma tarefa no sistema.");
            System.out.println("Erros de validação: " + Task.getTotalValidationErrors());
            System.out.println("Workload ativo: " + Task.getActiveWorkload());
            System.out.println("Total de tarefas: " + Task.getTotalTasksCreated());
            return;
        }

        String header = "+----+----------------------+-------------+------------+----------------------+--------+";
        System.out.println("\n" + header);
        System.out.printf("| %-2s | %-20s | %-11s | %-10s | %-20s | %-6s |%n", "ID", "TÍTULO", "STATUS", "DEADLINE", "OWNER", "EFFORT");
        System.out.println(header);

        for (Task t : tasks) {
            String owner = t.getOwner() != null ? t.getOwner().consultUsername() : "SEM OWNER";
            System.out.printf("| %-2d | %-20s | %-11s | %-10s | %-20s | %-6d |%n",
                    t.getId(),
                    truncar(t.getTitle(), 20),
                    t.getStatus(),
                    t.getDeadline(),
                    truncar(owner, 20),
                    t.getEstimatedEffort());
        }
        System.out.println(header);
        System.out.println("Erros de validação: " + Task.getTotalValidationErrors());
        System.out.println("Workload ativo: " + Task.getActiveWorkload());
        System.out.println("Total de tarefas: " + Task.getTotalTasksCreated());
    }

    /**
     * Lista todos os usuários na forma de tabela com contagem de tarefas
     * agrupadas por status. Se não houver usuários, imprime mensagem de notificação.
     */
    private static void listUsers() {
        if (users.isEmpty()) {
            System.out.println("\n[!] Nenhum usuário no sistema.");
            return;
        }

        String header = "+----------------------+------------------------+---------+-----------+----------+";
        System.out.println("\n" + header);
        System.out.printf("| %-20s | %-22s | %-7s | %-9s | %-8s |%n", "USERNAME", "EMAIL", "TO DO", "IN PROG", "DONE");
        System.out.println(header);

        for (User user : users) {
            List<Task> tasks = user.getAllTasks();
            long toDoCount = tasks.stream().filter(t -> t.getStatus() == com.nexus.model.TaskStatus.TO_DO).count();
            long inProgressCount = tasks.stream().filter(t -> t.getStatus() == com.nexus.model.TaskStatus.IN_PROGRESS).count();
            long doneCount = tasks.stream().filter(t -> t.getStatus() == com.nexus.model.TaskStatus.DONE).count();

            System.out.printf("| %-20s | %-22s | %-7d | %-9d | %-8d |%n",
                    truncar(user.consultUsername(), 20),
                    truncar(user.consultEmail(), 22),
                    toDoCount,
                    inProgressCount,
                    doneCount);
        }
        System.out.println(header);
    }

    /**
     * Lista todos os projetos em formato de tabela mostrando orçamento
     * (atual e total) e distribuição de tarefas por status. Se não houver
     * projetos, imprime mensagem de notificação.
     */
    private static void listProjects() {
        if (projects.isEmpty()) {
            System.out.println("\n[!] Nenhum projeto no sistema.");
            return;
        }

        String header = "+----------------------+----------------+----------------+---------+-----------+----------+";
        System.out.println("\n" + header);
        System.out.printf("| %-20s | %-14s | %-14s | %-7s | %-9s | %-8s |%n", "NAME", "CURRENT BUDGET", "TOTAL BUDGET", "TO DO", "IN PROG", "DONE");
        System.out.println(header);

        for (Project project : projects) {
            List<Task> tasks = project.getAllTasks();
            
            long toDoCount = tasks.stream().filter(t -> t.getStatus() == com.nexus.model.TaskStatus.TO_DO).count();
            long inProgressCount = tasks.stream().filter(t -> t.getStatus() == com.nexus.model.TaskStatus.IN_PROGRESS).count();
            long doneCount = tasks.stream().filter(t -> t.getStatus() == com.nexus.model.TaskStatus.DONE).count();

            System.out.printf("| %-20s | %-14d | %-14d | %-7d | %-9d | %-8d |%n",
                    truncar(project.consultName(), 20),
                    project.consultCurrentBudget(),
                    project.consultTotalBudget(),
                    toDoCount,
                    inProgressCount,
                    doneCount);
        }
        System.out.println(header);
    }

    /**
     * Trunca uma string para um comprimento máximo, acrescentando reticências
     * se ela for maior que o tamanho especificado.
     *
     * @param str a string a ser truncada (pode ser {@code null})
     * @param tam o comprimento máximo permitido
     * @return uma string possivelmente reduzida; nunca {@code null}
     */
    private static String truncar(String str, int tam) {
        if (str == null) return "";
        return str.length() > tam ? str.substring(0, tam - 3) + "..." : str;
    }
}