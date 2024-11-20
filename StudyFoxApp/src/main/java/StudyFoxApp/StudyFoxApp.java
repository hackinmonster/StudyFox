package StudyFoxApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.TimerTask;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.*;

abstract class AppUI {
    protected JPanel panel;

    public AppUI() {
        this.panel = new JPanel();
    }

    abstract void show(JPanel container);
}

class MenuUI extends AppUI {
    private final FileManager fileManager;

    public MenuUI(FileManager fileManager) {
        super();
        this.fileManager = fileManager;
    }

    @Override
    void show(JPanel container) {
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("StudyFox", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(new Color(0, 123, 255));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(titleLabel, gbc);

        JButton startSessionButton = new JButton("Start Study Session");
        JButton settingsButton = new JButton("Settings");
        JButton viewVisualizationButton = new JButton("View Visualization");

        startSessionButton.setPreferredSize(new Dimension(200, 40));
        settingsButton.setPreferredSize(new Dimension(200, 40));
        viewVisualizationButton.setPreferredSize(new Dimension(200, 40));

        startSessionButton.addActionListener(e -> openTimerUI(container));
        settingsButton.addActionListener(e -> openSettingsUI(container));
        viewVisualizationButton.addActionListener(e -> openVisualizationUI(container));

        gbc.gridy = 1;
        panel.add(startSessionButton, gbc);
        gbc.gridy = 2;
        panel.add(viewVisualizationButton, gbc);
        gbc.gridy = 3;
        panel.add(settingsButton, gbc);

        container.add(panel, "MenuUI");
        CardLayout cardLayout = (CardLayout) container.getLayout();
        cardLayout.show(container, "MenuUI");
    }

    private void openTimerUI(JPanel container) {
        AppUI timerUI = new TimerUI(fileManager);
        timerUI.show(container);
    }

    private void openSettingsUI(JPanel container) {
        AppUI settingsUI = new SettingsUI(fileManager);
        settingsUI.show(container);
    }

    private void openVisualizationUI(JPanel container) {
        AppUI visualizationUI = new VisualizationUI(fileManager);
        visualizationUI.show(container);
    }
}

class TimerUI extends AppUI {
    private final FileManager fileManager;
    private java.util.Timer timer;
    private long startTime;
    private long totalTime;
    private boolean isRunning;

    public TimerUI(FileManager fileManager) {
        super();
        this.fileManager = fileManager;
        totalTime = 0;
        timer = new java.util.Timer();
        isRunning = false;
    }

    @Override
    void show(JPanel container) {
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));

        JLabel timerLabel = new JLabel("00:00:00", JLabel.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 48));
        timerLabel.setForeground(new Color(0, 123, 255));

        panel.add(timerLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton startStopButton = new JButton("Start");
        JButton resetButton = new JButton("Reset");
        JButton backButton = new JButton("Back");

        startStopButton.setPreferredSize(new Dimension(100, 40));
        resetButton.setPreferredSize(new Dimension(100, 40));
        backButton.setPreferredSize(new Dimension(100, 40));

        startStopButton.addActionListener(e -> {
            if (!isRunning) {
                startTimer(timerLabel);
                startStopButton.setText("Stop");
            } else {
                stopTimer(timerLabel);
                startStopButton.setText("Start");
            }
        });
        resetButton.addActionListener(e -> resetTimer(timerLabel));
        backButton.addActionListener(e -> backToMenu(container));

        buttonPanel.add(startStopButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(backButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        container.add(panel, "TimerUI");
        CardLayout cardLayout = (CardLayout) container.getLayout();
        cardLayout.show(container, "TimerUI");
    }

    private void startTimer(JLabel timerLabel) {
        isRunning = true;
        startTime = System.currentTimeMillis() - totalTime;
        timer = new java.util.Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                totalTime = System.currentTimeMillis() - startTime;
                long hours = (totalTime / 1000) / 3600;
                long minutes = ((totalTime / 1000) % 3600) / 60;
                long seconds = (totalTime / 1000) % 60;
                timerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }
        }, 0, 1000);
    }

    private void stopTimer(JLabel timerLabel) {
        isRunning = false;
        timer.cancel();

        String courseName = JOptionPane.showInputDialog(panel, "Enter course name:");
        if (courseName != null && !courseName.trim().isEmpty()) {
            fileManager.saveSession(new StudySession(courseName, totalTime));
            JOptionPane.showMessageDialog(panel, "Session saved for " + courseName);
        }
    }

    private void resetTimer(JLabel timerLabel) {
        if (isRunning) {
            timer.cancel();
            isRunning = false;
        }
        totalTime = 0;
        timerLabel.setText("00:00:00");
    }

    private void backToMenu(JPanel container) {
        if (isRunning) {
            timer.cancel();
            isRunning = false;
        }
        AppUI menuUI = new MenuUI(fileManager);
        menuUI.show(container);
    }
}

