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

    var hasSuccess = document.querySelector("#govuk-notification-banner--success");
    if(hasSuccess) {
        hasSuccess.focus();
    }

};

function keep() {
    let xhr = new XMLHttpRequest();
    xhr.open("GET", "/manage-tariff-classifications/keep-alive", true);
    xhr.send(null);
};
setInterval(keep, 780000);

