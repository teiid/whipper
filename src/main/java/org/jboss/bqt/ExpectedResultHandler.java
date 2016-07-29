package org.jboss.bqt;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Class which holds expected result of the query.
 *
 * @author Juraj Duráni
 */
public class ExpectedResultHandler {

    private int updateCount = -1;
    private List<String> columnLabels;
    private List<String> columnTypeNames;
    private List<Row> rows;
    private final List<String> errors = new ArrayList<String>();
    private String exceptionClass;
    private String exceptionMessage;
    private String exceptionRegex;
    private Elements originalResult;

    /**
     * Builds holders based on input XML file.
     *
     * @param xmlFilePath XMl file with expected result
     * @throws IOException if some error occurs or in input file is malformed
     */
    public void buildResult(File xmlFilePath) throws IOException{
        clear();
        Document doc = XmlHelper.getDocument(xmlFilePath);
        Element res = XmlHelper.getResultElement(doc);
        if(res == null){
            throw new IOException("There is either no or more than one result in file " + xmlFilePath);
        }
        originalResult = res.children();
        Element update = XmlHelper.getUpdateElement(res);
        Element exception = XmlHelper.getExceptionElement(res);
        Element select = XmlHelper.getSelectElement(res);
        if(update != null){
            try{
                updateCount = XmlHelper.getUpdateCount(update);
            } catch (NumberFormatException ex){
                throw new IOException("Unable to read update count from result in file " + xmlFilePath, ex);
            }
        } else if (exception != null){
            exceptionClass = XmlHelper.getExceptionClass(exception);
            exceptionMessage = XmlHelper.getExceptionMessage(exception);
            exceptionRegex = XmlHelper.getExceptionMessageRegEx(exception);
            if(exceptionClass == null){
                throw new IOException("No exception class in result [" + xmlFilePath + "].");
            }
            if(exceptionMessage == null && exceptionRegex == null){
                throw new IOException("No exception message in result [" + xmlFilePath + "].");
            }
        } else if (select != null){
            columnLabels = XmlHelper.getLabels(select);
            columnTypeNames = XmlHelper.getTypes(select);
            if(columnLabels.isEmpty() || columnTypeNames.isEmpty()){
                throw new IOException("No labels or types in result [" + xmlFilePath + "].");
            }
            Element table = XmlHelper.getTableElement(select.parent());
            if(table == null){
                throw new IOException("No table in result [" + xmlFilePath + "].");
            } else {
                int rowCount;
                int columnCount;
                try{
                    rowCount = XmlHelper.getRowCount(table);
                    columnCount = XmlHelper.getColumnCount(table);
                } catch (NumberFormatException ex){
                    throw new IOException("Malformed table. Cannot get row count and column count [" + xmlFilePath + "].", ex);
                }
                if(columnCount != columnLabels.size()){
                    throw new IOException("Expected and actual column count are different [" + xmlFilePath + "].");
                }
                List<List<Object>> rowsObjs = XmlHelper.getRows(table);
                if(rowsObjs.size() != rowCount){
                    throw new IOException("Expected and actual row count are different [" + xmlFilePath + "].");
                }
                rows = new ArrayList<Row>(rowsObjs.size());
                for(List<Object> r : rowsObjs){
                    if(r.size() != columnCount){
                        throw new IOException("Row " + (rows.size() + 1) + " should contain exactly " + columnCount + " cells [" + xmlFilePath + "].");
                    }
                    rows.add(new Row(rows.size() + 1, r));
                }
            }
        } else {
            throw new IOException("Unsupported result format in file " + xmlFilePath);
        }
    }

    /**
     * Decides, whether this holder represents an exception.
     *
     * @return {@code true} if this holder represents an exception, {@code false} otherwise
     */
    private boolean isException(){
        return exceptionClass != null;
    }

    /**
     * Decides, whether this holder represents an update.
     *
     * @return {@code true} if this holder represents an update, {@code false} otherwise
     */
    private boolean isUpdate(){
        return updateCount >= 0;
    }

    /**
     * Decides, whether this holder represents a table.
     *
     * @return {@code true} if this holder represents a table, {@code false} otherwise
     */
    private boolean isResult(){
        return rows != null;
    }

    /**
     * Returns list of comparison errors.
     *
     * @return errors
     * @see #equals(ActualResultHandler, boolean, BigDecimal)
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Returns XMl representation of original result.
     *
     * @return original result.
     */
    public Elements getOriginalResult() {
        return originalResult;
    }

