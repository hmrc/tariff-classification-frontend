package controllers.v2

import com.google.inject.Inject
import config.AppConfig
import controllers.RequestActions
import models.viewmodels.MyCasesViewModel
import models.{NoPagination, Permission, Queue}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{CasesService, QueuesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global

class MyCasesController @Inject() (
                                    verify: RequestActions,
                                    casesService: CasesService,
                                    queuesService : QueuesService,
                                    mcc: MessagesControllerComponents,
                                    val commonCasesView: views.html.v2.common_cases_view,
                                    implicit val appConfig: AppConfig
                                  ) extends FrontendController(mcc)
  with I18nSupport {

  def displayMyCases: Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.VIEW_MY_CASES)).async { implicit request =>
    for {
      cases                         <- casesService.getCasesByAssignee(request.operator, NoPagination())
      myCases                        = MyCasesViewModel(cases.results)
//      queues: Seq[Queue]            <- queuesService.getAll
//      countQueues: Map[String, Int] <- casesService.countCasesByQueue(request.operator)
    } yield Ok(commonCasesView("title", myCases))
  }

}
