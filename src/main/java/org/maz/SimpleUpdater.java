package org.maz;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;


public class SimpleUpdater {

    /**
     * Reads data represented by the URL e.g. a website and searches for an arbitrary HTML element with the given
     * id. E.g. &#x3c;div id="version"&#x3e;1.0.4&#x3c;/div&#x3e;
     *
     * @return the inner HTML text of this element, which should represent a version string.
     */
    public static String getVersionText(URL url, String elementId) throws IOException, ElementNotFoundException {
        URLConnection connection = url.openConnection();
        String encoding = connection.getContentEncoding();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding != null ? encoding : "UTF-8"))) {
            Optional<String> versionLine = br.lines().filter(line -> line.matches(".*<.*id=\"" + elementId + "\".*>[.\\d]+</.*>")).findFirst();
            if (versionLine.isPresent()) {
                return versionLine.get().strip().split("<|>")[2];
            } else
                throw new ElementNotFoundException("Element with id '" + elementId + "' not found in '" + url + "'.");
        } catch (IOException exception) {
            throw new IOException("Could not open URL '" + url + "'.");
        }
    }

    /**
     * Compares two version strings in the form of "number[.number.number...]"
     *
     * @return true if remoteVersionString is higher.
     */
    public static boolean isRemoteVersionNumberHigher(String thisVersion, String remoteVersion) throws ParseException {
        if (!thisVersion.matches("(\\d?\\.?)+\\d+"))
            throw new ParseException("(This) version string '" + thisVersion + "' does not match criteria 'number[.number.number...]'", 0);
        if (!remoteVersion.matches("(\\d?\\.?)+\\d+"))
            throw new ParseException("(Remote) version string '" + remoteVersion + "' does not match criteria 'number[.number.number...]'", 0);
        ArrayList<String> thisVers = new ArrayList<>(Arrays.asList(thisVersion.split("\\.")));
        ArrayList<String> remoteVers = new ArrayList<>(Arrays.asList(remoteVersion.split("\\.")));
        // Make list of version tokens the same size for save comparison.
        int maxLength = Math.max(thisVers.size(), remoteVers.size());
        for (int i = thisVers.size(); i < maxLength; i++) {
            thisVers.add("0");
        }
        for (int i = remoteVers.size(); i < maxLength; i++) {
            remoteVers.add("0");
        }
        for (int i = 0; i < maxLength; i++) {
            if (Integer.valueOf(remoteVers.get(i)) > Integer.valueOf(thisVers.get(i)))
                return true;
            else if (Integer.valueOf(remoteVers.get(i)) < Integer.valueOf(thisVers.get(i)))
                return false;
        }
        return false;
    }


    public static void updateAndRestart(Path currentVersion, Path newVersion) {
        // Prepare .bat for restarting.

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("restart.bat"));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(SimpleUpdater.class.getClassLoader().getResourceAsStream("restartTemplate.bat"), StandardCharsets.UTF_8));
            for (String line : bufferedReader.lines().toList()) {
                bufferedWriter.append(line.replace("$CURRENT_VERSION", currentVersion.toFile().getName().replace("$NEW_VERSION", newVersion.toFile().getName())));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        System.out.println("x");
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/C", restartBat.toFile().getName());
        //processBuilder.directory(new File("C:\\data\\temp"));
        try {
            processBuilder.inheritIO();
            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
