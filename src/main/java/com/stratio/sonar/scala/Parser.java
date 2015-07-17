package com.stratio.sonar.scala;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;

public class Parser {
	private static final Logger LOG = LoggerFactory.getLogger(ScalaSensor.class);
	
	private Integer ncloc;
	private Integer classes;
	private Integer functions;
	private Integer cloc;
	private Integer tests;

	public Parser(InputFile inputFile) throws IOException {
		Integer ncloc = 0;
		Integer classes = 0;
		Integer functions = 0;
		Integer cloc = 0;
		FileReader fr = new FileReader(inputFile.absolutePath());
		BufferedReader br = new BufferedReader(fr);
		Boolean mlcomment = false;
		
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (!line.isEmpty()) {
				if (!mlcomment) {
					// Non-comment line
					if ((!line.startsWith("//")) && (!line.startsWith("/*")) && (!line.isEmpty())) {
						ncloc += 1;
					}
			  
					// Class
					if (line.startsWith("class ")) {
						classes += 1;
					}
				
					// Method/function
					if (line.startsWith("def ")) {
						functions += 1;
					}

					// Comment
					if (line.startsWith("//")) {
						cloc += 1;
					}
				
					// Multi-line comment
					if (line.startsWith("/*")) {
						cloc += 1;
						mlcomment = true;
					}
				} else {
					cloc += 1;
					if (line.endsWith("*/")) {
						mlcomment = false;
					}
				}
			}
		}
		  
		this.ncloc = ncloc;
		this.classes = classes;
		this.functions = functions;
		this.cloc = cloc;
	}
	
	public Integer getNCLOC() {
		return this.ncloc;
	}
	
	public Integer getClasses() {
		return this.classes;
	}
	
	public Integer getFunctions() {
		return this.functions;
	}
	
	public Integer getCLOC() {
		return this.cloc;
	}
	
	public Integer getTests() {
		return this.tests;
	}
}
