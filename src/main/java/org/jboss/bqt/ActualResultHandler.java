package org.jboss.bqt;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

/**
 * Class which holds actual result of the query.
 * <p>
 * Holder is reusable.
 *
 * @author Juraj Duráni
 */
public class ActualResultHandler {

    private int updateCount = -1;
    private List<String> columnLabels;
    private List<String> columnTypeNames;
    private List<List<Object>> rows;
    private SQLException originalException;
    private Throwable rootCause;

    /**
     * Decides, whether this holder represents an exception.
     *
     * @return {@code true} if this holder represents an exception, {@code false} otherwise
     */
    public boolean isException(){
        return originalException != null;
    }

    /**
     * Decides, whether this holder represents an update.
     *
     * @return {@code true} if this holder represents an update, {@code false} otherwise
     */
    public boolean isUpdate(){
        return updateCount >= 0;
    }

    /**
     * Decides, whether this holder represents a table.
     *
     * @return {@code true} if this holder represents a table, {@code false} otherwise
     */
    public boolean isResult(){
        return rows != null;
    }

    /**
     * Returns original exception thrown by query.
     *
     * @return original exception or {@code null} if this holder does not represent an exception
     */
    public SQLException getOriginalException() {
        return originalException;
    }

    /**
     * Returns a root cause of original exception thrown by query.
     *
     * @return a root cause of original exception or {@code null} if this holder does not represent an exception
     */
    public Throwable getRootCauseException() {
        return rootCause;
    }

    /**
     * Returns column labels.
     *
     * @return column labels or {@code null} if this holder does not represent a table
     */
    public List<String> getColumnLabels() {
        return columnLabels;
    }

    /**
     * Returns column types.
     *
     * @return column types or {@code null} if this holder does not represent a table
     */
    public List<String> getColumnTypeNames() {
        return columnTypeNames;
    }

    /**
     * Returns rows.
     *
     * @return rows or {@code null} if this holder does not represent a table
     */
    public List<List<Object>> getRows() {
        return rows;
    }

    /**
     * Returns class of original exception.
     *
     * @return class of original exception or {@code null} if this holder does not represent an exception
     */
    public Class<? extends Throwable> getOriginalExceptionClass() {
        return originalException == null ? null : originalException.getClass();
    }

    /**
     * Returns message of original exception.
     *
     * @return message of original exception or {@code null} if this holder does not represent an exception
     */
    public String getOriginalExceptionMessage() {
        return originalException == null ? null : originalException.getMessage();
    }

    /**
     * Returns class of root cause of original exception.
     *
     * @return class of root cause of original exception or {@code null} if this holder does not represent an exception
     */
    public Class<? extends Throwable> getRootCauseExceptionClass() {
        return rootCause == null ? null : rootCause.getClass();
    }

    /**
     * Returns message of root cause of original exception.
     *
     * @return message of root cause of original exception or {@code null} if this holder does not represent an exception
     */
    public String getRootCauseExceptionMessage() {
        return rootCause == null ? null : rootCause.getMessage();
    }

    /**
     * Returns update count.
     *
     * @return update count of the query or {@code -1} if this holder does not represent update query
     */
    public int getUpdateCount() {
        return updateCount;
    }

    /**
     * Builds holder which will represents an exception.
     *
     * @param sqlEx SQL exception
     * @throws IllegalArgumentException if sqlEx is null
     */
    public void buildResult(SQLException sqlEx) throws IllegalArgumentException{
        clear();
        if(sqlEx == null){
            throw new IllegalArgumentException("Exception cannot be null.");
        }
        originalException = sqlEx;
        rootCause = originalException;
        Iterator<Throwable> iter = originalException.iterator();
        while(iter.hasNext()){
            rootCause = iter.next();
        }
    }

    /**
     * Builds holder which will represents either a table or an update.
     *
     * @param s {@link Statement} which contains {@link ResultSet} of the query or update count
     * @throws SQLException if s throws an exception
     * @throws IllegalArgumentException is s is null or closed
     */
    public void buildResult(Statement s) throws SQLException, IllegalArgumentException{
        clear();
        if(s == null){
            throw new IllegalArgumentException("Statement cannot be null.");
        }
        if(s.isClosed()){
            throw new IllegalArgumentException("Statement is closed.");
        }
        ResultSet rs = s.getResultSet();
        if(rs == null){
            updateCount = s.getUpdateCount();
        } else {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            columnLabels = new ArrayList<String>(columnCount);
            columnTypeNames = new ArrayList<String>(columnCount);
            for(int i = 1; i <= columnCount; i++){
                columnLabels.add(md.getColumnLabel(i));
                columnTypeNames.add(md.getColumnTypeName(i));
            }
            rows = new LinkedList<List<Object>>();
            while(rs.next()){
                List<Object> row = new ArrayList<Object>(columnCount);
                for(int i = 1; i <= columnCount; i++){
                    Object o = rs.getObject(i);
                    if(o instanceof Clob){
                        o = ((Clob) o).getSubString(1l, (int)((Clob) o).length());
                    } else if(o instanceof Blob){
                        Blob b = (Blob)o;
                        o = Base64.encodeBase64String(b.getBytes(1l, (int)b.length()));
                    } else if(o instanceof SQLXML){
                        o = ((SQLXML)o).getString();
                    } else if(o instanceof byte[]){
                        o = Base64.encodeBase64String((byte[]) o);
                    } else if (o instanceof Byte[]){
                        o = Base64.encodeBase64String(toPrimitive((Byte[]) o));
                    }
                    row.add(o);
                }
                rows.add(row);
            }
        }
    }

    /**
     * Converts array to its primitive equivalent.
     *
     * @param in input array
     * @return primitive array
     */
    private byte[] toPrimitive(Byte[] in){
        byte[] out = new byte[in.length];
        for(int i = 0; i < in.length; i++){
            out[i] = in[i].byteValue();
        }
        return out;
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
        originalException = null;
        rootCause = null;
    }

    @Override
    public String toString() {
        return "Exception: " + isException() + ", result: " + isResult() + ", update: " + isUpdate();
    }
}
