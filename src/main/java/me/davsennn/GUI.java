package me.davsennn;

import me.davsennn.algorithm.Person;
import me.davsennn.algorithm.Result;
import me.davsennn.algorithm.Room;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
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
import java.util.*;
import java.util.List;

public class GUI {
    public static JFrame fenster;
    public static void update() { fenster.revalidate(); fenster.repaint(); }

    public static final String ROOT = System.getProperty("user.dir");

    public static final Locale[] locales = new Locale[]{ Locale.ENGLISH, Locale.GERMAN };
    public static Locale locale = Locale.getDefault();
    public static ResourceBundle resources;
    public static String get(String key) { return resources.getString(key); }

    private static JPanel settingsPage;
    private static JPanel peoplePage;
    private static JPanel roomsPage;
    private static JPanel computePage;

    private static FileNameExtensionFilter csvFilter;

    public static void createWindow() {
        fenster = new JFrame("roomdist Application " + Main.version);
        fenster.setSize(1200, 900);
        fenster.setVisible(true);
        fenster.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenster.setLayout(new BorderLayout(10, 10));

        JPanel navigation = new JPanel();
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.PAGE_AXIS));
        navigation.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // navigation.setBackground(Color.BLACK);

        JComboBox<Locale> localeSelector = new JComboBox<>(locales);
        localeSelector.setSelectedItem(locale);
        localeSelector.addActionListener(ignored -> {
            locale = (Locale) localeSelector.getSelectedItem();
            fenster.setVisible(false);
            createWindow();
            System.out.println(locale);
        });
        locale = (Locale) localeSelector.getSelectedItem();
        System.out.println(locale);
        resources = ResourceBundle.getBundle("lang", locale);

        csvFilter = new FileNameExtensionFilter(get("desc.csvfilter"), "csv");


        JTabbedPane tabs = new JTabbedPane();
        buildSettingsPage();
        buildPeoplePage();
        buildRoomsPage();
        buildComputePage();
        tabs.addTab(get("title.settings"), settingsPage);
        tabs.addTab(get("title.people"), peoplePage);
        tabs.addTab(get("title.rooms"), roomsPage);
        tabs.addTab(get("title.compute"), computePage);
        // tabs.setBackground(Color.GRAY);

        navigation.add(localeSelector);
        navigation.add(tabs);

        fenster.add(navigation, BorderLayout.NORTH);
    }

    private static void logDirect(String msg) {
        JOptionPane.showConfirmDialog(fenster, msg, get("title.info"),
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
    }
    private static void log(String key) { logDirect(get(key)); }
    private static void log(String... keys) {
        StringBuilder ret = new StringBuilder();
        for (String key : keys) {
            ret.append(key.isEmpty() ? "" : get(key)).append("\n");
        }
        ret.deleteCharAt(ret.length()-1);
        logDirect(ret.toString());
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
    private static JTextPane immutableGet(String key) { return immutableText(get(key)); }
    private static void buildSettingsPage() {
        settingsPage = new JPanel();
        settingsPage.setLayout(new BoxLayout(settingsPage, BoxLayout.PAGE_AXIS));
        // settingsPage.setBackground(Color.DARK_GRAY);

        JPanel pairParamSettings = new JPanel();
        pairParamSettings.setLayout(new GridLayout(8, 3, 10, 5));
        // pairParamSettings.setBackground(Color.DARK_GRAY);

        JSpinner preferenceSelector =           new JSpinner(new SpinnerNumberModel(Config.getPreferenceBonus()             , -999.0, 999.0, 0.5));
        JSpinner nonPreferenceSelector =        new JSpinner(new SpinnerNumberModel(Config.getNonPreferencePenalty()        , -999.0, 999.0, 0.5));
        JSpinner unfulfilledPreferenceSelector =new JSpinner(new SpinnerNumberModel(Config.getUnfulfilledPreferencePenalty(), -999.0, 999.0, 0.5));
        JSpinner mutualPreferenceSelector =     new JSpinner(new SpinnerNumberModel(Config.getMutualPreferenceBonus()       , -999.0, 999.0, 0.5));
        JSpinner ageDifferenceSelector =        new JSpinner(new SpinnerNumberModel(Config.getAgeDifferencePenalty()        , -999.0, 999.0, 0.5));
        JSpinner largeAgeDifferenceSelector =   new JSpinner(new SpinnerNumberModel(Config.getLargeAgeDifferencePenalty()   , -999.0, 999.0, 0.5));
        JSpinner locationSelector =             new JSpinner(new SpinnerNumberModel(Config.getSameLocationBonus()           , -999.0, 999.0, 0.5));
        JSpinner genderSelector =               new JSpinner(new SpinnerNumberModel(Config.getSameGenderBonus()             , -999.0, 999.0, 0.5));

        pairParamSettings.add(immutableGet("set.preference"));               pairParamSettings.add(preferenceSelector);          pairParamSettings.add(immutableGet("set.pts"));
        pairParamSettings.add(immutableGet("set.nonPreference"));         pairParamSettings.add(nonPreferenceSelector);       pairParamSettings.add(immutableGet("set.pts"));
        pairParamSettings.add(immutableGet("set.unfulfilledPreference")); pairParamSettings.add(unfulfilledPreferenceSelector);pairParamSettings.add(immutableGet("set.pts"));
        pairParamSettings.add(immutableGet("set.mutualPreference"));        pairParamSettings.add(mutualPreferenceSelector);    pairParamSettings.add(immutableGet("set.pts"));
        pairParamSettings.add(immutableGet("set.ageDiff"));         pairParamSettings.add(ageDifferenceSelector);       pairParamSettings.add(immutableGet("set.ptsperyear"));
        pairParamSettings.add(immutableGet("set.largeAgeDiff"));   pairParamSettings.add(largeAgeDifferenceSelector);  pairParamSettings.add(immutableGet("set.ptsperyear"));
        pairParamSettings.add(immutableGet("set.sameLocation"));            pairParamSettings.add(locationSelector);            pairParamSettings.add(immutableGet("set.pts"));
        pairParamSettings.add(immutableGet("set.sameGender"));              pairParamSettings.add(genderSelector);              pairParamSettings.add(immutableGet("set.pts"));

        settingsPage.add(pairParamSettings);
        settingsPage.add(Box.createVerticalStrut(20));

        JPanel groupParamSettings = new JPanel();
        groupParamSettings.setLayout(new GridLayout(3, 3, 10, 5));
        // groupParamSettings.setBackground(Color.DARK_GRAY);

        JSpinner largeGroupSelector = new JSpinner(new SpinnerNumberModel(Config.getLargeGroupBonus(), -999.0, 999.0, 0.5));
        JSpinner underoccupancySelector = new JSpinner(new SpinnerNumberModel(Config.getUnderOccupancyPenalty(), -999.0, 999.0, 0.5));
        JSpinner criticalOccupancySelector = new JSpinner(new SpinnerNumberModel(Config.getCriticalOccupancyPenalty(), -999.0, 999.0, 0.5));

        groupParamSettings.add(immutableGet("set.largeGroup"));         groupParamSettings.add(largeGroupSelector);         groupParamSettings.add(immutableGet("set.pts"));
        groupParamSettings.add(immutableGet("set.underoccupancy"));    groupParamSettings.add(underoccupancySelector);     groupParamSettings.add(immutableGet("set.pts"));
        groupParamSettings.add(immutableGet("set.criticalOccupancy"));groupParamSettings.add(criticalOccupancySelector);  groupParamSettings.add(immutableGet("set.pts"));

        settingsPage.add(groupParamSettings);
        settingsPage.add(Box.createVerticalStrut(20));

        JPanel ageSettings = new JPanel();
        ageSettings.setLayout(new GridLayout(4, 3, 10, 5));
        // ageSettings.setBackground(Color.DARK_GRAY);

        JSpinner ageDifferenceThresholdSelector = new JSpinner(new SpinnerNumberModel(Config.getAgeDifferenceThreshold(), 0, 999.0, 0.2));
        JSpinner largeAgeDifferenceThresholdSelector = new JSpinner(new SpinnerNumberModel(Config.getLargeAgeDifferenceThreshold(), 0, 999.0, 0.2));
        JSpinner largeGroupSizeThresholdSelector = new JSpinner(new SpinnerNumberModel(Config.getLargeGroupSizeThreshold(), -999, 999, 1));
        JSpinner largeGroupAgeLimitSelector = new JSpinner(new SpinnerNumberModel(Config.getLargeGroupAgeLimit(), -999.0, 999.0, 0.2));

        ageSettings.add(immutableGet("set.ageDiffThr"));         ageSettings.add(ageDifferenceThresholdSelector);        ageSettings.add(immutableGet("set.years"));
        ageSettings.add(immutableGet("set.largeAgeDiffThr"));   ageSettings.add(largeAgeDifferenceThresholdSelector);   ageSettings.add(immutableGet("set.years"));
        ageSettings.add(immutableGet("set.largeGroupThr"));       ageSettings.add(largeGroupSizeThresholdSelector);       ageSettings.add(immutableGet("set.years"));
        ageSettings.add(immutableGet("set.largeGroupAgeLimit"));            ageSettings.add(largeGroupAgeLimitSelector);            ageSettings.add(immutableGet("set.years"));

        settingsPage.add(ageSettings);
        settingsPage.add(Box.createVerticalStrut(20));

        JPanel buttons = new JPanel();

        JButton defaults = new JButton(get("set.reset"));
        defaults.addActionListener(ignored -> {
            Config.setDefaults();

            preferenceSelector.setValue(Config.getPreferenceBonus());
            nonPreferenceSelector.setValue(Config.getNonPreferencePenalty());
            unfulfilledPreferenceSelector.setValue(Config.getUnfulfilledPreferencePenalty());
            mutualPreferenceSelector.setValue(Config.getMutualPreferenceBonus());
            ageDifferenceSelector.setValue(Config.getAgeDifferencePenalty());
            largeAgeDifferenceSelector.setValue(Config.getLargeAgeDifferencePenalty());
            locationSelector.setValue(Config.getSameLocationBonus());
            genderSelector.setValue(Config.getSameGenderBonus());

            largeGroupSelector.setValue(Config.getLargeGroupBonus());
            underoccupancySelector.setValue(Config.getUnderOccupancyPenalty());
            criticalOccupancySelector.setValue(Config.getCriticalOccupancyPenalty());

            ageDifferenceThresholdSelector.setValue(Config.getAgeDifferenceThreshold());
            largeAgeDifferenceThresholdSelector.setValue(Config.getLargeAgeDifferenceThreshold());
            largeGroupSizeThresholdSelector.setValue(Config.getLargeGroupSizeThreshold());
            largeGroupAgeLimitSelector.setValue(Config.getLargeGroupAgeLimit());

            log("msg.reset");
        });

        JButton apply = new JButton(get("set.apply"));
        apply.addActionListener(ignored -> {
            Config.setPreferenceBonus((double) preferenceSelector.getValue());
            Config.setNonPreferencePenalty((double) nonPreferenceSelector.getValue());
            Config.setUnfulfilledPreferencePenalty((double) unfulfilledPreferenceSelector.getValue());
            Config.setMutualPreferenceBonus((double) mutualPreferenceSelector.getValue());
            Config.setAgeDifferencePenalty((double) ageDifferenceSelector.getValue());
            Config.setLargeAgeDifferencePenalty((double) largeAgeDifferenceSelector.getValue());
            Config.setSameLocationBonus((double) locationSelector.getValue());
            Config.setSameGenderBonus((double) genderSelector.getValue());

            Config.setLargeGroupBonus((double) largeGroupSelector.getValue());
            Config.setUnderoccupancyPenalty((double) underoccupancySelector.getValue());
            Config.setCriticalOccupancyPenalty((double) criticalOccupancySelector.getValue());

            Config.setAgeDifferenceThreshold((double) ageDifferenceThresholdSelector.getValue());
            Config.setLargeAgeDifferenceThreshold((double) largeAgeDifferenceThresholdSelector.getValue());
            Config.setLargeGroupSizeThreshold((int) largeGroupSizeThresholdSelector.getValue());
            Config.setLargeGroupAgeLimit((double) largeGroupAgeLimitSelector.getValue());

            log("msg.set");
        });

        JButton infoButton = new JButton(get("set.info"));
        infoButton.addActionListener(ignored -> log(
                "desc.pairwise",
                "desc.preference",
                "desc.nonPreference",
                "desc.unfulfilledPreference",
                "desc.mutualPreference",
                "desc.ageDiff",
                "desc.largeAgeDiff",
                "desc.sameLocation",
                "desc.sameGender",
                "",
                "desc.roomwise",
                "desc.largeGroup",
                "desc.underoccupancy",
                "desc.criticalOccupancy"
        ));

        buttons.add(infoButton);
        buttons.add(defaults);
        buttons.add(apply);

        settingsPage.add(buttons);
    }

    private static void buildPeoplePage() {
        peoplePage = new JPanel();
        peoplePage.setLayout(new BoxLayout(peoplePage, BoxLayout.PAGE_AXIS));

        AbstractTableModel peopleModel = new AbstractTableModel() {
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
                    case 0 -> get("col.group");
                    case 1 -> get("col.name");
                    case 2 -> get("col.birth");
                    case 3 -> get("col.gender");
                    case 4 -> get("col.location");
                    case 5 -> get("col.prefs");
                    default -> null;
                };
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                Person[] people = Person.getPeopleSorted();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-uuuu");
                return switch (columnIndex) {
                    case 0 -> people[rowIndex].getGroup();
                    case 1 -> people[rowIndex].getName();
                    case 2 -> people[rowIndex].getBirth().format(formatter);
                    case 3 -> people[rowIndex].getGender();
                    case 4 -> people[rowIndex].getLocation();
                    case 5 -> Result.toStringList(people[rowIndex].getPreferences());
                    default -> null;
                };
            }
        };
        JTable peopleTable = new JTable(peopleModel);
        peopleTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        peopleTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        peopleTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        peopleTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        peopleTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        peopleTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        peopleTable.getColumnModel().getColumn(5).setPreferredWidth(500);

        JPanel importPanel = new JPanel();
        JTextPane importText = immutableGet("ppl.import");
        importText.setToolTipText(get("ppl.importtooltip"));
        importPanel.add(importText);

        JFileChooser importFileChooser = new JFileChooser();
        importFileChooser.setFileFilter(csvFilter);

        JButton browseButton = new JButton(get("button.browse"));
        browseButton.addActionListener(ignored -> {
            int returnVal = importFileChooser.showOpenDialog(importPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    Person.addPeople(Main.parsePeople(importFileChooser.getSelectedFile()));
                } catch (IllegalArgumentException e) {
                    JOptionPane.showConfirmDialog(fenster, get("msg.illegalCsvFormat") + " \n" + e.getMessage(), get("title.warn"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                }
                peopleModel.fireTableDataChanged();
            }
        });
        JButton test = new JButton(get("button.test"));
        test.addActionListener(ignored -> {
            File file = new File(ROOT + "\\src\\main\\resources\\people.csv");
            Person.addPeople(Main.parsePeople(file));
            peopleModel.fireTableDataChanged();
        });

        importPanel.add(browseButton);
        importPanel.add(test);
        peoplePage.add(importPanel);

        JPanel manualPanel = new JPanel();
        manualPanel.setLayout(new BoxLayout(manualPanel, BoxLayout.PAGE_AXIS));
        manualPanel.setBackground(Color.LIGHT_GRAY);

        JPanel groupPanel = new JPanel();
        groupPanel.add(immutableGet("ppl.group"));
        JTextArea group = new JTextArea("");
        group.setPreferredSize(new Dimension(12, 20));
        groupPanel.add(group);
        groupPanel.setBackground(Color.LIGHT_GRAY);

        JPanel namePanel = new JPanel();
        namePanel.add(immutableGet("ppl.name"));
        JTextArea name = new JTextArea();
        name.setPreferredSize(new Dimension(300, 20));
        namePanel.add(name);
        namePanel.setBackground(Color.LIGHT_GRAY);

        JPanel birthPanel = new JPanel();
        birthPanel.add(immutableGet("ppl.birth"));
        JSpinner birthMonth = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        JSpinner birthYear = new JSpinner(new SpinnerNumberModel(2015, 1950, 2030, 1));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(birthYear, "#");
        birthYear.setEditor(editor);
        birthPanel.add(birthMonth);
        birthPanel.add(birthYear);
        birthPanel.setBackground(Color.LIGHT_GRAY);

        JPanel locationPanel = new JPanel();
        locationPanel.add(immutableGet("ppl.location"));
        JTextArea location = new JTextArea();
        location.setPreferredSize(new Dimension(300, 20));
        locationPanel.add(location);
        locationPanel.setBackground(Color.LIGHT_GRAY);

        JPanel genderPanel = new JPanel();
        genderPanel.add(immutableGet("ppl.gender"));
        JTextArea gender = new JTextArea();
        gender.setPreferredSize(new Dimension(12, 20));
        genderPanel.add(gender);
        genderPanel.setBackground(Color.LIGHT_GRAY);

        JPanel preferencePanel = new JPanel();
        preferencePanel.add(immutableGet("ppl.prefs"));
        JTextArea preferences = new JTextArea();
        preferences.setPreferredSize(new Dimension(500, 40)); // why i need 40, dont know dont care
        preferences.setLineWrap(true);
        preferencePanel.add(preferences);
        preferencePanel.setBackground(Color.LIGHT_GRAY);

        JButton addManual = new JButton(get("button.add"));
        addManual.addActionListener(ignored -> {
            try {
                char groupv = group.getText().charAt(0);
                String namev = name.getText().trim();
                if (Person.fromName(namev) != null &&
                    JOptionPane.showConfirmDialog(fenster, namev + " " + get("msg.personAlreadyExists"),
                    get("title.confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) return;

                YearMonth birthv = YearMonth.of((int) birthYear.getValue(), (int) birthMonth.getValue());
                String locationv = location.getText().trim();
                char genderv = gender.getText().charAt(0);
                List<Person> preferencesv = new ArrayList<>();
                for (String pref : preferences.getText().split(",")) {
                    Person prefPerson = Person.fromName(pref.trim());
                    if (prefPerson == null) {
                        if (!pref.trim().isEmpty())
                            log(pref + " " + get("msg.prefDoesNotExist") + " " + namev);
                        continue;
                    }
                    preferencesv.add(prefPerson);
                }

                new Person(namev, birthv, locationv, genderv, preferencesv, groupv);
                peopleModel.fireTableDataChanged();
            } catch (Exception e) {
                log(get("msg.err") + " \n" + e.getMessage());
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
        JButton delete = new JButton(get("button.delete"));
        delete.addActionListener(ignored -> {
            int[] rows = peopleTable.getSelectedRows();
            if (rows.length == 0)
                log("msg.noPeopleSelected");
            Person.removePeople(peopleTable.getSelectedRows());
            peopleModel.fireTableDataChanged();
        });
        buttons.add(delete);

        JButton restore = new JButton(get("button.restore"));
        restore.addActionListener(ignored -> {
            Person.restore();
            peopleModel.fireTableDataChanged();
        });
        buttons.add(restore);

        JButton export = new JButton(get("button.export"));
        export.addActionListener(ignored -> {
            JFileChooser saveFileChooser = new JFileChooser();
            saveFileChooser.setFileFilter(csvFilter);
            int returnValue = saveFileChooser.showSaveDialog(fenster);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                if (!saveFileChooser.getSelectedFile().getName().endsWith(".csv")) {
                    JOptionPane.showConfirmDialog(fenster, get("msg.noCSVExt"), get("title.warn"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(saveFileChooser.getSelectedFile()));
                    writer.write(Person.everyone());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    JOptionPane.showConfirmDialog(fenster, get("msg.exportFail"), get("title.warn"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttons.add(export);

        JButton viewAll = new JButton(get("button.viewall"));
        viewAll.addActionListener(ignored -> logDirect(Person.everyone()));
        buttons.add(viewAll);

        JButton deleteAll = new JButton(get("button.deleteall"));
        deleteAll.addActionListener(ignored -> {
            int returnValue = JOptionPane.showConfirmDialog(fenster, get("msg.deletePpl"),
                    get("title.confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (returnValue == JOptionPane.YES_OPTION) {
                Person.clearPeople();
            }
        });
        buttons.add(deleteAll);

        JButton everyone = new JButton(get("button.printall"));
        everyone.addActionListener(ignored -> {
            System.out.println(Person.getPeople().toString());
            // System.out.println(Person.everyone());
        });
        buttons.add(everyone);


        peoplePage.add(buttons);
        peoplePage.add(new JScrollPane(peopleTable));
    }

    private static void buildRoomsPage() {
        roomsPage = new JPanel();
        roomsPage.setLayout(new BoxLayout(roomsPage, BoxLayout.PAGE_AXIS));

        AbstractTableModel roomsModel = new AbstractTableModel() {
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
                    case 0 -> get("col.id");
                    case 1 -> get("col.capacity");
                    default -> null;
                };
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
        JTable roomsTable = new JTable(roomsModel);
        roomsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        roomsTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        roomsTable.getColumnModel().getColumn(1).setPreferredWidth(60);

        JPanel importPanel = new JPanel();
        JTextPane importText = immutableGet("rms.import");
        importText.setToolTipText(get("rms.importtooltip"));
        importPanel.add(importText);

        JFileChooser importFileChooser = new JFileChooser();
        importFileChooser.setFileFilter(csvFilter);

        JButton browseButton = new JButton(get("button.browse"));
        browseButton.addActionListener(ignored -> {
            int returnVal = importFileChooser.showOpenDialog(importPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    Room.addRooms(Main.parseRooms(importFileChooser.getSelectedFile()));
                } catch (IllegalArgumentException e) {
                    JOptionPane.showConfirmDialog(fenster, get("msg.illegalCsvFormat") + " \n" + e.getMessage(), get("title.warn"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                }
                roomsModel.fireTableDataChanged();
            }
        });
        JButton test = new JButton(get("button.test"));
        test.addActionListener(ignored -> {
            File file = new File(ROOT + "\\src\\main\\resources\\rooms.csv");
            Room.addRooms(Main.parseRooms(file));
            roomsModel.fireTableDataChanged();
        });
        importPanel.add(browseButton);
        importPanel.add(test);
        roomsPage.add(importPanel);

        JPanel manualPanel = new JPanel();
        manualPanel.setLayout(new BoxLayout(manualPanel, BoxLayout.PAGE_AXIS));
        manualPanel.setBackground(Color.LIGHT_GRAY);

        JPanel roomIdPanel = new JPanel();
        roomIdPanel.setBackground(Color.LIGHT_GRAY);
        roomIdPanel.add(immutableGet("rms.id"));
        JTextArea roomId = new JTextArea();
        roomId.setPreferredSize(new Dimension(100, 20));
        roomIdPanel.add(roomId);
        manualPanel.add(roomIdPanel);

        JPanel capacityPanel = new JPanel();
        capacityPanel.setBackground(Color.LIGHT_GRAY);
        capacityPanel.add(immutableGet("rms.capacity"));
        JSpinner capacity = new JSpinner(new SpinnerNumberModel(5, 0, 30, 1));
        capacityPanel.add(capacity);
        manualPanel.add(capacityPanel);

        JButton manualAdd = new JButton(get("button.add"));
        manualAdd.addActionListener(ignored -> {
            String roomIdv = roomId.getText().trim();
            int capacityv = (int) capacity.getValue();
            new Room(roomIdv, capacityv);
            roomsModel.fireTableDataChanged();
        });
        manualPanel.add(manualAdd);

        roomsPage.add(manualPanel);

        JPanel buttons = new JPanel();
        JButton delete = new JButton(get("button.delete"));
        delete.addActionListener(ignored -> {
            int[] rows = roomsTable.getSelectedRows();
            if (rows.length == 0)
                log("msg.noRoomsSelected");
            Room.removeRooms(rows);
            roomsModel.fireTableDataChanged();
        });
        buttons.add(delete);

        JButton restore = new JButton(get("button.restore"));
        restore.addActionListener(ignored -> {
            Room.restore();
            roomsModel.fireTableDataChanged();
        });
        buttons.add(restore);

        JButton export = new JButton(get("button.export"));
        export.addActionListener(ignored -> {
            JFileChooser saveFileChooser = new JFileChooser();
            saveFileChooser.setFileFilter(csvFilter);
            int returnValue = saveFileChooser.showSaveDialog(fenster);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                if (!saveFileChooser.getSelectedFile().getName().endsWith(".csv")) {
                    JOptionPane.showConfirmDialog(fenster, get("msg.noCSVExt"), get("title.warn"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(saveFileChooser.getSelectedFile()));
                    writer.write(Room.everywhere());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    JOptionPane.showConfirmDialog(fenster, get("msg.exportFail") + " \n" + e.getMessage(), get("title.warn"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttons.add(export);

        JButton viewAll = new JButton(get("button.viewall"));
        viewAll.addActionListener(ignored -> logDirect(Room.everywhere()));
        buttons.add(viewAll);

        JButton deleteAll = new JButton(get("button.deleteall"));
        deleteAll.addActionListener(ignored -> {
            int returnValue = JOptionPane.showConfirmDialog(fenster, get("msg.deleteRooms"),
                    get("title.confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (returnValue == JOptionPane.YES_OPTION) {
                Room.clearRooms();
                roomsModel.fireTableDataChanged();
            }
        });
        buttons.add(deleteAll);

        JButton everyone = new JButton(get("button.printall"));
        everyone.addActionListener(ignored -> System.out.println(Room.getRooms().toString()));
        buttons.add(everyone);

        roomsPage.add(buttons);

        roomsPage.add(new JScrollPane(roomsTable));

    }

    static DefaultListModel<String> model = new DefaultListModel<>();
    static JList<String> resultsList = new JList<>(model);
    static AbstractTableModel dataModel = new AbstractTableModel() {
        @Override
        public int getRowCount() {
            if (Main.results == null) return 0;
            if (resultsList.getSelectedIndex() == -1) return 0;
            return Main.results[resultsList.getSelectedIndex()].config.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return switch (columnIndex) {
                case 0 ->                     Main.results[9-resultsList.getSelectedIndex()].config.get(rowIndex).size();
                case 1 -> Result.toStringList(Main.results[9-resultsList.getSelectedIndex()].config.get(rowIndex)).substring(2).replace(']', ' ');
                default -> "";
            };
        }

        @Override
        public String getColumnName(int columnIndex) {
            return switch (columnIndex) {
                case 0 -> get("col.size");
                case 1 -> get("col.occupants");
                default -> "";
            };
        }
    };
    private static void buildComputePage() {
        computePage = new JPanel();
        computePage.setLayout(new BoxLayout(computePage, BoxLayout.PAGE_AXIS));

        JTextPane log = immutableText("");
        log.setPreferredSize(new Dimension(800, 20));

        JPanel buttonPanel = new JPanel();
        JButton computeButton = new JButton(get("cmp.exec"));
        computeButton.addActionListener(ignored -> {
            try {
                Main.execute();
            } catch (Exception e) {
                if (Person.getPeople() == null || Person.getPeople().isEmpty())
                    log("msg.err", "", "msg.noPeople");
                else if (Room.getRooms() == null || Room.getRooms().isEmpty())
                    log("msg.err", "", "msg.noRooms");
                else
                    log("msg.err", "", e.getMessage());
                return;
            }
            constructDisplay();
            log.setText(
                      String.format("%,d", Main.processed) + " " + get("msg.paths") + ", "
                    + String.format("%,d", (Main.endTime - Main.startTime)/1000000L) + "ms " + get("msg.ms") + " + "
                    + String.format("%,d", (Main.endTime - Main.startTime)%1000000L) + "ns");
            update();
        });
        buttonPanel.add(computeButton);

        JButton exportAllButton = new JButton(get("button.exportall"));
        exportAllButton.addActionListener(ignored -> {
            JFileChooser saveFileChooser = new JFileChooser();
            saveFileChooser.setFileFilter(new FileNameExtensionFilter(get("desc.txtfilter"), "txt"));
            int returnValue = saveFileChooser.showSaveDialog(fenster);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                if (!saveFileChooser.getSelectedFile().getName().endsWith(".txt")) {
                    JOptionPane.showConfirmDialog(fenster, get("msg.noTXTExt"), get("title.warn"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(saveFileChooser.getSelectedFile()));
                    StringBuilder ret = new StringBuilder(get("exp.all"));
                    ret.append("\n\n\n");
                    for (int i = 1; i <= Main.results.length; ++i) {
                        Result r = Main.results[10-i];
                        ret.append(get("exp.result")).append(" #").append(i).append(" (").append(((double)((int)(r.score*100)))/100).append("): [\n");
                        StringBuilder res = new StringBuilder("    " + Result.toString(r.config, ",\n    "));
                        res.deleteCharAt(res.indexOf("["));
                        res.deleteCharAt(res.length()-1).append("\n];");
                        ret.append(res).append("\n\n");
                    }
                    ret.append(get("exp.end"));
                    writer.write(ret.toString());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    JOptionPane.showConfirmDialog(fenster, get("msg.exportFail") + " \n" + e.getMessage(), get("title.warn"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);

                }
            }
        });
        buttonPanel.add(exportAllButton);

        JButton exportButton = new JButton(get("button.export"));
        exportButton.addActionListener(ignored -> {
            JFileChooser saveFileChooser = new JFileChooser();
            saveFileChooser.setFileFilter(new FileNameExtensionFilter(get("desc.txtfilter"), "txt"));
            int returnValue = saveFileChooser.showSaveDialog(fenster);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                if (!saveFileChooser.getSelectedFile().getName().endsWith(".txt")) {
                    JOptionPane.showConfirmDialog(fenster, get("msg.noTXTExt"), get("title.warn"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(saveFileChooser.getSelectedFile()));
                    StringBuilder ret = new StringBuilder();
                    Result r = Main.results[9-resultsList.getSelectedIndex()];
                    ret.append("Result #").append(resultsList.getSelectedIndex()).append(" (").append(((double)((int)(r.score*100)))/100).append("): \n")
                            .append(Result.toString(r.config, ",\n")).append(";\n");
                    writer.write(ret.toString());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    JOptionPane.showConfirmDialog(fenster, get("msg.exportFail") + " \n" + e.getMessage(), get("title.warn"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPanel.add(exportButton);

        JPanel logPanel = new JPanel();
        logPanel.add(log);

        JPanel displayPanel = new JPanel();

        resultsList.getSelectionModel().setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        resultsList.addListSelectionListener(ignored -> dataModel.fireTableDataChanged());
        displayPanel.add(resultsList);

        JTable resultsTable = new JTable(dataModel);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(800);
        JScrollPane tableScrollPane = new JScrollPane(resultsTable);
        tableScrollPane.setPreferredSize(new Dimension(850, 500));
        displayPanel.add(tableScrollPane);

        computePage.add(buttonPanel);
        computePage.add(logPanel);
        computePage.add(displayPanel);
    }
    static void constructDisplay() {
        model.clear();
        for (int i = 1; i <= Main.results.length; ++i) {
            model.addElement(get("exp.result") + " #" + i + " ("+ ((double)((int)(Main.results[10-i].score*100)))/100 + ")");
        }
        resultsList.setSelectedIndex(0);
    }
}
