package erp;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static ResourceBundle messages;

    public static void main(String[] args) throws Exception {
        escolherIdioma(); // Solicita ao usuário que escolha o idioma

        Loja loja;
        try {
            loja = Loja.cadastrarOuEntrarLoja(); // Cadastra ou entra em uma loja existente
            if (loja == null) {
            	System.out.println(messages.getString("store.not.found"));
                return;
            }

            loja.loadAll();  // Carregar dados da loja
            
            if (args.length > 0 && args[0].equals("blockchain")) {
                executarSimulacaoBlockchain();
                return; // Sai após a execução da simulação
            }

            // Carregar funcionários ao iniciar
            Funcionario.carregaFuncionarios(messages);

            // Verificar se é o primeiro uso e cadastrar o gerente, se necessário
            Funcionario.primeiroUso(messages, loja);

            Funcionario usuarioAutenticado = null;

            // Repetir até que o usuário seja autenticado com sucesso
            while (usuarioAutenticado == null) {
                usuarioAutenticado = Funcionario.autenticarFuncionario(messages);
                if (usuarioAutenticado == null) {
                    System.out.println(messages.getString("authFailed")); // Mensagem de falha de autenticação

                    // Solicitar nova tentativa ou sair
                    System.out.println(messages.getString("tryAgainOrExit"));
                    int escolha = scanner.nextInt();
                    scanner.nextLine(); // Consumir nova linha

                    if (escolha == 2) {
                        System.out.println(messages.getString("exiting"));
                        scanner.close();
                        return; // Sai do programa se o usuário escolher sair
                    }
                }
            }

            Estoque estoque = new Estoque(Locale.getDefault()); // Inicializa o estoque com o Locale escolhido

            while (true) {
                exibirMenu(usuarioAutenticado.getCargo(), loja); // Exibir o menu específico com base no cargo e plano
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consumir nova linha

                switch (choice) {
                case 1:
                    if (loja.isFuncionalidadeDisponivel("CadastroProduto")) {
                        estoque.addProduto(scanner);
                        Logger.logOperation(usuarioAutenticado, messages.getString("addProductLog"), messages);
                    } else {
                    	System.out.println(messages.getString("feature.not.available"));
                    }
                    break;
                case 2:
                    if (loja.isFuncionalidadeDisponivel("VisualizacaoProduto")) {
                        estoque.listaProdutos();
                        Logger.logOperation(usuarioAutenticado, messages.getString("listProductsLog"), messages);
                    } else {
                    	System.out.println(messages.getString("feature.not.available"));
                    }
                    break;
                case 3:
                    if (loja.isFuncionalidadeDisponivel("VendaProduto")) {
                        estoque.compraProduto(scanner);
                        Logger.logOperation(usuarioAutenticado, messages.getString("buyProductLog"), messages);
                    } else {
                    	System.out.println(messages.getString("feature.not.available"));
                    }
                    break;
                case 4:
                    if (loja.isFuncionalidadeDisponivel("fazPagamento")) {
                        estoque.fazPagamento(scanner);
                        Logger.logOperation(usuarioAutenticado, messages.getString("makePaymentLog"), messages);
                    } else {
                    	System.out.println(messages.getString("feature.not.available"));
                    }
                    break;
                case 5:
                    if (usuarioAutenticado instanceof Gerente || usuarioAutenticado instanceof Supervisor || usuarioAutenticado instanceof Atendente) {
                        Estoque.listarTitulosEmAberto(); // Adiciona a funcionalidade para listar títulos em aberto
                        Logger.logOperation(usuarioAutenticado, messages.getString("listOpenTitlesLog"), messages);
                    } else {
                    	System.out.println(messages.getString("titles.access.restricted"));
                    }
                    break;
                case 6:
                    if (loja.isFuncionalidadeDisponivel("CadastroCliente")) {
                        System.out.print(messages.getString("novo_cliente"));
                        String nome = scanner.nextLine();
                        System.out.print(messages.getString("telefone_cliente"));
                        String telefone = scanner.nextLine();
                        System.out.print(messages.getString("cliente_email"));
                        String email = scanner.nextLine();
                        Cliente.adicionarCliente(nome, telefone, email);
                        System.out.println(messages.getString("novo_cliente_cadastrado"));
                        Logger.logOperation(usuarioAutenticado, messages.getString("addClientLog"), messages);
                    } else {
                    	System.out.println(messages.getString("feature.not.available"));
                    }
                    break;
                case 7:
                    if (loja.isFuncionalidadeDisponivel("VisualizacaoCliente")) {
                        Cliente.listarClientes();
                        Logger.logOperation(usuarioAutenticado, messages.getString("listClientsLog"), messages);
                    } else {
                    	System.out.println(messages.getString("feature.not.available"));
                    }
                    break;
                case 8:
                    if (loja.isFuncionalidadeDisponivel("CadastroDepartamento")) {
                        estoque.addDepartamento(scanner);
                        Logger.logOperation(usuarioAutenticado, messages.getString("addDepartmentLog"), messages);
                    } else {
                    	System.out.println(messages.getString("feature.not.available"));
                    }
                    break;
                case 9:
                    if (loja.isFuncionalidadeDisponivel("RelatorioVendasPorDepartamento")) {
                        estoque.relatorioVendasPorDepartamento();
                        Logger.logOperation(usuarioAutenticado, messages.getString("reportSalesByDepartmentLog"), messages);
                    } else {
                    	System.out.println(messages.getString("feature.not.available"));
                    }
                    break;
                case 10:
                    if (loja.isFuncionalidadeDisponivel("ListagemFuncionarios")) {
                        Funcionario.listarFuncionarios();
                        Logger.logOperation(usuarioAutenticado, messages.getString("listEmployeesLog"), messages);
                    } else {
                    	System.out.println(messages.getString("feature.not.available"));
                    }
                    break;
                case 11:
                    if (loja.isFuncionalidadeDisponivel("CadastroFuncionario")) {
                        Funcionario.cadastrarFuncionario(usuarioAutenticado, messages); // Passa o usuário autenticado para o método de cadastro
                        Logger.logOperation(usuarioAutenticado, messages.getString("registerEmployeeLog"), messages);
                    } else {
                    	System.out.println(messages.getString("feature.not.available"));
                    }
                    break;
                case 12:
                    if (usuarioAutenticado instanceof Gerente) {
                        ((Gerente) usuarioAutenticado).gerenciarAssinatura(loja, scanner, messages);
                    } else {
                    	System.out.println(messages.getString("subscriptions.access.restricted"));
                    }
                    break;
                case 13:
                    if (usuarioAutenticado instanceof Gerente || usuarioAutenticado instanceof Supervisor) {
                        Funcionario.listarLogsOperacoes(loja);
                        Logger.logOperation(usuarioAutenticado, messages.getString("viewOperationLogs"), messages);
                    } else {
                    	System.out.println(messages.getString("logs.access.restricted"));
                    }
                    break;
                case 14:
                    executarSimulacaoBlockchain();
                    break;
                case 15: // Opção para visualizar avaliações
                    Avaliacao.exibirMenuAvaliacao(); // Chama o novo método para o menu de avaliações
                    break;
                case 16: // Opção para sair
                    Avaliacao.avaliarSistema(); // Chama o método de avaliação antes de sair
                    System.out.println(messages.getString("exiting"));
                    Logger.logOperation(usuarioAutenticado, messages.getString("exitLog"), messages);
                    scanner.close();
                    return; // Sai do sistema
                default:
                    System.out.println(messages.getString("invalidOption"));
                    Logger.logOperation(usuarioAutenticado, messages.getString("invalidOptionLog"), messages);
            }
           }
        } catch (IOException e) {
            System.err.println(messages.getString("systemInitError") + ": " + e.getMessage()); // Mensagem de erro de inicialização
        }
    }

    // Método para definir o idioma
    private static void setLocale(Locale locale) {
        try {
            Locale.setDefault(locale); // Define o Locale global
            messages = ResourceBundle.getBundle("messages", locale);
            System.out.println("Carregando arquivo de propriedades para o locale: " + locale);
        } catch (Exception e) {
            System.out.println("Arquivo para o locale " + locale + " não encontrado. Usando o padrão (pt_BR).");
            messages = ResourceBundle.getBundle("messages", new Locale("pt", "BR"));
            Locale.setDefault(new Locale("pt", "BR")); // Define o Locale global para pt_BR
        }
    }

    // Método para escolher o idioma
    private static void escolherIdioma() {
        while (true) {
            System.out.println("Escolha o idioma: //Elige el idioma: //Choose the language: \n");
            System.out.println("1. Português(Brasileiro)//Portugués(Brasileño)//Portuguese(Brazilian)");
            System.out.println("2. Espanhol(Argentino)//Español(Argentino)//Spanish(Argentinian)");
            System.out.println("3. Inglês(Americano)//Inglés(Americano)//English(American)");
            System.out.print("Digite o número da opção: // Ingresa el número de la opción: //Enter the option number:\n");

            int opcao = scanner.nextInt();
            scanner.nextLine(); // Consumir nova linha

            switch (opcao) {
                case 1:
                    setLocale(new Locale("pt", "BR"));
                    return; // Sai do loop após configuração bem-sucedida
                case 2:
                    setLocale(new Locale("es", "AR"));
                    return; // Sai do loop após configuração bem-sucedida
                case 3:
                    setLocale(new Locale("en", "US"));
                    return; // Sai do loop após configuração bem-sucedida
                default:
                	System.out.println(messages.getString("invalidOption"));
                    break; // Volta ao início do loop
            }
        }
    }

    // Exibe o menu baseado no cargo do funcionário autenticado e no plano da loja
 // Exibe o menu baseado no cargo do funcionário autenticado e no plano da loja
    private static void exibirMenu(String cargo, Loja loja) {
        System.out.println(messages.getString("menuOptions"));

        switch (cargo.toLowerCase()) {
            case "estoquista":
                if (loja.isFuncionalidadeDisponivel("CadastroProduto")) {
                    System.out.println("1. " + messages.getString("addProduct")); // Add Product
                }
                if (loja.isFuncionalidadeDisponivel("CadastroDepartamento")) {
                    System.out.println("8. " + messages.getString("addDepartment")); // Add Department
                }
                System.out.println("16. " + messages.getString("exit"));
                break;

            case "atendente":
                if (loja.isFuncionalidadeDisponivel("CadastroCliente")) {
                    System.out.println("6. " + messages.getString("addClient")); // Add Client
                }
                if (loja.isFuncionalidadeDisponivel("VendaProduto")) {
                    System.out.println("3. " + messages.getString("buyProduct")); // Buy Product
                }
                if (loja.isFuncionalidadeDisponivel("ListarTitulosEmAberto")) {
                    System.out.println("5. " + messages.getString("listOpenTitles")); // List Open Titles
                }
                System.out.println("16. " + messages.getString("exit"));
                break;

            case "supervisor":
                if (loja.isFuncionalidadeDisponivel("CadastroCliente")) {
                    System.out.println("6. " + messages.getString("addClient")); // Add Client
                }
                if (loja.isFuncionalidadeDisponivel("VendaProduto")) {
                    System.out.println("3. " + messages.getString("buyProduct")); // Buy Product
                }
                if (loja.isFuncionalidadeDisponivel("RelatorioVendasPorDepartamento")) {
                    System.out.println("9. " + messages.getString("reportSalesByDepartment")); // Sales Report by Department
                }
                if (loja.isFuncionalidadeDisponivel("listarLogsOperacoes")) {
                    System.out.println("13. " + messages.getString("viewOperationLogs")); // View Operation Logs
                }
                if (loja.isFuncionalidadeDisponivel("ListarTitulosEmAberto")) {
                    System.out.println("5. " + messages.getString("listOpenTitles")); // List Open Titles
                }
                System.out.println("16. " + messages.getString("exit"));
                break;

            case "gerente":
                if (loja.isFuncionalidadeDisponivel("CadastroProduto")) {
                    System.out.println("1. " + messages.getString("addProduct")); // Add Product
                }
                if (loja.isFuncionalidadeDisponivel("VisualizacaoProduto")) {
                    System.out.println("2. " + messages.getString("listProducts")); // List Products
                }
                if (loja.isFuncionalidadeDisponivel("VendaProduto")) {
                    System.out.println("3. " + messages.getString("buyProduct")); // Buy Product
                }
                if (loja.isFuncionalidadeDisponivel("Pagamento")) {
                    System.out.println("4. " + messages.getString("makePayment")); // Make Payment
                }
                if (loja.isFuncionalidadeDisponivel("ListarTitulosEmAberto")) {
                    System.out.println("5. " + messages.getString("listOpenTitles")); // List Open Titles
                }
                if (loja.isFuncionalidadeDisponivel("CadastroCliente")) {
                    System.out.println("6. " + messages.getString("addClient")); // Add Client
                }
                if (loja.isFuncionalidadeDisponivel("VisualizacaoCliente")) {
                    System.out.println("7. " + messages.getString("listClients")); // List Clients
                }
                if (loja.isFuncionalidadeDisponivel("CadastroDepartamento")) {
                    System.out.println("8. " + messages.getString("addDepartment")); // Add Department
                }
                if (loja.isFuncionalidadeDisponivel("RelatorioVendasPorDepartamento")) {
                    System.out.println("9. " + messages.getString("reportSalesByDepartment")); // Sales Report by Department
                }
                if (loja.isFuncionalidadeDisponivel("ListagemFuncionarios")) {
                    System.out.println("10. " + messages.getString("listEmployees")); // List Employees
                }
                if (loja.isFuncionalidadeDisponivel("CadastroFuncionario")) {
                    System.out.println("11. " + messages.getString("addEm")); // Add Employee
                }
                System.out.println("12. " + messages.getString("manageSubscription")); // Manage Subscription
                if (loja.isFuncionalidadeDisponivel("listarLogsOperacoes")) {
                    System.out.println("13. " + messages.getString("viewOperation")); // View Operation Logs
                }
                System.out.println("14. " + messages.getString("blockchain")); // Blockchain
                System.out.println("15. " + messages.getString("reviews")); // View Ratings
                System.out.println("16. " + messages.getString("exit")); // Exit
                break;

            case "deluxe":
                // Exibe todas as funcionalidades disponíveis para o modo deluxe
                System.out.println("1. " + messages.getString("addProduct")); // Add Product
                System.out.println("2. " + messages.getString("listProducts")); // List Products
                System.out.println("3. " + messages.getString("buyProduct")); // Buy Product
                System.out.println("4. " + messages.getString("makePayment")); // Make Payment
                System.out.println("5. " + messages.getString("listOpenTitles")); // List Open Titles
                System.out.println("6. " + messages.getString("addClient")); // Add Client
                System.out.println("7. " + messages.getString("listClients")); // List Clients
                System.out.println("8. " + messages.getString("addDepartment")); // Add Department
                System.out.println("9. " + messages.getString("reportSalesByDepartment")); // Sales Report by Department
                System.out.println("10. " + messages.getString("listEmployees")); // List Employees
                System.out.println("11. " + messages.getString("addEm")); // Add Employee
                System.out.println("12. " + messages.getString("manageSubscription")); // Manage Subscription
                System.out.println("13. " + messages.getString("viewOperation")); // View Operation Logs
                System.out.println("14. " + messages.getString("blockchain")); // Blockchain
                System.out.println("15. " + messages.getString("reviews")); // View Ratings
                System.out.println("16. " + messages.getString("exit")); // Exit
                break;

            default:
                System.out.println(messages.getString("invalidCargo")); // Invalid Position
                break;
        }
    }





    
    // Método para executar a simulação do blockchain
    private static void executarSimulacaoBlockchain() {
    	System.out.println(messages.getString("blockchain.simulation.starting"));
        // Aqui você pode chamar o método principal da sua simulação de blockchain
        SimuladorBlockchain.main(null); // Chama o simulador de blockchain
    }
    
}
