package io.github.awidesky.macFileNameFixer;

import java.awt.Desktop;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class FileNameFixer {

	private final Normalizer.Form form;
	
	public FileNameFixer(Normalizer.Form form) {
		this.form = form;
	}
	
	public String fix(String in) {
        return Normalizer.isNormalized(in, form) ? in : Normalizer.normalize(in, form);
	}

	public void copyDirectory() throws IOException, InvocationTargetException, InterruptedException {
		normalizedWalk(FileNameFixer::copy);
	}

	
	private void normalizedWalk(ThrowingBiPathCosumer job) throws IOException, InvocationTargetException, InterruptedException {
		AtomicReference<Path> atomic_in = new AtomicReference<>(), atomic_out = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> {
	        JFileChooser chooser = new JFileChooser();
	        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	        chooser.setMultiSelectionEnabled(false);

	        JOptionPane.showMessageDialog(null, "Choose Input folder.");
	        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
	        atomic_in.set(chooser.getSelectedFile().toPath());

	        
	        JOptionPane.showMessageDialog(null, "Choose Output folder.");
	        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
	        atomic_out.set(chooser.getSelectedFile().toPath());
		});

		Path inputDir = atomic_in.get(), outputDir = atomic_out.get();
		
        IOException e = Files.walk(inputDir).parallel().map(inputPath -> {
        	String in = inputDir.relativize(inputPath).toString();
        	String out = fix(in);
        	if(in.equals(out)) return null;
        	
        	Path outputPath = outputDir.resolve(out);
        	try {
        		job.run(inputPath, outputPath);
        		return null;
        	} catch (IOException ex) {
        		return ex;
        	}
        })
        .filter(Objects::nonNull)
        .findAny().orElse(null);
        
        if(e != null) throw e;
        
        SwingUtilities.invokeLater(() -> {
        	JOptionPane.showMessageDialog(null, "Copy complete!");
            try {
				Desktop.getDesktop().open(outputDir.toFile());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        });
	}
	
	
	private static void copy(Path inputPath, Path outputPath) throws IOException {
		if (Files.isDirectory(outputPath)) {
			if(!Files.exists(outputPath)) Files.createDirectories(outputPath);
		} else {
			if(!Files.exists(outputPath.getParent())) Files.createDirectories(outputPath.getParent());
			Files.copy(inputPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
		}
	}

}

@FunctionalInterface
interface ThrowingBiPathCosumer {
	public void run(Path input, Path output) throws IOException;
}
