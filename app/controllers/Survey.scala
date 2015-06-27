package controllers

/**
 * Created by Anish on 4/28/2015.
 */


import controllers.Application._
import play.api.data.Form
import play.modules.reactivemongo._
import play.api.mvc._
import play.twirl.api.Html
import reactivemongo.api.collections.default._
import reactivemongo.bson._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models._

object Survey extends Controller with MongoController{
  lazy val SurveyCollection = db("surveys")
  trait Survey

  def partialIndex(form:Form[models.Survey]) = {
    val found = LoginCollection.find(BSONDocument(
      "$query" -> BSONDocument()
    )).cursor[models.Survey]
    found.collect[List]().map{
      f => Ok
    }.recover {
      case e =>
        e.printStackTrace
        BadRequest(e.getMessage)
    }
  }

  def add = Action.async { implicit request =>
    println("I am in")
    models.Survey.form.bindFromRequest.fold(
      errors => {
        println("errors")
        Future.successful(Redirect(routes.Application.index))},
      survey => {
        println("storing into database")
        SurveyCollection.insert(survey).zip(partialIndex(models.Survey.form.fill(survey))).map(_._2)
      }
    )
  }

  def newSurvey = Action.async { implicit request =>
    Application.generatePage(request, views.html.newsurvey())
  }

  def index = Action.async { implicit request =>

    val authorhtmlfut = SurveyCollection.find(BSONDocument()).cursor[models.Survey].collect[List]().map{
      list =>
        views.html.survey(list)
    }

    val futauthpage = for{
      user <- Application.getLoggedInUser(request)
      authorpage <- authorhtmlfut
    //page <- Application.generatePage(request,authorpage,false)
    } yield {
        user match {
          case Application.LoggedInUser(userid,username) => views.html.aggregator(authorpage)(views.html.newsurvey())
          case _ => authorpage
        }
      }

    for {
      authorpage <- futauthpage
      page <- Application.generatePage(request,authorpage,false)
    } yield {
      page
    }
    //Ok(views.html.author(List()))
  }




  def get(authorid: String) = Action.async { implicit request =>
    val authorhtmlfut = SurveyCollection.find(BSONDocument("_id" -> BSONObjectID(authorid))).cursor[models.Survey].collect[List]().map{
      list =>
        views.html.survey(list)
    }

    for{
      authorpage <- authorhtmlfut
      page <- Application.generatePage(request,authorpage)
    } yield page

  }
}
