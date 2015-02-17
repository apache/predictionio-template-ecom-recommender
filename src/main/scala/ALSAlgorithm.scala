package org.template.ecommercerecommendation

import io.prediction.controller.P2LAlgorithm
import io.prediction.controller.Params
import io.prediction.data.storage.BiMap

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.{Rating => MLlibRating}

import grizzled.slf4j.Logger

import scala.collection.mutable.PriorityQueue

import scala.collection.immutable.HashMap

case class ALSAlgorithmParams(
  rank: Int,
  numIterations: Int,
  lambda: Double,
  seed: Option[Long]) extends Params

class ALSModel(
  val rank: Int,
  val userFeatures: Map[Int, Array[Double]],
  val productFeatures: Map[Int, Array[Double]],
  val userStringIntMap: BiMap[String, Int],
  val itemStringIntMap: BiMap[String, Int],
  val items: Map[Int, Item]
) extends Serializable {

  @transient lazy val itemIntStringMap = itemStringIntMap.inverse

  override def toString = {
    s" rank: ${rank}" +
    s" userFeatures: [${userFeatures.size}]" +
    s"(${userFeatures.take(2).toList}...)" +
    s" productFeatures: [${productFeatures.size}]" +
    s"(${productFeatures.take(2).toList}...)" +
    s" userStringIntMap: [${userStringIntMap.size}]" +
    s"(${userStringIntMap.take(2).toString}...)]" +
    s" itemStringIntMap: [${itemStringIntMap.size}]" +
    s"(${itemStringIntMap.take(2).toString}...)]" +
    s" items: [${items.size}]" +
    s"(${items.take(2).toString}...)]"
  }
}

/**
  * Use ALS to build item x feature matrix
  */
class ALSAlgorithm(val ap: ALSAlgorithmParams)
  extends P2LAlgorithm[PreparedData, ALSModel, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]

  def train(data: PreparedData): ALSModel = {
    require(!data.viewEvents.take(1).isEmpty,
      s"viewEvents in PreparedData cannot be empty." +
      " Please check if DataSource generates TrainingData" +
      " and Preprator generates PreparedData correctly.")
    require(!data.users.take(1).isEmpty,
      s"users in PreparedData cannot be empty." +
      " Please check if DataSource generates TrainingData" +
      " and Preprator generates PreparedData correctly.")
    require(!data.items.take(1).isEmpty,
      s"items in PreparedData cannot be empty." +
      " Please check if DataSource generates TrainingData" +
      " and Preprator generates PreparedData correctly.")
    // create User and item's String ID to integer index BiMap
    val userStringIntMap = BiMap.stringInt(data.users.keys)
    val itemStringIntMap = BiMap.stringInt(data.items.keys)

    // collect Item as Map and convert ID to Int index
    val items: Map[Int, Item] = data.items.map { case (id, item) =>
      (itemStringIntMap(id), item)
    }.collectAsMap.toMap

    val mllibRatings = data.viewEvents
      .map { r =>
        // Convert user and item String IDs to Int index for MLlib
        val uindex = userStringIntMap.getOrElse(r.user, -1)
        val iindex = itemStringIntMap.getOrElse(r.item, -1)

        if (uindex == -1)
          logger.info(s"Couldn't convert nonexistent user ID ${r.user}"
            + " to Int index.")

        if (iindex == -1)
          logger.info(s"Couldn't convert nonexistent item ID ${r.item}"
            + " to Int index.")

        ((uindex, iindex), 1)
      }.filter { case ((u, i), v) =>
        // keep events with valid user and item index
        (u != -1) && (i != -1)
      }.reduceByKey(_ + _) // aggregate all view events of same user-item pair
      .map { case ((u, i), v) =>
        // MLlibRating requires integer index for user and item
        MLlibRating(u, i, v)
      }.cache()

    // MLLib ALS cannot handle empty training data.
    require(!mllibRatings.take(1).isEmpty,
      s"mllibRatings cannot be empty." +
      " Please check if your events contain valid user and item ID.")

    // seed for MLlib ALS
    val seed = ap.seed.getOrElse(System.nanoTime)

    val m = ALS.trainImplicit(
      ratings = mllibRatings,
      rank = ap.rank,
      iterations = ap.numIterations,
      lambda = ap.lambda,
      blocks = -1,
      alpha = 1.0,
      seed = seed)

    new ALSModel(
      rank = m.rank,
      userFeatures = m.userFeatures.collectAsMap.toMap,
      productFeatures = m.productFeatures.collectAsMap.toMap,
      userStringIntMap = userStringIntMap,
      itemStringIntMap = itemStringIntMap,
      items = items
    )
  }

  def predict(model: ALSModel, query: Query): PredictedResult = {

    val userFeatures = model.userFeatures
    val productFeatures = model.productFeatures
    val items = model.items

    val whiteList: Option[Set[Int]] = query.whiteList.map( set =>
      set.map(model.itemStringIntMap.get(_)).flatten
    )
    val blackList: Option[Set[Int]] = query.blackList.map ( set =>
      set.map(model.itemStringIntMap.get(_)).flatten
    )

    val indexScores: Map[Int, Double] =
      model.userStringIntMap.get(query.user).map { userIndex =>
        userFeatures.get(userIndex)
      }
      // flatten Option[Option[Array[Double]]] to Option[Array[Double]]
      .flatten
      .map { uf =>
        productFeatures.par // convert to parallel collection
          .filter { case (i, v) =>
            isCandidateItem(
              i = i,
              items = items,
              categories = query.categories,
              whiteList = whiteList,
              blackList = blackList
            )
          }
          .map { case (i, f) =>
            (i, dotProduct(uf, f))
          }
          .seq // convert back to sequential collection

      }.getOrElse{
        logger.info(s"No userFeature found for user ${query.user}.")
        Map[Int, Double]()
      }

    val ord = Ordering.by[(Int, Double), Double](_._2).reverse
    val topScores = getTopN(indexScores, query.num)(ord).toArray

    val itemScores = topScores.map { case (i, s) =>
      new ItemScore(
        item = model.itemIntStringMap(i),
        score = s
      )
    }

    new PredictedResult(itemScores)
  }

  private
  def getTopN[T](s: Iterable[T], n: Int)(implicit ord: Ordering[T]): Seq[T] = {

    val q = PriorityQueue()

    for (x <- s) {
      if (q.size < n)
        q.enqueue(x)
      else {
        // q is full
        if (ord.compare(x, q.head) < 0) {
          q.dequeue()
          q.enqueue(x)
        }
      }
    }

    q.dequeueAll.toSeq.reverse
  }

  private
  def dotProduct(v1: Array[Double], v2: Array[Double]): Double = {
    val size = v1.size
    var i = 0
    var d: Double = 0
    while (i < size) {
      d += v1(i) * v2(i)
      i += 1
    }
    d
  }

  private
  def isCandidateItem(
    i: Int,
    items: Map[Int, Item],
    categories: Option[Set[String]],
    whiteList: Option[Set[Int]],
    blackList: Option[Set[Int]]
  ): Boolean = {
    whiteList.map(_.contains(i)).getOrElse(true) &&
    blackList.map(!_.contains(i)).getOrElse(true) &&
    // filter categories
    categories.map { cat =>
      items(i).categories.map { itemCat =>
        // keep this item if has ovelap categories with the query
        !(itemCat.toSet.intersect(cat).isEmpty)
      }.getOrElse(false) // discard this item if it has no categories
    }.getOrElse(true)
  }

}
