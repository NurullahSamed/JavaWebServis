
function onPageDetailsReceived(pageDetails)  { 
    document.getElementById('summary').innerText = pageDetails.summary; 
	document.getElementById('tumtext').innerText = pageDetails.tumtext;
} 



var statusDisplay = null;


function addBookmark() {

    event.preventDefault();


    var postUrl = 'http://localhost:8080/RESTJersey/rest/servis/id';


    var xhr = new XMLHttpRequest();
    xhr.open('POST', postUrl, true);
    


    var summary = document.getElementById('summary').value;
    
    var params = summary;
    

    params = params.replace(/%20/g, ' ');


    xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');


    xhr.onreadystatechange = function() { 

        if (xhr.readyState == 4) {
            statusDisplay.innerHTML = '';
            if (xhr.status == 200) {

                statusDisplay.innerHTML = 'Kaydedildi!';
                //window.setTimeout(window.close, 1000);
            } else {

                statusDisplay.innerHTML = 'Kaydedilme Htası!: ' + xhr.statusText;
            }
        }
    };


    xhr.send(params);
    statusDisplay.innerHTML = 'Kaydediliyor...';
	//document.getElementById('summary').innerText = "asfafs";
	
}

function addBookmark2()
{ 
	event.preventDefault();
	
	var xhttp = new XMLHttpRequest();
	xhttp.responseType = 'text';
	xhttp.onreadystatechange = function() {
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			var response = xhttp.response;
			document.getElementById('summary').innerText = response;
		}
	};
	xhttp.open("GET","http://localhost:8080/RESTJersey/rest/servis/string2",true);
	xhttp.send();
}

function addBookmark3()
{ 
	event.preventDefault();
	
	var xhttp = new XMLHttpRequest();
	xhttp.responseType = 'text';
	xhttp.onreadystatechange = function() {
		if (xhttp.readyState == 4 && xhttp.status == 200) {
			var response = xhttp.response;
			document.getElementById('summary').innerText = " En cok gecen kokler :"+response;
		}
	};
	xhttp.open("GET","http://localhost:8080/RESTJersey/rest/servis/tumtext",true);
	xhttp.send();

   
}



window.addEventListener('load', function(evt) {

    statusDisplay = document.getElementById('status-display');

    document.getElementById('addbookmark').addEventListener('submit', addBookmark);
	document.getElementById('addbookmark2').addEventListener('submit', addBookmark2);
	document.getElementById('addbookmark3').addEventListener('submit', addBookmark3);
    // Get the event page
    chrome.runtime.getBackgroundPage(function(eventPage) {

        eventPage.getPageDetails(onPageDetailsReceived);
    });
});