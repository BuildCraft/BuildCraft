## Welcome to Buildcraft on GitHub

### Contributing
If you wish to submit a pull request to fix bugs or broken behaviour feel free to do so. If you would like to add 
features or change existing behaviour or balance, please discuss it with Sengir or Krapht before submiting the pull request.

### Reporting issues
Issues are for bugs/crashes, please do not use them to ask general questions.
Always include the version you are having trouble with. Or if you're building from source, which source you're building.
If you don't, we might assume that you are using latest-greatest and waste a bunch of time trying to reproduce 
a problem that might have fixed been already. Such things makes for very grumpy devs. Grumpy devs spend 
less time coding and more time doing stuff that makes them less grumpy.

### Compiling and packaging Buildcraft
1. Ensure that `Apache Ant` (found [here](http://ant.apache.org/)) is installed correctly on your system.
 * Linux users will need the latest version of astyle installed as well.
1. Create a base directory for the build
1. Clone the Buildcraft repository into `basedir/BuildCraft/`
 * Optional: Copy BuildCraft localization repository into `basedir/BuildCraft-Localization`
1. Navigate to basedir/Buildcraft in a shell and run `ant` (this will take 2-5 minutes)
1. The compiled and obfuscated jar will be in basedir/bin

Your directory structure should look like this before running ant:
***

    basedir
    \- BuildCraft
     |- buildcraft_resources
     |- common
     |- ...
    \- BuildCraft-Localization
     |- lang

***

And like this after running ant:
***

    basedir
    \- BuildCraft
     |- bin
     |- build
     |- buildcraft_resources
     |- common
     |- download
     |- ...
    \- BuildCraft-Localization
     |- lang

***

### Localizations

Localizations can be submitted [here](https://github.com/BuildCraft/BuildCraft-Localization). Localization PRs against
this repository will have to be rejected.
