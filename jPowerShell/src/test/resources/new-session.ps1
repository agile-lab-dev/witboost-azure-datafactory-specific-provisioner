$username = $args[0]
$password = $args[1]
$server = $args[2]
$configName = $args[3]
$auth = $args[4]

$securePassword = ConvertTo-SecureString $password -AsPlainText -Force
$credential = New-Object System.Management.Automation.PSCredential $username,$securePassword

$sessionObject = New-PSSession `
     -Credential $credential `
     -ConfigurationName $configName `
     -ConnectionUri $server `
     -Authentication $auth

if ($sessionObject) {

    Import-PSSession -Session $sessionObject `
     -CommandName 'Get-Mailbox','Get-MailboxStatistics' `
     -AllowClobber `
     -DisableNameChecking | out-null

	Write-Output ''

    $session = [ordered]@{ `
        "id" = $sessionObject.Id; `
        "name" = $sessionObject.Name; `
        "guid" = $sessionObject.InstanceId.ToString(); `
        "computerName" = $sessionObject.ComputerName; `
        "computerType" = $sessionObject.ComputerType.ToString(); `
        "configurationName" = $sessionObject.ConfigurationName; `
        "idleTimeout" = $sessionObject.IdleTimeout `
    }

    $out = ConvertTo-Json $session
    Write-Output $out

} else {

    Write-Output $false
}