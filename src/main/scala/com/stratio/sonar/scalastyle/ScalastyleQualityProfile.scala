/*
 * Sonar Scalastyle Plugin
 * Copyright (C) 2014 All contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.stratio.sonar.scalastyle

import org.slf4j.LoggerFactory
import org.sonar.api.profiles.{ProfileDefinition, RulesProfile}
import org.sonar.api.rules.ActiveRule
import org.sonar.api.utils.ValidationMessages
import scala.collection.JavaConversions._
import org.scalastyle.ScalastyleError
import scala.xml.XML

import org.sonar.api.batch.SensorContext;
import org.sonar.api.server.rule.RulesDefinition.Context
import org.sonar.api.server.rule.RulesDefinition.Rule
import org.sonar.api.rules.Rule
import org.sonar.api.profiles.XMLProfileParser

/**
 * This class creates the default "Scalastyle" quality profile from Scalastyle's default_config.xml
 */
class ScalastyleQualityProfile(scalastyleRepository: ScalastyleRepository, xmlProfileParser: XMLProfileParser) extends ProfileDefinition {
  
  private val log = LoggerFactory.getLogger(classOf[ScalastyleRepository])
  private val defaultConfigRules = xmlFromClassPath("/default_config.xml") \\ "scalastyle" \ "check"
   
  override def createProfile(validation: ValidationMessages): RulesProfile = {
    val profile = xmlProfileParser.parseResource(getClass().getClassLoader, "scalastyle-profile.xml", validation)
    profile.setDefaultProfile(true)
    profile
  }

  private def xmlFromClassPath(s: String) = XML.load(classOf[ScalastyleError].getResourceAsStream(s))
}
