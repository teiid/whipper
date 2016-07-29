package org.jboss.bqt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.jboss.bqt.resultmode.ResultMode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

/**
 * XML helper class for reading and writing XML files (expected results, test queries, error files).
 *
 * TODO write information about base64 encoding
 *
 * @author Juraj Duráni
 */
public class XmlHelper {

    /**
     * XML tags
     *
     * @author Juraj Duráni
     */
    protected static interface XmlTags{
        public static final String ROOT = "root";
        public static final String QUERY = "query";
        public static final String SQL = "sql";
        public static final String QUERY_RESULTS = "queryResults";
        public static final String SELECT = "select";
        public static final String UPDATE = "update";
        public static final String DATA_ELEMENT = "dataElement";
        public static final String TABLE = "table";
        public static final String TABLE_ROW = "tableRow";
        public static final String TABLE_CELL = "tableCell";
        public static final String EXCEPTION = "exception";
        public static final String EXCEPTION_TYPE = "exceptionType";
        public static final String EXCEPTION_MESSAGE = "message";
        public static final String EXCEPTION_MESSAGE_REGEX = "messageRegex";
        public static final String EXCEPTION_CLASS = "class";
        public static final String FAILURE_MESSAGE = "failureMessage";
        public static final String ACTUAL_RESULT = "actualResult";
        public static final String EXPECTED_RESULT = "expectedResult";
        public static final String EXCEPTION_STACK_TRACE = "stackTrace";

        public static final String TYPE_INTEGER = "integer";
        public static final String TYPE_STRING = "string";
        public static final String TYPE_FLOAT = "float";
        public static final String TYPE_LONG = "long";
        public static final String TYPE_DOUBLE = "double";
        public static final String TYPE_BYTE = "byte";
        public static final String TYPE_DATE = "date";
        public static final String TYPE_TIME = "time";
        public static final String TYPE_TIMESTAMP = "timestamp";
        public static final String TYPE_BOOLEAN = "boolean";
        public static final String TYPE_CHAR = "char";
        public static final String TYPE_SHORT = "short";
        public static final String TYPE_BIGINTEGER = "biginteger";
        public static final String TYPE_BIGDECIMAL = "bigdecimal";
        public static final String TYPE_OBJECT = "object";
        public static final String TYPE_NULL = "null";
    }

    /**
     * XML attributes.
     *
     * @author Juraj Duráni
     */
    protected static interface XmlAttributes{
        public static final String NAME = "name";
        public static final String ROW_COUNT = "rowCount";
        public static final String COLUMN_COUNT = "columnCount";
        public static final String UPDATE_COUNT = "updatecnt";
        public static final String TYPE = "type";
        public static final String UNPRINTABLE = "unprintable";
        public static final String HEX = "hex";
        public static final String VALUE = "value";
    }

    /**
     * Returns a single element.
     *
     * @param root root element
     * @param selector selector
     * @return single element or {@code null} if there is either no element or more than one element
     */
    private static Element getSingleElement(Element root, String selector){
        Elements res = root.select(selector);
        if(res.isEmpty() || res.size() != 1){
            return null;
        }
        return res.first();
    }

    /**
     * Loads test queries from file {@code f}.
     *
     * @param f XML file with test queries
     * @param scen scenario
     * @param suite suite
     * @param resultMode result mode
     * @throws IOException if some error occurs
     */
    public static void loadQueries(File f, Scenario scen, Suite suite, ResultMode resultMode) throws IOException{
        Document doc = getDocument(f);
        for(Element queryElements : doc.select(XmlTags.ROOT + " > " + XmlTags.QUERY)){
            QuerySet set = new QuerySet(queryElements.attr(XmlAttributes.NAME), scen.isFastFail());
            Elements sqls = queryElements.select(XmlTags.SQL);
            if(sqls.isEmpty()){
                Query query = new Query(scen, suite, set, set.getId(), getWholeText(queryElements), resultMode);
                set.addQuery(query);
            } else {
                for(Element sql : sqls){
                    Query query = new Query(scen, suite, set, sql.attr(XmlAttributes.NAME),
                            getWholeText(sql), resultMode);
                    set.addQuery(query);
                }
            }
            suite.addQuerySet(set);
        }
    }

