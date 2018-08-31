package org.whipper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.whipper.xml.XmlHelper;
import org.whipper.xml.result.QueryResultType;

/**
 * Class which holds expected result of the query.
 *
 * @author Juraj Duráni
 */
public class ExpectedResultHolder {

    private int updateCount = -1;
    private List<String> columnLabels;
    private List<String> columnTypeNames;
    private List<Row> rows;
    private final List<String> errors = new ArrayList<>();
    private String exceptionClass;
    private String exceptionMessage;
    private String exceptionRegex;
    private QueryResultType originalResult;

    /**
     * Builds holders based on input XML file.
     *
     * @param xmlFilePath XMl file with expected result
     * @param q original query
     * @throws IOException if some error occurs or in input file is malformed
     */
    public void buildResult(File xmlFilePath, Query q) throws IOException{
        clear();
        XmlHelper.loadResult(xmlFilePath, this, q);
    }

    /**
     * Sets labels.
     *
     * @param columnLabels labels to be set
     */
    public void setColumnLabels(List<String> columnLabels){
        this.columnLabels = columnLabels;
    }

    /**
     * Sets types.
     *
     * @param columnTypeNames types to be set
     */
    public void setColumnTypeNames(List<String> columnTypeNames){
        this.columnTypeNames = columnTypeNames;
    }

    /**
     * Sets exception class.
     *
     * @param exceptionClass exception class to be set
     */
    public void setExceptionClass(String exceptionClass){
        this.exceptionClass = exceptionClass;
    }

    /**
     * Sets exception message.
     *
     * @param exceptionMessage exception message to be set
     */
    public void setExceptionMessage(String exceptionMessage){
        this.exceptionMessage = exceptionMessage;
    }

    /**
     * Sets exception message regex.
     *
     * @param exceptionRegex exception message regex to be set
     */
    public void setExceptionRegex(String exceptionRegex){
        this.exceptionRegex = exceptionRegex;
    }

    /**
     * Sets original result as XML object for of this expected result.
     *
     * @param originalResult original result to be set
     */
    public void setOriginalResult(QueryResultType originalResult){
        this.originalResult = originalResult;
    }

    /**
     * Sets rows.
     *
     * @param rows rows to be set
     */
    public void setRows(List<List<Object>> rows){
        if(this.rows != null){
            this.rows.clear();
        } else {
            this.rows = new ArrayList<>(rows.size());
        }
        for(List<Object> r : rows){
            this.rows.add(new Row(this.rows.size() + 1, r));
        }
    }

