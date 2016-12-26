/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.testGuiFramework.impl

import com.intellij.ide.GeneralSettings
import com.intellij.testGuiFramework.cellReader.SettingsTreeCellReader
import com.intellij.testGuiFramework.fixtures.*
import com.intellij.testGuiFramework.fixtures.newProjectWizard.NewProjectWizardFixture
import com.intellij.testGuiFramework.framework.GuiTestBase
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.framework.GuiTestUtil.waitUntilFound
import com.intellij.util.net.HttpConfigurable
import org.fest.swing.core.GenericTypeMatcher
import org.fest.swing.core.SmartWaitRobot
import org.fest.swing.exception.ComponentLookupException
import org.fest.swing.exception.LocationUnavailableException
import org.fest.swing.fixture.*
import org.fest.swing.timing.Condition
import org.fest.swing.timing.Pause
import java.awt.Component
import java.awt.Container
import java.lang.reflect.InvocationTargetException
import javax.swing.*
import javax.swing.text.JTextComponent


/**
 * @author Sergey Karashevich
 */
open class GuiTestCase : GuiTestBase() {


  class GuiSettings internal constructor() {


    init {
      GeneralSettings.getInstance().isShowTipsOnStartup = false
      GuiTestUtil.setUpDefaultProjectCreationLocationPath()
      GuiTestUtil.setUpSdks()
      val ideSettings = HttpConfigurable.getInstance()
      ideSettings.USE_HTTP_PROXY = false
      ideSettings.PROXY_HOST = ""
      ideSettings.PROXY_PORT = 80
      if (IS_UNDER_TEAMCITY) GitSettings.setup()
    }

