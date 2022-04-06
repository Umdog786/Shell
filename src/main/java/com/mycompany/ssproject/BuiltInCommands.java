package com.mycompany.ssproject;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.io.FileUtils;

/**
 * BuiltInCommands class takes a full parsed command and runs th correct method to get the desired result.
 * @author Umar Ali
 * @author James Maguire
 */
public class BuiltInCommands {
    private static String loggedInUsername = "";
    private static boolean loggedIn = false;
    private static String loggedInUserType = "";
    private static List<String> builtInOutput = new ArrayList<>();
    private static String currentDirectory = System.getProperty("user.dir");

    /**
     * Uses the parsed command to check if the user has permission to run the command and if so calls the correct class.
     * @param fullCommand The full input parsed into an array
     * @throws IOException Error with input handling
     * @throws CsvException Error with CSV handling
     */
    public void determineCommand(String[] fullCommand) throws IOException, CsvException {
        clearBuiltInOutput();
        if (loggedIn) {
            if (Objects.equals(fullCommand[0], "super")) {
                if (getLoggedInUserType().equals("super")) {
                    if (Objects.equals(fullCommand[1], "addUser")) {
                        if (builtInAddUser(fullCommand)) {
                            builtInOutput.add("<span style=\"color:#4d9a06;\">User added successfully</span>");
                        }
                    } else if (Objects.equals(fullCommand[1], "delUser")) {
                        if (builtInDelUser(fullCommand)) {
                            builtInOutput.add("<span style=\"color:#4d9a06;\">User deleted successfully</span>");
                        }
                    } else if (Objects.equals(fullCommand[1], "chPass")) {
                        if (builtInChangePassword(fullCommand)) {
                            builtInOutput.add("<span style=\"color:#4d9a06;\">Password changed successfully!</span>");
                        }
                    } else if (Objects.equals(fullCommand[1], "chUserType")) {
                        if (builtInChangeUserType(fullCommand)) {
                            builtInOutput.add("<span style=\"color:#4d9a06;\">User-type changed successfully</span>");
                        }
                    } else if (Objects.equals(fullCommand[1], "chUsername")) {
                        if (builtInChangeUsername(fullCommand)) {
                            builtInOutput.add("<span style=\"color:#4d9a06;\">Username changed Successfully</span>");
                        }
                    }
                } else {
                    builtInOutput.add("<span style=\"color:red;\">Unable to execute super commands whilst logged in as standard user</span>");
                }
            }

            if (Objects.equals(fullCommand[0], "copy")) {
                builtInCopy(fullCommand);
            } else if (Objects.equals(fullCommand[0], "move")) {
                builtInMove(fullCommand);
            } else if (Objects.equals(fullCommand[0], "whoami")) {
                builtInWhoAmI();
            } else if (Objects.equals(fullCommand[0], "login")) {
                builtInOutput.add("<span style=\"color:red;\">You are already logged in.</span>");
            } else if (Objects.equals(fullCommand[0], "logoff")) {
                builtInLogoff();
                builtInOutput.add("<span style=\"color:#4d9a06;\">Logoff successful</span>");
            } else if (Objects.equals(fullCommand[0], "showDir")) {
                builtInShowDir();
            } else if (Objects.equals(fullCommand[0], "cd")) {
                builtInChangeDirectory(fullCommand);
            } else if (Objects.equals(fullCommand[0], "help")) {
                builtInHelp();
            }
        } else if(Objects.equals(fullCommand[0], "login")) {
            builtInLogin(fullCommand);
        }
        else {
            builtInLogoff();
            builtInOutput.add("<span style=\"color:red;\">You must be logged in to execute commands.</span>");
            builtInOutput.add("Please enter \"login [username]\"");
        }
    }

