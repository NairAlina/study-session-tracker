package com.studytracker.timer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Handles the countdown logic and communicates with the UI.
 */
public class Timer {
    private int remainingSeconds;
    private int initialSeconds;
    private javax.swing.Timer swingTimer;
    private TimerListener listener;

    public interface TimerListener {
        void onTick(String timeString);
        void onFinish();
    }

    public Timer(TimerListener listener) {
        this.listener = listener;
        this.swingTimer = new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (remainingSeconds > 0) {
                    remainingSeconds--;
                    updateListener();
                    if (remainingSeconds == 0) {
                        stop();
                        if (Timer.this.listener != null) {
                            Timer.this.listener.onFinish();
                        }
                    }
                }
            }
        });
    }

    public void setDuration(int minutes, int seconds) {
        this.initialSeconds = (minutes * 60) + seconds;
        this.remainingSeconds = this.initialSeconds;
        updateListener();
    }
    
    public int getInitialSeconds() {
        return initialSeconds;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void start() {
        if (!swingTimer.isRunning() && remainingSeconds > 0) {
            swingTimer.start();
        }
    }

    public void stop() {
        if (swingTimer.isRunning()) {
            swingTimer.stop();
        }
    }

    public void reset() {
        stop();
        this.remainingSeconds = this.initialSeconds;
        updateListener();
    }

    private void updateListener() {
        if (listener != null) {
            int mins = remainingSeconds / 60;
            int secs = remainingSeconds % 60;
            String timeString = String.format("%02d:%02d", mins, secs);
            listener.onTick(timeString);
        }
    }

    public boolean isRunning() {
        return swingTimer.isRunning();
    }
}
