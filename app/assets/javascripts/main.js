window.onload = function () {
    var Tabs = window.GOVUKFrontend.Tabs
        var $tabs = document.querySelector('[data-module="govuk-tabs"]')
        if ($tabs) {
            setTimeout(function() {
            new Tabs($tabs).init()
            }, 200);
        }
    if (document.querySelectorAll && document.addEventListener) {
        var els = document.querySelectorAll('a[role="button"]');
        for (var i = 0; i < els.length; i++) {
            els[i].setAttribute("draggable", "false");
            els[i].addEventListener('keydown', function (e) {
                if (e.keyCode === 32) {
                    e.preventDefault();
                    e.target.click();
                }
            });
        }
    }
    if(document.getElementById("error-summary-heading")!=null){
        document.title = "Error: " + document.title
    }

    var hasErrors = document.querySelector("#error-summary");
    if (hasErrors) {
        hasErrors.focus();
    }

    if(document.getElementById("govuk-notification-banner--success")) {
         document.getElementById("govuk-notification-banner--success").focus()
    }

};


