import oracle.jdbc.OracleTypes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class AdminPanel extends JFrame {
    private Connection connection;
    private int id;
    private JComboBox<String> userComboBox;
    private JButton userIdJButton=new JButton("Listy Użytkownika");
    private JButton listyButton=new JButton("Wszystkie Listy");
    private JButton sklepyButton=new JButton("Panel sklepów");
    private JButton produktyButton=new JButton("Panel produktow");
    private JButton uzytkownicyButton=new JButton("Panel uzytkownikow");
    private JButton produktyWsklepieButton=new JButton("Panel produktow w sklepie");
    private JButton niezrealizowaneJButton=new JButton("Niezrealizowane listy");

    public AdminPanel(int userId, Connection connection) {
        this.connection =connection;
        this.id=userId;

        setSize(600,300);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.userComboBox = new JComboBox<>(); // Inicjowanie userComboBox
        createUIComponents(); // Teraz createUIComponents inicjuje komponenty UI, w tym userComboBox
        readLoginyUzytkownikow();

        setVisible(true);
        listyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pokazWszystkieListy();
            }});

        userIdJButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int id=findUzytkownikaByLogin();
                ListyUzytkownika listaUzytkownika = new ListyUzytkownika(userId, connection, id);
                dispose();
                listaUzytkownika.setVisible(true);

            }
        });
        niezrealizowaneJButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ListyNiezrealizowane listyNiezrealizowane = new ListyNiezrealizowane(userId, connection);
                dispose();
                listyNiezrealizowane.setVisible(true);

            }
        });
        produktyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProduktyPanel produkty = new ProduktyPanel(userId, connection);
                dispose();
                produkty.setVisible(true);

            }
        });
        uzytkownicyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UzytkownicyPanel uzytkownicy = new UzytkownicyPanel(userId, connection);
                dispose();
                uzytkownicy.setVisible(true);

            }
        });

        sklepyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SklepyPanel sklepy = new SklepyPanel(userId, connection);
                dispose();
                sklepy.setVisible(true);

            }
        });
        produktyWsklepieButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProduktyWsklepiePanel produktWsklepie = new ProduktyWsklepiePanel(userId, connection);
                dispose();
                produktWsklepie.setVisible(true);

            }
        });
    }
    public String getNazwaUzytkownika() {
        return (String) userComboBox.getSelectedItem();
    }
    public int findUzytkownikaByLogin() {
        String loginUzytkownika=getNazwaUzytkownika();
        String procedureCall = "{ ? = call FINDUZYTKOWNIKABYLOGIN(?) }";
        try (CallableStatement callableStatement = connection.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.setString(2, loginUzytkownika);
            callableStatement.execute();
            int idUzytkownikaSzukanego = callableStatement.getInt(1);
            return idUzytkownikaSzukanego;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }
    private void readLoginyUzytkownikow() {
        String procedureCall = "{ call ReadLoginyUzytkownikow(?) }";
        try (CallableStatement callableStatement = connection.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();

            try (ResultSet rs = (ResultSet) callableStatement.getObject(1)) {
                userComboBox.removeAllItems();
                while (rs.next()) {
                    String userLogin = rs.getString("LOGIN");
                    userComboBox.addItem(userLogin);
                }
            }
        } catch (SQLException e) {
            // Handle potential SQL exceptions in a user-friendly way
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void createUIComponents() {
    // Panel nagłówkowy
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel idLabel = new JLabel("ID: " + id);  // Założenie, że userId jest już dostępne
        JLabel welcomeLabel = new JLabel("Witaj!");
        JLabel roleLabel = new JLabel("Rola: Admin");  // Zakładając, że rola jest dostępna, trzeba by pobrać z bazy
        headerPanel.add(idLabel);
        headerPanel.add(welcomeLabel);
        headerPanel.add(roleLabel);
        // Główny panel
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 10, 10));  // Grid layout 3 wiersze, 2 kolumny

        // Panel: Modyfikuj Sklep
        JPanel modSklepPanel = new JPanel(new BorderLayout());
        modSklepPanel.add(new JLabel("Modyfikuj Sklep"), BorderLayout.NORTH);
        modSklepPanel.add(sklepyButton, BorderLayout.SOUTH);

        // Panel: Modyfikuj Uzytkownika
        JPanel modUserPanel = new JPanel(new BorderLayout());
        modUserPanel.add(new JLabel("Modyfikuj Uzytkownika"), BorderLayout.NORTH);
        modUserPanel.add(uzytkownicyButton, BorderLayout.SOUTH);


        // Panel: Modyfikuj Produkty
        JPanel modProdPanel = new JPanel(new BorderLayout());
        modProdPanel.add(new JLabel("Modyfikuj Produkty"), BorderLayout.NORTH);
        modProdPanel.add(produktyButton, BorderLayout.SOUTH);

        // Panel: Modyfikuj Produkty w Sklepie
        JPanel modProdSklepPanel = new JPanel(new BorderLayout());
        modProdSklepPanel.add(new JLabel("Modyfikuj Produkty w Sklepie"), BorderLayout.NORTH);
        modProdSklepPanel.add(produktyWsklepieButton, BorderLayout.SOUTH);

        // Panel: Wszystkie Listy
        JPanel wszystkieListyPanel = new JPanel(new BorderLayout());
        wszystkieListyPanel.add(new JLabel("Wszystkie Listy"), BorderLayout.NORTH);
        wszystkieListyPanel.add(listyButton, BorderLayout.SOUTH);;

        // Panel: Lista użytkowników
        JPanel listaUzytkPanel = new JPanel(new BorderLayout());
        listaUzytkPanel.add(userComboBox, BorderLayout.NORTH);
        listaUzytkPanel.add(userIdJButton, BorderLayout.SOUTH);

        // Panel: Niezrealizowane Listy
        JPanel niezrealListyPanel = new JPanel(new BorderLayout());
        niezrealListyPanel.add(new JLabel("Niezrealizowane Listy"), BorderLayout.NORTH);
        niezrealListyPanel.add(niezrealizowaneJButton, BorderLayout.SOUTH);

        // Dodawanie paneli do głównego panelu w kolejności pionowej
        panel.add(modSklepPanel);
        panel.add(wszystkieListyPanel);
        panel.add(modProdPanel);
        panel.add(modUserPanel);
        panel.add(listaUzytkPanel);
        panel.add(modProdSklepPanel);
        panel.add(niezrealListyPanel);

        // Dodanie głównego panelu do ramki
        this.add(headerPanel, BorderLayout.NORTH);
        this.add(panel, BorderLayout.CENTER);
    }


    private void pokazWszystkieProdukty() {
        JPanel myPanel = new JPanel();
        myPanel.add(Box.createHorizontalStrut(15)); // odstęp

        String procedureCall = "{ call READPRODUKTY(?) }";
        try (CallableStatement callableStatement = connection.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();

            try (ResultSet rs = (ResultSet) callableStatement.getObject(1)) {
                DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Nazwa Sklepu", "Nazwa Produktu", "Nazwa Producenta", "Cena"}, 0);
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("ID_PRODUKTU"),
                        rs.getString("NazwaSklepu"),
                        rs.getString("NazwaProduktu"),
                        rs.getString("NazwaProducenta"),
                        rs.getFloat("Cena")
                    });
                }

                JTable table = new JTable(model);
                table.setPreferredScrollableViewportSize(new Dimension(500, 300));  // Ustawienie preferowanego rozmiaru
                table.setFillsViewportHeight(true);

                JScrollPane scrollPane = new JScrollPane(table);
                myPanel.add(scrollPane);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(null, myPanel, "Lista Produktów", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            // Działania po zatwierdzeniu (opcjonalne)
        }
    }

    private void pokazWszystkieListy() {
        JPanel myPanel = new JPanel();
        myPanel.add(Box.createHorizontalStrut(15)); // odstęp

        String procedureCall = "{ call READLISTY2(?) }";
        try (CallableStatement callableStatement = connection.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();

            try (ResultSet rs = (ResultSet) callableStatement.getObject(1)) {
                DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Uzytkownik", "Utworzono", "Nazwa", "Status"}, 0);
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("idListy"),
                        rs.getString("login"),
                        rs.getDate("data"),
                        rs.getString("nazwa"),
                        rs.getString("status")
                    });
                }

                JTable table = new JTable(model);
                table.setPreferredScrollableViewportSize(new Dimension(500, 300));  // Ustawienie preferowanego rozmiaru
                table.setFillsViewportHeight(true);

                JScrollPane scrollPane = new JScrollPane(table);
                myPanel.add(scrollPane);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(null, myPanel, "Lista Produktów", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            // Działania po zatwierdzeniu (opcjonalne)
        }
    }

}
