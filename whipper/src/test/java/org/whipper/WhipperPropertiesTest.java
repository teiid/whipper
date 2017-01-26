package org.whipper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public class WhipperPropertiesTest{

    @Test public void getPlaceHolderTestSimple1(){ containsString(WhipperProperties.getPlaceHolders("${e.f}"), "e.f"); }
    @Test public void getPlaceHolderTestSimple2(){ containsString(WhipperProperties.getPlaceHolders("aa${e.f}"), "e.f"); }
    @Test public void getPlaceHolderTestSimple3(){ containsString(WhipperProperties.getPlaceHolders("${e.f}bb"), "e.f"); }
    @Test public void getPlaceHolderTestSimple4(){ containsString(WhipperProperties.getPlaceHolders("aa${e.f}bb"), "e.f"); }

    @Test public void getPlaceHolderTestEmpty1(){ containsString(WhipperProperties.getPlaceHolders("${}${e.f}"), "e.f"); }
    @Test public void getPlaceHolderTestEmpty2(){ containsString(WhipperProperties.getPlaceHolders("${}aa${e.f}"), "e.f"); }
    @Test public void getPlaceHolderTestEmpty3(){ containsString(WhipperProperties.getPlaceHolders("${}${e.f}bb"), "e.f"); }
    @Test public void getPlaceHolderTestEmpty4(){ containsString(WhipperProperties.getPlaceHolders("${}aa${e.f}bb"), "e.f"); }

    @Test public void getPlaceHolderTestEmpty5(){ containsString(WhipperProperties.getPlaceHolders("${e.f}${}"), "e.f"); }
    @Test public void getPlaceHolderTestEmpty6(){ containsString(WhipperProperties.getPlaceHolders("aa${e.f}${}"), "e.f"); }
    @Test public void getPlaceHolderTestEmpty7(){ containsString(WhipperProperties.getPlaceHolders("${e.f}bb${}"), "e.f"); }
    @Test public void getPlaceHolderTestEmpty8(){ containsString(WhipperProperties.getPlaceHolders("aa${e.f}bb${}"), "e.f"); }

    @Test public void getPlaceHolderTestMultiple1(){ containsString(WhipperProperties.getPlaceHolders("${e.f}${aaa}"), "e.f", "aaa"); }
    @Test public void getPlaceHolderTestMultiple2(){ containsString(WhipperProperties.getPlaceHolders("aa${e.f}${bbb}"), "e.f", "bbb"); }
    @Test public void getPlaceHolderTestMultiple3(){ containsString(WhipperProperties.getPlaceHolders("${e.f}bb${ccc}"), "e.f", "ccc"); }
    @Test public void getPlaceHolderTestMultiple4(){ containsString(WhipperProperties.getPlaceHolders("aa${e.f}bb${ddd}"), "e.f", "ddd"); }

    @Test public void getPlaceHolderTestNoPH1(){ containsString(WhipperProperties.getPlaceHolders("")); }
    @Test public void getPlaceHolderTestNoPH2(){ containsString(WhipperProperties.getPlaceHolders("${}")); }
    @Test public void getPlaceHolderTestNoPH3(){ containsString(WhipperProperties.getPlaceHolders("${}")); }
    @Test public void getPlaceHolderTestNoPH4(){ containsString(WhipperProperties.getPlaceHolders("${}bb")); }
    @Test public void getPlaceHolderTestNoPH5(){ containsString(WhipperProperties.getPlaceHolders("aa${}bb")); }

    @Test public void containsPlaceHolderTestNoPH1(){ Assert.assertFalse("There is no placeholder.", WhipperProperties.containsPlaceHolder("")); }
    @Test public void containsPlaceHolderTestNoPH2(){ Assert.assertFalse("There is no placeholder.", WhipperProperties.containsPlaceHolder("")); }
    @Test public void containsPlaceHolderTestNoPH3(){ Assert.assertFalse("There is no placeholder.", WhipperProperties.containsPlaceHolder("${}")); }
    @Test public void containsPlaceHolderTestNoPH4(){ Assert.assertFalse("There is no placeholder.", WhipperProperties.containsPlaceHolder("${")); }
    @Test public void containsPlaceHolderTestNoPH5(){ Assert.assertFalse("There is no placeholder.", WhipperProperties.containsPlaceHolder("}")); }
    @Test public void containsPlaceHolderTestNoPH6(){ Assert.assertFalse("There is no placeholder.", WhipperProperties.containsPlaceHolder("aaa${")); }
    @Test public void containsPlaceHolderTestNoPH7(){ Assert.assertFalse("There is no placeholder.", WhipperProperties.containsPlaceHolder("${aaa")); }
    @Test public void containsPlaceHolderTestNoPH8(){ Assert.assertFalse("There is no placeholder.", WhipperProperties.containsPlaceHolder("bbb}")); }
    @Test public void containsPlaceHolderTestNoPH9(){ Assert.assertFalse("There is no placeholder.", WhipperProperties.containsPlaceHolder("aaa${bbb")); }
    @Test public void containsPlaceHolderTestNoPH10(){ Assert.assertFalse("There is no placeholder.", WhipperProperties.containsPlaceHolder("aaa}bbb")); }

    @Test public void containsPlaceHolderTestPH1(){ Assert.assertTrue("There are placeholders.", WhipperProperties.containsPlaceHolder("${bbb ccc}")); }
    @Test public void containsPlaceHolderTestPH2(){ Assert.assertTrue("There are placeholders.", WhipperProperties.containsPlaceHolder("${bbb}")); }
    @Test public void containsPlaceHolderTestPH3(){ Assert.assertTrue("There are placeholders.", WhipperProperties.containsPlaceHolder("aaa${bbb}")); }
    @Test public void containsPlaceHolderTestPH4(){ Assert.assertTrue("There are placeholders.", WhipperProperties.containsPlaceHolder("${bbb}aaa")); }
    @Test public void containsPlaceHolderTestPH5(){ Assert.assertTrue("There are placeholders.", WhipperProperties.containsPlaceHolder("aaa${bbb}bbb")); }

    @Test
    public void resolvePlaceHoldersTestNoPH1(){
        checkResolved(new KeyValue[]{new KeyValue("xx", "bbb"), new KeyValue("aaa", "bbb"), new KeyValue("ddd", "eee")},
                new KeyValue[]{new KeyValue("xx", "bbb"), new KeyValue("aaa", "bbb"), new KeyValue("ddd", "eee")});
    }

    @Test
    public void resolvePlaceHoldersTestNoPH2(){
        checkResolved(new KeyValue[]{new KeyValue("yy", "aaa}"), new KeyValue("aaa", "${ddd"), new KeyValue("ddd", "${}")},
                new KeyValue[]{new KeyValue("yy", "aaa}"), new KeyValue("aaa", "${ddd"), new KeyValue("ddd", "${}")});
    }

    @Test
    public void resolvePlaceHoldersTestSimplePHOneNotResolved(){
        checkResolved(new KeyValue[]{new KeyValue("aaa", "bbb"), new KeyValue("ddd", "${aaa}"), new KeyValue("eee", "${aa}")},
                new KeyValue[]{new KeyValue("aaa", "bbb"), new KeyValue("ddd", "bbb"), new KeyValue("eee", "${aa}")});
    }

    @Test
    public void resolvePlaceHoldersTestNestedPH(){
        checkResolved(new KeyValue[]{new KeyValue("aaa", "${eee}"), new KeyValue("ddd", "${aaa}"), new KeyValue("eee", "aa")},
                new KeyValue[]{new KeyValue("aaa", "aa"), new KeyValue("ddd", "aa"), new KeyValue("eee", "aa")});
    }

    @Test
    public void resolvePlaceHoldersTestPHAsValueofPH1(){
        checkResolved(new KeyValue[]{new KeyValue("aaa", "eee"), new KeyValue("ddd", "${${aaa}}"), new KeyValue("eee", "aa")},
                new KeyValue[]{new KeyValue("aaa", "eee"), new KeyValue("ddd", "aa"), new KeyValue("eee", "aa")});
    }

    @Test
    public void resolvePlaceHoldersTestPHAsValueofPH2(){
        checkResolved(new KeyValue[]{new KeyValue("aaa", "eee"), new KeyValue("ddd", "${bb.${aaa}}"), new KeyValue("bb.eee", "aa")},
                new KeyValue[]{new KeyValue("aaa", "eee"), new KeyValue("ddd", "aa"), new KeyValue("bb.eee", "aa")});
    }

    @Test
    public void resolvePlaceHoldersTestNestedPHNotResolved(){
        checkResolved(new KeyValue[]{new KeyValue("aaa", "${eee}"), new KeyValue("ddd", "${aaa}"), new KeyValue("eee", "${5}")},
                new KeyValue[]{new KeyValue("aaa", "${5}"), new KeyValue("ddd", "${5}"), new KeyValue("eee", "${5}")});
    }

    @Test
    public void resolvePlaceHoldersTestMultiPHOneUnresolved(){
        checkResolved(new KeyValue[]{new KeyValue("aaa", "${eee}...${bbb}"), new KeyValue("bbb", "5"), new KeyValue("ee", "10")},
                new KeyValue[]{new KeyValue("aaa", "${eee}...5"), new KeyValue("bbb", "5"), new KeyValue("ee", "10")});
    }

    @Test
    public void resolvePlaceHoldersTestMultiPHResolved(){
        checkResolved(new KeyValue[]{new KeyValue("aaa", "abcd.efg_${eee}blabla://${ddd.1}"), new KeyValue("ddd.1", "55"), new KeyValue("eee", "5")},
                new KeyValue[]{new KeyValue("aaa", "abcd.efg_5blabla://55"), new KeyValue("ddd.1", "55"), new KeyValue("eee", "5")});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 100)
    public void resolvePlaceHoldersTestNestedCyclic1(){
        checkResolved(new KeyValue[]{new KeyValue("aaa", "${eee}...${bbb}"), new KeyValue("bbb", "${ccc}"), new KeyValue("ccc", "${aaa}"), new KeyValue("eee", "5")}, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 100)
    public void resolvePlaceHoldersTestNestedCyclic2(){
        checkResolved(new KeyValue[]{new KeyValue("aaa", "${eee}...${bbb}"), new KeyValue("bbb", "${ccc}"), new KeyValue("ccc", "${aaa}")}, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 100)
    public void resolvePlaceHoldersTestNestedCyclicDirect(){
        checkResolved(new KeyValue[]{new KeyValue("aaa", "${aaa}")}, null);
    }

    @Test
    public void connPropsTest(){
        WhipperProperties p = new WhipperProperties();
        p.setProperty("aaa", "bbb");
        p.setAdditionalConnectionProperty("abc", "cba");
        p.setProperty("bbb", "aaa");
        p.setProperty(WhipperProperties.Keys.CONNECTION_PROPERTY_PREFIX + "bbb", "aba");
        p.setProperty("ccc", "ddd");
        Assert.assertEquals("Number of stored properties.", 5, p.size());
        Properties acp = p.getAdditionalConnectionProperties();
        Assert.assertTrue("Connection property 'abc' is missing.", acp.containsKey("abc"));
        Assert.assertTrue("Connection property 'bbb' is missing.", acp.containsKey("bbb"));
        Assert.assertEquals("Connection property abc", "cba", acp.get("abc"));
        Assert.assertEquals("Connection propery bbb", "aba", acp.get("bbb"));
    }

    @Test
    public void copyFromTestProps(){
        WhipperProperties p = new WhipperProperties();
        p.setProperty("aaa", "bbb");
        Properties pp = new Properties();
        pp.setProperty("a", "b");
        pp.setProperty("b", "a");
        p.copyFrom(pp);
        Assert.assertEquals("Number of stored properties.", 2, p.size());
        Assert.assertEquals("Property a", "b", p.getProperty("a"));
        Assert.assertEquals("Property b", "a", p.getProperty("b"));
    }

    @Test
    public void copyFromTestWhipperProps(){
        WhipperProperties p = new WhipperProperties();
        p.setProperty("aaa", "bbb");
        WhipperProperties pp = new WhipperProperties();
        pp.setProperty("a", "b");
        pp.setProperty("b", "a");
        p.copyFrom(pp);
        Assert.assertEquals("Number of stored properties.", 2, p.size());
        Assert.assertEquals("Property a", "b", p.getProperty("a"));
        Assert.assertEquals("Property b", "a", p.getProperty("b"));
    }

    @Test
    public void addAllTestProps(){
        WhipperProperties p = new WhipperProperties();
        p.setProperty("aaa", "bbb");
        Properties pp = new Properties();
        pp.setProperty("a", "b");
        pp.setProperty("b", "a");
        p.addAll(pp);
        Assert.assertEquals("Number of stored properties.", 3, p.size());
        Assert.assertEquals("Property a", "b", p.getProperty("a"));
        Assert.assertEquals("Property b", "a", p.getProperty("b"));
        Assert.assertEquals("Property aaa", "bbb", p.getProperty("aaa"));
    }

    @Test
    public void addAllTestWhipperProps(){
        WhipperProperties p = new WhipperProperties();
        p.setProperty("aaa", "bbb");
        WhipperProperties pp = new WhipperProperties();
        pp.setProperty("a", "b");
        pp.setProperty("b", "a");
        p.addAll(pp);
        Assert.assertEquals("Number of stored properties.", 3, p.size());
        Assert.assertEquals("Property a", "b", p.getProperty("a"));
        Assert.assertEquals("Property b", "a", p.getProperty("b"));
        Assert.assertEquals("Property aaa", "bbb", p.getProperty("aaa"));
    }

    @Test
    public void getFastFailTest(){
        WhipperProperties p = new WhipperProperties();
        Assert.assertTrue("Default is true.", p.getQuerySetFastFail());
        p.setQuerySetFastFail("true");
        Assert.assertTrue("Default is true.", p.getQuerySetFastFail());
        p.setQuerySetFastFail("false");
        Assert.assertFalse("Set to false.", p.getQuerySetFastFail());
        p.setQuerySetFastFail("tru");
        Assert.assertFalse("Unparsable.", p.getQuerySetFastFail());
    }

    @Test
    public void getAllowedDivergenceTest(){
        WhipperProperties p = new WhipperProperties();
        Assert.assertNull("Default is null.", p.getAllowedDivergence());
        p.setAllowedDivergence("1");
        Assert.assertNotNull("Set to 1.", p.getAllowedDivergence());
        Assert.assertEquals("Set to 1.", 0, BigDecimal.ONE.compareTo(p.getAllowedDivergence()));
        p.setAllowedDivergence("a");
        Assert.assertNull("Unparsable.", p.getAllowedDivergence());
    }

    @Test
    public void getOutputDirTest(){
        WhipperProperties p = new WhipperProperties();
        Assert.assertNull("Default is null.", p.getOutputDir());
        p.setOutputDir("a");
        Assert.assertNotNull("Set to 'a'.", p.getOutputDir());
    }

    @Test
    public void getArtifactsDirTest(){
        WhipperProperties p = new WhipperProperties();
        Assert.assertNull("Default is null.", p.getArtifacstDir());
        p.setArtifacstDir("a");
        Assert.assertNotNull("Set to 'a'.", p.getArtifacstDir());
    }

    @Test
    public void getIncludeScenarioTest(){
        WhipperProperties p = new WhipperProperties();
        Assert.assertNull("Default is null.", p.getIncludeScenario());
        p.setIncludeScenario("a");
        Assert.assertNotNull("Set to 'a'.", p.getIncludeScenario());
    }

    @Test
    public void getExcludeScenarioTest(){
        WhipperProperties p = new WhipperProperties();
        Assert.assertNull("Default is null.", p.getExcludeScenario());
        p.setExcludeScenario("a");
        Assert.assertNotNull("Set to 'a'.", p.getExcludeScenario());
    }

    @Test
    public void getOneQueryTimeTest(){
        WhipperProperties p = new WhipperProperties();
        Assert.assertEquals("Default is -1.", -1l, p.getTimeForOneQuery());
        p.setTimeForOneQuery("1");
        Assert.assertEquals("Set to 1.", 1l, p.getTimeForOneQuery());
        p.setTimeForOneQuery("a");
        Assert.assertEquals("Unparsable.", -1l, p.getTimeForOneQuery());
    }

    private void containsString(Collection<String> l1, String... l2){
        List<String> act = new ArrayList<>(l1);
        List<String> exp = new ArrayList<>(Arrays.asList(l2));
        Collections.sort(exp);
        Collections.sort(act);
        Assert.assertEquals(exp, act);
    }

    private void checkResolved(KeyValue[] in, KeyValue[] out){
        WhipperProperties inWP = new WhipperProperties();
        for(KeyValue kv : in){
            inWP.setProperty(kv.key, kv.value);
        }
        inWP.resolvePlaceholders();
        Assert.assertEquals("Number of resolved properties.", out.length, inWP.size());
        for(KeyValue kv : out){
            Assert.assertNotNull(kv.key + " is not in resolved properties.", inWP.getProperty(kv.key));
            Assert.assertEquals("Value of resolved property " + kv.key, kv.value, inWP.getProperty(kv.key));
        }
    }

    private class KeyValue{
        private String key;
        private String value;

        public KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
}
