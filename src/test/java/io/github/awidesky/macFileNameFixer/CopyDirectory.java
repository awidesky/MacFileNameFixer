package io.github.awidesky.macFileNameFixer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.Normalizer;

import org.junit.jupiter.api.Test;

class CopyDirectory {

	@Test
	void copyDirectory() throws InvocationTargetException, IOException, InterruptedException {
		new FileNameFixer(Normalizer.Form.NFC).copyDirectory();
	}

}
