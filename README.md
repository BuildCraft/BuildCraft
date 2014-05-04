## Welcome to Buildcraft on GitHub

### Rule on opening issues
**Please only open an issue if you are planning to either fix the bug or implement the feature.**

If you're part of the BuildCraft team, this means that you will set yourself as the assignee. If you're an external contributor, that means that your issue must be a pull request. We are trying to keep the issues list to things that are scheduled for implementation / correction. If you would like to report a bug but do not plan to fix it, please report on the bug reporting board (http://mod-buildcraft.com/forums/index.php?board=2.0). If you would like to suggest a feature, you can start a thread on the feature ideas board (http://mod-buildcraft.com/forums/index.php?board=4.0). New issues that do not follow this rule will get closed, existing ones will be closed progressively.

Assigned features and issues that are not scheduled for any particular release and is left open for several months will get closed eventually. BuildCraft being an open-source project, if this particular fix or feature is important to you, feel free to go ahead and submit a pull request for it.

### Contributing
If you wish to submit a pull request to fix bugs or broken behaviour feel free to do so. If you would like to add 
features or change existing behaviour or balance, please discuss it on the feature ideas board before (http://mod-buildcraft.com/forums/index.php?board=4.0 before).

Do not submit pull requests which solely "fix" formatting. As these kinds of changes are usually very intrusive in commit history and everyone has their own idea what "proper formatting" is, they should be done by one of the main contributors. 
Please only submit "code cleanup", if the changes actually have a substantial impact on readability.

### Reporting issues
* Do not open an issue if you're not planning to fix it, instead open a thread on http://mod-buildcraft.com/forums/index.php?board=2.0
* Before reporting an issue, please check that it has not been reported before.
* Issues are for bugs/crashes, please do not use them to ask general questions.
* Always include the version you are having trouble with. Or if you're building from source, which source you're building.
If you don't, we might assume that you are using latest-greatest and waste a bunch of time trying to reproduce 
a problem that might have fixed been already. Such things makes for very grumpy devs. Grumpy devs spend 
less time coding and more time doing stuff that makes them less grumpy.
* If the issues occurs on a server, be sure it's a vanilla forge server and <b>not</b> a mcpc+ server.
* Issues with any logs mentioning Optifine will be closed on sight! Remove Optifine before reporting any issue.

#### Frequently reported
* java.lang.AbstractMethodError - Incompatibility between BC/Forge/Mod using BC API. Usually not a BC issue
* java.lang.NoSuchMethodException - Same as above
* Render issue (Quarry causes flickering) - Try without optifine, if it still flickers, report it

### Compiling and packaging Buildcraft
1. Ensure that `Java` (found [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)), `Git` (found [here](http://git-scm.com/)) are installed correctly on your system.
 * Optional: Install `Gradle` (found [here](http://www.gradle.org/downloads))
1. Create a base directory for the build
1. Clone the Buildcraft repository into 'baseDir/BuildCraft/'
 * Optional: Copy BuildCraft localization repository into `baseDir/BuildCraft-Localization`
1. Navigate to basedir/BuildCraft in a shell and run one of two commands:
 * `gradlew setupCIWorkspace build` to just build a current jar (this may take a while).
 * `gradlew setupDecompWorkspace` to setup a complete developement enviroment.
 * With `Gradle` installed: use `gradle` instead of `gradlew`
 * On Windows: use `gradlew.bat` instead of `gradlew`
1. The compiled and obfuscated jar will be in 'baseDir/BuildCraft/build/libs'

Your directory structure should look like this before running gradle:
***

    baseDir
    \- BuildCraft
     |- buildcraft_resources
     |- common
     |- ...
    \- BuildCraft-Localization
     |- lang

***

And like this after running gradle:
***

    basedir
    \- BuildCraft
     |- .gradle
     |- build
     |- buildcraft_resources
     |- common
     |- ...
    \- BuildCraft-Localization
     |- lang

***

### Localizations

Localizations can be submitted [here](https://github.com/BuildCraft/BuildCraft-Localization). Localization PRs against
this repository will have to be rejected.

### Depending on BuildCraft

add the following to your build.gradle file
```
dependencies {
    compile 'com.mod-buildcraft:buildcraft:6.0.8:dev'
}
```
where `6.0.8` is the desired version of BuildCraft