    /**
     * Displays all available commands and their syntax.
     */
    private void builtInHelp() {
        builtInOutput.add("Shell - Version 1.0-release");
        builtInOutput.add("These shell commands are defined internally.  Type `help' to see this list.");
        builtInOutput.add("A star (*) next to a name means that the command requires super permission.");
        builtInOutput.add("---------------------------------------------------------------");
        builtInOutput.add("cd [targetDirectory][..]");
        builtInOutput.add("ls");
        builtInOutput.add("cp [sourceFile] [destinationDirectory] ");
        builtInOutput.add("mv [sourceFile] [destinationDirectory]");
        builtInOutput.add("mkdir [directory]");
        builtInOutput.add("rmdir [directory]");
        builtInOutput.add("pwd");
        builtInOutput.add("ps");
        builtInOutput.add("which [command]");
        builtInOutput.add("*super");
        builtInOutput.add("*addUser [userType] [username] [password]");
        builtInOutput.add("*delUser [username]");
        builtInOutput.add("*chPass [username] [newPassword]");
        builtInOutput.add("*chUserType [username] [newUserType]");
        builtInOutput.add("*chUsername [oldUsername] [newUsername]");
        builtInOutput.add("copy [sourceFile] [destinationDirectory]");
        builtInOutput.add("move [sourceFile] [destinationDirectory]");
        builtInOutput.add("whoami");
        builtInOutput.add("login [username]");
        builtInOutput.add("logoff");
        builtInOutput.add("showDir");
        builtInOutput.add("help");
    }

    /**
     * Adds a user to the system
     * @param fullCommand The full input parsed into an array
     * @return true if Successful or False if failed
     * @throws IOException Error with input handling
     */
    public static boolean builtInAddUser(String[] fullCommand) throws IOException {
        if (fullCommand.length != 5) {
            builtInOutput.add("<span style=\"color:red;\">Invalid number of fields entered.</span> super addUser [userType] [username] [password]");
            return false;
        } else {
            String userType = fullCommand[2];
            String username = fullCommand[3];
            String password = fullCommand[4];
            if (userType.equals("standard") || userType.equals("super")) {
                FileWriter writer = new FileWriter("UserCredentials.csv", true);
                writer.append(userType); // User type
                writer.append(",");
                writer.append(username); // Username
                writer.append(",");
                writer.append(password); // Password
                writer.append("\n");
                writer.flush();
                writer.close();
            } else {
                builtInOutput.add("<span style=\"color:red;\">Invalid user type entered</span>");
                return false;
            }
            return true;
        }
    }

    /**
     *
     * Removes a user from the system
     * @param fullCommand The full input parsed into an array
     * @return true if Successful or False if failed
     * @throws IOException Error with input handling
     * @throws CsvException Error with CSV handling
     */
    public static boolean builtInDelUser(String[] fullCommand) throws IOException, CsvException {
        if (fullCommand.length != 3) {
            builtInOutput.add("<span style=\"color:red;\">Invalid number of fields entered. super delUser [username]</span>");
            return false;
        } else {
            File oldCSV = new File("UserCredentials.csv");
            File newCSV = new File("result.csv");
            int targetRow = 0;
            CSVReader reader = new CSVReader(new FileReader(oldCSV));
            List<String[]> myEntries = reader.readAll();

            boolean foundFlag = false;
            String targetUsername = fullCommand[2];

            for (int row=0; row < myEntries.size(); row++) {
                if (myEntries.get(row)[1].equals(targetUsername)) {
                    targetRow = row;
                    foundFlag = true;
                }
            }
            if (!foundFlag) {
                builtInOutput.add("<span style=\"color:red;\">Username not found in user database</span>");
                return false;
            }
            myEntries.remove(targetRow);
            FileWriter fw = new FileWriter(newCSV);
            CSVWriter w = new CSVWriter(fw);
            w.writeAll(myEntries);
            w.flush();
            w.close();
            oldCSV.delete();
            newCSV.renameTo(new File("UserCredentials.csv"));
            return true;
        }

    }

