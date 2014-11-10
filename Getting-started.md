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

`banana-rdf` works with both [eclipse](https://www.eclipse.org/) and [IntelliJ IDEA](http://www.jetbrains.com/idea/).

global.sbt
----------
Independent of your preferred IDE, optionally the add the following line to `~/.sbt/0.13/global.sbt` to prevent the 
generation of empty source directories:

```
    unmanagedSourceDirectories in Compile ~= { _.filter(_.exists) }
```

Eclipse
-------
Eclipse should work "out of the box" with the addition of the following global settings:

In `~/.sbt/0.13/global.sbt`:

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

IntelliJ IDEA works with just one global change:

In `~/.sbt/0.13/plugins/build.sbt`

```
    addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")
```

To generate IntelliJ project files, just run the command:

``` bash
$ sbt gen-idea
```