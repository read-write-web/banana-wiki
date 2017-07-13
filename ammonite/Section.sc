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
    val titleAndBody = head.split("\n").toList
    val title: String = titleAndBody.head
    val body: String = titleAndBody.drop(1).mkString("")
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

def ParseDocument(input: ammonite.ops.Path, output: ammonite.ops.Path){
  writeSubDirBasedWiki(markDownSplit(read! input), output)
}
