package erp;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.sql.ResultSet;



public class Estoque {
    private static final String SECRET_KEY = "1234567890123456"; // 16 bytes secret key for AES
    private Map<String, Produto> produtosMap;
    private static Map<String, Titulo> titulosMap;
    private Map<String, Cliente> clientesMap; // Mantido como cliente1
    Connection conn = DatabaseConnection.getConnection();

    private static ResourceBundle messages; // ResourceBundle para mensagens

    public Estoque(Locale locale) throws IOException, SQLException {
        produtosMap = new HashMap<>();
        titulosMap = new HashMap<>();
        clientesMap = new HashMap<>();
        messages = ResourceBundle.getBundle("messages", locale); // Inicializa o ResourceBundle
        carregaProduto();
        carregaTitulos();
        carregaDepartamentos();
    }
    
    public Map<String, Produto> getProdutosMap() {
        return produtosMap;
    }
    
    public Map<String, Titulo> getTitulosMap() {
        return titulosMap;
    }

    // Criptografia e descriptografia
    private static Cipher getCipher(int mode) throws Exception {
        Key key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, key);
        return cipher;
    }

    private String encrypt(String data) throws Exception {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private String decrypt(String data) throws Exception {
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
        byte[] decoded = Base64.getDecoder().decode(data);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }

    // Carregar e salvar departamentos com criptografia
    private void carregaDepartamentos() throws SQLException {
        String sql = "SELECT id, nome FROM departamento";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("id");
                String nome = rs.getString("nome");

                Departamento departamento = new Departamento(id, nome);
                Departamento.addDepartamento(id, nome);
            }
        }
    }

    
   
    public void addDepartamento(String id, String nome) throws IOException, SQLException {
        // Lógica para adicionar departamento
        // Chama o método que adiciona o departamento
        Departamento.addDepartamento(id, nome);

        System.out.println(messages.getString("department_added_successfully"));
    }
    

    private void saveDepartamentos() throws SQLException {
        String sql = "INSERT INTO departamento (id, nome) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            System.out.println(messages.getString("file.departments.saving")); // Mensagem antes de salvar
            for (Departamento departamento : Departamento.listarDepartamentos()) {
                pstmt.setString(1, departamento.getId());
                pstmt.setString(2, departamento.getNome());
                pstmt.executeUpdate(); // Executa a inser��o
            }
            System.out.println(messages.getString("departments_saved_successfully")); // Mensagem ap�s salvar
        } catch (SQLException e) {
            System.err.println("Erro ao salvar departamentos: " + e.getMessage()); // Depura��o
            throw new SQLException(messages.getString("error_saving_departments"), e);
        }
    }


    // Adicionar produto com criptografia
    public void addProduto(String id, String nome, double preco, String departamentoId) throws IOException, SQLException {
        // Verifica se o ID do produto já existe
        if (produtosMap.containsKey(id)) {
            throw new IllegalArgumentException(messages.getString("product_already_exists"));
        }

        // Verifica se o departamento existe
        Departamento departamento = Departamento.buscarDepartamentoPorId(departamentoId);
        if (departamento == null) {
            System.out.println(messages.getString("department_not_found"));
            return;
        }

        Produto produto = new Produto(id, nome, preco, departamentoId);
        produtosMap.put(id, produto);

        // Salva o produto no banco de dados
        try {
            saveProdutos(produto);
            System.out.println(messages.getString("product_added_successfully"));
        } catch (SQLException e) {
            System.err.println("Erro ao inserir produto no banco de dados: " + e.getMessage());
            throw e; // Lança a exceção para ser capturada no método chamador, se necessário
        }

        // Adiciona o registro no blockchain
        String descricao = "Novo produto adicionado: ID " + id + ", Nome " + nome + ", Preço R$ " + preco;
        SimuladorBlockchain.criarContrato(descricao);
    }



    // Listar produtos com criptografia
    public void listaProdutos() {
        System.out.println(messages.getString("products_list"));
        for (Produto produto : produtosMap.values()) {
            Departamento departamento = Departamento.buscarDepartamentoPorId(produto.getDepartamento());
            String departamentoNome = (departamento != null) ? departamento.getNome() : messages.getString("unknown");
            System.out.println(produto.getId() + " - " + produto.getNome() + " - R$ " + produto.getPreco() + " - " + messages.getString("department") + ": " + departamentoNome);
        }
    }


    public void compraProduto(String productId, String clientEmail, String nome, String telefone, String resposta) throws Exception {
        Produto produto = produtosMap.get(productId);

        if (produto != null) {
            // Criptografa o email antes de buscar no banco de dados
            String emailCriptografado = CryptoUtils.encrypt(clientEmail, SECRET_KEY);
            
            // Recupera o cliente do banco de dados
            Cliente cliente = Cliente.recuperarClientePorEmail(emailCriptografado);

            if (cliente == null) {
                // Se o cliente não for encontrado, utilize os dados coletados pela interface
                if (!(resposta.equalsIgnoreCase("sim") || resposta.equalsIgnoreCase("s") || resposta.equalsIgnoreCase("yes"))) {
                    System.out.println(messages.getString("registration_cancelled"));
                    return;
                }

                // Adiciona novo cliente no banco de dados
                Cliente.adicionarCliente(nome, telefone, clientEmail);
                System.out.println(messages.getString("new_client_registered"));

                // Recupera novamente o cliente após a adição
                cliente = Cliente.recuperarClientePorEmail(emailCriptografado);
            }

            // Cria e adiciona o título da compra com o email do cliente
            Titulo titulo = new Titulo(UUID.randomUUID().toString(), produto.getPreco(), false, produto.getDepartamento(), cliente.getEmail());
            titulosMap.put(titulo.getId(), titulo);
            
            cliente.addCompra(titulo);

            // Atualiza a lista de títulos e clientes no banco de dados
            try {
                saveTitulos(titulo);
            } catch (SQLException e) {
                System.err.println("Erro ao inserir título no banco de dados: " + e.getMessage());
            }

            String descricao = "Compra do produto " + titulo.getId() + " - " + produto.getNome() + " no valor de " + produto.getPreco();
            SimuladorBlockchain.criarContrato(descricao);

            System.out.println(messages.getString("product_purchased_successfully") + ". " + messages.getString("title_generated") + ": " + titulo.getId());
        } else {
            System.out.println(messages.getString("product_not_found"));
        }
    }

    public void fazPagamento(String tituloId) throws IOException {
        // Tente obter o título do mapa primeiro
        Titulo titulo = titulosMap.get(tituloId);

        if (titulo != null) {
            // Marca o título como pago
            titulo.setPaga(true);

            try {
                // Atualiza o título no banco de dados
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/amostraderp", "root", "Andersu_joestar123");
                String sql = "UPDATE titulo SET paga = ? WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setBoolean(1, true); // Marca como pago
                stmt.setString(2, tituloId); // ID do título a ser atualizado
                stmt.executeUpdate(); // Executa a atualização

                // Obter o email do cliente associado ao título
                String emailCliente = titulo.getEmailCliente(); // Método para obter o email do cliente associado
                if (emailCliente != null) {
                    // Emitir nota fiscal após o pagamento usando o objeto Titulo
                    emitirNotaFiscal(titulo, emailCliente); // Passando o email do cliente para o método
                }

                // Salvar o total vendido no relatório de vendas
                String departamentoId = titulo.getDepartamento(); // Obtém o ID do departamento
                salvarRelatorioVendas(departamentoId, titulo.getQuantidade()); // Chama o método para salvar vendas

                stmt.close();
                conn.close();

                System.out.println(messages.getString("title_paid_successfully"));

                String descricao = "Título de pagamento realizado: ID " + titulo.getId() + ", Valor R$ " + titulo.getQuantidade();
                SimuladorBlockchain.criarContrato(descricao);
            } catch (SQLException e) {
                e.printStackTrace(); // Ou faça o tratamento apropriado
                System.out.println("Erro ao salvar o título: " + e.getMessage());
            }
        } else {
            System.out.println(messages.getString("title_not_found"));
        }
    }


    private void emitirNotaFiscal(Titulo titulo, String emailCliente) throws IOException {
        // Criptografar o e-mail antes de buscar o cliente
        String emailCriptografado;
        try {
            emailCriptografado = CryptoUtils.encrypt(emailCliente, SECRET_KEY);
        } catch (Exception e) {
            System.out.println("Erro ao criptografar o e-mail: " + e.getMessage());
            return; // Encerra o m�todo se ocorrer uma exce��o
        }

        // Buscar o cliente que realizou a compra usando o e-mail criptografado
        Cliente cliente = Cliente.recuperarClientePorEmail(emailCriptografado);

        if (cliente != null) {
            // Buscar o departamento relacionado ao t�tulo
            Departamento departamento = Departamento.buscarDepartamentoPorId(titulo.getDepartamento());
            String nomeDepartamento = (departamento != null) ? departamento.getNome() : messages.getString("unknown");

            // Definir o fuso hor�rio com base no idioma selecionado
            ZoneId zoneId;
            Locale locale = Locale.getDefault();
            String simboloMoeda;
            if (locale.equals(new Locale("pt", "BR"))) {
                zoneId = ZoneId.of("America/Sao_Paulo");
                simboloMoeda = "R$";
            } else if (locale.equals(new Locale("es", "AR"))) {
                zoneId = ZoneId.of("America/Argentina/Buenos_Aires");
                simboloMoeda = "$";
            } else if (locale.equals(new Locale("en", "US"))) {
                zoneId = ZoneId.of("America/New_York");
                simboloMoeda = "US$";
            } else {
                // Fuso hor�rio e s�mbolo padr�o
                zoneId = ZoneId.systemDefault();
                simboloMoeda = "$";
            }

            // Obter a data e hora atuais no fuso hor�rio espec�fico
            ZonedDateTime dataHoraAtual = ZonedDateTime.now(zoneId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss z");
            String dataHoraFormatada = dataHoraAtual.format(formatter);

            // Formatar o valor pago manualmente com o s�mbolo personalizado
            String valorFormatado = String.format("%s %.2f", simboloMoeda, titulo.getQuantidade());

            // Gerar uma chave de acesso �nica
            String chaveAcesso = UUID.randomUUID().toString();

            // Escrever o arquivo da nota fiscal
            File notaFiscalFile = new File("nota_fiscal_" + titulo.getId() + ".txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(notaFiscalFile))) {
                writer.write(messages.getString("invoice"));
                writer.newLine();
                writer.write("===================================");
                writer.newLine();
                writer.write(messages.getString("company_name") + ": AmostradERP");
                writer.newLine();
                writer.write(messages.getString("company_cnpj") + ": 84.495.560/0001-54");
                writer.newLine();
                writer.write(messages.getString("city") + ": Salgueiro");
                writer.newLine();
                writer.write(messages.getString("state") + ": Pernambuco");
                writer.newLine();
                writer.write("===================================");
                writer.newLine();
                writer.write(messages.getString("client") + ": " + cliente.getNome());
                writer.newLine();
                writer.write(messages.getString("email") + ": " + ofuscarEmail(cliente.getEmail()));
                writer.newLine();
                writer.write(messages.getString("phone") + ": " + ofuscarTelefone(cliente.getTelefone()));
                writer.newLine();
                writer.write("-----------------------------------");
                writer.newLine();
                writer.write(messages.getString("title_id") + ": " + titulo.getId());
                writer.newLine();
                writer.write(messages.getString("amount_paid") + ": " + valorFormatado);
                writer.newLine();
                writer.write(messages.getString("department") + ": " + nomeDepartamento);
                writer.newLine();
                writer.write(messages.getString("status") + ": " + messages.getString("paid"));
                writer.newLine();
                writer.write(messages.getString("payment_method") + ": " + messages.getString("cash_payment"));
                writer.newLine();
                writer.write("===================================");
                writer.newLine();
                writer.write("");
                writer.newLine();
                writer.write("-----------------------------------");
                writer.newLine();
                writer.write(messages.getString("issue_date_time") + ": " + dataHoraFormatada);
                writer.newLine();
                writer.write(messages.getString("access_key") + ": " + chaveAcesso);
                writer.newLine();
            }

            System.out.println(messages.getString("invoice_issued") + ": " + notaFiscalFile.getAbsolutePath());

            // Adicionar a nota fiscal ao banco de dados
            salvarNotaFiscalNoBanco(chaveAcesso, emailCliente, titulo.getId(), titulo.getQuantidade());

            // Adicionar contrato ao simulador de blockchain
            String descricao = "Nota fiscal emitida: ID do t�tulo " + titulo.getId() + ", Valor " + valorFormatado;
            SimuladorBlockchain.criarContrato(descricao);
        } else {
            System.out.println(messages.getString("error_issuing_invoice_client_not_found"));
            System.out.println("Nenhum cliente encontrado para o email: " + emailCliente);
        }
    }


    // M�todo para salvar a nota fiscal no banco de dados
    private void salvarNotaFiscalNoBanco(String chaveAcesso, String emailCliente, String tituloId, double valorPago) {
        String sql = "INSERT INTO nota_fiscal (chave_acesso, email_cliente, titulo_id, valor_pago) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, chaveAcesso);
            statement.setString(2, emailCliente);
            statement.setString(3, tituloId);
            statement.setDouble(4, valorPago);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Nota fiscal registrada no banco de dados com sucesso!");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao salvar a nota fiscal no banco de dados.");
            e.printStackTrace();
        }
    }



    // M�todos para ofuscar dados sens�veis
    private String ofuscarEmail(String email) {
        if (email != null && !email.isEmpty()) {
            int atIndex = email.indexOf('@');
            if (atIndex > 2) {
                return email.substring(0, 2) + "*".repeat(atIndex - 2) + email.substring(atIndex);
            }
            return email.substring(0, 2) + "*".repeat(email.length() - 2);
        }
        return messages.getString("email_not_available");
    }

    private String ofuscarTelefone(String telefone) {
        if (telefone != null && telefone.length() > 2) {
            return telefone.substring(0, 2) + "*".repeat(telefone.length() - 2);
        }
        return telefone != null ? telefone : messages.getString("phone_not_available");
    }

    public static void listarTitulosEmAberto() throws Exception {
        try {
			System.out.println(messages.getString("open_titles"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        for (Titulo titulo : titulosMap.values()) {
            if (!titulo.isPago()) {
                System.out.println(titulo.getId() + " - R$ " + titulo.getQuantidade() + " - " + messages.getString("department") + ": " + titulo.getDepartamento());
            }
        }
    }

    private double totalAcumuladoAtual = 0.0; // Vari�vel para armazenar o total acumulado atual
    private String departamentoAtual = ""; // Vari�vel para armazenar o departamento atual

    private void salvarRelatorioVendas(String departamento, double totalVendido) {
        String query = "SELECT total_acumulado FROM relatorio_vendas WHERE departamento = ?";
        String sql = "INSERT INTO relatorio_vendas (departamento, nome_departamento, total_acumulado) VALUES (?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE total_acumulado = ?, nome_departamento = ?";

        // Se o departamento atual � diferente do novo departamento, zere o total acumulado
        if (!departamento.equals(departamentoAtual)) {
            totalAcumuladoAtual = 0.0; // Reseta o total acumulado
            departamentoAtual = departamento; // Atualiza o departamento atual
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(query)) {

            selectStmt.setString(1, departamento);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                totalAcumuladoAtual = rs.getDouble("total_acumulado");
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                double novoTotal = totalAcumuladoAtual + totalVendido;
                String nomeDepartamento = Departamento.buscarDepartamentoPorId(departamento).getNome(); // Busca o nome do departamento

                stmt.setString(1, departamento);
                stmt.setString(2, nomeDepartamento); // Armazena o nome do departamento
                stmt.setDouble(3, novoTotal); // Atualiza com o total acumulado
                stmt.setDouble(4, novoTotal); // Atualiza o total acumulado no banco de dados
                stmt.setString(5, nomeDepartamento); // Atualiza o nome do departamento no banco de dados
                stmt.executeUpdate();

                // Log de depura��o
                System.out.println("Salvando no banco de dados: Departamento: " + nomeDepartamento + ", Total Acumulado: " + novoTotal);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao salvar o relat�rio de vendas: " + e.getMessage());
        }
    }


    public String relatorioVendasPorDepartamento() {
        Map<String, Double> vendasPorDepartamento = new HashMap<>();
        String moedaSimbolo = "";
        StringBuilder relatorio = new StringBuilder();

        // Define o símbolo da moeda com base no idioma atual
        Locale localeAtual = Locale.getDefault();
        if (localeAtual.equals(new Locale("pt", "BR"))) {
            moedaSimbolo = "R$";
        } else if (localeAtual.equals(new Locale("es", "AR"))) {
            moedaSimbolo = "$";
        } else if (localeAtual.equals(new Locale("en", "US"))) {
            moedaSimbolo = "US$";
        }

        // Consulta para buscar o total vendido por departamento diretamente do banco de dados
        String query = "SELECT nome_departamento, total_acumulado FROM relatorio_vendas";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Processa os resultados da consulta
            while (rs.next()) {
                String nomeDepartamento = rs.getString("nome_departamento");
                double totalVendido = rs.getDouble("total_acumulado");

                // Adiciona ao mapa de vendas por departamento
                vendasPorDepartamento.put(nomeDepartamento, totalVendido);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar vendas do banco de dados: " + e.getMessage());
            return messages.getString("database_error"); // Retorna uma mensagem de erro
        }

        // Verifica se há vendas para exibir
        if (vendasPorDepartamento.isEmpty()) {
            return messages.getString("no_sales_for_departments"); // Retorna mensagem se não houver vendas
        }

        // Gera o relatório formatado
        for (Map.Entry<String, Double> entry : vendasPorDepartamento.entrySet()) {
            String nomeDepartamento = entry.getKey();
            double totalVendido = entry.getValue();

            // Formata o relatório com o símbolo de moeda
            relatorio.append(messages.getString("department")).append(": ").append(nomeDepartamento)
                     .append(" - ").append(messages.getString("total_sold")).append(": ")
                     .append(moedaSimbolo).append(String.format("%.2f", totalVendido)).append("\n");
        }

        // Adiciona o registro no blockchain
        String descricao = "Relatório de vendas por departamento gerado.";
        SimuladorBlockchain.criarContrato(descricao);

        // Retorna o relatório formatado como uma String
        return relatorio.toString();
    }






    private void carregaProduto() throws SQLException {
        String sql = "SELECT id, nome, preco, departamento FROM produto";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("id");
                String nome = rs.getString("nome");
                double preco = rs.getDouble("preco");
                String departamentoId = rs.getString("departamento");

                Produto produto = new Produto(id, nome, preco, departamentoId);
                produtosMap.put(id, produto);
            }
        }
    }


    private void carregaTitulos() throws SQLException {
        String sql = "SELECT id, quantidade, paga, departamento, email_cliente FROM titulo"; // Incluindo email_cliente
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("id");
                double quantidade = rs.getDouble("quantidade");
                boolean paga = rs.getBoolean("paga");
                String departamentoId = rs.getString("departamento");
                String emailCliente = rs.getString("email_cliente"); // Novo par�metro: e-mail do cliente

                Titulo titulo = new Titulo(id, quantidade, paga, departamentoId, emailCliente); // Adicionando o e-mail
                titulosMap.put(id, titulo);
            }
        }
    }

    private void carregaClientes() throws IOException {
        Map<Integer, Cliente> clientes = Cliente.listarClientes(); // Agora funciona com o novo m�todo
        // Caso queira armazenar os clientes em um mapa com a chave de email
        for (Map.Entry<Integer, Cliente> entry : clientes.entrySet()) {
            // Adicione l�gica aqui se voc� quiser manipular ou armazenar os clientes de forma diferente
            // Por exemplo, se quiser apenas armazenar no mapa original (clientesMap) por email:
            String email = entry.getValue().getEmail(); // Supondo que voc� tenha um m�todo getEmail na classe Cliente
            clientesMap.put(email, entry.getValue()); // Armazenando com o email como chave
        }
    }





    private void saveProdutos(Produto produto) throws IOException, SQLException {
        String sql = "INSERT INTO produto (id, nome, preco, departamento) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE nome = ?, preco = ?, departamento = ?";
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, produto.getId());
            pstmt.setString(2, produto.getNome());
            pstmt.setDouble(3, produto.getPreco());
            pstmt.setString(4, produto.getDepartamento());
            pstmt.setString(5, produto.getNome());
            pstmt.setDouble(6, produto.getPreco());
            pstmt.setString(7, produto.getDepartamento());
            pstmt.executeUpdate();
        }
    }



    private void saveTitulos(Titulo titulo) throws SQLException {
        String sql = "INSERT INTO titulo (id, quantidade, paga, departamento, email_cliente) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, titulo.getId());
            stmt.setDouble(2, titulo.getQuantidade());
            stmt.setBoolean(3, titulo.isPago());
            stmt.setString(4, titulo.getDepartamento());
            stmt.setString(5, titulo.getEmailCliente()); // Salva o email do cliente
            stmt.executeUpdate();
        }
    }
    // Adicione os m�todos a seguir
    public String listarProdutosString() {
        return Produto.listarProdutosString(produtosMap);
    }

    public String listarTitulosString() {
        return Titulo.listarTitulosString(titulosMap);
    }

    public String listarDepartamentosString() {
        return Departamento.listarDepartamentosString();
    }
}
