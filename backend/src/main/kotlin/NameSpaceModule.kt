interface NameSpaceModule {  
  /*
   * Essa função vai receber o nome da namespace que queremos
   */
  fun ReceiveName()

  /*
   * Aqui, vamos pegar as informações de json dentro de um
   * database
   * */
  fun GetNameSpace()

  /*
   * Vamos enviar um json de volta com as informações adquiridas
   * */
  fun SendNameSpace()
}
