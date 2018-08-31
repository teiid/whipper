package org.whipper.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Element;
import org.whipper.ActualResultHolder;
import org.whipper.ExpectedResultHolder;
import org.whipper.Query;
import org.whipper.QuerySet;
import org.whipper.Scenario;
import org.whipper.Suite;
import org.whipper.exceptions.WhipperException;
import org.whipper.resultmode.NoneResultMode;
import org.whipper.resultmode.ResultMode;
import org.whipper.xml.error.QueryError;
import org.whipper.xml.result.QueryException;
import org.whipper.xml.result.QueryResultType;
import org.whipper.xml.result.Result;
import org.whipper.xml.result.Select;
import org.whipper.xml.result.Table;
import org.whipper.xml.result.Update;
import org.whipper.xml.suite.MultiMetaQuery;
import org.whipper.xml.suite.MultiQuery;
import org.whipper.xml.suite.QuerySuite;
import org.whipper.xml.suite.SimpleMetaQuery;
import org.whipper.xml.suite.SimpleQuery;
import org.whipper.xml.suite.Sql;

/**
 * XML helper class for reading and writing XML files (expected results, test queries, error files).
 *
 * TODO write information about base64 encoding
 *
 * @author Juraj Duráni
 */
public class XmlHelper {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final ResultMode NONE = new NoneResultMode();
    /* XML 1.0 valid characters - #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF] */
    private static final Pattern INVALID_XML_1_0_TEXT = Pattern.compile(".*[^\u0009\r\n\u0020-\uD7FF\uE000-\uFFFD\ud800\udc00-\udbff\udfff].*");
    /* XML tags */
    private static final String TYPE_INTEGER = "integer";
    private static final String TYPE_FLOAT = "float";
    private static final String TYPE_LONG = "long";
    private static final String TYPE_DOUBLE = "double";
    private static final String TYPE_BYTE = "byte";
    private static final String TYPE_DATE = "date";
    private static final String TYPE_TIME = "time";
    private static final String TYPE_TIMESTAMP = "timestamp";
    private static final String TYPE_BOOLEAN = "boolean";
    private static final String TYPE_CHAR = "char";
    private static final String TYPE_SHORT = "short";
    private static final String TYPE_BIGINTEGER = "biginteger";
    private static final String TYPE_BIGDECIMAL = "bigdecimal";
    private static final String TYPE_UNPRINTABLE = "unprintable";
    private static final String TYPE_REGEX = "regex";
    /* XML I/O */
    private static final Unmarshaller RESULT_UNMARSHALLER;
    private static final Unmarshaller SUITE_UNMARSHALLER;
    private static final Marshaller ERROR_MARSHALLER;
    private static final Marshaller RESULT_MARSHALLER;
    private static final org.whipper.xml.error.ObjectFactory ERROR_OBJECT_FACTORY;
    private static final org.whipper.xml.result.ObjectFactory RESULT_OBJECT_FACTORY;

