package com.mycompany.ssproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * ExternalCommands class takes a full ProcessBuilder command and runs it.
 * @author Umar Ali
 * @author James Maguire
 */
public class ExternalCommands {

    /**
     * Takes the command and runs it with Process Builder then returns the result to the server.
     * @param fullCommand The full input parsed into an array
     * @return The output from the Process Builder to display on the webserver
     * @throws IOException Error with input handling
     * @throws InterruptedException Error with threading
     */
    public List<String> ExternalCommand(String[] fullCommand) throws IOException, InterruptedException {
        var processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(BuiltInCommands.getCurrentDirectory()));
        int parameterCount = fullCommand.length;
        processBuilder.command(fullCommand);
        var process = processBuilder.start();

        var ret = process.waitFor();
        List<String> output = new ArrayList<String>();

        try (var reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.add( line );
            }
        }
        output.add("<br>");
        output.add("Program exited with code: " + ret);
        return output;
    }
}
