
// Provide a default path to dwr.engine
if (dwr == null) var dwr = {};
if (dwr.engine == null) dwr.engine = {};
if (DWREngine == null) var DWREngine = dwr.engine;

if (VotingManagerBean == null) var VotingManagerBean = {};
VotingManagerBean._path = '/cpsps/dwr';
VotingManagerBean.vote = function(p0, p1, callback) {
  dwr.engine._execute(VotingManagerBean._path, 'VotingManagerBean', 'vote', p0, p1, callback);
}
VotingManagerBean.searchMostVoted = function(callback) {
  dwr.engine._execute(VotingManagerBean._path, 'VotingManagerBean', 'searchMostVoted', callback);
}
VotingManagerBean.isVotable = function(p0, callback) {
  dwr.engine._execute(VotingManagerBean._path, 'VotingManagerBean', 'isVotable', p0, callback);
}
VotingManagerBean.getAverageVote = function(p0, callback) {
  dwr.engine._execute(VotingManagerBean._path, 'VotingManagerBean', 'getAverageVote', p0, callback);
}
VotingManagerBean.getAverageAndAlreadyVoted = function(p0, callback) {
  dwr.engine._execute(VotingManagerBean._path, 'VotingManagerBean', 'getAverageAndAlreadyVoted', p0, callback);
}
VotingManagerBean.isAlreadyVoted = function(p0, callback) {
  dwr.engine._execute(VotingManagerBean._path, 'VotingManagerBean', 'isAlreadyVoted', p0, callback);
}
