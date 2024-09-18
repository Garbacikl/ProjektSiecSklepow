import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import oracle.jdbc.internal.OracleTypes;
    public class DatabaseConnect {
        public static Connection connectDatabase() {
            Connection connection = null;
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/freepdb1", "hr", "oracle");
                JOptionPane.showMessageDialog(null, "Connection successful", "Connection Status", JOptionPane.INFORMATION_MESSAGE);
            } catch(Exception e) {
                JOptionPane.showMessageDialog(null, "Failed to connect: " + e.getMessage(), "Connection Status", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            return connection;
        }
    }


