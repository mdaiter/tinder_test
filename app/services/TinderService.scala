package services

import java.util.concurrent.ConcurrentNavigableMap
import play.api.Logger
import play.api.Play.current
import scala.collection.mutable.Map
import scala.collection.JavaConversions._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import org.mapdb._
import utils.tinder.TinderApi
import utils.tinder.model._

/**
 * Manages state in-memory of sessions for Tinder.
 */
object TinderService {
  // if we don't set the ClassLoader it will be stuck in SBT
  Thread.currentThread().setContextClassLoader(play.api.Play.classloader)

  /**
   * Current active tokens.
   */
  private val sessions: ConcurrentNavigableMap[String, TinderAuth] = MapDB.db.getTreeMap("sessions")

  /**
   * Retrieve an active session.
   * @param xAuthToken
   * @return
   */
  def fetchSession(xAuthToken: String): Option[TinderAuth] = {
    sessions.get(xAuthToken) match {
      case null =>
        Logger.info("Creating new session for xAuthToken %s".format(xAuthToken))
        val tinderApi = new TinderApi(Some(xAuthToken))
        val result = Await.result(tinderApi.getProfile, 20 seconds)
        result match {
          case Left(error) =>
            Logger.error(error.toString)
            None
          case Right(profile) =>
            // create a placeholder auth object
            val tinderAuth = new TinderAuth(xAuthToken,new TinderGlobals(0,0,0,"",false,"",0,0,"",false,0),profile,new TinderVersion("","","","",""))
            // save it
            val f = future { storeSession(tinderAuth) }
            Some(tinderAuth)
        }
      case session => Some(session)
    }
  }

  /**
   * Save an active session asynchronously.
   * @param tinderAuth
   */
  def storeSession(tinderAuth: TinderAuth) { sessions.put(tinderAuth.token, tinderAuth) }

  /**
   * End an active session asynchronously.
   * @param xAuthToken
   */
  def deleteSession(xAuthToken: String) { sessions.remove(xAuthToken) }

  /**
   * Retrieve all active tokens
   */
  def activeSessions = {
    sessions.keySet()
  }

  /**
   * Retreieve all active user IDs
   */
  def activeUsers = {
    sessions.values().map(_.user).map(_._id)
  }
  
	/**
	 * Retrieve all active user sessions
	 */
	def activeSessionValues = {
		val seq : Seq[utils.tinder.model.TinderAuth] = activeSessions.filter(!_.isEmpty).map(id => fetchSession(id).get).seq.toList
		seq
	}
}
