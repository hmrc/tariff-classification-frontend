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
