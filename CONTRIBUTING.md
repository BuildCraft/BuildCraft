# Contributing to BuildCraft

Thank you for your interest in contributing to BuildCraft!

## Reporting Issues

Please open an issue for bug reports only if:

* You are sure the bug is caused by BuildCraft and not by any other mod
* You have at least one of the following:
  * A crash report
  * Means of reproducing the bug
  * Screenshots/videos to demonstrate the bug

**If you are not sure if a bug report is valid, please use the "Ask Help!" subforum.**

Please only use **official BuildCraft releases** for bug reports unless otherwise told. Custom builds are unsupported and will not get support from the developers.

Please check if the bug has been reported beforehand and provide the version of BuildCraft used.

## Pull Requests

### Bug Fixes
Feel free to submit pull requests to fix bugs or broken behaviour.

### New Features
If you would like to add features or change existing behaviour or balance, please discuss it on Discord before submitting a PR: https://discord.gg/v4geqgA

### Code Formatting
Do not submit pull requests which solely "fix" formatting. As these kinds of changes are usually very intrusive in commit history and everyone has their own idea what "proper formatting" is, they should be done by one of the main contributors.

Please only submit "code cleanup" if the changes actually have a substantial impact on readability.

### After Submitting
Complex changes are introducing bugs, and as thorough as testing and peer review may be, there will be bugs. Please carry on playing your changes after initial commit and fix residual issues. It is extremely frustrating for others to spend days fixing regressions introduced by unmaintained submissions.

## Building from Source

### Prerequisites
* Java (JDK 8 or higher)
* Git

### Setup
1. Clone the repository
2. Initialize submodules: `git submodule init && git submodule update`
3. Run one of:
   * `./gradlew setupCIWorkspace build` - Build a JAR
   * `./gradlew setupDecompWorkspace` - Setup development environment

### Common Issues
* `AbstractMethodError` or `NoSuchMethodException` - A mod has not updated to the current BuildCraft API, or you are not using the correct version for your Forge/Minecraft versions
* Render issues with Quarry - Try without OptiFine first

## Dependencies

BuildCraft depends on:
* Minecraft 1.12.2
* Forge 14.23.1.2593

## License

BuildCraft is licensed under the [Mozilla Public License 2.0](https://mozilla.org/MPL/2.0/).
