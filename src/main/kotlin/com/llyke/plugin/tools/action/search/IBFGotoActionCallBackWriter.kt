package com.llyke.plugin.tools.action.search

import com.intellij.ide.util.gotoByName.ChooseByNamePopup
import com.intellij.jam.JamPomTarget
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.ScrollType
import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiJavaFileImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.targets.AliasingPsiTarget
import com.intellij.psi.util.JavaElementKind
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.spring.model.jam.JamPsiClassSpringBean
import com.intellij.util.Consumer
import com.intellij.util.alsoIfNull
import com.llyke.plugin.tools.IBFBundle
import com.llyke.plugin.tools.dialog.search.RepetitionFieldDialogWrapper
import com.llyke.plugin.tools.setting.IBFSetting
import com.llyke.plugin.tools.util.IBFNotifier
import com.llyke.plugin.tools.util.IdeaUtil
import org.jetbrains.kotlin.idea.util.ifTrue
import org.jetbrains.kotlin.psi.KtClassOrObject


/**
 * @author lw
 * @date 2022/12/10 10:04
 */
class IBFGotoActionCallBackWriter(private val e: AnActionEvent) {

    private val psiFile = e.getData(CommonDataKeys.PSI_FILE)
    private val editor = e.getData(CommonDataKeys.EDITOR)
    private val project = e.getData(CommonDataKeys.PROJECT)

