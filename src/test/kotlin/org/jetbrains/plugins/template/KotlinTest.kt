package org.jetbrains.plugins.template

import com.jetbrains.rd.util.string.printToString
import com.llyke.plugin.tools.util.IdeaUtil
import org.junit.Test

/**
 * @author lw
 * @date 2022/11/30 16:48
 */
class KotlinTest {

    @Test
    fun testNull() {
        var name: String? = "123";

        var i = name as? Int
        println(i?.toString())
    }


    @Test
    fun testStrToCamelCase() {
        var s = IdeaUtil.toCamelCase("UserService")
        println(s)
    }


}

