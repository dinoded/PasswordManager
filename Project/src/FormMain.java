import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FormMain extends JFrame {
    private JPanel panel1;
    private JTable table1;
    private JButton clearButton;
    private JButton updateButton;
    private JTextField textField1;
    private JButton searchButton;
    private JButton deleteButton;
    private JButton addButton;
    private JComboBox<String> comboBox1;
    private JComboBox<String> categoryComboBox;
    private JLabel lastDecryptionLabel;
    private String lastDecryptionDate;
    private File currentFile;
    private JPanel MenuPanel;
    private JComboBox<String> comboBox2;
    private JComboBox<String> comboBox3;
    private JButton generatePasswordButton;
    private TableRowSorter<TableModel> sorter;
    private JPopupMenu suggestionPopup;

    private final int shift = 3;

    public FormMain(File file, String masterKey, boolean validLogin) {
        this.currentFile = file;
        initComponents();
        loadDataFromFile(file, masterKey, validLogin);
        setupClearButton();
        setupAddButton();
        setupUpdateButton();
        setupDeleteButton();
        setupSearchButton();
        setupSorting();
        setupComboBox2();
        setupCategoryComboBox();
        setupComboBox3Actions();
        setupSecondarySortingComboBoxes();
        setupGeneratePasswordButton();
        setupAutoComplete();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        comboBox1 = new JComboBox<>();
        lastDecryptionLabel = new JLabel();

        // Column Names
        String[] columnNames = { "Name", "Password", "Category", "Login", "Website/Service" };

        // Initializing the JTable with an empty model initially
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        table1.setModel(model);
        sorter = new TableRowSorter<>(table1.getModel());
        table1.setRowSorter(sorter);

        // Заполнение comboBox1 именами столбцов
        for (String columnName : columnNames) {
            comboBox1.addItem(columnName);

        }

        // Setup comboBox2 items
        comboBox2.addItem("File");
        comboBox2.addItem("Open");
        comboBox2.addItem("Create");
        comboBox2.addItem("Save");

        // Setup comboBox3 items
        comboBox3.addItem("Category");
        comboBox3.addItem("Delete");
        comboBox3.addItem("Add");
        comboBox3.addItem("Replace");
    }

    private void setupClearButton() {
        clearButton.addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel) table1.getModel();
            if (model.getRowCount() > 0) {
                model.setRowCount(0); // Очищаем содержимое таблицы, кроме названий колонок
            } else {
                JOptionPane.showMessageDialog(panel1, "Таблица уже пуста", "Информация", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void setupAddButton() {
        addButton.addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel) table1.getModel();
            model.addRow(new Object[]{"", "", "", "", ""}); 
        });
    }

    private void setupUpdateButton() {
        updateButton.addActionListener(e -> encryptDataToFile());
    }

    private void setupDeleteButton() {
        deleteButton.addActionListener(e -> {
            int selectedRow = table1.getSelectedRow();
            if (selectedRow != -1) {
                DefaultTableModel model = (DefaultTableModel) table1.getModel();
                model.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(panel1, "Выберите строку для удаления", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void setupSearchButton() {
        searchButton.addActionListener(e -> {
            String searchText = textField1.getText().toLowerCase();
            DefaultTableModel model = (DefaultTableModel) table1.getModel();

            for (int i = 0; i < model.getRowCount(); i++) {
                boolean found = false;
                for (int j = 0; j < model.getColumnCount(); j++) {
                    String value = model.getValueAt(i, j).toString().toLowerCase();
                    if (value.contains(searchText)) {
                        table1.setRowSelectionInterval(i, i);
                        found = true;
                        break;
                    }
                }
                if (found) break;
                table1.clearSelection();
            }
        });
    }

    private void setupSorting() {
        comboBox1.addActionListener(e -> {
            String selectedColumn = (String) comboBox1.getSelectedItem();
            int columnIndex = getColumnIndex(selectedColumn);
            if (columnIndex != -1) {
                sorter.setSortKeys(List.of(new RowSorter.SortKey(columnIndex, SortOrder.ASCENDING)));
            }
        });
    }

    private void setupComboBox2() {
        comboBox2.addActionListener(e -> {
            String selectedOption = (String) comboBox2.getSelectedItem();
            switch (selectedOption) {
                case "Open":
                    JFileChooser fileChooser = new JFileChooser();
                    int result = fileChooser.showOpenDialog(panel1);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        currentFile = fileChooser.getSelectedFile();
                        loadDataFromFile(currentFile, null, true);
                    }
                    break;
                case "Create":
                    JFileChooser createFileChooser = new JFileChooser();
                    int createResult = createFileChooser.showSaveDialog(panel1);
                    if (createResult == JFileChooser.APPROVE_OPTION) {
                        currentFile = createFileChooser.getSelectedFile();
                        try {
                            if (currentFile.createNewFile()) {
                                JOptionPane.showMessageDialog(panel1, "Файл успешно создан", "Информация", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(panel1, "Файл уже существует", "Ошибка", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    break;
                case "Save":
                    encryptDataToFile();
                    break;
                default:
                    break;
            }
        });
    }

    private void setupCategoryComboBox() {
        categoryComboBox.addActionListener(e -> {
            String selectedCategory = (String) categoryComboBox.getSelectedItem();
            if (selectedCategory != null) {
                filterTableByCategory(selectedCategory);
            }
        });
    }

    private void setupComboBox3Actions() {
        comboBox3.addActionListener(e -> {
            String selectedOption = (String) comboBox3.getSelectedItem();
            switch (selectedOption) {
                case "Delete":
                    deleteCategory();
                    break;
                case "Add":
                    addCategory();
                    break;
                case "Replace":
                    replaceCategory();
                    break;
                default:
                    break;
            }
        });
    }

    private void deleteCategory() {
        String selectedCategory = (String) JOptionPane.showInputDialog(panel1, "Select category to delete:", "Delete Category", JOptionPane.QUESTION_MESSAGE, null, getCategoryList(), getCategoryList()[0]);
        if (selectedCategory != null) {
            DefaultTableModel model = (DefaultTableModel) table1.getModel();
            for (int i = model.getRowCount() - 1; i >= 0; i--) {
                if (model.getValueAt(i, 2).equals(selectedCategory)) {
                    model.removeRow(i);
                }
            }
            updateCategoryComboBox(getCategoryList());
        }
    }

    private void addCategory() {
        String newCategory = JOptionPane.showInputDialog(panel1, "Enter new category name:", "Add Category", JOptionPane.QUESTION_MESSAGE);
        if (newCategory != null && !newCategory.trim().isEmpty()) {
            categoryComboBox.addItem(newCategory);
        }
    }

    private void replaceCategory() {
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JComboBox<String> oldCategoryComboBox = new JComboBox<>(getCategoryList());
        JTextField newCategoryField = new JTextField();

        JLabel oldCategoryLabel = new JLabel("Old Category:");
        JLabel newCategoryLabel = new JLabel("New Category:");

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(oldCategoryLabel)
                        .addComponent(newCategoryLabel))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(oldCategoryComboBox)
                        .addComponent(newCategoryField)));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(oldCategoryLabel)
                        .addComponent(oldCategoryComboBox))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(newCategoryLabel)
                        .addComponent(newCategoryField)));

        int result = JOptionPane.showConfirmDialog(panel1, panel, "Replace Category", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String oldCategory = (String) oldCategoryComboBox.getSelectedItem();
            String newCategory = newCategoryField.getText().trim();

            if (oldCategory != null && !newCategory.isEmpty()) {
                DefaultTableModel model = (DefaultTableModel) table1.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    if (model.getValueAt(i, 2).equals(oldCategory)) {
                        model.setValueAt(newCategory, i, 2);
                    }
                }
                updateCategoryComboBox(getCategoryList());
            }
        }
    }

    private String[] getCategoryList() {
        List<String> categories = new ArrayList<>();
        DefaultTableModel model = (DefaultTableModel) table1.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String category = (String) model.getValueAt(i, 2);
            if (!categories.contains(category)) {
                categories.add(category);
            }
        }
        return categories.toArray(new String[0]);
    }

    private void setupSecondarySortingComboBoxes() {
        comboBox3.addActionListener(e -> applySorting());

    }

    private void applySorting() {
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        String primarySortColumn = (String) comboBox1.getSelectedItem();
        String secondarySortColumn1 = (String) comboBox3.getSelectedItem();


        int primaryIndex = getColumnIndex(primarySortColumn);
        int secondaryIndex1 = getColumnIndex(secondarySortColumn1);

        if (primaryIndex != -1) {
            sortKeys.add(new RowSorter.SortKey(primaryIndex, SortOrder.ASCENDING));
        }
        if (secondaryIndex1 != -1) {
            sortKeys.add(new RowSorter.SortKey(secondaryIndex1, SortOrder.ASCENDING));
        }


        sorter.setSortKeys(sortKeys);
    }

    private int getColumnIndex(String columnName) {
        switch (columnName) {
            case "Name":
                return 0;
            case "Password":
                return 1;
            case "Category":
                return 2;
            case "Login":
                return 3;
            case "Website/Service":
                return 4;
            default:
                return -1;
        }
    }

    private void filterTableByCategory(String category) {
        if (category.equals("All Categories")) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter(category, 2));
        }
    }

    private void loadDataFromFile(File file, String masterKey, boolean validLogin) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            DefaultTableModel model = (DefaultTableModel) table1.getModel();
            model.setRowCount(0); // Очищаем таблицу перед заполнением
            List<String> categories = new ArrayList<>();
            SecureRandom random = new SecureRandom();

            while ((line = br.readLine()) != null) {
                if (line.startsWith("Last Decryption:")) {
                    lastDecryptionDate = line.split("Last Decryption: ")[1];
                    lastDecryptionLabel.setText("Last Decryption: " + lastDecryptionDate); // Обновляем метку
                    continue; // Пропускаем эту строку
                }

                if (line.startsWith("MASTER:")) {
                    continue; // Пропускаем строки с мастер-логинами
                }

                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    String password = validLogin ? decrypt(parts[1], shift) : generateRandomPassword(random, 8);
                    model.addRow(new Object[]{parts[0], password, parts[2], parts[3], parts[4]});
                    if (!categories.contains(parts[2])) {
                        categories.add(parts[2]);
                    }
                }
            }

            updateCategoryComboBox(categories);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCategoryComboBox(List<String> categories) {
        categoryComboBox.removeAllItems();
        categoryComboBox.addItem("All Categories"); // Default option to show all rows
        for (String category : categories) {
            categoryComboBox.addItem(category);
        }
    }

    private void updateCategoryComboBox(String[] categories) {
        categoryComboBox.removeAllItems();
        categoryComboBox.addItem("All Categories"); // Default option to show all rows
        for (String category : categories) {
            categoryComboBox.addItem(category);
        }
    }

    public void encryptDataToFile() {
        try (FileWriter writer = new FileWriter(currentFile)) {
            // Сначала записываем дату последней расшифровки
            lastDecryptionDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
            writer.write("Last Decryption: " + lastDecryptionDate + "\n");
            lastDecryptionLabel.setText("Last Decryption: " + lastDecryptionDate);

            DefaultTableModel model = (DefaultTableModel) table1.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                String name = (String) model.getValueAt(i, 0);
                String password = (String) model.getValueAt(i, 1);
                String category = (String) model.getValueAt(i, 2);
                String login = (String) model.getValueAt(i, 3);
                String website = (String) model.getValueAt(i, 4);
                if (name != null && password != null && !name.isEmpty() && !password.isEmpty()) {
                    String encryptedPassword = encrypt(password, shift);
                    writer.write(name + "|" + encryptedPassword + "|" + category + "|" + login + "|" + website + "\n");
                }
            }

            JOptionPane.showMessageDialog(panel1, "Данные успешно сохранены", "Информация", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String encrypt(String data, int shift) {
        StringBuilder result = new StringBuilder();
        for (char character : data.toCharArray()) {
            if (Character.isLetter(character)) {
                char base = Character.isLowerCase(character) ? 'a' : 'A';
                result.append((char) ((character - base + shift) % 26 + base));
            } else if (Character.isDigit(character)) {
                result.append((char) ((character - '0' + shift) % 10 + '0'));
            } else {
                result.append(character);
            }
        }
        return result.toString();
    }

    private String decrypt(String data, int shift) {
        return encrypt(data, 26 - shift);
    }

    private String generateRandomPassword(SecureRandom random, int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:,.<>?";
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }

    private void setupGeneratePasswordButton() {
        generatePasswordButton.addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel) table1.getModel();
            List<String> passwordNames = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                String name = (String) model.getValueAt(i, 0);
                if (name != null && !name.trim().isEmpty()) {
                    passwordNames.add(name);
                }
            }
            GeneratePasswordForm generatePasswordForm = new GeneratePasswordForm(this, passwordNames.toArray(new String[0]));
            generatePasswordForm.setVisible(true);
        });
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
        for (int i = 0; i < table1.getRowCount(); i++) {
            String value = table1.getValueAt(i, 0).toString();
            if (value.toLowerCase().startsWith(text.toLowerCase())) {
                suggestions.add(value);
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

    public void addPasswordToTable(String name, String password) {
        DefaultTableModel model = (DefaultTableModel) table1.getModel();
        boolean nameExists = false;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals(name)) {
                model.setValueAt(password, i, 1);
                nameExists = true;
                break;
            }
        }
        if (!nameExists) {
            model.addRow(new Object[]{name, password, "", "", ""});
        }
    }

    public JPanel getPanel1() {
        return panel1;
    }
}
