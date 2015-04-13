
// Provide a default path to dwr.engine
if (dwr == null) var dwr = {};
if (dwr.engine == null) dwr.engine = {};
if (DWREngine == null) var DWREngine = dwr.engine;

if (VisitManagerBean == null) var VisitManagerBean = {};
VisitManagerBean._path = '/cpsps/dwr';
VisitManagerBean.pingTest = function(callback) {
  dwr.engine._execute(VisitManagerBean._path, 'VisitManagerBean', 'pingTest', callback);
}
VisitManagerBean.increaseVisit = function(p0, callback) {
  dwr.engine._execute(VisitManagerBean._path, 'VisitManagerBean', 'increaseVisit', p0, callback);
}
VisitManagerBean.getMoreRead = function(p0, callback) {
  dwr.engine._execute(VisitManagerBean._path, 'VisitManagerBean', 'getMoreRead', p0, callback);
}
