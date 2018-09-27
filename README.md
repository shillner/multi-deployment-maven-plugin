# Multi Deployment Maven Plugin

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.itemis.maven.plugins/multi-deployment-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.itemis.maven.plugins/multi-deployment-maven-plugin)

The purpose of this plugin is to enable the build to deploy the project artifacts to additional Maven repositories instead of just a single one.
This can be very useful when the artifacts of a single maven build shall be distributed into several repositories for different customers.


## Sample usage

    <project>
      ...
      <build>
        ...
        <plugins>
          ...
          <plugin>
            <groupId>com.itemis.maven.plugins</groupId>
            <artifactId>multi-deployment-maven-plugin</artifactId>
            <version>${version.multi-deployment-maven-plugin}</version>
            <executions>
              <execution>
                <id>additional-deployments</id>
                <phase>deploy</phase>
                <goals>
                  <goal>deploy</goal>
                </goals>
                <configuration>
                  <repositories>
                    <repository>
                      <id>nexus2</id>
                      <url>http://localhost:8082/nexus/content/repositories/snapshots/</url>
                      <releasesEnabled>false</releasesEnabled>
                    </repository>
                    <repository>
                      <id>nexus3</id>
                      <url>http://localhost:8083/nexus/content/repositories/releases/</url>
                      <snapshotsEnabled>false</snapshotsEnabled>
                    </repository>
                  </repositories>
                </configuration>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </build>
    </project>

Note that only one of the additional repositories has been enabled for snapshot builds whilst the other is not enabled for release builds. This means that the project artifacts will be deployed to the project snapshot repository as well as nexus2 during a normal snapshot build. A release build using the maven-release-plugin will deploy the artifacts to the project's release repository as well as nexus3.


## Integration with the unleash-maven-plugin

The [unleash-maven-plugin](https://github.com/shillner/unleash-maven-plugin) is a powerful and configurable alternative for the maven-release-plugin. In contrast to the maven-release-plugin the deployment of the artifacts is not part of the actual release build but will be performed as a succeeding step to provide a reliable rollback machanism for the releases.

For this reason the multi-deployment-plugin integration shown above won't work for the unleash-maven-plugin, unfortunately. But good news: The unleash-maven-plugin comes with its own multi-deployment feature! Simply add the additional repositories to the plugin configuration and everything works out-of-the-box ;)

Please have a look at the [documentation of the unleash-maven-plugin](https://github.com/shillner/unleash-maven-plugin/wiki).


## Goals and parameters

### deploy

Deploys all the project artifacts to the specified additional repositories. Please keep in mind that the ids of the repositories are used to query the Maven settings for servers in order to get the respective authentication data.

Below you can find a table containing all configuration parameters of the deploy goal:

| Name | Type | Default Value | Required | Description |
|------|:----:|:-------------:|-------------|
| repositories | `Repository` | - | true | Use this parameter to specify a set of repositories. |

The type `Repository` provides the following parameters:
| Name | Type | Default Value | Required | Description |
|------|:----:|:-------------:|-------------|
| id | `String` | - | true | The id of the server or repository which is used to figure out the server configuration of the Maven settings. This server configuration is then used to setup the repository authentication. |
| url | `String` | - | true | The URL of the repository. |
| snapshotsEnabled | `Boolean` | `true` | true | If snapshots shall be deployed to this repository or not. |
| releasesEnabled | `Boolean` | `true` | true | If releases shall be deployed to this repository or not. |
