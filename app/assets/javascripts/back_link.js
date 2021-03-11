// =====================================================
// Back link uses a js session storage based history stack
// =====================================================
// store referrer value to cater for IE
// - https://developer.microsoft.com/en-us/microsoft-edge/platform/issues/10474810/  */

var docReferrer = document.referrer

// prevent resubmit warning
if (window.history
    && window.history.replaceState
    && typeof window.history.replaceState === 'function') {
        window.history.replaceState(null, null, window.location.href);
}

// back click handle, dependent upon presence of referrer & no host change
$('#back-link').on('click', function (e) {
    e.preventDefault();
    if (window.history
        && window.history.back
        && typeof window.history.back === 'function'
        && (docReferrer !== "" && docReferrer.indexOf(window.location.host) !== -1)) {
            const historyStack = JSON.parse(sessionStorage.getItem("historyStack")) || [];
            if (historyStack.length > 1) {
                historyStack.pop();    //take the url for page just exited off the stack
                const previousPath = historyStack[historyStack.length - 1];
                window.location.href = window.location.origin + previousPath;
                sessionStorage.setItem("historyStack", JSON.stringify(historyStack));
            }
    }
})