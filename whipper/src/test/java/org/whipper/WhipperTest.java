package org.whipper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.whipper.Whipper;

public class WhipperTest {


    @Test public void getPlaceHolderTestSimple1(){ containsString(Whipper.getPlaceHolders("${e.f}"), "e.f"); }
    @Test public void getPlaceHolderTestSimple2(){ containsString(Whipper.getPlaceHolders("aa${e.f}"), "e.f"); }
    @Test public void getPlaceHolderTestSimple3(){ containsString(Whipper.getPlaceHolders("${e.f}bb"), "e.f"); }
    @Test public void getPlaceHolderTestSimple4(){ containsString(Whipper.getPlaceHolders("aa${e.f}bb"), "e.f"); }

    @Test public void getPlaceHolderTestEmpty1(){ containsString(Whipper.getPlaceHolders("${}${e.f}"), "e.f"); }
    @Test public void getPlaceHolderTestEmpty2(){ containsString(Whipper.getPlaceHolders("${}aa${e.f}"), "e.f"); }
    @Test public void getPlaceHolderTestEmpty3(){ containsString(Whipper.getPlaceHolders("${}${e.f}bb"), "e.f"); }
    @Test public void getPlaceHolderTestEmpty4(){ containsString(Whipper.getPlaceHolders("${}aa${e.f}bb"), "e.f"); }

    @Test public void getPlaceHolderTestEmpty5(){ containsString(Whipper.getPlaceHolders("${e.f}${}"), "e.f"); }
    @Test public void getPlaceHolderTestEmpty6(){ containsString(Whipper.getPlaceHolders("aa${e.f}${}"), "e.f"); }
    @Test public void getPlaceHolderTestEmpty7(){ containsString(Whipper.getPlaceHolders("${e.f}bb${}"), "e.f"); }
    @Test public void getPlaceHolderTestEmpty8(){ containsString(Whipper.getPlaceHolders("aa${e.f}bb${}"), "e.f"); }

    @Test public void getPlaceHolderTestMultiple1(){ containsString(Whipper.getPlaceHolders("${e.f}${aaa}"), "e.f", "aaa"); }
    @Test public void getPlaceHolderTestMultiple2(){ containsString(Whipper.getPlaceHolders("aa${e.f}${bbb}"), "e.f", "bbb"); }
    @Test public void getPlaceHolderTestMultiple3(){ containsString(Whipper.getPlaceHolders("${e.f}bb${ccc}"), "e.f", "ccc"); }
    @Test public void getPlaceHolderTestMultiple4(){ containsString(Whipper.getPlaceHolders("aa${e.f}bb${ddd}"), "e.f", "ddd"); }

    @Test public void getPlaceHolderTestNoPH1(){ containsString(Whipper.getPlaceHolders("")); }
    @Test public void getPlaceHolderTestNoPH2(){ containsString(Whipper.getPlaceHolders("${}")); }
    @Test public void getPlaceHolderTestNoPH3(){ containsString(Whipper.getPlaceHolders("${}")); }
    @Test public void getPlaceHolderTestNoPH4(){ containsString(Whipper.getPlaceHolders("${}bb")); }
    @Test public void getPlaceHolderTestNoPH5(){ containsString(Whipper.getPlaceHolders("aa${}bb")); }

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

    @Test public void containsPlaceHolderTestNoPH1(){ Assert.assertFalse("There is no placeholder.", Whipper.containsPlaceHolder("")); }
    @Test public void containsPlaceHolderTestNoPH2(){ Assert.assertFalse("There is no placeholder.", Whipper.containsPlaceHolder("")); }
    @Test public void containsPlaceHolderTestNoPH3(){ Assert.assertFalse("There is no placeholder.", Whipper.containsPlaceHolder("${}")); }
    @Test public void containsPlaceHolderTestNoPH4(){ Assert.assertFalse("There is no placeholder.", Whipper.containsPlaceHolder("${")); }
    @Test public void containsPlaceHolderTestNoPH5(){ Assert.assertFalse("There is no placeholder.", Whipper.containsPlaceHolder("}")); }
    @Test public void containsPlaceHolderTestNoPH6(){ Assert.assertFalse("There is no placeholder.", Whipper.containsPlaceHolder("aaa${")); }
    @Test public void containsPlaceHolderTestNoPH7(){ Assert.assertFalse("There is no placeholder.", Whipper.containsPlaceHolder("${aaa")); }
    @Test public void containsPlaceHolderTestNoPH8(){ Assert.assertFalse("There is no placeholder.", Whipper.containsPlaceHolder("bbb}")); }
    @Test public void containsPlaceHolderTestNoPH9(){ Assert.assertFalse("There is no placeholder.", Whipper.containsPlaceHolder("aaa${bbb")); }
    @Test public void containsPlaceHolderTestNoPH10(){ Assert.assertFalse("There is no placeholder.", Whipper.containsPlaceHolder("aaa}bbb")); }

    @Test public void containsPlaceHolderTestPH1(){ Assert.assertTrue("There are placeholders.", Whipper.containsPlaceHolder("${bbb ccc}")); }
    @Test public void containsPlaceHolderTestPH2(){ Assert.assertTrue("There are placeholders.", Whipper.containsPlaceHolder("${bbb}")); }
    @Test public void containsPlaceHolderTestPH3(){ Assert.assertTrue("There are placeholders.", Whipper.containsPlaceHolder("aaa${bbb}")); }
    @Test public void containsPlaceHolderTestPH4(){ Assert.assertTrue("There are placeholders.", Whipper.containsPlaceHolder("${bbb}aaa")); }
    @Test public void containsPlaceHolderTestPH5(){ Assert.assertTrue("There are placeholders.", Whipper.containsPlaceHolder("aaa${bbb}bbb")); }

    @Test public void removeExtensionTestNoExtension(){ Assert.assertEquals(Whipper.removeExtension("file"), "file"); }
    @Test public void removeExtensionTestSimple(){ Assert.assertEquals(Whipper.removeExtension("file.txt"), "file"); }
    @Test public void removeExtensionTestWithDot(){ Assert.assertEquals(Whipper.removeExtension("file.1.txt"), "file.1"); }
    @Test public void removeExtensionTestHiddenNoExtension(){ Assert.assertEquals(Whipper.removeExtension(".file"), ".file"); }
    @Test public void removeExtensionTestHiddenSimple(){ Assert.assertEquals(Whipper.removeExtension(".file.txt"), ".file"); }
    @Test public void removeExtensionTestHiddenWithDot(){ Assert.assertEquals(Whipper.removeExtension(".file.1.txt"), ".file.1"); }

    private void checkResolved(KeyValue[] in, KeyValue[] out){
        Properties inP = new Properties();
        for(KeyValue kv : in){
            inP.put(kv.key, kv.value);
        }
        Properties resolved = Whipper.resolvePlaceHolders(inP);
        Assert.assertEquals("", out.length, resolved.size());
        for(KeyValue kv : out){
            Assert.assertTrue(kv.key + " is not in resolved properties.", resolved.containsKey(kv.key));
            Assert.assertEquals("Value of resolved property " + kv.key, kv.value, resolved.getProperty(kv.key));
        }
    }

    private void containsString(Collection<String> l1, String... l2){
        List<String> act = new ArrayList<>(l1);
        List<String> exp = new ArrayList<>(Arrays.asList(l2));
        Collections.sort(exp);
        Collections.sort(act);
        Assert.assertEquals(exp, act);
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
