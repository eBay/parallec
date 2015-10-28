/*  
Copyright [2013-2015] eBay Software Foundation
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package io.parallec.core.task;

import io.parallec.core.util.PcDateUtils;


/**
 * The Class TaskErrorMeta.
 */
public class TaskErrorMeta {

    /**
     * The Enum TaskErrorType.
     */
    public enum TaskErrorType {

        /** The validation error. */
        VALIDATION_ERROR,
        /** The global timeout. */
        GLOBAL_TIMEOUT,
        /** The command manager error. */
        COMMAND_MANAGER_ERROR,
        /** User canceled */
        USER_CANCELED,
        /** The unknown. */
        UNKNOWN

    }

    /** The type. */
    private TaskErrorType type;

    /** The error time. */
    private String errorTime;

    /** The throwable. */
    private Throwable throwable;

    /** The details. */
    private String details;

    /**
     * Instantiates a new task error meta.
     *
     * @param type
     *            the type
     * @param details
     *            the details
     */
    public TaskErrorMeta(TaskErrorType type, String details) {

        this.type = type;
        this.errorTime = PcDateUtils.getNowDateTimeStrStandard();
        this.details = details;
    }

    /**
     * Instantiates a new task error meta.
     *
     * @param type
     *            the type
     * @param details
     *            the details
     * @param throwable
     *            the throwable
     */
    public TaskErrorMeta(TaskErrorType type, String details, Throwable throwable) {

        this.type = type;
        this.errorTime = PcDateUtils.getNowDateTimeStrStandard();
        this.details = details;
        this.throwable = throwable;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public TaskErrorType getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type
     *            the new type
     */
    public void setType(TaskErrorType type) {
        this.type = type;
    }

    /**
     * Gets the error time.
     *
     * @return the error time
     */
    public String getErrorTime() {
        return errorTime;
    }

    /**
     * Sets the error time.
     *
     * @param errorTime
     *            the new error time
     */
    public void setErrorTime(String errorTime) {
        this.errorTime = errorTime;
    }

    /**
     * Gets the throwable.
     *
     * @return the throwable
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Sets the throwable.
     *
     * @param throwable
     *            the new throwable
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * Gets the details.
     *
     * @return the details
     */
    public String getDetails() {
        return details;
    }

    /**
     * Sets the details.
     *
     * @param details
     *            the new details
     */
    public void setDetails(String details) {
        this.details = details;
    }

}
