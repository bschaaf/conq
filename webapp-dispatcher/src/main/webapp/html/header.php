<html lang="it-IT">
<head>
    <meta charset="utf-8">
    <title> page.Layout </title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="shortcut icon" type="image/x-icon" href="/img/icon/favicon.ico" />
    <!--
        USE THE FOLLOWING ON POLOPOLY INSTEAD OF style.less:
        <link  rel="stylesheet" href="/css/catalog/style.css" >
    -->
    <script>localStorage.clear(); </script>
    <!-- <link  rel="stylesheet/less" type="text/css" href="/less/catalog/style.less" />
    <script src="/js/common/lib/vendor/less.min.js"></script> -->

    <!--  IN POLOPOLY REMOVE THE /built/ FOLDER IN THE FOLLOWING LINK  -->
    <!--[if lt IE 10]>

    <link  rel="stylesheet" href="/css/catalog/built/ie9style.css" >

    <![endif]-->
    
    <!-- CSS STYLE-->
    <link rel="stylesheet" href="/css/style.css" />
    <link rel="stylesheet" media=“print” href="/css/print.css" />
    <link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
    <link rel="stylesheet" href="/css/login.css" />
    <link rel="stylesheet" href="/css/img.css" />
    <script src="/js/jquery-1.11.2.min.js"></script>
    <script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
    <script src="/js/jquery.datepicker.it.js"></script>
    <script src="/js/jquery.scrollTo.min.js"></script>
    <script src="/js/jquery.marquee.min.js"></script>
    <script src="/js/jquery.raty.js"></script>
    <script src="/js/script.js"></script>
    
    <!-- FOUNDATIONG JS DEFAULT PAGES -->

    <script src="/js/placeholder.js"></script>
    
    <!-- end -->
    
    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
    <script>
        var bui = {
            config: {
                gigya   : { apikey: "xxxxxxxxxxx" },
                krux    : { confid: "yyyyy" },
                ga      : { trakerId: "UA-XXXXXX-X" },
                site    : { id: "2.1234", mainAlias: '' },
                google  : { apikey: ""}
            }
        };
    </script>
    <!--
        USE THE FOLLOWING SCRIPT TAG ON POLOPOLY:
        <script src="/js/catalog/main.catalog.js"></script>
    -->
    <!--=========================================-->
    <!-- DO NOT COMMIT THE FOLLOWING IN POLOPOLY -->
    <!--=========================================-->
    <script src="/js/common/lib/vendor/require.js"></script>
    <script src="/js/common/config.js"></script>
    <script src="/js/catalog/main.catalog.js"></script>
    <script>
        require(['lib/Cookie', 'lib/Functions'], function (Cookie, Functions) {
            var functions   = new Functions();
            var cookie      = new Cookie();
            var params      = functions.getQueryParams(window.location.search);
            if (params.loggedin != 'true') { return false }
            var cookieData  = functions.base64.encode("loginName=2.1234_test;cookieLoginName=2.1234_test;screenName=John Doe;memberId=2.1234_test");
            cookie.createCookie("2.1234.siteuser", cookieData);
        });

        require(['lib/Cookie', 'lib/Functions'], function (Cookie, Functions) {
            var functions   = new Functions();
            var cookie      = new Cookie();
            var params      = functions.getQueryParams(window.location.search);
            if (params.loggedin != 'false') { return false }
            cookie.eraseCookie("2.1234.siteuser");
        });
    </script>
    <!--=========================================-->
  <body>
        <!-- top navigation class, close the div at the end of footer   -->
        <div class="borderGray">
            <div class="row">
            <!-- container of the body site, the div is close at the end of footer   -->
                <div class="contTopNav">
                    <!-- date -->
                    <div class="large-6 medium-5 column">
                        <div class="date">
                            <p>Venerdì 6 febbraio 2015, ore 12,34</p>
                        </div>
                    </div>
                     <!-- Top Navigation -->
                    <div class="large-6 medium-12 small-12 column">
                        <div class="topNav">
                            <ul>
                                <li><a href="#" title="Chi siamo">Chi siamo</a></li>
                                <li style="border:0;"><a href="#" title="RSS">RSS</a></li>
                            </ul>
                        </div>
                        <div class="contResearch">
                        <form method="get">
                            <input class="ricerca" type="text"/>
                            <button type="submit">CERCA</button>
                        </form>
                        </div>
                        <div class="loginUser">
                            <a href="#" title="Login">Login</a>
                        </div>
                    </div>
                    <!-- Cancel the floating css rule -->
                    <div class="clear"></div>
                </div>
                <!-- Social -->
                <div class="large-1 medium-2 column noRight">
                   <div class="social">
                       <p>Seguici su</p>
                       <ul>
                           <li><a href="#" title="Facebook" target="_blank"><img src="/img/icon/fb.png" alt="Facebook Logo"/></a></li>
                           <li><a href="#" title="Twitter" target="_blank"><img src="/img/icon/twitter.png" alt="Twitter Logo"/></a></li>
                           <li><a href="#" title="Pinterest" target="_blank"><img src="/img/icon/pinterest.png" alt="Pinterest Logo"/></a></li>
                           <li><a href="#" title="Fr" target="_blank"><img src="/img/icon/fr.png" alt="Fr Logo"/></a></li>
                           <li><a href="#" title="Youtube" target="_blank"><img src="/img/icon/uTube.png" alt="Youtube Logo"/></a></li>
                           <li><a href="#" title="googlePlus" target="_blank"><img src="/img/icon/googlePlus.png" alt="googlePlus Logo"/></a></li>
                       </ul>
                   </div>
                </div>
                 <!-- title -->
                <div class="large-11 medium-12 column noLeft">
                    <div class="title">
                        <a href="#" title="CDL"><h1>Conquiste del Lavoro</h1></a>
                    </div>
                    <div class="contCsil">
                        <img src="/img/logoCisl.png">
                        <p>Quotidiano della CISL fondato nel 1948 da Giulio Pastore</p>
                    </div>
                </div>
            </div>
            <!-- NAVIGATION MENU -->
            <div class="row">
                <div class="large-12 column menuSmobile">
                    <!-- SMART MENU NAV - VISIBLE ELEMENTS ONLY FOR TABLET & SMARTPHONE DEVICES-->
                    <div class="contSmartNav">
                        <div class="openNav">
                            <span></span>
                        </div>
                        <div class="smartNav">
                            <a href="#">MENU DI NAVIGAZIONE </a>
                        </div>
                        <!-- Cancel the floating css rule -->
                        <div class="clear"></div>
                    </div>
                    <!-- END -->
                    <div class="nav tendina">
                        <ul>
                            <li class="closeMenu"><a href="#">&#9665Chiudi</a></li>
                            <li><a href="#" title="Sindacato">Sindacato</a></li>
                            <li><a href="#" title="Economia">Economia</a></li>
                            <li><a href="#" title="Vertenze">Vertenze</a></li>
                            <li><a href="#" title="Global">Global</a></li>
                            <li><a href="#" title="Glocal">Glocal</a></li>
                            <li><a href="#" title="Politica">Politica</a></li>
                            <li><a href="#" title="Dibattito">Dibattito</a></li>
                            <li><a href="#" title="Reportage">Reportage</a></li>
                            <li><a href="#" title="Social">Social</a></li>
                        </ul> 
                        <!-- Cancel the floating css rule -->
                        <div class="clear"></div>
                    </div>
                </div>
            </div>
            <!-- BREAKING NEWS -->
            <div class="row borderNews">
                <div class="large-2 medium-2 small-4 column breakingNews">
                    <img src="/img/breakingNews.png" alt="Breaking News">
                </div>
                <div class="large-10 medium-10 small-8 column">
                    <div class="contShotNews">
                        <ul>
                            <li><strong>12 : 45</strong> <a href="#" title="News">Ut enim ad minim veniam, quis nostrum exercitationem</a></li>
                            <li><strong>12 : 45</strong> <a href="#" title="News">Ullam corporis suscipit laboriosam</a></li>
                            <li><strong>12 : 45</strong> <a href="#" title="News"> Nisi ut aliquid ex</a></li>
                        </ul>
                    </div>
                </div>
            </div>