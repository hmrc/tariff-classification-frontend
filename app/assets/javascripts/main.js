window.onload = function () {
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
};


