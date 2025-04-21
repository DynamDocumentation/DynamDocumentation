interface LibModule {
  /*
   * Essa função vai receber o nome da lib que queremos
   */
  fun ReceiveName()

  /*
   * Aqui, vamos pegar as informações de json dentro de um
   * database
   * */
  fun GetLib()

  /*
   * Vamos enviar um json de volta com as informações adquiridas
   * */
  fun SendLib()
}
