package erp;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

public class SimuladorBlockchain {
    static ResourceBundle messages = ResourceBundle.getBundle("messages", Locale.getDefault());

    public static void executarSimulacaoBlockchain() {
        String[] options = { messages.getString("create_contract"), 
                             messages.getString("view_contracts"), 
                             messages.getString("deactivate_contract"), 
                             messages.getString("exit") };

        while (true) {
            int opcao = JOptionPane.showOptionDialog(null,
                    messages.getString("blockchain_simulator"),
                    messages.getString("blockchain"),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (opcao == -1 || opcao == 3) {
                break; // Sai se a opção for fechar ou "exit"
            }

            switch (opcao) {
                case 0:
                    String descricao = JOptionPane.showInputDialog(messages.getString("contract_description"));
                    criarContrato(descricao);
                    break;
                case 1:
                    visualizarContratos();
                    break;
                case 2:
                    int id = Integer.parseInt(JOptionPane.showInputDialog(messages.getString("contract_id_deactivate")));
                    desativarContrato(id);
                    break;
            }
        }
    }

    static void criarContrato(String descricao) {
        String sql = "INSERT INTO contratos (descricao, status) VALUES (?, 'ativo')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, descricao);
            pstmt.executeUpdate();
            System.out.println(messages.getString("contract_created_successfully"));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, messages.getString("error_creating_contract") + e.getMessage());
        }
    }

    public static void visualizarContratos() {
        String sql = "SELECT * FROM contratos";
        StringBuilder contratos = new StringBuilder();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String descricao = rs.getString("descricao");
                String status = rs.getString("status");

                contratos.append(messages.getString("contract_id") + id + ", " +
                                 messages.getString("contract_description") + descricao + ", " +
                                 messages.getString("contract_status") + status + "\n");
            }

            JOptionPane.showMessageDialog(null, contratos.toString());

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, messages.getString("error_visualizing_contracts") + e.getMessage());
        }
    }

    public static void desativarContrato(int id) {
        String sql = "UPDATE contratos SET status = 'inativo' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, messages.getString("contract_deactivated_successfully"));
            } else {
                JOptionPane.showMessageDialog(null, messages.getString("contract_not_found") + id);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, messages.getString("error_deactivating_contract") + e.getMessage());
        }
    }
}
