package org.maz;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

class HtmlReaderTest {
    @Test
    void getVersionText() {
        try {
            Assertions.assertEquals("1.0.4", HtmlReader.getVersionText(new URL("http://diskstation/lockunlock/version.html"), "version"));
        } catch (IOException | ElementNotFoundException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    @Test
    void isRemoteVersionNumberHigher() {
        try {
            Assertions.assertTrue(HtmlReader.isRemoteVersionNumberHigher("1.0", "2.0"));
            Assertions.assertTrue(HtmlReader.isRemoteVersionNumberHigher("1.0", "2"));
            Assertions.assertTrue(HtmlReader.isRemoteVersionNumberHigher("0.0.01", "0.04.01"));
            Assertions.assertTrue(HtmlReader.isRemoteVersionNumberHigher("0.001", "0.04.01.3"));
            Assertions.assertFalse(HtmlReader.isRemoteVersionNumberHigher("0.01.0.0", "0.0.03"));
            Assertions.assertThrows(ParseException.class, () -> HtmlReader.isRemoteVersionNumberHigher("abc", "0.1"));
            Assertions.assertThrows(ParseException.class, () -> HtmlReader.isRemoteVersionNumberHigher("0.1", "foo"));
        } catch (ParseException e) {
            System.out.println(e);
        }
    }
}
