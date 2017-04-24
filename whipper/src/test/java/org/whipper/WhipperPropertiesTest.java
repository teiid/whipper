package org.whipper;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WhipperPropertiesTest{

    @Test
    public void getPlaceHolderTest(){
        Assertions.assertAll("Resolved placeholders.", () -> containsString(WhipperProperties.getPlaceHolders("${e.f}"), "e.f"),
                () -> containsString(WhipperProperties.getPlaceHolders("aa${e.f}"), "e.f"),
                () -> containsString(WhipperProperties.getPlaceHolders("${e.f}bb"), "e.f"),
                () -> containsString(WhipperProperties.getPlaceHolders("aa${e.f}bb"), "e.f"),
                () -> containsString(WhipperProperties.getPlaceHolders("${}${e.f}"), "e.f"),
                () -> containsString(WhipperProperties.getPlaceHolders("${}aa${e.f}"), "e.f"),
                () -> containsString(WhipperProperties.getPlaceHolders("${}${e.f}bb"), "e.f"),
                () -> containsString(WhipperProperties.getPlaceHolders("${}aa${e.f}bb"), "e.f"),
                () -> containsString(WhipperProperties.getPlaceHolders("${e.f}${}"), "e.f"),
                () -> containsString(WhipperProperties.getPlaceHolders("aa${e.f}${}"), "e.f"),
                () -> containsString(WhipperProperties.getPlaceHolders("${e.f}bb${}"), "e.f"),
                () -> containsString(WhipperProperties.getPlaceHolders("aa${e.f}bb${}"), "e.f"),
                () -> containsString(WhipperProperties.getPlaceHolders("${e.f}${aaa}"), "e.f", "aaa"),
                () -> containsString(WhipperProperties.getPlaceHolders("aa${e.f}${bbb}"), "e.f", "bbb"),
                () -> containsString(WhipperProperties.getPlaceHolders("${e.f}bb${ccc}"), "e.f", "ccc"),
                () -> containsString(WhipperProperties.getPlaceHolders("aa${e.f}bb${ddd}"), "e.f", "ddd"),
                () -> containsString(WhipperProperties.getPlaceHolders("")),
                () -> containsString(WhipperProperties.getPlaceHolders("${}")),
                () -> containsString(WhipperProperties.getPlaceHolders("${}")),
                () -> containsString(WhipperProperties.getPlaceHolders("${}bb")),
                () -> containsString(WhipperProperties.getPlaceHolders("aa${}bb")));
    }

    @Test
    public void containsPlaceHolderTest(){
        Assertions.assertAll("There is no placeholder.", () -> Assertions.assertFalse(WhipperProperties.containsPlaceHolder("")),
                () -> Assertions.assertFalse(WhipperProperties.containsPlaceHolder("")),
                () -> Assertions.assertFalse(WhipperProperties.containsPlaceHolder("${}")),
                () -> Assertions.assertFalse(WhipperProperties.containsPlaceHolder("${")),
                () -> Assertions.assertFalse(WhipperProperties.containsPlaceHolder("}")),
                () -> Assertions.assertFalse(WhipperProperties.containsPlaceHolder("aaa${")),
                () -> Assertions.assertFalse(WhipperProperties.containsPlaceHolder("${aaa")),
                () -> Assertions.assertFalse(WhipperProperties.containsPlaceHolder("bbb}")),
                () -> Assertions.assertFalse(WhipperProperties.containsPlaceHolder("aaa${bbb")),
                () -> Assertions.assertFalse(WhipperProperties.containsPlaceHolder("aaa}bbb")));
        Assertions.assertAll("There are placeholders.", () -> Assertions.assertTrue(WhipperProperties.containsPlaceHolder("${bbb ccc}")),
                () -> Assertions.assertTrue(WhipperProperties.containsPlaceHolder("${bbb}")),
                () -> Assertions.assertTrue(WhipperProperties.containsPlaceHolder("aaa${bbb}")),
                () -> Assertions.assertTrue(WhipperProperties.containsPlaceHolder("${bbb}aaa")),
                () -> Assertions.assertTrue(WhipperProperties.containsPlaceHolder("aaa${bbb}bbb")));
    }

    @Test
    public void resolvePlaceHoldersTestNoPH1(){
        Assertions.assertAll(() -> checkResolved(new KeyValue[]{new KeyValue("xx", "bbb"), new KeyValue("aaa", "bbb"), new KeyValue("ddd", "eee")},
                new KeyValue[]{new KeyValue("xx", "bbb"), new KeyValue("aaa", "bbb"), new KeyValue("ddd", "eee")}),
                () -> checkResolved(new KeyValue[]{new KeyValue("yy", "aaa}"), new KeyValue("aaa", "${ddd"), new KeyValue("ddd", "${}")},
                        new KeyValue[]{new KeyValue("yy", "aaa}"), new KeyValue("aaa", "${ddd"), new KeyValue("ddd", "${}")}),
                () -> checkResolved(new KeyValue[]{new KeyValue("aaa", "bbb"), new KeyValue("ddd", "${aaa}"), new KeyValue("eee", "${aa}")},
                        new KeyValue[]{new KeyValue("aaa", "bbb"), new KeyValue("ddd", "bbb"), new KeyValue("eee", "${aa}")}),
                () -> checkResolved(new KeyValue[]{new KeyValue("aaa", "${eee}"), new KeyValue("ddd", "${aaa}"), new KeyValue("eee", "aa")},
                        new KeyValue[]{new KeyValue("aaa", "aa"), new KeyValue("ddd", "aa"), new KeyValue("eee", "aa")}),
                () -> checkResolved(new KeyValue[]{new KeyValue("aaa", "eee"), new KeyValue("ddd", "${${aaa}}"), new KeyValue("eee", "aa")},
                        new KeyValue[]{new KeyValue("aaa", "eee"), new KeyValue("ddd", "aa"), new KeyValue("eee", "aa")}),
                () -> checkResolved(new KeyValue[]{new KeyValue("aaa", "eee"), new KeyValue("ddd", "${bb.${aaa}}"), new KeyValue("bb.eee", "aa")},
                        new KeyValue[]{new KeyValue("aaa", "eee"), new KeyValue("ddd", "aa"), new KeyValue("bb.eee", "aa")}),
                () -> checkResolved(new KeyValue[]{new KeyValue("aaa", "${eee}"), new KeyValue("ddd", "${aaa}"), new KeyValue("eee", "${5}")},
                        new KeyValue[]{new KeyValue("aaa", "${5}"), new KeyValue("ddd", "${5}"), new KeyValue("eee", "${5}")}),
                () -> checkResolved(new KeyValue[]{new KeyValue("aaa", "${eee}...${bbb}"), new KeyValue("bbb", "5"), new KeyValue("ee", "10")},
                        new KeyValue[]{new KeyValue("aaa", "${eee}...5"), new KeyValue("bbb", "5"), new KeyValue("ee", "10")}),
                () -> checkResolved(new KeyValue[]{new KeyValue("aaa", "abcd.efg_${eee}blabla://${ddd.1}"), new KeyValue("ddd.1", "55"), new KeyValue("eee", "5")},
                        new KeyValue[]{new KeyValue("aaa", "abcd.efg_5blabla://55"), new KeyValue("ddd.1", "55"), new KeyValue("eee", "5")}),
                () -> Assertions.assertThrows(IllegalArgumentException.class, () ->
                        Assertions.assertTimeout(Duration.ofMillis(100),
                                () -> checkResolved(new KeyValue[]{new KeyValue("aaa", "${eee}...${bbb}"), new KeyValue("bbb", "${ccc}"), new KeyValue("ccc", "${aaa}"), new KeyValue("eee", "5")}, null))),
                () -> Assertions.assertThrows(IllegalArgumentException.class, () ->
                        Assertions.assertTimeout(Duration.ofMillis(100),
                                () -> checkResolved(new KeyValue[]{new KeyValue("aaa", "${eee}...${bbb}"), new KeyValue("bbb", "${ccc}"), new KeyValue("ccc", "${aaa}")}, null))),
                () -> Assertions.assertThrows(IllegalArgumentException.class, () ->
                        Assertions.assertTimeout(Duration.ofMillis(100),
                                () -> checkResolved(new KeyValue[]{new KeyValue("aaa", "${aaa}")}, null))));
    }

    @Test
    public void connPropsTest(){
        WhipperProperties p = new WhipperProperties();
        p.setProperty("aaa", "bbb");
        p.setAdditionalConnectionProperty("abc", "cba");
        p.setProperty("bbb", "aaa");
        p.setProperty(WhipperProperties.Keys.CONNECTION_PROPERTY_PREFIX + "bbb", "aba");
        p.setProperty("ccc", "ddd");
        Assertions.assertEquals(5, p.size(), "Number of stored properties.");
        Properties acp = p.getAdditionalConnectionProperties();
        Assertions.assertTrue(acp.containsKey("abc"), "Connection property 'abc' is missing.");
        Assertions.assertTrue(acp.containsKey("bbb"), "Connection property 'bbb' is missing.");
        Assertions.assertEquals("cba", acp.get("abc"), "Connection property abc");
        Assertions.assertEquals("aba", acp.get("bbb"), "Connection property bbb");
    }

    @Test
    public void copyFromPropsTest(){
        WhipperProperties p = new WhipperProperties();
        p.setProperty("aaa", "bbb");
        Properties pp = new Properties();
        pp.setProperty("a", "b");
        pp.setProperty("b", "a");
        p.copyFrom(pp);
        Assertions.assertAll(() -> Assertions.assertEquals(2, p.size(), "Number of stored properties."),
                () -> Assertions.assertEquals("b", p.getProperty("a"), "Property a"),
                () -> Assertions.assertEquals("a", p.getProperty("b"), "Property b"));
    }

    @Test
    public void copyFromWhipperPropsTest(){
        WhipperProperties p = new WhipperProperties();
        p.setProperty("aaa", "bbb");
        WhipperProperties pp = new WhipperProperties();
        pp.setProperty("a", "b");
        pp.setProperty("b", "a");
        p.copyFrom(pp);
        Assertions.assertAll(() -> Assertions.assertEquals(2, p.size(), "Number of stored properties."),
                () -> Assertions.assertEquals("b", p.getProperty("a"), "Property a"),
                () -> Assertions.assertEquals("a", p.getProperty("b"), "Property b"));
    }

    @Test
    public void addAllPropsTest(){
        WhipperProperties p = new WhipperProperties();
        p.setProperty("aaa", "bbb");
        Properties pp = new Properties();
        pp.setProperty("a", "b");
        pp.setProperty("b", "a");
        p.addAll(pp);
        Assertions.assertAll(() -> Assertions.assertEquals(3, p.size(), "Number of stored properties."),
                () -> Assertions.assertEquals("b", p.getProperty("a"), "Property a"),
                () -> Assertions.assertEquals("a", p.getProperty("b"), "Property b"),
                () -> Assertions.assertEquals("bbb", p.getProperty("aaa"), "Property aaa"));
    }

    @Test
    public void addAllWhipperPropsTest(){
        WhipperProperties p = new WhipperProperties();
        p.setProperty("aaa", "bbb");
        WhipperProperties pp = new WhipperProperties();
        pp.setProperty("a", "b");
        pp.setProperty("b", "a");
        p.addAll(pp);
        Assertions.assertAll(() -> Assertions.assertEquals(3, p.size(), "Number of stored properties."),
                () -> Assertions.assertEquals("b", p.getProperty("a"), "Property a"),
                () -> Assertions.assertEquals("a", p.getProperty("b"), "Property b"),
                () -> Assertions.assertEquals("bbb", p.getProperty("aaa"), "Property aaa"));
    }

    @Test
    public void getFastFailTest(){
        WhipperProperties p = new WhipperProperties();
        Assertions.assertTrue(p.getQuerySetFastFail(), "Default is true.");
        p.setQuerySetFastFail("true");
        Assertions.assertTrue(p.getQuerySetFastFail(), "Default is true.");
        p.setQuerySetFastFail("false");
        Assertions.assertFalse(p.getQuerySetFastFail(), "Set to false.");
        p.setQuerySetFastFail("tru");
        Assertions.assertFalse(p.getQuerySetFastFail(), "Invalid.");
    }

    @Test
    public void getAllowedDivergenceTest(){
        WhipperProperties p = new WhipperProperties();
        Assertions.assertNull(p.getAllowedDivergence(), "Default is null.");
        p.setAllowedDivergence("1");
        Assertions.assertNotNull(p.getAllowedDivergence(), "Set to 1.");
        Assertions.assertEquals(0, BigDecimal.ONE.compareTo(p.getAllowedDivergence()), "Set to 1.");
        p.setAllowedDivergence("a");
        Assertions.assertNull(p.getAllowedDivergence(), "Invalid.");
    }

    @Test
    public void getOutputDirTest(){
        WhipperProperties p = new WhipperProperties();
        Assertions.assertNull(p.getOutputDir(), "Default is null.");
        p.setOutputDir("a");
        Assertions.assertNotNull(p.getOutputDir(), "Set to 'a'.");
    }

    @Test
    public void getArtifactsDirTest(){
        WhipperProperties p = new WhipperProperties();
        Assertions.assertNull(p.getArtifactsDir(), "Default is null.");
        p.setArtifactsDir("a");
        Assertions.assertNotNull(p.getArtifactsDir(), "Set to 'a'.");
    }

    @Test
    public void getIncludeScenarioTest(){
        WhipperProperties p = new WhipperProperties();
        Assertions.assertSame(WhipperProperties.ALL_PATTERN, p.getIncludeScenario(), "Default is all.");
        p.setIncludeScenario("a");
        Assertions.assertNotNull(p.getIncludeScenario(), "Set to 'a'.");
    }

    @Test
    public void getExcludeScenarioTest(){
        WhipperProperties p = new WhipperProperties();
        Assertions.assertSame(WhipperProperties.NOTHING_PATTERN, p.getExcludeScenario(), "Default is nothing.");
        p.setExcludeScenario("a");
        Assertions.assertNotNull(p.getExcludeScenario(), "Set to 'a'.");
    }

    @Test
    public void getOneQueryTimeTest(){
        WhipperProperties p = new WhipperProperties();
        Assertions.assertEquals(-1L, p.getTimeForOneQuery(), "Default is -1.");
        p.setTimeForOneQuery("1");
        Assertions.assertEquals(1L, p.getTimeForOneQuery(), "Set to 1.");
        p.setTimeForOneQuery("a");
        Assertions.assertEquals(-1L, p.getTimeForOneQuery(), "Invalid.");
    }

    @Test
    public void getPropertyTest(){
        WhipperProperties p = new WhipperProperties();

        p.setProperty("number", "1");
        p.setProperty("empty", "");
        p.setProperty("boolean", "true");

        Assertions.assertAll(() -> Assertions.assertEquals("1", p.getProperty("number", String.class, "0"), "String1"),
                () -> Assertions.assertEquals("", p.getProperty("empty", String.class, "0"), "String2"),
                () -> Assertions.assertTrue(p.getProperty("boolean", Boolean.class, false), "Boolean"),
                () -> Assertions.assertEquals(1, (byte)p.getProperty("number", Byte.class, (byte)0), "Byte"),
                () -> Assertions.assertEquals(1, (short)p.getProperty("number", Short.class, (short)0), "Short"),
                () -> Assertions.assertEquals(1, (int)p.getProperty("number", Integer.class, 0), "Integer"),
                () -> Assertions.assertEquals(1L, (long)p.getProperty("number", Long.class, 0l), "Long"),
                () -> Assertions.assertEquals(1.0f, (float)p.getProperty("number", Float.class, 0.0f), "Float"),
                () -> Assertions.assertEquals(1.0, (double)p.getProperty("number", Double.class, 0.0), "Double"),
                () -> Assertions.assertEquals(new File("1"), p.getProperty("number", File.class, new File("0")), "File"),
                () -> Assertions.assertEquals(new BigDecimal("1"), p.getProperty("number", BigDecimal.class, BigDecimal.ZERO), "BigDecimal"),
                () -> Assertions.assertEquals(new BigInteger("1"), p.getProperty("number", BigInteger.class, new BigInteger("0")), "BigInteger"),
                () -> Assertions.assertEquals("^(1)$", p.getProperty("number", Pattern.class, Pattern.compile("0")).pattern(), "Pattern"));
    }

    @Test
    public void getPropertyDefaultTest(){
        WhipperProperties p = new WhipperProperties();

        p.setProperty("intMax", String.valueOf(Integer.MAX_VALUE));

        Assertions.assertAll(() -> Assertions.assertEquals(0, (byte)p.getProperty("intMax", Byte.class, (byte)0), "int to byte"),
                () -> Assertions.assertEquals("0", p.getProperty("", String.class, "0"), "non-existent property"));
    }

    private void containsString(Collection<String> l1, String... l2){
        List<String> act = new ArrayList<>(l1);
        List<String> exp = new ArrayList<>(Arrays.asList(l2));
        Collections.sort(exp);
        Collections.sort(act);
        Assertions.assertEquals(exp, act);
    }

    private void checkResolved(KeyValue[] in, KeyValue[] out){
        WhipperProperties inWP = new WhipperProperties();
        for(KeyValue kv : in){
            inWP.setProperty(kv.key, kv.value);
        }
        inWP.resolvePlaceholders();
        Assertions.assertEquals(out.length, inWP.size(), "Number of resolved properties.");
        for(KeyValue kv : out){
            Assertions.assertNotNull(inWP.getProperty(kv.key), kv.key + " is not in resolved properties.");
            Assertions.assertEquals(kv.value, inWP.getProperty(kv.key), "Value of resolved property " + kv.key);
        }
    }

    private class KeyValue{
        private String key;
        private String value;

        private KeyValue(String key, String value){
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString(){
            return key + "=" + value;
        }
    }
}
