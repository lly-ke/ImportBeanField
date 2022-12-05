//package com.llyke.plugin.tools.action.search;
//
//import com.intellij.navigation.NavigationItem;
//import com.intellij.openapi.module.Module;
//import com.intellij.openapi.util.text.StringUtil;
//import com.intellij.psi.PsiElement;
//import com.intellij.spring.SpringManager;
//import com.intellij.spring.contexts.model.SpringModel;
//import com.intellij.spring.model.SpringBeanPointer;
//import com.intellij.spring.model.utils.SpringCommonUtils;
//import com.intellij.spring.model.utils.SpringModelSearchers;
//import com.intellij.spring.model.utils.SpringModelUtils;
//import com.intellij.util.xml.model.gotosymbol.GoToSymbolProvider;
//import com.intellij.xml.util.PsiElementPointer;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//
///**
// * @author lw
// * @date 2022/12/5 11:16
// */
//public class IBFContributorProvider extends GoToSymbolProvider {
//
//    protected void addNames(@NotNull Module module, Set<String> result) {
//        if (module == null) {
//            return;
//        }
//
//        Set<String> names = new HashSet();
//        Iterator var4 = getModels(module).iterator();
//
//        label39:
//        while(var4.hasNext()) {
//            SpringModel springModel = (SpringModel)var4.next();
//            Iterator var6 = springModel.getAllCommonBeans().iterator();
//
//            while(true) {
//                SpringBeanPointer pointer;
//                String name;
//                do {
//                    if (!var6.hasNext()) {
//                        continue label39;
//                    }
//
//                    pointer = (SpringBeanPointer)var6.next();
//                    name = pointer.getName();
//                } while(!StringUtil.isNotEmpty(name));
//
//                names.add(name);
//                String[] var9 = pointer.getAliases();
//                int var10 = var9.length;
//
//                for(int var11 = 0; var11 < var10; ++var11) {
//                    String alias = var9[var11];
//                    if (StringUtil.isNotEmpty(alias)) {
//                        names.add(alias);
//                    }
//                }
//            }
//        }
//
//        result.addAll(names);
//    }
//
//    protected void addItems(@NotNull Module module, String name, List<NavigationItem> result) {
//        if (module == null) {
//            return;
//        }
//
//        Iterator var4 = getModels(module).iterator();
//
//        while(var4.hasNext()) {
//            SpringModel springModel = (SpringModel)var4.next();
//            Iterator var6 = SpringModelSearchers.findBeans(springModel, name).iterator();
//
//            while(var6.hasNext()) {
//                PsiElementPointer pointer = (PsiElementPointer)var6.next();
//                PsiElement element = pointer.getPsiElement();
//                if (element instanceof NavigationItem) {
//                    result.add((NavigationItem)element);
//                }
//            }
//        }
//
//    }
//
//    protected boolean acceptModule(Module module) {
//        return SpringCommonUtils.hasSpringFacet(module) || SpringModelUtils.getInstance().hasAutoConfiguredModels(module);
//    }
//
//    private static Set<SpringModel> getModels(Module module) {
//        return SpringManager.getInstance(module.getProject()).getAllModelsWithoutDependencies(module);
//    }
//}
