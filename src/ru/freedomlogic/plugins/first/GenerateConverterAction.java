package ru.freedomlogic.plugins.first;

import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor;
import org.jetbrains.kotlin.idea.internal.Location;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtPsiFactory;
import ru.freedomlogic.plugins.first.utils.KtClassHelper;

import java.util.List;

/**
 * Created by Fi5t on 04/02/2017.
 */
public class GenerateConverterAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final KtClass clazz = getPsiClassFromEvent(anActionEvent);
        final List<ValueParameterDescriptor> list = KtClassHelper.findParams(clazz);

        list.forEach(field -> {
            System.out.println(field.getType().toString());
            System.out.println(field.getName());
        });

        new WriteCommandAction.Simple(clazz.getProject(), clazz.getContainingFile()) {

            @Override
            protected void run() throws Throwable {
                KtPsiFactory factory = new KtPsiFactory(clazz.getProject());

                final KtFile file = factory.createFile(clazz.getName() + "Entity.kt", createClassBody(clazz, list));

                anActionEvent.getData(PlatformDataKeys.PSI_FILE).getContainingDirectory().add(file);
            }
        }.execute();


    }


    private String createClassBody(KtClass ktClass, List<ValueParameterDescriptor> fields) {
        final StringBuilder builder = new StringBuilder();

        builder.append("package " + ktClass.getContainingKtFile().getPackageFqName().toString());
        builder.append("\n\n");
        builder.append("data class " + ktClass.getName() + "Entity(");
        builder.append("\n");
        fields.forEach(field -> {
            builder.append("\t\tval " + field.getName() + ": " + field.getType().toString() + "? = null,");
            builder.append("\n");
        });

        builder.append(")");


        return builder.toString();
    }

    private KtClass getPsiClassFromEvent(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        assert editor != null;

        Project project = editor.getProject();
        if (project == null) return null;

        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null || !(psiFile instanceof KtFile) || ProjectRootsUtil.isOutsideSourceRoot(psiFile))
            return null;

        Location location = Location.fromEditor(editor, project);
        PsiElement psiElement = psiFile.findElementAt(location.getStartOffset());
        if (psiElement == null) return null;

        return KtClassHelper.getKtClassForElement(psiElement);
    }

    // anActionEvent.getData(PlatformDataKeys.PSI_ELEMENT).getChildren()[2].getChildren()[0].getChildren() - параметры конструктора
}