    /**
     * Compares actual result with this expected result.
     *
     * @param handler actual result
     * @param couldSort whether results could be sorted or not
     * @param allowedDivergence will be used in comparison of {@link BigDecimal}, {@code Double} and {@code Float} values
     * @return {@code true} if there were no comparison errors, {@code false} otherwise
     */
    public boolean equals(ActualResultHandler handler, boolean couldSort, BigDecimal allowedDivergence){
        if(isException()){
            if(handler.isResult()){
                addError("Expected exception [" + exceptionClass + "] but found table.");
            } else if (handler.isUpdate()){
                addError("Expected exception [" + exceptionClass + "] but found update.");   
            } else {
                if(!exceptionClass.equals(handler.getOriginalExceptionClass().getName())){
                    addError("Expected and actual exception class are different. Expected: [" + exceptionClass + "], actual: [" + handler.getOriginalExceptionClass().getName() + "].");
                } else {
                    if(exceptionMessage != null && !exceptionMessage.equals(handler.getRootCauseExceptionMessage())){
                        if(!exceptionMessage.trim().equals(handler.getRootCauseExceptionMessage().trim())){
                            addError("Expected and actual message are different. Expected: [" + exceptionMessage + "], actual: [" + handler.getRootCauseExceptionMessage() + "].");
                        }
                    }
                    if(exceptionRegex != null && !handler.getRootCauseExceptionMessage().matches(exceptionRegex)){
                        addError("Message does not match pattern. Message: [" + handler.getRootCauseExceptionMessage() + "], pattern: [" + exceptionRegex + "].");
                    }
                }
            }
        } else if(isUpdate()){
            if(handler.isResult()){
                addError("Expected update but found table.");
            } else if (handler.isException()){
                addError("Expected update but found exception[" + handler.getOriginalExceptionClass().getName() + "].");   
            } else {
                if(updateCount != handler.getUpdateCount()){
                    addError("Expected and actual update count are different. Expected: [" + updateCount + "], actual: [" + handler.getUpdateCount() + "].");
                }
            }
        } else if(isResult()){
            if(handler.isUpdate()){
                addError("Expected table but found update.");
            } else if (handler.isException()){
                addError("Expected table but found exception[" + handler.getOriginalExceptionClass() + "].");   
            } else {
                if(columnLabels.size() != handler.getColumnLabels().size()){
                    addError("Expected and actual column count are different. Expected: ["
                        + columnLabels.size() + "], actual: [" + handler.getColumnLabels().size() + "].");
                } else if(rows.size() != handler.getRows().size()){
                    addError("Expected and actual row count are different. Expected: [" + rows.size() + "], actual: [" + handler.getRows().size() + "].");
                } else {
                    List<String> labels = handler.getColumnLabels();
                    List<String> types = handler.getColumnTypeNames();
                    for(int i = 0; i < columnLabels.size(); i++){
                        if(!columnLabels.get(i).equalsIgnoreCase(labels.get(i))){
                            addError("Expected and actual column label are different. Expected:["
                                    + columnLabels.get(i) + "], actual: [" + labels.get(i) + "].");
                        }
                        if(!columnTypeNames.get(i).equalsIgnoreCase(types.get(i))){
                            addError("Expected and actual column type are different. Expected:["
                                    + columnTypeNames.get(i) + "], actual: [" + types.get(i) + "].");
                        }
                    }
                    if(errors.isEmpty()){
                        List<Row> actualRows = convertToRows(handler.getRows());
                        List<Row> expectedRows = new ArrayList<Row>(rows);
                        if(couldSort){
                            Collections.sort(actualRows);
                            Collections.sort(expectedRows);
                        }
                        for(int i = 0; i < expectedRows.size(); i++){
                            compareRows(expectedRows.get(i), actualRows.get(i), i, allowedDivergence);
                        }
                    }
                }
            }
        } else {
            return false;
        }
        return errors.isEmpty();
    }

    /**
     * Converts list of list of object to list of {@link Row}.
     *
     * @param list object
     * @return list of rows
     */
    private List<Row> convertToRows(List<List<Object>> list){
        List<Row> toReturn = new ArrayList<Row>(list.size());
        for(List<Object> l : list){
            toReturn.add(new Row(toReturn.size() + 1, l));
        }
        return toReturn;
    }

