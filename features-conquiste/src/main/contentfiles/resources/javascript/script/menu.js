/*
##############################################################################
#                               Eustema S.p.A.                               #
#                                js per CDL                                  #
#                          javascript per menu                               #
#                                (du from tn)                                #
##############################################################################
*/

/************** formatta la data ricavata da SSI ***************/

function cambiodata() {
	var hh, ii, gg, mm, aa;
	var strdata;
	var mesi = new Array('Gennaio','Febbraio','Marzo','Aprile','Maggio','Giugno','Luglio','Agosto','Settembre','Ottobre','Novembre','Dicembre');
	if (typeof serverdate=="string"){
		gg = serverdate.substr(0,2)*1;
		mm = mesi[((serverdate.substr(3,2)*1)-1)];
		aa = serverdate.substr(6,4);
		hh = serverdate.substr(11,2);
		ii = serverdate.substr(14,2);
		strdata =  gg+" "+mm+" "+aa+", ore "+hh+":"+ii;
		oD=document.getElementById('data')
		oD.innerHTML=strdata;
		}
}


/************* voce di menu di primo liv selezionata ********************/

var cl1 = "selezionato";

function selectedMenu(){
	if(typeof(channel)!="undefined"){
		oC = document.getElementById(channel);
		if (oC){
			if(typeof(subchannel)!="undefined"){
				oS = document.getElementById(subchannel);
				if (oS) {
					oS.className=cl1;
				}
			}
			//oC.parentNode.className=cl1;
			oC.className=oC.className+" "+cl1;
		}
	}
}

/*********** nasconde link archivio news box news **************/

function hideLinkArchivio(index){
	var linkObj = returnObjById("linkarchiviomese");
	if (index==0) {
		linkObj.className = "archiviomese";
	}
	else{
		linkObj.className = "hidden";
	}
}

/*********** link in nuova finestra ***********/

function nuovaFinestra() {
    if(!document.getElementById || !document.createTextNode){return;}
	var linkesterni = document.getElementsByTagName("a");
	for (var i=0; i < linkesterni.length; i++) {
	    if (linkesterni[i].getAttribute("rel") == "popup") {
		linkesterni[i].onclick = function() {
		    window.open(this.href);
		return false;
	    }
	}
    }
}

/********** get ID *****************/

function returnObjById(id){
        if (document.getElementById)
            var returnVar = document.getElementById(id);
        else if (document.all)
            var returnVar = document.all[id];
        else if (document.layers)
            var returnVar = document.layers[id];
        return returnVar;
}

/****************** inizializza funzioni al load della pagina ***********************************/

window.onload=function(){
	nuovaFinestra();
        // check for DOM
        //if(!document.getElementById || !document.createTextNode){return;}
        var MenuObj = returnObjById("menunavigazione");
        //alert(MenuObj);

        radice = MenuObj;
        selectedMenu(radice);
	cambiodata();
// 	alert(serverdate);
}

/******************************************************************/


