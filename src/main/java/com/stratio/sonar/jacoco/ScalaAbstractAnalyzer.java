package com.stratio.sonar.jacoco;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

import org.apache.commons.lang.StringUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.test.MutableTestCase;
import org.sonar.api.test.MutableTestPlan;
import org.sonar.api.test.MutableTestable;
import org.sonar.api.test.Testable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;

import static com.google.common.collect.Lists.newArrayList;

public abstract class ScalaAbstractAnalyzer {
	private final ResourcePerspectives perspectives;
	private final FileSystem fileSystem;
	private final PathResolver pathResolver;
	private final boolean readCoveragePerTests;
	private Map<String, File> classFilesCache;
	
	public ScalaAbstractAnalyzer(ResourcePerspectives perspectives, FileSystem fileSystem,
			PathResolver pathResolver) {
		this(perspectives, fileSystem, pathResolver, true);
	}
	
	public ScalaAbstractAnalyzer(ResourcePerspectives perspectives, FileSystem fileSystem,
			PathResolver pathResolver, boolean readCoveragePerTests) {
		this.perspectives = perspectives;
		this.fileSystem = fileSystem;
		this.pathResolver = pathResolver;
		this.readCoveragePerTests = readCoveragePerTests;
	}
	
	private static String fullyQualifiedClassName(String packageName, String simpleClassName) {
		return ("".equals(packageName) ? "" : (packageName + "/")) + StringUtils.substringBeforeLast(simpleClassName, ".");
	}
	
	private Resource getResource(ISourceFileCoverage coverage, SensorContext context) {
		String fileRelativePath = coverage.getPackageName() + "/" + coverage.getName();
		// Only take into account scala files
		// Java files will be analyzed by jacoco sensor itself.
		if (fileRelativePath.endsWith(".scala")) {
			Resource resourceInContext = getResource(fileRelativePath, context);
			if (resourceInContext != null && ResourceUtils.isUnitTestClass(resourceInContext)) {
				// Ignore unit tests
				return null;
			}
			return resourceInContext;
		}
		return null;		
	}
	
	@CheckForNull
	private Resource getResource(String fileRelativePath, SensorContext context) {
		for (InputFile sourceDir : fileSystem.inputFiles(fileSystem.predicates().hasType(InputFile.Type.MAIN))) {
			String absolutePath = sourceDir.absolutePath();
			if (absolutePath.endsWith(fileRelativePath)) {
				InputFile scalaFile = fileSystem.inputFile(fileSystem.predicates().hasAbsolutePath(sourceDir.absolutePath()));
				if (scalaFile != null) {
					return context.getResource(scalaFile);
				}
			}
		}
		return null;
	}
	
	public final void analyse(Project project, SensorContext context) {
		if (!atLeastOneBinaryDirectoryExists(project)) {
			JaCoCoScalaExtensions.LOG.warn("Project coverage is set to 0% since there is no directories with classes.");
			return;
		}
		String path = getReportPath(project);
		if (path == null) {
			JaCoCoScalaExtensions.LOG.warn("No jacoco coverage execution file found for project " + project.getName() + ".");
			return;
		}
		File jacocoExecutionData = pathResolver.relativeFile(fileSystem.baseDir(), path);
		try {
			readExecutionData(jacocoExecutionData, context);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean atLeastOneBinaryDirectoryExists(Project project) {
		java.io.File binariesDir = new File(fileSystem.baseDir() + "/target/classes");
		
		if (binariesDir.exists()) {
			return true;
		} else {
			JaCoCoScalaExtensions.LOG.warn("No binary directories defined for project " + project.getName() + ".");
		}
		
		return false;
	}
	
	public final void readExecutionData(File jacocoExecutionData, SensorContext context) throws IOException {
//		int analyzedResources = 0;
//		boolean collectedCoveragePerTest = false;

		ScalaExecutionDataVisitor executionDataVisitor = new ScalaExecutionDataVisitor();
		if (jacocoExecutionData == null || !jacocoExecutionData.exists() || !jacocoExecutionData.isFile()) {
			JaCoCoScalaExtensions.LOG.warn("Project coverage is set to 0% as no JaCoCo execution data has been dumped: {}", jacocoExecutionData);
		} else {
			JaCoCoScalaExtensions.LOG.info("Analysing {}", jacocoExecutionData);
			InputStream inputStream = null;
			try {
				inputStream = new BufferedInputStream(new FileInputStream(jacocoExecutionData));
				ExecutionDataReader reader = new ExecutionDataReader(inputStream);
				reader.setSessionInfoVisitor(executionDataVisitor);
				reader.setExecutionDataVisitor(executionDataVisitor);
				reader.read();
			} finally {
				Closeables.closeQuietly(inputStream);
			}
		}

		// We do not analyze coverage per test for overall, as this will cause
		// problems with duplicated information for certain files.
		//if (!jacocoExecutionData.toString().endsWith("jacoco-overall.exec")) {
			CoverageBuilder coverageBuilder = analyze(executionDataVisitor.getMerged());
			boolean collectedCoveragePerTest = readCoveragePerTests(context, executionDataVisitor);
			//collectedCoveragePerTest = readCoveragePerTests(context, executionDataVisitor);
			int analyzedResources = 0;
			for (ISourceFileCoverage coverage : coverageBuilder.getSourceFiles()) {
				Resource resource = getResource(coverage, context);
				if (resource != null) {
					CoverageMeasuresBuilder builder = analyzeFile(resource, coverage);
					saveMeasures(context, resource, builder.createMeasures());
					analyzedResources++;
				}
			}
		//}
		
		if (analyzedResources == 0) {
			JaCoCoScalaExtensions.LOG.warn("Coverage information was not collected. Perhaps you forget to include debug information into compiled classes?");
		} else if (collectedCoveragePerTest) {
			JaCoCoScalaExtensions.LOG.info("Information about coverage per test has been collected.");
		} else if (jacocoExecutionData != null) {
			JaCoCoScalaExtensions.LOG.info("No information about coverage per test.");
		}
		
	}

	private CoverageBuilder analyze(ExecutionDataStore executionDataStore) {
		CoverageBuilder coverageBuilder = new CoverageBuilder();
		Analyzer analyzer = new Analyzer(executionDataStore, coverageBuilder);
		java.io.File binariesDir = new File(fileSystem.baseDir() + "/target/classes");
		if (binariesDir.exists()) {
			analyzeAll(analyzer, binariesDir);
		}
		return coverageBuilder;
	}
	
	/**
	 * Copied from {@link Analyzer#analyzeAll(File)} in order to add logging.
	 */
	private void analyzeAll(Analyzer analyzer, File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				analyzeAll(analyzer, f);
			}
		} else if (file.getName().endsWith(".class")) {
			try {
				analyzer.analyzeAll(file);
			} catch (Exception e) {
				JaCoCoScalaExtensions.LOG.warn("Exception during analysis of file " + file.getAbsolutePath(), e);
			}
		}
	}
	
