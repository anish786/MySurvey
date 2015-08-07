package controllers

/**
 * Created by Anish on 4/28/2015.
 */

import controllers.Survey._
import play.api.data.{Forms, Form}
import play.api.data.Forms._
import play.api.libs.mailer.{MailerPlugin, Email}
import play.api.mvc._
import play.modules.reactivemongo._
import play.twirl.api.Html
import reactivemongo.api.collections.default._
import reactivemongo.bson._
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Response extends Controller with MongoController{
  lazy val ResponseCollection = db("responses")
  trait Response

  def partialIndex(form:Form[models.Response]) = {
    val found = ResponseCollection.find(BSONDocument(
      "$query" -> BSONDocument()
    )).cursor[models.Response]
    found.collect[List]().map{
      f => Ok
    }.recover {
      case e =>
        e.printStackTrace
        BadRequest(e.getMessage)
    }
  }

  def addResponse(responseid:String) = Action.async{ implicit request =>
    val form = Form("answers" -> list(text))
    val receivedResponses = form.bindFromRequest().get
    println("im in")
    println(receivedResponses)
    ResponseCollection.find(BSONDocument("_id" -> BSONObjectID(responseid))).one[models.Response].map{
      optSurvey => {
        optSurvey match {
          case Some(response) =>
            //ResponseCollection.update("answers": receivedResponses)
          case None => BadRequest
        }
      }
    }
    Future.successful(Ok)
  }
//  def addResponse(responseid: String) = Action.async { implicit request =>
//    val form = Form(single("answers" -> list(text)))
//    val answers = form.bindFromRequest().get
//    println(answers)
//    models.Response.form.bindFromRequest.fold(
//      errors => {
//        Future.successful(Redirect(routes.Application.index))},
//      response => {
//        //ResponseCollection.insert(response)
//      }
//    )
//    Future.successful(Ok)
//  }
//
  def index = Action.async { implicit request =>

    val authorhtmlfut = ResponseCollection.find(BSONDocument()).cursor[models.Response].collect[List]().map{
      list =>
        views.html.survey(list)
    }

    val futauthpage = for{
      user <- Application.getLoggedInUser(request)
      authorpage <- authorhtmlfut
    //page <- Application.generatePage(request,authorpage,false)
    } yield {
        user match {
          case Application.LoggedInUser(userid,username) => views.html.aggregator(authorpage)(views.html.confirmation())
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
//



  def getResponse(responseid: String) = Action.async { implicit request =>
    val authorhtmlfut = ResponseCollection.find(BSONDocument("_id" -> BSONObjectID(responseid))).cursor[models.Response].collect[List]().map{
      list =>
        views.html.usersurvey(list)
    }

    for{
      authorpage <- authorhtmlfut
      page <- Application.generatePage(request,authorpage)
    } yield page

  }
}