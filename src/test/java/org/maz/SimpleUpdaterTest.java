package org.maz;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

class SimpleUpdaterTest {
    @Test
    void getVersionText() {
        try {
            Assertions.assertEquals("1.0.4", SimpleUpdater.getRemoteVersionText(new URL("http://diskstation/lockunlock/version.html"), "version"));
        } catch (IOException | ElementNotFoundException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    @Test
    void isRemoteVersionNumberHigher() {
        try {
            Assertions.assertTrue(SimpleUpdater.compareVersions("1.0", "2.0"));
            Assertions.assertTrue(SimpleUpdater.compareVersions("1.0", "2"));
            Assertions.assertTrue(SimpleUpdater.compareVersions("0.0.01", "0.04.01"));
            Assertions.assertTrue(SimpleUpdater.compareVersions("0.001", "0.04.01.3"));
            Assertions.assertFalse(SimpleUpdater.compareVersions("0.01.0.0", "0.0.03"));
            Assertions.assertThrows(ParseException.class, () -> SimpleUpdater.compareVersions("abc", "0.1"));
            Assertions.assertThrows(ParseException.class, () -> SimpleUpdater.compareVersions("0.1", "foo"));
        } catch (ParseException e) {
            System.out.println(e);
        }
    }

    @Test
    void updateAndRestart() {
        // Be careful when developing. Will move your whole project files to a backup directory.
        try {
            SimpleUpdater.updateAndMaybeRestart(new File("test"), new File("test.exe"), false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