    /**
     *
     * Changes an existing users password
     * @param fullCommand The full input parsed into an array
     * @return true if Successful or False if failed
     * @throws IOException Error with input handling
     * @throws CsvException Error with CSV handling
     */
    public static boolean builtInChangePassword(String[] fullCommand) throws IOException, CsvException {
        if (fullCommand.length != 4) {
            builtInOutput.add("<span style=\"color:red;\">Invalid number of fields entered.</span> super chPass [username] [newPassword]");
            return false;
        } else {
            File userCredentialsCSV = new File("UserCredentials.csv");
            CSVReader reader = new CSVReader(new FileReader(userCredentialsCSV));
            List<String[]> myEntries = reader.readAll();
            String enteredUsername = fullCommand[2];
            boolean found = false;
            for (int targetRow = 0; targetRow < myEntries.size(); targetRow++) {
                if (myEntries.get(targetRow)[1].equals(enteredUsername)) {
                    found = true;
                    String newPassword = fullCommand[3];

                    myEntries.get(targetRow)[2] = newPassword;
                    CSVWriter writer = new CSVWriter(new FileWriter(userCredentialsCSV));
                    writer.writeAll(myEntries);
                    writer.flush();
                    writer.close();
                }
            }
            if (!found) {
                builtInOutput.add("<span style=\"color:red;\">Username not found in user database</span>");
                return false;
            }
        }
        return true;
    }

    /**
     *
     * Changes an existing users user type
     * @param fullCommand The full input parsed into an array
     * @return true if Successful or False if failed
     * @throws IOException Error with input handling
     * @throws CsvException Error with CSV handling
     */
    public static boolean builtInChangeUserType(String[] fullCommand) throws IOException, CsvException {
        if (fullCommand.length != 4) {
            builtInOutput.add("<span style=\"color:red;\">Invalid number of fields entered.</span> super chUserType [username] [newUserType]");
            return false;
        } else {
            File userCredentialsCSV = new File("UserCredentials.csv");
            CSVReader reader = new CSVReader(new FileReader(userCredentialsCSV));
            List<String[]> myEntries = reader.readAll();

            String enteredUsername = fullCommand[2];
            boolean found = false;

            for (int targetRow = 0; targetRow < myEntries.size(); targetRow++) {
                if (myEntries.get(targetRow)[1].equals(enteredUsername)) {
                    found = true;

                    String newUserType = fullCommand[3];

                    if (newUserType.equals("standard") || newUserType.equals("super")) {
                        myEntries.get(targetRow)[0] = newUserType;
                        CSVWriter writer = new CSVWriter(new FileWriter(userCredentialsCSV));
                        writer.writeAll(myEntries);
                        writer.flush();
                        writer.close();
                    } else {
                        builtInOutput.add("<span style=\"color:red;\">Invalid user-type entered!</span>");
                        return false;
                    }
                }
            }
            if (!found) {
                builtInOutput.add("<span style=\"color:red;\">Username not found in user database</span>");
                return false;
            }
        }
        return true;
    }

    /**
     *
     * Changes an existing users username
     * @param fullCommand The full input parsed into an array
     * @return true if Successful or False if failed
     * @throws IOException Error with input handling
     * @throws CsvException Error with CSV handling
     */
    public static boolean builtInChangeUsername (String [] fullCommand) throws IOException, CsvException {
        if (fullCommand.length != 4) {
            builtInOutput.add("<span style=\"color:red;\">Invalid number of fields entered.</span> super chUsername [oldUsername] [newUsername]");
            return false;
        } else {
            File userCredentialsCSV = new File("UserCredentials.csv");
            CSVReader reader = new CSVReader(new FileReader(userCredentialsCSV));
            List<String[]> myEntries = reader.readAll();

            String oldUsername = fullCommand[2];
            boolean found = false;
            for (int targetRow = 0; targetRow < myEntries.size(); targetRow++) {
                if (myEntries.get(targetRow)[1].equals(oldUsername)) {
                    found = true;
                    String newUsername = fullCommand[3];
                    myEntries.get(targetRow)[1] = newUsername;
                    CSVWriter writer = new CSVWriter(new FileWriter(userCredentialsCSV));
                    writer.writeAll(myEntries);
                    writer.flush();
                    writer.close();
                }
            }
            if(!found) {
                builtInOutput.add("<span style=\"color:red;\">Username not found in user database</span>");
                return false;
            }
        }
        return true;
    }

