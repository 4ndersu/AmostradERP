package erp;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Avaliacao {
    private static List<Integer> notas = new ArrayList<>(); // Lista para armazenar as notas localmente
    private static List<String> sugestoes = new ArrayList<>(); // Lista para armazenar as sugestões localmente
    private static ResourceBundle mensagens; // Para armazenar as mensagens traduzidas

    // Carregar as sugestões e notas do banco ao iniciar o sistema
    static {
        mensagens = ResourceBundle.getBundle("messages"); // Carregar mensagens do arquivo properties
        carregarSugestoes();
        carregarNotas();
    }

    public static void exibirMenuAvaliacao() {
        String[] opcoes = {mensagens.getString("menu.option1"), mensagens.getString("menu.option2"),
                mensagens.getString("menu.option3"), mensagens.getString("menu.option4")};

        int opcao;
        do {
            opcao = JOptionPane.showOptionDialog(null, mensagens.getString("menu.title"),
                    mensagens.getString("menu.title"), JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null, opcoes, opcoes[0]);

            switch (opcao) {
                case 0:
                    avaliarSistema(); // Chama o método para avaliar o sistema
                    break;
                case 1:
                    exibirAvaliacoes(); // Chama o método para exibir avaliações
                    break;
                case 2:
                    exibirSugestoes(); // Chama o método para exibir sugestões
                    break;
                case 3:
                    JOptionPane.showMessageDialog(null, mensagens.getString("menu.exit"));
                    break;
                default:
                    JOptionPane.showMessageDialog(null, mensagens.getString("menu.invalid_option"));
            }
        } while (opcao != 3);
    }

    // Método para capturar a avaliação do usuário
    public static void avaliarSistema() {
        int nota = 0;

        // Solicitar a nota ao usuário
        while (nota < 1 || nota > 10) {
            String input = JOptionPane.showInputDialog(mensagens.getString("evaluation.request_rating"));
            if (input != null) { // Verifica se o usuário não cancelou
                try {
                    nota = Integer.parseInt(input);
                    if (nota < 1 || nota > 10) {
                        JOptionPane.showMessageDialog(null, mensagens.getString("evaluation.invalid_rating"));
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, mensagens.getString("evaluation.invalid_rating"));
                }
            } else {
                return; // Se o usuário cancelar a entrada, sai do método
            }
        }

        // Adicionar a nota à lista de avaliações
        notas.add(nota);
        
        // Salvar a nota no banco de dados
        salvarNota(nota);

        // Mostrar mensagem com base na avaliação
        if (nota >= 1 && nota <= 3) {
            JOptionPane.showMessageDialog(null, mensagens.getString("evaluation.negative_feedback"));
        } else if (nota >= 4 && nota <= 7) {
            JOptionPane.showMessageDialog(null, mensagens.getString("evaluation.neutral_feedback"));
        } else {
            JOptionPane.showMessageDialog(null, mensagens.getString("evaluation.positive_feedback"));
        }

        // Perguntar se o usuário tem sugestões imediatamente após a avaliação
        capturarSugestao();
    }

    // Método para capturar a sugestão do usuário
    private static void capturarSugestao() {
        int resposta = JOptionPane.showConfirmDialog(null, mensagens.getString("suggestion.question"),
                mensagens.getString("suggestion.record"), JOptionPane.YES_NO_OPTION);
        
        if (resposta == JOptionPane.YES_OPTION) {
            String sugestao = JOptionPane.showInputDialog(mensagens.getString("suggestion.instructions"));
            if (sugestao != null && !sugestao.trim().isEmpty()) {
                sugestoes.add(sugestao);
                salvarSugestao(sugestao); // Salva a sugestão no banco de dados
                JOptionPane.showMessageDialog(null, mensagens.getString("suggestion.thank_you"));
            }
        } else {
            JOptionPane.showMessageDialog(null, mensagens.getString("suggestion.thank_you_no"));
        }
    }

    // Método para salvar sugestões no banco de dados
    private static void salvarSugestao(String sugestao) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO sugestoes (sugestao) VALUES (?)")) { // Corrigido aqui
            stmt.setString(1, sugestao);
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, mensagens.getString("error.save_suggestion") + e.getMessage());
        }
    }

    // Método para carregar as sugestões do banco de dados
    private static void carregarSugestoes() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT sugestao FROM sugestoes")) { // Ajuste aqui
            while (rs.next()) {
                sugestoes.add(rs.getString("sugestao")); // Mudei "texto" para "sugestao"
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, mensagens.getString("error.load_suggestions") + e.getMessage());
        }
    }

    // Método para salvar notas no banco de dados
    private static void salvarNota(int nota) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO avaliacoes (nota) VALUES (?)")) {
            stmt.setInt(1, nota);
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, mensagens.getString("error.save_rating") + e.getMessage());
        }
    }

    // Método para carregar as notas do banco de dados
    private static void carregarNotas() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nota FROM avaliacoes")) {
            while (rs.next()) {
                notas.add(rs.getInt("nota")); // Adiciona a nota à lista
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, mensagens.getString("error.load_ratings") + e.getMessage());
        }
    }

    // Método para exibir todas as avaliações armazenadas
    public static void exibirAvaliacoes() {
        if (notas.isEmpty()) {
            JOptionPane.showMessageDialog(null, mensagens.getString("evaluation.no_records"));
        } else {
            StringBuilder sb = new StringBuilder(mensagens.getString("evaluation.records") + "\n");
            for (int i = 0; i < notas.size(); i++) {
                sb.append(mensagens.getString("evaluation.record") + (i + 1) + ": Rating " + notas.get(i) + "\n");
            }
            // Exibir estatísticas adicionais, como média
            double media = calcularMedia();
            sb.append(mensagens.getString("evaluation.average") + media);
            JOptionPane.showMessageDialog(null, sb.toString());
        }
    }

    // Método para calcular a média das avaliações
    private static double calcularMedia() {
        if (notas.isEmpty()) {
            return 0;
        }

        int soma = 0;
        for (int nota : notas) {
            soma += nota;
        }

        return (double) soma / notas.size();
    }

    // Método para exibir as sugestões registradas
    public static void exibirSugestoes() {
        if (sugestoes.isEmpty()) {
            JOptionPane.showMessageDialog(null, mensagens.getString("suggestion.no_records"));
        } else {
            StringBuilder sb = new StringBuilder(mensagens.getString("suggestion.records") + "\n");
            for (int i = 0; i < sugestoes.size(); i++) {
                sb.append(mensagens.getString("suggestion.record") + (i + 1) + ": " + sugestoes.get(i) + "\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        }
    }
}
