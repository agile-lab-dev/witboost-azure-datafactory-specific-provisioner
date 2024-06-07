param (
    [Parameter(Mandatory=$true)][string]$RootFolder
)

$result = Test-AdfCode -RootFolder "$RootFolder"
if ($result.ErrorCount -gt 0) { Write-Host "VALIDATION_KO" }
else { Write-Host "VALIDATION_OK" }