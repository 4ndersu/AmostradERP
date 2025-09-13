package erp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Testes {
    
    private static final String RESULTADO_TESTES = "resultado_testes.txt";
    static final String CLIENTES_ARQUIVO = "clientes.enc";
    public static void main(String[] args) {
    	
   //Teste para clientes 	
        try {
            // Limpar o arquivo de resultados antes de começar
            limparArquivoResultados();
            
            // Testes para Logger
            testLogOperation();
            testLogErrorHandling();
            testFormattedTimestamp();
            testGetZoneIdForLocale();

            // Executar os testes - Cliente
            testAdicionarCliente();
            testListarClientes();
            testSalvarClientes();
            testCarregarClientes();
            testAdicionarClienteDuplicado();
            
            // Executar os testes - Departamento
            testAdicionarDepartamento();
            testBuscarDepartamentoPorId();
            testListarDepartamentos();
            testSalvarDepartamentos();
            testCarregarDepartamentos();
            
            // Testes para Produto
            testAdicionarProduto();
            testSalvarProdutos();
            testCarregarProdutos();
            testCriptografia();
            testFromStringProd();
            testFromStringFormatoInvalidoProd();
            
         // Testes para Titulo
            testAdicionarTitulo();
            testSalvarTitulos();
            testCarregarTitulos();
            testCriptografia();
            testFromStringTitulo();
            testFromStringFormatoInvalidoTitulo();
            
            // Testes para Loja
            testAdicionarFuncionario();
            testAdicionarProdutoLoja();
            testAdicionarTituloLoja();
            testSalvarELerLoja();
            testCadastrarOuEntrarLoja();
            testFuncionalidadeDisponivel();
            
            //Teste Funcionario
            testFuncionarioCreation();
            testPasswordEncryptionAndVerification();
            testFuncionarioRegistration();
            testListarFuncionarios();
            
            // Testes para Atendente
            testAtendenteCreation();
            testAtendentePermissions();
            
            // Testes para Estoquista
            testEstoquistaCreation();
            testEstoquistaPermissions();
            
            // Testes para Gerente
            testGerenteCreation();
            testGerentePermissions();
            testGerenciarAssinatura();
            
            // Testes para Supervisor
            testSupervisorCreation();
            testSupervisorPermissions();
            Estoque estoque = new Estoque(Locale.getDefault()); // Inicializa o estoque com o Locale escolhido
            //Executar os testes - Estoque
            testAddProduto(estoque);
            testListaProdutos(estoque);
            testCompraProduto(estoque);
            testFazPagamento(estoque);
            testRelatorioVendasPorDepartamento(estoque);
            
            //Testes blockchain
            testCriarContrato();
            testVisualizarContratos();
            testDesativarContrato();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void limparArquivoResultados() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESULTADO_TESTES, false))) {
            writer.write(""); // Limpa o conteúdo do arquivo
        }
    }
    
    private static void limparTestes() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CLIENTES_ARQUIVO, false))) {
            writer.write(""); // Limpa o conteúdo do arquivo
        } 
    }

    public static void registrarResultado(String resultado) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESULTADO_TESTES, true))) {
            writer.println(resultado);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testAdicionarCliente() {
        try {
            Cliente.adicionarCliente("Teste", "123456789", "teste@example.com");
            if (Cliente.getClientes().size() == 1) {
                registrarResultado("Teste adicionar cliente: PASSED");
            } else {
                registrarResultado("Teste adicionar cliente: FAILED");
            }
        } catch (IOException e) {
            registrarResultado("Teste adicionar cliente: FAILED - " + e.getMessage());
        }
    }

    private static void testListarClientes() {
        String email = "teste@example.com"; // Email do cliente a ser adicionado
        try {
            // Adiciona o cliente para o teste
            Cliente.adicionarCliente("Teste", "123456789", email);
            Cliente.listarClientes(); // Apenas chamando para verificar se não lança exceção
            registrarResultado("Teste listar clientes: PASSED");
        } catch (Exception e) {
            registrarResultado("Teste listar clientes: FAILED - " + e.getMessage());
        } finally {
            // Limpa o cliente após o teste
            Cliente.getClientes().remove(email); // Remove o cliente pelo email
            try {
                limparTestes(); // Limpa o arquivo de clientes, se necessário
            } catch (IOException e) {
                registrarResultado("Erro ao limpar arquivo de clientes: " + e.getMessage());
            }
        }
    }



    private static void testSalvarClientes() {
        try {
            Cliente.adicionarCliente("Teste", "123456789", "teste@example.com");
            Cliente.saveClientes();
            File file = new File(Cliente.CLIENTES_ARQUIVO);
            if (file.exists()) {
                registrarResultado("Teste salvar clientes: PASSED");
            } else {
                registrarResultado("Teste salvar clientes: FAILED - Arquivo não encontrado");
            }
        } catch (IOException e) {
            registrarResultado("Teste salvar clientes: FAILED - " + e.getMessage());
        }
    }

    private static void testCarregarClientes() {
        try {
            Cliente.adicionarCliente("Teste", "123456789", "teste@example.com");
            Cliente.saveClientes();
            Cliente.getClientes().clear(); // Limpa os clientes para simular um novo carregamento
            Cliente.loadClientes();
            if (Cliente.getClientes().size() == 1) {
                registrarResultado("Teste carregar clientes: PASSED");
            } else {
                registrarResultado("Teste carregar clientes: FAILED");
            }
        } catch (IOException e) {
            registrarResultado("Teste carregar clientes: FAILED - " + e.getMessage());
        }
    }

    private static void testAdicionarClienteDuplicado() {
        try {
            Cliente.adicionarCliente("Teste", "123456789", "teste@example.com");
            Cliente.adicionarCliente("Teste", "987654321", "teste@example.com"); // Duplicado
            if (Cliente.getClientes().size() == 1) {
                registrarResultado("Teste adicionar cliente duplicado: PASSED");
            } else {
                registrarResultado("Teste adicionar cliente duplicado: FAILED");
            }
        } catch (IOException e) {
            registrarResultado("Teste adicionar cliente duplicado: FAILED - " + e.getMessage());
        }
    }
    

	private static void testAdicionarDepartamento() {
	    Departamento.addDepartamento("1", "Departamento de Testes");
	    if (Departamento.buscarDepartamentoPorId("1") != null) {
	        registrarResultado("Teste adicionar departamento: PASSED");
	    } else {
	        registrarResultado("Teste adicionar departamento: FAILED");
	    }
	}

	private static void testBuscarDepartamentoPorId() {
	    Departamento departamento = Departamento.buscarDepartamentoPorId("1");
	    if (departamento != null && "Departamento de Testes".equals(departamento.getNome())) {
	        registrarResultado("Teste buscar departamento por ID: PASSED");
	    } else {
	        registrarResultado("Teste buscar departamento por ID: FAILED");
	    }
	}

	private static void testListarDepartamentos() {
	    Collection<Departamento> departamentos = Departamento.listarDepartamentos();
	    if (departamentos.size() > 0) {
	        registrarResultado("Teste listar departamentos: PASSED");
	    } else {
	        registrarResultado("Teste listar departamentos: FAILED");
	    }
	}

	private static void testSalvarDepartamentos() {
	    try {
	        Departamento.saveDepartamentos();
	        // Verificar se o arquivo foi criado
	        if (new java.io.File(Departamento.DEPARTAMENTOS_ARQUIVO).exists()) {
	            registrarResultado("Teste salvar departamentos: PASSED");
	        } else {
	            registrarResultado("Teste salvar departamentos: FAILED - Arquivo não encontrado");
	        }
	    } catch (IOException e) {
	        registrarResultado("Teste salvar departamentos: FAILED - " + e.getMessage());
	    }
	}

	private static void testCarregarDepartamentos() {
	    try {
	        Departamento.carregaDepartamentos();
	        if (Departamento.buscarDepartamentoPorId("1") != null) {
	            registrarResultado("Teste carregar departamentos: PASSED");
	        } else {
	            registrarResultado("Teste carregar departamentos: FAILED");
	        }
	    } catch (IOException e) {
	        registrarResultado("Teste carregar departamentos: FAILED - " + e.getMessage());
	    }
	}


	private static void testAddProduto(Estoque estoque) {
	    // Simula a entrada antes de criar o Scanner
	    System.setIn(new java.io.ByteArrayInputStream("1\nProduto Teste\n100.0\n1\n".getBytes()));

	    // Agora crie o Scanner com o System.in modificado
	    try (Scanner scanner = new Scanner(System.in)) {
	        estoque.addProduto(scanner);

	        // Verifica se o produto foi adicionado corretamente
	        if (estoque.getProdutosMap().containsKey("1")) {
	            registrarResultado("Teste adicionar produto: PASSED");
	        } else {
	            registrarResultado("Teste adicionar produto: FAILED");
	        }
	    } catch (IOException e) {
	        registrarResultado("Teste adicionar produto: FAILED - " + e.getMessage());
	    }
	}


	private static void testListaProdutos(Estoque estoque) {
	    // Redirecionar a saída do System.out para capturar o que for impresso no console
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    PrintStream originalOut = System.out;
	    System.setOut(new PrintStream(outputStream));

	    try {
	        // Chama o método que lista os produtos
	        estoque.listaProdutos();
	        
	        // Captura a saída e converte para String
	        String output = outputStream.toString();

	        // Verifica se a saída contém algum produto esperado ou uma linha específica
	        if (output.contains("Produto")) { // Supondo que "Produto" seja uma palavra que sempre aparece ao listar produtos
	            registrarResultado("Teste listar produtos: PASSED");
	        } else {
	            registrarResultado("Teste listar produtos: FAILED");
	        }
	    } finally {
	        // Restaura o System.out para o console padrão
	        System.setOut(originalOut);
	    }
	}


    private static void testCompraProduto(Estoque estoque) {
        // Simula a entrada do usuário
        System.setIn(new ByteArrayInputStream("1\ncliente@example.com\nCliente Teste\n123456789\nsim\n".getBytes()));

        try (Scanner scanner = new Scanner(System.in)) {
            estoque.compraProduto(scanner);

            // Verifica se o título da compra foi criado corretamente
            if (!estoque.getTitulosMap().isEmpty()) {
                registrarResultado("Teste compra produto: PASSED");
            } else {
                registrarResultado("Teste compra produto: FAILED");
            }
        } catch (IOException e) {
            registrarResultado("Teste compra produto: FAILED - " + e.getMessage());
        }
    }




    private static void testFazPagamento(Estoque estoque) {
        // Primeiro, verifique se há um título criado no estoque
        if (estoque.getTitulosMap().isEmpty()) {
            registrarResultado("Teste pagamento: FAILED - Nenhum título disponível para pagamento.");
            return;
        }

        // Obtenha um título existente
        String tituloId = estoque.getTitulosMap().keySet().iterator().next();
        
        // Simule a entrada do título
        System.setIn(new java.io.ByteArrayInputStream((tituloId + "\n").getBytes()));

        try (Scanner scanner = new Scanner(System.in)) {
            estoque.fazPagamento(scanner);

            // Verifica se o título foi marcado como pago
            if (estoque.getTitulosMap().get(tituloId).isPago()) {
                registrarResultado("Teste pagamento: PASSED");
            } else {
                registrarResultado("Teste pagamento: FAILED");
            }
        } catch (IOException e) {
            registrarResultado("Teste pagamento: FAILED - " + e.getMessage());
        }
    }

private static void testRelatorioVendasPorDepartamento(Estoque estoque) {
    // Redirecionar a saída do System.out para capturar o que for impresso no console
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outputStream));

    try {
        // Chama o método que gera o relatório de vendas por departamento
        estoque.relatorioVendasPorDepartamento();

        // Captura a saída e converte para String
        String output = outputStream.toString();

        // Verifica se a saída contém um padrão específico que indica que o relatório foi gerado
        if (output.contains("Relatório de Vendas") || output.contains("Departamento")) { // Ajuste as palavras-chave conforme necessário
            registrarResultado("Teste relatório de vendas por departamento: PASSED");
        } else {
            registrarResultado("Teste relatório de vendas por departamento: FAILED");
        }
    } finally {
        // Restaura o System.out para o console padrão
        System.setOut(originalOut);
    }
}

    
    private static void testAdicionarProduto() {
        try {
            Produto produto = new Produto("1", "Produto Teste", 100.0, "Departamento Teste");
            if (Produto.getId().equals("1") && Produto.getNome().equals("Produto Teste")) {
                registrarResultado("Teste adicionar produto: PASSED");
            } else {
                registrarResultado("Teste adicionar produto: FAILED");
            }
        } catch (Exception e) {
            registrarResultado("Teste adicionar produto: FAILED - " + e.getMessage());
        }
    }

    private static void testSalvarProdutos() {
        try {
            Loja loja = new Loja(); // Agora este construtor existe
            Produto produto = new Produto("1", "Produto Teste", 100.0, "Departamento Teste");
            loja.addProduto(produto);
            Produto.saveProdutos(loja);
            File file = new File(Produto.PRODUTOS_ARQUIVO);
            if (file.exists()) {
                registrarResultado("Teste salvar produtos: PASSED");
            } else {
                registrarResultado("Teste salvar produtos: FAILED - Arquivo não encontrado");
            }
        } catch (IOException e) {
            registrarResultado("Teste salvar produtos: FAILED - " + e.getMessage());
        }
    }


    private static void testCarregarProdutos() {
        try {
            Loja loja = new Loja();
            Produto produto = new Produto("1", "Produto Teste", 100.0, "Departamento Teste");
            loja.addProduto(produto);
            Produto.saveProdutos(loja);
            loja.clearProdutos(); // Limpa para simular carregamento
            Produto.carregaProdutos(loja);
            if (loja.produtosMap.size() == 1) {
                registrarResultado("Teste carregar produtos: PASSED");
            } else {
                registrarResultado("Teste carregar produtos: FAILED");
            }
        } catch (IOException e) {
            registrarResultado("Teste carregar produtos: FAILED - " + e.getMessage());
        }
    }

    private static void testCriptografiaProduto() {
        try {
            String data = "1,Produto Teste,100.0,Departamento Teste";
            String encryptedData = Produto.encrypt(data);
            String decryptedData = Produto.decrypt(encryptedData);
            if (data.equals(decryptedData)) {
                registrarResultado("Teste criptografia: PASSED");
            } else {
                registrarResultado("Teste criptografia: FAILED");
            }
        } catch (Exception e) {
            registrarResultado("Teste criptografia: FAILED - " + e.getMessage());
        }
    }

    private static void testFromStringProd() {
        try {
            Produto produto = Produto.fromString("1,Produto Teste,100.0,Departamento Teste");
            if (produto != null && "Produto Teste".equals(produto.getNome())) {
                registrarResultado("Teste fromString: PASSED");
            } else {
                registrarResultado("Teste fromString: FAILED");
            }
        } catch (Exception e) {
            registrarResultado("Teste fromString: FAILED - " + e.getMessage());
        }
    }

    private static void testFromStringFormatoInvalidoProd() {
        Produto produto = Produto.fromString("1,Produto Teste,invalido,Departamento Teste");
        if (produto == null) {
            registrarResultado("Teste fromString formato inválido: PASSED");
        } else {
            registrarResultado("Teste fromString formato inválido: FAILED");
        }
    }

    
    private static void testAdicionarTitulo() {
        try {
            Titulo titulo = new Titulo("1", 100.0, false, "Departamento Teste");
            if (titulo.getId().equals("1") && titulo.getQuantidade() == 100.0) {
                registrarResultado("Teste adicionar título: PASSED");
            } else {
                registrarResultado("Teste adicionar título: FAILED");
            }
        } catch (Exception e) {
            registrarResultado("Teste adicionar título: FAILED - " + e.getMessage());
        }
    }

    private static void testSalvarTitulos() {
        try {
            Loja loja = new Loja();
            Titulo titulo = new Titulo("1", 100.0, false, "Departamento Teste");
            loja.addTitulo(titulo);
            Titulo.saveTitulos(loja);
            File file = new File(Titulo.TITULOS_ARQUIVO);
            if (file.exists()) {
                registrarResultado("Teste salvar títulos: PASSED");
            } else {
                registrarResultado("Teste salvar títulos: FAILED - Arquivo não encontrado");
            }
        } catch (IOException e) {
            registrarResultado("Teste salvar títulos: FAILED - " + e.getMessage());
        }
    }

    private static void testCarregarTitulos() {
        try {
            Loja loja = new Loja();
            Titulo titulo = new Titulo("1", 100.0, false, "Departamento Teste");
            loja.addTitulo(titulo);
            Titulo.saveTitulos(loja);
            loja.clearTitulos(); // Limpa para simular carregamento
            Titulo.carregaTitulos(loja);
            if (loja.titulosMap.size() == 1) {
                registrarResultado("Teste carregar títulos: PASSED");
            } else {
                registrarResultado("Teste carregar títulos: FAILED");
            }
        } catch (IOException e) {
            registrarResultado("Teste carregar títulos: FAILED - " + e.getMessage());
        }
    }

    private static void testCriptografia() {
        try {
            String data = "1,100.0,false,Departamento Teste";
            String encryptedData = Titulo.encrypt(data);
            String decryptedData = Titulo.decrypt(encryptedData);
            if (data.equals(decryptedData)) {
                registrarResultado("Teste criptografia: PASSED");
            } else {
                registrarResultado("Teste criptografia: FAILED");
            }
        } catch (Exception e) {
            registrarResultado("Teste criptografia: FAILED - " + e.getMessage());
        }
    }

    private static void testFromStringTitulo() {
        try {
            Titulo titulo = Titulo.fromString("1,100.0,false,Departamento Teste");
            if (titulo != null && "Departamento Teste".equals(titulo.getDepartamento())) {
                registrarResultado("Teste fromString: PASSED");
            } else {
                registrarResultado("Teste fromString: FAILED");
            }
        } catch (Exception e) {
            registrarResultado("Teste fromString: FAILED - " + e.getMessage());
        }
    }

    private static void testFromStringFormatoInvalidoTitulo() {
        Titulo titulo = Titulo.fromString("1,invalido,false,Departamento Teste");
        if (titulo == null) {
            registrarResultado("Teste fromString formato inválido: PASSED");
        } else {
            registrarResultado("Teste fromString formato inválido: FAILED");
        }
    }
    
    private static void testAdicionarFuncionario() {
        try {
            Loja loja = new Loja("1", "Loja Teste", "Básico");

            // Testando adição de Gerente
            Funcionario gerente = new Gerente("g1", "Gerente Teste", 8000.0, "gerente@test.com", "senha123");
            loja.addFuncionario(gerente);
            if (loja.getFuncionario("g1") != null) {
                registrarResultado("Teste adicionar gerente: PASSED");
            } else {
                registrarResultado("Teste adicionar gerente: FAILED");
            }

            // Testando adição de Atendente
            Funcionario atendente = new Atendente("a1", "Atendente Teste", 3000.0, "atendente@test.com", "senha123");
            loja.addFuncionario(atendente);
            if (loja.getFuncionario("a1") != null) {
                registrarResultado("Teste adicionar atendente: PASSED");
            } else {
                registrarResultado("Teste adicionar atendente: FAILED");
            }

            // Testando adição de Supervisor
            Funcionario supervisor = new Supervisor("s1", "Supervisor Teste", 5000.0, "supervisor@test.com", "senha123");
            loja.addFuncionario(supervisor);
            if (loja.getFuncionario("s1") != null) {
                registrarResultado("Teste adicionar supervisor: PASSED");
            } else {
                registrarResultado("Teste adicionar supervisor: FAILED");
            }

            // Testando adição de Estoquista
            Funcionario estoquista = new Estoquista("e1", "Estoquista Teste", 3500.0, "estoquista@test.com", "senha123");
            loja.addFuncionario(estoquista);
            if (loja.getFuncionario("e1") != null) {
                registrarResultado("Teste adicionar estoquista: PASSED");
            } else {
                registrarResultado("Teste adicionar estoquista: FAILED");
            }

        } catch (Exception e) {
            registrarResultado("Teste adicionar funcionário: FAILED - " + e.getMessage());
        }
    }


    private static void testAdicionarProdutoLoja() {
        try {
            Loja loja = new Loja("1", "Loja Teste", "Básico");
            Produto produto = new Produto("p1", "Produto Teste", 10.0, "Departamento Teste");
            loja.addProduto(produto);
            
            if (loja.getProduto("p1") != null) {
                registrarResultado("Teste adicionar produto: PASSED");
            } else {
                registrarResultado("Teste adicionar produto: FAILED");
            }
        } catch (Exception e) {
            registrarResultado("Teste adicionar produto: FAILED - " + e.getMessage());
        }
    }


    private static void testAdicionarTituloLoja() {
        try {
            Loja loja = new Loja("1", "Loja Teste", "Básico");
            Titulo titulo = new Titulo("t1", 100.0, false, "Departamento Teste");
            loja.addTitulo(titulo);
            
            if (loja.titulosMap.containsKey("t1")) {
                registrarResultado("Teste adicionar título: PASSED");
            } else {
                registrarResultado("Teste adicionar título: FAILED");
            }
        } catch (Exception e) {
            registrarResultado("Teste adicionar título: FAILED - " + e.getMessage());
        }
    }

    private static void testSalvarELerLoja() {
        try {
            Loja loja = new Loja("1", "Loja Teste", "Básico");
            
            // Criando diferentes tipos de funcionários
            Funcionario atendente = new Atendente("f1", "Atendente Teste", 1500.0, "atendente@loja.com", "senha123");
            Funcionario gerente = new Gerente("f2", "Gerente Teste", 3000.0, "gerente@loja.com", "senha123");
            Funcionario supervisor = new Supervisor("f3", "Supervisor Teste", 2500.0, "supervisor@loja.com", "senha123");
            Funcionario estoquista = new Estoquista("f4", "Estoquista Teste", 1200.0, "estoquista@loja.com", "senha123");
            
            // Criando um produto e um título
            Produto produto = new Produto("p1", "Produto Teste", 10.0, "Departamento Teste");
            Titulo titulo = new Titulo("t1", 100.0, false, "Departamento Teste");
            
            // Adicionando funcionários, produto e título à loja
            loja.addFuncionario(atendente);
            loja.addFuncionario(gerente);
            loja.addFuncionario(supervisor);
            loja.addFuncionario(estoquista);
            loja.addProduto(produto);
            loja.addTitulo(titulo);
            
            loja.saveAll(); // Salvar dados da loja

            // Criar uma nova loja para carregar os dados
            Loja novaLoja = new Loja("", "", "");
            novaLoja.loadAll(); // Carregar dados da loja
            
            if (novaLoja.getFuncionario("f1") != null && 
                novaLoja.getFuncionario("f2") != null &&
                novaLoja.getFuncionario("f3") != null &&
                novaLoja.getFuncionario("f4") != null &&
                novaLoja.getProduto("p1") != null && 
                novaLoja.titulosMap.containsKey("t1")) {
                registrarResultado("Teste salvar e carregar loja: PASSED");
            } else {
                registrarResultado("Teste salvar e carregar loja: FAILED");
            }
        } catch (IOException e) {
            registrarResultado("Teste salvar e carregar loja: FAILED - " + e.getMessage());
        }
    }


    private static void testCadastrarOuEntrarLoja() {
        try {
            // Simulação de entrada/novo cadastro de loja
            // Isso pode envolver interações com o Scanner, então pode precisar de ajustes
            // Alternativamente, pode-se refatorar para permitir a injeção de dependências

            // Crie uma nova loja
            Loja loja = Loja.cadastrarOuEntrarLoja();
            if (loja != null) {
                registrarResultado("Teste cadastrar ou entrar loja: PASSED");
            } else {
                registrarResultado("Teste cadastrar ou entrar loja: FAILED - Loja não criada");
            }
        } catch (IOException e) {
            registrarResultado("Teste cadastrar ou entrar loja: FAILED - " + e.getMessage());
        }
    }

    private static void testFuncionalidadeDisponivel() {
        try {
            Loja loja = new Loja("1", "Loja Teste", "Básico");
            if (loja.isFuncionalidadeDisponivel("CadastroProduto")) {
                registrarResultado("Teste funcionalidade disponível (Básico): PASSED");
            } else {
                registrarResultado("Teste funcionalidade disponível (Básico): FAILED");
            }

            loja.setPlanoAtual("Premium");
            if (!loja.isFuncionalidadeDisponivel("listarLogsOperacoes")) {
                registrarResultado("Teste funcionalidade disponível (Premium): PASSED");
            } else {
                registrarResultado("Teste funcionalidade disponível (Premium): FAILED");
            }

            loja.setPlanoAtual("Deluxe");
            if (loja.isFuncionalidadeDisponivel("listarLogsOperacoes")) {
                registrarResultado("Teste funcionalidade disponível (Deluxe): PASSED");
            } else {
                registrarResultado("Teste funcionalidade disponível (Deluxe): FAILED");
            }
        } catch (Exception e) {
            registrarResultado("Teste funcionalidade disponível: FAILED - " + e.getMessage());
        }
    }
    
    
    private static void testLogOperation() {
        try {
            // Criar funcionários de diferentes cargos
            Funcionario gerente = new Gerente("f1", "Gerente Teste", 3000.0, "gerente@loja.com", "senha123");
            Funcionario atendente = new Atendente("f2", "Atendente Teste", 1500.0, "atendente@loja.com", "senha123");
            Funcionario supervisor = new Supervisor("f3", "Supervisor Teste", 2500.0, "supervisor@loja.com", "senha123");
            Funcionario estoquista = new Estoquista("f4", "Estoquista Teste", 1200.0, "estoquista@loja.com", "senha123");

            String operation = "Criar produto";
            ResourceBundle messages = ResourceBundle.getBundle("messages", Locale.getDefault());

            // Log a operação para cada funcionário
            Logger.logOperation(gerente, operation, messages);
            Logger.logOperation(atendente, operation, messages);
            Logger.logOperation(supervisor, operation, messages);
            Logger.logOperation(estoquista, operation, messages);

            // Verifica se o log foi criado (ex. lê o arquivo e valida a entrada)
            try (BufferedReader reader = new BufferedReader(new FileReader("operation_logs.txt"))) {
                String line;
                boolean foundGerente = false;
                boolean foundAtendente = false;
                boolean foundSupervisor = false;
                boolean foundEstoquista = false;

                while ((line = reader.readLine()) != null) {
                    if (line.contains("Criar produto")) {
                        if (line.contains("Gerente Teste")) {
                            foundGerente = true;
                        } else if (line.contains("Atendente Teste")) {
                            foundAtendente = true;
                        } else if (line.contains("Supervisor Teste")) {
                            foundSupervisor = true;
                        } else if (line.contains("Estoquista Teste")) {
                            foundEstoquista = true;
                        }
                    }
                }

                // Verifica se todas as entradas foram encontradas
                if (foundGerente && foundAtendente && foundSupervisor && foundEstoquista) {
                    registrarResultado("Teste de log de operação: PASSED");
                } else {
                    registrarResultado("Teste de log de operação: FAILED");
                }
            } catch (IOException e) {
                registrarResultado("Teste de log de operação: FAILED - " + e.getMessage());
            }
        } catch (Exception e) {
            registrarResultado("Teste de log de operação: FAILED - " + e.getMessage());
        }
    }


    private static void testLogErrorHandling() {
        // Simula uma falha no arquivo, como não ter permissão de escrita
        try {
            // Muda o caminho do arquivo para um local inválido (exemplo)
            System.setProperty("java.io.tmpdir", "/invalid/path");
            
            // Criar funcionários de diferentes cargos
            Funcionario gerente = new Gerente("f1", "Gerente Teste", 3000.0, "gerente@loja.com", "senha123");
            Funcionario atendente = new Atendente("f2", "Atendente Teste", 1500.0, "atendente@loja.com", "senha123");
            Funcionario supervisor = new Supervisor("f3", "Supervisor Teste", 2500.0, "supervisor@loja.com", "senha123");
            Funcionario estoquista = new Estoquista("f4", "Estoquista Teste", 1200.0, "estoquista@loja.com", "senha123");

            String operation = "Criar produto";
            ResourceBundle messages = ResourceBundle.getBundle("messages", Locale.getDefault());
            
            // Tentar logar a operação para cada funcionário
            Logger.logOperation(gerente, operation, messages);
            Logger.logOperation(atendente, operation, messages);
            Logger.logOperation(supervisor, operation, messages);
            Logger.logOperation(estoquista, operation, messages);

            // Se não ocorreu exceção, o teste falha
            registrarResultado("Teste de tratamento de erro ao gravar log: FAILED - Não ocorreu erro esperado.");
        } catch (Exception e) {
            registrarResultado("Teste de tratamento de erro ao gravar log: PASSED - " + e.getMessage());
        } finally {
            // Restaura o caminho do arquivo para o padrão
            System.clearProperty("java.io.tmpdir");
        }
    }


    private static void testFormattedTimestamp() {
        String timestamp = Logger.getFormattedTimestamp(Locale.getDefault());
        // Verifica se o timestamp está no formato esperado
        if (timestamp.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
            registrarResultado("Teste de formatação de timestamp: PASSED");
        } else {
            registrarResultado("Teste de formatação de timestamp: FAILED");
        }
    }

    private static void testGetZoneIdForLocale() {
        ZoneId zoneIdEs = Logger.getZoneIdForLocale(new Locale("es"));
        ZoneId zoneIdEn = Logger.getZoneIdForLocale(new Locale("en"));
        ZoneId zoneIdPt = Logger.getZoneIdForLocale(new Locale("pt"));
        
        if (zoneIdEs.equals(ZoneId.of("America/Argentina/Buenos_Aires")) &&
            zoneIdEn.equals(ZoneId.of("America/New_York")) &&
            zoneIdPt.equals(ZoneId.of("America/Sao_Paulo"))) {
            registrarResultado("Teste de ZoneId para Locale: PASSED");
        } else {
            registrarResultado("Teste de ZoneId para Locale: FAILED");
        }
    }
    
    private static void testAtendenteCreation() {
        Atendente atendente = new Atendente("a1", "João", 3000.0, "joao@example.com", "senha123");
        
        if (atendente.getId().equals("a1") && atendente.getNome().equals("João") &&
            atendente.getCargo().equals("Atendente") && atendente.getSalario() == 3000.0) {
            registrarResultado("Teste de criação de Atendente: PASSED");
        } else {
            registrarResultado("Teste de criação de Atendente: FAILED");
        }
    }

    private static void testAtendentePermissions() {
        Atendente atendente = new Atendente("a1", "João", 3000.0, "joao@example.com", "senha123");
        
        if (!atendente.podeCadastrarProduto() && 
            !atendente.podeCadastrarDepartamento() && 
            atendente.podeCadastrarCliente() && 
            atendente.podeVenderProduto() && 
            !atendente.podeVerRelatorios()) {
            registrarResultado("Teste de permissões do Atendente: PASSED");
        } else {
            registrarResultado("Teste de permissões do Atendente: FAILED");
        }
    }
    
    
    private static void testEstoquistaCreation() {
        Estoquista estoquista = new Estoquista("e1", "Maria", 2500.0, "maria@example.com", "senha123");
        
        if (estoquista.getId().equals("e1") && estoquista.getNome().equals("Maria") &&
            estoquista.getCargo().equals("Estoquista") && estoquista.getSalario() == 2500.0) {
            registrarResultado("Teste de criação de Estoquista: PASSED");
        } else {
            registrarResultado("Teste de criação de Estoquista: FAILED");
        }
    }

    private static void testEstoquistaPermissions() {
        Estoquista estoquista = new Estoquista("e1", "Maria", 2500.0, "maria@example.com", "senha123");
        
        if (estoquista.podeCadastrarProduto() && 
            estoquista.podeCadastrarDepartamento() && 
            !estoquista.podeCadastrarCliente() && 
            !estoquista.podeVenderProduto() && 
            !estoquista.podeVerRelatorios()) {
            registrarResultado("Teste de permissões do Estoquista: PASSED");
        } else {
            registrarResultado("Teste de permissões do Estoquista: FAILED");
        }
    }
    
    private static void testGerenteCreation() {
        Gerente gerente = new Gerente("g1", "Carlos", 5000.0, "carlos@example.com", "senha123");
        
        if (gerente.getId().equals("g1") && gerente.getNome().equals("Carlos") &&
            gerente.getCargo().equals("Gerente") && gerente.getSalario() == 5000.0) {
            registrarResultado("Teste de criação de Gerente: PASSED");
        } else {
            registrarResultado("Teste de criação de Gerente: FAILED");
        }
    }

    private static void testGerentePermissions() {
        Gerente gerente = new Gerente("g1", "Carlos", 5000.0, "carlos@example.com", "senha123");
        
        if (gerente.podeCadastrarProduto() && 
            gerente.podeCadastrarDepartamento() && 
            gerente.podeCadastrarCliente() && 
            gerente.podeVenderProduto() && 
            gerente.podeVerRelatorios()) {
            registrarResultado("Teste de permissões do Gerente: PASSED");
        } else {
            registrarResultado("Teste de permissões do Gerente: FAILED");
        }
    }

    private static void testGerenciarAssinatura() {
        Loja loja = new Loja("l1", "Loja Exemplo", "Básico");
        Gerente gerente = new Gerente("g1", "Carlos", 5000.0, "carlos@example.com", "senha123");

        // Simulação de entrada do usuário
        String input = "2\n"; // Simulando escolha do plano Premium
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in); // Scanner baseado no ByteArrayInputStream

        ResourceBundle messages = ResourceBundle.getBundle("messages"); // Simulação de mensagens
        gerente.gerenciarAssinatura(loja, scanner, messages);

        // Verificação do resultado do teste
        if (loja.getPlanoAtual().equals("Premium")) {
            registrarResultado("Teste de gerenciamento de assinatura: PASSED");
        } else {
            registrarResultado("Teste de gerenciamento de assinatura: FAILED");
        }
    }


    
    
    private static void testSupervisorCreation() {
        Supervisor supervisor = new Supervisor("s1", "Ana", 4000.0, "ana@example.com", "senha123");
        
        if (supervisor.getId().equals("s1") && supervisor.getNome().equals("Ana") &&
            supervisor.getCargo().equals("Supervisor") && supervisor.getSalario() == 4000.0) {
            registrarResultado("Teste de criação de Supervisor: PASSED");
        } else {
            registrarResultado("Teste de criação de Supervisor: FAILED");
        }
    }

    private static void testSupervisorPermissions() {
        Supervisor supervisor = new Supervisor("s1", "Ana", 4000.0, "ana@example.com", "senha123");
        
        if (!supervisor.podeCadastrarProduto() && 
            !supervisor.podeCadastrarDepartamento() && 
            supervisor.podeCadastrarCliente() && 
            supervisor.podeVenderProduto() && 
            supervisor.podeVerRelatorios()) {
            registrarResultado("Teste de permissões do Supervisor: PASSED");
        } else {
            registrarResultado("Teste de permissões do Supervisor: FAILED");
        }
    }
    
    private static void testFuncionarioCreation() {
        Funcionario funcionario = new Gerente("g1", "Carlos", 5000.0, "carlos@example.com", "senha123");
        
        assert funcionario.getId().equals("g1");
        assert funcionario.getNome().equals("Carlos");
        assert funcionario.getCargo().equals("Gerente");
        assert funcionario.getSalario() == 5000.0;
        assert funcionario.getEmail().equals("carlos@example.com");
    }

    private static void testPasswordEncryptionAndVerification() {
        String senha = "senha123";
        String encryptedSenha = null;

        try {
            encryptedSenha = CryptoUtils.encrypt(senha, Funcionario.SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Funcionario funcionario = new Gerente("g1", "Carlos", 5000.0, "carlos@example.com", encryptedSenha);
        
        assert funcionario.verificarSenha("senha123"); // Deve ser true
        assert !funcionario.verificarSenha("senhaErrada"); // Deve ser false
    }

    private static void testFuncionarioRegistration() {
        // Supondo que você tenha um gerente autenticado
        Funcionario gerente = new Gerente("g1", "Carlos", 5000.0, "carlos@example.com", "senha123");

        // Carregue o ResourceBundle corretamente
        ResourceBundle messages = ResourceBundle.getBundle("messages", Locale.getDefault());

        // Passa o ResourceBundle carregado
        Funcionario.cadastrarFuncionario(gerente, messages);
        
        assert Funcionario.getFuncionariosMap().containsKey("novoId"); // Verifique se o funcionário foi adicionado
    }

    private static void testListarFuncionarios() {
        Funcionario.listarFuncionarios(); // A saída deve conter os funcionários cadastrados
    }
    
    private static void testCriarContrato() {
        try {
            SimuladorBlockchain.criarContrato("Contrato de Teste");  // Cria um novo contrato de teste
            registrarResultado("Teste criar contrato(Blockchain): PASSED");
        } catch (Exception e) {
            registrarResultado("Teste criar contrato(Blockchain): FAILED - " + e.getMessage());
        }
    }


    // Teste para visualizar contratos
    private static void testVisualizarContratos() {
        try {
            SimuladorBlockchain.visualizarContratos(); // Visualiza todos os contratos
            registrarResultado("Teste visualizar contratos(Blockchain): PASSED");
        } catch (Exception e) {
            registrarResultado("Teste visualizar contratos(Blockchain): FAILED - " + e.getMessage());
        }
    }


    // Teste para desativar um contrato
    private static void testDesativarContrato() {
        try {
            // Lê o conteúdo descriptografado do arquivo de contratos
            String conteudoExistente = SimuladorBlockchain.lerArquivoCriptografado();
            
            // Obter o último contrato adicionado usando o conteúdo existente
            int contratoId = SimuladorBlockchain.obterProximoId(conteudoExistente) - 1;

            // Certifica-se de que o contrato existe antes de tentar desativá-lo
            if (contratoId >= 0) {
                SimuladorBlockchain.desativarContrato(contratoId); // Desativa o contrato com o ID correspondente
                registrarResultado("Teste desativar contrato(Blockchain): PASSED");
            } else {
                registrarResultado("Teste desativar contrato(Blockchain): FAILED - Nenhum contrato disponível para desativação");
            }
        } catch (Exception e) {
            registrarResultado("Teste desativar contrato(Blockchain): FAILED - " + e.getMessage());
        }
    }



}
