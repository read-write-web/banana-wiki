How to start geeking
--------------------

You only need a recent version of Java, that's all:

``` bash
$ git clone git@github.com:w3c/banana-rdf.git
$ cd banana-rdf
$ sbt
```

It's also easy to just build specific target platforms:
    
``` bash
$ sbt +banana_js/test    # for javascript only 
$ sbt +banana_jvm/test   # for jvm only
```

( note: scala-js compilation uses more memory. see [travis.yml](.travis.yml) )

IDE Setup
=========

`banana-rdf` works with [eclipse](https://www.eclipse.org/) , [IntelliJ IDEA](http://www.jetbrains.com/idea/) and other IDEs.

Eclipse
-------
Eclipse should work "out of the box" with the addition of the following global settings to global.sbt:

In `~/.sbt/0.13/global.sbt` (global sbt location in Linux and MacOS, in Windows it is different):

```
    unmanagedSourceDirectories in Compile ~= { _.filter(_.exists) }
```

In `~/.sbt/0.13/plugins/build.sbt`

```
    addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")
```

To generate eclipse project files, just run the command:

``` bash
$ sbt eclipse
```

IntelliJ IDEA
-------------

IntelliJ IDEA can open sbt projects directly. There is an annoying bug that in some cases it does not show all the subprojects after import ( to overcome this just go to SBT tool window and refresh the project). 