package com.stratio.sonar.surefire;

import java.util.List;

import org.sonar.api.BatchExtension;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Qualifiers;

import com.google.common.collect.ImmutableList;
import com.stratio.sonar.scala.Scala;

public class SurefireScalaConfiguration implements BatchExtension {
	
	public static final String SUREFIRE_REPORTS_PATH_PROPERTY = "sonar.scala.junit.reportsPath";
	public static final String SUREFIRE_REPORTS_PATH_DEFAULT_VALUE = "target/surefire-reports/";
	private final Settings settings;
	private final FileSystem fileSystem;

	public SurefireScalaConfiguration(Settings settings, FileSystem fileSystem) {
		this.settings = settings;
		this.fileSystem = fileSystem;
	}
	
	public boolean shouldExecuteOnProject(boolean reportFound) {
		return hasScalaFiles() && reportFound;
	}
	
	private boolean hasScalaFiles() {
		return fileSystem.hasFiles(fileSystem.predicates().hasLanguage(Scala.INSTANCE.getKey()));
	}
	
	public String getSurefireReportsPath() {
		return settings.getString(SUREFIRE_REPORTS_PATH_PROPERTY);
	}
	
	public static List<PropertyDefinition> getPropertyDefinitions() {
		String jUnitSubCategory = "JUnitScala";
		return ImmutableList
				.of(
						PropertyDefinition.builder(SurefireScalaConfiguration.SUREFIRE_REPORTS_PATH_PROPERTY)
						.defaultValue(SurefireScalaConfiguration.SUREFIRE_REPORTS_PATH_DEFAULT_VALUE)
						.category(CoreProperties.CATEGORY_JAVA)
						.subCategory(jUnitSubCategory)
						.name("Surefire Report")
						.description("Path to the Surefire report file. The path may be absolute or relative to the project base directory.")
						.onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
						.build()
						);
	}
	
}
