package org.whipper.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities class.
 */
public class Utils{

    public static final Logger LOG = LoggerFactory.getLogger("org.whipper.UI");

    /**
     * Private constructor.
     */
    private Utils(){}

    /**
     * Deletes file or directory recursively.
     *
     * @param f file or directory to be deleted
     */
    public static void delete(File f){
        if(f == null || !f.exists()){
            return;
        }
        if(f.isDirectory()){
            for(File sub : f.listFiles()){
                delete(sub);
            }
        }
        f.delete();
    }

    /**
     * Creates ZIP of the file.
     *
     * @param f file or directory to be zipped
     * @param out output file
     * @throws IOException in case of I/O error
     */
    public static void zip(File f, File out) throws IOException{
        try(FileOutputStream fos = new FileOutputStream(out);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ZipOutputStream zos = new ZipOutputStream(bos)){
            addToZip(f, f.getAbsolutePath().length(), zos, new byte[4096]);
        }
    }

    /**
     * Adds file or directory to the ZIP output stream.
     *
     * @param f file or directory to be added
     * @param truncate truncate name of the file
     * @param os output stream
     * @param buff buffer to use in I/O operations
     * @throws IOException in case of I/O error
     */
    private static void addToZip(File f, int truncate, ZipOutputStream os, byte[] buff) throws IOException{
        if(f.isFile()){
            os.putNextEntry(new ZipEntry(f.getAbsolutePath().substring(truncate)));
            try(FileInputStream fis = new FileInputStream(f);
                    BufferedInputStream bis = new BufferedInputStream(fis)){
                int read;
                while((read = bis.read(buff)) != -1){
                    os.write(buff, 0, read);
                }
            }
            os.closeEntry();
        } else {
            for(File sub : f.listFiles()){
                addToZip(sub, truncate, os, buff);
            }
        }
    }
}