    /**
     * Returns element with query result.
     *
     * @param elem root element
     * @return query-result element
     */
    public static Element getResultElement(Element elem){
        return getSingleElement(elem, XmlTags.ROOT + " > " + XmlTags.QUERY_RESULTS);
    }

    /**
     * Returns update element.
     *
     * @param elem root element
     * @return update element
     */
    public static Element getUpdateElement(Element elem){
        return getSingleElement(elem, XmlTags.UPDATE);
    }

    /**
     * Returns exception element.
     *
     * @param elem root element
     * @return exception element
     */
    public static Element getExceptionElement(Element elem){
        return getSingleElement(elem, XmlTags.EXCEPTION);
    }

    /**
     * Returns select element.
     *
     * @param elem root element
     * @return select element
     */
    public static Element getSelectElement(Element elem){
        return getSingleElement(elem, XmlTags.SELECT);
    }

    /**
     * Returns table element.
     *
     * @param elem root element
     * @return table element
     */
    public static Element getTableElement(Element elem){
        return getSingleElement(elem, XmlTags.TABLE);
    }

    /**
     * Returns exception class element.
     *
     * @param elem root element
     * @return exception class element
     */

    public static String getExceptionClass(Element elem){
        Element cls = getSingleElement(elem, XmlTags.EXCEPTION_CLASS);
        return getWholeText(cls);
    }

    /**
     * Returns exception message element.
     *
     * @param elem root element
     * @return exception message element
     */

    public static String getExceptionMessage(Element elem){
        Element cls = getSingleElement(elem, XmlTags.EXCEPTION_MESSAGE);
        return getWholeText(cls);
    }

    /**
     * Returns exception message regex element.
     *
     * @param elem root element
     * @return exception message regex element
     */

    public static String getExceptionMessageRegEx(Element elem){
        Element cls = getSingleElement(elem, XmlTags.EXCEPTION_MESSAGE_REGEX);
        return getWholeText(cls);
    }

    /**
     * Returns labels of the columns.
     *
     * @param root root element
     * @return list of labels
     */
    public static List<String> getLabels(Element root){
        Elements els = root.select(XmlTags.DATA_ELEMENT);
        List<String> out = new ArrayList<String>(els.size());
        for(Element e : els){
            out.add(getWholeText(e));
        }
        return out;
    }

    /**
     * Returns type of the columns.
     *
     * @param root root element
     * @return list of types
     */
    public static List<String> getTypes(Element root){
        Elements els = root.select(XmlTags.DATA_ELEMENT);
        List<String> out = new ArrayList<String>(els.size());
        for(Element e : els){
            out.add(e.attr(XmlAttributes.TYPE));
        }
        return out;
    }

    /**
     * Returns update count from the element.
     *
     * @param update update element
     * @return update count
     */
    public static int getUpdateCount(Element update){
        return Integer.parseInt(update.attr(XmlAttributes.UPDATE_COUNT));
    }

    /**
     * Returns row count from the element.
     *
     * @param table table element
     * @return row count
     */
    public static int getRowCount(Element table){
        return Integer.parseInt(table.attr(XmlAttributes.ROW_COUNT));
    }

    /**
     * Returns column count from the element.
     *
     * @param table table element
     * @return column count
     */
    public static int getColumnCount(Element table){
        return Integer.parseInt(table.attr(XmlAttributes.COLUMN_COUNT));
    }