    static{
        try{
            JAXBContext errorContext = JAXBContext.newInstance(org.whipper.xml.error.ObjectFactory.class.getPackage().getName());
            JAXBContext resultContext = JAXBContext.newInstance(org.whipper.xml.result.ObjectFactory.class.getPackage().getName());
            JAXBContext suiteContext = JAXBContext.newInstance(org.whipper.xml.suite.ObjectFactory.class.getPackage().getName());

            RESULT_UNMARSHALLER = resultContext.createUnmarshaller();
            SUITE_UNMARSHALLER = suiteContext.createUnmarshaller();

            ERROR_MARSHALLER = errorContext.createMarshaller();
            RESULT_MARSHALLER = resultContext.createMarshaller();
            RESULT_MARSHALLER.setProperty("jaxb.formatted.output", Boolean.TRUE);
            ERROR_MARSHALLER.setProperty("jaxb.formatted.output", Boolean.TRUE);

            ERROR_OBJECT_FACTORY = new org.whipper.xml.error.ObjectFactory();
            RESULT_OBJECT_FACTORY = new org.whipper.xml.result.ObjectFactory();
        } catch (JAXBException ex){
            throw new IllegalStateException("Cannot initialize JAXB classes - " + ex.getMessage(), ex);
        }
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
        try(FileInputStream fis = new FileInputStream(f)){
            QuerySuite xmlSuite = (QuerySuite)SUITE_UNMARSHALLER.unmarshal(fis);
            // load meta queries
            Map<String, QuerySet> meta = new HashMap<>();
            if(xmlSuite.getMetaQueries() != null){
                for(Object o : xmlSuite.getMetaQueries().getQueryOrQuerySet()){
                    QuerySet qs;
                    if(o instanceof SimpleMetaQuery){
                        SimpleMetaQuery smq = (SimpleMetaQuery)o;
                        qs = new QuerySet(smq.getName(), true, null);
                        qs.addQuery(new Query(scen, suite, qs, smq.getName(), smq.getValue(), scen.getMetaQuerySetResultMode()));
                    } else {
                        MultiMetaQuery mmq = (MultiMetaQuery)o;
                        qs = new QuerySet(mmq.getName(), true, null);
                        for(Sql sql : mmq.getSql()){
                            qs.addQuery(new Query(scen, suite, qs, sql.getName(), sql.getValue(), scen.getMetaQuerySetResultMode()));
                        }
                    }
                    meta.put(qs.getId(), qs);
                }
            }
            // set before/after suite
            suite.setBeforeEach(getMeta(meta, xmlSuite.getQueries().getBeforeEach()));
            suite.setAfterEach(getMeta(meta, xmlSuite.getQueries().getAfterEach()));
            suite.setBeforeSuite(getMeta(meta, xmlSuite.getQueries().getBeforeSuite()));
            suite.setAfterSuite(getMeta(meta, xmlSuite.getQueries().getAfterSuite()));

            // load queries
            for(Object o : xmlSuite.getQueries().getQueryOrQuerySet()){
                QuerySet qs;
                if(o instanceof SimpleQuery){
                    SimpleQuery sq = (SimpleQuery)o;
                    qs = new QuerySet(sq.getName(), scen.isFastFail(), scen.getMetaQuerySetResultMode());
                    qs.addQuery(new Query(scen, suite, qs, sq.getName(), sq.getValue(), resultMode));
                    qs.setBefore(getMeta(meta, sq.getBefore()));
                    qs.setAfter(getMeta(meta, sq.getAfter()));
                } else {
                    MultiQuery mq = (MultiQuery)o;
                    qs = new QuerySet(mq.getName(), scen.isFastFail(), scen.getMetaQuerySetResultMode());
                    for(Sql sql : mq.getSql()){
                        qs.addQuery(new Query(scen, suite, qs, sql.getName(), sql.getValue(), resultMode));
                    }
                    qs.setBefore(getMeta(meta, mq.getBefore()));
                    qs.setAfter(getMeta(meta, mq.getAfter()));
                }
                suite.addQuerySet(qs);
            }
        } catch (JAXBException ex) {
            throw new IOException("Cannot read suite file - " + ex.getMessage(), ex);
        }
    }

    /**
     * Returns meta-query-set from map {@code meta}.
     *
     * @param meta meta-query-set map
     * @param metaId meta-query-set id
     * @return meta-query-set with ID {@code metaId} or {@code null} if
     *      {@code metaId} is {@code null}
     * @throws IOException if {@code meta} does not contain entry with
     *      key {@code metaId}
     */
    private static QuerySet getMeta(Map<String, QuerySet> meta, String metaId) throws IOException{
        if(metaId == null){
            return null;
        }
        QuerySet out = meta.get(metaId);
        if(out == null){
            throw new IOException("Meta-query-set '" + metaId + "' does not exist.");
        }
        return out;
    }

