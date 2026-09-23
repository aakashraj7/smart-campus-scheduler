$PSScriptRoot = Split-Path -Parent -Path $MyInvocation.MyCommand.Definition
$wrapperJar = Join-Path $PSScriptRoot ".mvn\wrapper\maven-wrapper.jar"
$jvmArg = "-Dmaven.multiModuleProjectDirectory=$PSScriptRoot"
& java $jvmArg -cp $wrapperJar org.apache.maven.wrapper.MavenWrapperMain $args
exit $LASTEXITCODE
