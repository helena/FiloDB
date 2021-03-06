package filodb.query

import enumeratum.{Enum, EnumEntry}

import filodb.core.{DatasetRef, ErrorResponse, NodeCommand, NodeResponse}
import filodb.core.query.{ResultSchema, SerializableRangeVector}

trait QueryCommand extends NodeCommand with java.io.Serializable {
  def submitTime: Long
  def dataset: DatasetRef
}

trait QueryResponse extends NodeResponse with java.io.Serializable {
  def id: String
}

final case class QueryError(id: String, t: Throwable) extends QueryResponse with ErrorResponse {
  override def toString: String = s"QueryError id=$id ${t.getClass.getName} ${t.getMessage}\n" +
    t.getStackTrace.map(_.toString).mkString("\n")
}

/**
  * Use this exception to raise user errors when inside the context of an observable.
  * Currently no other way to raise user errors when returning an observable
  */
class BadQueryException(message: String) extends RuntimeException(message)

sealed trait QueryResultType extends EnumEntry
object QueryResultType extends Enum[QueryResultType] {
  val values = findValues
  case object RangeVectors extends QueryResultType
  case object InstantVector extends QueryResultType
  case object Scalar extends QueryResultType
}

final case class QueryResult(id: String,
                             resultSchema: ResultSchema,
                             result: Seq[SerializableRangeVector]) extends QueryResponse {
  def resultType: QueryResultType = {
    result match {
      case Seq(one) => if (one.numRows == 1) QueryResultType.Scalar else QueryResultType.RangeVectors
      case many: Seq[SerializableRangeVector] => if (many.forall(_.numRows == 1)) QueryResultType.InstantVector
                                                 else QueryResultType.RangeVectors
    }
  }
}