    /**
     * Loads result from file and stores it in expected result holder.
     *
     * @param f file with result
     * @param erh expected result holder where result will be stored
     * @param q original query
     * @throws IOException if some error occurs
     */
    public static void loadResult(File f, ExpectedResultHolder erh, Query q) throws IOException{
        try(FileInputStream fis = new FileInputStream(f)){
            Result res = (Result)RESULT_UNMARSHALLER.unmarshal(fis);
            QueryResultType qr = res.getQueryResult();
            erh.setOriginalResult(qr);
            if(qr.getUpdate() != null){
                erh.setUpdateCount(qr.getUpdate().getUpdateCount());
            } else if(qr.getException() != null){
                erh.setExceptionClass(qr.getException().getClazz());
                erh.setExceptionMessage(qr.getException().getMessage());
                erh.setExceptionRegex(qr.getException().getMessageRegex());
            } else if(qr.getSelect() != null){
                Select s = qr.getSelect();
                Table t = qr.getTable();
                if(s.getDataElement().size() != t.getColumnCount()){
                    throw new WhipperException("Number of data elements in select is not equal to expected column count " + t.getColumnCount());
                }
                if(t.getTableRow().size() != t.getRowCount()){
                    throw new WhipperException("Number of table rows is not equal to expected row count " + t.getRowCount());
                }
                List<String> labels = new ArrayList<>(t.getColumnCount());
                List<String> types = new ArrayList<>(t.getColumnCount());
                List<List<Object>> rows = new ArrayList<>((int)t.getRowCount());
                for(Select.DataElement de : s.getDataElement()){
                    labels.add(de.getValue());
                    types.add(de.getType());
                }
                for(Table.TableRow tr : t.getTableRow()){
                    List<Object> columns = new ArrayList<>(t.getColumnCount());
                    if(tr.getTableCell().size() != t.getColumnCount()){
                        throw new WhipperException("Number of cells is not equal to expected column count " + t.getColumnCount());
                    }
                    for(Table.TableRow.TableCell tc : tr.getTableCell()){
                        columns.add(getFromElement((Element)tc.getAny()));
                    }
                    rows.add(columns);
                }
                erh.setColumnLabels(labels);
                erh.setColumnTypeNames(types);
                erh.setRows(rows);
            } else if(qr.getSql() != null){
                Query sql = new Query(q.getScenario(), q.getSuite(), q.getQuerySet(), q.getId() + "_expected_result", qr.getSql(), NONE);
                sql.run();
                ActualResultHolder sqlArh = sql.getActualResult();
                if(sqlArh.isResult()){
                    erh.setColumnLabels(sqlArh.getColumnLabels());
                    erh.setColumnTypeNames(sqlArh.getColumnTypeNames());
                    erh.setRows(sqlArh.getRows());
                } else if(sqlArh.isException()){
                    erh.setExceptionClass(sqlArh.getOriginalExceptionClass().getName());
                    erh.setExceptionMessage(sqlArh.getRootCauseExceptionMessage());
                } else if(sqlArh.isUpdate()){
                    erh.setUpdateCount(sqlArh.getUpdateCount());
                }
            } else if (qr.getNoResult() != null){
                // OK, 'no-result' in the XML file
            } else {
                throw new WhipperException("Unknown result file format.");
            }
        } catch (JAXBException | WhipperException ex){
            throw new IOException("Cannot read result file - " + ex.getMessage(), ex);
        }
    }

