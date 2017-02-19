package ru.freedomlogic.plugins.first.utils;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.asJava.elements.KtLightElement;
import org.jetbrains.kotlin.caches.resolve.KotlinCacheService;
import org.jetbrains.kotlin.descriptors.ClassDescriptor;
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor;
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor;
import org.jetbrains.kotlin.incremental.components.NoLookupLocation;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.resolve.lazy.ResolveSession;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Fi5t on 05/02/2017.
 */
public class KtClassHelper {
    public static List<ValueParameterDescriptor> findParams(KtClass ktClass) {
        List<KtElement> list = new ArrayList<KtElement>();
        list.add(ktClass);

        ResolveSession resolveSession = KotlinCacheService.Companion.getInstance(ktClass.getProject()).
                getResolutionFacade(list).getFrontendService(ResolveSession.class);
        ClassDescriptor classDescriptor = resolveSession.getClassDescriptor(ktClass, NoLookupLocation.FROM_IDE);

        List<ValueParameterDescriptor> valueParameters = new ArrayList<ValueParameterDescriptor>();
//        if (classDescriptor.isData()) {
        ConstructorDescriptor constructorDescriptor = classDescriptor.getUnsubstitutedPrimaryConstructor();

        if (constructorDescriptor != null) {
            List<ValueParameterDescriptor> allParameters = constructorDescriptor.getValueParameters();

            for (ValueParameterDescriptor parameter : allParameters) {
                valueParameters.add(parameter);
            }
        }
//        }

        return valueParameters;
    }

    public static KtClass getKtClassForElement(@NotNull PsiElement psiElement) {
        if (psiElement instanceof KtLightElement) {
            PsiElement origin = ((KtLightElement) psiElement).getKotlinOrigin();
            if (origin != null) {
                return getKtClassForElement(origin);
            } else {
                return null;
            }

        } else if (psiElement instanceof KtClass && !((KtClass) psiElement).isEnum() &&
                !((KtClass) psiElement).isInterface() &&
                !((KtClass) psiElement).isAnnotation() &&
                !((KtClass) psiElement).isSealed()) {
            return (KtClass) psiElement;

        } else {
            PsiElement parent = psiElement.getParent();
            if (parent == null) {
                return null;
            } else {
                return getKtClassForElement(parent);
            }
        }
    }
}
