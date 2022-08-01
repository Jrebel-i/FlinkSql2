package com.hc.row_module

import org.apache.calcite.sql.parser.SqlParser

object LiteralInRowCalcite extends App{
  val sql = "SELECT Row(f0,f1,f2,f4, f5,f6,'literal_column') FROM source_table"
//  val sql = "SELECT Row('literal_column', f0) FROM source_table"
  val sqlParser = SqlParser.create(sql)

  val sqlNode = sqlParser.parseQuery()
}
