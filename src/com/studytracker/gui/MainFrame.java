package com.studytracker.gui;

import com.studytracker.model.SessionManager;
import com.studytracker.model.StudySession;
import com.studytracker.timer.Timer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;

public class MainFrame extends JFrame implements Timer.TimerListener {

    private JTextField subjectField;
    private JTextField minutesField;
    private JTextField secondsField;
    private CircularTimerPanel timerPanel;
    private RoundedButton startButton, stopButton, resetButton, deleteButton;
    private DefaultListModel<StudySession> listModel;
    private JList<StudySession> sessionList;
    private JLabel totalTimeLabel;

    private SessionManager sessionManager;
    private Timer timer;

    // Advanced Theme Colors
    private final Color bgColor = new Color(18, 18, 18); // Darker base
    private final Color cardColor = new Color(30, 30, 35); // Lighter cards
    private final Color fgColor = new Color(240, 240, 240); // Soft white
    private final Color accentColor = new Color(138, 43, 226); // Purple
    private final Color timerRunningColor = new Color(46, 204, 113); // Emerald Green
    private final Color timerStoppedColor = new Color(231, 76, 60); // Alizarin Red
    
    public MainFrame() {
        sessionManager = new SessionManager();
        timer = new Timer(this);

        setTitle("Study Session Timer & Tracker");
        setSize(550, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(bgColor);

        initComponents();
        loadSessionsToUI();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Top Panel: Input fields (Card style) ---
        JPanel inputCard = new JPanel(new GridBagLayout());
        inputCard.setBackground(cardColor);
        inputCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 60), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        inputCard.add(createLabel("Subject Name:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7; gbc.gridwidth = 3;
        subjectField = createTextField();
        inputCard.add(subjectField, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        inputCard.add(createLabel("Minutes:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.2;
        minutesField = createTextField();
        minutesField.setText("0");
        inputCard.add(minutesField, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.3;
        inputCard.add(createLabel("Seconds:"), gbc);

        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.2;
        secondsField = createTextField();
        secondsField.setText("0");
        inputCard.add(secondsField, gbc);

        // --- Center Panel: Custom Circular Timer ---
        JPanel centerCard = new JPanel(new BorderLayout(10, 15));
        centerCard.setBackground(cardColor);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 60), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        timerPanel = new CircularTimerPanel();
        centerCard.add(timerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        
        startButton = new RoundedButton("▶️ Start", new Color(46, 204, 113), new Color(39, 174, 96));
        stopButton = new RoundedButton("⏸️ Stop", new Color(231, 76, 60), new Color(192, 57, 43));
        resetButton = new RoundedButton("🔄 Reset", new Color(52, 152, 219), new Color(41, 128, 185));

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(resetButton);
        centerCard.add(buttonPanel, BorderLayout.SOUTH);

        // --- Bottom Panel: Styled Session History ---
        JPanel historyCard = new JPanel(new BorderLayout(5, 10));
        historyCard.setBackground(bgColor); // Blend into background
        historyCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(), 
                "Session History", 
                0, 0, new Font("Segoe UI", Font.BOLD, 16), fgColor));

        listModel = new DefaultListModel<>();
        sessionList = new JList<>(listModel);
        sessionList.setBackground(bgColor);
        sessionList.setCellRenderer(new SessionCardRenderer());
        
        JScrollPane scrollPane = new JScrollPane(sessionList);
        scrollPane.getViewport().setBackground(bgColor);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(0, 180));
        historyCard.add(scrollPane, BorderLayout.CENTER);

        JPanel historyBottomPanel = new JPanel(new BorderLayout());
        historyBottomPanel.setOpaque(false);
        
        totalTimeLabel = createLabel("Total Time: 00:00");
        totalTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalTimeLabel.setForeground(accentColor);
        
        deleteButton = new RoundedButton("🗑️ Delete", new Color(100, 100, 120), new Color(80, 80, 100));
        
        historyBottomPanel.add(totalTimeLabel, BorderLayout.WEST);
        historyBottomPanel.add(deleteButton, BorderLayout.EAST);
        historyCard.add(historyBottomPanel, BorderLayout.SOUTH);

        // Add main panels
        add(inputCard, BorderLayout.NORTH);
        add(centerCard, BorderLayout.CENTER);
        add(historyCard, BorderLayout.SOUTH);

        // --- Event Listeners ---
        startButton.addActionListener(e -> handleStart());
        stopButton.addActionListener(e -> {
            timer.stop();
            timerPanel.updateColor(timerStoppedColor);
            timerPanel.updateMotivation("Paused.");
            setControlsEnabled(true);
        });
        resetButton.addActionListener(e -> {
            timer.reset();
            timerPanel.updateColor(fgColor);
            timerPanel.updateMotivation("Ready to focus?");
            setControlsEnabled(true);
        });
        deleteButton.addActionListener(e -> handleDelete());
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(fgColor);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                super.paintComponent(g);
            }
        };
        field.setBackground(new Color(50, 50, 60));
        field.setForeground(fgColor);
        field.setCaretColor(fgColor);
        field.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setOpaque(false);
        return field;
    }

    private void setControlsEnabled(boolean enabled) {
        subjectField.setEnabled(enabled);
        minutesField.setEnabled(enabled);
        secondsField.setEnabled(enabled);
        startButton.setEnabled(enabled);
    }

    private void handleStart() {
        if (timer.isRunning()) return;

        int remainingInTimer = 0;
        try {
            String[] parts = timerPanel.getTimeText().split(":");
            remainingInTimer = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (Exception e) {}

        if (remainingInTimer == 0 || remainingInTimer == timer.getInitialSeconds()) {
            String subject = subjectField.getText().trim();
            String minutesStr = minutesField.getText().trim();
            String secondsStr = secondsField.getText().trim();

            if (subject.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a subject.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int mins = 0;
            int secs = 0;
            try {
                if (!minutesStr.isEmpty()) mins = Integer.parseInt(minutesStr);
                if (!secondsStr.isEmpty()) secs = Integer.parseInt(secondsStr);
                
                if (mins < 0 || secs < 0) throw new NumberFormatException();
                if (mins == 0 && secs == 0) {
                    JOptionPane.showMessageDialog(this, "Please enter a duration greater than 0.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                timer.setDuration(mins, secs);
                timerPanel.updateProgress(1.0f); // Reset full arc
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Time must be positive integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        setControlsEnabled(false);
        timerPanel.updateColor(timerRunningColor);
        timerPanel.updateMotivation("Stay focused 🚀");
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
        totalTimeLabel.setText("Total Time: " + sessionManager.getTotalTimeFormatted());
        // Scroll to bottom
        int size = listModel.getSize();
        if (size > 0) {
            sessionList.ensureIndexIsVisible(size - 1);
        }
    }

    @Override
    public void onTick(String timeString) {
        float p = (float) timer.getRemainingSeconds() / timer.getInitialSeconds();
        timerPanel.updateState(timeString, p);
    }

    @Override
    public void onFinish() {
        setControlsEnabled(true);
        timerPanel.updateColor(accentColor);
        timerPanel.updateMotivation("Session Complete! 🎉");
        timerPanel.updateProgress(0.0f);

        String subject = subjectField.getText().trim();
        int totalSeconds = timer.getInitialSeconds();
        
        StudySession newSession = new StudySession(subject, totalSeconds, LocalDateTime.now());
        sessionManager.addSession(newSession);
        
        loadSessionsToUI();
        
        JOptionPane.showMessageDialog(this, "Session Completed! Great job studying " + subject + "!", "Time's Up", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    // CUSTOM INNER UI COMPONENTS
    // =========================================================================

    /** Custom Circular Timer Panel using Graphics2D */
    class CircularTimerPanel extends JPanel {
        private String timeText = "00:00";
        private String motivationalText = "Ready to focus?";
        private float progress = 1.0f; 
        private Color progressColor = fgColor;

        public CircularTimerPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(250, 250));
        }

        public void updateState(String timeText, float progress) {
            this.timeText = timeText;
            this.progress = progress;
            repaint();
        }

        public void updateColor(Color c) {
            this.progressColor = c;
            repaint();
        }

        public void updateMotivation(String text) {
            this.motivationalText = text;
            repaint();
        }

        public void updateProgress(float p) {
            this.progress = p;
            repaint();
        }

        public String getTimeText() { return timeText; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int strokeWidth = 14;
            int size = Math.min(getWidth(), getHeight()) - strokeWidth * 2 - 10;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2 + 10;

            // Background Track
            g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(50, 50, 60));
            g2.drawArc(x, y, size, size, 0, 360);

            // Colored Progress Arc
            g2.setColor(progressColor);
            int angle = (int) (360 * progress);
            g2.drawArc(x, y, size, size, 90, angle);

            // Center Time Text
            g2.setFont(new Font("Segoe UI", Font.BOLD, 52));
            FontMetrics fm = g2.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(timeText)) / 2;
            int textY = y + size / 2 + fm.getAscent() / 4 + 5;
            g2.drawString(timeText, textX, textY);

            // Motivational Subtext
            g2.setColor(new Color(150, 150, 160));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            FontMetrics fm2 = g2.getFontMetrics();
            int motX = (getWidth() - fm2.stringWidth(motivationalText)) / 2;
            int motY = y + size / 2 - fm.getAscent() / 2 - 20;
            g2.drawString(motivationalText, motX, motY);

            g2.dispose();
        }
    }

    /** Modern Rounded Button with Hover Effects */
    class RoundedButton extends JButton {
        private Color normalColor;
        private Color hoverColor;

        public RoundedButton(String text, Color normalColor, Color hoverColor) {
            super(text);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(110, 40));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        setBackground(hoverColor);
                        repaint();
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(normalColor);
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (!isEnabled()) {
                g2.setColor(new Color(60, 60, 70));
            } else if (getModel().isPressed()) {
                g2.setColor(hoverColor.darker());
            } else if (getModel().isRollover()) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(normalColor);
            }
            
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Card-styled List Renderer for Session History */
    class SessionCardRenderer implements ListCellRenderer<StudySession> {
        @Override
        public Component getListCellRendererComponent(JList<? extends StudySession> list, StudySession value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel card = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    if (isSelected) {
                        g2.setColor(new Color(50, 50, 70));
                    } else {
                        g2.setColor(cardColor);
                    }
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                    
                    // Highlight the most recent session
                    if (index == list.getModel().getSize() - 1) { 
                        g2.setColor(accentColor);
                        g2.setStroke(new BasicStroke(2));
                        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                    } else {
                        g2.setColor(new Color(60, 60, 70));
                        g2.setStroke(new BasicStroke(1));
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                    }
                    g2.dispose();
                }
            };
            card.setOpaque(false);
            card.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

            JLabel subjectLbl = new JLabel(value.getSubject());
            subjectLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            subjectLbl.setForeground(fgColor);

            int mins = value.getDurationSeconds() / 60;
            int secs = value.getDurationSeconds() % 60;
            JLabel timeLbl = new JLabel(String.format("⏱ %02d:%02d", mins, secs));
            timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            timeLbl.setForeground(new Color(180, 180, 190));

            card.add(subjectLbl, BorderLayout.WEST);
            card.add(timeLbl, BorderLayout.EAST);

            // Wrapper for spacing
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(bgColor);
            wrapper.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            wrapper.add(card, BorderLayout.CENTER);

            return wrapper;
        }
    }
}
