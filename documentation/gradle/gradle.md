# Gradle
Is a modern and flexible build system for JVM based languages. This template is configured with the most recent version
as of writing (20230601) which is version 8.1.1.

## Why use Gradle over Maven?
The biggest reason to use Gradle over Maven is how much greater the flexibility is. To start you have the option of 
writing the build scripts in either groovy or kotlin instead of XML (this project is in groovy. Since Gradle uses actual
scripting there is and additional myriad of plugins that can be used to easily extend its functionality as well as 
writing custom tasks to vastly simplify your build process.

## This Build
This is a relatively advanced build with many features already configured. While a basic Gradle build will only require
a single `build.gradle` file as the build grows the file gets hard to manage. For that reason it is a good idea to break
the build into multiple steps. The various build steps are stored in `gradle/steps`.

*Note: Some parts of this build are commented out as they only pertain to Spring Boot based applications*

### What does this build include?
This build includes the following configurations ...
- Pre-configured plugins
- Pre-configured environment variable access
  - Note: These names may need to be edited / updated to the project purposes
- Ability to override certain configurations based on environment variable configuration
- Examples of custom repository connections for both GitHub and BitBucket
- Custom versioning that can include a build number
- Basic Slack alerts (requires configuration)
- Examples of how to add additional source sets
- Pre-built Slack messages for specific build alerts
- Pre-configured sonar (requires project name)
- Pre-configured jacoco / checkstyle / pmd
  - Note: In order to change coverage percents and packages edit the values in `gradle.properties`, DO NOT EDIT THE `.gradle` files!!!
- Dependency configuration
  - Note: All dependency versions should be stored in `gradle.properties`, this ensures minimal touching of any `.gradle` files.
- Easy test configuration through the `testlogger` dsl block
- Automatic retries (configurable) of any flakey tests
- Code coverage report through JaCoCo

### How to update Gradle wrapper
To upgrade the Gradle wrapper execute the following (replace the x.x.x with the desired version) ...
```shell
./gradlew wrapper --gradle-version x.x.x
```
To verify the upgrade was successful execute the following ...
```shell
./gradlew -v
```