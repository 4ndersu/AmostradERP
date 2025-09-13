package erp;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.ResourceBundle;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.SecureRandom;


public class Funcionario implements Serializable  {
    private String id;
    private String nome;
    private String cargo;
    private double salario;
    private String email;
    private String senha;// Senha criptografada
    private Loja loja;

    private static Map<String, Funcionario> funcionariosMap = new HashMap<>();
    public static final String SECRET_KEY = "1234567890123456"; // Chave secreta para criptografia
    private static final Scanner scanner = new Scanner(System.in);
    static ResourceBundle messages = ResourceBundle.getBundle("messages", Locale.getDefault());
    private static Map<String, String> VERIFY_CODES = new HashMap<>();
    

    public boolean podeCadastrarProduto() {
        return false; // implementa��o padr�o
    }

    public boolean podeCadastrarDepartamento() {
        return false; // implementa��o padr�o
    }

    public boolean podeCadastrarCliente() {
        return false; // implementa��o padr�o
    }

    public boolean podeVenderProduto() {
        return false; // implementa��o padr�o
    }

    public boolean podeVerRelatorios() {
        return false; // implementa��o padr�o
    }

    
    public Funcionario(String id, String nome, String cargo, double salario, String email, String senha, String lojaId) {
        this.id = id;
        this.nome = nome;
        this.cargo = cargo;
        this.salario = salario;
        this.email = email;
        this.senha = senha;
        this.loja = new Loja(lojaId); // Presumindo que voc� tenha um construtor que aceite um id de loja
    }

    // M�todos Getters e Setters
    public String getId() {
        return id;
    }
    
    public String getNome() {
        return nome;
    }

    public String getCargo() {
        return cargo;
    }

    public double getSalario() {
        return salario;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }
    
    // M�todos de acesso e modifica��es para o campo loja
    public void setLoja(Loja loja) {
        this.loja = loja;
    }

    public Loja getLoja() {
        return loja;
    }
    
    public String getLojaId() {
        return loja != null ? loja.getId() : null; // Retorna o ID da loja, ou null se a loja n�o estiver definida
    }
    
    public static Map<String, Funcionario> getFuncionariosMap() {
        return funcionariosMap;
    }

    public boolean verificarSenha(String senha) {
        return this.senha.equals(senha); // Compara diretamente com a senha armazenada em texto simples
    }



    @Override
    public String toString() {
        return id + "," + nome + "," + cargo + "," + salario + "," + email + "," + senha;
    }

    public static Funcionario fromString(String str) {
        String[] partes = str.split(",", -1);
        if (partes.length == 7) { // Atualizado para 7 partes
            String id = partes[0];
            String nome = partes[1];
            String cargo = partes[2];
            double salario;
            String email = partes[4];
            String senha = partes[5];
            String lojaId = partes[6]; // Novo par�metro

            try {
                salario = Double.parseDouble(partes[3]);
            } catch (NumberFormatException e) {
                System.err.println("Formato de sal�rio inv�lido para o Funcion�rio com ID: " + id + ". Valor fornecido: " + partes[3]);
                return null;
            }

            switch (cargo.toLowerCase()) {
                case "atendente":
                    return new Atendente(id, nome, salario, email, senha, lojaId);
                case "gerente":
                    return new Gerente(id, nome, salario, email, senha, lojaId);
                case "supervisor":
                    return new Supervisor(id, nome, salario, email, senha, lojaId);
                case "estoquista":
                    return new Estoquista(id, nome, salario, email, senha, lojaId);
                default:
                    System.err.println(messages.getString("error.invalid_position") + cargo);
                    return null;
            }
        } else {
            System.err.println("Formato de Funcion�rio inv�lido: " + str);
        }
        return null;
    }


