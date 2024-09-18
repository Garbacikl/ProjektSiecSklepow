import oracle.jdbc.OracleTypes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Sklepy extends JFrame {
    private JPanel panel1;
    private JLabel ID_UZYTKONIKA;
    private JLabel ROLE;
    private JButton przejdzDoSklepuButton;
    private JComboBox<String> NAZWA_SKLEPU;
    private JLabel ID;
    private JButton przejdzDoListy;
    private JComboBox NAZWA_LISTY;
    private JLabel id_listy;
    private Connection connection;

    public Sklepy(int userId, Connection connection) {
        // Konstruktor
        super("Sklepy");
        this.connection = connection;
        setSize(500, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createUIComponents();
        setContentPane(panel1);
        updateUserIdLabel(userId);
        String rola = wykonajPobierzRole(userId);
        updateRoleLabel(rola);
        readNazwySklepow();
        readNazwyListy(userId);
        setVisible(true);

        przejdzDoSklepuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int id=findSklepByName();
                ProduktySklep produktySklep = new ProduktySklep(userId, connection, id);
                dispose();
                produktySklep.setVisible(true);

            }
        });
        przejdzDoListy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int id=findListaByName();
                ListaZakupow listazakupow = new ListaZakupow(userId, connection, id);
                dispose();
                listazakupow.setVisible(true);

            }
        });
    }
    private void readNazwyListy(int idUser) {
        String procedureCall = "{ call ReadNazwyList(?,?) }";
        try (CallableStatement callableStatement = connection.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.setInt(2, idUser);
            callableStatement.execute();
            ResultSet rs = (ResultSet) callableStatement.getObject(1);
            while (rs.next()) {
                String listDetails = rs.getString("NAZWA");
                NAZWA_LISTY.addItem(listDetails);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public int findListaByName() {
        String nazwaListy=getNazwaListy();
        String procedureCall = "{ ? = call FINDLISTAPBYNAME(?) }";
        try (CallableStatement callableStatement = connection.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.setString(2, nazwaListy);
            callableStatement.execute();
            int ListaId = callableStatement.getInt(1);
            return ListaId;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }
    private void readNazwySklepow() {
        String procedureCall = "{ call ReadNazwySklepow(?) }";
        try (CallableStatement callableStatement = connection.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();
            ResultSet rs = (ResultSet) callableStatement.getObject(1);
            while (rs.next()) {
                String shopDetails = rs.getString("NAZWA_SKLEPU");
                NAZWA_SKLEPU.addItem(shopDetails);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public int findSklepByName() {
        String nazwaSklepu=getNazwaaa();
        String procedureCall = "{ ? = call FINDSKLEPBYNAME(?) }";
        try (CallableStatement callableStatement = connection.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.setString(2, nazwaSklepu);
            callableStatement.execute();
            int sklepId = callableStatement.getInt(1);
            return sklepId;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }
    public String getNazwaaa() {
        return (String) NAZWA_SKLEPU.getSelectedItem();
    }
    public String getNazwaListy() {
        return (String) NAZWA_LISTY.getSelectedItem();
    }

    public void updateUserIdLabel(int userId) {
        ID_UZYTKONIKA.setText(String.valueOf(userId));
    }
    private String wykonajPobierzRole(int userId) {
        String procedureCall = "{ ? = call POBIERZROLE(?) }";
        String rola = null;
        try (CallableStatement callableStatement = connection.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setInt(2, userId);
            callableStatement.execute();
            rola = callableStatement.getString(1);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return rola;
    }

    public void updateRoleLabel(String rola) {
        ROLE.setText(rola);
    }

    private void createUIComponents() {
        panel1 = new JPanel(new BorderLayout());

        // Top panel for welcome message and user info
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel userInfoPanel = new JPanel();
        JLabel welcomeLabel = new JLabel("Witaj użytkowniku", SwingConstants.CENTER);
        ID_UZYTKONIKA = new JLabel();
        ROLE = new JLabel(String.valueOf(SwingConstants.RIGHT));

        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.X_AXIS));
        userInfoPanel.add(ID_UZYTKONIKA);
        userInfoPanel.add(Box.createHorizontalGlue());
        userInfoPanel.add(welcomeLabel);
        userInfoPanel.add(Box.createHorizontalGlue());
        userInfoPanel.add(ROLE);

        topPanel.add(userInfoPanel, BorderLayout.NORTH);

        // Panel for LISTY
        JPanel listyPanel = new JPanel();
        JLabel listyLabel = new JLabel("LISTY", SwingConstants.CENTER);
        NAZWA_LISTY = new JComboBox<>();
        NAZWA_LISTY.setMaximumSize(NAZWA_LISTY.getPreferredSize());
        przejdzDoListy = new JButton("Przejdź do Listy");

        GroupLayout listyLayout = new GroupLayout(listyPanel);
        listyPanel.setLayout(listyLayout);
        listyLayout.setAutoCreateGaps(true);
        listyLayout.setAutoCreateContainerGaps(true);

        listyLayout.setHorizontalGroup(
            listyLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(listyLabel)
                .addComponent(NAZWA_LISTY)
                .addComponent(przejdzDoListy)
        );

        listyLayout.setVerticalGroup(
            listyLayout.createSequentialGroup()
                .addComponent(listyLabel)
                .addComponent(NAZWA_LISTY)
                .addComponent(przejdzDoListy)
        );

        // Panel for SKLEPY
        JPanel sklepyPanel = new JPanel();
        JLabel sklepyLabel = new JLabel("SKLEPY", SwingConstants.CENTER);
        NAZWA_SKLEPU = new JComboBox<>();
        NAZWA_SKLEPU.setMaximumSize(NAZWA_SKLEPU.getPreferredSize());
        przejdzDoSklepuButton = new JButton("Przejdź do Sklepu");

        GroupLayout sklepyLayout = new GroupLayout(sklepyPanel);
        sklepyPanel.setLayout(sklepyLayout);
        sklepyLayout.setAutoCreateGaps(true);
        sklepyLayout.setAutoCreateContainerGaps(true);

        sklepyLayout.setHorizontalGroup(
            sklepyLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(sklepyLabel)
                .addComponent(NAZWA_SKLEPU)
                .addComponent(przejdzDoSklepuButton)
        );

        sklepyLayout.setVerticalGroup(
            sklepyLayout.createSequentialGroup()
                .addComponent(sklepyLabel)
                .addComponent(NAZWA_SKLEPU)
                .addComponent(przejdzDoSklepuButton)
        );

        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        contentPanel.add(listyPanel);
        contentPanel.add(Box.createHorizontalGlue());
        contentPanel.add(sklepyPanel);

        panel1.add(topPanel, BorderLayout.NORTH);
        panel1.add(contentPanel, BorderLayout.CENTER);

        setContentPane(panel1);
    }


}
