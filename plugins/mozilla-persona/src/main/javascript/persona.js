/*!
 *  Register callbacks for Mozilla Persona client
 */

(function($) {
    window.persona = {
        settings: {},

        doLogin: function() {
            navigator.id.request(this.settings);
        },

        doLogout: function() {
            $.ajax({
                type: 'POST',
                url: '/persona/logout',
                success: function(res, status, xhr) {
                    window.location.reload(true);
                },
                error: function(xhr, status, err) {
                    alert("Logout failure: " + err);
                }
            });
        }
    };

    function enterLoggedInState() {
        $('.mozilla-persona .loggedout').hide();
        $('.mozilla-persona .loggedin').show();
    }

    function enterLoggedOutState() {
        $('.mozilla-persona .loggedin').hide();
        $('.mozilla-persona .loggedout').show();
    }

    if($.cookie("accessToken")) {
        enterLoggedInState();
    } else {
        enterLoggedOutState();
    }

    /* Register callbacks */
    navigator.id.watch({
        onlogin: function(assertion) {
            $.ajax({
                type: 'POST',
                url: '/persona/login',
                data: {assertion: assertion},
                success: function(res, status, xhr) {
                    navigator.id.logout();
                    window.location.reload(true);
                },
                error: function(xhr, status, err) {
                    navigator.id.logout();
                    alert("Login failure: " + err);
                }
            });
        },
    
        onlogout: function() {
        }
    });
})(jQuery);