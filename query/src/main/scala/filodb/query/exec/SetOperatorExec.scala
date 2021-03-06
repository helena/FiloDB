package filodb.query.exec

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import monix.reactive.Observable

import filodb.core.metadata.Dataset
import filodb.core.query._
import filodb.memory.format.{ZeroCopyUTF8String => Utf8Str}
import filodb.memory.format.ZeroCopyUTF8String._
import filodb.query._
import filodb.query.BinaryOperator.{LAND, LOR, LUnless}

/**
  * Set operator between results of lhs and rhs plan.
  *
  * This ExecPlan accepts two sets of RangeVectors lhs and rhs from child plans.
  * It then does a join of the RangeVectors based on the fields of their keys as
  * dictated by `on` or `ignoring` fields passed as params.
  *
  * Joins can be Many-to-Many.
  *
  * @param lhs ExecPlan that will return results of LHS expression
  * @param rhs ExecPlan that will return results of RHS expression
  * @param binaryOp the binary operator
  * @param cardinality the cardinality of the join relationship as a hint
  * @param on fields from range vector keys to include while performing the join
  * @param ignoring fields from range vector keys to exclude while performing the join
  */
final case class SetOperatorExec(id: String,
                                dispatcher: PlanDispatcher,
                                lhs: Seq[ExecPlan],
                                rhs: Seq[ExecPlan],
                                binaryOp: BinaryOperator,
                                on: Seq[String],
                                ignoring: Seq[String]) extends NonLeafExecPlan {
  require(on == Nil || ignoring == Nil, "Cannot specify both 'on' and 'ignoring' clause")
  require(!on.contains("__name__"), "On cannot contain metric name")

  val onLabels = on.map(Utf8Str(_)).toSet
  val ignoringLabels = ignoring.map(Utf8Str(_)).toSet + "__name__".utf8
  // if onLabels is non-empty, we are doing matching based on on-label, otherwise we are
  // doing matching based on ignoringLabels even if it is empty
  val onMatching = onLabels.nonEmpty

  def children: Seq[ExecPlan] = lhs ++ rhs

  protected def schemaOfCompose(dataset: Dataset): ResultSchema = lhs(0).schema(dataset)

  protected def args: String = s"binaryOp=$binaryOp, on=$on, ignoring=$ignoring"

  protected[exec] def compose(dataset: Dataset,
                              childResponses: Observable[(QueryResponse, Int)],
                              queryConfig: QueryConfig): Observable[RangeVector] = {
    val taskOfResults = childResponses.map {
      case (QueryResult(_, _, result), i) => (result, i)
      case (QueryError(_, ex), _)         => throw ex
    }.toListL.map { resp =>
      require(resp.size == lhs.size + rhs.size, "Did not get sufficient responses for LHS and RHS")
      val lhsRvs = resp.filter(_._2 < lhs.size).flatMap(_._1)
      val rhsRvs = resp.filter(_._2 >= lhs.size).flatMap(_._1)

      val results: List[SerializableRangeVector] = binaryOp  match {
        case LAND => setOpAnd(lhsRvs, rhsRvs)
        case LOR => setOpOr(lhsRvs, rhsRvs)
        case LUnless => setOpUnless(lhsRvs, rhsRvs)
        case _ => throw new IllegalArgumentException("requirement failed: " + "Only and, or and unless are supported ")
      }

      Observable.fromIterable(results)
    }
    Observable.fromTask(taskOfResults).flatten
  }

  private def joinKeys(rvk: RangeVectorKey): Map[Utf8Str, Utf8Str] = {
    if (onLabels.nonEmpty) rvk.labelValues.filter(lv => onLabels.contains(lv._1))
    else rvk.labelValues.filterNot(lv => ignoringLabels.contains(lv._1))
  }

  private def setOpAnd(lhsRvs: List[SerializableRangeVector]
                       , rhsRvs: List[SerializableRangeVector]): List[SerializableRangeVector] = {
    val rhsKeysSet = new mutable.HashSet[Map[Utf8Str, Utf8Str]]()
    var result = new ListBuffer[SerializableRangeVector]()
    rhsRvs.foreach { rv =>
      val jk = joinKeys(rv.key)
      if (!jk.isEmpty)
        rhsKeysSet += jk
    }

    lhsRvs.foreach { lhs =>
      val jk = joinKeys(lhs.key)
      // Add range vectors from lhs which are present in lhs and rhs both
      // Result should also have range vectors for which rhs does not have any keys
      if (rhsKeysSet.contains(jk) || rhsKeysSet.isEmpty) {
        result += lhs
      }
    }
    result.toList
  }

  private def setOpOr(lhsRvs: List[SerializableRangeVector]
                      , rhsRvs: List[SerializableRangeVector]): List[SerializableRangeVector] = {
    val lhsKeysSet = new mutable.HashSet[Map[Utf8Str, Utf8Str]]()
    var result = new ListBuffer[SerializableRangeVector]()
    // Add everything from left hand side range vector
    lhsRvs.foreach { rv =>
      val jk = joinKeys(rv.key)
      lhsKeysSet += jk
      result += rv
    }
    // Add range vectors from right hand side which are not present on lhs
    rhsRvs.foreach { rhs =>
      val jk = joinKeys(rhs.key)
      if (!lhsKeysSet.contains(jk)) {
        result += rhs
      }
    }
    result.toList
  }

  private def setOpUnless(lhsRvs: List[SerializableRangeVector]
                          , rhsRvs: List[SerializableRangeVector]): List[SerializableRangeVector] = {
    val rhsKeysSet = new mutable.HashSet[Map[Utf8Str, Utf8Str]]()
    var result = new ListBuffer[SerializableRangeVector]()
    rhsRvs.foreach { rv =>
      val jk = joinKeys(rv.key)
      rhsKeysSet += jk
    }
    // Add range vectors which are not present in rhs
    lhsRvs.foreach { lhs =>
      val jk = joinKeys(lhs.key)
      if (!rhsKeysSet.contains(jk)) {
        result += lhs
      }
    }
    result.toList
  }
}