class SettingsUI extends AppUI {
    private final FileManager fileManager;

    public SettingsUI(FileManager fileManager) {
        super();
        this.fileManager = fileManager;
    }

    @Override
    void show(JPanel container) {
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label = new JLabel("Default Study Time (minutes):");
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField studyTimeField = new JTextField(String.valueOf(fileManager.getDefaultStudyTime()), 10);

        JButton saveButton = new JButton("Save");
        JButton backButton = new JButton("Back");

        saveButton.addActionListener(e -> {
            try {
                int studyTime = Integer.parseInt(studyTimeField.getText());
                fileManager.saveDefaultStudyTime(studyTime);
                JOptionPane.showMessageDialog(panel, "Settings saved!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Please enter a valid number.");
            }
        });
        backButton.addActionListener(e -> backToMenu(container));

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(label, gbc);
        gbc.gridy = 1;
        panel.add(studyTimeField, gbc);
        gbc.gridy = 2;
        panel.add(saveButton, gbc);
        gbc.gridy = 3;
        panel.add(backButton, gbc);

        container.add(panel, "SettingsUI");
        CardLayout cardLayout = (CardLayout) container.getLayout();
        cardLayout.show(container, "SettingsUI");
    }

    private void backToMenu(JPanel container) {
        AppUI menuUI = new MenuUI(fileManager);
        menuUI.show(container);
    }
}

class VisualizationUI extends AppUI {
    private final FileManager fileManager;

    public VisualizationUI(FileManager fileManager) {
        super();
        this.fileManager = fileManager;
    }

    @Override
    void show(JPanel container) {
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));

        Map<String, Long> courseData = fileManager.getCourseStudyTimes();

        if (courseData.isEmpty()) {
            JLabel label = new JLabel("No data available for visualization.", JLabel.CENTER);
            label.setFont(new Font("Arial", Font.PLAIN, 18));
            panel.add(label, BorderLayout.CENTER);
        } else {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<String, Long> entry : courseData.entrySet()) {
                String courseName = entry.getKey();
                long timeInMinutes = entry.getValue() / 60;
                dataset.addValue(timeInMinutes, "Study Time", courseName);
            }

            JFreeChart barChart = ChartFactory.createStackedBarChart(
                    "Study Time per Course",
                    "Course",
                    "Time (minutes)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    false, true, false);

            CategoryPlot plot = barChart.getCategoryPlot();
            plot.setBackgroundPaint(Color.white);
            plot.setRangeGridlinePaint(Color.gray);

            ChartPanel chartPanel = new ChartPanel(barChart);
            panel.add(chartPanel, BorderLayout.CENTER);
        }

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> backToMenu(container));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.add(backButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        container.add(panel, "VisualizationUI");
        CardLayout cardLayout = (CardLayout) container.getLayout();
        cardLayout.show(container, "VisualizationUI");
    }

    private void backToMenu(JPanel container) {
        AppUI menuUI = new MenuUI(fileManager);
        menuUI.show(container);
    }
}

class FileManager {
    private int defaultStudyTime = 30;
    private static final String SESSION_FILE = "study_sessions.csv";

    public int getDefaultStudyTime() {
        return defaultStudyTime;
    }

    public void saveDefaultStudyTime(int time) {
        this.defaultStudyTime = time;
    }

    public void saveSession(StudySession session) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SESSION_FILE, true))) {
            long timeSpentInSeconds = session.getDuration() / 1000;
            writer.printf("%s,%d%n", session.getCourseName(), timeSpentInSeconds);
        } catch (IOException e) {
            System.err.println("Error saving session: " + e.getMessage());
        }
    }

    public Map<String, Long> getCourseStudyTimes() {
        Map<String, Long> courseData = new LinkedHashMap<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(SESSION_FILE));
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String courseName = parts[0];
                    long timeSpent = Long.parseLong(parts[1]);
                    courseData.put(courseName, courseData.getOrDefault(courseName, 0L) + timeSpent);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading session data: " + e.getMessage());
        }
        return courseData;
    }
}

class StudySession {
    private String courseName;
    private long duration;

    public StudySession(String courseName, long duration) {
        this.courseName = courseName;
        this.duration = duration;
    }

    public String getCourseName() {
        return courseName;
    }

    public long getDuration() {
        return duration;
    }
}

public class StudyFoxApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("StudyFox");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 500);

            FileManager fileManager = new FileManager();
            JPanel container = new JPanel(new CardLayout());

            AppUI menuUI = new MenuUI(fileManager);
            menuUI.show(container);

            frame.add(container);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}