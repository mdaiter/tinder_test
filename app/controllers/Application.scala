package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits._
import com.codahale.jerkson.Json._
import services.TinderService
import utils.tinder.TinderApi
import utils.tinder.model._


object Application extends Controller {

  /**
   * Home page let's you select active Tinder sessions or create one.
   */
  def index = Action.async { implicit request =>
    val f = future { views.html.Application.index() }
    f.map { result => Ok(result) }
  }

	/**
	 * Pulls up the first active Tinder session in a dashboard
	 */

	def dashboardDefault = Action.async { implicit request =>
		val f = future { TinderService.fetchSession(TinderService.activeSessions.first()); }
		f.map { result =>
      result match {
        case Some(session) => Ok(views.html.Application.dashboard(session, TinderService.activeSessionValues))
        case None => BadRequest
			}
		}
	}

	def dashboardIndex(num: Int) = Action.async { implicit request =>
		val activeSessionsArr = TinderService.activeSessions.toArray();
		val indicie = if (num < activeSessionsArr.length) { num } else { activeSessionsArr.length - 1 }
		val f = future { TinderService.fetchSession(activeSessionsArr(indicie).asInstanceOf[String]); }
		f.map { result =>
      result match {
        case Some(session) => Ok(views.html.Application.dashboard(session, TinderService.activeSessionValues))
        case None => BadRequest
			}
		}
	}

  /**
   * Pulls up an active Tinder session in a dashboard.
   */
  def dashboard(xAuthToken: String) = Action.async { implicit request =>
    val f = future { TinderService.fetchSession(xAuthToken) }
    f.map { result =>
      result match {
        case Some(session) => Ok(views.html.Application.dashboard(session, TinderService.activeSessionValues))
        case None => BadRequest
      }
    }
  }

  /**
   * Update the user's current location.
   */
  def authenticate = Action.async(parse.json) { implicit request =>
    val facebook_token = (request.body \ "facebook_token").as[String]
    val facebook_id = (request.body \ "facebook_id").as[String]

    val f = new TinderApi().authorize(facebook_token, facebook_id)
    f.map { result =>
      result match {
        case Right(result) =>
          Ok(generate(result)).as("application/json")
        case Left(e) =>
          BadRequest
      }
    }
  }

  /**
   * Ends the current session
   */
  def logout(xAuthToken: String) = Action { implicit request =>
    val f = future { TinderService.deleteSession(xAuthToken) }
    Redirect(routes.Application.index())
  }
	
	def switchUser(xAuthToken: String) = Action.async { implicit request =>
	  val f = future { TinderService.fetchSession(xAuthToken) }
    f.map { result =>
      result match {
        case Some(session) => Ok(views.html.Application.switchUser(session, TinderService.activeSessionValues))
        case None => BadRequest
      }
    }

	}

  /**
   * Pulls up an active Tinder session in a dashboard.
   */
  def activeSessions = Action { implicit request =>
    Ok(generate(TinderService.activeSessions))
  }

}
