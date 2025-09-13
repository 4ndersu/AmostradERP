package erp;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Locale;
import java.util.ResourceBundle;


public class Loja {
	
	 private static ResourceBundle messages;

	    static {
	        Locale locale = Locale.getDefault(); // Pode ser ajustado conforme necessário
	        messages = ResourceBundle.getBundle("messages", locale); // Carrega o ResourceBundle
	    }
	    
    private String id;
    private String nome;
 //   private String plano; // Novo campo para o plano de assinatura
    private String planoAtual;
    private Map<String, Funcionario> funcionariosMap = new HashMap<>();
    Map<String, Produto> produtosMap = new HashMap<>();
    Map<String, Titulo> titulosMap = new HashMap<>();

    private static final String SECRET_KEY = "1234567890123456"; // 16 bytes secret key for AES
    //private static final String LOJA_ARQUIVO = "loja.enc"; // File to store encrypted store data
    
    Connection conn = DatabaseConnection.getConnection();


    public Loja(String id) {
        this.id = id;
        this.nome = ""; // ou um valor padrão
        this.planoAtual = ""; // ou um valor padrão
        this.funcionariosMap = new HashMap<>();
        this.produtosMap = new HashMap<>();
        this.titulosMap = new HashMap<>();
    }

    
    public Loja(String id, String nome, String plano) {
        this.id = id;
        this.nome = nome;
        this.planoAtual = plano;
        this.funcionariosMap = new HashMap<>();
        this.produtosMap = new HashMap<>();
        this.titulosMap = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

 /*   public String getPlano() {
        return plano;
    }

    public void setPlano(String plano) {
        this.plano = plano;
    }
*/    
    public String getPlanoAtual() {
        return planoAtual;
    }

    public void setPlanoAtual(String planoAtual) {
        this.planoAtual = planoAtual;
    }

    public void addFuncionario(Funcionario funcionario) {
        funcionariosMap.put(funcionario.getId(), funcionario);
    }

    public Funcionario getFuncionario(String id) {
        return funcionariosMap.get(id);
    }

    public Produto getProduto(String id) {
        return produtosMap.get(id);
    }

    public void addProduto(Produto produto) {
        produtosMap.put(produto.getId(), produto);
    }

    public void addTitulo(Titulo titulo) {
        titulosMap.put(titulo.getId(), titulo);
    }
    
    public void clearProdutos() {
        produtosMap.clear(); // Limpa o mapa de produtos
    }
    
    public void clearTitulos() {
        titulosMap.clear(); // Limpa o mapa de títulos
    }


    // Métodos para salvar e carregar informações da loja
    public void saveAll() throws IOException, SQLException {
    	 try (Connection connection = DatabaseConnection.getConnection()) {
             // Salvar informações da loja
             String insertLojaSQL = "INSERT INTO loja (id, nome, planoAtual) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE nome = ?, planoAtual = ?";
             try (PreparedStatement pstmt = connection.prepareStatement(insertLojaSQL)) {
                 pstmt.setString(1, id);
                 pstmt.setString(2, nome);
                 pstmt.setString(3, planoAtual);
                 pstmt.setString(4, nome);
                 pstmt.setString(5, planoAtual);
                 pstmt.executeUpdate();
             }

             // Salvar funcionários
             String insertFuncionarioSQL = "INSERT INTO funcionarios (id, nome, cargo, salario, email, senha, id) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE nome = ?, cargo = ?, salario = ?, email = ?, senha = ?";
             for (Funcionario func : funcionariosMap.values()) {
                 try (PreparedStatement pstmt = connection.prepareStatement(insertFuncionarioSQL)) {
                     pstmt.setString(1, func.getId());
                     pstmt.setString(2, func.getNome());
                     pstmt.setString(3, func.getCargo());
                     pstmt.setDouble(4, func.getSalario());
                     pstmt.setString(5, func.getEmail());
                     pstmt.setString(6, func.getSenha());
                     pstmt.setString(7, id);
                     pstmt.setString(8, func.getNome());
                     pstmt.setString(9, func.getCargo());
                     pstmt.setDouble(10, func.getSalario());
                     pstmt.setString(11, func.getEmail());
                     pstmt.setString(12, func.getSenha());
                     pstmt.executeUpdate();
                 }
             }

             // Você pode adicionar aqui o código para salvar produtos e títulos, da mesma forma que foi feito para funcionários.
         } catch (SQLException e) {
             throw new SQLException("Erro ao salvar informações da loja.", e);
         }
     
    }


    public Loja loadAll() throws IOException, SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Carregar informações da loja
            String sql = "SELECT * FROM loja WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    this.nome = rs.getString("nome");
                    this.planoAtual = rs.getString("planoAtual");

                    // Carregar funcionários
                    String selectFuncionariosSQL = "SELECT * FROM funcionarios WHERE lojaId = ?"; // Use lojaId para filtrar corretamente
                    try (PreparedStatement pstmt1 = connection.prepareStatement(selectFuncionariosSQL)) {
                        pstmt1.setString(1, id);
                        try (ResultSet rsFuncionario = pstmt1.executeQuery()) {
                            while (rsFuncionario.next()) {
                                String idFuncionario = rsFuncionario.getString("id");
                                String nomeFuncionario = rsFuncionario.getString("nome");
                                String cargoFuncionario = rsFuncionario.getString("cargo");
                                double salarioFuncionario = rsFuncionario.getDouble("salario");
                                String emailFuncionario = rsFuncionario.getString("email");
                                String senhaFuncionario = rsFuncionario.getString("senha");

                                Funcionario funcionario = new Funcionario(idFuncionario, nomeFuncionario, cargoFuncionario, salarioFuncionario, emailFuncionario, senhaFuncionario, id);
                                funcionariosMap.put(funcionario.getId(), funcionario);
                            }
                        }
                    }

                    // Carregar produtos
                    String sqlProdutos = "SELECT * FROM produto WHERE lojaId = ?";
                    try (PreparedStatement stmtProdutos = connection.prepareStatement(sqlProdutos)) {
                        stmtProdutos.setString(1, id);
                        ResultSet rsProdutos = stmtProdutos.executeQuery();
                        while (rsProdutos.next()) {
                            Produto produto = new Produto(
                                    rsProdutos.getString("id"),
                                    rsProdutos.getString("nome"),
                                    rsProdutos.getDouble("preco"),
                                    rsProdutos.getString("departamento")
                            );
                            produtosMap.put(produto.getId(), produto);
                        }
                    }

                    // Carregar títulos
                    String sqlTitulo = "SELECT * FROM titulo WHERE lojaId = ?"; // Presumindo que você ainda quer filtrar pelo ID da loja
                    try (PreparedStatement stmtTitulo = connection.prepareStatement(sqlTitulo)) {
                        stmtTitulo.setString(1, id);
                        ResultSet rsTitulo = stmtTitulo.executeQuery();
                        while (rsTitulo.next()) {
                            Titulo titulo = new Titulo(
                                    rsTitulo.getString("id"),
                                    rsTitulo.getDouble("quantidade"),
                                    rsTitulo.getBoolean("paga"),
                                    rsTitulo.getString("departamento"),
                                    rsTitulo.getString("email_cliente") // Novo parâmetro: e-mail do cliente
                            );
                            titulosMap.put(titulo.getId(), titulo);
                        }
                    }
              

                    return this; // Retorne a instância atual
                } else {
                    System.out.println("Loja não encontrada com o ID: " + id);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Para obter detalhes da exceção
            throw new SQLException("Erro ao carregar informações da loja.", e);
        }
        return null; // Retorna null se não encontrar a loja
    }



    // Criptografia e descriptografia
    private static Cipher getCipher(int mode) throws Exception {
        Key key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, key);
        return cipher;
    }

    private static String encrypt(String data) throws Exception {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private static String decrypt(String data) throws Exception {
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
        byte[] decoded = Base64.getDecoder().decode(data);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }

 // Método para cadastrar uma nova loja ou entrar em uma existente
    public static Loja cadastrarOuEntrarLoja() throws SQLException, IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println(messages.getString("enter_store_id"));
        String id = scanner.nextLine();

        Loja loja = new Loja(id, "", ""); // Inicializa a loja com o ID

        // Carrega informações da loja do banco de dados
        loja.loadAll(); // Carrega as informações da loja do banco de dados

        // Verifica se a loja foi encontrada
        if (loja.getNome() != null && !loja.getNome().isEmpty()) {
            System.out.println(messages.getString("entered_existing_store") + loja.getNome());
            return loja;
        } else {
            System.out.println(messages.getString("store_id_not_found"));
            String resposta = scanner.nextLine();
            if (!resposta.equalsIgnoreCase("S") && !resposta.equalsIgnoreCase("Y")) {
                System.out.println(messages.getString("operation_cancelled"));
                return null;
            }
        }

        // Cria nova loja
        System.out.println(messages.getString("enter_store_name"));
        String nome = scanner.nextLine();

        System.out.println(messages.getString("choose_store_plan"));
        String planoAtual;
        int escolhaPlano = scanner.nextInt();
        scanner.nextLine(); // Consumir nova linha

        switch (escolhaPlano) {
            case 1:
                planoAtual = "Básico";
                break;
            case 2:
            	planoAtual = "Premium";
                break;
            case 3:
            	planoAtual = "Deluxe";
                break;
            default:
                System.out.println(messages.getString("invalid_plan"));
                planoAtual = "Básico";
                break;
        }

        loja = new Loja(id, nome, planoAtual);
        loja.saveAll(); // Salva a nova loja no banco de dados
        System.out.println(messages.getString("new_store_created"));
        return loja;
    }

        
    

    // Método para verificar se uma funcionalidade está disponível para o plano atual
    public boolean isFuncionalidadeDisponivel(String funcionalidade) {
        switch (planoAtual) {
            case "Deluxe":
                return true; // Todos os recursos estão disponíveis no plano Deluxe
            case "Premium":
                return !funcionalidade.equals("listarLogsOperacoes"); // Função de registro de logs não disponível no plano Premium
            case "Básico":
                return funcionalidade.equals("CadastroProduto") || funcionalidade.equals("VisualizacaoProduto") ||
                        funcionalidade.equals("VendaProduto") || funcionalidade.equals("CadastroCliente") ||
                        funcionalidade.equals("VisualizacaoCliente") || funcionalidade.equals("CadastroFuncionario") || 
                        funcionalidade.equals("ListagemFuncionarios") || funcionalidade.equals("fazPagamento") || 
                        funcionalidade.equals("listarTitulosAberto")||funcionalidade.equals("CadastroDepartamento");
            default:
                return false; // Nenhuma funcionalidade disponível se o plano não for reconhecido
        }
    }
}
