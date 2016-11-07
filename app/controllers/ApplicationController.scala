package controllers

import models.{User, UserForm}
import play.api.libs.iteratee.Iteratee
import play.api.libs.streams.Streams
import play.api.mvc._
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * ApplicationController
 *
 * @author Elliot Wright <elliot@elliotwright.co>
 */
class ApplicationController extends Controller {
  def index = Action.async { implicit request =>
    val userEnumerator = Streams.publisherToEnumerator(UserService.streamAllUsers)
    val userIteratee = Iteratee.getChunks[User]

    userEnumerator.run(userIteratee).map(users => {
      Ok(views.html.index(UserForm.form, users))
    })
  }

  def addUser() = Action.async { implicit request =>
    UserForm.form.bindFromRequest.fold(
      // On error
      errorForm => Future.successful(Ok(views.html.index(errorForm, Seq.empty[User]))),
      // On success
      data => {
        val newUser = User(0, data.firstName, data.lastName, data.mobile, data.email)
        UserService.addUser(newUser).map(res => {
          Redirect(routes.ApplicationController.index())
        })
      })
  }

  def deleteUser(id: Long) = Action.async { implicit request =>
    UserService.deleteUser(id).map(res => {
      Redirect(routes.ApplicationController.index())
    })
  }
}
