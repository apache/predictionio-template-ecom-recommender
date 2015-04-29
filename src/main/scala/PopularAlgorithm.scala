package org.template.ecommercerecommendation
/*
import io.prediction.controller.P2LAlgorithm
import io.prediction.controller.Params


case class PopularAlgorithmParams() extends Params

class PopularModel(
  val itemModel: Vector[(String, (Item, Int))] // Vector of (item ID, (Item, Count))
) extends Serializable {

}

class PopularAlgorithm(val ap: PopularAlgorithmParams)
  extends P2LAlgorithm[PreparedData, PopularModel, Query, PredictedResult] {


  def train(sc: SparkContext, data: PreparedData): PopularModel = {

    // calculate number of buys for each item
    val buyCounts: RDD[(String, Int)] = data.buyEvents
      .map { buy => (buy.item, 1) }
      .reduceByKey{ case (a, b) => a + b }

    // combine item data with the count
    val itemWithCount: RDD[(String, (Item, Int))] = data.items.join(buyCounts)

    // collect to local vector, and sort save as model
    val itemModel = itemWithCount.collect.toVector
      .sortBy{ case (id, (item, count)) => count }(Ordering.Int.revese)

    PopularModel(
      itemModel = itemModel
    )

  }


  def predict(model: PopularModel, query: Query): PredictedResult = {
    model.itemModel.filter {
      case (id, (item, count)) =>
        isCandidateItem(

        )
      )
    }
  }

  private
  def isCandidateItem(
    i: Int,
    item: Item,
    categories: Option[Set[String]],
    whiteList: Option[Set[Int]],
    blackList: Set[Int]
  ): Boolean = {
    // can add other custom filtering here
    whiteList.map(_.contains(i)).getOrElse(true) &&
    !blackList.contains(i) &&
    // filter categories
    categories.map { cat =>
      item.categories.map { itemCat =>
        // keep this item if has ovelap categories with the query
        !(itemCat.toSet.intersect(cat).isEmpty)
      }.getOrElse(false) // discard this item if it has no categories
    }.getOrElse(true)

  }

}
*/
