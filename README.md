# BoeingGradleLambdaTemplate

**Please read at least the whole README before copying this project**

This template project is what any new Lambda projects should use as a base to get started. It is intended to be generic
and support Lambda functions in AWS/GCP/Azure as well as track the various incarnations (versions) of those services.
Reusable / generic code should be placed in the models / service or other respective packages. For the Lambda connection
itself that code should be placed under the lambda package in the respective cloud provider package.

This template provides a simple example of a Person service which can create a new Person in a DynamoDB table and can 
retrieve that person either through an url param or through a query param (see postman example file for clarity).

To get started create the new repository then copy everything within the BoeingGradleLambdaTemplate folder 
(not the top level directory) into the new top level directory.

## What does this template provide?
This template provides a pre-architected lambda application skeleton to jumpstart future development by including the
following ...
- predefined package structure
- designed to support multiple clouds
- designed to support multiple versions
- preconfigured Gradle build with many bells and whistles for testing and code quality
  - For more info on the Gradle build see [here](documentation/gradle/gradle.md)
- preconfigured Serverless Framework deployment options
  - For more info on the Serverless Framework see [here](documentation/serverless/serverless.md)
- prebuilt postman for all example endpoints
  - For more info on Postman and included starter Postman file see [here](documentation/postman/postman.md)

## How to build and deploy (Quick Start)
As stated above this project uses Gradle and Serverless Framework. Due to how Lambda functions work a standard Gradle
build will not generate the correct jar for deployment. Lambda functions require that all dependencies must be packaged
as part of the deployed artifact, this is sometime referred to as a fatJar, shaded jar, or shadowJar. In the case of Gradle
we need to build a shadow jar. This can be accomplished by executing the following command ...

### Build the artifact
```shell
./gradlew clean shadow
```
The result of the above command will be generated under the `build/libs` directory and should have a name of whatever
is specified for `rootProject.name` in the `settings.gradle` file, the version, and should include `-all` in the name.

*Note: The designation `-all` is what indicates that the artifact is a shadow jar and not a standard Gradle build jar
(which would NOT include `-all` in the name)*

**Note: There is a size limit for jars (250mb?) and zip (50mb) files for lambdas**

### Deploy using serverless
In order to deploy you need to be authenticated through AWS Cli and you may need to manually move the credentials into 
the appropriate file for serverless to function correctly. By default, this will deploy to `dev`. This can be done as follows ...
#### Authenticate to AWS
```shell
aws sso login
```
#### Copy AWS credentials for Serverless
```shell
ssocreds -p PROFILE-NAME
```
*Note: ssocreds is a plugin that you will need to install via NPM (I will work on adding instructions for this)*
#### Deploy
```shell
serverless deploy --aws-profile PROFILE-NAME
```
*Note: PROFILE-NAME must match between ssocreds and serverless deploy commands.*

#### Un-Deploy
It is worth noting that you can not just deploy over a previous deployment using serverless, you must first remove the 
previous instance. That can be done with the reverse of the above command ...
```shell
serverless remove --aws-profile PROFILE-NAME
```

### Deploy using serverless with stages
Using stages is serverless primary way of dealing with different environments and there are two main ways in which this
may work depending on the setup.
#### In-built API Gateway stages
This is the simpler of the two, AWS API Gateway has the inbuilt concept of stages. What this means is if the configured
profiles all use the same AWS account like the following example 
```yaml
custom:
  profiles:                                             # These values MUST match an AWS Profile in the `./aws/credentials` file. example [default]
    dev:   default
    test:  default
    rgrs:  default
    stage: default
    prod:  default
```
Is that the same API Gateway will use the same base server and differentiate the environment by url alone like
```shell
endpoints:
  GET - https://cxqduu7k13.execute-api.us-east-2.amazonaws.com/dev/persons-http
  GET - https://cxqduu7k13.execute-api.us-east-2.amazonaws.com/dev/persons-http/{id}
  PUT - https://cxqduu7k13.execute-api.us-east-2.amazonaws.com/dev/persons-http

endpoints:
  GET - https://cxqduu7k13.execute-api.us-east-2.amazonaws.com/prod/persons-http
  GET - https://cxqduu7k13.execute-api.us-east-2.amazonaws.com/prod/persons-http/{id}
  PUT - https://cxqduu7k13.execute-api.us-east-2.amazonaws.com/prod/persons-http

ect ...
```
In many cases this may not work with actual enterprise AWS account configuration as each environment will be its own 
account which brings us to the next case.

