package erp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Produto {
    private String id;
    private String nome;
    private double preco;
    private String departamento;

    private static Map<String, Produto> produtosMap = new HashMap<>();

    public Produto(String id, String nome, double preco, String departamento) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.departamento = departamento;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public double getPreco() {
        return preco;
    }

    public String getDepartamento() {
        return departamento;
    }

    @Override
    public String toString() {
        return id + "," + nome + "," + preco + "," + departamento;
    }

    // M�todo para salvar o produto no banco de dados
    public void saveToDatabase() throws SQLException {
        String sql = "INSERT INTO produto (id, nome, preco, departamento) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE nome = ?, preco = ?, departamento = ?";
        
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.id);
            pstmt.setString(2, this.nome);
            pstmt.setDouble(3, this.preco);
            pstmt.setString(4, this.departamento);
            pstmt.setString(5, this.nome); // Para atualizar
            pstmt.setDouble(6, this.preco); // Para atualizar
            pstmt.setString(7, this.departamento); // Para atualizar
            pstmt.executeUpdate();
        }
    } 

    // M�todo para carregar todos os produtos do banco de dados
    public static void loadAllFromDatabase(Loja loja) throws SQLException {
        String sql = "SELECT id, nome, preco, departamento FROM produto";
        
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql); 
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("id");
                String nome = rs.getString("nome");
                double preco = rs.getDouble("preco");
                String departamento = rs.getString("departamento");
                Produto produto = new Produto(id, nome, preco, departamento);
                loja.addProduto(produto); // Adiciona o produto � loja
            }
        }
    }

    // M�todo para salvar todos os produtos da loja no banco de dados
    public static void saveProdutos(Loja loja) throws SQLException {
        for (Produto produto : loja.produtosMap.values()) {
            produto.saveToDatabase(); // Salva cada produto individualmente
        }

        // Detalhamento da opera��o para o blockchain
        StringBuilder logBuilder = new StringBuilder();
        for (Produto produto : loja.produtosMap.values()) {
            logBuilder.append("ID: ").append(produto.getId())
                      .append(", Nome: ").append(produto.getNome())
                      .append(", Pre�o: ").append(produto.getPreco())
                      .append(", Departamento: ").append(produto.getDepartamento())
                      .append(";\n");
        }

        String logDetails = logBuilder.toString();
        String descricao = "Produtos salvos na loja. Detalhes:\n" + logDetails;
        SimuladorBlockchain.criarContrato(descricao);
    }

    // M�todo para carregar produtos da loja
    public static void carregaProdutos(Loja loja) throws SQLException {
        // Limpa a loja antes de carregar novos produtos
        loja.clearProdutos(); // Corrigido para usar o m�todo existente
        loadAllFromDatabase(loja); // Carrega todos os produtos do banco de dados
    }
    
    public static String listarProdutosString(Map<String, Produto> produto) {
        StringBuilder sb = new StringBuilder();
        for (Produto produto1 : produto.values()) {
            sb.append(produto1.getId()).append(" - ").append(produto1.getNome()).append(" - R$ ").append(produto1.getPreco()).append("\n");
        }
        return sb.toString();
    }

}
