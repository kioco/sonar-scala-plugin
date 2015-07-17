package com.stratio.sonar.jacoco;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.scan.filesystem.PathResolver;

import java.io.File;
import java.util.Collection;

public class JaCoCoScalaSensor implements Sensor {
	
	private static final Logger LOG = LoggerFactory.getLogger(JaCoCoScalaSensor.class);
	
	private JaCoCoScalaConfiguration configuration;
	private final ResourcePerspectives perspectives;
	private final FileSystem fileSystem;
	private final PathResolver pathResolver;
	
	public JaCoCoScalaSensor(
			JaCoCoScalaConfiguration configuration,
			ResourcePerspectives perspectives,
			FileSystem fileSystem,
			PathResolver pathResolver) {
		this.configuration = configuration;
		this.perspectives = perspectives;
		this.fileSystem = fileSystem;
		this.pathResolver = pathResolver;
	}
	
	@DependsUpon
	public String dependsUponSurefireSensors() {
		return "surefire-java";
	}
	
	@Override
	public void analyse(Project project, SensorContext context) {
		new UnitTestsAnalyzer().analyse(project, context);
	}
	
	@Override
	public boolean shouldExecuteOnProject(Project project) {
		File report = pathResolver.relativeFile(fileSystem.baseDir(), configuration.getReportPath());
		boolean foundReport = report.exists() && report.isFile();
		boolean shouldExecute = configuration.shouldExecuteOnProject(foundReport);
		if (!foundReport && shouldExecute) {
			JaCoCoScalaExtensions.LOG.info("JaCoCoScalaSensor: JaCoCo report not found.");
		}
		return shouldExecute;
	}
	
	class UnitTestsAnalyzer extends ScalaAbstractAnalyzer {
		public UnitTestsAnalyzer() {
			super(perspectives, fileSystem, pathResolver);
		}
		
		@Override
		protected String getReportPath(Project project) {
			return configuration.getReportPath();
		}
		
		@Override
		protected void saveMeasures(SensorContext context, Resource resource, Collection<Measure> measures) {
			for (Measure measure : measures) {
				context.saveMeasure(resource, measure);
			}
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}