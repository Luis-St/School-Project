package net.vgc.test.data.tag.tags.numeric;

import java.io.File;
import java.nio.file.Path;

import net.vgc.data.tag.Tag;
import net.vgc.data.tag.tags.numeric.DoubleTag;
import net.vgc.test.IVGTest;
import net.vgc.test.VGCTest;
import net.vgc.test.VGCTestMain;

@VGCTest
public class DoubleTagTest implements IVGTest {
	
	protected final Path path = new File("test/tag/numeric/double_test.txt").toPath();
	
	@Override
	public void start() throws Exception {
		DoubleTag tag = DoubleTag.valueOf((double) 0.0424500450785454);
		Tag.write(VGCTestMain.resourceDir.resolve(this.path), tag);
		LOGGER.debug("{}", tag);
	}

	@Override
	public void stop() throws Exception {
		Tag tag = Tag.load(VGCTestMain.resourceDir.resolve(this.path));
		LOGGER.debug("{}", tag);
	}

}
