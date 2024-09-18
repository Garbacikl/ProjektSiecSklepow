import javax.swing.*;
import java.sql.*;

public class Logowanie {
    private Connection connection;
    private Sklepy sklepyInstance;
    private AdminPanel adminPanel;
    private JFrame frame;
    private static int loggedInUserId = -1; // Domyslnie -1, oznaczający brak zalogowanego uzytkownika

    public static void storeLoggedInUserId(int userId) {
        loggedInUserId = userId;
    }

    public static int getLoggedInUserId() {
        return loggedInUserId;
    }
    private JPanel panel1;
    private JButton zalogujButton;
    private JPasswordField passwordField;
    private JTextField loginField;
    private JButton zarejestrujButton;
    private JTextField loginRejestracjaField;
    private JTextField hasloField;
    private JTextField emailField;
    private JTextField numerTelefonuField;
    private JTextField nazwiskoField;
    private JTextField imieField;
    private JPasswordField passwordRejestracyjnyField;
    private JPasswordField powtorzPasswordRejestracjaField;

    public Logowanie(Connection conn) {
        this.connection = conn;
        zalogujButton.addActionListener(e -> zaloguj());
        zarejestrujButton.addActionListener(e -> zarejestruj());

        frame = new JFrame("Logowanie");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel1); // Użyj panelu z .form
        frame.pack();
        frame.setVisible(true);
    }
    public void clear(){
        imieField.setText("");
        nazwiskoField.setText("");
        numerTelefonuField.setText("");
        emailField.setText("");
        loginRejestracjaField.setText("");
        passwordRejestracyjnyField.setText("");
        powtorzPasswordRejestracjaField.setText("");
    }
    public boolean zarejestruj() {
        String imie = imieField.getText();
        String nazwisko = nazwiskoField.getText();
        String telefon = numerTelefonuField.getText();
        String email = emailField.getText();
        String login = loginRejestracjaField.getText();
        String password = new String(passwordRejestracyjnyField.getPassword());
        String password2 = new String(powtorzPasswordRejestracjaField.getPassword());

        if (!password.equals(password2)) {
            JOptionPane.showMessageDialog(frame, "Hasła nie pasuja.");
            return false;
        }

        if (imie.trim().isEmpty() || nazwisko.trim().isEmpty() || telefon.trim().isEmpty() || email.trim().isEmpty() || login.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Wszytskie pola musza byc uzupełnione.");
            return false;
        }

        String query = "{ CALL INSERTNEWUZYTKOWNIK(?, ?, ?, ?, ?, ?, 0, 'user') }";
        try (PreparedStatement stmt = connection.prepareCall(query)) {
            stmt.setString(1, imie);
            stmt.setString(2, nazwisko);
            stmt.setString(3, telefon);
            stmt.setString(4, email);
            stmt.setString(5, login);
            stmt.setString(6, password);

            stmt.execute();
            JOptionPane.showMessageDialog(frame, "Rejestracja pomyślna.");
            clear();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error during registration: " + e.getMessage());
            return false;
        }
    }

    public boolean zaloguj() {
        String username = loginField.getText();
        String password = new String(passwordField.getPassword());

        if (username != null && password != null && !username.trim().isEmpty() && !password.trim().isEmpty()) {
            String query = "SELECT *  FROM uzytkownicy WHERE LOGIN = ? AND HASLO = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        String rola = rs.getString("ROLE");
                        JOptionPane.showMessageDialog(null, "Login successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        int userId = rs.getInt("ID_UZYTKOWNIKA");
                        storeLoggedInUserId(userId); // Metoda do przechowywania ID
                        if("admin".equals(rola)) {
                            if (adminPanel == null) {
                                adminPanel = new AdminPanel(userId, connection); // Zakładając, że Sklepy ma konstruktor bezargumentowy
                            }
                            frame.dispose(); // Ukrycie okna logowania
                            adminPanel.setVisible(true); // Upewnij się, że okno Sklepy jest widoczne
                        }
                        else {
                            // Przekierowanie do Sklepy
                            if (sklepyInstance == null) {
                                sklepyInstance = new Sklepy(userId, connection); // Zakładając, że Sklepy ma konstruktor bezargumentowy
                            }
                            frame.dispose(); // Ukrycie okna logowania
                            sklepyInstance.setVisible(true); // Upewnij się, że okno Sklepy jest widoczne
                        }
                        return true;
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please enter both username and password.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
}

