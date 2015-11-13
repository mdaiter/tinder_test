package models.bot.tasks

import akka.actor._
import models.bot.BotCommand
import services.{TinderService, FacialAnalysisService}
import scala.concurrent._
import scala.concurrent.duration._
import play.api.Logger
import utils.tinder.TinderApi
import utils.tinder.model._

/**
 * Worker task that processes recommendations.
 */
class RecommendationsTask(val xAuthToken: String, val tinderBot: ActorRef) extends TaskActor {

  override def preStart() = {
    Logger.debug("[tinderbot] Starting new recommendations task.")
    self ! "tick"
  }

  def receive = {
    case "tick" =>
      val session = TinderService.fetchSession(xAuthToken).get

      // ensure that models are valid to help bot accuracy
      if(FacialAnalysisService.modelsAreValid(session.user._id)) {
        // fetch a list of recommendations
        val tinderApi = new TinderApi(Some(xAuthToken))
        val recs = Await.result(tinderApi.getRecommendations(40), 20 seconds)
        recs match {
          case Left(e) =>
            Logger.error("[tinderbot] Recommendation task had an error on Tinder: " + e.error)

          case Right(r) =>
            // create a new worker task for each recommendation
            try {
              r.foreach { rec =>
                Logger.debug("[tinderbot] Creating new swipe task for user %s" format rec._id)
                val task = Props(new SwipeTask(xAuthToken, tinderBot, rec))
                tinderBot ! task
              }
            } catch {
              case e: NullPointerException =>
                Logger.info("[tinderbot] No new recommendations are available, sleeping.")
                tinderBot ! BotCommand("sleep")
            }
        }

      } else {
        Logger.warn("[tinderbot] Waiting to start automatic Swipes. User needs to make more choices to ensure accuracy.")
      }

      // make sure we properly shut down this actor
      self ! PoisonPill

    // someone is sending invalid messages
    case e: Any =>
      Logger.error("[tinderbot] Task received an unknown message")
      Logger.error("[tinderbot] Received: \n %s" format e.toString)

  }

}
