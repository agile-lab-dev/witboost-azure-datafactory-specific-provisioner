param (
    [Parameter(Mandatory=$true)][string]$RootFolder,
    [Parameter(Mandatory=$true)][string]$ResourceGroupName,
    [Parameter(Mandatory=$true)][string]$DataFactoryName,
    [Parameter(Mandatory=$true)][string]$Location,
    [string]$Stage = ""
)

try {
    $SecurePassword = ConvertTo-SecureString -String $env:AZURE_CLIENT_SECRET -AsPlainText -Force
    $Credential = New-Object -TypeName System.Management.Automation.PSCredential -ArgumentList $env:AZURE_CLIENT_ID, $SecurePassword
    Connect-AzAccount -ServicePrincipal -Tenant $env:AZURE_TENANT_ID -Credential $Credential -Scope Process -ErrorAction Stop
    if ([string]::IsNullOrEmpty($Stage) -eq $true)
    {
        Publish-AdfV2FromJson -RootFolder "$RootFolder" -ResourceGroupName "$ResourceGroupName" -DataFactoryName "$DataFactoryName" -Location "$Location" -ErrorAction Stop
    }
    else
    {
        Publish-AdfV2FromJson -RootFolder "$RootFolder" -ResourceGroupName "$ResourceGroupName" -DataFactoryName "$DataFactoryName" -Location "$Location" -Stage "$Stage" -ErrorAction Stop
    }
    Write-Host "PUBLISH_OK"
}
catch {
    Write-Host "PUBLISH_KO"
}