    /**
     * Copies a file from one directory to another or in the same directory
     * @param fullCommand The full input parsed into an array
     */
    public static void builtInCopy(String[] fullCommand) {
        if (fullCommand.length == 2) {
            File sourceFile = new File(fullCommand[1]);
            String oldFileName = sourceFile.getName();
            File copiedFile = new File(oldFileName + "_copy");
            try {
                FileUtils.copyFile(sourceFile, copiedFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            builtInOutput.add("<span style=\"color:#4d9a06;\">Copied successfully</span>");
        } else if (fullCommand.length == 3)  {
            File sourceFile = new File(fullCommand[1]);
            File destinationDirectory = new File(fullCommand[2]);
            try {
                FileUtils.copyFileToDirectory(sourceFile, destinationDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
            builtInOutput.add("<span style=\"color:#4d9a06;\">Copied successfully</span>");
        } else {
            builtInOutput.add("<span style=\"color:red;\">Invalid number of fields entered.</span> copy [sourceFile] [destinationDirectory]");
        }
    }

    /**
     * Moves a file from one directory to another
     * @param fullCommand The full input parsed into an array
     */
    public static void builtInMove(String[] fullCommand) {
        if (fullCommand.length == 3)  {
            boolean success = false;
            File sourceFile = new File(currentDirectory + "/"+ fullCommand[1]);
            File destinationDirectory = new File(currentDirectory + "/"+ fullCommand[2]);
            try {
                FileUtils.moveFileToDirectory(sourceFile, destinationDirectory , true);
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(success) {
                builtInOutput.add("<span style=\"color:#4d9a06;\">Moved successfully</span>");
            } else {
                builtInOutput.add("<span style=\"color:#4d9a06;\">Error: Move failed. Check file and directory exist.</span>");
            }
        } else {
            builtInOutput.add("<span style=\"color:red;\">Invalid number of fields entered.</span> move [sourceFile] [destinationDirectory]");
        }
    }

    /**
     * Outputs the current users username
     */
    public static void builtInWhoAmI() {
        String currentUser = getCurrentUser();
        if (Objects.equals(currentUser, "")) {
            builtInOutput.add("No-one logged in");
        } else {
            builtInOutput.add(currentUser + " is currently logged in");
        }
    }

    /**
     * Checks if the username exists and then prompts the user with a password input
     * @param fullCommand The full input parsed into an array
     * @throws IOException Error with inout handling
     * @throws CsvException Error with CSV handling
     */
    public static void builtInLogin(String[] fullCommand) throws IOException, CsvException {
        if (fullCommand.length != 2) {
            builtInOutput.add("<span style=\"color:red;\">Invalid number of fields entered.</span> login [username]");
        } else {
            File userCredentials = new File("UserCredentials.csv");
            CSVReader reader = new CSVReader(new FileReader(userCredentials));
            List<String[]> allCredentials = reader.readAll();

            String username = fullCommand[1];

            int targetRow = 0;
            boolean foundFlag = false;
            String correctPassword;

            for (int row=0; row < allCredentials.size(); row++) {
                if (allCredentials.get(row)[1].equals(username)) {
                    targetRow = row;
                    foundFlag = true;
                }
            }

            if (foundFlag) {
                setLoggedInUserType(allCredentials.get(targetRow)[0]);
                setCurrentUser(username);
                builtInOutput.add("<body style = \"background-color: black;" +
                        "color:#63de00\"></body>" +
                        "<p style = \"color:dark-gray\"></p>" +
                        "<form name=\"input\" method=\"post\">"+"Enter Password" + "$ " + "<input " +
                        "style = \"background-color:dark-gray;" +
                        "border-color:white ;" +
                        "margin-right:20px;" +
                        "color:dark-gray;" +
                        "width:300px\"" +
                        " type=\"password\" name=\"pass\"><input type=\"submit\" value=\"Submit\"></form>");


            } else {
                builtInOutput.add("<span style=\"color:red;\">Username not found!</span>");
            }
        }

    }

    /**
     * Swithces the active user account
     * @param switchLoggedIn The new user to switch too
     */
    public static void setLoggedIn(boolean switchLoggedIn) {
        loggedIn = switchLoggedIn;
    }

    /**
     * Finds out if their is a user activley logged in
     * @return True if there is a user and False if there is not
     */
    public static boolean getLoggedIn() {
        return loggedIn;
    }

    /**
     * Set the username of the logged-in user
     * @param currentUsername The username to set it too
     */
    public static void setCurrentUser(String currentUsername) {
        loggedInUsername = currentUsername;
    }

    /**
     * Get the username of the active user
     * @return The username
     */
    public static String getCurrentUser() {
        return loggedInUsername;
    }

    /**
     * Set the type of the logged-in user
     * @param currentUserType The users type
     */
    public static void setLoggedInUserType(String currentUserType) {
        loggedInUserType = currentUserType;
    }

    /**
     * Get the type of the active user
     * @return The users type
     */
    public static String getLoggedInUserType() {
        return loggedInUserType;
    }

    /**
     * Logs out the current user
     * @throws IOException Error with input handling
     * @throws CsvException Error with CSV handling
     */
    public static void builtInLogoff() throws IOException, CsvException {
        setCurrentUser("");
        setLoggedInUserType("");
        setLoggedIn(false);
    }

    /**
     * Shows the current working directory
     */
    public static void builtInShowDir() {
        builtInOutput.add(currentDirectory);
    }

    /**
     * Gets the list of what needs to be output to the web server
     * @return The list of items to be output
     */
    public List<String> getBuiltInOutput() {
        return builtInOutput;
    }

    /**
     * Clears the list of items to be output
     */
    public static void clearBuiltInOutput() {
        builtInOutput.clear();
    }

    /**
     * Gets the current working directory
     * @return The current working directory
     */
    public static String getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * Validates if the password inputted from the password prompt matches the password stored for that user
     * @param password The password entered by the user
     * @return The output to display on the webserver
     * @throws IOException Error with input handling
     * @throws CsvException Error with CSV handling
     */
    public static List<String> validPassword(String password) throws IOException, CsvException {
        clearBuiltInOutput();
        File userCredentials = new File("UserCredentials.csv");
        CSVReader reader = new CSVReader(new FileReader(userCredentials));
        List<String[]> allCredentials = reader.readAll();

        int targetRow = 0;
        for (int row=0; row < allCredentials.size(); row++) {
            if (allCredentials.get(row)[1].equals(loggedInUsername)) {
                targetRow = row;
            }
        }

        String correctPassword = allCredentials.get(targetRow)[2];
        if (password.equals(correctPassword)) {
            builtInOutput.add("<span style=\"color:#4d9a06;\">Login Successful</span>");
            setLoggedIn(true);
        } else {
            builtInOutput.add("<span style=\"color:red;\">Password verification unsuccessful!</span>");
            loggedInUsername = "";
            loggedInUserType = "";
        }
        return builtInOutput;
    }

    /**
     * Changes the current working directory
     * @param fullCommand The parsed command
     */
    void builtInChangeDirectory(String[] fullCommand) {
        if (fullCommand.length != 2) {
            builtInOutput.add("<span style=\"color:red;\">Invalid number of fields entered.</span> cd [targetDirectory][..]");
        } else {
            File filePath = new File(currentDirectory);
            String[] directoryList = filePath.list();
            try {
                if (fullCommand[1].equals("..")) {
                    String previousDirectory = currentDirectory.substring(currentDirectory.lastIndexOf("/"));
                    currentDirectory = currentDirectory.substring(0, currentDirectory.indexOf(previousDirectory));
                } else {
                    if (Arrays.toString(directoryList).contains(fullCommand[1])) {
                        if (!fullCommand[1].contains("/")) {
                            fullCommand[1] = "/" + fullCommand[1];
                        }
                        currentDirectory += fullCommand[1];
                    } else {
                        builtInOutput.add("<span style=\"color:red;\">Directory does not exist</span>");
                    }
                }
            } catch (Exception e) {
                builtInOutput.add("<span style=\"color:red;\">An error occurred - Try again</span>");
            }
        }
    }
}
