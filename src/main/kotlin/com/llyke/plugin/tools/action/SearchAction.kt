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
import org.jetbrains.kotlin.psi.KtClassOrObject


/**
 * @author lw
 * @date 2022/12/1 10:12
 */
class SearchAction : GotoActionBase() {

    companion object {
        val LOG = logger<SearchAction>()
    }

    override fun update(event: AnActionEvent) {
        super.update(event)
    }

    override fun gotoActionPerformed(e: AnActionEvent) {

        if (detectionWriteTargetClass(e) == null) {
            // myInAction = null必须要调用, 负责会导致插件不可用
            myInAction = null
            return
        }
        val model = ImportBeanFieldModel(e.project ?: return)
        showNavigationPopup(e, model, object : GotoActionCallback<Any>() {
            override fun elementChosen(chooseByNamePopup: ChooseByNamePopup, o: Any) {
                LOG.info("选择的元素是：$o")
                when (o) {
                    is PsiClass -> {
                        writeToJavaField(o, e)
                    }

                    is PsiMethod -> {
                        writeToJavaField(o.returnType as? PsiClassType ?: return, e)
                    }

                    is PomTargetPsiElement -> {
                        when (val target = o.target) {
                            is JamPomTarget -> { // bean别名
                                val jamElement = target.jamElement as? JamPsiClassSpringBean ?: return
                                val psiClass = jamElement.psiElement

                                writeToJavaField(psiClass, e)
                            }

                            is AliasingPsiTarget -> {
                                when (val navigationElement = target.navigationElement) {
                                    is PsiClass -> {
                                        writeToJavaField(navigationElement, e)
                                    }

                                    is KtClassOrObject -> {
                                        writeToJavaField(navigationElement, e)
                                    }

                                    else -> {
                                        LOG.info("未知的类型：$navigationElement")
                                    }
                                }
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

            private fun writeToJavaField(psiClass: PsiClass, e: AnActionEvent) {
                writeToJavaField(JavaPsiFacade.getInstance(e.project ?: return).elementFactory.createType(psiClass), e)
            }

            private fun writeToJavaField(psiType: Any, e: AnActionEvent) {
                val detectionWriteTargetClass = detectionWriteTargetClass(e) ?: return
                val project = e.project ?: return
                LOG.info("需要注入的目标类：${detectionWriteTargetClass.qualifiedName}")

                when (psiType) {
                    // 源bean是java类
                    is PsiClassType -> {
                        val psiElementFactory: PsiElementFactory = JavaPsiFacade.getElementFactory(project) ?: return
                        val transformPsiType = transformPsiType(psiType)
                        val psiField = psiElementFactory.createField(
                            IdeaUtil.toCamelCase(transformPsiType.name), transformPsiType
                        )
                        val modifierList = psiField.modifierList ?: return
                        modifierList.addAnnotation("Autowired")

                        WriteCommandAction.runWriteCommandAction(e.project) {
                            detectionWriteTargetClass.add(psiField)
                        }
                    }

                    // 源bean是kotlin类
                    is KtClassOrObject -> {

                        val psiElementFactory: PsiElementFactory = JavaPsiFacade.getElementFactory(project) ?: return
                        val transformPsiType = transformPsiType(psiType)
                        val psiField = psiElementFactory.createFieldFromText(
                            "private ${transformPsiType.name} ${
                                IdeaUtil.toCamelCase(transformPsiType.name!!)
                            };", detectionWriteTargetClass
                        )
                        val modifierList = psiField.modifierList ?: return
                        modifierList.addAnnotation("Autowired")

                        WriteCommandAction.runWriteCommandAction(e.project) {
                            detectionWriteTargetClass.add(psiField)
                        }
//                        val transformPsiType = transformPsiType(psiType)
//                        val ktPsiFactory = KtPsiFactory(project)
//                        val ktProperty =
//                            ktPsiFactory.createProperty(IdeaUtil.toCamelCase(transformPsiType.name!!))
//
//                        ktProperty.addAnnotationEntry(ktPsiFactory.createAnnotationEntry("@Autowired"))
//                        WriteCommandAction.runWriteCommandAction(e.project) {
//                            targetClass.add(ktProperty)
//                        }
                    }
                }

            }

            /**
             * 转换成真正注入的类型, 比如UserServiceImpl -> UserService
             */
            private fun transformPsiType(paramPisType: PsiClassType): PsiClassType {
                val psiClass = paramPisType.resolve() ?: return paramPisType
                val list = psiClass.interfaces.filter {
                    // java 内部包直接过滤
                    !(it.qualifiedName?.startsWith("java") ?: true)
                }
                // 只有一个接口直接用接口来注入
                if (list.size == 1) {
                    return JavaPsiFacade.getInstance(
                        e.project ?: return paramPisType
                    ).elementFactory.createType(list[0])
                }
                return paramPisType
            }

            private fun transformPsiType(paramPisType: KtClassOrObject): KtClassOrObject {
                // TODO 后期支持kotlin transformPsiType, 相关api暂时还没找到
                return paramPisType
            }

        })
    }

    private fun detectionWriteTargetClass(
        e: AnActionEvent
    ): PsiClass? {
        val res = detectionWriteTargetClassInner(e)
        if (res == null) {
            LOG.info("请将光标移动到正确的位置, 检测不到写入目标文件")
            IBFNotifier.notifyInformation(e.project ?: return null, IBFBundle.getMessage("ibf.notifier.error.noClass"))
        }
        return res
    }

    private fun detectionWriteTargetClassInner(
        e: AnActionEvent
    ): PsiClass? {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return null
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return null
        val targetClass: PsiClass = IdeaUtil.getTargetClassByCursor(editor, psiFile) ?: let {
            // 当前光标找不到并且当前文件只有一个类, 就用当前文件唯一的类
            val psiJavaFile = e.getData(CommonDataKeys.PSI_FILE) as? PsiJavaFileImpl ?: return null
            val classes = psiJavaFile.classes
            if (classes.size == 1) {
                classes[0]
            } else {
                return null
            }
        }
        return targetClass
    }

}