package com.studytracker.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single study session.
 */
public class StudySession implements Serializable {
    private static final long serialVersionUID = 2L;
    
    private String subject;
    private int durationSeconds; // Duration in seconds for precision
    private LocalDateTime date;

    public StudySession(String subject, int durationSeconds, LocalDateTime date) {
        this.subject = subject;
        this.durationSeconds = durationSeconds;
        this.date = date;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        int mins = durationSeconds / 60;
        int secs = durationSeconds % 60;
        return String.format("%s - %02d:%02d (%s)", subject, mins, secs, date.format(formatter));
    }
}
