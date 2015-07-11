package controllers

/**
 * Created by Anish on 4/28/2015.
 */

import play.api.data.Form
import play.api.mvc._
import play.modules.reactivemongo._
import reactivemongo.api.collections.default._
import reactivemongo.bson._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Links extends Controller with MongoController{
  lazy val LinksCollection = db("links")
  trait Links

  def partialIndex(form:Form[models.Links]) = {
    val found = LinksCollection.find(BSONDocument(
      "$query" -> BSONDocument()
    )).cursor[models.Links]
    found.collect[List]().map{
      f => Ok
    }.recover {
      case e =>
        e.printStackTrace
        BadRequest(e.getMessage)
    }
  }

  def add = Action.async { implicit request =>
    models.Links.form.bindFromRequest.fold(
      errors => {
        Future.successful(Redirect(routes.Application.index))},
      links => {
        Application.generatePage(request,views.html.loginform())
        LinksCollection.insert(links).zip(partialIndex(models.Links.form.fill(links))).map(_._2)
      }
    )
  }
  def submit = Action.async { implicit request =>

    Application.getLoggedInUser(request).map{
      user =>
        user match {
          case Application.LoggedInUser(userid,username) => {
            models.Links.form.bindFromRequest.fold(
              hasErrors => Redirect(routes.Application.index()),
              post => {
                val postWithAuthorInfo = models.Links(
                  post.links,
                  BSONObjectID(userid)
                )
                LinksCollection.save(postWithAuthorInfo)
                Redirect(routes.Application.index())
              }
            )
          }
          case _ => Redirect(routes.Application.index())
        }
    }
  }
}
