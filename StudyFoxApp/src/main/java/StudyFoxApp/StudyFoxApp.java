package StudyFoxApp;

import java.awt.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.*;
import org.jfree.data.general.DefaultPieDataset;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.time.temporal.WeekFields;

//Extended by menu,timer,visualization,settings UI classes.
abstract class AppUI {
    protected JPanel panel;

    public AppUI() {
        this.panel = new JPanel();
    }

    abstract void show(JPanel container);
}

//Main menu from which we access program features.
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

//Timer to record study sessions.
class TimerUI extends AppUI {
    private final FileManager fileManager;
    private java.util.Timer timer;
    private long startTime;
    private long startTimeStamp;
    private long stopTimeStamp;
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
        startTimeStamp = System.currentTimeMillis();
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
        stopTimeStamp = System.currentTimeMillis();

        ZonedDateTime startDate = Instant.ofEpochMilli(startTimeStamp).atZone(ZoneId.of("America/New_York"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String date = startDate.format(formatter);

        List<StudySession> sessionsData = fileManager.getCourseStudyTimes();

        List<String> courses = VisualizationUI.getAllCourses(sessionsData);

        JComboBox<String> courseComboBox = new JComboBox<>(courses.toArray(new String[0]));
        courseComboBox.setEditable(true);
        int selection = JOptionPane.showConfirmDialog(panel, courseComboBox, "Select or Enter Course Name", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (selection == JOptionPane.OK_OPTION) {
            String selectedCourse = (String) courseComboBox.getSelectedItem();
            
            if (selectedCourse != null && !selectedCourse.trim().isEmpty()) {
                fileManager.saveSession(new StudySession(selectedCourse, totalTime, startTimeStamp, stopTimeStamp, date));
                JOptionPane.showMessageDialog(panel, "Session saved for " + selectedCourse);
            }
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

//Settings to handle user configuration for visualizations.
class SettingsUI extends AppUI {
    private final FileManager fileManager;
    private final JComboBox<String> visualizationDropdown;

    public SettingsUI(FileManager fileManager) {
        super();
        this.fileManager = fileManager;
        visualizationDropdown = new JComboBox<>(new String[]{"Stacked Bar Chart", "Line Graph", "Total Time", "Total Time by Subject", "Pie Chart by Subject"});
    }

    @Override
    void show(JPanel container) {
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel visualizationLabel = new JLabel("Select Visualization Type:");
        visualizationLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        visualizationDropdown.setSelectedItem(fileManager.getVisualizationType());

        JLabel startDateLabel = new JLabel("Start Date:");
        startDateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JSpinner startDateSpinner = createDateSpinner(fileManager.getStartDate());

        JLabel endDateLabel = new JLabel("End Date:");
        endDateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JSpinner endDateSpinner = createDateSpinner(fileManager.getEndDate());

        JButton saveButton = new JButton("Save");
        JButton backButton = new JButton("Back");

        saveButton.addActionListener(e -> {
            try {
                String startDate = formatDate((Date) startDateSpinner.getValue());
                String endDate = formatDate((Date) endDateSpinner.getValue());

                fileManager.saveStartDate(startDate);
                fileManager.saveEndDate(endDate);

                String selectedVisualization = (String) visualizationDropdown.getSelectedItem();
                fileManager.saveVisualizationType(selectedVisualization);

                JOptionPane.showMessageDialog(panel, "Settings saved!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Please select valid dates.");
            }
        });

        backButton.addActionListener(e -> backToMenu(container));

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(visualizationLabel, gbc);
        gbc.gridy = 1;
        panel.add(visualizationDropdown, gbc);
        gbc.gridy = 2;
        panel.add(startDateLabel, gbc);
        gbc.gridy = 3;
        panel.add(startDateSpinner, gbc);
        gbc.gridy = 4;
        panel.add(endDateLabel, gbc);
        gbc.gridy = 5;
        panel.add(endDateSpinner, gbc);
        gbc.gridy = 6;
        panel.add(saveButton, gbc);
        gbc.gridy = 7;
        panel.add(backButton, gbc);

        container.add(panel, "SettingsUI");
        CardLayout cardLayout = (CardLayout) container.getLayout();
        cardLayout.show(container, "SettingsUI");
    }

    private JSpinner createDateSpinner(String dateStr) {
        Date date = parseDate(dateStr);
        SpinnerDateModel model = new SpinnerDateModel();
        model.setValue(date != null ? date : new Date());
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "MM/dd/yyyy");
        spinner.setEditor(editor);
        return spinner;
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return sdf.format(date);
    }

    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            return sdf.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    private void backToMenu(JPanel container) {
        AppUI menuUI = new MenuUI(fileManager);
        menuUI.show(container);
    }
}

//Handles generating the visualizations to display.
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

        String startDate = fileManager.getStartDate();
        String endDate = fileManager.getEndDate();

        List<StudySession> sessionsData = filterSessionsByDate(fileManager.getCourseStudyTimes(), startDate, endDate);

        if (sessionsData.isEmpty()) {
            JLabel label = new JLabel("No data available for visualization.", JLabel.CENTER);
            label.setFont(new Font("Arial", Font.PLAIN, 18));
            panel.add(label, BorderLayout.CENTER);
        } else {
            String visualizationType = fileManager.getVisualizationType();
            switch (visualizationType) {
                case "Line Graph":
                    createLineGraph(sessionsData);
                    break;
                case "Total Time":
                    createTotalTimeLabel(sessionsData);
                    break;
                case "Stacked Bar Chart":
                    createStackedBarChart(sessionsData);
                    break;
                case "Total Time by Subject":
                    createTotalTimeBySubjectBarChart(sessionsData);
                    break;
                case "Pie Chart by Subject":
                    createPieChart(sessionsData);
                    break;
                default:
                    createStackedBarChart(sessionsData);
            }
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


    private void createStackedBarChart(List<StudySession> sessionsData) {
        Map<String, Map<String, Float>> dayToCourseTimeMap = new LinkedHashMap<>();
        String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for (String day : daysOfWeek) {
            dayToCourseTimeMap.put(day, new HashMap<>());
        }
    
        for (StudySession session : sessionsData) {
            String date = session.getDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
            DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
            String dayName = dayOfWeek.name().charAt(0) + dayOfWeek.name().substring(1).toLowerCase();
            
            Map<String, Float> courseTimeMap = dayToCourseTimeMap.get(dayName);
    
            String courseName = session.getCourseName();
            float timeInMinutes = session.getDuration();
            courseTimeMap.put(courseName, courseTimeMap.getOrDefault(courseName, 0.0f) + timeInMinutes);
    
        }
    
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (String day : daysOfWeek) {
            Map<String, Float> courseTimeMap = dayToCourseTimeMap.get(day);
            for (Map.Entry<String, Float> courseEntry : courseTimeMap.entrySet()) {
                dataset.addValue(courseEntry.getValue(), courseEntry.getKey(), day);
            }
            if (courseTimeMap.isEmpty()) {
                for (String course : getAllCourses(sessionsData)) {
                    dataset.addValue(0.0, course, day);
                }
            }
        }
    
        JFreeChart barChart = ChartFactory.createStackedBarChart(
                "Study Time by Day of the Week",
                "Day of the Week",
                "Time (minutes)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    
        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.gray);
    
        ChartPanel chartPanel = new ChartPanel(barChart);
        panel.add(chartPanel, BorderLayout.CENTER);
    }


    private void createLineGraph(List<StudySession> sessionsData) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
    
        Map<String, Map<Integer, Float>> courseWeeklyData = new HashMap<>();
        Map<String, Integer> courseYear = new HashMap<>();
    
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
        for (StudySession session : sessionsData) {
            String courseName = session.getCourseName();
            float timeInMinutes = session.getDuration();
            String sessionDate = session.getDate();
    
            LocalDateTime dateTime = LocalDateTime.parse(sessionDate, formatter);
    
            int weekNumber = dateTime.get(WeekFields.ISO.weekOfYear());
    
            int year = dateTime.getYear();
    
            courseYear.putIfAbsent(courseName, year);
    
            courseWeeklyData
                .computeIfAbsent(courseName, k -> new HashMap<>())
                .merge(weekNumber, timeInMinutes, Float::sum);
        }
    
        for (Map.Entry<String, Map<Integer, Float>> courseEntry : courseWeeklyData.entrySet()) {
            String courseName = courseEntry.getKey();
            TimeSeries courseSeries = new TimeSeries(courseName);
    
            int year = courseYear.get(courseName);
    
            for (Map.Entry<Integer, Float> weekEntry : courseEntry.getValue().entrySet()) {
                int weekNumber = weekEntry.getKey();
                float totalTime = weekEntry.getValue();
    
                Week week = new Week(weekNumber, year);
    
                courseSeries.addOrUpdate(week, totalTime);
            }
    
            dataset.addSeries(courseSeries);
        }
    
        JFreeChart lineChart = ChartFactory.createTimeSeriesChart(
                "Study Time per Course (Weekly)",
                "Week",
                "Time (minutes)",
                dataset,
                true,
                true,
                false
        );
    
        XYPlot plot = (XYPlot) lineChart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
    
        ChartPanel chartPanel = new ChartPanel(lineChart);
        panel.add(chartPanel, BorderLayout.CENTER);
    } 

    private void createTotalTimeLabel(List<StudySession> sessionsData) {
        float totalTime = 0;
        for (StudySession session : sessionsData) {
            totalTime += session.getDuration();
        }

        JLabel totalLabel = new JLabel("Total Study Time: " + totalTime + " minutes", JLabel.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(totalLabel, BorderLayout.CENTER);
    }

    private void createTotalTimeBySubjectBarChart(List<StudySession> sessionsData) {
        Map<String, Float> subjectToTotalTimeMap = new HashMap<>();
    
        for (StudySession session : sessionsData) {
            String subjectName = session.getCourseName();
            float timeInMinutes = session.getDuration();
            subjectToTotalTimeMap.put(subjectName, subjectToTotalTimeMap.getOrDefault(subjectName, 0.0f) + timeInMinutes);
        }
    
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Float> entry : subjectToTotalTimeMap.entrySet()) {
            dataset.addValue(entry.getValue(), "Time Spent", entry.getKey());
        }
    
        JFreeChart barChart = ChartFactory.createBarChart(
                "Total Study Time by Subject",
                "Subject",
                "Time (minutes)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    
        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.gray);
    
        ChartPanel chartPanel = new ChartPanel(barChart);
        panel.add(chartPanel, BorderLayout.CENTER);
    }

    private void createPieChart(List<StudySession> sessionsData) {
        Map<String, Float> courseToTotalTimeMap = new HashMap<>();
        
        for (StudySession session : sessionsData) {
            String courseName = session.getCourseName();
            float timeInMinutes = session.getDuration();
            courseToTotalTimeMap.put(courseName, courseToTotalTimeMap.getOrDefault(courseName, 0.0f) + timeInMinutes);
        }
        
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Float> entry : courseToTotalTimeMap.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        
        JFreeChart pieChart = ChartFactory.createPieChart(
                "Study Time by Course",
                dataset,
                true,
                true,
                false
        );
        
        pieChart.setBackgroundPaint(Color.white);
        
        ChartPanel chartPanel = new ChartPanel(pieChart);
        panel.add(chartPanel, BorderLayout.CENTER);
    }

    private void backToMenu(JPanel container) {
        AppUI menuUI = new MenuUI(fileManager);
        menuUI.show(container);
    }

    public static List<String> getAllCourses(List<StudySession> sessionsData) {
        Set<String> courseNames = new HashSet<>();
        for (StudySession session : sessionsData) {
            courseNames.add(session.getCourseName());
        }
        return new ArrayList<>(courseNames);
    }

    private List<StudySession> filterSessionsByDate(List<StudySession> sessionsData, String startDate, String endDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
        LocalDate start = LocalDate.parse(startDate, dateFormatter);
        LocalDate end = LocalDate.parse(endDate, dateFormatter);
    
        return sessionsData.stream()
                .filter(session -> {
                    LocalDateTime sessionDateTime = LocalDateTime.parse(session.getDate(), inputFormatter);
                    LocalDate sessionDate = sessionDateTime.toLocalDate();
                    return !sessionDate.isBefore(start) && !sessionDate.isAfter(end);
                })
                .collect(Collectors.toList());
    }
    
}

//Handles the saving/retrieving for our data files, settings and study_sessions.
class FileManager {
    private int defaultStudyTime = 30;
    private String visualizationType = "Stacked Bar Chart";
    private String startDate = "12/01/2024";
    private String endDate = "01/01/2025";
    private static final String SETTINGS_FILE = "settings.csv";
    private static final String SESSION_FILE = "study_sessions.csv";

    public int getDefaultStudyTime() {
        return defaultStudyTime;
    }

    public void saveDefaultStudyTime(int time) {
        this.defaultStudyTime = time;
        saveSettings();
    }

    public String getVisualizationType() {
        return visualizationType;
    }

    public void saveVisualizationType(String type) {
        this.visualizationType = type;
        saveSettings();
    }

    public String getStartDate() {
        return startDate;
    }

    public void saveStartDate(String startDate) {
        this.startDate = startDate;
        saveSettings();
    }

    public String getEndDate() {
        return endDate;
    }

    public void saveEndDate(String endDate) {
        this.endDate = endDate;
        saveSettings();
    }

    private void saveSettings() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SETTINGS_FILE))) {
            writer.printf("defaultStudyTime,%d%n", defaultStudyTime);
            writer.printf("visualizationType,%s%n", visualizationType);
            writer.printf("startDate,%s%n", startDate);
            writer.printf("endDate,%s%n", endDate);

        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    public void loadSettings() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(SETTINGS_FILE));
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    switch (parts[0]) {
                        case "defaultStudyTime":
                            defaultStudyTime = Integer.parseInt(parts[1]);
                            break;
                        case "visualizationType":
                            visualizationType = parts[1];
                            break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
        }
    }

    public void saveSession(StudySession session) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SESSION_FILE, true))) {
            long timeSpentInMinutes = session.getDuration() / 60000;
            long startTimeStampInMinutes = session.getStartTimeStamp() / 60000;
            long stopTimeStampInMinutes = session.getStopTimeStamp() / 60000;

            ZonedDateTime startDate = Instant.ofEpochMilli(session.getStartTimeStamp()).atZone(ZoneId.of("America/New_York"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedStartDate = startDate.format(formatter);

            writer.printf("%s,%d,%d,%d,%s%n", session.getCourseName(), timeSpentInMinutes, startTimeStampInMinutes, stopTimeStampInMinutes, formattedStartDate);
        } catch (IOException e) {
            System.err.println("Error saving session: " + e.getMessage());
        }
    }

    public List<StudySession> getCourseStudyTimes() {
        List<StudySession> sessionsData = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(SESSION_FILE));
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String courseName = parts[0];
                    long timeSpent = Long.parseLong(parts[1]);
                    long startTime = Long.parseLong(parts[2]);
                    long endTime = Long.parseLong(parts[3]);
                    String date = parts[4];

                    StudySession newCourse = new StudySession(courseName, timeSpent, startTime, endTime, date);

                    sessionsData.add(newCourse);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading session data: " + e.getMessage());
        }
        return sessionsData;
    }
}

//Defines what a study session is.
class StudySession {
    private String courseName;
    private long duration;
    private long startTimeStamp;
    private long stopTimeStamp;
    private String date;

    public StudySession(String courseName, long duration, long startTimeStamp, long stopTimeStamp, String date) {
        this.courseName = courseName;
        this.duration = duration;
        this.startTimeStamp = startTimeStamp;
        this.stopTimeStamp = stopTimeStamp;
        this.date = date;
    }

    public String getCourseName() {
        return courseName;
    }

    public long getDuration() {
        return duration;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public long getStopTimeStamp() {
        return stopTimeStamp;
    }

    public String getDate() {
        return date;
    }
}

//Main class to run program.
public class StudyFoxApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("StudyFox");

            ImageIcon icon = new ImageIcon(StudyFoxApp.class.getResource("/studyfox_icon.png"));
            frame.setIconImage(icon.getImage());

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