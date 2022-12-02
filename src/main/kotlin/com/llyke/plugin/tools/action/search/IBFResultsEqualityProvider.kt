package com.llyke.plugin.tools.action.search

import com.intellij.ide.actions.searcheverywhere.AbstractEqualityProvider
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereFoundElementInfo

/**
 * @author lw
 * @date 2022/12/1 11:18
 */
class IBFResultsEqualityProvider : AbstractEqualityProvider() {
    override fun areEqual(
        newItem: SearchEverywhereFoundElementInfo,
        alreadyFoundItem: SearchEverywhereFoundElementInfo
    ): Boolean {
        return newItem.element == alreadyFoundItem.element
    }
}