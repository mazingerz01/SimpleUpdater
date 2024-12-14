/*
 * Copyright (c) 2023.
 * M. Maz.
 * File/Directory movement/deletion - use at own risk!
 */

package org.maz;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleUpdater {

    /**
     * Reads data represented by URL e.g. a website and searches for an arbitrary HTML element with the given
     * id. E.g. &#x3c;div id="version"&#x3e;1.0.4&#x3c;/div&#x3e;
     *
     * @return the inner HTML text of this element, which should represent a version string.
     */
    public static String getRemoteVersionText(URL url, String elementId) throws IOException, ElementNotFoundException {
        URLConnection connection = url.openConnection();
        String encoding = connection.getContentEncoding();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding != null ? encoding : "UTF-8"))) {
            Optional<String> versionLine = br.lines().filter(line -> line.matches(".*<.*id=\"" + elementId + "\".*>[.\\d]+</.*>")).findFirst();
            if (versionLine.isPresent()) {
                return versionLine.get().strip().split("<|>")[2];
            } else {
                throw new ElementNotFoundException("Element with id '" + elementId + "' not found in '" + url + "'.");
            }
        } catch (IOException exception) {
            throw new IOException("Could not open URL '" + url + "'.");
        }
    }

    /**
     * Compares two version strings in the form of "number[.number[.number.[...]]]".
     * E.g. "1.0.9" is a higher version than "0.9.3.2" bc. of "1." > "0.".
     *
     * @return true if versionB is a higher version than versionA. Equal versions will result false as result.
     */
    public static boolean compareVersions(String versionA, String versionB) throws ParseException {
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
// TODO
//  - Simple Download and extract newVersion as directory in zip file
//  - find last backup dir for cleanup and check, create cleanup method (remove backup)


    /**
     * After downloading and extracting the new version contained in a temporary directory, start this method and exit your application.
     * The current version directory will be deleted and the temporary directory will be renamed to the name of the latter.
     *
     * @param newVersion The temporary directory containing all files of the new version incl. an executable.
     * @param executable The new version executable to be started after updating. Null if start is not required.
     * @param backup     If true, a backup directory "SimpleUpdaterBackup_yyyyMMddHHmmssSSS" will be created. The content
     *                   of the current directory will be copied there after the original program was closed.
     */
    public static void updateAndMaybeRestart(File newVersion, File executable, boolean backup) throws IOException {
        boolean restart = executable != null;
        if (!(newVersion.exists() && newVersion.isDirectory()))
            throw new IOException("Provided paths are not existent or not a directory.");
        if (restart && !(executable.exists() && executable.isFile() && executable.canExecute())) {
            throw new IOException("Provided executable is not existent or not a file.");
        }
        if (restart && !executable.canExecute()) {
            throw new IOException("Provided executable is not executable.");
        }

        final File rootDir = new File(System.getProperty("user.dir")); // current working directory
        if (rootDir.listFiles() == null) {
            throw new IOException("Current directory (" + rootDir + ") is empty. Expected: actual running program and new version.");
        }

        // Create controlling batch file
        final File batFile = new File("simpleUpdater.bat");
        BufferedWriter batFileBufferedWriter = new BufferedWriter(new FileWriter(batFile.getAbsolutePath()));

        // Create backup directory
        File backupDir = null;
        if (backup) {
            backupDir = new File("SimpleUpdaterBackup" + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
            if (!backupDir.mkdir()) {
                throw new IOException("Could not create backup directory: " + backupDir.getAbsolutePath());
            }
        }

        Supplier<Stream<File>> oldVersionFiles = () -> Arrays.stream(Objects.requireNonNull(rootDir.listFiles())).filter(
            file -> (!file.getName().equals(newVersion.getName())) && !file.getName().equals(batFile.getName()));

        // Fill controlling bat file
        writeWithNewLine(batFileBufferedWriter, "powershell -noprofile -command \"& {[system.threading.thread]::sleep(5000)}\"");
        if (backup) {
            for (String s : createBackupLines(backupDir, oldVersionFiles)) {
                writeWithNewLine(batFileBufferedWriter, s);
            }
        }
        writeWithNewLine(batFileBufferedWriter, "del /F /Q " + oldVersionFiles.get().map(File::getName).collect(Collectors.joining(" ")));
        writeWithNewLine(batFileBufferedWriter, "xcopy \"" + newVersion.getPath() + "\" \\*.* .\\ /E /Y /Q");
        writeWithNewLine(batFileBufferedWriter, "rmdir /S /Q \"" + newVersion.getPath() + "\"");
        if (restart) {
            writeWithNewLine(batFileBufferedWriter, "cmd /c start \"\" /I /MIN \"" + executable.getName() + "\"");
        }
        writeWithNewLine(batFileBufferedWriter, "del /F /Q simpleUpdater.bat");
        batFileBufferedWriter.close();

        // Execute controlling batch file
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/C", batFile.getName());
        //processBuilder.directory(new File(""));
        processBuilder.inheritIO();
        processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        //processBuilder.start();xxxm

    }

    private static List<String> createBackupLines(File backupDir, Supplier<Stream<File>> filesToDelete) throws IOException {
        ArrayList<String> ret = new ArrayList<>();
        filesToDelete.get().forEach(file -> {
            if (file.isFile()) {
                ret.add("xcopy " + file.getName() + " " + backupDir.getName() + " /Y /Q");
            } else if (file.isDirectory()) {
                ret.add("xcopy " + file.getName() + " " + backupDir.getName() + "\\" + file.getName() + " /Y /Q /E /I");
            }
        });
        return ret;
    }

    private static void writeWithNewLine(BufferedWriter bufferedWriter, String s) throws IOException {
        bufferedWriter.write(s);
        bufferedWriter.newLine();
    }

}
