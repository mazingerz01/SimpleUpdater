/*
 * Copyright (c) 2023.
 * M. Maz.
 * File/Directory movement/deletion - use at own risk!
 */

package org.maz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SimpleUpdater {

    /**
     * Reads data represented by URL e.g. a website and searches for an arbitrary HTML element with the given
     * id. E.g. &#x3c;div id="version"&#x3e;1.0.4&#x3c;/div&#x3e;
     *
     * @return the inner HTML text of this element, which should represent a version string.
     */
    public static String getRemoteVersionText(URL url, String elementId) throws IOException, NoSuchElementException {
        URLConnection connection = url.openConnection();
        String encoding = connection.getContentEncoding();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding != null ? encoding : "UTF-8"))) {
            Optional<String> versionLine = br.lines().filter(line -> line.matches(".*<.*id=\"" + elementId + "\".*>[.\\d]+</.*>")).findFirst();
            if (versionLine.isPresent()) {
                return versionLine.get().strip().split("[<>]")[2];
            } else {
                throw new NoSuchElementException("Element with id '" + elementId + "' not found in '" + url + "'.");
            }
        } catch (IOException exception) {
            throw new IOException("Could not open URL '" + url + "'.");
        }
    }

    /**
     * Compares two version strings in the form of "number[.number[.number.[...]]]".
     * E.g. "1.0.9" is a higher version than "0.9.3.2" bc. of "1." > "0.".
     *
     * @return true if versionB is a higher version than versionA. Equal versions return false.
     */
    public static boolean isVersionHigher(String versionA, String versionB) throws ParseException {
        if (!versionA.matches("(\\d?\\.?)+\\d+")) {
            throw new ParseException("(This) version string '" + versionA + "' does not match criteria 'number[.number.number...]'", 0);
        }
        if (!versionB.matches("(\\d?\\.?)+\\d+")) {
            throw new ParseException("(Remote) version string '" + versionB + "' does not match criteria 'number[.number.number...]'", 0);
        }
        ArrayList<String> thisVers = new ArrayList<>(Arrays.asList(versionA.split("\\.")));
        ArrayList<String> remoteVers = new ArrayList<>(Arrays.asList(versionB.split("\\.")));
        // Make list of version tokens the same size for save comparison.
        int maxLength = Math.max(thisVers.size(), remoteVers.size());
        for (int i = thisVers.size(); i < maxLength; i++) {
            thisVers.add("0");
        }
        for (int i = remoteVers.size(); i < maxLength; i++) {
            remoteVers.add("0");
        }
        for (int i = 0; i < maxLength; i++) {
            if (Integer.parseInt(remoteVers.get(i)) > Integer.parseInt(thisVers.get(i))) {
                return true;
            } else if (Integer.parseInt(remoteVers.get(i)) < Integer.parseInt(thisVers.get(i))) {
                return false;
            }
        }
        return false;
    }

    public static void downloadAndExtract(String fileUrl, String outputDir) throws IOException {
        ZipDownloadUtil.downloadAndExtractZip(new URL(fileUrl), outputDir);
    }


// TODO
//  - find last backup dir for cleanup and check, create cleanup method (remove backup)


}
