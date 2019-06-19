window.onload = function () {
    if (document.querySelectorAll && document.addEventListener) {
        var els = document.querySelectorAll('a[role="button"]'),
            i, _i;
        for (i = 0, _i = els.length; i < _i; i++) {
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


