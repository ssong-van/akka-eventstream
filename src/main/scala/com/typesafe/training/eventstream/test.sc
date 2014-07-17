val test = List(("ie","other Infomation"),("ie",1),("ff",1),("ie",1))

val test2 = (for(
  t <- test
) yield t._1).groupBy(item => item)

//val test3 = test2 map(item => (item._1, item._2.size))