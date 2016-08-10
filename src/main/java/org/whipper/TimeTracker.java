package org.whipper;

/**
 * Interface for tracking time information of the test.
 *
 * @author Juraj Duráni
 */
public interface TimeTracker{

    /**
     * Returns start (system) time of the test.
     *
     * @return start time or -1 if test has not been started yet
     */
    long getStartTime();

    /**
     * Returns end (system) time of the test.
     *
     * @return end time or -1 if test has not yet ended
     */
    long getEndTime();

    /**
     * Returns duration of the test.
     *
     * @return duration or -1 if test either has not been started or has not ended
     */
    long getDuration();
}
