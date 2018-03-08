package org.jetbrains.plugins.scala
package lang
package psi
package api
package toplevel
package typedef

import javax.swing.Icon

import com.intellij.psi.PsiElement
import org.jetbrains.plugins.scala.icons.Icons
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.psi.api.base.ScPrimaryConstructor
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScDecoratedIconOwner, ScFunction}

/**
* @author Alexander Podkhalyuzin
* Date: 20.02.2008
*/

trait ScClass extends ScTypeDefinition with ScConstructorOwner with ScDecoratedIconOwner {

  override protected def getBaseIcon(flags: Int): Icon =
    if (hasAbstractModifier) Icons.ABSTRACT_CLASS else Icons.CLASS

  def typeParamString: String = typeParameters
    .map(ScalaPsiUtil.typeParamString) match {
    case Seq() => ""
    case seq => seq.mkString("[", ", ", "]")
  }

  def tooBigForUnapply: Boolean = constructor.exists(_.parameters.length > 22)

  def getSyntheticMethodsText: List[String] = {
    val typeParamStringRes =
      if (typeParameters.nonEmpty)
        typeParameters.map(_.name).mkString("[", ", ", "]")
      else ""

    val unapply: Option[String] = if (tooBigForUnapply) None else {
      val paramStringRes = constructor match {
        case Some(x: ScPrimaryConstructor) =>
          val clauses = x.parameterList.clauses
          if (clauses.isEmpty) "scala.Boolean"
          else {
            val params = clauses.head.parameters
            if (params.isEmpty) "scala.Boolean"
            else {
              val strings = params.map(p =>
                (if (p.isRepeatedParameter) "scala.Seq[" else "") +
                  p.typeElement.fold("scala.Any")(_.getText) +
                  (if (p.isRepeatedParameter) "]" else ""))
              strings.mkString("scala.Option[" + (if (strings.length > 1) "(" else ""), ", ",
                (if (strings.length > 1) ")" else "") + "]")
            }
          }
        case None => "scala.Boolean"
      }
      val unapplyName = constructor match {
        case Some(x: ScPrimaryConstructor) =>
          (for {
            c1 <- x.parameterList.clauses.headOption
            plast <- c1.parameters.lastOption
            if plast.isRepeatedParameter
          } yield "unapplySeq").getOrElse("unapply")
        case None => "unapply"
      }
      Option(s"def $unapplyName$typeParamString(x$$0: $name$typeParamStringRes): $paramStringRes = throw new Error()")
    }

    val apply: Option[String] = if (hasModifierProperty("abstract")) None else {
      val paramString = constructor match {
        case Some(x: ScPrimaryConstructor) =>
          (if (x.parameterList.clauses.length == 1 &&
            x.parameterList.clauses.head.isImplicit) "()" else "") + x.parameterList.clauses.map(c =>
            c.parameters.map(p =>
              p.name + " : " +
                p.typeElement.fold("Any")(_.getText) +
                (if (p.isDefaultParam) " = " + p.getDefaultExpression.fold("{}")(_.getText)
                else if (p.isRepeatedParameter) "*" else "")).
              mkString(if (c.isImplicit) "(implicit " else "(", ", ", ")")).mkString("")
        case None => ""
      }

      Option(s"def apply$typeParamString$paramString: $name$typeParamStringRes = throw new Error()")
    }

    List(apply, unapply).flatten
  }

  def getSyntheticImplicitMethod: Option[ScFunction]

  def getClassToken: PsiElement = findFirstChildByType(ScalaTokenTypes.kCLASS)

  def getObjectClassOrTraitToken: PsiElement = getClassToken

  override def accept(visitor: ScalaElementVisitor): Unit = visitor.visitClass(this)
}
