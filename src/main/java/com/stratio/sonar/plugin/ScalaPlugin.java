package com.stratio.sonar.plugin;

import java.util.List;

import org.sonar.api.SonarPlugin;

import com.google.common.collect.ImmutableList;
import com.stratio.sonar.jacoco.JaCoCoScalaExtensions;
import com.stratio.sonar.scala.ScalaSensor;
import com.stratio.sonar.scalastyle.ScalastyleQualityProfile;
import com.stratio.sonar.scalastyle.ScalastyleRepository;
import com.stratio.sonar.scalastyle.ScalastyleSensor;
import com.stratio.sonar.scalastyle.core.Scala;
import com.stratio.sonar.surefire.SurefireScalaExtensions;

public final class ScalaPlugin extends SonarPlugin {
  public List<Object> getExtensions() {
	  ImmutableList.Builder<Object> builder = ImmutableList.builder();
	  builder.add(
			  // Batch
			  ScalaSensor.class,
			  Scala.class,
			  ScalastyleRepository.class,
			  ScalastyleQualityProfile.class,
			  ScalastyleSensor.class
	  );
	  builder.addAll(JaCoCoScalaExtensions.getExtensions());
	  builder.addAll(SurefireScalaExtensions.getExtensions());
	  return builder.build();
  }
}
