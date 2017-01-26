package org.whipper;

import org.junit.Assert;
import org.junit.Test;

public class WhipperTest {

    @Test public void removeExtensionTestNoExtension(){ Assert.assertEquals(Whipper.removeExtension("file"), "file"); }
    @Test public void removeExtensionTestSimple(){ Assert.assertEquals(Whipper.removeExtension("file.txt"), "file"); }
    @Test public void removeExtensionTestWithDot(){ Assert.assertEquals(Whipper.removeExtension("file.1.txt"), "file.1"); }
    @Test public void removeExtensionTestHiddenNoExtension(){ Assert.assertEquals(Whipper.removeExtension(".file"), ".file"); }
    @Test public void removeExtensionTestHiddenSimple(){ Assert.assertEquals(Whipper.removeExtension(".file.txt"), ".file"); }
    @Test public void removeExtensionTestHiddenWithDot(){ Assert.assertEquals(Whipper.removeExtension(".file.1.txt"), ".file.1"); }
}
