package com.mycompany.ssproject;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpsServer;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import com.sun.net.httpserver.*;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 *
 * Session class runs the secure web server and controls input and output from it.
 *
 * @author Umar Ali
 * @author James Maguire
 *
 */
public class Session {

    /**
     * Handles inputs and outputs to and from the HTTPS server.
     */
    public static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if("GET".equals(t.getRequestMethod()))
            {
                String response = "<body style = \"background-color: black; font-family:Ubuntu; font-weight: 600; color:white\">" +
                        "<form name=\"input\" method=\"post\"><span style=\"color:#3365a3;\">"+BuiltInCommands.getCurrentDirectory() +"/</span>" + "$ " + "<input " +
                        "style = \"background-color:dark-gray;" +
                        "border-color:white;" +
                        "margin-right:20px;" +
                        "color:dark-gray;" +
                        "width:300px\"" +
                        " type=\"text\" name=\"user\"><input type=\"submit\" value=\"Submit\"></form>";
                t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                t.getResponseHeaders().add("Content-type", "text/html");
                t.sendResponseHeaders(200, response.getBytes().length);
                try ( OutputStream os = t.getResponseBody()) {
                    os.write(response.getBytes());
                }}
            if("POST".equals(t.getRequestMethod()))
            { Scanner s = new Scanner(t.getRequestBody());
                String message = s.nextLine();
                String[] output = new String[0];
                String response;
                BuiltInCommands.clearBuiltInOutput();
                String check = message.substring(0,4);
                List<String> responseList = null;

                if (check.equals("user")) {
                    message = message.substring(5);
                    message = message.replace("%2F", "/");
                    String[] fullCommand = message.split("\\+");
                    try {
                        responseList = Session.ExternalOrBuiltIn(fullCommand);
                    } catch (InterruptedException | CsvException e) {
                        e.printStackTrace();
                    }
                }   else {
                    String password = message.substring(5);
                    try {
                        responseList = BuiltInCommands.validPassword(password);
                    } catch (CsvException e) {
                        e.printStackTrace();
                    }
                }

                if(!BuiltInCommands.getLoggedIn()) {
                    response = "<body style = \"background-color: black; font-family:Ubuntu; font-weight: 600; color:white\">" +
                            "<form name=\"input\" method=\"post\"><span style=\"color:#3365a3;\">"+ BuiltInCommands.getCurrentDirectory() +"/</span>" + "$ " + "<input " +
                            "style = \"background-color:dark-gray;" +
                            "border-color:white;" +
                            "margin-right:20px;" +
                            "color:dark-gray;" +
                            "width:300px\"" +
                            " type=\"text\" name=\"user\"><input type=\"submit\" value=\"Submit\"></form>";
                } else {
                    response = "<body style = \"background-color: black; font-family:Ubuntu; font-weight: 600; color:white\">" +
                            "<form name=\"input\" method=\"post\"><span style=\"color:#FCAE1E;\">"+BuiltInCommands.getCurrentUser()+"</span> : <span style=\"color:#3365a3;\">"+BuiltInCommands.getCurrentDirectory() +"/</span>" + "$ " + "<input " +
                            "style = \"background-color:dark-gray;" +
                            "border-color:white;" +
                            "margin-right:20px;" +
                            "color:dark-gray;" +
                            "width:300px\"" +
                            " type=\"text\" name=\"user\"><input type=\"submit\" value=\"Submit\"></form>";
                }

                    for (int i = 0; i < responseList.size(); i++) {
                        response = response + "<p style = \"color:white\">" + responseList.get(i) + "</p>";
                    }

                    t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    t.getResponseHeaders().add("Content-type", "text/html");
                    t.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = t.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    BuiltInCommands.builtInShowDir();
                }
            }
        }

    /**
     *
     * Starts the secure web server and hosts on port 2222.
     * @param args Any input arguments.
     * @throws java.io.IOException Error with input handling
     * @throws java.lang.InterruptedException Error with threading
     *
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        String pass = "pedro123";
        int port = 2222;
        String credentialsFilePath="comp20081.jks";

        try {
            // setup the socket address
            InetSocketAddress address = new InetSocketAddress(port);

            // initialise the HTTPS server
            HttpsServer httpsServer = HttpsServer.create(address, 0);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            char[] password = pass.toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream(credentialsFilePath);
            ks.load(fis, password);

            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            // setup the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            // setup the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                @Override
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext c = getSSLContext();
                        SSLEngine engine = c.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        // Set the SSL parameters
                        SSLParameters sslParameters = c.getSupportedSSLParameters();
                        params.setSSLParameters(sslParameters);

                    } catch (Exception ex) {
                        System.out.println("Failed to create HTTPS port");
                        System.out.println(ex.getMessage());
                    }
                }
            });
            httpsServer.createContext("/", new MyHandler());
            httpsServer.setExecutor(null); // creates a default executor
            System.out.println("HTTPS server on port 2222");
            httpsServer.start();

        } catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException ex) {
            System.out.println("Failed to create HTTPS server on port 2222");
            System.out.println("Please ensure the \"comp20081.jks\" file and the \"UserCredentials.csv\" are in the same directory that you run the .jar file from.");
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Checks to see if the entered command exists and then calls the correct method from the ExternalCommands or BuiltInCommands class.
     * @param fullCommand The fully parsed command.
     * @return Returns the HTML output in a list for the server.
     * @throws IOException Error with input handling
     * @throws InterruptedException Error with threading
     * @throws CsvException Error with CSV handling
     */
    public static List<String> ExternalOrBuiltIn(String[] fullCommand) throws CsvException, InterruptedException, IOException {
        String[] externalCommands = {"ls", "cp", "mv", "mkdir", "rmdir", "pwd", "ps", "which"};
        String[] builtInCommands = {"super", "addUser", "delUser", "chPass", "chUserType", "chUsername", "copy", "move", "whoami", "login", "logoff","showDir", "cd", "help"};

        boolean containsExternal = Arrays.stream(externalCommands).anyMatch(Array.get(fullCommand, 0)::equals);
        boolean containsBuiltIn = Arrays.stream(builtInCommands).anyMatch(Array.get(fullCommand, 0)::equals);

        if (containsExternal && BuiltInCommands.getLoggedIn()) {
            ExternalCommands NewCommand = new ExternalCommands();
            return NewCommand.ExternalCommand(fullCommand);
        } else if (containsBuiltIn) {
            BuiltInCommands NewCommand = new BuiltInCommands();
            NewCommand.determineCommand(fullCommand);
            return NewCommand.getBuiltInOutput();
        } else if (!BuiltInCommands.getLoggedIn()){
            List<String> error = new ArrayList<String>();
            error.add("<span style=\"color:red;\">You must be logged in to execute commands.</span>");
            error.add("Please enter \"login [username]\"");
            return error;
        }
        else {
            List<String> error = new ArrayList<String>();
            error.add("<span style=\"color:red;\">Error: Command " + Array.get(fullCommand, 0) + " was not found.</span>");
            error.add("Type \"help\" for a list of commands.");
            return error;
        }
    }
}
