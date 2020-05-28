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
function getAnchor(targetURL, csrf) {
    let xhr = new XMLHttpRequest();
    xhr.open("GET", targetURL, true);
    xhr.setRequestHeader('Csrf-Token', csrf);

    xhr.onload = function () {
        if (xhr.readyState === xhr.DONE) {
            if (xhr.status === 200) {
                setLocation(xhr.responseText)
            }
        }
    };

    xhr.send(null);
}

function setLocation(anchor) {
    if (anchor && anchor.toString().startsWith("#")) {
        let ourElement = document.getElementById(anchor.toString().substr(1))
        if (ourElement) {
            window.location.hash = anchor;
        }
    }
}

function fixAnchorInURL(targetURL, csrf) {
    let anchor = window.location.hash
    if (anchor.toString() === "") {
        //no anchor in url
        getAnchor(targetURL, csrf)
    }
}
