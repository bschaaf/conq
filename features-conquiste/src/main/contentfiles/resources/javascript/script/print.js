// script stampa pagina
function stampaPagina(){
   bV = parseInt(navigator.appVersion)
   if (bV >= 4) window.print()
}

function funzioniPagina(langId, stampa_title, stampa_value, inviaa_title,inviaa_value, torna_title, torna_value){
	var url = location.href;
	document.write('<ul id="funzionipagina"><li class=\"stampa\"><a href="javascript:stampaPagina()" title="'+stampa_title+'" id="stampa">'+stampa_value+'</a></li>');
	document.write('<li class=\"inviapagina\"><a href="/cpsps/inviaa/index.jsp?langId='+langId+'" title="'+inviaa_title+'" id="inviapagina">'+inviaa_value+'</a></li>');
	document.write('<li class=\"su\"><a href="#testata" id="su" title="'+torna_title+'">'+torna_value+'</a></li></ul>');
		
}
