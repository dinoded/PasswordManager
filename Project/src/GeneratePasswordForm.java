import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class GeneratePasswordForm extends JDialog {
    private JPanel panel1;
    private JButton generatePasswordButton;
    private JCheckBox uppercaseCheckBox;
    private JCheckBox lowercaseCheckBox;
    private JCheckBox numbersCheckBox;
    private JCheckBox symbolsCheckBox;
    private JComboBox<String> comboBox2; // Используем comboBox2 вместо comboBox1
    private JProgressBar progressBar1;
    private JSpinner spinner1;
    private JTextField textField1;
    private JButton searchButton;
    private JButton addButton;
    private JLabel strengthLabel; // Новая метка для отображения силы пароля
    private String[] passwordNames;
    private FormMain formMain;
    private String generatedPassword;
    private JPopupMenu suggestionPopup;

    public GeneratePasswordForm(FormMain owner, String[] passwordNames) {
        super(owner, "Generate Password", true);
        this.formMain = owner;
        this.passwordNames = passwordNames;

        // Устанавливаем содержимое панели и свойства формы
        setContentPane(panel1);
        setModal(true);
        setResizable(true); // Разрешаем изменение размера формы
        getRootPane().setDefaultButton(generatePasswordButton);

        // Загружаем имена паролей в comboBox2
        for (String name : passwordNames) {
            comboBox2.addItem(name);
        }

        spinner1.setModel(new SpinnerNumberModel(8, 1, 64, 1)); // Длина по умолчанию 8, мин 1, макс 64

        generatePasswordButton.addActionListener(e -> generatePassword());
        searchButton.addActionListener(e -> searchPassword());
        addButton.addActionListener(e -> addPasswordToTable());
        addButton.setEnabled(false); // Изначально отключаем кнопку добавления

        // Добавляем слушатель изменений к спиннеру для обновления сложности пароля
        spinner1.addChangeListener(e -> updatePasswordStrength());

        // Настройка автозаполнения
        setupAutoComplete();

        pack(); // Упаковываем компоненты
        setSize(600, 400); // Устанавливаем размер формы
        setLocationRelativeTo(owner); // Центрируем форму относительно владельца
    }

    private void setupAutoComplete() {
        suggestionPopup = new JPopupMenu();

        textField1.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                showSuggestions();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                showSuggestions();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                showSuggestions();
            }
        });

        textField1.addActionListener(e -> applySuggestion());
    }

    private void showSuggestions() {
        String text = textField1.getText();
        suggestionPopup.removeAll();

        if (text.isEmpty()) {
            suggestionPopup.setVisible(false);
            return;
        }

        List<String> suggestions = new ArrayList<>();
        for (String name : passwordNames) {
            if (name.toLowerCase().startsWith(text.toLowerCase())) {
                suggestions.add(name);
            }
        }

        if (suggestions.isEmpty()) {
            suggestionPopup.setVisible(false);
        } else {
            for (String suggestion : suggestions) {
                JMenuItem item = new JMenuItem(suggestion);
                item.addActionListener(e -> {
                    textField1.setText(suggestion);
                    suggestionPopup.setVisible(false);
                });
                suggestionPopup.add(item);
            }

            suggestionPopup.show(textField1, 0, textField1.getHeight());
        }
    }

    private void applySuggestion() {
        if (suggestionPopup.isVisible()) {
            JMenuItem selectedItem = (JMenuItem) suggestionPopup.getComponent(0);
            textField1.setText(selectedItem.getText());
            suggestionPopup.setVisible(false);
        }
    }

    private void generatePassword() {
        int length = (int) spinner1.getValue();
        boolean useUppercase = uppercaseCheckBox.isSelected();
        boolean useLowercase = lowercaseCheckBox.isSelected();
        boolean useNumbers = numbersCheckBox.isSelected();
        boolean useSymbols = symbolsCheckBox.isSelected();

        if (!useUppercase && !useLowercase && !useNumbers && !useSymbols) {
            JOptionPane.showMessageDialog(this, "Please select at least one character type.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        generatedPassword = generatePassword(length, useUppercase, useLowercase, useNumbers, useSymbols);

        // Обновляем индикатор сложности пароля
        int strength = calculatePasswordStrength(generatedPassword);
        progressBar1.setValue(strength);
        updateStrengthLabel(strength);

        // Показываем сгенерированный пароль в диалоговом окне
        JOptionPane.showMessageDialog(this, "Generated Password: " + generatedPassword);

        // Включаем кнопку добавления после генерации пароля
        addButton.setEnabled(true);
    }


    private String generatePassword(int length, boolean useUppercase, boolean useLowercase, boolean useNumbers, boolean useSymbols) {
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String symbols = "!@#$%^&*()-_=+[]{}|;:,.<>?/";

        StringBuilder characterPool = new StringBuilder();
        if (useUppercase) {
            characterPool.append(uppercase);
        }
        if (useLowercase) {
            characterPool.append(lowercase);
        }
        if (useNumbers) {
            characterPool.append(numbers);
        }
        if (useSymbols) {
            characterPool.append(symbols);
        }

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characterPool.length());
            password.append(characterPool.charAt(index));
        }

        return password.toString();
    }

    private int calculatePasswordStrength(String password) {
        int length = password.length();
        int criteria = 0;

        if (password.matches(".*[A-Z].*")) criteria++;
        if (password.matches(".*[a-z].*")) criteria++;
        if (password.matches(".*[0-9].*")) criteria++;
        if (password.matches(".*[!@#$%^&*()\\-_=+\\[{\\]};:'\",<.>/?].*")) criteria++;

        // Максимальная длина для полного прогресса - 64 символа
        int maxCriteria = 4;
        int maxLength = 64;
        int strengthPercentage = (int) (((double) length / maxLength) * 100) * criteria / maxCriteria;

        return Math.min(strengthPercentage, 100);
    }

    private void updatePasswordStrength() {
        int length = (int) spinner1.getValue();
        StringBuilder samplePassword = new StringBuilder();

        if (uppercaseCheckBox.isSelected()) samplePassword.append("A");
        if (lowercaseCheckBox.isSelected()) samplePassword.append("a");
        if (numbersCheckBox.isSelected()) samplePassword.append("1");
        if (symbolsCheckBox.isSelected()) samplePassword.append("!");

        while (samplePassword.length() < length) {
            samplePassword.append("x");
        }

        int strength = calculatePasswordStrength(samplePassword.toString());
        progressBar1.setValue(strength);
        updateStrengthLabel(strength);
    }

    private void updateStrengthLabel(int strength) {
        String strengthText;
        if (strength <= 33) {
            strengthText = "Слабый пароль";
        } else if (strength <= 66) {
            strengthText = "Средний пароль";
        } else {
            strengthText = "Сильный пароль";
        }
        strengthLabel.setText(strengthText);
    }

    private void searchPassword() {
        String searchText = textField1.getText().toLowerCase();
        for (String name : passwordNames) {
            if (name.toLowerCase().contains(searchText)) {
                comboBox2.setSelectedItem(name);
                break;
            }
        }
    }

    private void addPasswordToTable() {
        String selectedName = (String) comboBox2.getSelectedItem();
        formMain.addPasswordToTable(selectedName, generatedPassword); // Используем сгенерированный пароль
    }
}