 // M�todo auxiliar para verificar se a loja existe
    private static boolean lojaExiste(Connection conn, String lojaId) throws SQLException {
        String query = "SELECT COUNT(*) FROM loja WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, lojaId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
 // Criptografa a senha
    public static String encrypt(String data, String secretKey) throws Exception {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    // Descriptografa a senha
    public static String decrypt(String encryptedData, String secretKey) throws Exception {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] originalData = cipher.doFinal(decodedData);
        return new String(originalData);
    }


    // Modifique o m�todo saveFuncionarios para incluir a verifica��o
    public static void saveFuncionarios() throws SQLException {
        System.out.println(messages.getString("saving.employees"));
        String sql = "INSERT INTO funcionarios (nome, cargo, salario, email, senha, loja) VALUES (?, ?, ?, ?, ?, ?)";
        String checkEmailSql = "SELECT COUNT(*) FROM funcionarios WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkEmailSql);
             PreparedStatement insertStmt = conn.prepareStatement(sql)) {

            for (Funcionario funcionario : funcionariosMap.values()) {
                String lojaId = funcionario.getLoja() != null ? funcionario.getLoja().getId() : null;

                // Verifica se a loja existe antes de salvar o funcion�rio
                if (lojaId != null && !lojaExiste(conn, lojaId)) {
                    System.out.println("A loja com ID " + lojaId + " n�o existe. O funcion�rio " + funcionario.getNome() + " n�o ser� salvo.");
                    continue;
                }

                // Verifica��o de email duplicado
                checkStmt.setString(1, funcionario.getEmail());
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    System.out.println("O funcion�rio com o email " + funcionario.getEmail() + " j� est� cadastrado.");
                    continue;
                }

                // Criptografa a senha antes de salvar
                String senhaCriptografada = encrypt(funcionario.getSenha(), SECRET_KEY);
                insertStmt.setString(1, funcionario.getNome());
                insertStmt.setString(2, funcionario.getCargo());
                insertStmt.setDouble(3, funcionario.getSalario());
                insertStmt.setString(4, funcionario.getEmail());
                insertStmt.setString(5, senhaCriptografada);
                insertStmt.setString(6, lojaId);

                insertStmt.executeUpdate();
            }

            System.out.println(messages.getString("employees_saved_successfully"));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(messages.getString("error.saving_employees"), e);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(messages.getString("encryption_error"));
        }
    }


    public static void carregaFuncionarios(ResourceBundle messages) throws SQLException {
        System.out.println(messages.getString("loadingEmployees"));
        String sql = "SELECT id, nome, cargo, salario, email, senha, loja FROM funcionarios";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                String nome = rs.getString("nome");
                String cargo = rs.getString("cargo");
                double salario = rs.getDouble("salario");
                String email = rs.getString("email");
                String senhaCriptografada = rs.getString("senha");
                String lojaId = rs.getString("loja");

                // Descriptografa a senha ao carregar
                String senha = decrypt(senhaCriptografada, SECRET_KEY);

                Funcionario funcionario;
                switch (cargo.toLowerCase()) {
                    case "gerente":
                        funcionario = new Gerente(id, nome, salario, email, senha, lojaId);
                        break;
                    case "atendente":
                        funcionario = new Atendente(id, nome, salario, email, senha, lojaId);
                        break;
                    case "supervisor":
                        funcionario = new Supervisor(id, nome, salario, email, senha, lojaId);
                        break;
                    case "estoquista":
                        funcionario = new Estoquista(id, nome, salario, email, senha, lojaId);
                        break;
                    default:
                        continue;
                }

                funcionariosMap.put(id, funcionario);
            }

            System.out.println(messages.getString("employeesLoaded"));
        } catch (SQLException e) {
            throw new SQLException(messages.getString("loadingError"), e);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(messages.getString("decryption_error"));
        }
    }



    private static boolean isPrimeiroUso() {
        return funcionariosMap.isEmpty();
    }

    public static void primeiroUso(ResourceBundle messages, Loja loja2) throws SQLException {
        if (isPrimeiroUso()) {
            System.out.println(messages.getString("firstUseWelcome"));
            System.out.println(messages.getString("firstUsePrompt"));

            // Solicitar informa��es para o primeiro gerente
            System.out.print(messages.getString("enterManagerId"));
            String id = scanner.nextLine();
            System.out.print(messages.getString("enterManagerName"));
            String nome = scanner.nextLine();
            System.out.print(messages.getString("enterManagerSalary"));
            double salario = scanner.nextDouble();
            scanner.nextLine(); // Consumir nova linha
            System.out.print(messages.getString("enterManagerEmail"));
            String email = scanner.nextLine();
            System.out.print(messages.getString("enterManagerPassword"));
            String senha = scanner.nextLine();

            // Armazenar a senha em texto simples (sem criptografia)
            // N�o � mais necess�rio criptografar a senha
            String lojaId = loja2.getId(); // Atualize conforme sua implementa��o

            // Criar o gerente
            Funcionario gerente = new Gerente(id, nome, salario, email, senha, lojaId); // Armazenando a senha sem criptografia
            funcionariosMap.put(id, gerente);

            saveFuncionarios(); // Certifique-se de que o novo gerente foi salvo
            System.out.println(messages.getString("managerRegisteredSuccess"));

            // Registrar no blockchain
            String descricao = String.format("Gerente criado: ID=%s, Nome=%s, Sal�rio=%s, Email=%s, LojaID=%s", id, nome, salario, email, lojaId);
            SimuladorBlockchain.criarContrato(descricao);
        }
    }




    public static Funcionario autenticarFuncionario(ResourceBundle messages) {
        System.out.print(messages.getString("enterEmail"));
        String email = scanner.nextLine();
        System.out.print(messages.getString("enterPassword"));
        String senha = scanner.nextLine();

        for (Funcionario funcionario : funcionariosMap.values()) {
            if (funcionario.getEmail().equals(email) && funcionario.verificarSenha(senha)) {
                // Obtenha o ID da loja
                String lojaId = funcionario.getLoja() != null ? funcionario.getLoja().getId() : null;
                if (lojaId != null) {
                    System.out.println("Funcion�rio autenticado na loja com ID: " + lojaId);
                }

                // Etapa de MFA
                String verificationCode = sendVerificationCode(email, messages);
                System.out.print(messages.getString("verificationCodePrompt"));
                String enteredCode = scanner.nextLine();

                if (verifyCode(email, enteredCode)) {
                    System.out.println(messages.getString("authSuccess"));
                    return funcionario;
                } else {
                    System.out.println(messages.getString("invalidCode"));
                }
                return null;
            }
        }

        System.out.println(messages.getString("invalidEmailPassword"));
        return null;
    }


    private static String sendVerificationCode(String email, ResourceBundle messages) {
        String code = generateCode();
        VERIFY_CODES.put(email, code);

        // Limpa o arquivo de c�digos de verifica��o antes de adicionar novos c�digos
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("verification_codes.txt"))) {
            // O arquivo ser� limpo ao criar um novo BufferedWriter com o par�metro false (ou padr�o)
        } catch (IOException e) {
            System.err.println(messages.getString("fileClearError") + e.getMessage());
        }

        // Grava o novo c�digo em um arquivo de texto
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("verification_codes.txt", true))) {
            writer.write("Email: " + email + " - C�digo: " + code);
            writer.newLine();
        } catch (IOException e) {
            System.err.println(messages.getString("fileSaveError") + e.getMessage());
        }

        System.out.println(messages.getString("verificationCodeSaved"));
        return code;
    }

    private static String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // C�digo entre 100000 e 999999
        return Integer.toString(code);
    }

    private static boolean verifyCode(String email, String code) {
        return code.equals(VERIFY_CODES.get(email));
    }


    public static void cadastrarFuncionario(Funcionario usuarioAutenticado, ResourceBundle messages) throws SQLException, IOException {
        if (messages == null) {
            messages = ResourceBundle.getBundle("messages", Locale.getDefault());
        }

        if (usuarioAutenticado == null || !(usuarioAutenticado.getCargo().equalsIgnoreCase("gerente"))) {
            System.out.println(messages.getString("onlyManagerCanRegister"));
            return;
        }

        System.out.print(messages.getString("enterEmployeeName"));
        String nome = scanner.nextLine();

        int cargo = 0;

        // L� o n�mero do cargo e lida com a exce��o se o valor n�o for um n�mero
        while (true) {
            try {
                System.out.print(messages.getString("enterEmployeePositionNumber"));
                cargo = scanner.nextInt();
                scanner.nextLine(); // Consumir a nova linha
                if (cargo < 1 || cargo > 4) {
                    System.out.println(messages.getString("invalidPositionNumber"));
                } else {
                    break; // Sai do loop se a entrada for v�lida
                }
            } catch (InputMismatchException e) {
                System.out.println(messages.getString("invalidNumberInput"));
                scanner.nextLine(); // Limpar o buffer
            }
        }

        double salario = 0;

        // L� o sal�rio e lida com a exce��o se o valor n�o for um n�mero
        while (true) {
            try {
                System.out.print(messages.getString("enterEmployeeSalary"));
                salario = scanner.nextDouble();
                scanner.nextLine(); // Consumir a nova linha
                break; // Sai do loop se a entrada for v�lida
            } catch (InputMismatchException e) {
                System.out.println(messages.getString("invalidSalaryInput"));
                scanner.nextLine(); // Limpar o buffer
            }
        }

        System.out.print(messages.getString("enterEmployeeEmail"));
        String email = scanner.nextLine();

        // Verifica se o funcion�rio j� est� cadastrado pelo email
        if (funcionariosMap.containsKey(email)) {
            System.out.println(messages.getString("employeeAlreadyRegistered")); // Mensagem para funcion�rio duplicado
            return; // Sai do m�todo se o email j� existir
        }

        System.out.print(messages.getString("enterEmployeePassword"));
        String senha = scanner.nextLine();

        // Armazenar a senha em texto simples (sem criptografia)
        String lojaId = usuarioAutenticado.getLojaId(); // Obter o ID da loja do usu�rio autenticado

        Funcionario funcionario;
        switch (cargo) {
            case 1:
                funcionario = new Atendente(null, nome, salario, email, senha, lojaId); // O ID ser� gerado automaticamente
                break;
            case 2:
                funcionario = new Gerente(null, nome, salario, email, senha, lojaId);
                break;
            case 3:
                funcionario = new Supervisor(null, nome, salario, email, senha, lojaId);
                break;
            case 4:
                funcionario = new Estoquista(null, nome, salario, email, senha, lojaId);
                break;
            default:
                System.out.println(messages.getString("invalidPosition"));
                return;
        }

        // Adiciona o novo funcion�rio ao mapa e salva no banco de dados
        funcionariosMap.put(email, funcionario);
        saveFuncionarios();
        System.out.println(messages.getString("employeeRegisteredSuccess"));

        // Registrar no blockchain
        String descricao = String.format("Funcion�rio registrado: Nome=%s, Cargo=%s, Sal�rio=%s, Email=%s, LojaID=%s",
                                         nome, funcionario.getCargo(), salario, email, lojaId);
        SimuladorBlockchain.criarContrato(descricao);
    }


    public static void listarFuncionarios() {
        if (funcionariosMap.isEmpty()) {
            System.out.println(messages.getString("no_employees_registered"));
        } else {
            System.out.println(messages.getString("employees_registered"));
            
            String sql = "SELECT id, nome, cargo, salario, email, loja FROM funcionarios";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String id = rs.getString("id");
                    String nome = rs.getString("nome");
                    String cargo = rs.getString("cargo");
                    double salario = rs.getDouble("salario");
                    String email = rs.getString("email");
                    String lojaId = rs.getString("loja");

                    System.out.println("ID: " + id + ", Nome: " + nome + ", Cargo: " + cargo +
                                       ", Sal�rio: " + salario + ", Email: " + email + ", LojaID: " + lojaId);
                }
            } catch (SQLException e) {
                System.out.println(messages.getString("error.loading_employees"));
                e.printStackTrace();
            }
        }
    }

    
    public static void listarLogsOperacoes(Loja loja) {
        if (!"Deluxe".equals(loja.getPlanoAtual())) {
        	System.out.println(messages.getString("logs_viewing_functionality_unavailable"));
            return;
        }

        File file = new File("operation_logs.txt");
        if (!file.exists()) {
        	System.out.println(messages.getString("operation_logs_file_not_found"));

            return;
        }

        System.out.println(messages.getString("operation_logs"));


        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo de logs de opera��es: " + e.getMessage());
        }
    }

}
