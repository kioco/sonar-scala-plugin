package com.stratio.sonar.scala;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.stratio.sonar.colorization.ScalaConfiguration;
import com.stratio.sonar.colorization.ScalaHighlighter;
import com.stratio.sonar.plugin.ScalaPlugin;
import com.stratio.sonar.scala.Parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.source.Highlightable;

public class ScalaSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(ScalaSensor.class);

  private Settings settings;
  private FileSystem fileSystem;
  
  private final ResourcePerspectives resourcePerspectives;

  /**
   * Use of IoC to get Settings and FileSystem
   */
  public ScalaSensor(Settings settings, FileSystem fileSystem, ResourcePerspectives resourcePerspectives) {
    this.settings = settings;
    this.fileSystem = fileSystem;
    this.resourcePerspectives = resourcePerspectives;
  }

  // This sensor is executed only when there are Scala files
  public boolean shouldExecuteOnProject(Project project) {
	  return fileSystem.languages().contains(Scala.INSTANCE.getKey());
  }

  public void analyse(Project project, SensorContext sensorContext) {
    // This sensor create a measure for metric SCALA_LINES on each Scala file
    for (InputFile inputFile : fileSystem.inputFiles(fileSystem.predicates().all())) {
      if (inputFile.relativePath().endsWith(".scala")) {

    	Integer lastIndex = inputFile.relativePath().lastIndexOf(File.separator);
    	Integer lastIndexDot = inputFile.relativePath().lastIndexOf(".");
    	String fileName = inputFile.relativePath().substring(lastIndex+1, lastIndexDot);
   	
    	Integer lines = inputFile.lines() - 1;
    	Integer ncloc = 0;
    	Integer classes = 0;
    	Integer functions = 0;
    	Integer cloc = 0;
		try {
			Parser result = new Parser(inputFile);
			ncloc = result.getNCLOC();
			classes = result.getClasses();
			functions = result.getFunctions();
			cloc = result.getCLOC();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (ncloc > 0) {
			sensorContext.saveMeasure(inputFile, CoreMetrics.NCLOC, ncloc.doubleValue());
		}
		if (classes > 0) {
			sensorContext.saveMeasure(inputFile, CoreMetrics.CLASSES, classes.doubleValue());
		}
    	if (functions > 0) {
    		sensorContext.saveMeasure(inputFile, CoreMetrics.FUNCTIONS, functions.doubleValue());
    	}
    	if (cloc > 0) {
    		sensorContext.saveMeasure(inputFile, CoreMetrics.COMMENT_LINES, cloc.doubleValue());
    	}
    	
    	// Highlighting scala code
    	highlight(inputFile);
    	
      }
    }
  }
  
  private void highlight(InputFile inputFile) {
	  ScalaHighlighter highlighter = new ScalaHighlighter(createConfiguration());
	  
	  Highlightable perspective = resourcePerspectives.as(Highlightable.class, inputFile);
	  
	  if (perspective != null) {
		  highlighter.highlight(perspective, inputFile.file());
      } else {
        LOG.warn("Could not get " + Highlightable.class.getCanonicalName() + " for " + inputFile.file());  
	  }
  }
  
  private ScalaConfiguration createConfiguration() {
	  return new ScalaConfiguration(fileSystem.encoding());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
