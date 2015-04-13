RSS Feed Plugin
==============
Contains an RSS 2.0 feed element as a Polopoly plugin

## Polopoly Version
10.12.0

## How to use
Implement interface com.atex.plugins.rssfeed.Rssable on your articles.

This feed uses the new style images (using the image service) for feed images.

The feed has a content list that can take anything but only content implementing the interface mentioned above will be rendered. It does support multiple publishing queues as well.

## Code Status
The code in this repository is provided with the following status: **EXAMPLE**

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

See file **LICENSE** for details