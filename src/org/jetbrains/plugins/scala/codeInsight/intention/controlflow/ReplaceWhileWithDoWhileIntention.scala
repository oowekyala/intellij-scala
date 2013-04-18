package org.jetbrains.plugins.scala
package codeInsight.intention.controlflow

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.editor.Editor
import com.intellij.psi.{PsiDocumentManager, PsiElement}
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScExpression, ScWhileStmt}
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.extensions._

/**
 * Nikolay.Tropin
 * 4/17/13
 */

object ReplaceWhileWithDoWhileIntention {
  def familyName = "Replace while with do while"
}

class ReplaceWhileWithDoWhileIntention extends PsiElementBaseIntentionAction {
  def getFamilyName = ReplaceWhileWithDoWhileIntention.familyName

  override def getText: String = ReplaceWhileWithDoWhileIntention.familyName

  def isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean = {
    for {
      whileStmt <- Option(PsiTreeUtil.getParentOfType(element, classOf[ScWhileStmt], false))
      condition <- whileStmt.condition
      body <- whileStmt.body
    } {
      val offset = editor.getCaretModel.getOffset
      if (offset >= whileStmt.getTextRange.getStartOffset && offset <= condition.getTextRange.getStartOffset - 1)
        return true
    }

    false
  }

  override def invoke(project: Project, editor: Editor, element: PsiElement) {
    val whileStmt: ScWhileStmt = PsiTreeUtil.getParentOfType(element, classOf[ScWhileStmt])
    if (whileStmt == null || !whileStmt.isValid) return

    for {
      condition <- whileStmt.condition
      body <- whileStmt.body
    } {
      val condText = condition.getText
      val bodyText = body.getText

      val expr = new StringBuilder
      expr.append("if (").append(condText).append(") {\n")
      expr.append("do ").append(bodyText).append(" while (").append(condText).append(")\n")
      expr.append("}")

      val newStmt: ScExpression = ScalaPsiElementFactory.createExpressionFromText(expr.toString(), element.getManager)

      inWriteAction {
        whileStmt.replaceExpression(newStmt, removeParenthesis = true)
        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument)
      }
    }
  }
}
