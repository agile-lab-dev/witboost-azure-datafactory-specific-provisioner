FROM eclipse-temurin:17.0.11_9-jre-jammy

RUN curl -o opentelemetry-javaagent.jar -L https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.29.0/opentelemetry-javaagent.jar

RUN apt-get update && \
    apt-get install -y wget apt-transport-https software-properties-common && \
    wget -q https://packages.microsoft.com/config/ubuntu/22.04/packages-microsoft-prod.deb && \
    dpkg -i packages-microsoft-prod.deb && \
    rm packages-microsoft-prod.deb && \
    apt-get update && \
    apt-get install -y powershell && \
    rm -rf /var/lib/apt/lists/*

RUN pwsh -Command Set-PSRepository PSGallery -InstallationPolicy Trusted && \
    pwsh -Command Install-Module -Name Az.Resources -RequiredVersion 6.16.2 && \
    pwsh -Command Install-Module -Name Az.DataFactory -RequiredVersion 1.18.3 && \
    pwsh -Command Install-Module -Name azure.datafactory.tools -RequiredVersion 1.9.0

COPY run_app.sh .
RUN chmod +x run_app.sh

COPY datafactory/target/*.jar .

ENTRYPOINT ["bash", "run_app.sh"]
