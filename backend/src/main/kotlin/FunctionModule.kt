interface FunctionModule {  
  /*
   * Essa função vai receber o nome da function que queremos
   */
  fun ReceiveName()

  /*
   * Aqui, vamos pegar as informações de json dentro de um
   * database
   * */
  fun GetFunction()

  /*
   * Vamos enviar um json de volta com as informações adquiridas
   * */
  fun SendFunction()
}
