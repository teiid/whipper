package org.jboss.bqt;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.Assert;
import org.junit.Test;

public class XmlHelperTest {

    @Test public void getWholeTextTestSimple1(){ Assert.assertEquals(XmlHelper.getWholeText(Jsoup.parse("<tag1> this Is MY   tExT  </tag>", "", Parser.xmlParser())), " this Is MY   tExT  "); }
    @Test public void getWholeTextTestSimple2(){ Assert.assertEquals(XmlHelper.getWholeText(Jsoup.parse("<tag1>   aaa bb</tag>", "", Parser.xmlParser())), "   aaa bb"); }
    @Test public void getWholeTextTestSimple3(){ Assert.assertEquals(XmlHelper.getWholeText(Jsoup.parse("<tag1>aaa bb  </tag>", "", Parser.xmlParser())), "aaa bb  "); }
    @Test public void getWholeTextTestSimple4(){ Assert.assertEquals(XmlHelper.getWholeText(Jsoup.parse("<tag1>aaa  bb</tag>", "", Parser.xmlParser())), "aaa  bb"); }
    @Test public void getWholeTextTestSimple5(){ Assert.assertEquals(XmlHelper.getWholeText(Jsoup.parse("<tag1> aaa  bb </tag>", "", Parser.xmlParser())), " aaa  bb "); }
    @Test public void getWholeTextTestInnerTag1(){ Assert.assertEquals(XmlHelper.getWholeText(Jsoup.parse("<tag1>  <script> aaa</script></tag>", "", Parser.xmlParser())), "   aaa"); }
    @Test public void getWholeTextTestInnerTag2(){ Assert.assertEquals(XmlHelper.getWholeText(Jsoup.parse("<tag1> aaa<tag2>  </tag2>bb </tag>", "", Parser.xmlParser())), " aaa  bb "); }
    @Test public void getWholeTextTestInnerTag3(){ Assert.assertEquals(XmlHelper.getWholeText(Jsoup.parse("<tag1> aaa<tag2>\n  a </tag2>bb </tag>", "", Parser.xmlParser())), " aaa\n  a bb "); }

    @Test public void toStrTest1(){ toStrHelper("<tag><inner>text</inner></tag>", "<tag>\n  <inner>text</inner>\n</tag>"); }
    @Test public void toStrTest2(){ toStrHelper("<tag>aaa<inner>text</inner>aaa</tag>", "<tag>\n  <inner>text</inner>\n</tag>"); }
    @Test public void toStrTest3(){ toStrHelper("<tag><inner>  text \n </inner></tag>", "<tag>\n  <inner>  text \n </inner>\n</tag>"); }
    @Test public void toStrTest4(){ toStrHelper("<tag><inner key=\"value\" id=\"value2\">  text  </inner></tag>", "<tag>\n  <inner key=\"value\" id=\"value2\">  text  </inner>\n</tag>"); }
    @Test public void toStrTest5(){ toStrHelper("<tag><inner></inner></tag>", "<tag>\n  <inner/>\n</tag>"); }
    @Test public void toStrTest6(){ toStrHelper("<tag><inner><select/></inner></tag>", "<tag>\n  <inner>\n    <select/>\n  </inner>\n</tag>"); }
    @Test public void toStrTest7(){ toStrHelper("<tag><inner><select row=\"1\"/></inner></tag>", "<tag>\n  <inner>\n    <select row=\"1\"/>\n  </inner>\n</tag>"); }
    @Test public void toStrTest8(){ toStrHelper("<tag><inner><select></select></inner></tag>", "<tag>\n  <inner>\n    <select/>\n  </inner>\n</tag>"); }

    @Test
    public void toStrTest9(){
        toStrHelper("<tag><inner><select>aaa>0</select><select>aaa\"1</select><select>aaa&2</select></inner></tag>",
                "<tag>\n  <inner>\n    <select>aaa&gt;0</select>\n    <select>aaa&quot;1</select>\n    <select>aaa&amp;2</select>\n  </inner>\n</tag>");
    }

    @Test
    public void toStrTest10(){
        toStrHelper("<tag><inner><select>aaa<0</select><select>aaa\n1</select><select><inner2>bla</inner2></select></inner></tag>",
                "<tag>\n  <inner>\n    <select>aaa&lt;0</select>\n    <select>aaa&nbsp;1</select>\n    <select>\n      <inner2>bla</inner2>\n    </select>\n  </inner>\n</tag>");
    }

    private void toStrHelper(String xml, String exp){
        StringBuilder sb = new StringBuilder();
        XmlHelper.toStr("", sb, Jsoup.parse(exp, "", Parser.xmlParser()).child(0));
        Assert.assertEquals(sb.toString(), exp);
    }
}












