package com.stratio.sonar.jacoco;

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.scan.filesystem.PathResolver;
import java.io.File;
import java.util.Collection;

public class JaCoCoScalaItSensor implements Sensor {
	
	private final JaCoCoScalaConfiguration configuration;
	private final ResourcePerspectives perspectives;
	private final FileSystem fileSystem;
	private final PathResolver pathResolver;
	
	public JaCoCoScalaItSensor(JaCoCoScalaConfiguration configuration, ResourcePerspectives perspectives, FileSystem fileSystem, PathResolver pathResolver) {
		this.configuration = configuration;
		this.perspectives = perspectives;
		this.fileSystem = fileSystem;
		this.pathResolver = pathResolver;
	}
	
	@Override
	public boolean shouldExecuteOnProject(Project project) {
		File report = pathResolver.relativeFile(fileSystem.baseDir(), configuration.getItReportPath());
		boolean foundReport = report.exists() && report.isFile();
		boolean shouldExecute = configuration.shouldExecuteOnProject(foundReport);
		if (!foundReport && shouldExecute) {
			JaCoCoScalaExtensions.LOG.info("JaCoCoScalaItSensor: JaCoCo IT report not found.");
		}
		return shouldExecute;
	}
	
	@Override
	public void analyse(Project project, SensorContext context) {
		new ITAnalyzer(perspectives).analyse(project, context);
	}
	
	class ITAnalyzer extends ScalaAbstractAnalyzer {
		public ITAnalyzer(ResourcePerspectives perspectives) {
			super(perspectives, fileSystem, pathResolver);
		}
		
		@Override
		protected String getReportPath(Project project) {
			return configuration.getItReportPath();
		}
		
		@Override
		protected void saveMeasures(SensorContext context, Resource resource, Collection<Measure> measures) {
			for (Measure measure : measures) {
				Measure itMeasure = convertForIT(measure);
				if (itMeasure != null) {
					context.saveMeasure(resource, itMeasure);
				}
			}
		}
		
		private Measure convertForIT(Measure measure) {
			Measure itMeasure = null;
			if (CoreMetrics.LINES_TO_COVER.equals(measure.getMetric())) {
				itMeasure = new Measure(CoreMetrics.IT_LINES_TO_COVER, measure.getValue());
			} else if (CoreMetrics.UNCOVERED_LINES.equals(measure.getMetric())) {
				itMeasure = new Measure(CoreMetrics.IT_UNCOVERED_LINES, measure.getValue());
			} else if (CoreMetrics.COVERAGE_LINE_HITS_DATA.equals(measure.getMetric())) {
				itMeasure = new Measure(CoreMetrics.IT_COVERAGE_LINE_HITS_DATA, measure.getData());
			} else if (CoreMetrics.CONDITIONS_TO_COVER.equals(measure.getMetric())) {
				itMeasure = new Measure(CoreMetrics.IT_CONDITIONS_TO_COVER, measure.getValue());
			} else if (CoreMetrics.UNCOVERED_CONDITIONS.equals(measure.getMetric())) {
				itMeasure = new Measure(CoreMetrics.IT_UNCOVERED_CONDITIONS, measure.getValue());
			} else if (CoreMetrics.COVERED_CONDITIONS_BY_LINE.equals(measure.getMetric())) {
				itMeasure = new Measure(CoreMetrics.IT_COVERED_CONDITIONS_BY_LINE, measure.getData());
			} else if (CoreMetrics.CONDITIONS_BY_LINE.equals(measure.getMetric())) {
				itMeasure = new Measure(CoreMetrics.IT_CONDITIONS_BY_LINE, measure.getData());
			}
			return itMeasure;
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
