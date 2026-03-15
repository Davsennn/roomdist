package me.davsennn;

import javax.swing.*;
import java.awt.*;

public class GUI {
    public static JFrame fenster;

    private static JPanel settingsPage;

    public static void createWindow() {
        fenster = new JFrame("roomdist Application 1.0.0");
        fenster.setSize(800, 600);
        fenster.setVisible(true);
        fenster.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenster.setLayout(new BorderLayout(10, 10));

        JPanel navigation = new JPanel();
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.LINE_AXIS));
        navigation.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane tabs = new JTabbedPane();
        buildSettingsPage();
        tabs.addTab("Settings", settingsPage);

        navigation.add(tabs);

        fenster.add(navigation, BorderLayout.NORTH);
    }

    private static JTextArea immutableText(String message) {
        JTextArea ret = new JTextArea(message);
        ret.setEditable(false);
        return ret;
    }
    private static void buildSettingsPage() {
        System.out.println("Building settings page");

        settingsPage = new JPanel();
        settingsPage.setLayout(new BoxLayout(settingsPage, BoxLayout.PAGE_AXIS));

        JPanel pairParamSettings = new JPanel();
        pairParamSettings.setLayout(new GridLayout(6, 3, 10, 5));

        JSpinner preferenceSelector =       new JSpinner(new SpinnerNumberModel(2.0D, -999.0, 999.0, 0.5));
        JSpinner mutualPreferenceSelector = new JSpinner(new SpinnerNumberModel(5.0D, -999.0, 999.0, 0.5));
        JSpinner ageDifferenceSelector =    new JSpinner(new SpinnerNumberModel(1.0D, -999.0, 999.0, 0.5));
        JSpinner largeAgeDifferenceSelector=new JSpinner(new SpinnerNumberModel(2.0D, -999.0, 999.0, 0.5));
        JSpinner locationSelector =         new JSpinner(new SpinnerNumberModel(1.0D, -999.0, 999.0, 0.5));
        JSpinner genderSelector =           new JSpinner(new SpinnerNumberModel(2.0D, -999.0, 999.0, 0.5));

        pairParamSettings.add(immutableText("PREFERENCE BONUS"));            pairParamSettings.add(preferenceSelector);          pairParamSettings.add(immutableText("Punkte"));
        pairParamSettings.add(immutableText("MUTUAL PREFERENCE BONUS"));     pairParamSettings.add(mutualPreferenceSelector);    pairParamSettings.add(immutableText("Punkte"));
        pairParamSettings.add(immutableText("AGE DIFFERENCE PENALTY"));      pairParamSettings.add(ageDifferenceSelector);       pairParamSettings.add(immutableText("Punkte pro Jahr Unterschied"));
        pairParamSettings.add(immutableText("LARGE AGE DIFFERENCE PENALTY"));pairParamSettings.add(largeAgeDifferenceSelector);  pairParamSettings.add(immutableText("Punkte pro Jahr Unterschied"));
        pairParamSettings.add(immutableText("SAME LOCATION BONUS"));         pairParamSettings.add(locationSelector);            pairParamSettings.add(immutableText("Punkte"));
        pairParamSettings.add(immutableText("SAME GENDER BONUS"));           pairParamSettings.add(genderSelector);              pairParamSettings.add(immutableText("Punkte"));

        settingsPage.add(pairParamSettings);

        settingsPage.add(Box.createVerticalStrut(20));

        JPanel groupParamSettings = new JPanel();
        groupParamSettings.setLayout(new GridLayout(3, 3, 10, 5));

        JSpinner largeGroupSelector = new JSpinner(new SpinnerNumberModel(0.0D, -999.0, 999.0, 0.5));
        JSpinner underoccupancySelector = new JSpinner(new SpinnerNumberModel(1.0D, -999.0, 999.0, 0.5));
        JSpinner criticalOccupancySelector = new JSpinner(new SpinnerNumberModel(10.0D, -999.0, 999.0, 0.5));

        groupParamSettings.add(immutableText("LARGE GROUP BONUS"));         groupParamSettings.add(largeGroupSelector);         groupParamSettings.add(immutableText("Punkte"));
        groupParamSettings.add(immutableText("UNDEROCCUPANCY PENALTY"));    groupParamSettings.add(underoccupancySelector);     groupParamSettings.add(immutableText("Punkte"));
        groupParamSettings.add(immutableText("CRITICAL OCCUPANCY PENALTY"));groupParamSettings.add(criticalOccupancySelector);  groupParamSettings.add(immutableText("Punkte"));

        settingsPage.add(groupParamSettings);
        settingsPage.add(Box.createVerticalStrut(20));

        JPanel ageSettings = new JPanel();
        ageSettings.setLayout(new GridLayout(2, 3, 10, 5));

        JSpinner ageDifferenceThresholdSelector = new JSpinner(new SpinnerNumberModel(2.0D, 0, 999.0, 0.2));
        JSpinner largeAgeDifferenceThresholdSelector = new JSpinner(new SpinnerNumberModel(3.0D, 0, 999.0, 0.2));

        ageSettings.add(immutableText("AGE DIFFERENCE THRESHOLD"));         ageSettings.add(ageDifferenceThresholdSelector);        ageSettings.add(immutableText("Jahre"));
        ageSettings.add(immutableText("LARGE AGE DIFFERENCE THRESHOLD"));   ageSettings.add(largeAgeDifferenceThresholdSelector);   ageSettings.add(immutableText("Jahre"));

        settingsPage.add(ageSettings);
        settingsPage.add(Box.createVerticalStrut(20));

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(1, 2, 10, 0));

        JButton defaults = new JButton("Zuruecksetzen");
        defaults.addActionListener(e -> {
            Config.setPreferenceBonus(2.0);
            Config.setMutualPreferenceBonus(5.0);
            Config.setAgeDifferencePenalty(1.0);
            Config.setLargeAgeDifferencePenalty(2.0);
            Config.setSameLocationBonus(1.0);
            Config.setSameGenderBonus(2.0);

            Config.setLargeGroupBonus(0.0);
            Config.setUnderOccupancyPenalty(1.0);
            Config.setCriticalOccupancyPenalty(10.0);

            Config.setAgeDifferenceThreshold(2.0);
            Config.setLargeAgeDifferenceThreshold(3.0);
        });

        JButton apply = new JButton("Anwenden");
        apply.addActionListener(e -> {
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
        });

        buttons.add(defaults);
        buttons.add(apply);

        settingsPage.add(buttons);
    }
}
