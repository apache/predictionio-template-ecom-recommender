package org.example.ecommercerecommendation

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class PreparatorTest
  extends FlatSpec with EngineTestSparkContext with Matchers {

  val preparator = new Preparator()
  val users = Map(
    "u0" -> User(),
    "u1" -> User()
  )

  val items = Map(
    "i0" -> Item(categories = Some(List("c0", "c1"))),
    "i1" -> Item(categories = None)
  )

  val view = Seq(
    ViewEvent("u0", "i0", 1000010),
    ViewEvent("u0", "i1", 1000020),
    ViewEvent("u1", "i1", 1000030)
  )

  val buy = Seq(
    BuyEvent("u0", "i0", 1000020),
    BuyEvent("u0", "i1", 1000030),
    BuyEvent("u1", "i1", 1000040)
  )

  // simple test for demonstration purpose
  "Preparator" should "prepare PreparedData" in {

    val trainingData = new TrainingData(
      users = sc.parallelize(users.toSeq),
      items = sc.parallelize(items.toSeq),
      viewEvents = sc.parallelize(view.toSeq),
      buyEvents = sc.parallelize(buy.toSeq)
    )

    val preparedData = preparator.prepare(sc, trainingData)

    preparedData.users.collect should contain theSameElementsAs users
    preparedData.items.collect should contain theSameElementsAs items
    preparedData.viewEvents.collect should contain theSameElementsAs view
    preparedData.buyEvents.collect should contain theSameElementsAs buy
  }
}
