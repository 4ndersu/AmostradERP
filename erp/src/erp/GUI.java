package erp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

public class GUI {
    private static Estoque estoque; 
    private static ResourceBundle messages;
    private static final String SECRET_KEY = "1234567890123456";

    public static void main(String[] args) {
        escolherIdioma();
        estoque = initializeEstoque();

        JFrame frame = new JFrame("ERP System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 640);
        frame.setLayout(new BorderLayout());

        // Adiciona a imagem ao fundo do painel
        JLabel imageLabel = new JLabel();
        imageLabel.setIcon(new ImageIcon("C:\\Users\\Usuario.DESKTOP-PVE85AC\\eclipse-workspace\\AmostradERP\\erp\\src\\erp\\images\\image.jpg"));
        frame.add(imageLabel, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        menuBar.add(menu);

        JMenuItem addProductItem = new JMenuItem(messages.getString("addProduct"));
        JMenuItem listProductsItem = new JMenuItem(messages.getString("listProducts"));
        JMenuItem buyProductItem = new JMenuItem(messages.getString("buyProduct"));
        JMenuItem makePaymentItem = new JMenuItem(messages.getString("makePayment"));
        JMenuItem listOpenTitlesItem = new JMenuItem(messages.getString("listOpenTitles"));
        JMenuItem addClientItem = new JMenuItem(messages.getString("addClient"));
        JMenuItem listClientsItem = new JMenuItem(messages.getString("listClients"));
        JMenuItem addDepartmentItem = new JMenuItem(messages.getString("addDepartment"));
        JMenuItem reportSalesItem = new JMenuItem(messages.getString("reportSalesByDepartment"));
        JMenuItem blockchainItem = new JMenuItem(messages.getString("blockchain")); // Nova opção de menu
        JMenuItem viewLogsItem = new JMenuItem(messages.getString("viewOperationLogs")); // Nova opção para visualizar logs
        JMenuItem evaluationItem = new JMenuItem(messages.getString("reviews")); // Avaliação
        JMenuItem exitItem = new JMenuItem(messages.getString("exit"));

        menu.add(addProductItem);
        menu.add(listProductsItem);
        menu.add(buyProductItem);
        menu.add(makePaymentItem);
        menu.add(listOpenTitlesItem);
        menu.add(addClientItem);
        menu.add(listClientsItem);
        menu.add(addDepartmentItem);
        menu.add(reportSalesItem);
        menu.add(viewLogsItem); 
        menu.add(blockchainItem);
        menu.add(evaluationItem); // Adiciona a opção de avaliação
        menu.add(exitItem);

        frame.setJMenuBar(menuBar);

        addProductItem.addActionListener(e -> showAddProductDialog());
        listProductsItem.addActionListener(e -> showMessageDialog(estoque.listarProdutosString()));
        buyProductItem.addActionListener(e -> showBuyProductDialog());
        makePaymentItem.addActionListener(e -> showMakePaymentDialog());
        listOpenTitlesItem.addActionListener(e -> showMessageDialog(estoque.listarTitulosString()));
        addClientItem.addActionListener(e -> showAddClientDialog());
        listClientsItem.addActionListener(e -> showMessageDialog(Cliente.listarClientes()));
        addDepartmentItem.addActionListener(e -> showAddDepartmentDialog());

        // Ação para a opção de blockchain
        blockchainItem.addActionListener(e -> SimuladorBlockchain.executarSimulacaoBlockchain());

        // Ação para visualizar logs de operações
        viewLogsItem.addActionListener(e -> Logger.visualizarLogs());

        // Modificar a lógica de saída para incluir a avaliação
        exitItem.addActionListener(e -> {
            Avaliacao.avaliarSistema(); // Chama o método de avaliação antes de sair
            System.out.println(messages.getString("exiting")); // Mensagem de saída
            System.exit(0); // Fecha a aplicação
        });

        frame.setVisible(true);
    }

    private static Estoque initializeEstoque() { 
        try {
            return new Estoque(Locale.getDefault());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao inicializar o sistema: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return null; // Esta linha nunca será executada devido ao System.exit(1)
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro de SQL: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return null; // Esta linha nunca será executada devido ao System.exit(1)
        }
    }

    private static void setLocale(Locale locale) {
        try {
            Locale.setDefault(locale); // Define o Locale global
            messages = ResourceBundle.getBundle("messages", locale);
            System.out.println("Carregando arquivo de propriedades para o locale: " + locale);
        } catch (Exception e) {
            System.out.println("Arquivo para o locale " + locale + " n�o encontrado. Usando o padr�o (pt_BR).");
            messages = ResourceBundle.getBundle("messages", new Locale("pt", "BR"));
            Locale.setDefault(new Locale("pt", "BR")); // Define o Locale global para pt_BR
        }
    }

    private static void escolherIdioma() {
        String[] options = {"Português-BR", "Espanhol-AR", "Inglês-EUA"};
        int choice = JOptionPane.showOptionDialog(null, "Escolha o idioma:", "Idioma", 
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0:
                setLocale(new Locale("pt", "BR"));
                return;
            case 1:
                setLocale(new Locale("es", "AR"));
                return;
            case 2:
                setLocale(new Locale("en", "US"));
                return;
            default:
                setLocale(new Locale("pt", "BR"));
                break;
        }

        messages = ResourceBundle.getBundle("messages", Locale.getDefault());
    }

    private static void showMessageTerms() {
        // Defina o texto dos Termos e Condi��es
        String termos = messages.getString("privacy_policy_body");
        
        // Cria a JTextArea com o texto dos termos
        JTextArea textArea = new JTextArea(10, 25); // 20 linhas e 50 colunas
        textArea.setText(termos);
        textArea.setEditable(false); // Torna o texto n�o edit�vel
        textArea.setLineWrap(true); // Quebra de linha autom�tica
        textArea.setWrapStyleWord(true); // Quebra de linha por palavra
        textArea.setCaretPosition(0); // Move o cursor para o in�cio do texto 

        // Adiciona a JTextArea a um JScrollPane
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(200, 200)); // Ajusta o tamanho do JScrollPane

        // Exibe a caixa de di�logo
        JOptionPane.showMessageDialog(null, scrollPane, messages.getString("privacy_policy_header"), JOptionPane.INFORMATION_MESSAGE);
    }
    
    private static void showMessageDialog(Object object) {//info
        JOptionPane.showMessageDialog(null, object, messages.getString("info"), JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showAddProductDialog() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField departmentIdField = new JTextField();

        Object[] message = {
            messages.getString("product_id_prompt"), idField,
            messages.getString("product_name_prompt"), nameField,
            messages.getString("product_price_prompt"), priceField,
            messages.getString("department_id_prompt"), departmentIdField
        };

        int option = JOptionPane.showConfirmDialog(null, message, messages.getString("addProduct"), JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                String id = idField.getText();
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                String departmentId = departmentIdField.getText();
                estoque.addProduto(id, name, price, departmentId);
                JOptionPane.showMessageDialog(null, messages.getString("product_added_successfully"), messages.getString("sucesso"), JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, messages.getString("erro_carregar_produtos") + e.getMessage(), messages.getString("erro"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void showBuyProductDialog() {
        JTextField productIdField = new JTextField();
        JTextField clientEmailField = new JTextField();

        Object[] initialMessage = {
            messages.getString("product_id_prompt"), productIdField,
            messages.getString("cliente_email"), clientEmailField
        };

        int option = JOptionPane.showConfirmDialog(null, initialMessage, messages.getString("buyProduct"), JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String productId = productIdField.getText().trim();
            String clientEmail = clientEmailField.getText().trim();

            try {
                // Verifica se o cliente já existe pelo email
                String emailCriptografado = CryptoUtils.encrypt(clientEmail, SECRET_KEY);
                Cliente cliente = Cliente.recuperarClientePorEmail(emailCriptografado);

                if (cliente == null) {
                    // Solicita nome e telefone do novo cliente
                    JTextField clientNameField = new JTextField();
                    JTextField clientPhoneField = new JTextField();

                    Object[] newClientMessage = {
                        messages.getString("client_name_prompt"), clientNameField,
                        messages.getString("client_phone_prompt"), clientPhoneField
                    };

                    int newClientOption = JOptionPane.showConfirmDialog(null, newClientMessage, messages.getString("new_client_data"), JOptionPane.OK_CANCEL_OPTION);

                    if (newClientOption == JOptionPane.OK_OPTION) {
                        String clientName = clientNameField.getText().trim();
                        String clientPhone = clientPhoneField.getText().trim();

                        // Exibe a política de privacidade
                        JTextArea privacyPolicyTextArea = new JTextArea(messages.getString("privacy_policy_body"));
                        privacyPolicyTextArea.setLineWrap(true);
                        privacyPolicyTextArea.setWrapStyleWord(true);
                        privacyPolicyTextArea.setEditable(false);
                        JScrollPane scrollPane = new JScrollPane(privacyPolicyTextArea);
                        scrollPane.setPreferredSize(new Dimension(400, 200));

                        Object[] privacyPolicyMessage = {
                            scrollPane,
                            messages.getString("accept_privacy_policy_prompt")
                        };

                        int privacyOption = JOptionPane.showConfirmDialog(null, privacyPolicyMessage, messages.getString("privacy_policy_title"), JOptionPane.YES_NO_OPTION);

                        if (privacyOption == JOptionPane.YES_OPTION) {
                            // Realiza a compra com o novo cliente que aceita a política
                            estoque.compraProduto(productId, clientEmail, clientName, clientPhone, "sim");
                            JOptionPane.showMessageDialog(null, messages.getString("product_purchased_successfully"), messages.getString("sucesso"), JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, messages.getString("registration_cancelled"), messages.getString("info"), JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                } else {
                    // Caso o cliente já exista, realiza a compra diretamente
                    estoque.compraProduto(productId, clientEmail, null, null, null);
                    JOptionPane.showMessageDialog(null, messages.getString("product_purchased_successfully"), messages.getString("sucesso"), JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, messages.getString("erro_compra") + e.getMessage(), messages.getString("erro"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    
    private static void showMakePaymentDialog() {
        JTextField titleIdField = new JTextField();

        Object[] message = {
            messages.getString("title_id_to_pay_prompt"), titleIdField
        };

        int option = JOptionPane.showConfirmDialog(null, message, messages.getString("efetua"), JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                String titleId = titleIdField.getText();
                estoque.fazPagamento(titleId); // Passa apenas o ID do título
                JOptionPane.showMessageDialog(null, messages.getString("title_paid_successfully"), messages.getString("info"), JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, messages.getString("erro_paga") + e.getMessage(), messages.getString("erro"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void showAddClientDialog() {
       showMessageTerms(); // Mostra os termos e condi��es
 
        // Cria o di�logo para adicionar o cliente
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();

        Object[] message = {
        	messages.getString("novo_cliente"), nameField,
        	messages.getString("telefone_cliente"), phoneField,
        	messages.getString("cliente_email"), emailField
        };

        int option = JOptionPane.showConfirmDialog(null, message, messages.getString("add_cliente"), JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            //int termsAccepted = JOptionPane.showConfirmDialog(null, messages.getString("accept_privacy_policy_prompt"), messages.getString("aceitaTerm"), JOptionPane.YES_NO_OPTION);

            //if (termsAccepted == JOptionPane.YES_OPTION) {
                try {
                    String name = nameField.getText();
                    String phone = phoneField.getText();
                    String email = emailField.getText();

                    // Verifique se todos os campos est�o preenchidos
                    if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                        JOptionPane.showMessageDialog(null, messages.getString("vazio"), messages.getString("aviso"), JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Chame o m�todo para adicionar o cliente
                    Cliente.adicionarCliente(name, phone, email);

                    JOptionPane.showMessageDialog(null, messages.getString("client_added_successfully"), messages.getString("sucesso"), JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    // Log de erro
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, messages.getString("registration_cancelled") + e.getMessage(), messages.getString("erro"), JOptionPane.ERROR_MESSAGE);
                }
            }
            //else {
            	
                //JOptionPane.showMessageDialog(null, "Voc� deve aceitar os termos para continuar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            //}
        //}
    }



    private static void showAddDepartmentDialog() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();

        Object[] message = {
            messages.getString("department_id_prompt"), idField,
            messages.getString("department_name_prompt"), nameField
        };

        int option = JOptionPane.showConfirmDialog(null, message, messages.getString("addDepartment"), JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                String id = idField.getText();
                String name = nameField.getText(); 
                estoque.addDepartamento(id, name); // Chamada correta do método
                JOptionPane.showMessageDialog(null, messages.getString("department_added_successfully"), messages.getString("sucesso"), JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, messages.getString("error_saving_departments") + e.getMessage(), messages.getString("erro"), JOptionPane.ERROR_MESSAGE); 
            } 
        }
    }

}