    private val ibfSetting = IBFSetting.getInstance()

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
        writeToJavaField(JavaPsiFacade.getInstance(project ?: return).elementFactory.createType(psiClass))
    }

    private fun writeToJavaField(psiType: Any) {
        val detectionWriteTargetClass = detectionWriteTargetClass() ?: return
        val project = project ?: return
        LOG.info("需要注入的目标类：${detectionWriteTargetClass.qualifiedName}")
        var fieldName = "";

        when (psiType) {
            // 源bean是java类
            is PsiClassType -> {
                val psiElementFactory: PsiElementFactory = JavaPsiFacade.getElementFactory(project) ?: return
                val transformPsiType = transformPsiType(psiType)

                if (detectionWriteTargetClass.fields.any {
                        it.type == transformPsiType
                    } && !RepetitionFieldDialogWrapper(transformPsiType, detectionWriteTargetClass).showAndGet()) {
                    LOG.info("已存在相同类型的字段，且用户选择不添加")
                    return
                }

                fieldName = transformFieldName(transformPsiType.name)
                val psiField = psiElementFactory.createField(
                    fieldName, transformPsiType
                )
                val modifierList = psiField.modifierList ?: return
                when (ibfSetting.injectMode) {
                    0 -> {

                        // 字段注解注入
                        when (ibfSetting.fieldInjectMode) {
                            0 -> {
                                modifierList.addAnnotation("Autowired")
                                val writePackageToJavaFileConsumer = parseAddPackageToJavaFile(
                                    detectionWriteTargetClass,
                                    "org.springframework.beans.factory.annotation.Autowired"
                                )
                                WriteCommandAction.runWriteCommandAction(project) {
                                    writePackageToJavaFileConsumer?.consume(null)
                                    detectionWriteTargetClass.add(psiField)
                                    insertFieldNameOnCursor(fieldName)
                                }
                            }

                            1, 2 -> {
                                modifierList.addAnnotation("Resource")
                                val annotationName =
                                    if (ibfSetting.fieldInjectMode == 1) "javax.annotation.Resource" else "jakarta.annotation.Resource"
                                val writePackageToJavaFileConsumer = parseAddPackageToJavaFile(
                                    detectionWriteTargetClass,
                                    annotationName
                                )
                                WriteCommandAction.runWriteCommandAction(project) {
                                    writePackageToJavaFileConsumer?.consume(null)
                                    detectionWriteTargetClass.add(psiField)
                                    insertFieldNameOnCursor(fieldName)
                                }
                            }

                        }

                    }

                    1 -> {
                        // Lombok构造函数注入
                        psiField.modifierList?.setModifierProperty(PsiModifier.PRIVATE, true)
                        psiField.modifierList?.setModifierProperty(PsiModifier.FINAL, true)
                        val addAnnotationToClassConsumer =
                            parseAddAnnotationToClass(detectionWriteTargetClass, "lombok.RequiredArgsConstructor")
                        WriteCommandAction.runWriteCommandAction(project) {
                            addAnnotationToClassConsumer?.consume(null)
                            detectionWriteTargetClass.add(psiField)
                            insertFieldNameOnCursor(fieldName)
                        }
                    }
                }

            }

            // 源bean是kotlin类
            is KtClassOrObject -> {

                val psiElementFactory: PsiElementFactory = JavaPsiFacade.getElementFactory(project) ?: return
                val transformPsiType = transformPsiType(psiType)
                fieldName = transformFieldName(transformPsiType.name!!)
                val psiField = psiElementFactory.createFieldFromText(
                    "private ${transformPsiType.name} $fieldName;", detectionWriteTargetClass
                )
                val modifierList = psiField.modifierList ?: return
                modifierList.addAnnotation("Autowired")

                WriteCommandAction.runWriteCommandAction(project) {
                    parseAddPackageToJavaFile(
                        detectionWriteTargetClass,
                        "org.springframework.beans.factory.annotation.Autowired"
                    )
                    detectionWriteTargetClass.add(psiField)
                    insertFieldNameOnCursor(fieldName)
                }
            }
        }

    }

    private fun insertFieldNameOnCursor(fieldName: String) {
        ibfSetting.insertFieldNameOnCursor?.ifTrue {
            if (editor == null || project == null) {
                return
            }
            val caretModel = editor.caretModel
            val document = editor.document

            // 防止 Document is locked by write PSI operations
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document)
            editor.document.insertString(caretModel.offset, fieldName)
            PsiDocumentManager.getInstance(project).commitDocument(document)

            // 异步执行
            ApplicationManager.getApplication().invokeLater {
                caretModel.moveToOffset(caretModel.offset + fieldName.length)
                editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
            }
        }
    }

    private fun transformFieldName(name: String): String {
        // IUserService -> userService
        if (name.length >= 2 && name[0] == 'I' && name[1].isUpperCase()) {
            return IdeaUtil.toCamelCase(name.substring(1))
        }

        return IdeaUtil.toCamelCase(name)
    }

    private fun parseAddAnnotationToClass(
        detectionWriteTargetClass: PsiClass,
        annotationName: String
    ): Consumer<Void>? {
        val annotation = detectionWriteTargetClass.modifierList?.annotations?.find {
            it.qualifiedName == annotationName
        }
        if (annotation == null) {
            val addPackageToJavaFileConsumer = parseAddPackageToJavaFile(
                detectionWriteTargetClass,
                "lombok.RequiredArgsConstructor"
            )
            return Consumer<Void> {
                addPackageToJavaFileConsumer?.consume(null)
                detectionWriteTargetClass.modifierList?.addAnnotation(annotationName)
            }
        }
        return null
    }

    private fun parseAddPackageToJavaFile(
        detectionWriteTargetClass: PsiClass,
        annotationName: String
    ): Consumer<Void>? {
        val psiImportListArray =
            PsiTreeUtil.getChildrenOfType(detectionWriteTargetClass.containingFile, PsiImportList::class.java)
                ?: return null
        // 正常来说数组只会有一个元素
        for (psiImportList in psiImportListArray) {
            var psiImportStatement =
                psiImportList.findSingleClassImportStatement(annotationName)
            psiImportStatement.alsoIfNull {
                psiImportStatement =
                    psiImportList.findOnDemandImportStatement(annotationName.substringBefore(".", annotationName))
            }
            if (psiImportStatement == null) {
                return object : Consumer<Void> {
                    override fun consume(t: Void?) {
                        psiImportList.add(
                            JavaPsiFacade.getElementFactory(detectionWriteTargetClass.project)
                                .createImportStatement(
                                    JavaPsiFacade.getInstance(detectionWriteTargetClass.project)
                                        .findClass(
                                            annotationName,
                                            GlobalSearchScope.allScope(detectionWriteTargetClass.project)
                                        ) ?: return
                                )
                        )

                    }
                }
            }
        }
        return null
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
                project ?: return paramPisType
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
            IBFNotifier.notifyInformation(project ?: return null, IBFBundle.getMessage("ibf.notifier.error.noClass"))
        }
        return res
    }

    private fun detectionWriteTargetClassInner(): PsiClass? {
        if (editor == null || psiFile == null) {
            return null
        }
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

        // class不可写返回null
        if (!targetClass.isWritable) {
            return null
        }
        return targetClass
    }


}