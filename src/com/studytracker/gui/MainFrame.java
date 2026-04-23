package com.studytracker.gui;

import com.studytracker.model.SessionManager;
import com.studytracker.model.StudySession;
import com.studytracker.timer.Timer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;

public class MainFrame extends JFrame implements Timer.TimerListener {

    private JTextField subjectField;
    private JTextField durationField;
    private JLabel timerLabel;
    private JButton startButton, stopButton, resetButton, deleteButton;
    private DefaultListModel<StudySession> listModel;
    private JList<StudySession> sessionList;
    private JLabel totalTimeLabel;

    private SessionManager sessionManager;
    private Timer timer;
    private int currentDuration;

    public MainFrame() {
        sessionManager = new SessionManager();
        timer = new Timer(this);

        setTitle("Study Session Timer & Tracker");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadSessionsToUI();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Top Panel: Input fields ---
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.add(new JLabel("Subject Name:"));
        subjectField = new JTextField();
        inputPanel.add(subjectField);

        inputPanel.add(new JLabel("Duration (minutes):"));
        durationField = new JTextField();
        inputPanel.add(durationField);

        // --- Center Panel: Timer and Controls ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        
        timerLabel = new JLabel("00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 64));
        centerPanel.add(timerLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        resetButton = new JButton("Reset");

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(resetButton);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- Bottom Panel: Session History ---
        JPanel historyPanel = new JPanel(new BorderLayout(5, 5));
        historyPanel.setBorder(BorderFactory.createTitledBorder("Session History"));

        listModel = new DefaultListModel<>();
        sessionList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(sessionList);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel historyBottomPanel = new JPanel(new BorderLayout());
        totalTimeLabel = new JLabel("Total Time: 0 mins");
        deleteButton = new JButton("Delete Selected");
        historyBottomPanel.add(totalTimeLabel, BorderLayout.WEST);
        historyBottomPanel.add(deleteButton, BorderLayout.EAST);
        historyPanel.add(historyBottomPanel, BorderLayout.SOUTH);

        // Add main panels
        add(inputPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(historyPanel, BorderLayout.SOUTH);

        // --- Event Listeners ---
        startButton.addActionListener(e -> handleStart());
        stopButton.addActionListener(e -> timer.stop());
        resetButton.addActionListener(e -> timer.reset());
        deleteButton.addActionListener(e -> handleDelete());
    }

    private void handleStart() {
        if (timer.isRunning()) return;

        // If not started yet or reset, read from inputs
        if (timerLabel.getText().equals("00:00") || timerLabel.getText().equals(String.format("%02d:00", currentDuration))) {
            String subject = subjectField.getText().trim();
            String durationStr = durationField.getText().trim();

            if (subject.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a subject.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (durationStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a duration.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                currentDuration = Integer.parseInt(durationStr);
                if (currentDuration <= 0) {
                    throw new NumberFormatException();
                }
                timer.setDuration(currentDuration);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Duration must be a positive integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Disable inputs while running
        subjectField.setEnabled(false);
        durationField.setEnabled(false);
        timer.start();
    }

    private void handleDelete() {
        int selectedIndex = sessionList.getSelectedIndex();
        if (selectedIndex != -1) {
            sessionManager.deleteSession(selectedIndex);
            loadSessionsToUI();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a session to delete.", "Delete Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void loadSessionsToUI() {
        listModel.clear();
        for (StudySession session : sessionManager.getSessions()) {
            listModel.addElement(session);
        }
        totalTimeLabel.setText("Total Time: " + sessionManager.getTotalTime() + " mins");
    }

    @Override
    public void onTick(String timeString) {
        timerLabel.setText(timeString);
    }

    @Override
    public void onFinish() {
        // Re-enable inputs
        subjectField.setEnabled(true);
        durationField.setEnabled(true);

        String subject = subjectField.getText().trim();
        StudySession newSession = new StudySession(subject, currentDuration, LocalDateTime.now());
        sessionManager.addSession(newSession);
        
        loadSessionsToUI();
        
        JOptionPane.showMessageDialog(this, "Session Completed! Great job studying " + subject + "!", "Time's Up", JOptionPane.INFORMATION_MESSAGE);
    }
}
