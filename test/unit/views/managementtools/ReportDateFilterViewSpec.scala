package views.managementtools

import models._
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.pseudoCaseStatus
import views.html.managementtools.reportDateFilter
import models.forms.ReportDateFormData
import models.forms.ReportDateForm

class ReportDateFilterViewSpec extends ViewSpec {
  "reportDateFilter view" should {
    val form = ReportDateForm.form

    "render min and max date fields with the correct IDs" in {
      val doc = view(reportDateFilter(form))
      doc should containElementWithID("dateRange_min")
      doc should containElementWithID("dateRange_min_year")
      doc should containElementWithID("dateRange_min_month")
      doc should containElementWithID("dateRange_min_day")

      doc should containElementWithID("dateRange_max")
      doc should containElementWithID("dateRange_max_year")
      doc should containElementWithID("dateRange_max_month")
      doc should containElementWithID("dateRange_max_day")
    }

    "show label" in {
      val doc = view(reportDateFilter(form))
      doc.getElementById("dateRange_min") should containText(messages("reporting.choose_date.start_date"))
      doc.getElementById("dateRange_max") should containText(messages("reporting.choose_date.end_date"))
    }

    "show hint text" in {
      val doc = view(reportDateFilter(form))
      doc.getElementById("dateRange_min") should containText(messages("reporting.choose_date.example_date"))
      doc.getElementById("dateRange_max") should containText(messages("reporting.choose_date.example_date"))
    }
  }
}
