package com.stratio.sonar.surefire;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.surefire.data.SurefireStaxHandler;
import org.sonar.plugins.surefire.data.UnitTestClassReport;
import org.sonar.plugins.surefire.data.UnitTestIndex;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;

public abstract class AbstractSurefireScalaParser {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSurefireScalaParser.class);
	
	public void collect(SensorContext context, File reportsDir, FileSystem myFS) {
		File[] xmlFiles = getReports(reportsDir);
		if (xmlFiles.length > 0) {
			parseFiles(context, xmlFiles, myFS);
		} else {
			LOGGER.warn("No Unit Test information will be saved, because no Unit Test report has been found in the given directory: {}" + reportsDir.getAbsolutePath());
		}
	}
	private File[] getReports(File dir) {
		if (dir == null) {
			return new File[0];
		} else if (!dir.isDirectory()) {
			LOGGER.warn("Reports path not found: " + dir.getAbsolutePath());
			return new File[0];
		}
		File[] unitTestResultFiles = findXMLFilesStartingWith(dir, "TEST-");
		if (unitTestResultFiles.length == 0) {
			// maybe there's only a test suite result file
			unitTestResultFiles = findXMLFilesStartingWith(dir, "TESTS-");
		}
		return unitTestResultFiles;
	}
	
	private File[] findXMLFilesStartingWith(File dir, final String fileNameStart) {
		return dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(fileNameStart) && name.endsWith(".xml");
			}
		});
	}
	
	private void parseFiles(SensorContext context, File[] reports, FileSystem myFS) {
		UnitTestIndex index = new UnitTestIndex();
		parseFiles(reports, index);
		sanitize(index);
		save(index, context, myFS);
	}
	
	private void parseFiles(File[] reports, UnitTestIndex index) {
		SurefireStaxHandler staxParser = new SurefireStaxHandler(index);
		StaxParser parser = new StaxParser(staxParser, false);
		for (File report : reports) {
			try {
				parser.parse(report);
			} catch (XMLStreamException e) {
				throw new IllegalStateException("Fail to parse the Surefire report: " + report, e);
			}
		}
	}

	private void sanitize(UnitTestIndex index) {
		for (String classname : index.getClassnames()) {
			if (StringUtils.contains(classname, "$")) {
				// Surefire reports classes whereas sonar supports files
				String parentClassName = StringUtils.substringBefore(classname, "$");
				index.merge(classname, parentClassName);
			}
		}
	}
	
	private void save(UnitTestIndex index, SensorContext context, FileSystem myFS) {
		for (Map.Entry<String, UnitTestClassReport> entry : index.getIndexByClassname().entrySet()) {
			UnitTestClassReport report = entry.getValue();
			if (report.getTests() > 0) {
				InputFile inputFile = getUnitTestInputFile(entry.getKey(), myFS);
				if (inputFile != null) {
					save(entry.getValue(), inputFile, context);
				}
			}
		}
	}
	
	private void save(UnitTestClassReport report, InputFile inputFile, SensorContext context) {
		double testsCount = report.getTests() - report.getSkipped();
		saveMeasure(context, inputFile, CoreMetrics.SKIPPED_TESTS, report.getSkipped());
		saveMeasure(context, inputFile, CoreMetrics.TESTS, testsCount);
		saveMeasure(context, inputFile, CoreMetrics.TEST_ERRORS, report.getErrors());
		saveMeasure(context, inputFile, CoreMetrics.TEST_FAILURES, report.getFailures());
		saveMeasure(context, inputFile, CoreMetrics.TEST_EXECUTION_TIME, report.getDurationMilliseconds());
		double passedTests = testsCount - report.getErrors() - report.getFailures();
		
		if (testsCount > 0) {
			double percentage = passedTests * 100d / testsCount;
			saveMeasure(context, inputFile, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(percentage));
		}
		saveResults(context, inputFile, report);
	}
	
	private void saveMeasure(SensorContext context, InputFile inputFile, Metric metric, double value) {
		if (!Double.isNaN(value)) {
			context.saveMeasure(inputFile, metric, value);
		}
	}
	
	protected void saveResults(SensorContext context, InputFile inputFile, UnitTestClassReport report) {
		context.saveMeasure(inputFile, new Measure(CoreMetrics.TEST_DATA, report.toXml()));
	}
	
	protected InputFile getUnitTestInputFile(String classKey, FileSystem myFS) {
      String filename = classKey.replace('.', '/') + ".scala";
      InputFile sonarFile = myFS.inputFile(myFS.predicates().matchesPathPattern("**/" + filename));
      return sonarFile;
    }
}
