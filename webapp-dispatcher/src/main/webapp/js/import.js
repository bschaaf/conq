//IMPORT ID FOOTER
var link = document.querySelector('link[rel=import]');
var content = link.import.querySelector('#blog-post');
document.body.appendChild(document.importNode(content, true));
