package org.maz;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class ZipDownloadUtil {

    // Method to download a ZIP file from a URL and extract it to a directory
    static void downloadAndExtractZip(URL fileUrl, String outputDir) throws IOException {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Could not create output directory: " + outputDir);
            }
        }

        // Download the ZIP file
        try (InputStream in = fileUrl.openStream();
             FileOutputStream out = new FileOutputStream(outputDir + "/downloaded.zip")) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        // Extract the downloaded ZIP file
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(outputDir + "/downloaded.zip"))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outputFile = new File(outputDir, entry.getName());
                // Create directories or files according to entry
                if (entry.isDirectory()) {
                    if (!outputFile.mkdirs()) {
                        throw new IOException("Could not create directory like in zip file: " + outputFile.getName());
                    }
                } else {
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

}
