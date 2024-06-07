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

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

/**
 * Processor used to send commands to PowerShell console.
 * <p>
 * It works as an independent thread and its results are collected using the Future interface.
 *
 * @author Javier Garcia Alonso
 */
class PowerShellScriptProcessor extends PowerShellCommandProcessor implements Callable<String> {

    /**
     * Constructor that takes the output and the input of the PowerShell session
     *
     * @param inputStream the stream needed to read the command output
     * @param waitPause   long the wait pause in milliseconds
     */
    public PowerShellScriptProcessor(InputStream inputStream, int waitPause) {
        super(inputStream, waitPause);
    }

    @Override
    protected boolean canContinueReading(final StringBuilder powerShellOutput)
            throws IOException, InterruptedException {

        return powerShellOutput.length() < PowerShell.END_SCRIPT_STRING.length()
                || powerShellOutput.length() >= PowerShell.END_SCRIPT_STRING.length()
                        && powerShellOutput.lastIndexOf(PowerShell.END_SCRIPT_STRING) == -1;
    }
}