    companion object {

      private val lock = Any()

      private var SETTINGS: GuiSettings? = null

      fun setUp(): GuiSettings {
        synchronized(lock) {
          if (SETTINGS == null) SETTINGS = GuiSettings()
          return SETTINGS as GuiSettings
        }
      }
    }

  }

  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    myRobot = SmartWaitRobot()
    GuiSettings.setUp()
  }

  @Throws(InvocationTargetException::class, InterruptedException::class)
  override fun tearDown() {
    GitSettings.restore()
    super.tearDown()
  }

  companion object {
    val IS_UNDER_TEAMCITY = System.getenv("TEAMCITY_VERSION") != null
  }

  //*********CONTEXT FUNCTIONS ON LAMBDA RECEIVERS
  fun welcomeFrame(func: WelcomeFrameFixture.() -> Unit) {func.invoke(welcomeFrame())}
  fun ideaFrame(func: IdeFrameFixture.() -> Unit) { func.invoke(findIdeFrame())}
  fun dialog(title: String? = null, func: JDialogFixture.() -> Unit) {func.invoke(dialog(title))}
  fun simpleProject(func: IdeFrameFixture.() -> Unit) {func.invoke(importSimpleProject())}
  fun projectWizard(func: NewProjectWizardFixture.() -> Unit) {func.invoke(findNewProjectWizard())}

  fun IdeFrameFixture.projectView(func: ProjectViewFixture.() -> Unit) {func.invoke(this.projectView)}

  //*********FIXTURES METHODS WITHOUT ROBOT and TARGET; KOTLIN ONLY
  fun <S, C : Component> ComponentFixture<S, C>.jList(containingItem: String? = null): JListFixture = if (target() is Container) jList(
    target() as Container, containingItem)
  else throw UnsupportedOperationException("Sorry, unable to find JList component with ${target().toString()} as a Container")

  fun <S, C : Component> ComponentFixture<S, C>.button(name: String): JButtonFixture = if (target() is Container) button(
    target() as Container, name)
  else throw UnsupportedOperationException(
    "Sorry, unable to find JButton component named by \"${name}\" with ${target().toString()} as a Container")

  fun <S, C : Component> ComponentFixture<S, C>.combobox(labelText: String): JComboBoxFixture = if (target() is Container) combobox(
    target() as Container, labelText)
  else throw UnsupportedOperationException(
    "Sorry, unable to find JComboBox component near label by \"${labelText}\" with ${target().toString()} as a Container")

  fun <S, C : Component> ComponentFixture<S, C>.checkbox(labelText: String): CheckBoxFixture = if (target() is Container) checkbox(
    target() as Container, labelText)
  else throw UnsupportedOperationException(
    "Sorry, unable to find JCheckBox component near label by \"${labelText}\" with ${target().toString()} as a Container")

  fun <S, C : Component> ComponentFixture<S, C>.actionLink(name: String): ActionLinkFixture = if (target() is Container) actionLink(
    target() as Container, name)
  else throw UnsupportedOperationException(
    "Sorry, unable to find ActionLink component by name \"${name}\" with ${target().toString()} as a Container")

  fun <S, C : Component> ComponentFixture<S, C>.actionButton(actionName: String): ActionButtonFixture = if (target() is Container) actionButton(
    target() as Container, actionName)
  else throw UnsupportedOperationException(
    "Sorry, unable to find ActionButton component by action name \"${actionName}\" with ${target().toString()} as a Container")

  fun <S, C : Component> ComponentFixture<S, C>.radioButton(textLabel: String): JRadioButtonFixture = if (target() is Container) radioButton(
    target() as Container, textLabel)
  else throw UnsupportedOperationException(
    "Sorry, unable to find RadioButton component by label \"${textLabel}\" with ${target().toString()} as a Container")

  fun <S, C : Component> ComponentFixture<S, C>.textfield(textLabel: String): JTextComponentFixture = if (target() is Container) textfield(
    target() as Container, textLabel)
  else throw UnsupportedOperationException(
    "Sorry, unable to find JTextComponent (JTextField) component by label \"${textLabel}\" with ${target().toString()} as a Container")

  fun <S, C : Component> ComponentFixture<S, C>.jTree(path: String? = null): JTreeFixture = if (target() is Container) jTree(
    target() as Container, path)
  else throw UnsupportedOperationException(
    "Sorry, unable to find JTree component \"${if (path != null) "by path ${path}" else ""}\" with ${target().toString()} as a Container")

  fun <S, C : Component> ComponentFixture<S, C>.popupClick(itemName: String) = if (target() is Container) popupClick(
    target() as Container, itemName)
  else throw UnsupportedOperationException(
    "Sorry, unable to find Popup component with ${target().toString()} as a Container")


  //*********COMMON FUNCTIONS WITHOUT CONTEXT
  fun typeText(text: String) = GuiTestUtil.typeText(text, myRobot, 10)
  fun invokeAction(keyStroke: String) = GuiTestUtil.invokeActionViaShortcut(myRobot, keyStroke)


  fun ideFrame() = findIdeFrame()!!
  fun welcomeFrame() = findWelcomeFrame()
  fun dialog(title: String? = null): JDialogFixture {
    if (title == null) {
      val jDialog = waitUntilFound(myRobot, object : GenericTypeMatcher<JDialog>(JDialog::class.java) {
        override fun isMatching(p0: JDialog): Boolean = true
      })
      return JDialogFixture(myRobot, jDialog)
    }
    else {
      return JDialogFixture.find(myRobot, title)
    }
  }

  //*********PRIVATE REALISATIONS
  private fun jList(container: Container, containingItem: String? = null): JListFixture {

    Pause.pause(object : Condition("Finding for JList") {
      override fun test(): Boolean {
        val lists = myRobot.finder().findAll(container, { it is JList<*> })
        return !lists.isEmpty()
      }
    }, GuiTestUtil.SHORT_TIMEOUT)

    val lists = myRobot.finder().findAll(container, { component -> component is JList<*> })
    if (lists.size == 1)
      return JListFixture(myRobot, lists.first() as JList<*>)
    else {
      if (containingItem == null) throw ComponentLookupException("Found more than one JList, please specify item")
      val filterJList: (JList<*>) -> Boolean = { myList -> (0..(myList.model.size - 1)).any { myList.model.getElementAt(it).toString() == containingItem } }
      return JListFixture(myRobot, lists.filter { filterJList(it as JList<*>) }.first() as JList<*>)
    }
  }

  private fun button(container: Container, name: String): JButtonFixture {
    val jButton = GuiTestUtil.waitUntilFound(myRobot, container, object : GenericTypeMatcher<JButton>(JButton::class.java) {
      override fun isMatching(jButton: JButton): Boolean = (jButton.text == name)
    })

    return JButtonFixture(myRobot, jButton)
  }

  private fun combobox(container: Container, labelText: String): JComboBoxFixture {
    //wait until label has appeared
    GuiTestUtil.waitUntilFound(myRobot, container, object : GenericTypeMatcher<JLabel>(JLabel::class.java) {
      override fun isMatching(jLabel: JLabel): Boolean = (jLabel.text == labelText)
    })

    return GuiTestUtil.findComboBox(myRobot, container, labelText)
  }

  private fun checkbox(container: Container, labelText: String): CheckBoxFixture {
    //wait until label has appeared
    GuiTestUtil.waitUntilFound(myRobot, container, object : GenericTypeMatcher<JCheckBox>(JCheckBox::class.java) {
      override fun isMatching(checkBox: JCheckBox): Boolean = (checkBox.text == labelText)
    })

    return CheckBoxFixture.findByText(labelText, container, myRobot, false)
  }

  private fun actionLink(container: Container, name: String) = ActionLinkFixture.findActionLinkByName(name, myRobot, container)

  private fun actionButton(container: Container, actionName: String) = ActionButtonFixture.findByActionId(actionName, myRobot, container)

  private fun radioButton(container: Container, labelText: String) = GuiTestUtil.findRadioButton(myRobot, container, labelText)

  private fun textfield(container: Container, labelText: String): JTextComponentFixture {
    GuiTestUtil.waitUntilFound(myRobot, container, object : GenericTypeMatcher<JLabel>(JLabel::class.java) {
      override fun isMatching(jLabel: JLabel): Boolean = (jLabel.text == labelText)
    })
    return JTextComponentFixture(myRobot, myRobot.finder().findByLabel(labelText, JTextComponent::class.java))
  }

  private fun popupClick(container: Container, itemName: String) {
    GuiTestUtil.clickPopupMenuItem(itemName, false, container, myRobot)
  }


  private fun jTree(container: Container, path: String? = null): JTreeFixture {
    val myTree: JTree?
    if (path == null) {
      myTree = waitUntilFound(myRobot, container, object : GenericTypeMatcher<JTree>(JTree::class.java) {
        override fun isMatching(p0: JTree) = true
      })
    }
    else {
      myTree = waitUntilFound(myRobot, container, object : GenericTypeMatcher<JTree>(JTree::class.java) {
        override fun isMatching(p0: JTree): Boolean {
          try {
            JTreeFixture(myRobot, p0).node(path)
            return true
          } catch(locationUnavailableException: LocationUnavailableException) {
            return false
          }
        }
      })
    }
    if (myTree.javaClass.name == "com.intellij.openapi.options.newEditor.SettingsTreeView\$MyTree") {
     //replace cellreader
      return JTreeFixture(myRobot, myTree).replaceCellReader(SettingsTreeCellReader())
    } else
    return JTreeFixture(myRobot, myTree)
  }

}
