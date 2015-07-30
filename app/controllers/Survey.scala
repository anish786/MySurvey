package controllers

/**
 * Created by Anish on 4/28/2015.
 */

import controllers.Application._
import play.api.data.Form
import play.api.libs.mailer.{MailerPlugin, Email}
import play.api.mvc._
import play.modules.reactivemongo._
import play.twirl.api.Html
import reactivemongo.api.collections.default._
import reactivemongo.bson._
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Survey extends Controller with MongoController{
  lazy val SurveyCollection = db("surveys")
  lazy val ResponseCollection = db("responses")
  trait Survey

  def partialIndex(form:Form[models.Survey]) = {
    val found = SurveyCollection.find(BSONDocument(
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
    models.Survey.form.bindFromRequest.fold(
      errors => {
        Future.successful(Redirect(routes.Application.index))},
      survey => {
        //generate a new bson object id
        //val q = BSONObjectID("_id" -> models.Survey.fldId)
        //create a new survey object from the contents of the old one but with new survey id
        Application.generatePage(request,views.html.loginform())
        SurveyCollection.insert(survey).zip(partialIndex(models.Survey.form.fill(survey))).map(_._2)
        //do links.insert with the new survey object id
      }
    )
  }

  def sendSurvey(surveyid:String,emails:String) = Action.async{ implicit request =>
    val lstEmails = emails.split(",")
    println(lstEmails)
    SurveyCollection.find(BSONDocument("_id" -> BSONObjectID(surveyid))).one[models.Survey].map{
      optSurvey => {
        optSurvey match {
          case Some(survey) =>
            val answer = lstEmails.foldLeft(""){
              (result,email) =>
                val responseId = BSONObjectID.generate
                val response = survey.createResponse(responseId)
                ResponseCollection.insert(response).map{
                 a =>
                  val email_bucket = Email(
                    "Simple email",
                    "Mister FROM <mysurveydev@gmail.com>",
                    Seq(email),
                    //      attachments = Seq(
                    //        AttachmentFile("favicon.png", new File(current.classloader.getResource("public/images/favicon.png").getPath)),
                    //        AttachmentData("data.txt", "data".getBytes, "text/plain", Some("Simple data"), Some(EmailAttachment.INLINE))
                    //      ),
                    bodyText = Some("A text message"),
                    bodyHtml = Some("<html><body><p>localhost:9000/" +responseId.stringify+ "</p></body></html>")
                  )
                  val id = MailerPlugin.send(email_bucket)
                }
                result.concat(email).concat(responseId.stringify)

            }
            play.api.Logger.debug(answer)
            Ok(Html(answer))
          case None => BadRequest
        }
      }
    }

    Future.successful(Ok)
  }

  def newSurvey = Action.async { implicit request =>

    Application.generatePage(request, views.html.newsurvey())
  }

//  def index = Action.async { implicit request =>
//
//    val authorhtmlfut = SurveyCollection.find(BSONDocument()).cursor[models.Survey].collect[List]().map{
//      list =>
//        views.html.survey(list)
//    }
//
//    val futauthpage = for{
//      user <- Application.getLoggedInUser(request)
//      authorpage <- authorhtmlfut
//    //page <- Application.generatePage(request,authorpage,false)
//    } yield {
//        user match {
//          case Application.LoggedInUser(userid,username) => views.html.aggregator(authorpage)(views.html.confirmation())
//          case _ => authorpage
//        }
//      }
//
//    for {
//      authorpage <- futauthpage
//      page <- Application.generatePage(request,authorpage,false)
//    } yield {
//      page
//    }
//    //Ok(views.html.author(List()))
//  }




//  def get(authorid: String) = Action.async { implicit request =>
//    val authorhtmlfut = SurveyCollection.find(BSONDocument("_id" -> BSONObjectID(authorid))).cursor[models.Survey].collect[List]().map{
//      list =>
//        views.html.usersurvey(list)
//    }
//
//    for{
//      authorpage <- authorhtmlfut
//      page <- Application.generatePage(request,authorpage)
//    } yield page
//
//  }
}
