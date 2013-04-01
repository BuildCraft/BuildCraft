## Welcome to Buildcraft on GitHub

### Contributing
If you wish to submit a pull request to fix bugs or broken behaviour feel free to do so. If you would like to add 
features or change existing behaviour or balance, please discuss it with Sengir or Krapht before submiting the pull request.

### Compiling and packaging Buildcraft
1. Ensure that `Apache Ant` (found [here](http://ant.apache.org/)) is installed correctly on your system.
 * Linux users will need the latest version of astyle installed as well.
2. Create a base directory for the build
3. Clone the Buildcraft repository into `basedir/BuildCraft/`
 * Optional: Copy BuildCraft localization repository into `basedir/BuildCraft-Localization`
4. Navigate to basedir/Buildcraft in a shell and run `ant` (this will take 2-5 minutes)
5. The compiled and obfuscated jar will be in basedir/bin

Your directory structure should look like this:
***

    basedir
    \- jars
     |- minecraft_server.jar
     \- bin
      |- minecraft.jar
      |- ...
    \- BuildCraft
     |- buildcraft_resources
     |- common
     |- ...
    \- BuildCraft-Localization
     |- lang

***

### Localizations

Localizations can be submitted [here](https://github.com/BuildCraft/BuildCraft-Localization). Localization PRs against
this repository will have to be rejected.
