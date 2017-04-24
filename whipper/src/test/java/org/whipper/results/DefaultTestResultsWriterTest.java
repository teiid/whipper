package org.whipper.results;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultTestResultsWriterTest{

    @Test
    public void padTest(){
        Assertions.assertAll(() -> Assertions.assertEquals(DefaultTestResultsWriter.pad("file", 10), "file      "),
                () -> Assertions.assertEquals(DefaultTestResultsWriter.pad("file  ", 10), "file      "),
                () -> Assertions.assertEquals(DefaultTestResultsWriter.pad("file   ", 10), "file      "),
                () -> Assertions.assertEquals(DefaultTestResultsWriter.pad("   file", 10), "   file   "),
                () -> Assertions.assertEquals(DefaultTestResultsWriter.pad("file", 4), "file"),
                () -> Assertions.assertEquals(DefaultTestResultsWriter.pad("file", 3), "file"),
                () -> Assertions.assertEquals(DefaultTestResultsWriter.pad("  file", 4), "  file"),
                () -> Assertions.assertEquals(DefaultTestResultsWriter.pad("  file", 6), "  file"));
    }

    @Test
    public void timeToStringTest(){
        Assertions.assertAll(() -> Assertions.assertEquals(DefaultTestResultsWriter.timeToString(0), "00:00:00.000"),
                () -> Assertions.assertEquals(DefaultTestResultsWriter.timeToString(10), "00:00:00.010"),
                () -> Assertions.assertEquals(DefaultTestResultsWriter.timeToString(1000), "00:00:01.000"),
                () -> Assertions.assertEquals(DefaultTestResultsWriter.timeToString(60000), "00:01:00.000"),
                () -> Assertions.assertEquals(DefaultTestResultsWriter.timeToString(61000), "00:01:01.000"),
                () -> Assertions.assertEquals(DefaultTestResultsWriter.timeToString(23 * 3600000 + 59 * 60000 + 59 * 1000 + 999), "23:59:59.999"),
                () -> Assertions.assertEquals(DefaultTestResultsWriter.timeToString(26 * 3600000 + 57 * 60000 + 4 * 1000 + 532), "26:57:04.532"),
                () -> Assertions.assertEquals(DefaultTestResultsWriter.timeToString(-1), "-1"));
    }
}
