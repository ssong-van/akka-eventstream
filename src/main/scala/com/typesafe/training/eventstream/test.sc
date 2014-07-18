import events.Request

import scala.collection.mutable.ListBuffer

val test = List(("ie","other Infomation"),("ie",1),("ff",1),("ie",1))

val test2 = for(
  t <- test
) yield (t._1,t._2)

//val test3 = test2 map(item => (item._1, item._2.size))


val requests : ListBuffer[String] = ListBuffer()
"first" +=: requests
"second" +: requests
println(requests)
