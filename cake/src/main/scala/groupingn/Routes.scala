package groupingn

import models._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val groupFormat           = jsonFormat1(Group)
  implicit val groupedFormat         = jsonFormat1(Grouped)
  implicit val identifiedGroupFormat = jsonFormat2(IdentifiedGroup)
  implicit val candidatesFormat      = jsonFormat2(Candidates)
}

object Routes extends Directives with JsonSupport {

  def groupingRoutes = //(implicit system: ActorSystem[_]) =
    pathPrefix("groupings") {
      pathEnd {
        post {
          entity(as[Candidates]) { candidates =>
            onSuccess(GroupingUseCase.grouping(candidates)) { identified =>
              identified
                .map(complete(Created, _))
                .left
                .map(e => complete(BadRequest, e.toString()))
                .merge
            }
          }
        }
      } ~
        path(Segment) { id =>
          get {
            rejectEmptyResponse {
              onSuccess(GroupingUseCase.identifiedGroup(id)) { identified =>
                identified
                  .map(_.map(complete(_)).getOrElse(complete(NotFound)))
                  .left
                  .map(e => complete(BadRequest, e.toString()))
                  .merge
              }
            }
          }
        }
    }

}