    /**
     * Compare two rows.
     *
     * @param expected expected row
     * @param actual actual row
     * @param rowNum row number
     * @param allowedDivergence allowed divergence
     */
    private void compareRows(Row expected, Row actual, int rowNum, BigDecimal allowedDivergence){
        final String rowId = "[row " + rowNum + "; row number in expected result " + expected.idx + "; row number in actual result" + actual.idx + "].";
        for(int i = 0; i < expected.cells.size(); i++){
            final String cellId = "[cell " + (i+1) + "]" + rowId;
            Object ex = expected.cells.get(i);
            Object ac = actual.cells.get(i);
            if(ex == null && ac == null){
                continue;
            } else if(ex == null && ac != null){
                addError("Expected null but get value. " + cellId);
            } else if(ex != null && ac == null){
                addError("Expected value but get null." + cellId);
            } else {
                boolean fail = false;
                if(ex instanceof BigDecimal){
                    BigDecimal exBD = (BigDecimal)ex;
                    BigDecimal acBD = (BigDecimal)ac;
                    fail = exBD.compareTo(acBD) != 0 && (exBD.add(allowedDivergence).compareTo(acBD) < 0 || exBD.subtract(allowedDivergence).compareTo(acBD) > 0);
                } else  if(ex instanceof BigInteger){
                    BigInteger exBI = (BigInteger)ex;
                    BigInteger acBI = (BigInteger)ac;
                    fail = exBI.compareTo(acBI) != 0;
                } else  if(ex instanceof Float){
                    float exF = ((Float)ex).floatValue();
                    float acF = ((Float)ac).floatValue();
                    fail = (exF != acF) && (exF + allowedDivergence.floatValue() < acF || exF - allowedDivergence.floatValue() > acF);
                } else  if(ex instanceof Double){
                    double exD = ((Double)ex).doubleValue();
                    double acD = ((Double)ac).doubleValue();
                    fail = (exD != acD) && (exD + allowedDivergence.doubleValue() < acD || exD - allowedDivergence.doubleValue() > acD);
                }
                if(fail){
                    addError("Actual and expected value are different. Actual: [" + ac + "], expected: [" + ex + "]. " + cellId);
                } else {
                    if(!ex.equals(ac)){
                        // convert both to string and try to compare them
                        char[] v1 = ex.toString().toCharArray();
                        char[] v2 = ac.toString().toCharArray();
                        if(v1.length != v2.length){
                            addError("Actual and expected value are different. Actual length: [" + v2.length + "], expected length: [" + v1.length + "]. " + cellId);
                        } else {
                            for(int c = 0; c < v1.length; c++){
                                if(v1[c] != v2[c]){
                                    addError("Actual and expected value are different at position " + (c + 1) + ". Actual: [..."
                                            + new String(v2, Math.max(0, c - 10), Math.min(v2.length - c, 20)) + "...], expected: [..."
                                            + new String(v1, Math.max(0, c - 10), Math.min(v1.length - c, 20)) + "...]. " + cellId);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds an error to actual error list.
     *
     * @param error error message to be added.
     */
    private void addError(String error){
        errors.add(error);
    }

    /**
     * Clears this holder.
     */
    private void clear(){
        updateCount = -1;
        if(columnLabels != null){ columnLabels.clear(); }
        if(columnTypeNames != null){ columnTypeNames.clear(); }
        if(rows != null){ rows.clear(); }
        columnLabels = null;
        columnTypeNames = null;
        rows = null;
        errors.clear();
        exceptionClass = null;
        exceptionMessage = null;
        exceptionRegex = null;
    }

    /**
     * Class which represents single row of result.
     *
     * @author Juraj Duráni
     */
    private class Row implements Comparable<Row> {
        private final int idx;
        private final List<Object> cells;

        private Row(int idx, List<Object> cells) {
            this.idx = idx;
            this.cells = cells;
        }

        @Override
        public int compareTo(Row o) {
            for(int i = 0; i < cells.size(); i++){
                int comp = compareCells(cells.get(i), o.cells.get(i));
                if(comp != 0){
                    return comp;
                }
            }
            return 0;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private int compareCells(Object o1, Object o2){
            if(o1 == o2){
                return 0;
            }
            if(o1 == null && o2 != null){
                return -1;
            }
            if(o1 != null && o2 == null){
                return 1;
            }
            if(o1 instanceof Comparable){
                return ((Comparable)o1).compareTo(2);
            }
            return o1.toString().compareTo(o2.toString());
        }
    }
}
