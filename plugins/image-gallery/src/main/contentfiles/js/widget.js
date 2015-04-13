var jsImageDropTargetWidgetId;
var jsImageDropTargetWidgetFiles;

function JSImageDropTargetWidget() { }

JSImageDropTargetWidget.prototype.initSelf = function() {

    jsImageDropTargetWidgetId = this.initParams[0];

    var maxFile = this.initParams[1];
    var maxFileSize = this.initParams[2];
    var maxFileMsg = this.initParams[3];
    var maxFileSizeMsg = this.initParams[4];
    var maxFilesSizeMsg = this.initParams[5];
    var invalidContentTypeMsg = this.initParams[6];
    var invalidContentsTypeMsg = this.initParams[7];

    // does not support in IE yet
    if (jQuery.browser.msie) {
        $('.dnd').hide();
    }
    else {
        var error = false;
        var dropbox = $('#dropbox');

        dropbox.fileDrop({

            uploadFileList: function(num) {
                if(num > maxFile) {
                    errorHandler(maxFileMsg);
                    error = true;
                    return false;
                }
            },

            beforeEach: function(file, nof){
                if(!file.type.match(/^image\/|application\/zip|application\/x-zip-compressed/)){
                    if(nof > 1) {
                        errorHandler(updateMessage (invalidContentsTypeMsg, file.type));
                    }
                    else {
                        errorHandler(updateMessage (invalidContentTypeMsg, file.name, file.type));
                    }
                    error = true;
                    return false;
                }
                checkFileSize (file, nof);
            },

            uploadStarted:function(file){
                if(!error) {
                    jsImageDropTargetWidgetFiles = file;
                    uploadFiles(0);
                }
                else {
                    // reset
                    error = false;
                }
            }

        });

        function checkFileSize (file, nof) {
            var max = 1024 * 1024 * maxFileSize;
            if(file.size >  max) {
                if(nof > 1) {
                    errorHandler(maxFilesSizeMsg);
                }
                else {
                    errorHandler(updateMessage(maxFileSizeMsg, file.name));
                }
                error = true;
                return false;
            }
        }

    }
}

JSImageDropTargetWidget.prototype.callbackEvent = function (n) {
    uploadFiles (n);
}

function uploadFiles (index) {
    var files = jsImageDropTargetWidgetFiles;
    var i = index;

    if(i < files.length) {
        busyCursor(); // build-in general function
        $('img', $('#dropbox')).show(); // show spinner

        var ajaxEvents;
        var file = files[i];

        var reader = new FileReader();
        reader.onloadend = (function(file) {
            return function(e) {
                ajaxEvents = {
                    'javaClass': 'com.atex.plugins.widget.AjaxUploadEvent',
                    'fileName': file.name,
                    'fileData': btoa(e.target.result),
                    'index': i
                };
                AjaxEventController.trigger(jsImageDropTargetWidgetId, ajaxEvents);
            };
        })(file);
        reader.readAsBinaryString(file);
    }
    else {
        idleCursor(); // build-in general function
        $('img', $('#dropbox')).hide(); // hide spinner
    }
}


function errorHandler(exMsg) {
    var jsonMsg = {
        source: 'js',
        icon: 'error',
        iconClass: 'error',
        message: exMsg
    };
    Polopoly.ui.systemMessageBox.showSystemMessages(false, [jsonMsg]);
}

function updateMessage () {
    var args = Array.prototype.slice.call(arguments);
    var patternString = args.shift();

    return patternString.replace(/\{\{|\}\}|\{(\d+)\}/g, function (curlyBrack, index) {
        return ((curlyBrack == "{{") ? "{" : ((curlyBrack == "}}") ? "}" : args[index]));
    });
}