def pomSetMunitConfig(propertyName,propertyValue) {
  sh """
    echo "${propertyName}\n"
    echo "${propertyValue}\n\n"
  """
}