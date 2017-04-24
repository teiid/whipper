package org.whipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WhipperTest{

    @Test
    public void removeExtensionTest(){
        Assertions.assertAll("Remove extension", () -> Assertions.assertEquals("file", Whipper.removeExtension("file")),
                () -> Assertions.assertEquals("file", Whipper.removeExtension("file.txt")),
                () -> Assertions.assertEquals("file.1", Whipper.removeExtension("file.1.txt")),
                () -> Assertions.assertEquals(".file", Whipper.removeExtension(".file")),
                () -> Assertions.assertEquals(".file", Whipper.removeExtension(".file.txt")),
                () -> Assertions.assertEquals(".file.1", Whipper.removeExtension(".file.1.txt")));
    }
}
