package erp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.awt.BorderLayout;
import java.io.*;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class Cliente {
    private String nome;
    private String telefone;
    private String email;
    private List<Titulo> compras;

    private static Map<String, Cliente> clientesMap = new HashMap<>();
    static final String CLIENTES_ARQUIVO = "clientes.enc";
    private static final String SECRET_KEY = "1234567890123456"; // Chave secreta de 16 bytes (128 bits)

    // Adicionando ResourceBundle para tradu��o
    private static ResourceBundle messages = ResourceBundle.getBundle("messages", new Locale("pt", "BR"));

    public Cliente(String nome, String telefone, String email) {
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.compras = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    } 

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Titulo> getCompras() {
        return compras;
    }

    public void setCompras(List<Titulo> compras) {
        this.compras = compras;
    }
  
    public void addCompra(Titulo titulo) {
        if (compras == null) {
            compras = new ArrayList<>();
        }
        compras.add(titulo);
        // Log ao adicionar t�tulo
        System.out.println(messages.getString("title_added") + ": " + titulo); // Log ao adicionar t�tulo
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(nome).append(",").append(telefone).append(",").append(email);
        if (compras != null && !compras.isEmpty()) {
            sb.append(",").append(compras.size());
            for (Titulo t : compras) {
                sb.append(",").append(t.toString());
            }
        } else {
            sb.append(",0");
        }
        return sb.toString();
    }

    // M�todo modificado para salvar clientes criptografados no banco de dados MySQL
    public static void adicionarCliente(String nome, String telefone, String email) {
        // Use diálogo gráfico em vez de Scanner para aplicações GUI
        int resposta = JOptionPane.showConfirmDialog(null, 
                          messages.getString("privacy_policy_body"), 
                          messages.getString("privacy_policy_header"), 
                          JOptionPane.YES_NO_OPTION);
        
        if (resposta != JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(null, messages.getString("registration_cancelled"));
            return;
        }

        Cliente cliente = new Cliente(nome, telefone, email);

        if (clientesMap.containsKey(email)) {
            JOptionPane.showMessageDialog(null, messages.getString("client_already_registered"));
        } else {
            clientesMap.put(email, cliente);

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try (Connection conn = DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/erp", "root", "@Edu060705");
                         PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO cliente (nome, telefone, email, compras) VALUES (?, ?, ?, ?)")) {

                        // Criptografa os dados do cliente antes de enviar ao banco de dados
                        String nomeCriptografado = CryptoUtils.encrypt(cliente.getNome(), SECRET_KEY);
                        String telefoneCriptografado = CryptoUtils.encrypt(cliente.getTelefone(), SECRET_KEY);
                        String emailCriptografado = CryptoUtils.encrypt(cliente.getEmail(), SECRET_KEY);
                        String comprasCriptografadas = CryptoUtils.encrypt(cliente.getCompras().toString(), SECRET_KEY);

                        // Define os valores para inserção
                        stmt.setString(1, nomeCriptografado);
                        stmt.setString(2, telefoneCriptografado);
                        stmt.setString(3, emailCriptografado);
                        stmt.setString(4, comprasCriptografadas);
                        stmt.executeUpdate();

                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(messages.getString("error_saving_to_database"), e);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // Verifica se houve exceção
                        JOptionPane.showMessageDialog(null, messages.getString("client_added_successfully"));
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, e.getMessage(), 
                                                      messages.getString("error"), 
                                                      JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute(); // Executa o SwingWorker para não bloquear a GUI
        }
    }


    public static Map<Integer, Cliente> listarClientes() {
        Map<Integer, Cliente> clientes = new HashMap<>();
        StringBuilder clientesInfo = new StringBuilder();

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/erp", "root", "@Edu060705");
            String sql = "SELECT id, nome, telefone, email FROM cliente"; // Remover compras da consulta
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Recupera os dados do cliente
                int id = rs.getInt("id");
                String nomeCriptografado = rs.getString("nome");
                String telefoneCriptografado = rs.getString("telefone");
                String emailCriptografado = rs.getString("email");

                // Descriptografa os dados
                String nomeDescriptografado = CryptoUtils.decrypt(nomeCriptografado, SECRET_KEY);
                String telefoneDescriptografado = CryptoUtils.decrypt(telefoneCriptografado, SECRET_KEY);
                String emailDescriptografado = CryptoUtils.decrypt(emailCriptografado, SECRET_KEY);

                // Cria um novo objeto Cliente e adiciona ao mapa usando o ID como chave
                Cliente cliente = new Cliente(nomeDescriptografado, telefoneDescriptografado, emailDescriptografado);
                clientes.put(id, cliente); // Usando o ID como chave

                // Formata as informações do cliente para exibição no JTextArea
                clientesInfo.append("ID: ").append(id).append("\n");
                clientesInfo.append("Nome: ").append(nomeDescriptografado).append("\n");
                clientesInfo.append("Telefone: ").append(telefoneDescriptografado).append("\n");
                clientesInfo.append("Email: ").append(emailDescriptografado).append("\n");
                clientesInfo.append("Compras:\n");
                clientesInfo.append(listarTitulosPorCliente(conn, emailDescriptografado)).append("\n");
                clientesInfo.append("----------------------------------\n");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(messages.getString("error_loading_from_database"));
        }

        // Exibe as informações dos clientes no GUI
        mostrarClientesGUI(clientesInfo.toString());
        return clientes; // Retorna o mapa de clientes
    }


    
    private static void mostrarClientesGUI(String clientesInfo) {
        JFrame frame = new JFrame("Lista de Clientes");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 400);

        JTextArea textArea = new JTextArea();
        textArea.setText(clientesInfo);
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }
    private static String listarTitulosPorCliente(Connection conn, String emailCliente) {
        StringBuilder titulosInfo = new StringBuilder();
        try {
            String sql = "SELECT * FROM titulo WHERE email_cliente = ?"; // Ajuste conforme seu banco de dados
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, emailCliente); // Usar emailCliente em vez de cliente_id
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tituloId = rs.getString("id"); // Ajuste conforme sua estrutura de tabela
                double quantidade = rs.getDouble("quantidade");
                boolean paga = rs.getBoolean("paga");
                String departamento = rs.getString("departamento"); // Sem descriptografia

                // Adiciona os detalhes do título ao StringBuilder
                titulosInfo.append("Título ID: ").append(tituloId)
                            .append(", Quantidade: ").append(quantidade)
                            .append(", Paga: ").append(paga)
                            .append(", Departamento: ").append(departamento)
                            .append("\n");
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("Erro ao listar títulos: " + e.getMessage());
        }
        
        return titulosInfo.toString(); // Retorna os detalhes dos títulos como String
    }


    public static Cliente recuperarClientePorEmail(String emailCriptografado) {
        Cliente cliente = null;
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/erp", "root", "@Edu060705");
            String sql = "SELECT nome, telefone, email, compras FROM cliente WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, emailCriptografado);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nomeCriptografado = rs.getString("nome");
                String telefoneCriptografado = rs.getString("telefone");
                String comprasCriptografadas = rs.getString("compras");

                // Descriptografa os dados
                String nomeDescriptografado = CryptoUtils.decrypt(nomeCriptografado, SECRET_KEY);
                String telefoneDescriptografado = CryptoUtils.decrypt(telefoneCriptografado, SECRET_KEY);
                String emailDescriptografado = CryptoUtils.decrypt(emailCriptografado, SECRET_KEY);
                
                // Cria um novo objeto Cliente
                cliente = new Cliente(nomeDescriptografado, telefoneDescriptografado, emailDescriptografado);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cliente; // Retorna o cliente ou null se n�o encontrado
    }

}
