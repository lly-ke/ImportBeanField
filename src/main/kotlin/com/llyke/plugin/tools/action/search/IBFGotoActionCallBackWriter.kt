package com.llyke.plugin.tools.action.search

import com.intellij.ide.util.gotoByName.ChooseByNamePopup
import com.intellij.jam.JamPomTarget
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiJavaFileImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.targets.AliasingPsiTarget
import com.intellij.psi.util.JavaElementKind
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.spring.model.jam.JamPsiClassSpringBean
import com.intellij.util.alsoIfNull
import com.llyke.plugin.tools.IBFBundle
import com.llyke.plugin.tools.util.IBFNotifier
import com.llyke.plugin.tools.util.IdeaUtil
import org.jetbrains.kotlin.psi.KtClassOrObject


/**
 * @author lw
 * @date 2022/12/10 10:04
 */
class IBFGotoActionCallBackWriter(private val e: AnActionEvent) {


    companion object {
        val LOG = logger<IBFGotoActionCallBackWriter>()
    }

    fun elementChosen(chooseByNamePopup: ChooseByNamePopup?, o: Any?) {
        LOG.info("选择的元素是：$o")
        when (o) {
            is PsiClass -> {
                writeToJavaField(o)
            }

            is PsiMethod -> {
                writeToJavaField(o.returnType as? PsiClassType ?: return)
            }

            is PomTargetPsiElement -> {
                when (val target = o.target) {
                    is JamPomTarget -> { // bean别名
                        val jamElement = target.jamElement as? JamPsiClassSpringBean ?: return
                        val psiClass = jamElement.psiElement

                        writeToJavaField(psiClass)
                    }

                    is AliasingPsiTarget -> {
                        when (val navigationElement = target.navigationElement) {
                            is PsiClass -> {
                                writeToJavaField(navigationElement)
                            }

                            is KtClassOrObject -> {
                                writeToJavaField(navigationElement)
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

    private fun writeToJavaField(psiClass: PsiClass) {
        writeToJavaField(JavaPsiFacade.getInstance(e.project ?: return).elementFactory.createType(psiClass))
    }

    private fun writeToJavaField(psiType: Any) {
        val detectionWriteTargetClass = detectionWriteTargetClass() ?: return
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

                writePackageToJavaFile(detectionWriteTargetClass)
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

    private fun writePackageToJavaFile(detectionWriteTargetClass: PsiClass) {
        val psiImportListArray =
            PsiTreeUtil.getChildrenOfType(detectionWriteTargetClass.containingFile, PsiImportList::class.java) ?: return
        // 正常来说数组只会有一个元素
        for (psiImportList in psiImportListArray) {
            var psiImportStatement =
                psiImportList.findSingleClassImportStatement("org.springframework.beans.factory.annotation.Autowired")
            psiImportStatement.alsoIfNull {
                psiImportStatement =
                    psiImportList.findOnDemandImportStatement("org.springframework.beans.factory.annotation")
            }
            if (psiImportStatement == null) {
                WriteCommandAction.runWriteCommandAction(detectionWriteTargetClass.project) {
                    psiImportList.add(
                        JavaPsiFacade.getElementFactory(detectionWriteTargetClass.project)
                            .createImportStatement(
                                JavaPsiFacade.getInstance(detectionWriteTargetClass.project)
                                    .findClass(
                                        "org.springframework.beans.factory.annotation.Autowired",
                                        GlobalSearchScope.allScope(detectionWriteTargetClass.project)
                                    ) ?: return@runWriteCommandAction
                            )
                    )
                }
            }
        }
    }

    /**
     * 转换成真正注入的类型, 比如UserServiceImpl -> UserService
     */
    private fun transformPsiType(paramPisType: PsiClassType): PsiClassType {
        val psiClass = paramPisType.resolve() ?: return paramPisType
        val javaElementKind = JavaElementKind.fromElement(psiClass) ?: return paramPisType

        // 不是java类, 直接返回
        if (javaElementKind != JavaElementKind.CLASS) {
            return paramPisType
        }

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

    fun detectionWriteTargetClass(): PsiClass? {
        val res = detectionWriteTargetClassInner()
        if (res == null) {
            LOG.info("请将光标移动到正确的位置, 检测不到写入目标文件")
            IBFNotifier.notifyInformation(e.project ?: return null, IBFBundle.getMessage("ibf.notifier.error.noClass"))
        }
        return res
    }

    private fun detectionWriteTargetClassInner(): PsiClass? {
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