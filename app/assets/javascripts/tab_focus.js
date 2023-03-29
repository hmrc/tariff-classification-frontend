// post
function postCurrentAnchor(anchor, url, csrf) {
    let xhr = new XMLHttpRequest();
    xhr.open("POST", url, true);
    xhr.setRequestHeader('Csrf-Token', csrf);
    xhr.setRequestHeader('Content-Type', 'text/plain;charset=UTF-8')
    xhr.send(anchor);
}

function saveAnchor(button, url, csrf) {
    button.addEventListener('click', function () {
        const hash = window.location.hash;
        const anchor = hash.length > 0 ? hash.substring(1) : hash;
        if (anchor.length > 0) {
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
    if (anchor) {
        let ourElement = document.getElementById(anchor.toString())
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
    } else if (anchor.includes("&")) {
        //anchor in url with query params, move it to the end
        var finalURL = window.location.href
        let anchorParts = anchor.split("&")
        let anchorName = anchorParts[0]
        let url = finalURL.replace(anchorName, "")
        window.location = url + anchorName
    }
}
