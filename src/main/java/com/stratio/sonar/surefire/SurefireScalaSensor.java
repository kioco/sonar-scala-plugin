package com.stratio.sonar.surefire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.CoverageExtension;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;

import com.stratio.sonar.scala.Scala;

import java.io.File;

public class SurefireScalaSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(SurefireScalaSensor.class);

  private SurefireScalaConfiguration configuration;
  private Settings settings;
  private static FileSystem fileSystem;
  private final PathResolver pathResolver;

  /**
   * Use of IoC to get Settings and FileSystem
   */
  public SurefireScalaSensor(Settings settings, FileSystem fileSystem, SurefireScalaConfiguration configuration, PathResolver pathResolver) {
    this.settings = settings;
    this.fileSystem = fileSystem;
    this.configuration = configuration;
    this.pathResolver = pathResolver;
  }
  
  @DependsUpon
  public Class<?> dependsUponCoverageSensors() {
    return CoverageExtension.class;
  }

  public boolean shouldExecuteOnProject(Project project) {
	File report = pathResolver.relativeFile(fileSystem.baseDir(), configuration.getSurefireReportsPath());
	boolean foundReport = report.exists();
	boolean shouldExecute = configuration.shouldExecuteOnProject(foundReport);
	if (!foundReport && shouldExecute) {
	  LOG.info("SurefireScalaSensor: Surefire report not found.");
	}
	return shouldExecute && fileSystem.languages().contains(Scala.INSTANCE.getKey());
  }

  public void analyse(Project project, SensorContext context) {
	String path = configuration.getSurefireReportsPath();
    
    if (path != null) {
      File pathFile = fileSystem.resolvePath(path);
      FileSystem myFS = fileSystem;
      collect(project, context, pathFile, myFS);
    }
  }

  protected void collect(Project project, SensorContext context, File reportsDir, FileSystem myFS) {
    LOG.info("parsing {}", reportsDir);
    SUREFIRE_PARSER.collect(context, reportsDir, myFS);
  }

  private static final AbstractSurefireScalaParser SUREFIRE_PARSER = new AbstractSurefireScalaParser() {
  };

  @Override
  public String toString() {
    return "Scala SurefireSensor";
  }
}
