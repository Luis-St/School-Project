package net.vgc.test.data.tag.tags.numeric;

import java.io.File;
import java.nio.file.Path;

import net.vgc.data.tag.Tag;
import net.vgc.data.tag.tags.numeric.ByteTag;
import net.vgc.test.IVGTest;
import net.vgc.test.VGCTest;
import net.vgc.test.VGCTestMain;

@VGCTest
public class ByteTagTest implements IVGTest {
	
	protected final Path path = new File("test/tag/numeric/byte_test.txt").toPath();
	
	@Override
	public void start() throws Exception {
		ByteTag tag = ByteTag.valueOf((byte) 10);
		Tag.write(VGCTestMain.resourceDir.resolve(this.path), tag);
		LOGGER.debug("{}", tag);
	}

	@Override
	public void stop() throws Exception {
		Tag tag = Tag.load(VGCTestMain.resourceDir.resolve(this.path));
		LOGGER.debug("{}", tag);
	}

}
