# Helper script to run all tests with Java 17
if (-not (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    if (Test-Path "C:\Program Files\Java\jdk-17") {
        $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
    } elseif (Test-Path "C:\Program Files\Java\jdk-21") {
        $env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
    }
}

Write-Host "Using Java 17 LTS: $env:JAVA_HOME" -ForegroundColor Cyan
cmd /c "SET JAVA_HOME=$env:JAVA_HOME&& .\mvnw.cmd clean test"
