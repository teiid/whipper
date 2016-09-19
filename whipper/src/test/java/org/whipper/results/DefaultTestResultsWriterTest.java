package org.whipper.results;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultTestResultsWriterTest{

    private DefaultTestResultsWriter dtrw;

    @Before
    public void create(){
        dtrw = new DefaultTestResultsWriter();
    }

    @After
    public void drop(){
        dtrw = null;
    }

    @Test public void padTestSimple(){ Assert.assertEquals(dtrw.pad("file", 10), "file      "); }
    @Test public void padTestSpacesAtEnd1(){ Assert.assertEquals(dtrw.pad("file  ", 10), "file      "); }
    @Test public void padTestSpacesAtEnd2(){ Assert.assertEquals(dtrw.pad("file   ", 10), "file      "); }
    @Test public void padTestSpacesAtStart(){ Assert.assertEquals(dtrw.pad("   file", 10), "   file   "); }
    @Test public void padTestSameSize(){ Assert.assertEquals(dtrw.pad("file", 4), "file"); }
    @Test public void padTestShorter(){ Assert.assertEquals(dtrw.pad("file", 3), "file"); }
    @Test public void padTestShorterSpacesAtStart(){ Assert.assertEquals(dtrw.pad("  file", 4), "  file"); }
    @Test public void padTestSmaeSizeSpacesAtStart(){ Assert.assertEquals(dtrw.pad("  file", 6), "  file"); }

    @Test public void timeToStringTestZero(){ Assert.assertEquals(dtrw.timeToString(0), "00:00:00.000"); }
    @Test public void timeToStringTest10ms(){ Assert.assertEquals(dtrw.timeToString(10), "00:00:00.010"); }
    @Test public void timeToStringTest1s(){ Assert.assertEquals(dtrw.timeToString(1000), "00:00:01.000"); }
    @Test public void timeToStringTest1m(){ Assert.assertEquals(dtrw.timeToString(60000), "00:01:00.000"); }
    @Test public void timeToStringTest1m1s(){ Assert.assertEquals(dtrw.timeToString(61000), "00:01:01.000"); }
    @Test public void timeToStringTest1msToMidnight(){ Assert.assertEquals(dtrw.timeToString(23*3600000 + 59*60000 + 59*1000 + 999), "23:59:59.999"); }
    @Test public void timeToStringTest26hPlus(){ Assert.assertEquals(dtrw.timeToString(26*3600000 + 57*60000 + 4*1000 + 532), "26:57:04.532"); }
    @Test public void timeToStringTestNegativeTime(){ Assert.assertEquals(dtrw.timeToString(-1), "-1"); }
}
