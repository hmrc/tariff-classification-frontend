// post
function postCurrentAnchor(anchor, url, csrf) {
    let xhr = new XMLHttpRequest();
    xhr.open("POST", url, true);
    xhr.setRequestHeader('Csrf-Token', csrf);

    xhr.send('#' + anchor);
}

function saveAnchor(button, url, csrf) {
    button.addEventListener('click', function () {
        let baseURL = window.location.href.toString();
        let parts = baseURL.split("#");
        if (parts.length === 2) {
            let anchor = parts[1].toString();
            postCurrentAnchor(anchor, url, csrf);
        }
    }, false);
}

// get
function getAnchor(baseURL, targetURL, callback, csrf) {
    let xhr = new XMLHttpRequest();
    xhr.open("GET", targetURL, true);
    xhr.setRequestHeader('Csrf-Token', csrf);

    xhr.onload = function () {
        if (xhr.readyState === xhr.DONE) {
            if (xhr.status === 200) {
                callback(xhr.responseText)
            }
        }
    };

    xhr.send(null);
}

function setLocation(anchor) {
    if (anchor && anchor.toString().startsWith("#")) {
        let ourElement = document.getElementById(anchor)
        if (ourElement) {
            window.location.hash = anchor;
        }
    }
}

function fixAnchorInURL(targetURL, csrf) {
    let url = window.location.href.toString()
    let parts = url.split("#")
    if (parts.length === 1) {
        //no anchor in url
        getAnchor(url, targetURL, setLocation, csrf)
    }
}
