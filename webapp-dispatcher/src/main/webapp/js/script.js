$(document).ready(function(){
    /*CALENDAR*/
    $.datepicker.setDefaults($.datepicker.regional['it']);
    $( ".dataPubblicazione" ).datepicker();
/* SLIDER EDICOLA */   
    $(".buttonBackSlider").click(function(){
    var figlio=$(this).parent().children(".contPaper");
    var width=figlio.children("ul").children("li").width();
    figlio.scrollTo({top:'+=0', left:'+='+(width+30)}, 800);
	});
	$(".buttonNextSlider").click(function(){
		var figlio=$(this).parent().children(".contPaper");
		var width=figlio.children("ul").children("li").width();
		figlio.scrollTo({top:'+=0', left:'-='+(width+30)}, 800);
	});

	$(".contPaper").width(($('.edicolaSlider').innerWidth()-140));
	$(".contPaper").each(function(){
		var width=0;
		$(this).children("ul").children('li').each(function(){
			width+=$(this).width();
		});
		$(this).children("ul").css("width",(width-40)+"px");
	});

	$(window).resize(function(){
		$(".contPaper").width(($('.edicolaSlider').innerWidth()-140));
	});

/* SLIDER HOMEPAGE */   
	$(".buttonBackSlider").click(function(){
		var figlio=$(this).parent().children(".contBlogger");
		var width=figlio.children("ul").children("li").width();
		figlio.scrollTo({top:'+=0', left:'+='+width}, 800);
	});
	$(".buttonNextSlider").click(function(){
		var figlio=$(this).parent().children(".contBlogger");
		var width=figlio.children("ul").children("li").width();
		figlio.scrollTo({top:'+=0', left:'-='+width}, 800);
	});
	$(".contBlogger").width(($('.topicSlider').innerWidth()-120));
	$(".contBlogger").each(function(){
		var width=0;
		$(this).children("ul").children('li').each(function(){
			width+=$(this).width();
		});
		$(this).children("ul").css("width",(width+90)+"px");
	});
	$(window).resize(function(){
		$(".contBlogger").width(($('.topicSlider').innerWidth()-120));
	});
	
/* MARQUEE */   

	/* Breaking News */ 
	$(".contShotNews").marquee({
		speed:10000,
		direction:"left",
        pauseOnHover: true
	});

	/*In breve */
	$(".scrollVertical").marquee({
		direction: 'up',
		speed: 8000,
        pauseOnHover: true
	});

/* MANAGE STARS */  

	$('.bloccostelle').raty({
		starOff : '/img/icon/starEmpty.png',
		starOn  : '/img/icon/starHover.png',
		click: function(score, evt) {
			console.log(score);
			$(".grazie").show();
		}

	});

/* SLIDER HOMEPAGE */    

	function callRefreshSlider(){
            if((parseInt($(".page").html()*1)+1)>parseInt($(".totalPage").html())){
                    $(".page").html("1");
            }else{
                    $(".page").html(parseInt($(".page").html()*1)+1);
            }
            var imgUrl=$(".contentPreviewSlide div.column:first").children("input.imageUrl").val();
            $(".leadTitletop").html($(".contentPreviewSlide div.column:first").children("input.leadTitle").val());
            $(".urlLink").attr("href",$(".contentPreviewSlide div.column:first").children("input.linkhidden").val());
            $(".imgSlider").css("background-image","url('"+imgUrl+"')");

            if($(".contentPreviewSlide div.column:first").children("input.author").val()!=""){
                $(".zoneAuthor").parent().show();
                $(".zoneAuthor").html($(".contentPreviewSlide div.column:first").children("input.author").val());
            }else{
                $(".zoneAuthor").parent().hide();
            }

            $(".titleThree").html($(".contentPreviewSlide div.column:first").children("input.title").val());
            $(".siparietto p").html($(".contentPreviewSlide div.column:first").children("input.testo").val());
            $(".contentPreviewSlide").each(function(){
                    $htmlFirst=$(this).children("div.column:first").clone();
                    $(this).children("div.column:first").remove();
                    $(this).append($htmlFirst);
            });
	}



	var myTimer=setInterval(callRefreshSlider,8000);

 	/* Initial slider setting */

    var imgUrl=$(".contentPreviewSlide div.column:first").children("input.imageUrl").val();
	$(".imgSlider").css("background-image","url('"+imgUrl+"')");
    $(".contentPreviewSlide div.column:last-child").show();
    $(".leadTitletop").html($(".contentPreviewSlide div.column:first").children("input.leadTitle").val());
    $(".urlLink").attr("href",$(".contentPreviewSlide div.column:first").children("input.linkhidden").val());
    if($(".contentPreviewSlide div.column:first").children("input.author").val()!=""){
        $(".zoneAuthor").parent().show();
        $(".zoneAuthor").html($(".contentPreviewSlide div.column:first").children("input.author").val());
	}else{
        $(".zoneAuthor").parent().hide();
    }
    $(".totalPage").html($(".contentPreviewSlide div").length);
    $(".page").html("1");
	$(".zoneAuthor").html($(".contentPreviewSlide div.column:first").children("input.author").val());
	$(".titleThree").html($(".contentPreviewSlide div.column:first").children("input.title").val());
    $(".siparietto p").html($(".contentPreviewSlide div.column:first").children("input.testo").val());
	$(".sipariettoGallery p").html($(".contentPreviewSlide div.column:first").children("input.testoGallery").val());
	$(".contentPreviewSlide").each(function(){
		$htmlFirst=$(this).children("div.column:first").clone();
		$(this).children("div.column:last-child").show();
		$(this).children("div.column:first").remove();
		$(this).append($htmlFirst);
		$(this).children("div.column:last-child").hide();
	});

	/* Click Dx arrow */
	$(".backSliderHome").click(function(e){
		if((parseInt($(".page").html()*1)+1)>parseInt($(".totalPage").html())){
			$(".page").html("1");
		}else{
			$(".page").html(parseInt($(".page").html()*1)+1);
		}
                var imgUrl=$(".contentPreviewSlide div.column:first").children("input.imageUrl").val();
                $(".leadTitletop").html($(".contentPreviewSlide div.column:first").children("input.leadTitle").val());
                $(".urlLink").attr("href",$(".contentPreviewSlide div.column:first").children("input.linkhidden").val());
                $(".imgSlider").css("background-image","url('"+imgUrl+"')");

                if($(".contentPreviewSlide div.column:first").children("input.author").val()!=""){
                    $(".zoneAuthor").parent().show();
                    $(".zoneAuthor").html($(".contentPreviewSlide div.column:first").children("input.author").val());
                }else{
                    $(".zoneAuthor").parent().hide();
                }

                $(".titleThree").html($(".contentPreviewSlide div.column:first").children("input.title").val());
                $(".siparietto p").html($(".contentPreviewSlide div.column:first").children("input.testo").val());
				$(".sipariettoGallery p").html($(".contentPreviewSlide div.column:first").children("input.testoGallery").val());
                $(".contentPreviewSlide").each(function(){
                        $htmlFirst=$(this).children("div.column:first").clone();
                        $(this).children("div.column:first").remove();
                        $(this).append($htmlFirst);
                });
		clearInterval(myTimer);
		myTimer=setInterval(callRefreshSlider,8000);
                e.stopPropagation();
	});

	/* Click Sx arrow */
	$(".nextSliderHome").click(function(e){
		if((parseInt($(".page").html()*1)-1)<1){
			$(".page").html($(".totalPage").html());
		}else{
			$(".page").html(parseInt($(".page").html()*1)-1);
		}
        var imgUrl=$(".contentPreviewSlide div.column:nth-child(2)").children("input.imageUrl").val();
        $(".leadTitletop").html($(".contentPreviewSlide div.column:nth-child(2)").children("input.leadTitle").val());
        $(".urlLink").attr("href",$(".contentPreviewSlide div.column:nth-child(2)").children("input.linkhidden").val());
        $(".imgSlider").css("background-image","url('"+imgUrl+"')");   
        if($(".contentPreviewSlide div.column:nth-child(2)").children("input.author").val()!=""){
            $(".zoneAuthor").parent().show();
            $(".zoneAuthor").html($(".contentPreviewSlide div.column:nth-child(2)").children("input.author").val());
        }else{
            $(".zoneAuthor").parent().hide();
        }
		$(".titleThree").html($(".contentPreviewSlide div.column:nth-child(2)").children("input.title").val());
        $(".siparietto p").html($(".contentPreviewSlide div.column:nth-child(2)").children("input.testo").val());
		$(".sipariettoGallery p").html($(".contentPreviewSlide div.column:nth-child(2)").children("input.testoGallery").val());
		$(".contentPreviewSlide").each(function(){
			$htmlFirst=$(this).children("div.column:nth-child(2)").clone();
			$htmlFirstTwo=$(this).children("div.column:first").clone();
			$(this).children("div.column:nth-child(2)").remove();
			$(this).children("div.column:first").remove();
			$(this).append($htmlFirstTwo);
			$(this).append($htmlFirst);
		});
		clearInterval(myTimer);
		myTimer=setInterval(callRefreshSlider,8000);
                e.stopPropagation();
	});

	/* Click image redirect to page */
    $(".imgSlider").click(function(){
		if($(".urlLink").attr("href")!="#"){
			window.location=$(".urlLink").attr("href");
		}
        return false;
    });
    $(".smartNav").on("click",function(){
		$(".tendina").toggleClass("apri");
        
    });
    $(".closeMenu").on("click",function(){
        $(".tendina").removeClass("apri");
    });


    
    

});