/* = membership.js ========================================================== */
/* Defines basic functionality for use in personalization
 * The global object in charge, window.polopoly, contains functionality for
 * cookies, base64 encoding.
 * ========================================================================== */

if (polopoly) throw("Initialized twice");
var polopoly = {};

/* = Custom services ======================================================== */
/* Namespace to use for custom services. Services added to this namespace     */
/* have their init method called                                              */
polopoly.service = {};

/* = User preferences ======================================================= */
polopoly.user = {
       isLoggedIn: function() {
           return polopoly.cookie.get("accessToken") != null;
       }
};

/* = Cookie functions ======================================================= */
polopoly.cookie = {
        clear: function(name) {
            document.cookie=name + '= ; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/';
        },
        set: function(name, value) {
            document.cookie=name + '=' + value + '; expires=Fri, 01 Jan 2038 00:00:00 UTC; path=/';
        },
        setForSession: function(name, value) {
            document.cookie=name + '=' + value + '; expires=-1; path=/';
        },
        get: function(name) {
            return polopoly.util.stringAsHashValue(name, document.cookie, ';');
        }
 };


/* = General utilities =======================================================*/
polopoly.util = {

    stringAsHashValue: function(key, string, sep) {
        if (string && string != '') {
            var items = string.split(sep);
            for (var i = 0; i < items.length; i++) {
                var value = jQuery.trim(items[i]);
                if (value.substring(0, key.length + 1) == (key + '=')) {
                    return decodeURIComponent(value.substring(key.length + 1));
                }
            }
        }
    }
};

/* = Base64 encode/decode =================================================== */
polopoly.base64 = {

    _keyStr: "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",

        /* This code was written by Tyler Akins and has been placed in the
         * public domain.  It would be nice if you left this header intact.
         * Base64 code from Tyler Akins -- http://rumkin.com
         * Modded to handle input with stripped ending =.
         */

        //
        // Decode a string in utf8 encoded in base64 into a string.
        //
        stringDecode: function(input) {
            return this._utf8_decode(this.decode(input));
        },
        decode : function (input) {
            try {
                if (input.length==0) {
                    return "";
                }
            }
            catch (e) {
                return "";
            }

            // remove all characters that are not A-Z, a-z, 0-9, +, /, or =
            input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
            input = this._padIfNecessary(input);

            var output = "";
            var chr1, chr2, chr3;
            var enc1, enc2, enc3, enc4;

            var i = 0;
            do {
                enc1 = this._keyStr.indexOf(input.charAt(i++) || "=");
                enc2 = this._keyStr.indexOf(input.charAt(i++) || "=");
                enc3 = this._keyStr.indexOf(input.charAt(i++) || "=");
                enc4 = this._keyStr.indexOf(input.charAt(i++) || "=");

                chr1 = (enc1 << 2) | (enc2 >> 4);
                chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
                chr3 = ((enc3 & 3) << 6) | enc4;

                output = output + String.fromCharCode(chr1);

                if (enc3 != 64) {
                    output = output + String.fromCharCode(chr2);
                }
                if (enc4 != 64) {
                    output = output + String.fromCharCode(chr3);
                }
            } while (i < input.length);

            return output;
        },
        _padIfNecessary: function(input) {
            if ((input.length % 4) == 0) {
                return input;
            }

            var missingChars = (4 - (input.length % 4));
            for (var i = 0; i < missingChars; i++) {
                input += "=";
            }

            return input;
        },

        //
        // Encode a string into utf8 encoded bytes in a base64 string.
        //
        stringEncode: function(input) {
            return this.encode(this._utf8_encode(input));
        },
        encode : function (input) {
            var output = "";
            var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
            var i = 0;

            while (i < input.length) {

                chr1 = input.charCodeAt(i++);
                chr2 = input.charCodeAt(i++);
                chr3 = input.charCodeAt(i++);

                enc1 = chr1 >> 2;
                enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
                enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
                enc4 = chr3 & 63;

                if (isNaN(chr2)) {
                    enc3 = enc4 = 64;
                } else if (isNaN(chr3)) {
                    enc4 = 64;
                }

                output = output +
                this._keyStr.charAt(enc1) + this._keyStr.charAt(enc2) +
                this._keyStr.charAt(enc3) + this._keyStr.charAt(enc4);

            }

            return output;
        },
        _utf8_encode : function (string) {
            string = string.replace(/\r\n/g,"\n");
            var utftext = "";

            for (var n = 0; n < string.length; n++) {

                var c = string.charCodeAt(n);

                if (c < 128) {
                    utftext += String.fromCharCode(c);
                }
                else if((c > 127) && (c < 2048)) {
                    utftext += String.fromCharCode((c >> 6) | 192);
                    utftext += String.fromCharCode((c & 63) | 128);
                }
                else {
                    utftext += String.fromCharCode((c >> 12) | 224);
                    utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                    utftext += String.fromCharCode((c & 63) | 128);
                }

            }

            return utftext;
        },
        _utf8_decode: function(utftext) {
            var string = "";
            var i = 0;
            var c = 0;
            var c1 = 0;
            var c2 = 0;

            while ( i < utftext.length ) {
                c = utftext.charCodeAt(i);
                if (c < 128) {
                    string += String.fromCharCode(c);
                    i++;
                }
                else if((c > 191) && (c < 224)) {
                    c2 = utftext.charCodeAt(i+1);
                    string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                    i += 2;
                }
                else {
                    c2 = utftext.charCodeAt(i+1);
                    var c3 = utftext.charCodeAt(i+2);
                    string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                    i += 3;
                }
            }
            return string;
        }
 };

jQuery().ready(function() {
  return function(namespaces) {
    for (var i = 0; i < namespaces.length; i++) {
      for (key in namespaces[i]) {
        var obj = namespaces[i][key];
        if (typeof(obj["init"]) == "function") {
            obj.init();
        }
      }
    }
  }([polopoly, polopoly.service]);
});