    /**
     * Returns list of rows from the element.
     *
     * @param table table element
     * @return list of rows
     * @throws IOException if some error occurs
     */
    public static List<List<Object>> getRows(Element table) throws IOException{
        Elements xmlRows = table.select(XmlTags.TABLE_ROW);
        List<List<Object>> rows = new ArrayList<List<Object>>(xmlRows.size());
        for(Element xmlRow : xmlRows){
            Elements cells = xmlRow.select(XmlTags.TABLE_CELL);
            List<Object> row = new ArrayList<Object>(cells.size()); 
            for(Element cell : cells){
                row.add(getFromElement(cell));
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * Returns object of which string representation is written in element {@code cell}.
     *
     * @param cell cell element
     * @return object from {@code cell} element
     * @throws IOException if some error occurs
     */
    private static Object getFromElement(Element cell) throws IOException{
        if(XmlTags.TYPE_NULL.equalsIgnoreCase(getWholeText(cell))){
            return null;
        }
        Element child = cell.child(0);
        String tagName = child.tagName();
        String data;
        if("true".equalsIgnoreCase(child.attr(XmlAttributes.UNPRINTABLE))){
            data = decodeHex(child.attr(XmlAttributes.HEX));
        } else {
            data = getWholeText(child);
        }
        try{
            if(XmlTags.TYPE_INTEGER.equalsIgnoreCase(tagName)){
                return Integer.valueOf(data);
            } else if(XmlTags.TYPE_FLOAT.equalsIgnoreCase(tagName)){
                return Float.valueOf(data);
            } else if(XmlTags.TYPE_LONG.equalsIgnoreCase(tagName)){
                return Long.valueOf(data);
            } else if(XmlTags.TYPE_DOUBLE.equalsIgnoreCase(tagName)){
                return Double.valueOf(data);
            } else if(XmlTags.TYPE_BYTE.equalsIgnoreCase(tagName)){
                return Byte.valueOf(data);
            } else if(XmlTags.TYPE_DATE.equalsIgnoreCase(tagName)){
                return Date.valueOf(data);
            } else if(XmlTags.TYPE_TIME.equalsIgnoreCase(tagName)){
                return Time.valueOf(data);
            } else if(XmlTags.TYPE_TIMESTAMP.equalsIgnoreCase(tagName)){
                return Timestamp.valueOf(data);
            } else if(XmlTags.TYPE_BOOLEAN.equalsIgnoreCase(tagName)){
                return Boolean.valueOf(data);
            } else if(XmlTags.TYPE_CHAR.equalsIgnoreCase(tagName)){
                if(data.length() != 1){
                    throw new IOException("Invalid character length " + data.length());
                }
                return new Character(data.charAt(0));
            } else if(XmlTags.TYPE_SHORT.equalsIgnoreCase(tagName)){
                return Short.valueOf(data);
            } else if(XmlTags.TYPE_BIGINTEGER.equalsIgnoreCase(tagName)){
                return new BigInteger(data);
            } else if(XmlTags.TYPE_BIGDECIMAL.equalsIgnoreCase(tagName)){
                return new BigDecimal(data);
            } else { // String, Object and any other
                return data;
            }
        } catch (Exception ex){
            throw new IOException("Unable to read data of element " + cell, ex);
        }
    }

    /**
     * Decodes HEX representation of the string.
     *
     * @param data HEX
     * @return String representation of the {@code data}
     * @throws IOException if some error occurs
     */
    private static String decodeHex(String data) throws IOException{
        if(data == null || data.isEmpty()){
            return data;
        }
        if(data.length() % 2 != 0){
            throw new IOException("String is not in hexadecimal representation.");
        }
        byte[] bytes = new byte[data.length() / 2];
        for(int i = 0; i < data.length(); i+=2){
            bytes[i / 2] = Integer.valueOf(data.substring(i, i + 2), 16).byteValue();
        }
        return new String(bytes, "UTF-8");
    }

    /**
     * Appends text of the node {@code orig} to the string builder.
     *
     * @param sb string builder
     * @param orig node
     */
    private static void appendText(StringBuilder sb, Node orig){
        if(orig instanceof TextNode){
            sb.append(((TextNode)orig).getWholeText());
        } else {
            List<Node> nodes = orig.childNodes();
            if(nodes.isEmpty()){
                return;
            }
            for(Node n : nodes){
                appendText(sb, n);
            }
        }
    }

    /**
     * Returns whole text of the node. Including text from nested tags.
     *
     * @param orig node
     * @return text of the node
     */
    static String getWholeText(Node orig){
        if(orig == null){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        appendText(sb, orig);
        return sb.toString();
    }

    /**
     * Writes result of the query {@code q} to the file {@code out}.
     *
     * @param q query
     * @param out output file
     * @throws IOException if some error occurs
     */
    public static void writeResult(Query q, File out) throws IOException{
        Document doc = new Document("");
        Element root = doc.appendElement(XmlTags.ROOT);
        root.appendElement(XmlTags.QUERY).appendChild(new TextNode(q.getSql(), ""));
        produceXml(root.appendElement(XmlTags.QUERY_RESULTS).attr(XmlAttributes.NAME, q.getId()),
                q.getActualResult(), false);
        printDocument(doc, out);
    }

    /**
     * Writes comparison error for query {@code q} to the file {@code out}.
     *
     * @param q query
     * @param exp expected result
     * @param out output file
     * @throws IOException if some error occurs
     */
    public static void writeError(Query q, ExpectedResultHandler exp, File out) throws IOException{
        Document d = new Document("");
        Element root = d.appendElement(XmlTags.ROOT);
        Element result = root.appendElement(XmlTags.QUERY_RESULTS)
                .attr(XmlAttributes.NAME, q.getId())
                .attr(XmlAttributes.VALUE, q.getSql());
        for(String err : exp.getErrors()){
            result.appendElement(XmlTags.FAILURE_MESSAGE).appendChild(new TextNode(err, ""));
        }
        produceXml(root.appendElement(XmlTags.ACTUAL_RESULT), q.getActualResult(), true);
        Element er = root.appendElement(XmlTags.EXPECTED_RESULT);
        for(Element e : exp.getOriginalResult()){
            er.appendChild(e);
        }
        printDocument(d, out);
    }

    /**
     * Parses XML file {@code xmlFilePath}.
     *
     * @param xmlFilePath XML file
     * @return document
     * @throws IOException
     */
    public static Document getDocument(File xmlFilePath) throws IOException{
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(xmlFilePath);
            Document doc = Jsoup.parse(fis, "UTF-8", "", Parser.xmlParser());
            return doc;
        } catch (Exception ex){
            throw new IOException("Unable to parse file " + xmlFilePath, ex);
        } finally {
            BqtTool.close(fis);
        }
    }

    /**
     * Prints document {@code doc} to file {@code filePath}.
     *
     * @param doc document to be written
     * @param filePath output file
     * @throws IOException if some error occurs
     */
    public static void printDocument(Document doc, File filePath) throws IOException{
        FileWriter out = null;
        try{
            out = new FileWriter(filePath);
            StringBuilder sb = new StringBuilder();
            toStr("", sb, doc.child(0));
            out.write(sb.toString());
        } finally {
            BqtTool.close(out);
        }
    }

    /**
     * Appends element {@code e} to the string builder {@code sb}.
     * Basic indent of the element will be {@code indent}.
     *
     * @param indent indent
     * @param sb string builder
     * @param e root element
     */
    static void toStr(String indent, StringBuilder sb, Element e){
        sb.append(indent).append("<").append(e.tagName());
        for(Attribute a : e.attributes()){
            sb.append(" ").append(a.getKey()).append("=\"").append(a.getValue()).append("\"");
        }
        List<Node> chs = e.childNodes();
        if(chs.size() == 0){
            sb.append("/>");
        } else {
            sb.append(">");
            if(chs.size() == 1 && chs.get(0) instanceof TextNode){
                sb.append(escape(((TextNode)chs.get(0)).getWholeText()));
            } else {
                boolean added = false;
                for(Node n : chs){
                    if(n instanceof Element){
                        String ind = indent + "  ";
                        sb.append(BqtTool.LS);
                        toStr(ind, sb, (Element)n);
                        added = true;
                    }
                }
                if(added){
                    sb.append(BqtTool.LS).append(indent);
                }
            }
            sb.append("</").append(e.tagName()).append(">");
        }
    }

    /**
     * Produces XML representation of actual result.
     *
     * @param rootElement root element
     * @param result actual result
     * @param addStacktrace whether to add stacktrace of the exception to the result
     */
    public static void produceXml(Element rootElement, ActualResultHandler result, boolean addStacktrace){
        if(result.isException()){
            produceXml(rootElement, result.getOriginalException(), result.getRootCauseExceptionMessage(), addStacktrace);
        } else if(result.isUpdate()){
            produceXml(rootElement, result.getUpdateCount());
        } else {
            produceXml(rootElement, result.getColumnTypeNames(), result.getColumnLabels(), result.getRows());
        }
    }

    /**
     * Produces XML representation of update.
     *
     * @param rootElement root element
     * @param updCnt update count
     */
    private static void produceXml(Element rootElement, int updCnt){
        rootElement.appendElement(XmlTags.UPDATE)
            .attr(XmlAttributes.UPDATE_COUNT, Integer.toString(updCnt));
    }

    /**
     * Produces XML representation of exception.
     *
     * @param rootElement root element
     * @param ex exception
     * @param rootMessage message of the root cause of the exception
     * @param addStacktrace whether to add stacktrace to the XML
     */
    private static void produceXml(Element rootElement, Exception ex, String rootMessage, boolean addStacktrace){
        Element exception = rootElement.appendElement(XmlTags.EXCEPTION);
        exception.appendElement(XmlTags.EXCEPTION_TYPE)
            .appendChild(new TextNode(ex.getClass().getSimpleName(), ""));
        exception.appendElement(XmlTags.EXCEPTION_MESSAGE)
            .appendChild(new TextNode(rootMessage, ""));
        exception.appendElement(XmlTags.EXCEPTION_CLASS)
            .appendChild(new TextNode(ex.getClass().getName(), ""));
        if(addStacktrace){
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            exception.appendElement(XmlTags.EXCEPTION_STACK_TRACE)
                .appendChild(new TextNode(sw.toString(), ""));
        }
    }

    /**
     * Produces XML representation of the table.
     *
     * @param rootElement root element
     * @param types column types
     * @param labels column labels
     * @param rows rows
     */
    private static void produceXml(Element rootElement, List<String> types, List<String> labels, List<List<Object>> rows){
        Element select = rootElement.appendElement(XmlTags.SELECT);
        int columns = types.size();
        for(int i = 0; i < columns; i++){
            select.appendElement(XmlTags.DATA_ELEMENT)
                .attr(XmlAttributes.TYPE, types.get(i))
                .appendChild(new TextNode(labels.get(i), ""));
        }
        Element table = rootElement.appendElement(XmlTags.TABLE)
                .attr(XmlAttributes.ROW_COUNT, Integer.toString(rows.size()))
                .attr(XmlAttributes.COLUMN_COUNT, Integer.toString(columns));
        for(List<Object> row : rows){
            Element tableRow = table.appendElement(XmlTags.TABLE_ROW);
            for(int i = 0; i < columns; i++){
                Element tableCell = tableRow.appendElement(XmlTags.TABLE_CELL);
                Object o = row.get(i);
                if(o == null){
                    tableCell.appendChild(new TextNode(XmlTags.TYPE_NULL, ""));
                } else {
                    tableCell.appendElement(types.get(i))
                        .appendChild(new TextNode(o.toString(), ""));
                }
            }
        }
    }

    /**
     * Escapes string for XML.
     *
     * @param in input string
     * @return escaped string
     */
    private static String escape(String in){
        StringBuilder sb = new StringBuilder();
        for(char c : in.toCharArray()){
            switch(c){
                case '&':
                    sb.append("&amp;");
                    break;
                case 0xA0: // \n
                    sb.append("&nbsp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
