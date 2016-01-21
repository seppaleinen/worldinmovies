window.onload = setupFunc;

function setupFunc() {
	document.getElementsByTagName('body')[0].onclick = clickFunc;
	hideIndicator();
	Wicket.Ajax.registerPreCallHandler(showIndicator);
	Wicket.Ajax.registerPostCallHandler(hideIndicator);
	Wicket.Ajax.registerFailureHandler(hideIndicator);
}

function hideIndicator() {
	document.getElementById('default_indicator').style.display = 'none';
}

function showIndicator() {
	document.getElementById('default_indicator').style.display = 'inline';
}
function clickFunc(eventData) {
	var clickedElement = (window.event) ? event.srcElement : eventData.target;
	var tagName = clickedElement.tagName.toUpperCase();
	if (tagName == 'BUTTON' || tagName == 'A' || clickedElement.parentNode.tagName.toUpperCase() == 'A' || (tagName == 'INPUT' && (clickedElement.type.toUpperCase() == 'BUTTON' || clickedElement.type.toUpperCase() == 'SUBMIT'))) {
		if (checkForCssClass(clickedElement, 'no_indicator')
				&& checkForCssClass(clickedElement, 'link-close') /* Datepicker */
				&& checkForCssClass(clickedElement, 'selector') /* Datepicker */
				&& checkForCssClass(clickedElement, 'calheader') /* Datepicker */
				&& (clickedElement.href.indexOf('help.html') < 0)
				&& (tagName != 'A' || clickedElement.href.indexOf('WicketAjaxDebug') < 0) /* WicketDebugWindow */) {

			showIndicator();
		}
	}
}

function checkForCssClass(clickedElement, cssClass) {
	return clickedElement.className.indexOf(cssClass) < 0 && clickedElement.parentNode.className.indexOf(cssClass) < 0;
}