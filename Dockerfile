FROM eclipse-temurin:17.0.11_9-jre-jammy

RUN apt-get update && \
    apt-get install -y wget apt-transport-https software-properties-common && \
    wget -q https://packages.microsoft.com/config/ubuntu/22.04/packages-microsoft-prod.deb && \
    dpkg -i packages-microsoft-prod.deb && \
    rm packages-microsoft-prod.deb && \
    apt-get update && \
    apt-get install -y powershell && \
    rm -rf /var/lib/apt/lists/*

RUN groupadd -g 2000 javagroup
RUN useradd -m -g 2000 -u 1001 javauser

USER 1001

RUN pwsh -Command Set-PSRepository PSGallery -InstallationPolicy Trusted && \
    pwsh -Command Install-Module -Name Az.Resources -RequiredVersion 6.16.2 && \
    pwsh -Command Install-Module -Name Az.DataFactory -RequiredVersion 1.18.3 && \
    pwsh -Command Install-Module -Name azure.datafactory.tools -RequiredVersion 1.9.0

WORKDIR /app

RUN curl -o opentelemetry-javaagent.jar -L https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.29.0/opentelemetry-javaagent.jar

COPY run_app.sh .

USER root
RUN chmod +x run_app.sh
USER 1001

COPY datafactory/target/*.jar .

ENTRYPOINT ["bash", "run_app.sh"]
