package ru.ciridiri

import ru.circumflex.core._
import ru.circumflex.scalate.Scalate._

class CiriDiri extends RequestRouter with AuthHelper {

  get("/") = redirect("/index.html")

  get("+.html") = Page.findByUri(uri(1)) match {
    case Some(page) =>
      onFound(page)
      "ciripage" := page
      render("/WEB-INF/ciridiri/page.jade")
    case None =>
      onNotFound()
      redirect(uri(0) + ".e")
  }

  get("+.md") = Page.findByUri(uri(1)) match {
    case Some(page) =>
      onFound(page)
      contentType("text/plain; charset=utf-8")
      page.content
    case _ =>
      onNotFound()
      error(404, "Page not found")
  }

  get("+.html.e") = {
    "ciripage" := Page.findByUriOrEmpty(uri(1))
    render("/WEB-INF/ciridiri/edit.jade")
  }

  get("/WEB-INF/scalate/errors/500.scaml") = render(uri(0))

  post("+.html") = {
    protected_!
    var page = Page.findByUriOrEmpty(uri(1))
    page.content = param('content)
    page.save
    redirect(uri(0))
  }

  // ## Callbacks

  // Callbacks can be used to run user code in response to various CiriDiri events.
  // Usage is simple:
  //
  //     class Main extends RequestRouter {
  //       // some routing...
  //       new CiriDiri {
  //         override def onFound(page: Page) = println("I found the page " + page)
  //       }
  //     }

  def onFound(page: Page): Unit = {}
  def onNotFound(): Unit = {}

}