#### Multi-account deployment (not tested yet)
Multi-account deployment is actually pretty simple, reusing the same example above but with different accounts
```yaml
custom:
  profiles:                                             # These values MUST match an AWS Profile in the `~/.aws/credentials` file. example [default]
    dev:   devAccount
    test:  testAccount
    rgrs:  regressionAccount
    stage: stageAccount
    prod:  prodAccount
```
Ensure that your `~/.aws/credentials` has an account for each environment, ie. `[devAccount]`, `[testAccount]`, ect.
Then when you deploy with the following ...
```shell
serverless deploy --stage test
```
it will automatically set the AWS profile and push to the selected account. It will also still maintain the unique URL
naming convention in the environment. So in this case the test environment will only have the following urls.
```shell
endpoints:
  GET - https://cxzduu7k14.execute-api.us-east-2.amazonaws.com/test/persons-http
  GET - https://cxzduu7k14.execute-api.us-east-2.amazonaws.com/test/persons-http/{id}
  PUT - https://cxzduu7k14.execute-api.us-east-2.amazonaws.com/test/persons-http
```

## Troubleshooting

### ENOENT Error
If you see the following ...
```shell
╰ serverless deploy --aws-profile default                                                                                                                                                                                                                                                     17.0.6+10-LTS  

Deploying boeing-gradle-lambda-template to stage dev (us-east-2)

✖ Stack boeing-gradle-lambda-template-dev failed to deploy (0s)
Environment: darwin, node 20.2.0, framework 3.31.0, plugin 6.2.3, SDK 4.3.2
Credentials: Local, "default" profile
Docs:        docs.serverless.com
Support:     forum.serverless.com
Bugs:        github.com/serverless/serverless/issues

Error:
Cannot access package artifact at "build/libs/boeing-gradle-lambda-template-0.0.1-SNAPSHOT-all.jar": ENOENT: no such file or directory, access '/Users/xtheshadowgod/Repository/external/BoeingGradleLambdaTemplate/build/libs/boeing-gradle-lambda-template-0.0.1-SNAPSHOT-all.jar'
```
This indicates that the shadow jar is not present / created or the name is different. Check that the `*-all.jar` file is
present in `build/libs`. If the jar is present check the declared jar name in the `serverless.yml` file search for the
`artifact:` key.

## Best Practices with this project
Below are some of the best practices when working with this code base ...

### How to add new service version
In the event a service is not just being updated but a whole never version is being created then a new version package
should be added under `com.boeing.BoeingGradleLambdaTemplate` and should look as follows ...
```
com.boeing.BoeingGradleLambdaTemplate
    v1
        ...
    v2
        ...
```

### Postman
Always make sure the Postman examples file has been updated and re-exported into the project for easy testing and documentation.

### Changing the project name
To get started create the new repository then copy everything within the BoeingGradleLambdaTemplate folder
(not the top level directory) into the new top level directory.

Now that everything is inside the new top level project directory it should look something like this ...
```
MyNewProject
  .gradle
  .idea
  .serverless
  documentation
  gradle
  src
  ...
  ...
  ect
```
With that complete open the `settings.gradle` file and update the project name to match. The `rootProject.name` is what
controls the name of the generated jar file.

### Changing the package name
If you are creating a new project with a new name it is also recommended to update the top level package name from 
`com.boeing.BoeingGradleLambdaTemplate` to `com.boeing.MyNewProjectName`. This can be achieved by right-clicking the 
package in IntelliJ, selecting Refactor, and selecting Rename.