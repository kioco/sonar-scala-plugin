package com.stratio.sonar.colorization;

import org.sonar.squidbridge.api.SquidConfiguration;
import java.nio.charset.Charset;

public class ScalaConfiguration extends SquidConfiguration{
	 
	private boolean ignoreHeaderComments;
	 
	 public ScalaConfiguration(Charset charset) {
		 super(charset);
	 }
	 
	 public void setIgnoreHeaderComments(boolean ignoreHeaderComments) {
		 this.ignoreHeaderComments = ignoreHeaderComments;
	 }
	
	 public boolean getIgnoreHeaderComments() {
		 return ignoreHeaderComments;
	 }
}
