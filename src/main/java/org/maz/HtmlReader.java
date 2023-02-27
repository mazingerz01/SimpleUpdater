package org.maz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class HtmlReader {

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
}
