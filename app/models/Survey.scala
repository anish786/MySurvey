package models

/**
 * Created by Anish on 4/28/2015.
 */

import models.BSONProducers._
import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

case class Survey (id:Option[BSONObjectID],
                    createDate:Option[java.util.Date],
                    //sentDate:Option[java.util.Date],
                    title:String,
                    questions:List[String],
                    emails:List[String]){
  def createResponse(responseId: BSONObjectID){
    Response(responseId,id.get,None,title,questions,List(),false)
  }
}
                    //links:List[String])

object Survey {
  val fldId = "_id"
  val fldCreateDate = "createdate"
 // val fldSentDate = "sentdate"
  val fldTitle = "title"
  val fldQuestions = "questions"
  val fldEmails = "emails"
  //val fldLinks = "links"

  implicit object SurveyWriter extends BSONDocumentWriter[Survey]{
    def write(survey:Survey):BSONDocument = BSONDocument(
      fldId -> survey.id.getOrElse(BSONObjectID.generate),
      fldCreateDate -> survey.createDate.getOrElse(new java.util.Date()),
      //fldSentDate -> survey.sentDate.getOrElse(new java.util.Date()),
      fldTitle -> survey.title,
      fldQuestions -> survey.questions,
      fldEmails -> survey.emails
//      fldLinks -> survey.links
    )
  }

  implicit object SurveyReader extends BSONDocumentReader[Survey]{
    def read(doc:BSONDocument):Survey = Survey(
      doc.getAs[BSONObjectID](fldId),
      doc.getAs[java.util.Date](fldCreateDate),
      //doc.getAs[java.util.Date](fldSentDate),
      doc.getAs[String](fldTitle).get,
      doc.getAs[List[String]](fldQuestions).getOrElse(List()),
      doc.getAs[List[String]](fldEmails).getOrElse(List())
//      doc.getAs[List[String]](fldLinks).getOrElse(List())
    )
  }

  val form = Form(
    mapping(
      fldId -> optional(of[String] verifying pattern (
        Common.objectIdRegEx,
        "constraint.objectId",
        "error.objectId"
      )),
      fldCreateDate -> optional(of[java.util.Date]),
      //fldSentDate -> optional(of[java.util.Date]),
      fldTitle -> nonEmptyText,
      fldQuestions -> text,
      fldEmails -> text
    )
    { (id,createDate,title,questions,emails) =>
      Survey(
        id.map(BSONObjectID(_)),
        createDate,
        //sentDate,
        title,
        questions.split(",").foldLeft(List[String]()){(c,e) => e.trim :: c},
        emails.split(" ").foldLeft(List[String]()){(c,e) => e.trim :: c}
      )
    }
    {
      survey => Some(
        (survey.id.map(_.stringify),survey.createDate,survey.title,survey.questions.mkString(","),survey.emails.mkString(" "))
      )
    }
  )
}
