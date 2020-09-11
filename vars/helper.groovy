def pomSetMunitConfig(propertyName,propertyValue) {
  node {
    sh """
      echo "${propertyName}\n"
      echo "${propertyValue}\n\n"
    """
  }
}