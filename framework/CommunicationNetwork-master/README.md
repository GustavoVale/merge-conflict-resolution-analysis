# CommunicationNetwork

The tool will be renamed to 4CsNet. 4CsNet build networks to help understanding code changes and interactions (communication) among contributors.

Code changes are the _contribution networks_ and interactions among contributors are the _communication networks_. We use git commit information to build contribution networks wrapped by [GitWrapper](https://github.com/...) and GitHub information to build the communication networks wrapped by [GitHubWrapper](https://github.com/...).

Contribution networks are built at chunk, file and merge scenario levels. It means that we may know all developers that change a chunk, a file, or a merge scenario. The scope of such networks are merge scenarios. So, we can know developers which change chunks and files regarding each merge scenario.

Communication networks can also be called _social networks_ and they are built using comments and events reported by _Issues_. There three approaches: awareness-based, pull-request-based, and changed-artifact-based. The awareness-based approach considers all events and comments of all issues while a merge scenario is opened. The idea come to overcome communication that happens in other communication channels. Hence, we are interested to retrieve the communication of contributors that were active during a merge scenario. The pull-request-based approach was created to get a more refined communication, hence, we retrieve only the communication of the merge scenario pull request and related issues. The changed-artifact-based approach came after we note that most of the merge scenario integrations are not made by means of pull-requests. Hence, aiming at obtain a more precise communication than the awareness-based and with greater coverage we created the changed-artifact-based. This approach retrieves the communication of issues opened during the merge scenarios and that refer to files changed in the merge scenario.

## Setup

### Gradle
4CsNet uses the Gradle build system.

Be sure that the files settings.gradle and build.gradle have the following lines

**settings.gradle**
```
#!groovy
includeFlat 'GitWrapper' // The name of the directory containing your clone of GitWrapper.
```

**build.gradle**
```
#!groovy
dependencies {
    compile project(':GitWrapper')
    compile project(':GitHubWrapper')
}
```
### GitHub Token
*Note: To get more than the unauthenticated limit of 60 requests per hour, you need to supply your own OAuth token. 
For further information and to generate your own token visit [https://github.com/settings/tokens](https://github.com/settings/tokens).*

The `GitHubRepository` object with token allows us to access to the local repository copy using native git calls as well as read only access to the GitHub API for issues (including comments and events) and pull requests.

A file should be added in the resources file with the .properties extension. For instance, 4CsNet.properties and the content should be "github.token=" and the number of your token.

### Database
Set JDBC url(server), user, and password in /resources/db.properties file. For instance,

      database.url=jdbc:mysql://localhost:3306/4cesNet-DB?serverTimezone=UTC&useSSL=false
      database.user=root
      database.password=root

If SSL is required by the database we suggest set the following changes in the url
```
...UTC&verifyServerCertificate=false&useSSL=true&requireSSL=true
```
## Run
The main file is /src/commnet/CN_Main.java. To run the tool it is expected four arguments. The first is the mode of the tool you want to run, the second is the file with GitHub project URL to be analyzed, the third is the file with tokens (see GitHub Token topic), and the fourth is a file with merge commit hashes case you want to analyze only them and not all the merge commits of the target project. 

The tool can run in several modes (look at the CN_Main class). For instance, building merge scenarios (**ms**), issues (**i**), contribution networks (**contnet**), communication networks (**comnet**), project metrics (**mepr**), merge scenario mertrics (**mems**), store developers (**storeDevs**), committers (**storeCommitter**), integrators (**storeIntegrators**). As the communication network is based on merge scenario and to get them we need the contribution network or at least the merge scenario mode ran before. If the communication mode is selected, but no contribution network or merge scenario is stored before it will just build issues and data related to issues. The _ms_ and _i_ modes will just set the merge scenarios and issues in the database, respectively.

In the URL file each line should have an URL that refers to a GitHub project and in the tokens file each line should contain a token. More details in the main file. The following example run the tool building both networks using the files systems_urls.txt and tokens.txt.

```
-ms resources/systems_urls.txt resources/tokens.txt resources/mergeCommitsList.txt
```

The tool is defined to start compute merge scenarios since the beginning of the project. The user can change this date changing variable _startingDate_ in MergeScenarioBuilder class.

## Additional Settings
Actually, we made two local changes in the dependent projects. In GitHubWrapper we changed the visibility of GitHubRepository.getGit() method to public. And, in GitWrapper we changed the visibility of Repository.getGit() method to public.

## Tooling
1. [**Gradle**](http://gradle.org): a build tool with a focus on build automation and support for multi-language development.
2. [**GitWrapper**](https://github.com/...): A wrapper for working with Git repositories using native git calls.
3. [**GitHubWrapper**](https://github.com/...): A wrapper for interaction with the GitHub issue and pull-request API.
4. [**Git**](https://git-scm.com/): a free and open source distributed version control system.
3. [**MySQL**](http://www.mysql.com): open source database.
