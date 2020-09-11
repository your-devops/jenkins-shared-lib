def pomSetMunitConfig(propertyName,propertyValue) {
  sh """
    xmlstarlet edit --inplace \
      -N pom=http://maven.apache.org/POM/4.0.0 \
      --update "//pom:project/pom:build/pom:plugins/pom:plugin[pom:artifactId='munit-maven-plugin']/pom:configuration/pom:coverage/pom:${propertyName}" --value '${propertyValue}' \
      --subnode "//pom:project/pom:build/pom:plugins/pom:plugin[pom:artifactId='munit-maven-plugin']/pom:configuration/pom:coverage[not(pom:${propertyName})]" --type elem -n ${propertyName} --value "${propertyValue}" \
      pom.xml
  """
}