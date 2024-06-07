/*
 * Copyright 2016-2018 Javier Garcia Alonso.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.profesorfalken.jpowershell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processor used to send commands to PowerShell console.<p>
 * It works as an independent thread and its results are collected using the Future interface.
 *
 * @author Javier Garcia Alonso
 */
class PowerShellCommandProcessor implements Callable<String> {

    private final BufferedReader reader;

    protected boolean closed = false;

    protected final int waitPause;

    /**
     * Constructor that takes the output and the input of the PowerShell session
     *
     * @param name        the name of the CommandProcessor
     * @param inputStream the stream needed to read the command output
     * @param waitPause   long the wait pause in milliseconds
     */
    public PowerShellCommandProcessor(InputStream inputStream, int waitPause) {
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
        this.waitPause = waitPause;
    }

    /**
     * Calls the command and returns its output
     *
     * @return String output of call
     * @throws InterruptedException error when reading data
     */
    @Override
    public String call() throws InterruptedException {
        StringBuilder powerShellOutput = new StringBuilder();

        try {
            if (startReading()) {
                readData(powerShellOutput);
            }
        } catch (IOException ioe) {
            Logger.getLogger(PowerShell.class.getName())
                    .log(Level.SEVERE, "Unexpected error reading PowerShell output", ioe);
            return ioe.getMessage();
        }

        // Remove last CRLF and possible end script string from result
        return powerShellOutput.toString().replaceAll("\\s+$", "").replace(PowerShell.END_SCRIPT_STRING, "");
    }

    // Reads all data from output
    private void readData(StringBuilder powerShellOutput) throws IOException {

        int c = 0;
        while ((c = reader.read()) != -1) {

            // Exit when closed or command has finished
            try {
                if (this.closed || !canContinueReading(powerShellOutput)) {
                    break;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(PowerShellCommandProcessor.class.getName())
                        .log(Level.SEVERE, "Error executing command and reading result", ex);
            }

            powerShellOutput.append((char) c);
        }
    }

    // Checks when we can start reading the output. Timeout if it takes too long in order to avoid hangs
    private boolean startReading() throws IOException, InterruptedException {
        // If the reader is not ready, gives it some milliseconds
        while (!this.reader.ready()) {
            Thread.sleep(this.waitPause);
            if (this.closed) {
                return false;
            }
        }
        return true;
    }

    // Checks when we the reader can continue to read.
    protected boolean canContinueReading(final StringBuilder powerShellOutput)
            throws IOException, InterruptedException {
        // If the reader is not ready, gives it some milliseconds
        // It is important to do that, because the ready method guarantees that the readline will not be blocking
        if (!this.reader.ready()) {
            Thread.sleep(this.waitPause);
        }

        // If not ready yet, wait a moment to make sure it is finished
        if (!this.reader.ready()) {
            Thread.sleep(50);
        }

        return this.reader.ready();
    }

    /**
     * Closes the command processor, canceling the current work if not finish
     */
    public void close() {
        this.closed = true;
    }
}
