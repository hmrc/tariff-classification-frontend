/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package metrics

import com.codahale.metrics.{MetricRegistry, Timer}
import play.api.mvc.{Action, MessagesBaseController, Result}

import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait HasActionMetrics extends HasMetrics { self: MessagesBaseController =>

  /** Execute a [[play.api.mvc.Action]] with a metrics timer. Intended for use in controllers that return HTTP
    * responses.
    *
    * @param metric
    *   The id of the metric to be collected
    * @param action
    *   The action to wrap with metrics collection
    * @param ec
    *   The [[scala.concurrent.ExecutionContext]] on which the block of code should run
    * @return
    *   an action which captures metrics about the wrapped action
    */
  def withMetricsTimerAction[A](metric: Metric)(action: Action[A])(implicit ec: ExecutionContext): Action[A] =
    Action(action.parser).async(request => withMetricsTimerResult(metric)(action(request)))
}

trait HasMetrics {
  type Metric = String

  def metrics: MetricRegistry

  private lazy val registry: MetricRegistry = metrics

  val localMetrics = new LocalMetrics

  class LocalMetrics {
    def startTimer(metric: Metric): Timer.Context     = registry.timer(s"$metric-timer").time()
    def stopTimer(context: Timer.Context): Long       = context.stop()
    def incrementSuccessCounter(metric: Metric): Unit = registry.counter(s"$metric-success-counter").inc()
    def incrementFailedCounter(metric: Metric): Unit  = registry.counter(s"$metric-failed-counter").inc()
  }

  class MetricsTimer(metric: Metric) {
    val timer: Timer.Context = localMetrics.startTimer(metric)
    private val timerRunning = new AtomicBoolean(true)

    def completeWithSuccess(): Unit =
      if (timerRunning.compareAndSet(true, false)) {
        localMetrics.stopTimer(timer)
        localMetrics.incrementSuccessCounter(metric)
      }

    def completeWithFailure(): Unit =
      if (timerRunning.compareAndSet(true, false)) {
        localMetrics.stopTimer(timer)
        localMetrics.incrementFailedCounter(metric)
      }
  }

  /** Execute a block of code with a metrics timer. Intended for use in controllers that return HTTP responses.
    *
    * @param metric
    *   The id of the metric to be collected
    * @param block
    *   The block of code to execute asynchronously
    * @param ec
    *   The [[scala.concurrent.ExecutionContext]] on which the block of code should run
    * @return
    *   The result of the block of code
    */
  def withMetricsTimerResult(metric: Metric)(block: => Future[Result])(implicit ec: ExecutionContext): Future[Result] =
    withMetricsTimer(metric) { timer =>
      try {
        val result = block

        // Clean up timer according to server response
        result.foreach { result =>
          if (isFailureStatus(result.header.status)) {
            timer.completeWithFailure()
          } else {
            timer.completeWithSuccess()
          }
        }

        // Clean up timer for unhandled exceptions
        result.failed.foreach(_ => timer.completeWithFailure())

        result

      } catch {
        case NonFatal(e) =>
          timer.completeWithFailure()
          throw e
      }
    }

  /** Execute a block of code with a metrics timer.
    *
    * Intended for use in connectors that call other microservices.
    *
    * It's expected that the user of this method might want to handle connector failures gracefully and therefore they
    * are given a [[MetricsTimer]] to optionally record whether the call was a success or a failure.
    *
    * If the user does not specify otherwise the status of the result Future is used to determine whether the block was
    * successful or not.
    *
    * @param metric
    *   The id of the metric to be collected
    * @param block
    *   The block of code to execute asynchronously
    * @param ec
    *   The [[scala.concurrent.ExecutionContext]] on which the block of code should run
    * @return
    *   The result of the block of code
    */
  def withMetricsTimerAsync[T](
    metric: Metric
  )(block: MetricsTimer => Future[T])(implicit ec: ExecutionContext): Future[T] =
    withMetricsTimer(metric) { timer =>
      try {
        val result = block(timer)

        // Clean up timer if the user doesn't
        result.foreach(_ => timer.completeWithSuccess())

        // Clean up timer on unhandled exceptions
        result.failed.foreach(_ => timer.completeWithFailure())

        result

      } catch {
        case NonFatal(e) =>
          timer.completeWithFailure()
          throw e
      }
    }

  private def withMetricsTimer[T](metric: Metric)(block: MetricsTimer => T): T =
    block(new MetricsTimer(metric))

  private def isFailureStatus(status: Int) =
    status / 100 >= 4
}
