package com.studytracker.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the collection of study sessions, including calculating totals
 * and persisting data to disk.
 */
public class SessionManager {
    private List<StudySession> sessions;
    private static final String DATA_FILE = "sessions.dat";

    public SessionManager() {
        sessions = new ArrayList<>();
        loadSessions();
    }

    public void addSession(StudySession session) {
        sessions.add(session);
        saveSessions();
    }

    public List<StudySession> getSessions() {
        return sessions;
    }

    public int getTotalTime() {
        int total = 0;
        for (StudySession session : sessions) {
            total += session.getDuration();
        }
        return total;
    }
    
    public void deleteSession(int index) {
        if (index >= 0 && index < sessions.size()) {
            sessions.remove(index);
            saveSessions();
        }
    }

    private void saveSessions() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(sessions);
        } catch (IOException e) {
            System.err.println("Error saving sessions: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadSessions() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                sessions = (List<StudySession>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading sessions: " + e.getMessage());
                sessions = new ArrayList<>();
            }
        }
    }
}
