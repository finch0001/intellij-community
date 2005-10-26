package com.intellij.codeInsight.daemon.impl.quickfix;

import com.intellij.codeInsight.daemon.QuickFixBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;

/**
 * @author Mike
 */
public class CreateClassFromUsageAction extends CreateFromUsageBaseAction {
  private static final Logger LOG = Logger.getInstance(
    "#com.intellij.codeInsight.daemon.impl.quickfix.CreateClassFromUsageAction");

  private final boolean myCreateInterface;
  private final PsiJavaCodeReferenceElement myRefElement;

  public CreateClassFromUsageAction(PsiJavaCodeReferenceElement refElement, boolean createInterface) {
    myRefElement = refElement;
    myCreateInterface = createInterface;
  }

  public String getText(String varName) {
    if (myCreateInterface) {
      return QuickFixBundle.message("create.class.from.usage.interface.text", varName);
    }
    else {
      return QuickFixBundle.message("create.class.from.usage.class.text", varName);
    }
  }

  protected void invokeImpl(PsiClass targetClass) {
    if (CreateFromUsageUtils.isValidReference(myRefElement, true)) {
      return;
    }
    String superClassName = null;
    if (myRefElement.getParent().getParent() instanceof PsiMethod) {
      PsiMethod method = (PsiMethod)myRefElement.getParent().getParent();
      if (method.getThrowsList() == myRefElement.getParent()) {
        superClassName = "java.lang.Exception";
      }
    }
    final PsiClass aClass = CreateFromUsageUtils.createClass(myRefElement, myCreateInterface, superClassName);
    ApplicationManager.getApplication().runWriteAction(
      new Runnable() {
        public void run() {
          if (aClass == null) return;
          try {
            myRefElement.bindToElement(aClass);
          }
          catch (IncorrectOperationException e) {
            LOG.error(e);
          }

          OpenFileDescriptor descriptor = new OpenFileDescriptor(myRefElement.getProject(), aClass.getContainingFile().getVirtualFile(),
                                                                 aClass.getTextOffset());
          FileEditorManager.getInstance(aClass.getProject()).openTextEditor(descriptor, true);
        }
      }
    );
  }

  protected boolean isValidElement(PsiElement element) {
    return CreateFromUsageUtils.isValidReference((PsiReference)element, true);
  }

  protected PsiElement getElement() {
    if (!myRefElement.isValid() || !myRefElement.getManager().isInProject(myRefElement)) return null;
    if (!CreateFromUsageUtils.isValidReference(myRefElement, true) &&
        myRefElement.getReferenceNameElement() != null && checkClassName(myRefElement.getReferenceName())) {
      PsiElement parent = myRefElement.getParent();

      if (parent instanceof PsiTypeElement) {
        if (parent.getParent() instanceof PsiReferenceParameterList) return myRefElement;

        while (parent.getParent() instanceof PsiTypeElement) parent = parent.getParent();
        if (parent.getParent() instanceof PsiVariable || parent.getParent() instanceof PsiMethod ||
            parent.getParent() instanceof PsiClassObjectAccessExpression ||
            parent.getParent() instanceof PsiTypeCastExpression ||
            (parent.getParent() instanceof PsiInstanceOfExpression && ((PsiInstanceOfExpression)parent.getParent()).getCheckType() == parent)) {
          return myRefElement;
        }
      }
      else if (parent instanceof PsiReferenceList) {
        if (parent.getParent() instanceof PsiClass) {
          PsiClass psiClass = (PsiClass)parent.getParent();
          if (psiClass.getExtendsList() == parent) {
            if (!myCreateInterface && !psiClass.isInterface()) return myRefElement;
            if (myCreateInterface && psiClass.isInterface()) return myRefElement;
          }
          if (psiClass.getImplementsList() == parent && myCreateInterface) return myRefElement;
        }
        else if (parent.getParent() instanceof PsiMethod) {
          PsiMethod method = (PsiMethod)parent.getParent();
          if (method.getThrowsList() == parent && !myCreateInterface) return myRefElement;
        }
      }
      else if (parent instanceof PsiAnonymousClass && ((PsiAnonymousClass)parent).getBaseClassReference() == myRefElement) {
        return myRefElement;
      }
    }

    if (myRefElement instanceof PsiReferenceExpression) {
      PsiReferenceExpression referenceExpression = (PsiReferenceExpression)myRefElement;

      PsiElement parent = referenceExpression.getParent();

      if (parent instanceof PsiMethodCallExpression) {
        return null;
      }
      if (parent.getParent() instanceof PsiMethodCallExpression && myCreateInterface) return null;

      if (referenceExpression.getReferenceNameElement() != null &&
          checkClassName(referenceExpression.getReferenceName()) &&
          !CreateFromUsageUtils.isValidReference(referenceExpression, true)) {
        return referenceExpression;
      }
    }

    return null;
  }

  private boolean checkClassName(String name) {
    return Character.isUpperCase(name.charAt(0));
  }

  protected boolean isAvailableImpl(int offset) {
    PsiElement nameElement = myRefElement.getReferenceNameElement();
    if (nameElement == null) return false;
    PsiElement parent = myRefElement.getParent();
    if (parent instanceof PsiExpression && !(parent instanceof PsiReferenceExpression)) return false;
    if (shouldShowTag(offset, nameElement, myRefElement)) {
      setText(getText(nameElement.getText()));
      return true;
    }

    return false;
  }

  public String getFamilyName() {
    return QuickFixBundle.message("create.class.from.usage.family");
  }

  public boolean startInWriteAction() {
    return false;
  }
}
