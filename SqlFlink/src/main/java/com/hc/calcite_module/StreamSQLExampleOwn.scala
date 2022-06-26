package com.hc.calcite_module

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala._
import org.apache.flink.types.Row

object StreamSQLExampleOwn {
  case class Order(id: Long, uid: Long, product: String, amount: Int)

  case class User(id: Long, name: String, age: Int)

  def main(args: Array[String]): Unit = {
    val settings = EnvironmentSettings.newInstance()
      .useBlinkPlanner()
      .inStreamingMode()
      .build()

    // set up execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val tEnv = StreamTableEnvironment.create(env, settings)

    val orders: DataStream[Order] = env.fromCollection(Seq(
      Order(20200819177L, 1L, "beer", 3),
      Order(20200819234L, 2L, "diaper", 4),
      Order(20200819239L, 2L, "beef", 6),
      Order(20200820066L, 3L, "rubber", 2),
      Order(20200820100L, 3L, "beer", 5)))

    val users: DataStream[User] = env.fromCollection(Seq(
      User(1L, "Alice", 27),
      User(2L, "Bob", 26),
      User(3L, "Charlie", 25)))

    // register DataStream as Table
    val tableA = tEnv.createTemporaryView("orders", orders, 'id, 'uid, 'product, 'amount)
    val tableB = tEnv.createTemporaryView("users", users, 'id, 'name, 'age)

    // join the two tables
    val sql =
      s"""
         |SELECT u.name,sum(o.amount) AS total
         |FROM orders o
         |INNER JOIN users u ON o.uid = u.id
         |WHERE u.age < 27
         |GROUP BY u.name
        """.stripMargin

    print(tEnv.explainSql(sql))

    val result = tEnv.sqlQuery(sql)
    result.toRetractStream[Row].print()

    env.execute()
  }


}