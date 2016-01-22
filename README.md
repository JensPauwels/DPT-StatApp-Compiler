DPT-StatApp-Compiler
====================

Target audience
---------------

This project aims to help webdevelopers with small to medium size projects who don't want
to set-up a node.js backend and install gulp, grunt or other similar tools just to make
a purely static HTML5 website. 

Even for small projects HTML partials would be great. At the moment only Ajax requests
with javascript code seem to be able to do this. (Except when you wan't to use horrible 
iframes off course). But all these extra requests can slow down a normally hyperfast
static HTML5 website significantly. 

The DPT-StatApp-Compiler is a standalone Java program that uses a standard webapp folder
structure. It precompiles partials and HTML pages into your webapp. 

How does it work?
-----------------

When you have installed the Java runtime on your development machine just make the 
folder where you wan't to develop your app in and type in the following command in 
a terminal:

```
java -jar DPT-StatApp-Compiler.jar generate <path/to/directory>
```

You can compile the JAR with JDK 7 or higher or use the precompiled JAR file in the dist
directory of this repository. 

The following folders will be generated:

 - app: this directory will contain the output after compiling. Just upload the contents
        of this directory to your static HTML webserver. 

 - css: place all your stylesheets in this directory
 
 - html: place every actual site page in this directory
 
 - images: drop all your images and sprites here
 
 - licences: a handy place to store all the licences of the libraries you are using
 
 - locales: when making an i18n site you can place locale files here
 
 - partial: place all partials here. For example menu, footer, ...
 
 - script: place all your scripts here
 
 
There are 3 lines of syntax you need to learn and they should only be used
between de normal HTML in the files you dropped in the html directory:
 
Adding a partial:
```
<- partial(filename.extension) ->
```

Including CSS styling:
```
<- style(filename.extension) ->
```

Including scripts:
```
<- script(filename.extension, <number determining the include order>) ->
```

The compiler whill automatically determine which scripts and CSS files are equal
for all HTML files and they will be combined into one file. This speeds up the
website even more. 

As the order of javascript includes is very important a number including 
the order is required. This way you can tell the compiler which script file it
should place first in the combined script file. The lowest number is placed
first. If the compiler encounters different sort orders in multiple files
it will pick the lowest encountered order.

When you are ready run the following command:

```
java -jar DPT-StatApp-Compiler.jar compile <path/to/directory>
```

You can now find your app in the 'app' directory. 

Todo
----

 - Include minify option
 - Include clean option
 - Code cleanup
 
Disclaimer
----------

As we needed this project in a rush for internal development the code may feel
a bit rough at the edges. Please feel free to contribute if you have any improvements. 