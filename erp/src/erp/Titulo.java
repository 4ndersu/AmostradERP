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
import java.util.Objects;
import java.util.Locale;
import java.util.ResourceBundle;

public class Titulo {
    static String id;
    static double quantidade;
    static boolean paga;
    static String departamento;
    static String emailCliente; // Novo campo para armazenar o ID do cliente

    private static Map<String, Titulo> titulosMap = new HashMap<>();
    private static final String SECRET_KEY = "1234567890123456"; // 16 bytes secret key for AES

    private static ResourceBundle messages;

    static {
        // Carrega o ResourceBundle com a locale desejada
        Locale currentLocale = new Locale("pt", "BR"); // Altere para a locale desejada
        messages = ResourceBundle.getBundle("messages", currentLocale);
    }

    public Titulo(String id, double quantidade, boolean paga, String departamento, String emailCliente) {
        this.id = id;
        this.quantidade = quantidade;
        this.paga = paga;
        this.departamento = encrypt(departamento); // Criptografa o departamento
        this.emailCliente = emailCliente;  // Armazena o ID do cliente
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public boolean isPago() {
        return paga;
    }

    public void setPaga(boolean paga) {
        this.paga = paga;
    }

    public String getDepartamento() {
        try {
            return decrypt(departamento); // Descriptografa o departamento ao ser acessado
        } catch (Exception e) {
            System.err.println("Erro ao descriptografar departamento: " + e.getMessage());
            return null; // Ou trate-o de acordo
        }
    }

    public String getEmailCliente() {
        return emailCliente; // Adiciona m�todo para retornar o email do cliente
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Titulo titulo = (Titulo) obj;
        return this.id.equals(titulo.id); // Compara IDs
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Gera hash baseado no ID
    }

    @Override
    public String toString() {
        return id + "," + quantidade + "," + paga + "," + departamento + "," + emailCliente; // Inclui clienteId
    }

    public static Titulo fromString(String str) {
        String[] partes = str.split(",", -1);
        if (partes.length == 5) { // Atualiza o tamanho para incluir clienteId
            String id = partes[0];
            double quantidade;
            boolean paga;
            String departamento = partes[3];
            String clienteId = partes[4]; // Obt�m o clienteId

            try {
                quantidade = Double.parseDouble(partes[1]);
            } catch (NumberFormatException e) {
                System.err.println(messages.getString("invalid_quantity_format") + " ID: " + id + ". Valor fornecido: " + partes[1]);
                return null;
            }

            try {
                paga = Boolean.parseBoolean(partes[2]);
            } catch (Exception e) {
                System.err.println(messages.getString("invalid_boolean_format") + " ID: " + id + ". Valor fornecido: " + partes[2]);
                return null;
            }

            // Decrypting the departamento before creating the Titulo instance
            try {
                departamento = decrypt(departamento); // Descriptografa o departamento
            } catch (Exception e) {
                System.err.println(messages.getString("error_decrypting_department") + " ID: " + id);
                return null;
            }

            return new Titulo(id, quantidade, paga, departamento, clienteId); // Inclui clienteId
        } else {
            System.err.println(messages.getString("invalid_title_format") + ": " + str);
        }
        return null;
    }

    // Criptografia e descriptografia
    private static Cipher getCipher(int mode) throws Exception {
        Key key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, key);
        return cipher;
    }

 // M�todo para criptografar
    public static String encrypt(String data) {
        try {
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted); // Codifica para Base64
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // M�todo para descriptografar
    public static String decrypt(String data) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(data); // Decodifica Base64
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }


    // M�todos para salvar e carregar t�tulos no banco de dados
    public void salvarNoBanco(Connection connection) throws SQLException {
        String sql = "INSERT INTO titulo (id, quantidade, paga, departamento, cliente_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setDouble(2, quantidade);
            stmt.setBoolean(3, paga);
            stmt.setString(4, departamento); // O departamento j� est� criptografado
            stmt.setString(5, emailCliente); // Adiciona clienteId
            stmt.executeUpdate();
        }
    }

    public static Titulo carregarDoBanco(Connection connection, String id) throws SQLException, Exception {
        String sql = "SELECT * FROM titulo WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String deptoCriptografado = rs.getString("departamento");
                String departamentoDescriptografado = decrypt(deptoCriptografado); // Descriptografa o departamento
                String clienteId = rs.getString("cliente_id"); // Obt�m o clienteId

                return new Titulo(
                    rs.getString("id"),
                    rs.getDouble("quantidade"),
                    rs.getBoolean("paga"),
                    departamentoDescriptografado,
                    clienteId // Passa clienteId
                );
            }
        }
        return null; // Retorna null se n�o encontrar
    }
    
    public static String listarTitulosString(Map<String, Titulo> titulosMap) {
        StringBuilder sb = new StringBuilder();
        for (Titulo titulo : titulosMap.values()) {
            sb.append(titulo.getId()).append(" - ").append(titulo.getQuantidade()).append(" - ").append(titulo.isPago() ? "Pago" : "N�o Pago").append("\n");
        }
        return sb.toString();
    }
}