	private boolean readCoveragePerTests(SensorContext context, ScalaExecutionDataVisitor executionDataVisitor) {
		boolean collectedCoveragePerTest = false;
		if (readCoveragePerTests) {
			for (Map.Entry<String, ExecutionDataStore> entry : executionDataVisitor.getSessions().entrySet()) {
				if (analyzeLinesCoveredByTests(entry.getKey(), entry.getValue(), context)) {
					collectedCoveragePerTest = true;
				}
			}
		}
		return collectedCoveragePerTest;
	}
	
	private boolean analyzeLinesCoveredByTests(String sessionId, ExecutionDataStore executionDataStore, SensorContext context) {
		int i = sessionId.indexOf(' ');
		if (i < 0) {
			return false;
		}
		String testClassName = sessionId.substring(0, i);
		String testName = sessionId.substring(i + 1);
		
		String name = testClassName.replace('.', '/');
		
		
		//InputFile scalaFile = fileSystem.inputFile(fileSystem.predicates().hasRelativePath(name));
		String path = "**/"+name+".*";
		InputFile scalaFile = fileSystem.inputFile(fileSystem.predicates().matchesPathPattern(path));
		
		
		Resource testResource = null;
		if (scalaFile != null) {
			testResource = context.getResource(scalaFile);
		}
		if (testResource == null) {
			// No such test class
			return false;
		}
		boolean result = false;
		CoverageBuilder coverageBuilder = analyze(executionDataStore);
		for (ISourceFileCoverage coverage : coverageBuilder.getSourceFiles()) {
			Resource resource = getResource(coverage, context);
			if (resource != null) {
				CoverageMeasuresBuilder builder = analyzeFile(resource, coverage);
				List<Integer> coveredLines = getCoveredLines(builder);
				if (!coveredLines.isEmpty() && addCoverage(resource, testResource, testName, coveredLines)) {
					result = true;
				}
			}
		}
		return result;
	}
	
	private static List<Integer> getCoveredLines(CoverageMeasuresBuilder builder) {
		List<Integer> linesCover = newArrayList();
		for (Map.Entry<Integer, Integer> hitsByLine : builder.getHitsByLine().entrySet()) {
			if (hitsByLine.getValue() > 0) {
				linesCover.add(hitsByLine.getKey());
			}
		}
		return linesCover;
	}
	
	private boolean addCoverage(Resource resource, Resource testFile, String testName, List<Integer> coveredLines) {
		boolean result = false;
		Testable testAbleFile = perspectives.as(MutableTestable.class, resource);
		if (testAbleFile != null) {
			MutableTestPlan testPlan = perspectives.as(MutableTestPlan.class, testFile);
			if (testPlan != null) {
				for (MutableTestCase testCase : testPlan.testCasesByName(testName)) {
					testCase.setCoverageBlock(testAbleFile, coveredLines);
					result = true;
				}
			}
		}
		return result;
	}
	
	private static CoverageMeasuresBuilder analyzeFile(Resource resource, ISourceFileCoverage coverage) {
		CoverageMeasuresBuilder builder = CoverageMeasuresBuilder.create();
		for (int lineId = coverage.getFirstLine(); lineId <= coverage.getLastLine(); lineId++) {
			final int hits;
			ILine line = coverage.getLine(lineId);
			switch (line.getInstructionCounter().getStatus()) {
			case ICounter.FULLY_COVERED:
			case ICounter.PARTLY_COVERED:
				hits = 1;
				break;
			case ICounter.NOT_COVERED:
				hits = 0;
				break;
			case ICounter.EMPTY:
				continue;
			default:
				JaCoCoScalaExtensions.LOG.warn("Unknown status for line {} in {}", lineId, resource);
				continue;
			}
			builder.setHits(lineId, hits);
			ICounter branchCounter = line.getBranchCounter();
			int conditions = branchCounter.getTotalCount();
			if (conditions > 0) {
				int coveredConditions = branchCounter.getCoveredCount();
				builder.setConditions(lineId, conditions, coveredConditions);
			}
		}
		return builder;
	}
	
	protected abstract void saveMeasures(SensorContext context, Resource resource, Collection<Measure> measures);
	
	protected abstract String getReportPath(Project project);
}