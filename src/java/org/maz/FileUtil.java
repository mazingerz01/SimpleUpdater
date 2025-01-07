package org.maz;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil {
    /**
     * After downloading and extracting the new version contained in a temporary directory, start this method and exit your application.
     * The current version directory will be deleted and the temporary directory will be renamed to the name of the latter.
     *
     * @param newVersion The temporary directory containing all files of the new version incl. an executable.
     * @param executable The new version executable to be started after updating. Null if start is not required/wanted.
     * @param backup     If true, a backup directory "SimpleUpdaterBackup_yyyyMMddHHmmssSSS" will be created. The content
     *                   of the current directory (original program) will be copied there after the original program was closed.
     */
    static void updateAndMaybeRestart(File newVersion, File executable, boolean backup) throws IOException {
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

    private static List<String> createBackupLines(File backupDir, Supplier<Stream<File>> filesToDelete) {
        ArrayList<String> ret = new ArrayList<String>();
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
