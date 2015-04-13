Atex.namespace("Atex.plugin.search");

(function(global) {
    "use strict";

    Atex.plugin.search = (function () {

        var url = location.href;

        function endsWith(str, suffix) {
            return str.indexOf(suffix, str.length - suffix.length) !== -1;
        }

        function doInit() {
          if (endsWith(url, '#')) {
            url = url.substring(0, url.length - 1);
          }
        }

        function filterSearch(fq) {
          url += "&fq=" + encodeURIComponent(fq);
          location.href = url;
        }

        function removeFilterSearch(fq) {
          url = url.replace("&fq=" + encodeURIComponent(fq), "");
          location.href = url;
        }

        function goToPage(pagenr) {
          url = url.replace(/&page=\d+/, '');
          url += '&page=' + encodeURIComponent(pagenr);
          location.href = url;
        }

        // Public interface
        return {
            doInit:     doInit,
            filterSearch: filterSearch,
            removeFilterSearch: removeFilterSearch,
            goToPage: goToPage,
        };

    }());

    Atex.plugin.search.doInit();

}(this));
