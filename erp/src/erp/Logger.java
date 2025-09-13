package erp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;

public class Logger {

    public static void logOperation(Funcionario funcionario, String operation, ResourceBundle messages) {
        String logEntry = createLogEntry(funcionario, operation, messages);
        saveLogToDatabase(funcionario, operation, logEntry);
    }

    private static String createLogEntry(Funcionario funcionario, String operation, ResourceBundle messages) {
        String id = funcionario.getId();
        String nome = funcionario.getNome();
        String cargo = funcionario.getCargo();
        String timestamp = getFormattedTimestamp(Locale.getDefault());
        return String.format("%s: %s, %s: %s, %s: %s, %s: %s, %s: %s",
                messages.getString("logId"), id,
                messages.getString("logName"), nome,
                messages.getString("logRole"), cargo,
                messages.getString("logOperation"), operation,
                messages.getString("logDateTime"), timestamp);
    }

    private static String getFormattedTimestamp(Locale locale) {
        ZonedDateTime now = ZonedDateTime.now(getZoneIdForLocale(locale));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    private static void saveLogToDatabase(Funcionario funcionario, String operation, String logEntry) {
        String sql = "INSERT INTO operation_logs (funcionario_id, nome, cargo, operacao, timestamp) VALUES (?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, funcionario.getId());
            pstmt.setString(2, funcionario.getNome());
            pstmt.setString(3, funcionario.getCargo());
            pstmt.setString(4, operation);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving log to database: " + e.getMessage());
        }
    }

    private static ZoneId getZoneIdForLocale(Locale locale) {
        switch (locale.getLanguage()) {
            case "es":
                return ZoneId.of("America/Argentina/Buenos_Aires");
            case "en":
                return ZoneId.of("America/New_York");
            case "pt":
                return ZoneId.of("America/Sao_Paulo");
            default:
                return ZoneId.systemDefault();
        }
    }

    public static void visualizarLogs() {
        String sql = "SELECT * FROM operation_logs ORDER BY timestamp DESC"; // Ordena os logs por data/hora

        StringBuilder logEntries = new StringBuilder();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("funcionario_id");
                String nome = rs.getString("nome");
                String cargo = rs.getString("cargo");
                String operacao = rs.getString("operacao");
                String timestamp = rs.getString("timestamp");
                
                logEntries.append(String.format("ID: %s, Nome: %s, Cargo: %s, Operação: %s, Data/Hora: %s%n",
                        id, nome, cargo, operacao, timestamp));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving logs from database: " + e.getMessage());
        }
        
        // Exibe os logs em um JOptionPane
        if (logEntries.length() == 0) {
            logEntries.append("Nenhum log encontrado.");
        }
        JOptionPane.showMessageDialog(null, logEntries.toString(), "Logs de Operações", JOptionPane.INFORMATION_MESSAGE);
    }
}
