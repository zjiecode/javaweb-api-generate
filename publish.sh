echo '请输入bintrayKey：'
read bintrayKey
./gradlew clean build bintrayUpload -PbintrayUser=zjiecode -PbintrayKey=$bintrayKey -PdryRun=false