    /**
     * Returns object of which string representation is written in element {@code cell}.
     *
     * @param cell cell element
     * @return object from {@code cell} element
     * @throws IOException if some error occurs
     */
    private static Object getFromElement(Element cell) throws IOException{
        if(cell == null){
            return null;
        }
        String tagName = cell.getTagName();
        String data = cell.getTextContent();
        try{
            if(TYPE_UNPRINTABLE.equalsIgnoreCase(tagName)){
                return decodeToString(data);
            } else if(TYPE_INTEGER.equalsIgnoreCase(tagName)){
                return Integer.valueOf(data);
            } else if(TYPE_FLOAT.equalsIgnoreCase(tagName)){
                return Float.valueOf(data);
            } else if(TYPE_LONG.equalsIgnoreCase(tagName)){
                return Long.valueOf(data);
            } else if(TYPE_DOUBLE.equalsIgnoreCase(tagName)){
                return Double.valueOf(data);
            } else if(TYPE_BYTE.equalsIgnoreCase(tagName)){
                return Byte.valueOf(data);
            } else if(TYPE_DATE.equalsIgnoreCase(tagName)){
                return Date.valueOf(data);
            } else if(TYPE_TIME.equalsIgnoreCase(tagName)){
                return Time.valueOf(data);
            } else if(TYPE_TIMESTAMP.equalsIgnoreCase(tagName)){
                return Timestamp.valueOf(data);
            } else if(TYPE_BOOLEAN.equalsIgnoreCase(tagName)){
                return Boolean.valueOf(data);
            } else if(TYPE_CHAR.equalsIgnoreCase(tagName)){
                if(data.length() != 1){
                    throw new IOException("Invalid character length " + data.length());
                }
                return data.charAt(0);
            } else if(TYPE_SHORT.equalsIgnoreCase(tagName)){
                return Short.valueOf(data);
            } else if(TYPE_BIGINTEGER.equalsIgnoreCase(tagName)){
                return new BigInteger(data);
            } else if(TYPE_BIGDECIMAL.equalsIgnoreCase(tagName)){
                return new BigDecimal(data);
            } else if (TYPE_REGEX.equalsIgnoreCase(tagName)) {
                return Pattern.compile(data);
            } else { // String, Object and any other
                return data;
            }
        } catch (Exception ex){
            throw new IOException("Unable to read data of element " + cell + " - " + ex.getMessage(), ex);
        }
    }

    /**
     * Decodes string.
     *
     * @param data encoded string
     * @return decoded {@code data}
     */
    public static byte[] decode(String data){
        return Base64.decodeBase64(data);
    }

    /**
     * Decodes string and returns its string representation.
     *
     * @param data encoded string
     * @return decoded string
     */
    public static String decodeToString(String data){
        return new String(decode(data), UTF_8);
    }

    /**
     * Encodes string.
     *
     * @param data string to be encoded
     * @return encoded string
     */
    public static String encode(String data){
        return encode(data.getBytes(UTF_8));
    }

    /**
     * Encodes input byte array.
     *
     * @param data data to be encoded
     * @return encoded {@code data}
     */
    public static String encode(byte[] data){
        return Base64.encodeBase64String(data);
    }

    /**
     * Returns true if input string is printable according to XML 1.0.
     *
     * @param in input string
     * @return {@code true} if input does not contain invalid XML 1.0 characters
     *      {@code false} otherwise
     */
    private static boolean isPrintable(String in){
        return !INVALID_XML_1_0_TEXT.matcher(in).find();
    }

    /**
     * Writes result of the query {@code q} to the file {@code out}.
     *
     * @param q query
     * @param out output file
     * @throws IOException if some error occurs
     */
    public static void writeResult(Query q, File out) throws IOException{
        QueryResultType qr = produceQueryResult(q.getActualResult(), q.getId(), false);
        Result res = RESULT_OBJECT_FACTORY.createResult();
        res.setQuery(q.getSql());
        res.setQueryResult(qr);
        marshal(RESULT_MARSHALLER, out, res);
    }

    /**
     * Writes comparison error for query {@code q} to the file {@code out}.
     *
     * @param q query
     * @param exp expected result
     * @param out output file
     * @param expectedResultDirectoryName directory name used to load expected result
     * @throws IOException if some error occurs
     */
    public static void writeError(Query q, ExpectedResultHolder exp, File out, String expectedResultDirectoryName) throws IOException{
        QueryError err = ERROR_OBJECT_FACTORY.createQueryError();
        err.setQuery(q.getSql());
        if(exp.getOriginalResult() != null) {
            err.setExpectedResult(exp.getOriginalResult());
            err.getExpectedResult().setDirectory(expectedResultDirectoryName);
            err.getExpectedResult().setSuite(q.getSuite().getId());
        } else {
            // this probably means that the expected result file could not be found
            err.setExpectedResult(null);
        }
        err.setActualResult(produceQueryResult(q.getActualResult(), q.getId(), true));
        err.setFailures(ERROR_OBJECT_FACTORY.createQueryErrorFailures());
        err.getFailures().getFailure().addAll(exp.getErrors());
        marshal(ERROR_MARSHALLER, out, err);
    }

