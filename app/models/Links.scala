package models

/**
 * Created by Anish on 4/28/2015.
 */

import play.api.data.Forms._
import play.api.data._
import reactivemongo.bson._

case class Links (links:List[String],
                  surveyid:BSONObjectID)

object Links {
  val fldLinks = "links"
  val fldSurveyId = "surveyid"

  implicit object LinksWriter extends BSONDocumentWriter[Links]{
    def write(links:Links):BSONDocument = BSONDocument(
      fldLinks -> links.links,
      fldSurveyId -> links.surveyid
    )
  }

  implicit object LinksReader extends BSONDocumentReader[Links]{
    def read(doc:BSONDocument):Links = Links(
      doc.getAs[List[String]](fldLinks).getOrElse(List()),
      doc.getAs[BSONObjectID](fldSurveyId).get
    )
  }

  val form = Form(
    mapping(
      fldLinks -> text
      //      fldLinks -> text
    )
    { (links) =>
      Links(
        links.split(" ").foldLeft(List[String]()){(c,e) => e.trim :: c},
        BSONObjectID.generate
      )
    }
    {
      links => Some(
        (links.links.mkString(" "))
      )
    }
  )
}
