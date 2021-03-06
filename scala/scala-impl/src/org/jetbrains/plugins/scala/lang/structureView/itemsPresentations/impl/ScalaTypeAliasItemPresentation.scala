package org.jetbrains.plugins.scala.lang.structureView.itemsPresentations.impl

import com.intellij.openapi.editor.colors.{CodeInsightColors, TextAttributesKey}
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScTypeAlias
import org.jetbrains.plugins.scala.lang.structureView.ScalaElementPresentation
import org.jetbrains.plugins.scala.lang.structureView.itemsPresentations.ScalaItemPresentation

/**
 * User: Alexander Podkhalyuzin
 * Date: 31.07.2008
 */
 
class ScalaTypeAliasItemPresentation(element: ScTypeAlias, isInherited: Boolean) extends ScalaItemPresentation(element) {
  override def getPresentableText: String = ScalaElementPresentation.getTypeAliasPresentableText(element)

  override def getTextAttributesKey: TextAttributesKey =
    if (isInherited) CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES else null
}