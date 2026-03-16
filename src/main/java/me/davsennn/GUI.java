package me.davsennn;

import me.davsennn.algorithm.Person;
import me.davsennn.algorithm.Room;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GUI {
    public static JFrame fenster;

    public static void update() { fenster.revalidate(); fenster.repaint(); }

    private static JPanel settingsPage;
    private static JPanel peoplePage;
    private static JPanel roomsPage;
    private static JPanel computePage;

    public static void createWindow() {
        fenster = new JFrame("roomdist Application 1.0.0");
        fenster.setSize(1200, 900);
        fenster.setVisible(true);
        fenster.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenster.setLayout(new BorderLayout(10, 10));

        JPanel navigation = new JPanel();
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.LINE_AXIS));
        navigation.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // navigation.setBackground(Color.BLACK);

        JTabbedPane tabs = new JTabbedPane();
        buildSettingsPage();
        buildPeoplePage();
        buildRoomsPage();
        buildComputePage();
        tabs.addTab("Settings", settingsPage);
        tabs.addTab("People", peoplePage);
        tabs.addTab("Rooms", roomsPage);
        tabs.addTab("Compute", computePage);
        // tabs.setBackground(Color.GRAY);

        navigation.add(tabs);

        fenster.add(navigation, BorderLayout.NORTH);
    }

    private static void log(String message) {
        JOptionPane.showConfirmDialog(fenster, message, "Info",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
    }

    private static JTextPane immutableText(String message, Color c) {
        JTextPane ret = new JTextPane();
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = ret.getDocument().getLength();
        ret.setCaretPosition(len);
        ret.setCharacterAttributes(aset, false);
        ret.replaceSelection(message);
        ret.setEditable(false);
        // ret.setBackground(Color.DARK_GRAY);
        return ret;
    }
    private static JTextPane immutableText(String message) { return immutableText(message, Color.BLACK); }
    private static void buildSettingsPage() {
        System.out.println("Building settings page");

        settingsPage = new JPanel();
        settingsPage.setLayout(new BoxLayout(settingsPage, BoxLayout.PAGE_AXIS));
        // settingsPage.setBackground(Color.DARK_GRAY);

        JPanel pairParamSettings = new JPanel();
        pairParamSettings.setLayout(new GridLayout(6, 3, 10, 5));
        // pairParamSettings.setBackground(Color.DARK_GRAY);

        JSpinner preferenceSelector =         new JSpinner(new SpinnerNumberModel(Config.getPreferenceBonus()          , -999.0, 999.0, 0.5));
        JSpinner mutualPreferenceSelector =   new JSpinner(new SpinnerNumberModel(Config.getMutualPreferenceBonus()    , -999.0, 999.0, 0.5));
        JSpinner ageDifferenceSelector =      new JSpinner(new SpinnerNumberModel(Config.getAgeDifferencePenalty()     , -999.0, 999.0, 0.5));
        JSpinner largeAgeDifferenceSelector = new JSpinner(new SpinnerNumberModel(Config.getLargeAgeDifferencePenalty(), -999.0, 999.0, 0.5));
        JSpinner locationSelector =           new JSpinner(new SpinnerNumberModel(Config.getSameLocationBonus()        , -999.0, 999.0, 0.5));
        JSpinner genderSelector =             new JSpinner(new SpinnerNumberModel(Config.getSameGenderBonus()          , -999.0, 999.0, 0.5));

        pairParamSettings.add(immutableText("PREFERENCE BONUS"));            pairParamSettings.add(preferenceSelector);          pairParamSettings.add(immutableText("Pts"));
        pairParamSettings.add(immutableText("MUTUAL PREFERENCE BONUS"));     pairParamSettings.add(mutualPreferenceSelector);    pairParamSettings.add(immutableText("Pts"));
        pairParamSettings.add(immutableText("AGE DIFFERENCE PENALTY"));      pairParamSettings.add(ageDifferenceSelector);       pairParamSettings.add(immutableText("Pts per year of difference"));
        pairParamSettings.add(immutableText("LARGE AGE DIFFERENCE PENALTY"));pairParamSettings.add(largeAgeDifferenceSelector);  pairParamSettings.add(immutableText("Pts per year of difference"));
        pairParamSettings.add(immutableText("SAME LOCATION BONUS"));         pairParamSettings.add(locationSelector);            pairParamSettings.add(immutableText("Pts"));
        pairParamSettings.add(immutableText("SAME GENDER BONUS"));           pairParamSettings.add(genderSelector);              pairParamSettings.add(immutableText("Pts"));

        settingsPage.add(pairParamSettings);
        settingsPage.add(Box.createVerticalStrut(20));

        JPanel groupParamSettings = new JPanel();
        groupParamSettings.setLayout(new GridLayout(3, 3, 10, 5));
        // groupParamSettings.setBackground(Color.DARK_GRAY);

        JSpinner largeGroupSelector = new JSpinner(new SpinnerNumberModel(Config.getLargeGroupBonus(), -999.0, 999.0, 0.5));
        JSpinner underoccupancySelector = new JSpinner(new SpinnerNumberModel(Config.getUnderOccupancyPenalty(), -999.0, 999.0, 0.5));
        JSpinner criticalOccupancySelector = new JSpinner(new SpinnerNumberModel(Config.getCriticalOccupancyPenalty(), -999.0, 999.0, 0.5));

        groupParamSettings.add(immutableText("LARGE GROUP BONUS"));         groupParamSettings.add(largeGroupSelector);         groupParamSettings.add(immutableText("Punkte"));
        groupParamSettings.add(immutableText("UNDEROCCUPANCY PENALTY"));    groupParamSettings.add(underoccupancySelector);     groupParamSettings.add(immutableText("Punkte"));
        groupParamSettings.add(immutableText("CRITICAL OCCUPANCY PENALTY"));groupParamSettings.add(criticalOccupancySelector);  groupParamSettings.add(immutableText("Punkte"));

        settingsPage.add(groupParamSettings);
        settingsPage.add(Box.createVerticalStrut(20));

        JPanel ageSettings = new JPanel();
        ageSettings.setLayout(new GridLayout(2, 3, 10, 5));
        // ageSettings.setBackground(Color.DARK_GRAY);

        JSpinner ageDifferenceThresholdSelector = new JSpinner(new SpinnerNumberModel(Config.getAgeDifferenceThreshold(), 0, 999.0, 0.2));
        JSpinner largeAgeDifferenceThresholdSelector = new JSpinner(new SpinnerNumberModel(Config.getLargeAgeDifferenceThreshold(), 0, 999.0, 0.2));

        ageSettings.add(immutableText("AGE DIFFERENCE THRESHOLD"));         ageSettings.add(ageDifferenceThresholdSelector);        ageSettings.add(immutableText("Jahre"));
        ageSettings.add(immutableText("LARGE AGE DIFFERENCE THRESHOLD"));   ageSettings.add(largeAgeDifferenceThresholdSelector);   ageSettings.add(immutableText("Jahre"));

        settingsPage.add(ageSettings);
        settingsPage.add(Box.createVerticalStrut(20));

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(1, 2, 10, 0));
        // buttons.setBackground(Color.DARK_GRAY);

        JButton defaults = new JButton("Reset");
        defaults.addActionListener(ignored -> {
            Config.setDefaults();

            preferenceSelector.setValue(2.0);
            mutualPreferenceSelector.setValue(5.0);
            ageDifferenceSelector.setValue(1.0);
            largeAgeDifferenceSelector.setValue(2.0);
            locationSelector.setValue(1.0);
            genderSelector.setValue(2.0);

            largeGroupSelector.setValue(0.0);
            underoccupancySelector.setValue(1.0);
            criticalOccupancySelector.setValue(10.0);

            ageDifferenceThresholdSelector.setValue(2.0);
            largeAgeDifferenceThresholdSelector.setValue(3.0);

            log("Reset to defaults");
        });

        JButton apply = new JButton("Apply");
        apply.addActionListener(ignored -> {
            Config.setPreferenceBonus((double) preferenceSelector.getValue());
            Config.setMutualPreferenceBonus((double) mutualPreferenceSelector.getValue());
            Config.setAgeDifferencePenalty((double) ageDifferenceSelector.getValue());
            Config.setLargeAgeDifferencePenalty((double) largeAgeDifferenceSelector.getValue());
            Config.setSameLocationBonus((double) locationSelector.getValue());
            Config.setSameGenderBonus((double) genderSelector.getValue());

            Config.setLargeGroupBonus((double) largeGroupSelector.getValue());
            Config.setUnderOccupancyPenalty((double) underoccupancySelector.getValue());
            Config.setCriticalOccupancyPenalty((double) criticalOccupancySelector.getValue());

            Config.setAgeDifferenceThreshold((double) ageDifferenceThresholdSelector.getValue());
            Config.setLargeAgeDifferenceThreshold((double) largeAgeDifferenceThresholdSelector.getValue());

            log("Parameters were set.");
        });

        buttons.add(defaults);
        buttons.add(apply);

        settingsPage.add(buttons);
    }

    private static void buildPeoplePage() {
        peoplePage = new JPanel();
        peoplePage.setLayout(new BoxLayout(peoplePage, BoxLayout.PAGE_AXIS));

        AbstractTableModel model = new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return Person.getPeople().size();
            }

            @Override
            public int getColumnCount() {
                return 6;
            }

            @Override
            public String getColumnName(int col) {
                return switch (col) {
                    case 0 -> "Group";
                    case 1 -> "Name";
                    case 2 -> "Birth";
                    case 3 -> "Gender";
                    case 4 -> "Location";
                    case 5 -> "Preferences";
                    default -> null;
                };
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                List<Person> people = Person.getPeople();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-uuuu");
                return switch (columnIndex) {
                    case 0 -> people.get(rowIndex).getGroup();
                    case 1 -> people.get(rowIndex).getName();
                    case 2 -> people.get(rowIndex).getBirth().format(formatter);
                    case 3 -> people.get(rowIndex).getGender();
                    case 4 -> people.get(rowIndex).getLocation();
                    case 5 -> people.get(rowIndex).getPreferences().stream().map(Person::getName).toList().toString();
                    default -> null;
                };
            }
        };
        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(50);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(500);

        JPanel importPanel = new JPanel();
        JTextPane importText = immutableText("Import people from File");
        importText.setToolTipText("Import people data from CSV file. Format should be \n" +
                "{name}, {birth}[MM-YYYY], {location}, {gender}[f|m|d], {preferences}[name1;name2;...], {group}");
        importPanel.add(importText);

        JFileChooser importFileChooser = new JFileChooser();
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV Spreadsheets", "csv");
        importFileChooser.setFileFilter(csvFilter);

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(ignored -> {
            int returnVal = importFileChooser.showOpenDialog(importPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                Person.addPeople(Main.parse(importFileChooser.getSelectedFile()));
                model.fireTableDataChanged();
            }
        });
        JButton test = new JButton("Test");
        test.addActionListener(ignored -> {
            File file = new File("C:\\Users\\david\\IdeaProjects\\roomdist\\src\\main\\resources\\people.csv");
            Person.addPeople(Main.parse(file));
            model.fireTableDataChanged();
        });

        importPanel.add(browseButton);
        importPanel.add(test);

        peoplePage.add(importPanel);

        JPanel manualPanel = new JPanel();
        manualPanel.setLayout(new BoxLayout(manualPanel, BoxLayout.PAGE_AXIS));
        manualPanel.setBackground(Color.LIGHT_GRAY);

        JPanel groupPanel = new JPanel();
        groupPanel.add(immutableText("Group (one character)"));
        JTextArea group = new JTextArea("");
        group.setPreferredSize(new Dimension(12, 20));
        groupPanel.add(group);
        groupPanel.setBackground(Color.LIGHT_GRAY);

        JPanel namePanel = new JPanel();
        namePanel.add(immutableText("Name"));
        JTextArea name = new JTextArea();
        name.setPreferredSize(new Dimension(300, 20));
        namePanel.add(name);
        namePanel.setBackground(Color.LIGHT_GRAY);

        JPanel birthPanel = new JPanel();
        birthPanel.add(immutableText("Birth Month (MM YYYY)"));
        JSpinner birthMonth = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        JSpinner birthYear = new JSpinner(new SpinnerNumberModel(2015, 1950, 2030, 1));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(birthYear, "#");
        birthYear.setEditor(editor);
        birthPanel.add(birthMonth);
        birthPanel.add(birthYear);
        birthPanel.setBackground(Color.LIGHT_GRAY);

        JPanel locationPanel = new JPanel();
        locationPanel.add(immutableText("Location"));
        JTextArea location = new JTextArea();
        location.setPreferredSize(new Dimension(300, 20));
        locationPanel.add(location);
        locationPanel.setBackground(Color.LIGHT_GRAY);

        JPanel genderPanel = new JPanel();
        genderPanel.add(immutableText("Gender (m|f|d)"));
        JTextArea gender = new JTextArea();
        gender.setPreferredSize(new Dimension(12, 20));
        genderPanel.add(gender);
        genderPanel.setBackground(Color.LIGHT_GRAY);

        JPanel preferencePanel = new JPanel();
        preferencePanel.add(immutableText("Preferences (\"name1, name2, name3, ...\")"));
        JTextArea preferences = new JTextArea();
        preferences.setPreferredSize(new Dimension(500, 40)); // why i need 40, dont know dont care
        preferences.setLineWrap(true);
        preferencePanel.add(preferences);
        preferencePanel.setBackground(Color.LIGHT_GRAY);

        JButton addManual = new JButton("Add");
        addManual.addActionListener(ignored -> {
            try {
                char groupv = group.getText().charAt(0);
                String namev = name.getText().trim();
                YearMonth birthv = YearMonth.of((int) birthYear.getValue(), (int) birthMonth.getValue());
                String locationv = location.getText().trim();
                char genderv = gender.getText().charAt(0);
                List<Person> preferencesv = new ArrayList<>();
                for (String pref : preferences.getText().split(",")) {
                    Person prefPerson = Person.fromName(pref.trim());
                    if (prefPerson == null) {
                        log(pref + " does not exist. This person was not added to preferences list of " + namev);
                        continue;
                    }
                    preferencesv.add(prefPerson);
                }

                new Person(namev, birthv, locationv, genderv, preferencesv, groupv);
                model.fireTableDataChanged();
            } catch (Exception e) {
                log("Insufficient information. \n Stacktrace \n" + e.getMessage());
            }
        });

        manualPanel.add(groupPanel);
        manualPanel.add(namePanel);
        manualPanel.add(birthPanel);
        manualPanel.add(locationPanel);
        manualPanel.add(genderPanel);
        manualPanel.add(preferencePanel);
        manualPanel.add(addManual);

        peoplePage.add(manualPanel);

        JPanel buttons = new JPanel();
        JButton export = new JButton("Export");
        export.addActionListener(ignored -> {
            JFileChooser saveFileChooser = new JFileChooser();
            saveFileChooser.setFileFilter(csvFilter);
            int returnValue = saveFileChooser.showSaveDialog(fenster);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(saveFileChooser.getSelectedFile()));
                    writer.write(Person.everyone());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    JOptionPane.showConfirmDialog(fenster, "Failed to export data", "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttons.add(export);

        JButton viewAll = new JButton("View All");
        viewAll.addActionListener(ignored -> log(Person.everyone()));
        buttons.add(viewAll);

        JButton deleteAll = new JButton("Delete All");
        deleteAll.addActionListener(ignored -> {
            int returnValue = JOptionPane.showConfirmDialog(fenster, "This will delete all people! Are you sure you want to proceed?",
                    "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (returnValue == JOptionPane.YES_OPTION) {
                Person.clearPeople();
            }
        });
        buttons.add(deleteAll);

        JButton everyone = new JButton("Print all to console");
        everyone.addActionListener(ignored -> {
            System.out.println(Person.getPeople().toString());
            // System.out.println(Person.everyone());
        });
        buttons.add(everyone);


        peoplePage.add(buttons);
        peoplePage.add(new JScrollPane(table));
    }

    private static void buildRoomsPage() {
        roomsPage = new JPanel();
        roomsPage.setLayout(new BoxLayout(roomsPage, BoxLayout.PAGE_AXIS));

        AbstractTableModel model = new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return Room.getRooms().size();
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public String getColumnName(int col) {
                return switch (col) {
                    case 0 -> "RoomID";
                    case 1 -> "Capacity";
                    default -> null;
                };
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> Room.getRooms().get(rowIndex).getId();
                    case 1 -> Room.getRooms().get(rowIndex).getCapacity();
                    default -> null;
                };
            }
        };
        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);

        JPanel importPanel = new JPanel();
        JTextPane importText = immutableText("Import rooms from File");
        importText.setToolTipText("Import rooms data from CSV file. Format should be \n" +
                "{RoomID},{Capacity}");
        importPanel.add(importText);

        JFileChooser importFileChooser = new JFileChooser();
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV Spreadsheets", "csv");
        importFileChooser.setFileFilter(csvFilter);

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(ignored -> {
            int returnVal = importFileChooser.showOpenDialog(importPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                Room.addRooms(Main.parseRooms(importFileChooser.getSelectedFile()));
                model.fireTableDataChanged();
            }
        });
        JButton test = new JButton("Test");
        test.addActionListener(ignored -> {
            File file = new File("C:\\Users\\david\\IdeaProjects\\roomdist\\src\\main\\resources\\rooms.csv");
            Room.addRooms(Main.parseRooms(file));
            model.fireTableDataChanged();
        });
        importPanel.add(browseButton);
        importPanel.add(test);
        roomsPage.add(importPanel);

        JPanel manualPanel = new JPanel();
        manualPanel.setLayout(new BoxLayout(manualPanel, BoxLayout.PAGE_AXIS));
        manualPanel.setBackground(Color.LIGHT_GRAY);

        JPanel roomIdPanel = new JPanel();
        roomIdPanel.setBackground(Color.LIGHT_GRAY);
        roomIdPanel.add(immutableText("RoomID"));
        JTextArea roomId = new JTextArea();
        roomId.setPreferredSize(new Dimension(100, 20));
        roomIdPanel.add(roomId);
        manualPanel.add(roomIdPanel);

        JPanel capacityPanel = new JPanel();
        capacityPanel.setBackground(Color.LIGHT_GRAY);
        capacityPanel.add(immutableText("Capacity"));
        JSpinner capacity = new JSpinner(new SpinnerNumberModel(5, 0, 30, 1));
        capacityPanel.add(capacity);
        manualPanel.add(capacityPanel);

        JButton manualAdd = new JButton("Add");
        manualAdd.addActionListener(ignored -> {
            String roomIdv = roomId.getText().trim();
            int capacityv = (int) capacity.getValue();
            new Room(roomIdv, capacityv);
            model.fireTableDataChanged();
        });
        manualPanel.add(manualAdd);

        roomsPage.add(manualPanel);

        JPanel buttons = new JPanel();
        JButton export = new JButton("Export");
        export.addActionListener(ignored -> {
            JFileChooser saveFileChooser = new JFileChooser();
            saveFileChooser.setFileFilter(csvFilter);
            int returnValue = saveFileChooser.showSaveDialog(fenster);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(saveFileChooser.getSelectedFile()));
                    writer.write(Room.everywhere());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    JOptionPane.showConfirmDialog(fenster, "Failed to export data", "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttons.add(export);

        JButton viewAll = new JButton("View All");
        viewAll.addActionListener(ignored -> {log(Room.everywhere()); update();});
        buttons.add(viewAll);

        JButton deleteAll = new JButton("Delete All");
        deleteAll.addActionListener(ignored -> {
            int returnValue = JOptionPane.showConfirmDialog(fenster, "This will delete all rooms! Are you sure you want to proceed?",
                    "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (returnValue == JOptionPane.YES_OPTION) {
                Room.clearRooms();
                model.fireTableDataChanged();
            }
        });
        buttons.add(deleteAll);

        JButton everyone = new JButton("Print all to console");
        everyone.addActionListener(ignored -> {
            System.out.println(Room.getRooms().toString());
        });
        buttons.add(everyone);

        roomsPage.add(buttons);

        roomsPage.add(new JScrollPane(table));

    }

    private static void buildComputePage() {
        computePage = new JPanel();

    }
}
