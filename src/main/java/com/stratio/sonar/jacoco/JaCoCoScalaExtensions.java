package com.stratio.sonar.jacoco;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class JaCoCoScalaExtensions {
	
	public static final Logger LOG = LoggerFactory.getLogger(JaCoCoScalaExtensions.class.getName());
	
	private JaCoCoScalaExtensions() {
	}
	
	public static List<Object> getExtensions() {
		ImmutableList.Builder<Object> extensions = ImmutableList.builder();
		extensions.addAll(JaCoCoScalaConfiguration.getPropertyDefinitions());
		extensions.add(
				JaCoCoScalaConfiguration.class,
				// Unit tests
				JaCoCoScalaSensor.class,
				// Integration tests
				JaCoCoScalaItSensor.class,
				JaCoCoScalaOverallSensor.class);
		return extensions.build();
	}
}
