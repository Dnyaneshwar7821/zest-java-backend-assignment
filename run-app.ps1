# Helper script to run the Spring Boot application locally with JAVA_HOME configured
if (-not $env:JAVA_HOME) {
    if (Test-Path "C:\Program Files\Java\jdk-17") {
        $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
    } elseif (Test-Path "C:\Program Files\Java\jdk-21") {
        $env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
    }
}

Write-Host "Using JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Cyan
Write-Host "Starting Spring Boot Application..." -ForegroundColor Green
.\mvnw.cmd spring-boot:run
