package erp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Gerente extends Funcionario {
    public Gerente(String id, String nome, double salario, String email, String senha, String lojaId) {
        super(id, nome, "Gerente", salario, email, senha, lojaId);
    }

    @Override
    public boolean podeCadastrarProduto() {
        return true;
    }

    @Override
    public boolean podeCadastrarDepartamento() {
        return true;
    }

    @Override
    public boolean podeCadastrarCliente() {
        return true;
    }

    @Override
    public boolean podeVenderProduto() {
        return true;
    }

    @Override
    public boolean podeVerRelatorios() {
        return true;
    }

 // Método para gerenciar a assinatura
    public void gerenciarAssinatura(Loja loja, Scanner scanner, ResourceBundle messages) {
        System.out.println(messages.getString("currentPlan") + loja.getPlanoAtual());
        System.out.println(messages.getString("chooseNewPlan"));
        System.out.println(messages.getString("plan_basic"));
        System.out.println(messages.getString("plan_premium"));
        System.out.println(messages.getString("plan_deluxe"));

        String input = scanner.nextLine(); // Lê a entrada como String
        int escolha;

        try {
            escolha = Integer.parseInt(input); // Tenta converter para inteiro
        } catch (NumberFormatException e) {
            System.out.println(messages.getString("invalidOption"));
            return;
        }

        String novoPlano;
        switch (escolha) {
            case 1:
                novoPlano = "Básico";
                break;
            case 2:
                novoPlano = "Premium";
                break;
            case 3:
                novoPlano = "Deluxe";
                break;
            default:
                System.out.println(messages.getString("invalidOption"));
                return;
        }

        // Atualiza o plano atual da loja
        loja.setPlanoAtual(novoPlano);

        // Atualiza o plano no banco de dados
        atualizarPlanoNoBanco(loja.getId(), novoPlano); // Assumindo que Loja tem um método getId()

        System.out.println(messages.getString("planUpdatedSuccess"));
    }

    // Método para atualizar o plano da loja no banco de dados
    private void atualizarPlanoNoBanco(String lojaId, String novoPlano) {
        String sql = "UPDATE loja SET planoAtual = ? WHERE id = ?"; // Substitua 'lojas' e 'plano_atual' conforme necessário

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, novoPlano);
            stmt.setString(2, lojaId);
            stmt.executeUpdate();
            
            // Log de depuração
            System.out.println("Plano atualizado no banco de dados: Loja ID: " + lojaId + ", Novo Plano: " + novoPlano);
            
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar o plano no banco de dados: " + e.getMessage());
        }
    }    

}

