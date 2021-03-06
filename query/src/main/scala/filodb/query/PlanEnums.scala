package filodb.query

import enumeratum.{Enum, EnumEntry}

//scalastyle:off
sealed abstract class InstantFunctionId(override val entryName: String) extends EnumEntry

object InstantFunctionId extends Enum[InstantFunctionId] {
  val values = findValues

  case object Abs extends InstantFunctionId("abs")

  case object Absent extends InstantFunctionId("absent")

  case object Ceil extends InstantFunctionId("ceil")

  case object ClampMax extends InstantFunctionId("clamp_max")

  case object ClampMin extends InstantFunctionId("clamp_min")

  case object Exp extends InstantFunctionId("exp")

  case object Floor extends InstantFunctionId("floor")

  case object HistogramQuantile extends InstantFunctionId("histogram_quantile")

  case object HistogramMaxQuantile extends InstantFunctionId("histogram_max_quantile")

  case object HistogramBucket extends InstantFunctionId("histogram_bucket")

  case object Ln extends InstantFunctionId("ln")

  case object Log10 extends InstantFunctionId("log10")

  case object Log2 extends InstantFunctionId("log2")

  case object Round extends InstantFunctionId("round")

  case object Sqrt extends InstantFunctionId("sqrt")

  // TODO time, vector, scalar
}

sealed abstract class RangeFunctionId(override val entryName: String) extends EnumEntry

object RangeFunctionId extends Enum[RangeFunctionId] {
  val values = findValues

  case object AvgOverTime extends RangeFunctionId("avg_over_time")

  case object Changes extends RangeFunctionId("changes")

  case object CountOverTime extends RangeFunctionId("count_over_time")

  case object Delta extends RangeFunctionId("delta")

  case object Deriv extends RangeFunctionId("deriv")

  case object HoltWinters extends RangeFunctionId("holt_winters")

  case object Idelta extends RangeFunctionId("idelta")

  case object Increase extends RangeFunctionId("increase")

  case object Irate extends RangeFunctionId("irate")

  case object MaxOverTime extends RangeFunctionId("max_over_time")

  case object MinOverTime extends RangeFunctionId("min_over_time")

  case object PredictLinear extends RangeFunctionId("predict_linear")

  case object QuantileOverTime extends RangeFunctionId("quantile_over_time")

  case object Rate extends RangeFunctionId("rate")

  case object Resets extends RangeFunctionId("resets")

  case object StdDevOverTime extends RangeFunctionId("stddev_over_time")

  case object StdVarOverTime extends RangeFunctionId("stdvar_over_time")

  case object SumOverTime extends RangeFunctionId("sum_over_time")

}

sealed abstract class FiloFunctionId(override val entryName: String) extends EnumEntry

object FiloFunctionId extends Enum[FiloFunctionId] {
  val values = findValues

  case object ChunkMetaAll extends FiloFunctionId("_filodb_chunkmeta_all")
}

sealed abstract class AggregationOperator(override val entryName: String) extends EnumEntry

object AggregationOperator extends Enum[AggregationOperator] {
  val values = findValues

  case object Avg extends AggregationOperator("avg")

  case object Count extends AggregationOperator("count")

  case object Sum extends AggregationOperator("sum")

  case object Min extends AggregationOperator("min")

  case object Max extends AggregationOperator("max")

  case object Stddev extends AggregationOperator("stddev")

  case object Stdvar extends AggregationOperator("stdvar")

  case object TopK extends AggregationOperator("topk")

  case object BottomK extends AggregationOperator("bottomk")

  case object CountValues extends AggregationOperator("count_values")

  case object Quantile extends AggregationOperator("quantile")

}

sealed abstract class BinaryOperator extends EnumEntry

sealed class MathOperator extends BinaryOperator

sealed class SetOperator extends BinaryOperator

sealed class ComparisonOperator extends BinaryOperator

object BinaryOperator extends Enum[BinaryOperator] {
  val values = findValues

  case object SUB extends MathOperator

  case object ADD extends MathOperator

  case object MUL extends MathOperator

  case object MOD extends MathOperator

  case object DIV extends MathOperator

  case object POW extends MathOperator

  case object LAND extends SetOperator

  case object LOR extends SetOperator

  case object LUnless extends SetOperator

  case object EQL extends ComparisonOperator

  case object NEQ extends ComparisonOperator

  case object LTE extends ComparisonOperator

  case object LSS extends ComparisonOperator

  case object GTE extends ComparisonOperator

  case object GTR extends ComparisonOperator

  case object EQL_BOOL extends ComparisonOperator

  case object NEQ_BOOL extends ComparisonOperator

  case object LTE_BOOL extends ComparisonOperator

  case object LSS_BOOL extends ComparisonOperator

  case object GTE_BOOL extends ComparisonOperator

  case object GTR_BOOL extends ComparisonOperator

  case object EQLRegex extends BinaryOperator // FIXME when implemented

  case object NEQRegex extends BinaryOperator // FIXME when implemented


}

sealed trait Cardinality extends EnumEntry

object Cardinality extends Enum[Cardinality] {
  val values = findValues

  case object OneToOne extends Cardinality

  case object OneToMany extends Cardinality

  case object ManyToOne extends Cardinality

  case object ManyToMany extends Cardinality

}

sealed abstract class MiscellaneousFunctionId(override val entryName: String) extends EnumEntry

object MiscellaneousFunctionId extends Enum[MiscellaneousFunctionId] {
  val values = findValues

  case object DaysInMonth extends MiscellaneousFunctionId("days_in_month")

  case object DaysOfMonth extends MiscellaneousFunctionId("day_of_month")

  case object DayOfWeek extends MiscellaneousFunctionId("day_of_week")

  case object Hour extends MiscellaneousFunctionId("hour")

  case object LabelReplace extends MiscellaneousFunctionId("label_replace")

  case object LabelJoin extends MiscellaneousFunctionId("label_join")

  case object Minute extends MiscellaneousFunctionId("minute")

  case object Month extends MiscellaneousFunctionId("month")

  case object Sort extends MiscellaneousFunctionId("sort")

  case object SortDesc extends MiscellaneousFunctionId("sort_desc")

  case object Timestamp extends MiscellaneousFunctionId("timestamp")

  case object Year extends MiscellaneousFunctionId("year")

}


//scalastyle:on

