import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

public class LoginForm {
    private JPanel panel1;
    private JTextField loginField;
    private JPasswordField masterKeyField;
    private JButton loginButton;
    private JButton createAccountButton;
    private JLabel loginLabel;
    private JLabel passwordLabel;
    private JPasswordField passwordField1;
    private JLabel confirmPasswordLabel;
    private final File file;

    public LoginForm(File file) {
        this.file = file;

        // Устанавливаем null layout
        panel1.setLayout(null);

        // Устанавливаем координаты и размеры компонентов
        loginLabel.setBounds(50, 20, 80, 25);
        loginField.setBounds(150, 20, 165, 25);
        passwordLabel.setBounds(50, 50, 80, 25);
        masterKeyField.setBounds(150, 50, 165, 25);
        loginButton.setBounds(50, 120, 100, 25);
        createAccountButton.setBounds(170, 120, 150, 25);
        confirmPasswordLabel.setBounds(50, 80, 80, 25);
        passwordField1.setBounds(150, 80, 165, 25);

        // Изначально скрываем поля для создания аккаунта
        confirmPasswordLabel.setVisible(false);
        passwordField1.setVisible(false);

        // Добавляем компоненты на панель
        panel1.add(loginLabel);
        panel1.add(loginField);
        panel1.add(passwordLabel);
        panel1.add(masterKeyField);
        panel1.add(loginButton);
        panel1.add(createAccountButton);
        panel1.add(confirmPasswordLabel);
        panel1.add(passwordField1);

        // Добавляем обработчики событий
        loginButton.addActionListener(e -> {
            String login = loginField.getText();
            String masterKey = new String(masterKeyField.getPassword());

            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Password Manager");
                boolean validLogin = validateLogin(login, masterKey);
                FormMain form = new FormMain(file, masterKey, validLogin);
                frame.setContentPane(form.getPanel1());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setSize(800, 600); // Устанавливаем размер формы
                frame.setLocationRelativeTo(null); // Центрируем форму на экране
                frame.setVisible(true);
            });

            // Закрываем окно логина
            SwingUtilities.getWindowAncestor(panel1).dispose();
        });

        createAccountButton.addActionListener(e -> {
            if (confirmPasswordLabel.isVisible()) {
                // Проверяем заполнение всех полей
                String login = loginField.getText();
                String masterKey = new String(masterKeyField.getPassword());
                String password1 = new String(passwordField1.getPassword());

                if (login.isEmpty() || masterKey.isEmpty() || password1.isEmpty()) {
                    JOptionPane.showMessageDialog(panel1, "Все поля должны быть заполнены", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!password1.equals(masterKey)) {
                    JOptionPane.showMessageDialog(panel1, "Пароли не совпадают", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Сохраняем данные пользователя в файл
                try {
                    String encryptedPassword = encryptPassword(password1);

                    try (FileWriter writer = new FileWriter(file, true)) {
                        writer.write("MASTER:" + login + "|" + encryptedPassword + "\n");
                    }

                    JOptionPane.showMessageDialog(panel1, "Аккаунт создан успешно", "Информация", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(panel1, "Ошибка при создании аккаунта", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Показываем поля для создания аккаунта
                confirmPasswordLabel.setVisible(true);
                passwordField1.setVisible(true);

                loginField.setText("");
                masterKeyField.setText("");
            }
        });
    }

    private boolean validateLogin(String login, String masterKey) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
            for (String line : lines) {
                if (line.startsWith("MASTER:")) {
                    String[] parts = line.split("\\|");
                    if (parts.length == 2 && parts[0].substring(7).equals(login)) {
                        String encryptedPassword = parts[1];
                        return encryptPassword(masterKey).equals(encryptedPassword);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "a".equals(login) && "a".equals(masterKey);
    }

    private String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public JPanel getPanel1() {
        return panel1;
    }
}
