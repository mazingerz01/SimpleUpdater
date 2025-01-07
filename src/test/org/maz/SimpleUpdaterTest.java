package org.maz;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.NoSuchElementException;

class SimpleUpdaterTest {
    @Test
    void getVersionText() {
        try {
            Assertions.assertEquals("1.0.4", SimpleUpdater.getRemoteVersionText(new URL("http://diskstation/lockunlock/version.html"), "version"));
        } catch (IOException | NoSuchElementException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    @Test
    void isRemoteVersionNumberHigher() {
        try {
            Assertions.assertTrue(SimpleUpdater.isVersionHigher("1.0", "2.0"));
            Assertions.assertTrue(SimpleUpdater.isVersionHigher("1.0", "2"));
            Assertions.assertTrue(SimpleUpdater.isVersionHigher("0.0.01", "0.04.01"));
            Assertions.assertTrue(SimpleUpdater.isVersionHigher("0.001", "0.04.01.3"));
            Assertions.assertFalse(SimpleUpdater.isVersionHigher("0.01.0.0", "0.0.03"));
            Assertions.assertThrows(ParseException.class, () -> SimpleUpdater.isVersionHigher("abc", "0.1"));
            Assertions.assertThrows(ParseException.class, () -> SimpleUpdater.isVersionHigher("0.1", "foo"));
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
    }
}
