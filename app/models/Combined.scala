package models

/**
 * Created by NabirDinani on 8/14/15.
 */

case class Combined (surveyid:String,
                     title:String,
                     createDate:java.util.Date,
                     sent:Int,
                     received:Int) {
  def incSent = Combined(surveyid,title,createDate,sent+1,received)
  def incReceived = Combined(surveyid,title,createDate,sent,received+1)
}



