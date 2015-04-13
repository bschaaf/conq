/*
 * Modified base on jquery-filedrop version: 0.1.0
 *
 * Original Author: Weixi Yen
 * Copyright (c) 2010 Resopollution
 *
 * Original Project home:
 * http://www.github.com/weixiyen/jquery-filedrop
 *
 * Licensed under the MIT license:
 * http://www.opensource.org/licenses/mit-license.php
 */

(function($){

    jQuery.event.props.push("dataTransfer");
    var opts = {},
        default_opts = {
            data: {},
            drop: empty,
            dragEnter: empty,
            dragOver: empty,
            dragLeave: empty,
            docEnter: empty,
            docOver: empty,
            docLeave: empty,
            beforeEach: empty,
            afterAll: empty,
            uploadStarted: empty,
            uploadFileList: empty,
        },
        doc_leave_timer,
        stop_loop = false,
        files_count = 0,
        files;

    $.fn.fileDrop = function(options) {
        opts = $.extend( {}, default_opts, options );

        this.bind('drop', drop).bind('dragenter', dragEnter).bind('dragover', dragOver).bind('dragleave', dragLeave);
        $(this).bind('drop', docDrop).bind('dragenter', docEnter).bind('dragover', docOver).bind('dragleave', docLeave);
    };

    function drop(e) {
        opts.drop(e);
        files = e.dataTransfer.files;
        if (files === null || files === undefined) {
            return false;
        }

        files_count = files.length;
        upload();
        e.preventDefault();
        return false;
    }

    function upload() {
        stop_loop = false;

        opts.uploadFileList(files_count);

        for (var i=0; i<files_count; i++) {
            if (stop_loop) return false;
            if (beforeEach(files[i], files_count) != false) {
                if (i === files_count) return;
            }
        }

        opts.uploadStarted(files);
    }

    function beforeEach(file, nof) {
        return opts.beforeEach(file, nof);
    }

    function afterAll() {
        return opts.afterAll();
    }

    function dragEnter(e) {
        clearTimeout(doc_leave_timer);
        e.preventDefault();
        opts.dragEnter(e);
    }

    function dragOver(e) {
        clearTimeout(doc_leave_timer);
        e.preventDefault();
        opts.docOver(e);
        opts.dragOver(e);
    }

    function dragLeave(e) {
        clearTimeout(doc_leave_timer);
        opts.dragLeave(e);
        e.stopPropagation();
    }

    function docDrop(e) {
        e.preventDefault();
        opts.docLeave(e);
        return false;
    }

    function docEnter(e) {
        clearTimeout(doc_leave_timer);
        e.preventDefault();
        opts.docEnter(e);
        return false;
    }

    function docOver(e) {
        clearTimeout(doc_leave_timer);
        e.preventDefault();
        opts.docOver(e);
        return false;
    }

    function docLeave(e) {
        doc_leave_timer = setTimeout(function(){
            opts.docLeave(e);
        }, 200);
    }

    function empty(){}

    try {
        if (XMLHttpRequest.prototype.sendAsBinary) return;
        XMLHttpRequest.prototype.sendAsBinary = function(datastr) {
            function byteValue(x) {
                return x.charCodeAt(0) & 0xff;
            }
            var ords = Array.prototype.map.call(datastr, byteValue);
            var ui8a = new Uint8Array(ords);
            this.send(ui8a.buffer);
        }
    } catch(e) {}

})(jQuery);