    /**
     * Sets update count.
     *
     * @param updateCount update count to be set
     */
    public void setUpdateCount(int updateCount){
        this.updateCount = updateCount;
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
     * @see #equals(ActualResultHolder, boolean, BigDecimal)
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Decides, whether this holder does not represent any result.
     *
     * @return {@code true} if this holder represents none of update, result, exception
     */
    public boolean isNoResult(){
        return !isException() && !isResult() && !isUpdate();
    }

    /**
     * Returns XMl representation of original result.
     *
     * @return original result
     */
    public QueryResultType getOriginalResult() {
        return originalResult;
    }

    /**
     * Compares actual result with this expected result.
     *
     * @param holder actual result
     * @param couldSort whether results could be sorted or not
     * @param allowedDivergence will be used in comparison of {@link BigDecimal}, {@code Double} and {@code Float} values
     * @return {@code true} if there were no comparison errors, {@code false} otherwise
     */
    public boolean equals(ActualResultHolder holder, boolean couldSort, BigDecimal allowedDivergence){
        errors.clear();
        if(isException()){
            equalsException(holder);
        } else if(isUpdate()){
            equalsUpdate(holder);
        } else if(isResult()){
            equalsTable(holder, couldSort, allowedDivergence);
        } else {
            equalsNoResult(holder);
        }
        return errors.isEmpty();
    }

    private void equalsNoResult(ActualResultHolder holder){
        if(holder.isException()){
            addError("Expected no-result but found exception.");
        } else if(holder.isResult()){
            addError("Expected no-result but found result.");
        } else if(holder.isUpdate()){
            addError("Expected no-result but found update.");
        }
    }

    /**
     * Compares table of actual result and this expected result.
     *
     * @param holder actual result
     * @param couldSort whether result could be sorted or not
     * @param allowedDivergence for comparison of real numbers
     */
    private void equalsTable(ActualResultHolder holder, boolean couldSort, BigDecimal allowedDivergence){
        if(holder.isUpdate()){
            addError("Expected table but found update.");
        } else if(holder.isException()){
            addError("Expected table but found exception[" + holder.getOriginalExceptionClass() + "].");
        } else if(holder.isNoResult()){
            addError("Expected table but found no-result.");
        } else {
            if(columnLabels.size() != holder.getColumnLabels().size()){
                addError("Expected and actual column count are different. Expected: ["
                    + columnLabels.size() + "], actual: [" + holder.getColumnLabels().size() + "].");
            } else if(rows.size() != holder.getRows().size()){
                addError("Expected and actual row count are different. Expected: [" + rows.size() + "], actual: [" + holder.getRows().size() + "].");
            } else {
                List<String> labels = holder.getColumnLabels();
                List<String> types = holder.getColumnTypeNames();
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
                    List<Row> actualRows = convertToRows(holder.getRows());
                    List<Row> expectedRows = new ArrayList<>(rows);
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
    }

    /**
     * Compares update count of actual result and this expected result.
     *
     * @param handler actual result
     */
    private void equalsUpdate(ActualResultHolder handler){
        if (handler.isResult()){
            addError("Expected update but found table.");
        } else if (handler.isException()){
            addError("Expected update but found exception[" + handler.getOriginalExceptionClass().getName() + "].");
        } else if(handler.isNoResult()){
            addError("Expected update but found no-result.");
        } else {
            if (updateCount != handler.getUpdateCount()) {
                addError("Expected and actual update count are different. Expected: [" + updateCount + "], actual: ["
                        + handler.getUpdateCount() + "].");
            }
        }
    }

    /**
     * Compares exception of actual result and this expected result.
     *
     * @param handler actual result
     */
    private void equalsException(ActualResultHolder handler){
        if(handler.isResult()){
            addError("Expected exception [" + exceptionClass + "] but found table.");
        } else if(handler.isUpdate()){
            addError("Expected exception [" + exceptionClass + "] but found update.");
        } else if(handler.isNoResult()){
            addError("Expected exception but found no-result.");
        } else {
            if(!exceptionClass.equals(handler.getOriginalExceptionClass().getName())){
                addError("Expected and actual exception class are different. Expected: [" + exceptionClass + "], actual: [" + handler.getOriginalExceptionClass().getName() + "].");
            } else {
                if(exceptionMessage != null && !exceptionMessage.equals(handler.getRootCauseExceptionMessage())){
                    if(!exceptionMessage.trim().equals(handler.getRootCauseExceptionMessage().trim())){
                        addError("Expected and actual message are different. Expected: [" + exceptionMessage + "], actual: [" + handler.getRootCauseExceptionMessage() + "].");
                    }
                }
                if(exceptionRegex != null && !Pattern.compile(exceptionRegex, Pattern.DOTALL).matcher(handler.getRootCauseExceptionMessage()).matches()){
                    addError("Message does not match pattern. Message: [" + handler.getRootCauseExceptionMessage() + "], pattern: [" + exceptionRegex + "].");
                }
            }
        }
    }

    /**
     * Converts list of list of object to list of {@link Row}.
     *
     * @param list object
     * @return list of rows
     */
    private List<Row> convertToRows(List<List<Object>> list){
        List<Row> toReturn = new ArrayList<>(list.size());
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
        final String rowId = "[row " + (rowNum + 1) + "; row number in expected result " + expected.idx + "; row number in actual result " + actual.idx + "].";
        for(int i = 0; i < expected.cells.size(); i++){
            final String cellId = "[cell " + (i + 1) + "]" + rowId;
            Object ex = expected.cells.get(i);
            Object ac = actual.cells.get(i);
            if(ex == null && ac == null){
                continue;
            } else if(ex == null){
                addError("Expected null but get value. " + cellId);
            } else if(ac == null){
                addError("Expected value but get null." + cellId);
            } else {
                boolean fail = false;
                if(ex instanceof BigDecimal){
                    BigDecimal exBD = (BigDecimal)ex;
                    BigDecimal acBD = (BigDecimal)ac;
                    fail = exBD.compareTo(acBD) != 0 && (exBD.add(allowedDivergence).compareTo(acBD) < 0 || exBD.subtract(allowedDivergence).compareTo(acBD) > 0);
                } else if(ex instanceof BigInteger){
                    BigInteger exBI = (BigInteger)ex;
                    BigInteger acBI = (BigInteger)ac;
                    fail = exBI.compareTo(acBI) != 0;
                } else if(ex instanceof Float){
                    float exF = (Float)ex;
                    float acF = (Float)ac;
                    fail = (exF != acF) && (exF + allowedDivergence.floatValue() < acF || exF - allowedDivergence.floatValue() > acF);
                } else if(ex instanceof Double){
                    double exD = (Double)ex;
                    double acD = (Double)ac;
                    fail = (exD != acD) && (exD + allowedDivergence.doubleValue() < acD || exD - allowedDivergence.doubleValue() > acD);
                } else if(ex instanceof Number){
                    fail = ((Number)ex).longValue() != ((Number)ac).longValue();
                } else if(ex instanceof Boolean){
                    fail = ((Boolean)ex).booleanValue() != ((Boolean)ac).booleanValue();
                } else if (ex instanceof Pattern)  {
                    fail = !((Pattern) ex).matcher(ac.toString()).matches();
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
                                    int offset = Math.max(0, c - 10);
                                    int count = Math.min(Math.min(Math.min(c + offset + 10, v1.length - c + 10), 20), v1.length);
                                    addError("Actual and expected value are different at position " + c + ". Actual: [..."
                                            + new String(v2, offset, count) + "...], expected: [..."
                                            + new String(v1, offset, count) + "...]. " + cellId);
                                    break;
                                }
                            }
                        }
                    }
                }
                if(fail){
                    addError("Actual and expected value are different. Actual: [" + ac + "], expected: [" + ex + "]. " + cellId);
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
    public void clear(){
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
            if(o1 == null){
                return -1;
            }
            if(o2 == null){
                return 1;
            }
            if((o1.getClass() == o2.getClass()) && (o1 instanceof Comparable)){
                return ((Comparable)o1).compareTo(o2);
            }
            return o1.toString().compareTo(o2.toString());
        }
    }
}
