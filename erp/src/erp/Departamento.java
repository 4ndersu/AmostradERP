package erp;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Departamento {
    private String id;
    private String nome;

    private static Map<String, Departamento> departamentos = new HashMap<>();
    // Carregar o ResourceBundle para mensagens de erro
    private static ResourceBundle messages = ResourceBundle.getBundle("messages", new Locale("pt", "BR"));

    public Departamento(String id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public static void addDepartamento(String id, String nome) {
        // Verifica se o departamento j� existe
        if (buscarDepartamentoPorId(id) != null) {
            System.err.println(messages.getString("department_id_exists")); // Mensagem informando que o ID j� existe
            return; // Sai do m�todo se o departamento j� existir
        }

        Departamento departamento = new Departamento(id, nome);
        departamentos.put(id, departamento);

        // Salvar no banco de dados
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Departamento (id, nome) VALUES (?, ?) ON DUPLICATE KEY UPDATE nome = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, id);
                statement.setString(2, nome);
                statement.setString(3, nome); // Atualiza se j� existir
                statement.executeUpdate();
                System.out.println("Departamento adicionado ao banco de dados com sucesso!");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar departamento ao banco de dados: " + e.getMessage());
        }

        // Registrar a cria��o no blockchain
        String descricao = String.format("Departamento criado: ID=%s, Nome=%s", id, nome);
        SimuladorBlockchain.criarContrato(descricao);
    }

    public static Departamento buscarDepartamentoPorId(String id) {
        return departamentos.get(id);
    }

    public static Collection<Departamento> listarDepartamentos() {
        return departamentos.values();
    }

    @Override
    public String toString() {
        return id + "," + nome;
    }

    public static Departamento fromString(String str) {
        String[] parts = str.split(",");
        if (parts.length == 2) {
            return new Departamento(parts[0], parts[1]);
        }
        return null;
    }

    // Salvar e carregar departamentos com criptografia
    public static void saveDepartamentos() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Departamentos (id, nome) VALUES (?, ?) ON DUPLICATE KEY UPDATE nome = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Departamento departamento : listarDepartamentos()) {
                    stmt.setString(1, departamento.getId());
                    stmt.setString(2, departamento.getNome());
                    stmt.setString(3, departamento.getNome()); // Para atualizar se o ID j� existir
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println(messages.getString("error.save_departments") + ": " + e.getMessage());
        }
    }


    public static void carregaDepartamentos() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, nome FROM Departamentos";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String id = rs.getString("id");
                    String nome = rs.getString("nome");
                    addDepartamento(id, nome); // Adicionar o departamento carregado � lista
                }
            }
        } catch (SQLException e) {
            System.err.println(messages.getString("error.load_departments") + ": " + e.getMessage());
        }
    }
    
    public static String listarDepartamentosString() {
        StringBuilder sb = new StringBuilder();
        for (Departamento departamento : departamentos.values()) {
            sb.append(departamento.getId()).append(" - ").append(departamento.getNome()).append("\n");
        }
        return sb.toString();
        
    }
}

