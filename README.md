<img src="https://github.com/perfectsense/gyro/blob/master/etc/gyro.png" height="200"/>

[![Gitter](https://img.shields.io/gitter/room/perfectsense/gyro)](https://gitter.im/perfectsense/gyro)
[![TravisCI](https://api.travis-ci.org/perfectsense/gyro-azure-provider.svg?branch=master)](https://travis-ci.org/perfectsense/gyro-azure-provider)
[![Apache License 2.0](https://img.shields.io/github/license/perfectsense/gyro-azure-provider)](https://github.com/perfectsense/gyro-azure-provider/blob/master/LICENSE)


The **Azure Provider for Gyro** enables users to easily work with Microsoft Azure Services. The Azure provider extends Gyro allowing you to manage your Azure infrastructure.

To learn more about Gyro see [getgyro.io](https://getgyro.io) and [gyro](https://github.com/perfectsense/gyro). 

* [Resource Documentation](https://gyro.dev/providers/azure/index.html)
* [Submit an Issue](https://github.com/perfectsense/gyro-azure-provider/issues)
* [Getting Help](#getting-help)

## Using the Azure Provider

### Azure Account ###

Before you can use Azure provider, you will need an Azure account. Please see [Sign Up for Azure](https://azure.microsoft.com/en-us/) to create an Azure Account.

Once your account is set up and ready to be used, you need to set up an [Azure service principal](https://docs.microsoft.com/en-us/azure/active-directory/develop/app-objects-and-service-principals) and save the credentials in a file in the following format

```
# sample management library properties file
subscription=########-####-####-####-############
client=########-####-####-####-############
key=XXXXXXXXXXXXXXXX
tenant=########-####-####-####-############
managementURI=https\://management.core.windows.net/
baseURL=https\://management.azure.com/
authURL=https\://login.windows.net/
graphURL=https\://graph.windows.net/
``` 
For more info refer [Azure file based authentication](https://docs.microsoft.com/en-us/azure/java/java-sdk-azure-authenticate#file-based-authentication-preview) and  [Setting up Azure credentials for Gyro](https://gyro.dev/providers/azure/index.html#authentication).

### Using The Provider ###

#### Import ####

Load the Azure provider in your project by consuming it as a `plugin` directive in your init file. It uses the format `@plugin: gyro:gyro-azure-provider:<version>`.

```shell
@repository: 'https://artifactory.psdops.com/gyro-releases'
@plugin: 'gyro:gyro-azure-provider:1.0.0'
```

#### Authentication ####

Provide the Azure provider with the path of the credentials file by defining the following in your `.gyro/init.gyro` file:

```
@credentials 'azure::credentials'
    log-level: 'basic'
    region: 'westus'
    credential-file-path: '<azure_credentials_file_path>'
@end
```

See [Azure authentication for Gyro](https://gyro.dev/providers/azure/index.html#authentication) for more details.

## Supported Services

* [AccessManagement](https://gyro.dev/providers/azure/accessmanagement/index.html)
* [CDN](https://gyro.dev/providers/azure/cdn/index.html)
* [Compute](https://gyro.dev/providers/azure/compute/index.html)
* [CosmosDb](https://gyro.dev/providers/azure/cosmosdb/index.html)
* [DNS](https://gyro.dev/providers/azure/dns/index.html)
* [Identity](https://gyro.dev/providers/azure/identity/index.html)
* [KeyVault](https://gyro.dev/providers/azure/keyvault/index.html)
* [Network](https://gyro.dev/providers/azure/network/index.html)
* [Resource](https://gyro.dev/providers/azure/resources/index.html)
* [Sql](https://gyro.dev/providers/azure/sql/index.html)
* [Storage](https://gyro.dev/providers/azure/storage/index.html)

## Developing the Azure Provider

The provider is written in Java using Gradle as the build tool.

We recommend installing [AdoptOpenJDK](https://adoptopenjdk.net/) 11 or higher if you're going to contribute to this provider. 

Gyro uses the Gradle build tool. Once you have a JDK installed building is easy, just run ./gradlew at the root of the Gyro project. This wrapper script will automatically download and install Gradle for you, then build the provider:
```shell
$ ./gradlew
Downloading https://services.gradle.org/distributions/gradle-5.2.1-all.zip
..............................................................................................................................

Welcome to Gradle 5.2.1!

Here are the highlights of this release:
 - Define sets of dependencies that work together with Java Platform plugin
 - New C++ plugins with dependency management built-in
 - New C++ project types for gradle init
 - Service injection into plugins and project extensions

For more details see https://docs.gradle.org/5.2.1/release-notes.html

Starting a Gradle Daemon, 1 stopped Daemon could not be reused, use --status for details

.
.
.

BUILD SUCCESSFUL in 17s
38 actionable tasks: 28 executed, 10 from cache
$
```

## Getting Help

* Join the Gyro community chat on [Gitter](https://gitter.im/perfectsense/gyro).
* Take a look at the [documentation](https://gyro.dev/providers/azure/index.html) for tutorial and examples.

## License

This software is open source under the [Apache License 2.0](https://github.com/perfectsense/gyro-azure-provider/blob/master/LICENSE).
