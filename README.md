<p align="center">
    <a href="https://www.agilelab.it/witboost">
        <img src="docs/img/witboost_logo.svg" alt="witboost" width=600 >
    </a>
</p>

Designed by [Agile Lab](https://www.agilelab.it/), Witboost is a versatile platform that addresses a wide range of sophisticated data engineering challenges. It enables businesses to discover, enhance, and productize their data, fostering the creation of automated data platforms that adhere to the highest standards of data governance. Want to know more about Witboost? Check it out [here](https://www.agilelab.it/witboost) or [contact us!](https://www.agilelab.it/contacts).

This repository is part of our [Starter Kit](https://github.com/agile-lab-dev/witboost-starter-kit) meant to showcase Witboost's integration capabilities and provide a "batteries-included" product.

# Azure Data Factory Specific Provisioner

- [Overview](#overview)
- [Building](#building)
- [Running](#running)
- [Configuring](#configuring)
- [OpenTelemetry Setup](docs/opentelemetry.md)
- [Deploying](#deploying)
- [HLD](docs/HLD.md)
- [API specification](docs/API.md)


## Overview

This project implements a Specific Provisioner that provision Azure Data Factories using its Git-based collaboration workflow.

### What's a Specific Provisioner?

A Specific Provisioner is a microservice which is in charge of deploying components that use a specific technology. When the deployment of a Data Product is triggered, the platform generates it descriptor and orchestrates the deployment of every component contained in the Data Product. For every such component the platform knows which Specific Provisioner is responsible for its deployment, and can thus send a provisioning request with the descriptor to it so that the Specific Provisioner can perform whatever operation is required to fulfill this request and report back the outcome to the platform.

You can learn more about how the Specific Provisioners fit in the broader picture [here](https://docs.witboost.agilelab.it/docs/p2_arch/p1_intro/#deploy-flow).

### Azure Data Factory

Azure Data Factory is Azure's cloud ETL service for scale-out serverless data integration and data transformation. It offers a code-free UI for intuitive authoring and single-pane-of-glass monitoring and management.

### Software stack

This microservice is written in Java 17, using SpringBoot for the HTTP layer. Project is built with Apache Maven and supports packaging and Docker image, ideal for Kubernetes deployments (which is the preferred option).


### Git hooks

Hooks are programs you can place in a hooks directory to trigger actions at certain points in git’s execution. Hooks that don’t have the executable bit set are ignored.

The hooks are all stored in the hooks subdirectory of the Git directory. In most projects, that’s `.git/hooks`.

Out of the many available hooks supported by Git, we use `pre-commit` hook in order to check the code changes before each commit. If the hook returns a non-zero exit status, the commit is aborted.


#### Setup Pre-commit hooks

In order to use `pre-commit` hook, you can use [**pre-commit**](https://pre-commit.com/) framework to set up and manage multi-language pre-commit hooks.

To set up pre-commit hooks, follow the below steps:

- Install pre-commit framework either using pip (or) using homebrew (if your Operating System is macOS):

    - Using pip:
      ```bash
      pip install pre-commit
      ```
    - Using homebrew:
      ```bash
      brew install pre-commit
      ```

- Once pre-commit is installed, you can execute the following:

```bash
pre-commit --version
```

If you see something like `pre-commit 3.3.3`, your installation is ready to use!


- To use pre-commit, create a file named `.pre-commit-config.yaml` inside the project directory. This file tells pre-commit which hooks needed to be installed based on your inputs. Below is an example configuration:

```bash
repos:
  - repo: https://github.com/pre-commit/pre-commit-hooks
    rev: v4.4.0
    hooks:
      - id: trailing-whitespace
```

The above configuration says to download the `pre-commit-hooks` project and run its trailing-whitespace hook on the project.


- Run the below command to install pre-commit into your git hooks. pre-commit will then run on every commit.

```bash
pre-commit install
```

## Building

**Requirements:**

- Java 17
- Apache Maven 3.9+

**Version:** the version is set dynamically via an environment variable, `PROVISIONER_VERSION`. Make sure you have it exported, even for local development. Example:

```bash
export PROVISIONER_VERSION=0.0.0-SNAPHSOT
```

**Build:**

The project uses the `openapi-generator` Maven plugin to generate the API endpoints from the interface specification located in `src/main/resources/interface-specification.yml`. For more information on the documentation, check [API docs](docs/API.md).

```bash
mvn compile
```

**Type check:** is handled by Checkstyle:

```bash
mvn checkstyle:check
```

**Bug checks:** are handled by SpotBugs:

```bash
mvn spotbugs:check
```

**Tests:** are handled by JUnit:

```bash
mvn test
```

**Artifacts & Docker image:** the project leverages Maven for packaging. Build artifacts (normal and fat jar) with:

```bash
mvn package spring-boot:repackage
```

The Docker image can be built with:

```bash
docker build .
```

More details can be found [here](docs/docker.md).

*Note:* when running in the CI/CD pipeline the version for the project is automatically computed using information gathered from Git, using branch name and tags. Unless you are on a release branch `1.2.x` or a tag `v1.2.3` it will end up being `0.0.0`. You can follow this branch/tag convention or update the version computation to match your preferred strategy. When running locally if you do not care about the version (ie, nothing gets published or similar) you can manually set the environment variable `PROVISIONER_VERSION` to avoid warnings and oddly-named artifacts; as an example you can set it to the build time like this:
```bash
export PROVISIONER_VERSION=$(date +%Y%m%d-%H%M%S);
```

**CI/CD:** the pipeline is based on GitLab CI as that's what we use internally. It's configured by the `.gitlab-ci.yaml` file in the root of the repository. You can use that as a starting point for your customizations.

## Running

To run the server locally, use:

```bash
mvn -pl datafactory spring-boot:run
```

By default, the server binds to port `8888` on localhost. After it's up and running you can make provisioning requests to this address. You can access the running application [here](http://127.0.0.1:8888).

SwaggerUI is configured and hosted on the path `/docs`. You can access it [here](http://127.0.0.1:8888/docs)

## Configuring

Application configuration is handled using the features provided by Spring Boot. You can find the default settings in the `application.yml`. Customize it and use the `spring.config.location` system property or the other options provided by the framework according to your needs.

### Authentication

By default, Microsoft Entra ID token authentication depends on correct configuration of the following environment variables.

- `AZURE_CLIENT_ID` for Azure client ID.
- `AZURE_TENANT_ID` for Azure tenant ID.
- `AZURE_CLIENT_SECRET` or `AZURE_CLIENT_CERTIFICATE_PATH` for client secret or client certificate.
- `AZURE_SUBSCRIPTION_ID` for Azure subscription ID.

The configured Service Principal needs the following role assignments:
- `Data Factory Contributor`: to create and manage data factories, as well as child resources within them
- `Role Based Access Control Administrator`: to manage access to Azure resources by assigning roles using Azure RBAC

To link a GIT repository with a Data Factory instance, a `Custom Role` with permission `Microsoft.DataFactory/locations/configureFactoryRepo/action` needs to be assigned to the configured service principal.

### Principal Mapping

A service principal is required to authenticate against Microsoft Graph API. The following permissions are required for the service principal:
- `User.Read.All`
- `GroupMember.Read.All`

| Configuration      | Description   | 
|:-------------------|:--------------|
| graph.tenantId     | Tenant ID     |
| graph.clientId     | Client ID     | 
| graph.clientSecret | Client Secret | 

### Permissions

For the users to be able to test connection in a linked service or preview data in a dataset, a `Custom Role` is required with permissions for the following actions:
- `Microsoft.DataFactory/factories/getFeatureValue/read`
- `Microsoft.DataFactory/factories/getDataPlaneAccess/action`

| Configuration                     | Description                                          | 
|:----------------------------------|:-----------------------------------------------------|
| permission.customRoleDefinitionId | The definition ID of the Custom Role described above |

### PowerShell wrapper

| Configuration         | Description                                                | 
|:----------------------|:-----------------------------------------------------------|
| powershell.waitPause  | The pause in ms between each loop pooling for a response   |
| powershell.maxWait    | The maximum wait in ms for the command to execute          | 
| powershell.tempFolder | Temp folder used to store temporary the scripts to execute |

## Deploying

This microservice is meant to be deployed to a Kubernetes cluster with the included Helm chart and the scripts that can be found in the `helm` subdirectory. You can find more details [here](helm/README.md).

## License

This project is available under the [Apache License, Version 2.0](https://opensource.org/licenses/Apache-2.0); see [LICENSE](LICENSE) for full details.

## About us

<p align="center">
    <a href="https://www.agilelab.it">
        <img src="docs/img/agilelab_logo.svg" alt="Agile Lab" width=600>
    </a>
</p>

Agile Lab creates value for its Clients in data-intensive environments through customizable solutions to establish performance driven processes, sustainable architectures, and automated platforms driven by data governance best practices.

Since 2014 we have implemented 100+ successful Elite Data Engineering initiatives and used that experience to create Witboost: a technology-agnostic, modular platform, that empowers modern enterprises to discover, elevate and productize their data both in traditional environments and on fully compliant Data mesh architectures.

[Contact us](https://www.agilelab.it/contacts) or follow us on:
- [LinkedIn](https://www.linkedin.com/company/agile-lab/)
- [Instagram](https://www.instagram.com/agilelab_official/)
- [YouTube](https://www.youtube.com/channel/UCTWdhr7_4JmZIpZFhMdLzAA)
- [Twitter](https://twitter.com/agile__lab)
