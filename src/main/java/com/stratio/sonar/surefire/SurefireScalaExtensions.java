package com.stratio.sonar.surefire;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;


public class SurefireScalaExtensions {
	public static final Logger LOG = LoggerFactory.getLogger(SurefireScalaExtensions.class.getName());
	
	private SurefireScalaExtensions() {
	}
	
	public static List<Object> getExtensions() {
		ImmutableList.Builder<Object> extensions = ImmutableList.builder();
		extensions.addAll(SurefireScalaConfiguration.getPropertyDefinitions());
		extensions.add(
				SurefireScalaConfiguration.class,
				SurefireScalaSensor.class
				);
		return extensions.build();
	}
}
