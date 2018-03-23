package org.whipper;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.whipper.utils.OverrideFileSelector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OverrideFileSelectorTest {

    static File baseDir;
    static File testFileinBase;
    File testFileinOverride;
    static File testDir;
    static File testFileinBaseSameName;
    static File[] overrideDirs;
    static File[] overrideDirsFiles;
    static File[] overrideDirsFilesSameName;
    static List<File> directories = new ArrayList<>();
    static OverrideFileSelector testObj;

    @BeforeAll
    public static void setUp() throws IOException {
        baseDir = Files.createTempDirectory("baseDir").toFile();
        baseDir.deleteOnExit();
        testFileinBase = new File(baseDir, "baseFile");
        testFileinBase.createNewFile();
        testFileinBase.deleteOnExit();

        testFileinBaseSameName = new File(baseDir, "sameFileName");
        testFileinBaseSameName.createNewFile();
        testFileinBaseSameName.deleteOnExit();

        overrideDirs = new File[3];
        overrideDirsFiles = new File[3];
        overrideDirsFilesSameName = new File[3];
        for (int i = 0; i < 3; i++) {
            overrideDirs[i] = Files.createTempDirectory("overrideDir").toFile();
            overrideDirs[i].deleteOnExit();
            overrideDirsFiles[i] = new File(overrideDirs[i], "File" + i);
            overrideDirsFiles[i].createNewFile();
            overrideDirsFiles[i].deleteOnExit();
            overrideDirsFilesSameName[i] = new File(overrideDirs[i], "sameFileName");
            overrideDirsFilesSameName[i].createNewFile();
            overrideDirsFilesSameName[i].deleteOnExit();
        }

        testDir = Files.createTempDirectory(Paths.get(baseDir.getPath()), "DirFile").toFile();
        testDir.deleteOnExit();
        directories.add(baseDir);
        directories.addAll(Arrays.asList(overrideDirs));
        testObj = new OverrideFileSelector(directories);
    }

    @Test
    public void findFileInBaseDirTest() {
        assertEquals(testObj.getExpectedResultFile(testFileinBase.getName()), testFileinBase,
            "unexpected file returned");
    }

    @Test
    public void findFileNonExistentFileTest() {
        assertEquals(testObj.getExpectedResultFile("madeUpFile"), null, "unexpected value returned");
    }

    @Test
    public void findFileInOverrideDirTest() {
        for (File file : overrideDirsFiles) {
            assertEquals(testObj.getExpectedResultFile(file.getName()), file, "unexpected file returned");
        }
    }

    @Test
    public void findFileIfDirTest() {
        assertEquals(testObj.getExpectedResultFile(testDir.getName()), null, "unexpected file returned");
    }

    @Test
    public void findFileSameNameInDirsTest() {
        assertEquals(testObj.getExpectedResultFile("sameFileName"), overrideDirsFilesSameName[2],
            "unexpected file returned");
    }
}