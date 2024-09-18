import oracle.jdbc.OracleTypes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class UzytkownicyPanel extends JFrame {
    private Connection connectionNow;
    private int id;
    private JButton cofnijButton=new JButton("Wstecz");
    private JButton updateButton = new JButton("Update");
    private JButton deleteButton = new JButton("Usun");
    private JTable listaUzytkonikowTable;
    private JScrollPane scrollPane;
    private JComboBox <String> user;
    public UzytkownicyPanel(int adminID, Connection connection){
        this.connectionNow=connection;
        this.id=adminID;

        initializeLayout();
        createUIComponents();

        readUzytkonikow();

        setupListeners();
        loadData();
    }
    private void initializeLayout() {
        setTitle("Panel Uzytkownikow");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void createUIComponents() {
        JLabel headerLabel = new JLabel("Uzytkownicy", JLabel.CENTER);
        headerLabel.setFont(new Font("Serif", Font.BOLD, 20));
        add(headerLabel, BorderLayout.NORTH);

        listaUzytkonikowTable = new JTable();
        scrollPane = new JScrollPane(listaUzytkonikowTable);
        add(scrollPane, BorderLayout.CENTER);

        user = new JComboBox<String>();

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(cofnijButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    private void setupListeners() {
        cofnijButton.addActionListener(ActionEvent -> {
            AdminPanel adminPanel = new AdminPanel(id, connectionNow);
            dispose();
            adminPanel.setVisible(true);
        });

        deleteButton.addActionListener(e -> pokazOknoUsuwanaUzytkownika(user));

        updateButton.addActionListener(e -> pokazOknoAktualizowaniaSklepu());
    }


    private void pokazOknoUsuwanaUzytkownika(JComboBox<String> uzytkownik) {
        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Uzytkownik:"));
        myPanel.add(uzytkownik);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
            "Podaj login", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String userString = (String) uzytkownik.getSelectedItem();
            int userInt = findUzytkownikaByName(userString);
            deleteUzytkownika(userInt);
        }
    }

    private void deleteUzytkownika(int idUzytkownika){
        String procedureCall = "{ call DELETEUZYTKOWNIK(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.setInt(1, idUzytkownika);
            callableStatement.execute();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pokazOknoAktualizowaniaSklepu() {

        JTextField idUzytkownika = new JTextField(30);
        JTextField nowyTelefon = new JTextField(30);
        JTextField nowyEmail = new JTextField(30);
        JTextField noweHaslo = new JTextField(30);
        JComboBox<String> rolaBox = new JComboBox<>(new String[]{"admin", "user"});

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Zmiany sklepu ID:"));
        myPanel.add(idUzytkownika);
        myPanel.add(new JLabel("Nowy numer"));
        myPanel.add(nowyTelefon);
        myPanel.add(new JLabel("Nowy E-mail"));
        myPanel.add(nowyEmail);
        myPanel.add(new JLabel("Nowe haslo"));
        myPanel.add(noweHaslo);
        myPanel.add(new JLabel("Nowa rola"));
        myPanel.add(rolaBox);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
            "Wprowadź nowy adres sklepu", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String tel = (String) nowyTelefon.getText();
            String email = (String) nowyEmail.getText();
            String haslo = (String) noweHaslo.getText();
            String rola = (String) rolaBox.getSelectedItem();
            int idUzytkownika2= Integer.parseInt(idUzytkownika.getText());
            updateUzytkownika(idUzytkownika2, tel, email, haslo, rola);
        }
    }
    private void updateUzytkownika(int idUser, String p_tel, String p_email, String p_haslo, String p_rola) {
        String procedureCall = "{ call UPDATEUZYTKOWNIK(?, ?, ?, ?, ?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.setInt(1, idUser);
            callableStatement.setString(2, p_tel);
            callableStatement.setString(3, p_email);
            callableStatement.setString(4, p_haslo);
            callableStatement.setString(5, p_rola);
            callableStatement.execute();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Błąd SQL: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void readUzytkonikow(){
        String procedureCall = "{ call READUZYTKOWNIKOW(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();
            ResultSet rs = (ResultSet) callableStatement.getObject(1);
            while (rs.next()) {
                String listDetails = rs.getString("LOGIN");
                user.addItem(listDetails);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public int findUzytkownikaByName(String login) {
        String procedureCall = "{ ? = call FINDUZYTKOWNIKABYLOGIN(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.setString(2, login);
            callableStatement.execute();
            int idSklepu = callableStatement.getInt(1);
            return idSklepu;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    private void loadData() {
        String procedureCall = "{ call READUZYTKOWNIKOW(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            // Rejestracja parametru OUT jako kursor
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();

            // Pobranie ResultSet z kursora
            try (ResultSet rs = (ResultSet) callableStatement.getObject(1)) {
                // Przygotowanie modelu tabeli do wyświetlenia danych
                DefaultTableModel model = new DefaultTableModel(
                    new String[]{"ID UZYTKOWNIKA", "IMIE", "NAZWISKO", "NR TELEFONU", "E-MAIL", "LOGIN", "HASLO", "PIENIADZE", "ROLA"},
                    0
                );

                // Iteracja przez wiersze ResultSet
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("ID_UZYTKOWNIKA"),
                        rs.getString("IMIE"),
                        rs.getString("NAZWISKO"),
                        rs.getString("NR_TELEFONU"),
                        rs.getString("ADRES_EMAIL"),
                        rs.getString("LOGIN"),
                        rs.getString("HASLO"),
                        rs.getFloat("PIENIADZE"),
                        rs.getString("ROLE"),
                    });
                }

                // Ustawienie modelu tabeli w JTable
                listaUzytkonikowTable.setModel(model);
            }
        } catch (SQLException e) {
            // Wyświetlenie komunikatu o błędzie
            JOptionPane.showMessageDialog(this, "Błąd SQL: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }


}
