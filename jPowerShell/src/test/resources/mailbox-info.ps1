$user = $args[0]

function Get-AsInteger {

    $Input = $args[0]

    if ($Input) {

        $r = $Input.trimend(" bytes)")
        $r = $r.Split("(")
        $rI = [int64]$r[1]

        return $rI
    }
    return 0
}

$mb = Get-Mailbox $user
#Write-Host ''

$mbStats = Get-MailboxStatistics $user
#Write-Host ''

$mailboxQuota = Get-AsInteger $mb.ProhibitSendQuota
$totalDeletedItemSize = Get-AsInteger $mbStats.TotalDeletedItemSize.Value.ToString()
$totalItemSize = Get-AsInteger $mbStats.TotalItemSize.Value.ToString()

$jsonHash = [ordered]@{
    "Username" = $mb.Name
    "DN" = $mb.DistinguishedName
    "DisplayName" = $mb.DisplayName
    "Quota" = [string]($mailboxQuota / 1024 / 1024 / 1024) + " GB"
    "UsedQuota" = [string]([math]::Round($totalItemSize / 1024 / 1024 / 1024, 4)) + " GB"
    "Database" = $mb.Database
    "ServerName" = $mb.ServerName
    #"TotalDeletedItemSize" = [string]([math]::Round($totalDeletedItemSize / 1024 / 1024 / 1024, 4)) + " GB"
}
$out = ConvertTo-Json $jsonHash

Write-Host $out