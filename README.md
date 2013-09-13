## Welcome to Buildcraft on GitHub

### Contributing
If you wish to submit a pull request to fix bugs or broken behaviour feel free to do so. If you would like to add 
features or change existing behaviour or balance, please discuss it with Sengir or Krapht before submiting the pull request.

Do not submit pull requests which solely "fix" formatting. As these kinds of changes are usually very intrusive in commit history and everyone has their own idea what "proper formatting" is, they should be done by one of the main contributors. 
Please only submit "code cleanup", if the changes actually have a substantial impact on readability.

### Reporting issues
* Before reporting an issue, please check that it has not been reported before.
* Issues are for bugs/crashes, please do not use them to ask general questions.
* Always include the version you are having trouble with. Or if you're building from source, which source you're building.
If you don't, we might assume that you are using latest-greatest and waste a bunch of time trying to reproduce 
a problem that might have fixed been already. Such things makes for very grumpy devs. Grumpy devs spend 
less time coding and more time doing stuff that makes them less grumpy.
* If the issues occurs on a server, be sure it's a vanilla forge server and <b>not</b> a mcpc+ server.

#### Frequently reported
* java.lang.AbstractMethodError - Incompatibility between BC/Forge/Mod using BC API. Usually not a BC issue
* java.lang.NoSuchMethodException - Same as above
* Render issue (Quarry causes flickering) - Try without optifine, if it still flickers, report it

### Compiling and packaging Buildcraft
1. Ensure that `Apache Ant` (found [here](http://ant.apache.org/)) and `Git` (found [here](http://git-scm.com/)) are installed correctly on your system.
 * Linux users will need the latest version of astyle installed as well.
1. Create a base directory for the build
1. Clone the Buildcraft repository into `basedir/BuildCraft/`
 * Optional: Copy BuildCraft localization repository into `basedir/BuildCraft-Localization`
1. Navigate to basedir/Buildcraft in a shell and run `ant` (this will take 2-5 minutes)
1. The compiled and obfuscated jar will be in basedir/bin

Your directory structure should look like this before running ant:
***

    basedir
    \- buildcraft
     |- buildcraft_resources
     |- common
     |- ...
    \- buildcraft.localization
     |- lang

***

And like this after running ant:
***

    basedir
    \- buildcraft
     |- bin
     |- build
     |- buildcraft_resources
     |- common
     |- download
     |- ...
    \- buildcraft.localization
     |- lang

***

### Localizations

Localizations can be submitted [here](https://github.com/BuildCraft/BuildCraft-Localization). Localization PRs against
this repository will have to be rejected.