    /**
     * Creates query result XML element.
     *
     * @param arh actual result holder
     * @param name name of the result
     * @param appendStackTrace whether append full stack trace of the exception
     *      to the result or not
     * @return query result
     */
    private static QueryResultType produceQueryResult(ActualResultHolder arh, String name, boolean appendStackTrace){
        QueryResultType qr = RESULT_OBJECT_FACTORY.createQueryResultType();
        qr.setName(name);
        if(arh.isUpdate()){
            Update u = RESULT_OBJECT_FACTORY.createUpdate();
            u.setUpdateCount(arh.getUpdateCount());
            qr.setUpdate(u);
        } else if(arh.isException()){
            QueryException ex = RESULT_OBJECT_FACTORY.createQueryException();
            ex.setClazz(arh.getOriginalExceptionClass().getName());
            ex.setType(arh.getOriginalExceptionClass().getSimpleName());
            ex.setMessage(arh.getRootCauseExceptionMessage());
            if(appendStackTrace){
                StringWriter sw = new StringWriter();
                arh.getOriginalException().printStackTrace(new PrintWriter(sw));
                ex.setStackTrace(sw.toString());
            }
            qr.setException(ex);
        } else if(arh.isResult()){
            Select s = RESULT_OBJECT_FACTORY.createSelect();
            List<String> types = arh.getColumnTypeNames();
            List<String> labels = arh.getColumnLabels();
            for(int i = 0; i < types.size(); i++){
                Select.DataElement de = RESULT_OBJECT_FACTORY.createSelectDataElement();
                de.setType(types.get(i));
                de.setValue(labels.get(i));
                s.getDataElement().add(de);
            }
            qr.setSelect(s);
            Table t = RESULT_OBJECT_FACTORY.createTable();
            List<List<Object>> rows = arh.getRows();
            t.setColumnCount(types.size());
            t.setRowCount((long)rows.size());
            for(List<Object> row : rows){
                Table.TableRow r = RESULT_OBJECT_FACTORY.createTableTableRow();
                int i = 0;
                for(Object cell : row){
                    Table.TableRow.TableCell c = RESULT_OBJECT_FACTORY.createTableTableRowTableCell();
                    if(cell != null){
                        String cellStr = cell.toString();
                        QName tagName;
                        String ns = Result.class.getPackage().getAnnotation(XmlSchema.class).namespace();
                        if(isPrintable(cellStr)){
                            tagName = new QName(ns, types.get(i));
                        } else {
                            tagName = new QName(ns, TYPE_UNPRINTABLE);
                            cellStr = encode(cellStr);
                        }
                        JAXBElement<String> elem = new JAXBElement<>(tagName, String.class, cellStr);
                        c.setAny(elem);
                    }
                    r.getTableCell().add(c);
                    i++;
                }
                t.getTableRow().add(r);
            }
            qr.setTable(t);
        } else {
            qr.setNoResult(RESULT_OBJECT_FACTORY.createQueryResultTypeNoResult());
        }
        return qr;
    }

    /**
     * Stores object into file.
     *
     * @param m marshaller
     * @param out output file
     * @param o object to be stored
     * @throws IOException if some error occurs
     */
    private static void marshal(Marshaller m, File out, Object o) throws IOException{
        try(FileOutputStream fos = new FileOutputStream(out)){
            m.marshal(o, fos);
        } catch (JAXBException  ex){
            throw new IOException("Cannot write object to file - " + ex.getMessage(), ex);
        }
    }
}
