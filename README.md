# ReactGUI
[![](https://jitpack.io/v/iHDeveloper/ReactGUI.svg)](https://jitpack.io/#iHDeveloper/ReactGUI)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/eb87b1864b9c4497ba093395c3250e3c)](https://app.codacy.com/gh/iHDeveloper/ReactGUI?utm_source=github.com&utm_medium=referral&utm_content=iHDeveloper/ReactGUI&utm_campaign=Badge_Grade_Settings)

A spigot plugin API for making reactive inventories. Using the philosophy of [React](https://github.com/facebook/react).
This plugin will allow you on creating GUIs faster. And, focus on designing them without caring about how it would work.

The plugin includes a rich standard library to help you build using built-in components.

## Architecture
The API assumes that the inventory is called **Screen**.
And, the items are called **Components**.

Each **Screen** has multiple components up to 54. With up to 6 columns. Each row has 9 components at least

Each **Component** has multiple states. Changing one of those states will flag the **Component** to be re-rendered.

The screen gets re-rendered every tick. It will re-render any flagged **Components**.

[Spigot](https://www.spigotmc.org/resources/lib-react-gui.91047/) - [JitPack](https://jitpack.io/#iHDeveloper/ReactGUI)

## Examples
Checkout the examples
- [Java](https://github.com/iHDeveloper/ReactGUI/tree/master/test/src/main/java/me/ihdeveloper/react/gui/test/screen)
- [Kotlin](https://github.com/iHDeveloper/ReactGUI/tree/master/test/src/main/kotlin/me/ihdeveloper/react/gui/test/gui)

