package org.jetbrains.plugins.template

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class MyPluginTest : BasePlatformTestCase() {

//    fun testXMLFile() {
//        val psiFile = myFixture.configureByText(XmlFileType.INSTANCE, "<foo>bar</foo>")
//        val xmlFile = assertInstanceOf(psiFile, XmlFile::class.java)
//
//        assertFalse(PsiErrorElementUtil.hasErrors(project, xmlFile.virtualFile))
//
//        assertNotNull(xmlFile.rootTag)
//
//        xmlFile.rootTag?.let {
//            assertEquals("foo", it.name)
//            assertEquals("bar", it.value.text)
//        }
//    }
//
//    fun testRename() {
//        myFixture.testRename("foo.xml", "foo_after.xml", "a2")
//    }

//    fun testProjectService() {
//        val projectService = project.service<MyProjectService>()
//
//        assertEquals(4, projectService.getRandomNumber())
//    }


    //    fun testPsiType() {
//        myFixture.editor.printToString()
//        var javaLangClass =
//
    //            .getJavaLangClass(targetClass.manager, GlobalSearchScope.allScope(targetClass.project))
//        println(javaLangClass)
//    }
    override fun getTestDataPath() = "src/test/testData/rename"
}
