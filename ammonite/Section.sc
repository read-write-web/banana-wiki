import ammonite.ops._

case class Wiki(title: String, body: String, val subsections: List[Wiki]) {
  lazy val fileName: String = title.trim().replaceAll("\\s+","_")
}

def markDownSplit(input: String, depth: Int = 0): Wiki = {
  def hashN: String = "#"*(depth + 1)

  def creatRegEx: String = "(?m)(?=^" + hashN + "[^#])"

  val wikiList = input.split(creatRegEx).toList
  val head::tail = wikiList
  if(head.charAt(0) == '#'){
    val splitpos = head.indexOf("\n")
    val (title, body) = if (splitpos<0) (head,"") else head.splitAt(splitpos)
    Wiki(title , body, tail.map(markDownSplit(_ , depth + 1)))
  }
  else{
      Wiki("" , head, tail.map(markDownSplit(_ , depth + 1)))
  }
}

def writeSubDirBasedWiki(wiki: Wiki, path: Path): Unit = {
  val PathForFile = if(wiki.title.isEmpty){
    RelPath("README.md")
  }
  else{
    RelPath(wiki.fileName + ".md")
  }

  write(path/PathForFile , wiki.title + "\n" + wiki.body)

  wiki.subsections.foreach { subWiki =>
      val dir = path/RelPath(subWiki.fileName)
      mkdir(dir)
      writeSubDirBasedWiki(subWiki, dir)
  }
}

def ParseDocument(inputFile: ammonite.ops.Path, outputDir: ammonite.ops.Path){
  writeSubDirBasedWiki(markDownSplit(read! inputFile), outputDir)
}
