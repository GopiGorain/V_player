package com.vplayer.services;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

/**
 * Service to manage system power states, specifically to prevent the display
 * from turning off or the system from entering sleep mode during video playback.
 */
public class ScreenSleepService {
    
    /**
     * JNA mapping for Windows Kernel32 library.
     */
    private interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);
        
        /**
         * Enables an application to inform the system that it is in use, 
         * thereby preventing the system from entering sleep or turning off 
         * the display while the application is running.
         */
        int SetThreadExecutionState(int esFlags);
    }

    // Execution state flags
    private static final int ES_CONTINUOUS = 0x80000000;
    private static final int ES_DISPLAY_REQUIRED = 0x00000002;
    private static final int ES_SYSTEM_REQUIRED = 0x00000001;

    private static boolean isPreventing = false;

    /**
     * Prevents the display from locking and the system from sleeping.
     * This should be called when video playback starts.
     */
    public static void preventSleep() {
        if (isWindows() && !isPreventing) {
            try {
                // ES_CONTINUOUS ensures the state persists until the next call
                // ES_DISPLAY_REQUIRED keeps the monitor on
                // ES_SYSTEM_REQUIRED keeps the system from sleeping
                Kernel32.INSTANCE.SetThreadExecutionState(ES_CONTINUOUS | ES_DISPLAY_REQUIRED | ES_SYSTEM_REQUIRED);
                isPreventing = true;
                System.out.println("Power Management: Display sleep prevented");
            } catch (Throwable t) {
                System.err.println("Power Management: Failed to prevent sleep: " + t.getMessage());
            }
        }
    }

    /**
     * Allows the display to turn off and the system to sleep normally.
     * This should be called when video playback is paused, stopped, or finished.
     */
    public static void allowSleep() {
        if (isWindows() && isPreventing) {
            try {
                // Calling SetThreadExecutionState with only ES_CONTINUOUS resets the state
                Kernel32.INSTANCE.SetThreadExecutionState(ES_CONTINUOUS);
                isPreventing = false;
                System.out.println("Power Management: Display sleep allowed");
            } catch (Throwable t) {
                System.err.println("Power Management: Failed to allow sleep: " + t.getMessage());
            }
        }
    }

    /**
     * Checks if the current OS is Windows.
     */
    private static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }
}
