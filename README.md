## Greenfield Online Next Generation

This repository contains the Greenfield Online Next Generation (GONG) project.
Read more on the [GONG confluence space](http://support.atex.com/confluence/display/GONG11 "GONG confluence space").

### Branches

The GONG repository contains release branches. These are prefixed with 'RELEASE' and represent released versions of GONG. Code on these branches depends on released versions of Polopoly.

### Issue handling

All GONG issue and support handling is performed in the [GONG JIRA project](http://support.polopoly.com/jira/browse/GONG "Greenfield Online Next Generation") on the support site.

### Contact

For information on the GONG repository, contact Polopoly Support at support.polopoly@atex.com.

## Pull requests

Pull requests to the GONG repository is a good way to contribute changes and enhancements. However, please follow these guidelines when making changes to GONG that you want to have committed back to the master branch:

### 1. Fork the repository

Fork the GONG repository at either `https://github.com/atex-polopoly/gong` or `git@github.com:atex-polopoly/gong.git` (the latter one require that you've set up your GitHub SSH keys).

### 2. Make a branch for the fix

In order to not pollute your entire GONG fork for this specific fix, make a new branch for it.

This fix-branch should be based on the *latest* release-branch available in GONG (such as RELEASE-1.0).

For example, if you want to fix a bug in the article and the latest release-branch is RELEASE-1.0:

```
git push origin RELEASE-1.0:article-bug-fix
git checkout -t origin/article-bug-fix
```

### 3. Make your changes

Make your changes to GONG on the branch you now have checked out. When you are finished, make sure that everything builds correctly and that all tests pass. Make sure your changes are committed.
 
### 4. Create a JIRA issue

When you are ready to make a pull request, create a JIRA issue for it in the [GONG JIRA project](http://support.polopoly.com/jira/browse/GONG "Greenfield Online Next Generation"). It doesn't matter very much what you name the issue, just make sure to describe that you want to make a pull request. Don't set any fix version or component for the issue. Assign the issue to yourself if possible.

### 5. Prepare the pull request

Create a new branch based on the master branch, and cherry-pick over the changes you want to be included:

```
# The following will make sure the master branch of your fork is up to date. This is important because
# we want all pull requests to be made from the master branch, and we want you to solve any merge
# conflicts against the latest state of the master branch.

git remote add GONG https://github.com/atex-polopoly/gong.git

git fetch GONG

git checkout master
git merge GONG/master

git push

# The following will create a branch 'pull-request-13' (in your forked repository) based on the now up to
# date master branch.
 
git push origin master:pull-request-13

git checkout -t origin/pull-request-13

# The following replicates your changes to the pull request branch (which is based on the master branch)
# and commits them as a single changeset.

git cherry-pick [hash #1] -n
git cherry-pick [hash #2] -n
...
git cherry-pick [hash #n] -n
 
git commit -m "GONG-YYY: The Article bug has been fixed."
 
git push
```

### 6. Make the pull request

Now make the pull request on GitHub. Make sure the pull request is FROM [myaccount]:gong:[branch] TO atex-polopoly:gong:master.

### What happens now?

Given that the changes contained in the pull request are deemed to be good, what will now happen (on the Polopoly Products side) is:

#### 1. Code review

Everything changed in the pull request will be reviewed. Minor errors will be fixed, but anything major will be reported back in the pull request discussion instead. It will then have to be fixed by the person making the pull request.

#### 2. Test jobs

Everything changed in the pull request will be run through test jobs, ensuring nothing obvious is broken. Minor errors will be fixed, but anything major will be reported back in the pull request discussion instead. It will then have to be fixed by the person making the pull request.

#### 3. Merge to master

If everything in (1) and (2) works out, the pull request will be merged to the master GONG repository.

#### 4. Closed pull request

The pull request will be closed with information on when and where the changes of the pull request will appear. Note that due to technical details concerning how the GONG repository is currently set up, the actual merge to the master branch is performed "behind the scenes" by Polopoly Products. The pull request will not be "accepted", but instead closed with information on where and in which GONG version the changes will appear.

## Code Status
The code in this repository is provided with the following status: **EXAMPLE**.

Under the open source initiative, Atex provides source code for plugin with different levels of support. There are three different levels of support used. These are:

- EXAMPLE
The code is provided as an illustration of a pattern or blueprint for how to use a specific feature. Code provided as is.

- PROJECT
The code has been identified in an implementation project to be generic enough to be useful also in other projects. This means that it has actually been used in production somewhere, but it comes "as is", with no support attached. The idea is to promote code reuse and to provide a convenient starting point for customization if needed.

- PRODUCT
The code is provided with full product support, just as the core Polopoly product itself.
If you modify the code (outside of configuraton files), the support is voided.

## License
Atex Polopoly Source Code License
Version 1.0 February 2012

See file **LICENSE** for details.

