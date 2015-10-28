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
package io.parallec.core.exception;

/**
 * The Class ExecutionManagerExecutionException.
 */
public class ExecutionManagerExecutionException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The type. */
    private ManagerExceptionType type;

    /**
     * The Enum ManagerExceptionType.
     */
    public enum ManagerExceptionType {

        /** The timeout. */
        TIMEOUT,
        /** The cancel. */
        CANCEL
    }

    /**
     * Instantiates a new command manager execution exception.
     *
     * @param error
     *            the error
     * @param type
     *            the type
     */
    public ExecutionManagerExecutionException(String error,
            ManagerExceptionType type) {
        super(error);
        this.setType(type);
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public ManagerExceptionType getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type
     *            the new type
     */
    public void setType(ManagerExceptionType type) {
        this.type = type;
    }

}
