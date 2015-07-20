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
import org.sonar.api.rules._

import scala.collection.JavaConversions._

import org.sonar.api.server.rule.RulesDefinitionXmlLoader
import org.sonar.api.server.rule.RulesDefinition
import org.sonar.api.server.rule.RulesDefinition.Context
import org.sonar.api.server.rule.RulesDefinition.NewRepository
import org.sonar.api.server.rule.RulesDefinition.NewRule
import org.sonar.api.server.rule.RuleParamType
import org.sonar.squidbridge.rules.SqaleXmlLoader

/**
 * Scalastyle rules repository - creates a rule for each checker shipped with Scalastyle based
 * on the scalastyle_definition.xml file that ships with the Scalastyle jar.
 */
class ScalastyleRepository extends RulesDefinition {

  private val log = LoggerFactory.getLogger(classOf[ScalastyleRepository])

  override def define(context: Context) {
    val repository = context.createRepository(Constants.RepositoryKey, Constants.ScalaKey).setName(Constants.ProfileName)
    createRules(context, repository)
    SqaleXmlLoader.load(repository, "/com/sonar/sqale/scalastyle-model.xml")
    repository.done()   
  }
  
  def createRules(context : Context, repository: NewRepository): java.util.List[NewRule] = {
    ScalastyleResources.allDefinedRules map (rule => toRule(repository, rule))
  }
  
  private def toRule(repository: NewRepository, repoRule : RepositoryRule) = {
    var name = ScalastyleResources.nameFromProfile(repoRule.clazz)
    if (name == "UNAVAILABLE")
    {
      name = repoRule.id
    }  
    val newRule = repository.createRule(repoRule.clazz).setName(name)

    //val newRule = repository.createRule(repoRule.clazz).setName(repoRule.id)
    newRule.setHtmlDescription(repoRule.description)
    val severity = ScalastyleResources.severityFromProfile(repoRule.clazz)
    newRule.setSeverity(severity)

    val params = repoRule.params map { (r: Param) => newRule.createParam(r.name)
                                                        .setDefaultValue(r.defaultVal)
                                                        .setType(RuleParamType.parse(r.typeName))
                                                        .setDescription(r.desc)}

    params foreach ( p => log.debug("Created param for " + newRule.key() + " : " + p) )

    newRule
  }

}
