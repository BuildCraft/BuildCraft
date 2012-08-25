## Welcome to Buildcraft on GitHub

### Official Builds
An official jenkins build server can be found [here](http://jenkins.mc-epoch.com:8080/). Jenkins will generate a new 
build every time a change is pushed to github.

### Contributing
If you wish to submit a pull request to fix bugs or broken behaivor feel free to do so. If you would like to add 
features or change existing behaivour, please discuss it with Sengir or Krapht before submiting the pull request.

### Compiling and packaging Buildcraft
1. Ensure that `Apache Ant` (found [here](http://ant.apache.org/)) is installed correctly on your system.
 * Linux users will need the latest version of astyle installed as well.
1. Create a base directory for the build
1. Clone the Buildcraft repository into `basedir/src/`
1. Copy the minecraft bin dir and minecraft_server.jar into `basedir/jars/`
1. Navigate to basedir/src in a shell and run `ant clean package` (this will take 2-5 minutes)
1. The compiled and obfuscated jars will be in basedir/build/dist

Your directory structure should look like this:
***

    basedir
    \- jars
     |- minecraft_server.jar
     \- bin
      |- minecraft.jar
      |- ...
    \- src
     |- buildcraft_client
     |- buildcraft_server
     |- ...

***