package me.davsennn;

import me.davsennn.algorithm.Person;
import me.davsennn.algorithm.PersonPair;
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
import java.awt.event.ItemEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

// @SuppressWarnings("DuplicatedCode")
public final class GUI {
    public static JFrame fenster;
    public static void update() { fenster.revalidate(); fenster.repaint(); }

    public static final String ROOT = System.getProperty("user.dir");

    public static final Locale[] locales = new Locale[]{ Locale.ENGLISH, Locale.GERMAN };
    public static Locale locale = Locale.getDefault();
    public static ResourceBundle resources;
    public static String get(String key) { return resources.getString(key); }

    private static JPanel settingsPage;
    private static JPanel peoplePage;
    private static JPanel bonusesPage;
    private static JPanel roomsPage;
    private static JPanel computePage;

    private static FileNameExtensionFilter csvFilter;
    private static FileNameExtensionFilter txtFilter;

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
        });
        locale = (Locale) localeSelector.getSelectedItem();
        if (locale == null) locale = Locale.getDefault();
        resources = ResourceBundle.getBundle("lang", locale);

        csvFilter = new FileNameExtensionFilter(get("desc.csvfilter"), "csv");
        txtFilter = new FileNameExtensionFilter(get("desc.txtfilter"), "txt");

        JTabbedPane tabs = new JTabbedPane();
        buildSettingsPage();
        buildPeoplePage();
        buildBonusesPage();
        buildRoomsPage();
        buildComputePage();
        tabs.addTab(get("title.settings"), settingsPage);
        tabs.addTab(get("title.people"), peoplePage);
        tabs.addTab(get("title.bonuses"), bonusesPage);
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
            if (!key.isEmpty() && key.charAt(0) == '%') { ret.append(key); System.out.println(key); continue; }
            ret.append(key.isEmpty() ? "" : get(key)).append("\n");
        }
        ret.deleteCharAt(ret.length()-1);
        logDirect(ret.toString());
    }

    private static int ask(String message) {
        return JOptionPane.showConfirmDialog(fenster, message,
                get("title.confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    private static JTextPane immutableText(String message) {
        JTextPane ret = new JTextPane();
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLACK);

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
    private static JTextPane immutableGet(String key) { return immutableText(get(key)); }
    private static void buildSettingsPage() {
        settingsPage = new JPanel();
        settingsPage.setLayout(new BoxLayout(settingsPage, BoxLayout.PAGE_AXIS));
        // settingsPage.setBackground(Color.DARK_GRAY);

        JPanel pairParamSettings = new JPanel();
        pairParamSettings.setLayout(new GridLayout(8, 3, 10, 5));
        // pairParamSettings.setBackground(Color.DARK_GRAY);

        JSpinner preferenceSelector =           new JSpinner(new SpinnerNumberModel(Config.getPreferenceBonus()             , -999.0, 999.0, 0.1));
        JSpinner nonPreferenceSelector =        new JSpinner(new SpinnerNumberModel(Config.getNonPreferencePenalty()        , -999.0, 999.0, 0.1));
        JSpinner unfulfilledPreferenceSelector =new JSpinner(new SpinnerNumberModel(Config.getUnfulfilledPreferencePenalty(), -999.0, 999.0, 0.1));
        JSpinner mutualPreferenceSelector =     new JSpinner(new SpinnerNumberModel(Config.getMutualPreferenceBonus()       , -999.0, 999.0, 0.1));
        JSpinner ageDifferenceSelector =        new JSpinner(new SpinnerNumberModel(Config.getAgeDifferencePenalty()        , -999.0, 999.0, 0.1));
        JSpinner largeAgeDifferenceSelector =   new JSpinner(new SpinnerNumberModel(Config.getLargeAgeDifferencePenalty()   , -999.0, 999.0, 0.1));
        JSpinner locationSelector =             new JSpinner(new SpinnerNumberModel(Config.getSameLocationBonus()           , -999.0, 999.0, 0.1));
        JSpinner genderSelector =               new JSpinner(new SpinnerNumberModel(Config.getSameGenderBonus()             , -999.0, 999.0, 0.1));

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

        JPanel pruneSettings = new JPanel();
        pruneSettings.setLayout(new GridLayout(2, 3, 10, 5));

        JCheckBox useEarlyPruningSelector = new JCheckBox(get("set.useEarlyPrune"), true);
        JSpinner earlyPruningStrengthSelector = new JSpinner(new SpinnerNumberModel(Config.getEarlyPruningStrength(), 0, 999, 1));
        JSpinner earlyPruningLengthSelector = new JSpinner(new SpinnerNumberModel(Config.getEarlyPruningLength(), 0, 999, 1));

        AtomicBoolean useEarlyPruningValue = new AtomicBoolean(true);
        useEarlyPruningSelector.addItemListener(i -> {
            boolean isEnabled = i.getStateChange() == ItemEvent.SELECTED;
            useEarlyPruningValue.set(isEnabled);
            earlyPruningStrengthSelector.setEnabled(isEnabled);
            earlyPruningLengthSelector.setEnabled(isEnabled);
        });

        settingsPage.add(useEarlyPruningSelector);
        pruneSettings.add(immutableGet("set.earlyPruneStrength"));  pruneSettings.add(earlyPruningStrengthSelector);    pruneSettings.add(immutableGet("set.prefs"));
        pruneSettings.add(immutableGet("set.earlyPruneLength"));    pruneSettings.add(earlyPruningLengthSelector);      pruneSettings.add(immutableGet("set.iterations"));

        settingsPage.add(pruneSettings);
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

            useEarlyPruningSelector.getModel().setSelected(Config.getUseEarlyPruning());
            earlyPruningStrengthSelector.setValue(Config.getEarlyPruningStrength());
            earlyPruningLengthSelector.setValue(Config.getEarlyPruningLength());

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

            Config.setUseEarlyPruning(useEarlyPruningValue.get());
            Config.setEarlyPruningStrength((int) earlyPruningStrengthSelector.getValue());
            Config.setEarlyPruningLength((int) earlyPruningLengthSelector.getValue());

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
                "desc.criticalOccupancy",
                "",
                "desc.pruning"
        ));

        buttons.add(infoButton);
        buttons.add(defaults);
        buttons.add(apply);

        settingsPage.add(buttons);
    }

    private static File saveFile(boolean txtInsteadOfCSV) {
        JFileChooser saveFileChooser = new JFileChooser();
        saveFileChooser.setFileFilter(txtInsteadOfCSV ? csvFilter : txtFilter);
        int returnValue = saveFileChooser.showSaveDialog(fenster);
        if (returnValue != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        if (!saveFileChooser.getSelectedFile().getName().endsWith(txtInsteadOfCSV ? ".txt" : ".csv")) {
            JOptionPane.showConfirmDialog(fenster, get(txtInsteadOfCSV ? "msg.noTXTExt" : "msg.noCSVExt"), get("title.warn"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return saveFileChooser.getSelectedFile();
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
            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return;
            }
            try {
                Person.addPeople(Main.parsePeople(importFileChooser.getSelectedFile()));
            } catch (IllegalArgumentException e) {
                JOptionPane.showConfirmDialog(fenster, get("msg.illegalCsvFormat") + " \n" + e.getMessage(), get("title.warn"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
            peopleModel.fireTableDataChanged();
            firePeopleChange();
        });
        JButton test = new JButton(get("button.test"));
        test.addActionListener(ignored -> {
            File file = new File(ROOT + "\\src\\main\\resources\\people.csv");
            Person.addPeople(Main.parsePeople(file));
            peopleModel.fireTableDataChanged();
            firePeopleChange();
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
                    ask(namev + " " + get("msg.personAlreadyExists")) == JOptionPane.NO_OPTION) return;

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
                firePeopleChange();
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
            firePeopleChange();
        });
        buttons.add(delete);

        JButton restore = new JButton(get("button.restore"));
        restore.addActionListener(ignored -> {
            Person.restore();
            peopleModel.fireTableDataChanged();
            firePeopleChange();
        });
        buttons.add(restore);

        JButton export = new JButton(get("button.export"));
        export.addActionListener(ignored -> {
            File file = saveFile(false);
            if (file == null) return;
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(Person.everyone());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                JOptionPane.showConfirmDialog(fenster, get("msg.exportFail"), get("title.warn"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        });
        buttons.add(export);

        JButton viewAll = new JButton(get("button.viewall"));
        viewAll.addActionListener(ignored -> logDirect(Person.everyone()));
        buttons.add(viewAll);

        JButton deleteAll = new JButton(get("button.deleteall"));
        deleteAll.addActionListener(ignored -> {
            if (ask(get("msg.deletePpl")) == JOptionPane.YES_OPTION) {
                Person.clearPeople();
            }
            peopleModel.fireTableDataChanged();
            firePeopleChange();
        });
        buttons.add(deleteAll);

        JButton everyone = new JButton(get("button.printall"));
        everyone.addActionListener(ignored -> System.out.println(Person.getPeople()));
        buttons.add(everyone);


        peoplePage.add(buttons);
        peoplePage.add(new JScrollPane(peopleTable));
    }

    static final DefaultListModel<String> peopleListModel = new DefaultListModel<>();
    static final JList<String> peopleList = new JList<>(peopleListModel);
    static void firePeopleChange() { peopleListModel.clear(); peopleListModel.addAll(Arrays.stream(Person.getPeopleSorted()).map(Person::getName).toList()); }
    private static void buildBonusesPage() {
        Person.custom_bonuses = new LinkedHashMap<>();

        bonusesPage = new JPanel();
        bonusesPage.setLayout(new BoxLayout(bonusesPage, BoxLayout.PAGE_AXIS));

        JPanel titlePanel = new JPanel();
        titlePanel.add(immutableGet("cbs.title"));
        bonusesPage.add(titlePanel);

        JPanel centralPanel = new JPanel();
        centralPanel.setLayout(new FlowLayout());

        centralPanel.add(new JScrollPane(peopleList));
        peopleList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel manualPanel = new JPanel();
        manualPanel.setLayout(new BoxLayout(manualPanel, BoxLayout.Y_AXIS));

        JTextArea p1 = new JTextArea(); p1.setPreferredSize(new Dimension(200, 20));
        JTextArea p2 = new JTextArea(); p2.setPreferredSize(new Dimension(200, 20));
        JSpinner score = new JSpinner(new SpinnerNumberModel(0.0, -999.0, 999.0, 0.1));
        JCheckBox isNegativeInfinity = new JCheckBox(get("cbs.negativeInfinity"));
        AtomicBoolean selected = new AtomicBoolean(false);
        isNegativeInfinity.addItemListener(i -> selected.set(i.getStateChange() == ItemEvent.SELECTED));
        JPanel p1Panel = new JPanel(); p1Panel.add(immutableText("1")); p1Panel.add(p1);
        manualPanel.add(p1Panel);
        JPanel p2Panel = new JPanel(); p2Panel.add(immutableText("2")); p2Panel.add(p2);
        manualPanel.add(p2Panel);
        JPanel scorePanel = new JPanel(); scorePanel.add(immutableGet("cbs.score")); scorePanel.add(score);
        manualPanel.add(scorePanel);
        isNegativeInfinity.setToolTipText(get("cbs.negInfTooltip"));
        manualPanel.add(isNegativeInfinity);

        centralPanel.add(manualPanel);

        AbstractTableModel bonusesModel = new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return Person.custom_bonuses.size();
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> Person.custom_bonuses.sequencedKeySet().toArray(new PersonPair[0])[rowIndex].a().getName();
                    case 1 -> Person.custom_bonuses.sequencedKeySet().toArray(new PersonPair[0])[rowIndex].b().getName();
                    case 2 -> Person.custom_bonuses.sequencedValues().toArray()[rowIndex];
                    default -> "";
                };
            }

            @Override
            public String getColumnName(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> "p1";
                    case 1 -> "p2";
                    case 2 -> get("cbs.score");
                    default -> "";
                };
            }
        };
        JTable bonusesTable = new JTable(bonusesModel);
        bonusesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JPanel addButtonPanel = new JPanel();
        JButton addButton = new JButton(get("button.add"));
        addButton.addActionListener(ignored -> {
            Person p1v = Person.fromName(p1.getText());
            Person p2v = Person.fromName(p2.getText());
            double scorev = selected.get() ? Double.NEGATIVE_INFINITY : (double) score.getValue();
            if (p1v == null)
                { logDirect(get("msg.err") + "\n" + p1.getText() + " " + get("msg.personDoesNotExist")); return; }
            if (p2v == null)
                { logDirect(get("msg.err") + "\n" + p2.getText() + " " + get("msg.personDoesNotExist")); return; }
            PersonPair p1p2 = new PersonPair(p1v, p2v);
            if (Person.custom_bonuses.containsKey(p1p2)) {
                if (ask(get("msg.pairHasValue")) == JOptionPane.YES_OPTION)
                    Person.custom_bonuses.replace(p1p2, scorev); }
            else Person.custom_bonuses.put(p1p2, scorev);
            //System.out.println(Person.custom_bonuses.keySet().stream().findAny().get()[0].getName());
            bonusesModel.fireTableDataChanged();
        });
        addButtonPanel.add(addButton);

        centralPanel.add(addButtonPanel);

        bonusesPage.add(centralPanel);

        JPanel buttonPanel = new JPanel();
        JButton deleteButton = new JButton(get("button.delete"));

        deleteButton.addActionListener(ignored -> {
            int[] rows = bonusesTable.getSelectedRows();
            if (rows.length == 0)
                log("msg.noPairsSelected");
            List<PersonPair> remove = new ArrayList<>(rows.length);
            for (int i : rows) {
                remove.add(Person.custom_bonuses.sequencedKeySet().toArray(new PersonPair[0])[i]);
            }
            for (PersonPair key : remove) {
                Person.custom_bonuses.remove(key);
            }
            bonusesModel.fireTableDataChanged();
        });
        buttonPanel.add(deleteButton);

        bonusesPage.add(buttonPanel);
        bonusesPage.add(new JScrollPane(bonusesTable));
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
                    case 0 -> Room.getRooms().get(rowIndex).id();
                    case 1 -> Room.getRooms().get(rowIndex).capacity();
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
            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return;
            }
            try {
                Room.addRooms(Main.parseRooms(importFileChooser.getSelectedFile()));
            } catch (IllegalArgumentException e) {
                JOptionPane.showConfirmDialog(fenster, get("msg.illegalCsvFormat") + " \n" + e.getMessage(), get("title.warn"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
            roomsModel.fireTableDataChanged();
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
            File file = saveFile(false);
            if (file == null) return;
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(Room.everywhere());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                JOptionPane.showConfirmDialog(fenster, get("msg.exportFail") + " \n" + e.getMessage(), get("title.warn"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        });
        buttons.add(export);

        JButton viewAll = new JButton(get("button.viewall"));
        viewAll.addActionListener(ignored -> logDirect(Room.everywhere()));
        buttons.add(viewAll);

        JButton deleteAll = new JButton(get("button.deleteall"));
        deleteAll.addActionListener(ignored -> {
            if (ask(get("msg.deleteRooms")) == JOptionPane.YES_OPTION) {
                Room.clearRooms();
                roomsModel.fireTableDataChanged();
            }
        });
        buttons.add(deleteAll);

        JButton everyone = new JButton(get("button.printall"));
        everyone.addActionListener(ignored -> System.out.println(Room.getRooms()));
        buttons.add(everyone);

        roomsPage.add(buttons);

        roomsPage.add(new JScrollPane(roomsTable));

    }

    static final DefaultListModel<String> model = new DefaultListModel<>();
    static final JList<String> resultsList = new JList<>(model);
    static final AbstractTableModel dataModel = new AbstractTableModel() {
        @Override
        public int getRowCount() {
            if (Main.results == null) return 0;
            if (resultsList.getSelectedIndex() == -1) return 0;
            return Main.results[resultsList.getSelectedIndex()].config().size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return switch (columnIndex) {
                case 0 ->                     Main.results[9-resultsList.getSelectedIndex()].config().get(rowIndex).size();
                case 1 -> Result.toStringList(Main.results[9-resultsList.getSelectedIndex()].config().get(rowIndex)).substring(2).replace(']', ' ');
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
            log.setText(get("msg.seeConsoleLog"));
            update();
            try {
                Main.execute();
            } catch (Exception e) {
                if (Person.getPeople().isEmpty())
                    log("msg.err", "", "msg.noPeople");
                else if (Room.getRooms().isEmpty())
                    log("msg.err", "", "msg.noRooms");
                else
                    log("msg.err", "", "%"+e.getMessage());
                return;
            }
            constructDisplay();
            long diff = Main.endTime - Main.startTime;
            log.setText(String.format(get("msg.performanceReport"),
                    Main.processed, diff/1000000L, diff%1000000L, (Main.processed*1000000000L/diff)));
            update();
        });
        buttonPanel.add(computeButton);

        JButton exportAllButton = new JButton(get("button.exportall"));
        exportAllButton.addActionListener(ignored -> {
            File file = saveFile(true);
            if (file == null) return;
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                StringBuilder ret = new StringBuilder(get("exp.all"));
                ret.append("\n\n\n");
                for (int i = 1; i <= Main.results.length; ++i) {
                    Result r = Main.results[10-i];
                    ret.append(String.format("%1$s #%2$d (%3$+4.5g): [%n", get("exp.result"), i, r.score()));
                    StringBuilder res = new StringBuilder("    " + Result.toString(r.config(), ",\n    "));
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
        });
        buttonPanel.add(exportAllButton);

        JButton exportButton = new JButton(get("button.export"));
        exportButton.addActionListener(ignored -> {
            File file = saveFile(true);
            if (file == null) return;
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                Result r = Main.results[9-resultsList.getSelectedIndex()];
                writer.write(
                        String.format("Result #%1$d (%2$+4.5g): %n%3$s;%n", resultsList.getSelectedIndex(), r.score(), Result.toString(r.config(), ",\n"))
                );
                writer.flush();
                writer.close();
            } catch (IOException e) {
                JOptionPane.showConfirmDialog(fenster, get("msg.exportFail") + " \n" + e.getMessage(), get("title.warn"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
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
            model.addElement(String.format("%1$s #%2$d (%3$+5.4g)", get("exp.result"), i, Main.results[10-i].score()));
        }
        resultsList.setSelectedIndex(0);
    }
}
