import java.sql.Connection; // Import klasy Connection z pakietu java.sql
import javax.swing.JOptionPane; // Opcjonalnie, jeśli używasz JOptionPane do wyświetlania komunikatów
import oracle.jdbc.internal.OracleTypes;


public class Main {
    public static void main(String[] args) {
        Connection conn = DatabaseConnect.connectDatabase(); // Uzyskanie połączenia z metodą z klasy DatabaseConnect
        if (conn != null) {
            new Logowanie(conn);
        } else {
            JOptionPane.showMessageDialog(null, "dsfsd", "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

