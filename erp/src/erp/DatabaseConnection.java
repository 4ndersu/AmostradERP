package erp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	 private static final String URL = "jdbc:mysql://localhost:3306/amostraderp"; // URL do banco de dados
	    private static final String USER = "root"; // Nome de usu�rio
	    private static final String PASSWORD = "Andersu_joestar123"; // Senha

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Carregar o driver JDBC do MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Estabelecer a conex�o
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conex�o estabelecida com sucesso!");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC do MySQL n�o encontrado.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erro ao estabelecer a conex�o com o banco de dados.");
            e.printStackTrace();
        }
        return connection;
    }
}


