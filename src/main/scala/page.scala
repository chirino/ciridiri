package ru.ciridiri

import java.io.File
import org.apache.commons.io.FileUtils._
import org.apache.commons.io.FilenameUtils
import ru.circumflex.core._
import java.util.regex.{Pattern}
import ru.circumflex.md.Markdown

class Page(val uri: String, var content: String) {
  val path = Page.pathFromUri(uri)
  val title = Page.findTitle(content)
  val cachedPath = path + ".html"

  def save() = {
    val file = new File(path)
    if (!file.exists)
      forceMkdir(new File(FilenameUtils.getFullPath(path)))
    writeStringToFile(file, content, "UTF-8")
  }

  def cache_!() = writeStringToFile(new File(cachedPath), Markdown(content), "UTF-8")

  def sweep_!() = new File(cachedPath).delete

  def toHtml() = {
    if (Page.caching_?) {
      val f = new File(path)
      val cf = new File(cachedPath)
      if (!cf.exists || f.lastModified > cf.lastModified)
        cache_!

      readFileToString(cf, "UTF-8")
    } else {
      Markdown(content)
    }
  }

}

object Page {
  var contentDir = Circumflex.cfg("ciridiri.contentRoot")
      .getOrElse("src/main/webapp/pages")
      .toString
  val sourceExt = ".md"
  val mdTitle = Pattern.compile("(^#{1,3}\\s*?([^#].*?)#*$)|(^ {0,3}(\\S.*?)\\n(?:=|-)+(?=\\n+|\\Z))",
    Pattern.MULTILINE)
  val password = Circumflex.cfg("ciridiri.password").getOrElse("pass")

  def caching_?(): Boolean = Circumflex.cfg("ciridiri.caching").getOrElse(true) match {
    case b: Boolean => b
    case s: String => s.toBoolean
    case _ => true
  }

  def pathFromUri(uri: String) = FilenameUtils
      .concat(contentDir, (FilenameUtils.separatorsToSystem(uri) + sourceExt)
      .replaceAll("^/", ""))

  def uriFromPath(path: String) = FilenameUtils
      .separatorsToUnix(new File(path)
      .getAbsolutePath
      .replaceAll("^" + new File(contentDir).getAbsolutePath, "")
      .replaceAll(sourceExt + "$", ""))

  def findByUri(uri: String): Option[Page] = {
    val file = new File(pathFromUri(uri))
    if (file.exists)
      Some(new Page(uri, readFileToString(file, "UTF-8").replaceAll("\\r\\n|\\r", "\n")))
    else None
  }

  def findByPath(path: String): Option[Page] = findByUri(uriFromPath(path))

  def findByUriOrEmpty(uri: String) = findByUri(uri).getOrElse(new Page(uri, ""))

  def findTitle(text: String) = {
    val m = mdTitle.matcher(text)
    if (m.find) {
      var result = m.group(2)
      if (result != null) result
      else m.group(4)
    }
    else ""
  }

}
