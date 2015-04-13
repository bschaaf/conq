///////////////VOTO//////////
	var nomeImgStellaSI = "/img/stella_si.gif";
	var nomeImgStellaNO = "/img/stella_no.gif";
	var nomeImgStellaME = "/img/stella_mezza.gif";
	var maxStelle = 5;

	
	function checkDWR(){
		if(typeof(DWRUtil) == 'undefined') return false;
		if(typeof(VotingManagerBean) == 'undefined') return false;
		if(typeof(VisitManagerBean) == 'undefined') return false;		
		return true;
	}
	
	/**
	 * carica la media dei voti
	 * @return
	 */
	function getAverage(){
		  if(checkDWR() == false){
			  return;
		  }
		
		VotingManagerBean.getAverageAndAlreadyVoted(cpsId, function(data){	
			
			if (data !=null ){
				setStars (data[0], !data[1]);
			}
		});
	}

	/**
	 * crea l'html per visualizzare le stelle
	 * @param media
	 * @return
	 */
	function setStars(media, cliccabile) {
		 if(checkDWR() == false){
    		 return;
    	 } 
		stars =calcolaStelle(media);
		
		var h = "";
		var conta = 0;
		if (cliccabile) h+= "Vota:&nbsp;";
		for (var i =0; i< stars[0] ; i++){
			conta++;
			if (cliccabile) h+="<a href=\"javascript:sendVote("+conta+")\" title=\"vota "+conta+"\">";
			h+= "<img src='"+nomeImgStellaSI+"'  />";
			if (cliccabile) h+= "<\/a>";
			h+= "&nbsp;";
		}
		for (var i =0; i< stars[1] ; i++){
			conta++;
			if (cliccabile) h+= "<a href=\"javascript:sendVote("+conta+")\" title=\"vota "+conta+"\">";
			h+= "<img src='"+nomeImgStellaME+"'  />";
			if (cliccabile) h+= "<\/a>";
			h+= "&nbsp;";
		}
		for (var i =0; i< stars[2] ; i++){
			conta++;
			if (cliccabile) h+= "<a href=\"javascript:sendVote("+conta+")\" title=\"vota "+conta+"\">";
			h+= "<img src='"+nomeImgStellaNO+"'  />";
			if (cliccabile) h+= "<\/a>";
			h+= "&nbsp;";
		}	
		if (!cliccabile) h+= "&nbsp;(media: "+media+")";
		DWRUtil.byId("areavoto").innerHTML= h;		
			
	}


	
	 /**
	  * invia il voto dell'utente, e ritorna la nuova media del punteggio 
	  * aggiornando la visualizzazione delle stelle
	  * @param vote
	  * @return
	  */
    function sendVote(vote){
		  if(checkDWR() == false){
			  return;
		  } 
		  VotingManagerBean.vote(cpsId, vote, function (data){
		
			  setStars (data, false);
			});		
    }
    
    /**
     * calcola le stelle da visualizzare secondo i tipi
     * @param media
     * @return
     */
    function calcolaStelle (media){
   
		stelleSI = Math.floor(media);	
		stelleME = 0;
		var appo = Math.floor((media -Math.floor(media))*10 )		
		if (appo >= 5)
		{
			stelleME = 1;
		}
		stelleNO = maxStelle-stelleSI-stelleME;
		mediaS = [stelleSI ,  stelleME, stelleNO];
		return mediaS;	
	}
    
    
  
    /**
     * restituisce la lista delle entit� pi� votate
     */
    function getPiuVotate(nomeDiv){
    	  if(checkDWR() == false){
			  return;
		  } 

    	//controllo se esiste il div sulla pagina
    	if ( DWRUtil.byId(nomeDiv) != null ){
	    	VotingManagerBean.searchMostVoted(  function (lista){
	    	  	html = "";
				if (lista != null && lista.length >0){
	    			
	    			html += "<ol>";
	    			for (var i = 0; i< lista.length ; i++){
						var riga = lista[i];
						var voto 	= riga[0];
		            	var cpsid 	= riga[1];
		            	var title 	= riga[2];
		            	var path = riga[3];
		            	html += "<li><a href=\""+path+"\" title=\"vai alla notizia: "+title+"\" >"+title+"<\/a><\/li>";
	            	}	
	    			html += "<\/ol>";
				}
				DWRUtil.byId(nomeDiv).innerHTML= html;		
				
	    	});
    	
    	}
    }
    	
        /**
         * restituisce la lista delle entit� pi� votate.
         */
        
    	function getMoreVisited(divId, maxLength){
        	  if(checkDWR() == false){
    			  return;
    		  } 
    		//controllo se esiste il div sulla pagina
        	if ( DWRUtil.byId(divId) != null ){
        	VisitManagerBean.getMoreRead(maxLength, function (list){
            	html = "";
    			if (list != null && list.length >0){
        			
        			html += "<ol>";
        			for (var i = 0; i< list.length ; i++){
    					var riga = list[i];
    	            	var cpsid 	= riga[0];
    	            	var title 	= riga[1];
    	            	var path = riga[2];
    	            	html += "<li><a href=\""+path+"\" title=\"vai alla notizia: "+title+"\" >"+title+"<\/a><\/li>";
                	}	
        			html += "<\/ol>";
    			}
    			DWRUtil.byId(divId).innerHTML= html;		
    			
        	});
        	}
        }         
         
         function increaseVisit() {
        	 if(checkDWR() == false){
        		 return;
    		 }
        	 
        
        	 //controllo che se sono in staging non incremento il contatore 
        	 var nome_host=window.location.host;
        	 if(nome_host.toLowerCase().indexOf("staging")>=0){
        		 return;
        	 }

        	 if (cpsId != null){
	       	  	VisitManagerBean.increaseVisit(cpsId, function(data) {});
        	 }
       	}
    