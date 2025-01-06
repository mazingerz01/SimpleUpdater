package org.maz;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class ZipDownloadUtil {

    // Method to download a ZIP file from a URL and extract it to a directory
    static void downloadAndExtractZip(String fileUrl, String outputDir) throws IOException {
        // Create output directory if it doesn't exist
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Step 1: Download the ZIP file from the URL
        URL url = new URL(fileUrl);
        try (InputStream in = url.openStream();
             FileOutputStream out = new FileOutputStream(outputDir + "/downloaded.zip")) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        // Step 2: Extract the downloaded ZIP file
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(outputDir + "/downloaded.zip"))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outputFile = new File(outputDir, entry.getName());
                // Create directories if the entry is a directory
                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    // Extract the file
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

        System.out.println("Download and extraction completed!");
    }

}
