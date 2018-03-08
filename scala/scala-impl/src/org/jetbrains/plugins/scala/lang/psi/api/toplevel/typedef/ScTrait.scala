package org.jetbrains.plugins.scala
package lang
package psi
package api
package toplevel
package typedef

import javax.swing.Icon

import com.intellij.psi.{PsiClass, PsiElement}
import org.jetbrains.plugins.scala.icons.Icons
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScDecoratedIconOwner
import org.jetbrains.plugins.scala.lang.psi.light.PsiClassWrapper

/** 
* @author Alexander Podkhalyuzin
* @since 20.02.2008
*/

trait ScTrait extends ScTypeDefinition with ScDecoratedIconOwner {

  override protected def getBaseIcon(flags: Int): Icon = Icons.TRAIT

  def getTraitToken: PsiElement = findFirstChildByType(ScalaTokenTypes.kTRAIT)

  def getObjectClassOrTraitToken: PsiElement = getTraitToken

  def fakeCompanionClass: PsiClass = {
    def withSuffix(name: String) = s"$name$$class"
    new PsiClassWrapper(this, withSuffix(getQualifiedName), withSuffix(getName))
  }
}