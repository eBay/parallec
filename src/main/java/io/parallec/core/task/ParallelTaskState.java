package io.parallec.core.task;

// Job Id to Job Data
/**
 * The Enum ParallelTaskState.
 */
// finishNotGathered is slave are ready but not send to Director
public enum ParallelTaskState {

    /** The waiting. */
    WAITING,
    /** The in progress. */
    IN_PROGRESS,
    /** The completed without error. */
    COMPLETED_WITHOUT_ERROR,
    /** The completed with error. */
    COMPLETED_WITH_ERROR
}