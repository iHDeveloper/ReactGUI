# ReactGUI
A spigot plugin API for making reactive inventories. Using the philosophy of [React](https://github.com/facebook/react).
This plugin will allow you on creating GUIs faster. And, focus on designing them without caring about how it would work.

The plugin includes a rich standard library to help you build using built-in components.

## Samples of the prototype of the project

- Creating custom component like a **Button**
![image](https://user-images.githubusercontent.com/20463031/102676135-2cb2e000-41ad-11eb-8157-096227521b12.png)

- Creating a simple screen
![image](https://user-images.githubusercontent.com/20463031/102676164-4a804500-41ad-11eb-8381-3a3a5bcacf88.png)

- Test the simple screen

![image](https://user-images.githubusercontent.com/20463031/102676192-6be13100-41ad-11eb-8690-e1ca15632aa7.png)


## Architecture
The API assumes that the inventory is called **Screen**.
And, the items are called **Components**.

Each **Screen** has multiple components up to 54. With up to 6 columns. Each row has 9 components at least

Each **Component** has multiple states. Changing one of those states will flag the **Component** to be re-rendered.

The screen gets re-rendered every tick. It will re-render any flagged **Components**.

## Getting started
Checkout the examples [Java](https://github.com/iHDeveloper/ReactGUI/tree/master/test/src/main/java/me/ihdeveloper/react/gui/test/screen) / [Kotlin](https://github.com/iHDeveloper/ReactGUI/tree/master/test/src/main/kotlin/me/ihdeveloper/react/gui/test/gui)
