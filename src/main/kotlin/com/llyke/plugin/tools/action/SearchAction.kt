package com.llyke.plugin.tools.action

import com.intellij.ide.actions.GotoActionBase
import com.intellij.ide.util.gotoByName.ChooseByNamePopup
import com.intellij.jam.JamPomTarget
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiJavaFileImpl
import com.intellij.psi.targets.AliasingPsiTarget
import com.intellij.spring.model.jam.JamPsiClassSpringBean
import com.llyke.plugin.tools.IBFBundle
import com.llyke.plugin.tools.action.search.ImportBeanFieldModel
import com.llyke.plugin.tools.util.IBFNotifier
import com.llyke.plugin.tools.util.IdeaUtil


/**
 * @author lw
 * @date 2022/12/1 10:12
 */
class SearchAction : GotoActionBase() {

    companion object {
        val LOG = logger<SearchAction>()
    }

    override fun gotoActionPerformed(e: AnActionEvent) {
        val model = ImportBeanFieldModel(e.project ?: return)
        showNavigationPopup(e, model, object : GotoActionCallback<Any>() {
            override fun elementChosen(chooseByNamePopup: ChooseByNamePopup, o: Any) {
                LOG.info("选择的元素是：$o")
                when (o) {
                    is PsiClass -> {
                        writeField(o, o.name!!, e)
                    }

                    is PsiMethod -> {
                        writeField(o.returnType ?: return, o.name ?: return, e)
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
                writeField(JavaPsiFacade.getInstance(e.project ?: return).elementFactory.createType(psiClass), name, e)
            }

            private fun writeField(psiType: PsiType, name: String, e: AnActionEvent) {

                val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
                val editor = e.getData(CommonDataKeys.EDITOR) ?: return
                val targetClass: PsiClass = IdeaUtil.getTargetClassByCursor(editor, psiFile) ?: let {
                    // 当前光标找不到并且当前文件只有一个类, 就用当前文件唯一的类
                    val classes = (e.getData(CommonDataKeys.PSI_FILE) as PsiJavaFileImpl).classes
                    if (classes.size == 1) {
                        classes[0]
                    } else {
                        IBFNotifier.notifyError(e.project ?: return, IBFBundle.getMessage("ibf.notifier.error.noClass"))
                        return
                    }
                }
                val project = e.project ?: return
                LOG.info("写入目标类：${targetClass.qualifiedName}")

                val psiElementFactory: PsiElementFactory = JavaPsiFacade.getElementFactory(project) ?: return

                //        val search: Query<PsiClass> =
                //            AllClassesSearch.search(GlobalSearchScope.allScope(targetClass.project), targetClass.project)
                //        val psiClass = search.findFirst() ?: return
                //        val name = psiClass.name ?: return
                val psiField = psiElementFactory.createField(
                    IdeaUtil.toCamelCase(name), transformPsiType(psiType)
                )
                val modifierList = psiField.modifierList ?: return
                modifierList.addAnnotation("Autowired")

                WriteCommandAction.runWriteCommandAction(e.project) {
                    targetClass.add(psiField)
                }
            }

            private fun transformPsiType(psiType: PsiType): PsiType {

                return psiType
            }

        })
    }

}