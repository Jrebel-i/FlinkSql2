package com.hc.state_process_module

import org.apache.flink.shaded.guava18.com.google.common.hash.Hashing
import org.apache.flink.util.StringUtils.byteToHexString

import java.nio.charset.Charset
import scala.collection.mutable

object JobVertexGenerator extends App{
  val hashFunction = Hashing.murmur3_128(0)
  val hashes = mutable.HashMap.empty[Int, Array[Byte]]

  // Plan
  // {"nodes":[{"id":1,"type":"Source: Custom Source","pact":"Data Source","contents":"Source: Custom Source","parallelism":4},{"id":2,"type":"Map","pact":"Operator","contents":"Map","parallelism":4,"predecessors":[{"id":1,"ship_strategy":"FORWARD","side":"second"}]},{"id":4,"type":"Map","pact":"Operator","contents":"Map","parallelism":4,"predecessors":[{"id":2,"ship_strategy":"HASH","side":"second"}]},{"id":5,"type":"Sink: Print to Std. Out","pact":"Data Sink","contents":"Sink: Print to Std. Out","parallelism":4,"predecessors":[{"id":4,"ship_strategy":"FORWARD","side":"second"}]}]}

  case class MicroStreamGraph(nodeId: Int,
                              userSpecifiedUID: String,
                              chainableOutEdgeCnt: Int,
                              inEdgeNodeIds: List[Int])

  // https://izualzhy.cn/flink-source-kafka-checkpoint-init#3-reading-state
  val streamNodes = List(
    MicroStreamGraph(1, null, 1, List.empty[Int]),
    MicroStreamGraph(2, null, 0, List(1)),
    MicroStreamGraph(4, null, 1, List(2)),
    MicroStreamGraph(5, null, 0, List(4))
  )
  /*
  // https://izualzhy.cn/flink-source-job-graph
  val streamNodes = List(
    MicroStreamGraph(1, null, 1, List.empty[Int]),
    MicroStreamGraph(2, null, 0, List(1)),
    MicroStreamGraph(4, null, 0, List(2)),
    MicroStreamGraph(5, null, 0, List(4))
  )
   */

  // StreamGraphHasherV2.traverseStreamGraphAndGenerateHashes
  streamNodes.map{
    case streamNode if streamNode.userSpecifiedUID == null =>
      // StreamGraphHasherV2.generateDeterministicHash
      val hasher = hashFunction.newHasher()

      hasher.putInt(hashes.size)
      (0 until streamNode.chainableOutEdgeCnt).foreach(_ => hasher.putInt(hashes.size))

      val hash = hasher.hash().asBytes()

      streamNode.inEdgeNodeIds.foreach{inEdgeNodeId =>
        val inEdgeNodeHash = hashes(inEdgeNodeId)

        println(s"inEdgeNodeHash:${byteToHexString(inEdgeNodeHash)}")
        (0 until hash.length).foreach(i =>
          hash(i) = (hash(i) * 37 ^ inEdgeNodeHash(i)).toByte)
      }
      println(s"hash:${byteToHexString(hash)}")
      hashes.update(streamNode.nodeId, hash)

    case streamNode if streamNode.userSpecifiedUID != null =>
      // StreamGraphHasherV2.generateUserSpecifiedHash
      // OperatorIDGenerator.fromUid
      val hasher = hashFunction.newHasher()
      hasher.putString(streamNode.userSpecifiedUID.toString, Charset.forName("UTF-8"))
      hashes.update(streamNode.nodeId, hasher.hash().asBytes())
  }

  streamNodes.foreach(streamNode => println(s"${streamNode.nodeId} ${byteToHexString(hashes(streamNode.nodeId))}"))
}
