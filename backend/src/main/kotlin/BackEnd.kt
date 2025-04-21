//Intefaces

interface BackEnd {
  /*
   * Aqui estamos implementando a nossa interface
   * para pegar a lib. Por exemplo, recebemos numpy
   * e enviamos de volta um json com o namespace:
   * - linalg
   * - random
   *   .
   *   .
   *   .
   * */
  fun RecieveLib()
  fun SendLib()

  /*
   * Aqui estamos implementando a nossa interface
   * para pegar o namespace. Por exemplo, recebemos linalg
   * e enviamos de volta um json com as funções do namespace:
   * - eig
   * - inv
   *   .
   *   .
   *   .
   * */
  fun ReceiveNameSpace()
  fun SendNameSpace()
  
  /*
   * Aqui estamos implementando a nossa interface
   * para pegar a Função em específico. Por exemplo, recebemos
   * inv e enviamos de volta um json com a documentação da função,
   * paremetros, e exemplos.
   * */
  fun ReceiveFunction()
  fun SendFunction()
}
