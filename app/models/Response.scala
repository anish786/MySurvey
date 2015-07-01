package models

/**
 * Created by Anish on 4/28/2015.
 */

import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import reactivemongo.bson.{BSONDateTime, BSONObjectID, BSONDocumentWriter, BSONDocumentReader, BSONDocument}
import models.BSONProducers._

case class Response (id:Option[BSONObjectID],
                     finishDate:Option[java.util.Date],
                     title:String,
                     questions:List[String],
                     answers:List[String],
                     sent:Boolean)
object Response{
  val fldId = "_id"
  val fldFinishDate = "finishdate"
  val fldTitle = "title"
  val fldQuestions = "questions"
  val fldAnswers = "answers"
  val fldSent = "sent"

  implicit object ResponseWriter extends BSONDocumentWriter[Response]{
    def write(response:Response):BSONDocument = BSONDocument(
      fldId -> response.id.getOrElse(BSONObjectID.generate),
      fldFinishDate -> response.finishDate.getOrElse(new java.util.Date()),
      fldTitle -> response.title,
      fldQuestions -> response.questions,
      fldAnswers -> response.answers,
      fldSent -> response.sent
    )
  }

  implicit object ResponseReader extends BSONDocumentReader[Response]{
    def read(doc:BSONDocument):Response = Response(
      doc.getAs[BSONObjectID](fldId),
      doc.getAs[java.util.Date](fldFinishDate),
      //doc.getAs[java.util.Date](fldSentDate),
      doc.getAs[String](fldTitle).get,
      doc.getAs[List[String]](fldQuestions).getOrElse(List()),
      doc.getAs[List[String]](fldAnswers).getOrElse(List()),
      doc.getAs[Boolean](fldSent).get
    )
  }
  val form = Form(
    mapping(
      fldId -> optional(of[String] verifying pattern (
        Common.objectIdRegEx,
        "constraint.objectId",
        "error.objectId"
      )),
      fldFinishDate -> optional(of[java.util.Date]),
      fldTitle -> nonEmptyText,
      fldQuestions -> nonEmptyText,
      fldAnswers -> nonEmptyText,
      fldSent -> boolean
    )
    { (id,finishDate,title,questions,answers,sent) =>
      Response(
        id.map(BSONObjectID(_)),
        finishDate,
        title,
        questions.split(",").foldLeft(List[String]()){(c,e) => e.trim :: c},
        answers.split(",").foldLeft(List[String]()){(c,e) => e.trim :: c},
        sent
      )
    }
    {
      response => Some(
        (response.id.map(_.stringify),response.finishDate,response.title,response.questions.mkString(","),response.answers.mkString(","),response.sent)
      )
    }
  )
}
