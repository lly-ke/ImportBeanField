package com.llyke.plugin.tools.action

import com.intellij.ide.actions.GotoActionBase
import com.intellij.ide.util.gotoByName.ChooseByNamePopup
import com.intellij.jam.JamPomTarget
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.targets.AliasingPsiTarget
import com.intellij.spring.model.jam.JamPsiClassSpringBean
import com.llyke.plugin.tools.action.search.ImportBeanFieldModel
import com.llyke.plugin.tools.util.IdeaUtil


/**
 * @author lw
 * @date 2022/12/1 10:12
 */
class SearchAction : GotoActionBase() {

    val LOG = logger<SearchAction>()

    override fun gotoActionPerformed(e: AnActionEvent) {
        val model = ImportBeanFieldModel(e.project ?: return)
        showNavigationPopup(e, model, object : GotoActionCallback<Any>() {
            override fun elementChosen(chooseByNamePopup: ChooseByNamePopup, o: Any) {
                LOG.info("选择的元素是：$o")
                when (o) {
                    is PsiClass -> {
                        writeField(o, o.name!!, e)
                    }

                    is PomTargetPsiElement -> {
                        when (val target = o.target) {
                            is JamPomTarget -> { // bean别名
                                val name = target.name
                                val jamElement = target.jamElement as? JamPsiClassSpringBean ?: return
//                                val jamElement: SpringService = target.jamElement as? SpringService ?: return
                                val psiClass = jamElement.psiElement

                                writeField(psiClass, name, e)
                            }

                            is AliasingPsiTarget -> {
                                val name = target.name
                                val psiClass = target.navigationElement as? PsiClass ?: return
                                writeField(psiClass, name, e)
                            }

                            else -> {
                                LOG.error("未支持的类型：$o")
                            }
                        }
                    }

                    else -> {
                        LOG.error("暂未支持的元素类型：$o")
                    }
                }
            }

            private fun writeField(psiClass: PsiClass, name: String, e: AnActionEvent) {
                val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
                val editor = e.getData(CommonDataKeys.EDITOR) ?: return
                val targetClass: PsiClass = IdeaUtil.getTargetClass(editor, psiFile) ?: return
                val project = e.project ?: return
                LOG.info("写入目标类：${targetClass.qualifiedName}")

                val psiElementFactory: PsiElementFactory = JavaPsiFacade.getElementFactory(project) ?: return

        //        val search: Query<PsiClass> =
        //            AllClassesSearch.search(GlobalSearchScope.allScope(targetClass.project), targetClass.project)
        //        val psiClass = search.findFirst() ?: return
        //        val name = psiClass.name ?: return
                val psiField = psiElementFactory.createField(
                    IdeaUtil.toCamelCase(name), JavaPsiFacade.getInstance(project).elementFactory.createType(psiClass)
                )
                val modifierList = psiField.modifierList ?: return
                modifierList.addAnnotation("Autowired")

                WriteCommandAction.runWriteCommandAction(e.project) {
                    targetClass.add(psiField)
                }
            }

        })
    }

}