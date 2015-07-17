package com.stratio.sonar.jacoco;

import com.google.common.collect.ImmutableList;
import com.stratio.sonar.scala.Scala;

import org.sonar.api.BatchExtension;
import org.sonar.api.CoreProperties;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Qualifiers;
import java.util.List;

public class JaCoCoScalaConfiguration implements BatchExtension {
	
	public static final String REPORT_PATH_PROPERTY = "sonar.scala.jacoco.reportPath";
	public static final String REPORT_PATH_DEFAULT_VALUE = "target/jacoco.exec";
	public static final String IT_REPORT_PATH_PROPERTY = "sonar.scala.jacoco.itReportPath";
	public static final String IT_REPORT_PATH_DEFAULT_VALUE = "target/jacoco-it.exec";
	public static final String REPORT_MISSING_FORCE_ZERO = "sonar.scala.jacoco.reportMissing.force.zero";
	public static final boolean REPORT_MISSING_FORCE_ZERO_DEFAULT_VALUE = false;
	private final Settings settings;
	private final FileSystem fileSystem;
	
	public JaCoCoScalaConfiguration(Settings settings, FileSystem fileSystem) {
		this.settings = settings;
		this.fileSystem = fileSystem;
	}
	
	public boolean shouldExecuteOnProject(boolean reportFound) {
		return hasScalaFiles() && (reportFound || isCoverageToZeroWhenNoReport());
	}
	
	private boolean hasScalaFiles() {
		return fileSystem.hasFiles(fileSystem.predicates().hasLanguage(Scala.INSTANCE.getKey()));
	}
	
	public String getReportPath() {
		return settings.getString(REPORT_PATH_PROPERTY);
	}
	
	public String getItReportPath() {
		return settings.getString(IT_REPORT_PATH_PROPERTY);
	}
	
	private boolean isCoverageToZeroWhenNoReport() {
		return settings.getBoolean(REPORT_MISSING_FORCE_ZERO);
	}
	
	public static List<PropertyDefinition> getPropertyDefinitions() {
		String jacocoSubCategory = "JaCoCoScala";
		return ImmutableList
				.of(
						PropertyDefinition.builder(JaCoCoScalaConfiguration.REPORT_PATH_PROPERTY)
						.defaultValue(JaCoCoScalaConfiguration.REPORT_PATH_DEFAULT_VALUE)
						.category(CoreProperties.CATEGORY_JAVA)
						.subCategory(jacocoSubCategory)
						.name("UT JaCoCo Report")
						.description("Path to the JaCoCo report file containing coverage data by unit tests. The path may be absolute or relative to the project base directory.")
						.onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
						.build(),
						PropertyDefinition.builder(JaCoCoScalaConfiguration.IT_REPORT_PATH_PROPERTY)
						.defaultValue(JaCoCoScalaConfiguration.IT_REPORT_PATH_DEFAULT_VALUE)
						.category(CoreProperties.CATEGORY_JAVA)
						.subCategory(jacocoSubCategory)
						.name("IT JaCoCo Report")
						.description("Path to the JaCoCo report file containing coverage data by integration tests. The path may be absolute or relative to the project base directory.")
						.onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
						.build(),
						PropertyDefinition.builder(JaCoCoScalaConfiguration.REPORT_MISSING_FORCE_ZERO)
						.defaultValue(JaCoCoScalaConfiguration.REPORT_MISSING_FORCE_ZERO_DEFAULT_VALUE + "")
						.name("Force zero coverage")
						.category(CoreProperties.CATEGORY_JAVA)
						.subCategory(jacocoSubCategory)
						.description("Force coverage to 0% if no JaCoCo reports are found during analysis.")
						.onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
						.type(PropertyType.BOOLEAN)
						.build()
						);
	}
}
